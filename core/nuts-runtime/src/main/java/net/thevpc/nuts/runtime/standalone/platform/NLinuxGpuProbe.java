package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.platform.NGpu;
import net.thevpc.nuts.platform.NGpuCapabilities;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.platform.NGpuVendor;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NRam;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Discovers GPU devices on linux, as {@link NGpu} instances carrying the
 * capabilities declared by {@link NGpuCapabilities}.
 * <p>
 * Detection is layered by cost. The kernel exposes vendor, pci identity, bound
 * module, device count, model name and uuid as plain files, and those readings
 * are identical on every distribution because they are kernel interfaces rather
 * than a userspace layout. Only compute capability and memory amounts require
 * running {@code nvidia-smi}, which is done once for all devices and cached.
 * <p>
 * Every probe is fail-safe, an unreadable or missing source degrades the
 * corresponding property to unknown and never propagates an error : a machine
 * with no GPU, a container without device passthrough and a card left to
 * {@code nouveau} are all normal outcomes rather than failures.
 */
public class NLinuxGpuProbe {

    private static final String SYS_PCI_DEVICES = "/sys/bus/pci/devices";
    private static final String PROC_NVIDIA_GPUS = "/proc/driver/nvidia/gpus";
    private static final String SYS_NVIDIA_VERSION = "/sys/module/nvidia/version";

    /**
     * Pci class prefix of display controllers, the kernel writes the class as a
     * six digits value such as {@code 0x030000}.
     */
    private static final String PCI_CLASS_DISPLAY_PREFIX = "0x03";

    private static final long NVIDIA_SMI_TIMEOUT_SECONDS = 5;

    /**
     * Upper bound on the output kept from a vendor tool. The queries issued here
     * yield one short line per device, so anything beyond this is a runaway tool
     * rather than a reading worth parsing.
     */
    private static final int MAX_OUTPUT_BYTES = 1024 * 1024;

    /**
     * Null device to discard a tool's stderr into. Detection only ever runs on
     * linux, where this path always exists.
     */
    private static final String NULL_DEVICE = "/dev/null";

    private static List<NGpu> cached;

    private NLinuxGpuProbe() {
    }

    /**
     * Probes the machine, caching the result.
     *
     * @return detected devices sorted by pci address, empty when none is found
     */
    public static List<NGpu> gpus() {
        List<NGpu> c = cached;
        if (c == null) {
            synchronized (NLinuxGpuProbe.class) {
                c = cached;
                if (c == null) {
                    c = Collections.unmodifiableList(probe0());
                    cached = c;
                }
            }
        }
        return c;
    }

    private static List<NGpu> probe0() {
        List<NGpu> all = new ArrayList<>();
        if (NOsFamily.current() != NOsFamily.LINUX) {
            return all;
        }
        List<String[]> pciDevices = scanPciDisplayDevices();
        if (pciDevices.isEmpty()) {
            return all;
        }
        String nvidiaDriverVersion = readNvidiaDriverVersion();
        Map<String, String[]> smi = queryNvidiaSmi();
        List<String[]> ordered = new ArrayList<>(pciDevices);
        Collections.sort(ordered, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return String.valueOf(a[0]).compareTo(String.valueOf(b[0]));
            }
        });
        for (String[] pci : ordered) {
            String pciBusId = pci[0];
            NGpuVendor vendor = NGpuVendor.ofPciVendorId(pci[1]);
            String kernelDriver = pci[3];

            Map<String, String> caps = new LinkedHashMap<>();
            put(caps, NGpuCapabilities.PCI_BUS_ID, pciBusId);
            put(caps, NGpuCapabilities.PCI_DEVICE_ID, pci[2]);
            put(caps, NGpuCapabilities.VENDOR, vendor.id());
            put(caps, NGpuCapabilities.DEVICE_TYPE, resolveDeviceType(vendor, pciBusId).id());
            put(caps, NGpuCapabilities.KERNEL_DRIVER, kernelDriver);

            String modelName = null;
            NRam vram = null;

            if (vendor == NGpuVendor.NVIDIA) {
                String[] info = readNvidiaProcInformation(pciBusId);
                modelName = info[0];
                put(caps, NGpuCapabilities.UUID, info[1]);
                if ("nvidia".equals(kernelDriver)) {
                    put(caps, NGpuCapabilities.DRIVER_VERSION, nvidiaDriverVersion);
                }
                String[] smiRow = smi.get(pciBusId);
                if (smiRow != null) {
                    // published in the major.minor form the vendor tool reports,
                    // matching the key the nvidia-smi based detection already fills
                    put(caps, NGpuCapabilities.COMPUTE_CAPABILITY, smiRow[0]);
                    long total = parseMemoryMib(smiRow[1]);
                    long used = parseMemoryMib(smiRow[2]);
                    long free = parseMemoryMib(smiRow[3]);
                    if (total >= 0) {
                        vram = new NRam(modelName == null ? pciBusId : modelName, total, used, free);
                    }
                }
            }
            if (modelName == null) {
                modelName = vendor == NGpuVendor.UNKNOWN ? pciBusId : vendor.id();
            }
            if (vram == null) {
                vram = new NRam(modelName, -1, -1, -1);
            }
            all.add(new NGpu(modelName, vram, caps));
        }
        return all;
    }

    private static void put(Map<String, String> caps, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            caps.put(key, value.trim());
        }
    }

    /**
     * Reads the currently free memory of a device, bypassing the cache.
     * <p>
     * {@link #gpus()} caches its reading, so the free amount it carries is a
     * snapshot taken at first probe. This reads a fresh value, and is the one to
     * use when the current amount actually matters.
     *
     * @param pciBusId pci address of the device, such as {@code 0000:01:00.0}
     * @return free memory in bytes, negative when unknown
     */
    public static long queryFreeMemoryBytes(String pciBusId) {
        if (pciBusId == null || NOsFamily.current() != NOsFamily.LINUX) {
            return -1;
        }
        String out = exec("nvidia-smi", "--query-gpu=pci.bus_id,memory.free", "--format=csv,noheader,nounits");
        if (out == null) {
            return -1;
        }
        for (String line : out.split("\n")) {
            String[] cols = line.split(",");
            if (cols.length >= 2 && pciBusId.equals(normalizePciBusId(cols[0].trim()))) {
                return parseMemoryMib(cols[1].trim());
            }
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // kernel interfaces, identical on every distribution
    // -------------------------------------------------------------------------

    /**
     * Lists display controllers as {@code {pciBusId, vendorId, deviceId, kernelDriver}}.
     */
    private static List<String[]> scanPciDisplayDevices() {
        List<String[]> result = new ArrayList<>();
        File[] entries = new File(SYS_PCI_DEVICES).listFiles();
        if (entries == null) {
            return result;
        }
        for (File entry : entries) {
            String pciClass = readFirstLine(entry.toPath().resolve("class"));
            if (pciClass == null || !pciClass.startsWith(PCI_CLASS_DISPLAY_PREFIX)) {
                continue;
            }
            String vendorId = readFirstLine(entry.toPath().resolve("vendor"));
            String deviceId = readFirstLine(entry.toPath().resolve("device"));
            result.add(new String[]{entry.getName(), vendorId, deviceId, readBoundKernelDriver(entry.toPath())});
        }
        return result;
    }

    /**
     * Resolves the kernel module bound to a device, which is the capability gate :
     * an NVIDIA card bound to {@code nouveau} has no CUDA capability at all.
     */
    private static String readBoundKernelDriver(Path deviceDir) {
        try {
            Path driver = deviceDir.resolve("driver");
            if (Files.exists(driver)) {
                return driver.toRealPath().getFileName().toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Reads model name and uuid from {@code /proc/driver/nvidia/gpus}, which the
     * proprietary module exposes as plain text and spares an extra process.
     *
     * @return {@code {modelName, uuid}}, entries being null when unavailable
     */
    private static String[] readNvidiaProcInformation(String pciBusId) {
        String[] result = new String[]{null, null};
        String content = readFile(Paths.get(PROC_NVIDIA_GPUS, pciBusId, "information"));
        if (content == null) {
            return result;
        }
        for (String line : content.split("\n")) {
            int i = line.indexOf(':');
            if (i < 0) {
                continue;
            }
            String key = line.substring(0, i).trim();
            String value = line.substring(i + 1).trim();
            if ("Model".equals(key)) {
                result[0] = value;
            } else if ("GPU UUID".equals(key)) {
                result[1] = value;
            }
        }
        return result;
    }

    private static String readNvidiaDriverVersion() {
        return readFirstLine(Paths.get(SYS_NVIDIA_VERSION));
    }

    /**
     * An integrated GPU sits on the root pci bus, a discrete one is reached
     * through a bridge and therefore reports a non zero bus number. NVIDIA never
     * ships an integrated part reachable this way.
     */
    private static NGpuDeviceType resolveDeviceType(NGpuVendor vendor, String pciBusId) {
        if (vendor == NGpuVendor.NVIDIA) {
            return NGpuDeviceType.DEDICATED_GPU;
        }
        if (pciBusId == null) {
            return NGpuDeviceType.UNKNOWN;
        }
        String[] parts = pciBusId.split(":");
        if (parts.length < 2) {
            return NGpuDeviceType.UNKNOWN;
        }
        return "00".equals(parts[1]) ? NGpuDeviceType.INTEGRATED_GPU : NGpuDeviceType.DEDICATED_GPU;
    }

    // -------------------------------------------------------------------------
    // vendor tooling, the only step requiring a process
    // -------------------------------------------------------------------------

    /**
     * Queries every NVIDIA device at once, keyed by normalized pci address.
     *
     * @return map of pci address to
     * {@code {computeCapability, totalMemoryMib, usedMemoryMib, freeMemoryMib}}
     */
    private static Map<String, String[]> queryNvidiaSmi() {
        Map<String, String[]> result = new LinkedHashMap<>();
        String out = exec("nvidia-smi",
                "--query-gpu=pci.bus_id,compute_cap,memory.total,memory.used,memory.free",
                "--format=csv,noheader,nounits");
        if (out == null) {
            return result;
        }
        for (String line : out.split("\n")) {
            String[] cols = line.split(",");
            if (cols.length >= 5) {
                result.put(normalizePciBusId(cols[0].trim()), new String[]{
                        cols[1].trim(), cols[2].trim(), cols[3].trim(), cols[4].trim()});
            }
        }
        return result;
    }

    /**
     * nvidia-smi writes the pci domain on eight digits where the kernel uses
     * four, so {@code 00000000:01:00.0} has to be reduced to {@code 0000:01:00.0}
     * before it can be matched against a sysfs entry.
     */
    private static String normalizePciBusId(String pciBusId) {
        if (pciBusId == null) {
            return null;
        }
        String s = pciBusId.trim().toLowerCase();
        int i = s.indexOf(':');
        if (i > 4) {
            s = s.substring(i - 4);
        }
        return s;
    }

    /**
     * Compute capability is reported as {@code major.minor} and is stored
     * encoded as {@code major * 100 + minor * 10}.
     */
    private static int parseComputeCapability(String value) {
        if (value == null) {
            return -1;
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0]) * 100 + Integer.parseInt(parts[1]) * 10;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static long parseMemoryMib(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value.trim()) * 1024L * 1024L;
        } catch (Exception ignored) {
            return -1;
        }
    }

    // -------------------------------------------------------------------------
    // fail-safe helpers, never throw
    // -------------------------------------------------------------------------

    private static String readFile(Path path) {
        try {
            if (!Files.isReadable(path)) {
                return null;
            }
            return new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String readFirstLine(Path path) {
        String content = readFile(path);
        if (content == null) {
            return null;
        }
        int i = content.indexOf('\n');
        String line = (i < 0 ? content : content.substring(0, i)).trim();
        return line.isEmpty() ? null : line;
    }

    /**
     * Runs a vendor tool, bounded in time so that a wedged driver cannot hang
     * the caller.
     * <p>
     * impl-note: stderr is redirected to the null device rather than left
     * unread. An unread stderr is not merely noise : once the tool fills the
     * pipe buffer it blocks on write and never closes stdout, so the read below
     * would never return and the timeout, which can only be applied after it,
     * would never be reached. Discarding at the os level also keeps stderr out
     * of the parsed output, which merging the two streams would not.
     *
     * @return standard output, or null when the tool is missing, fails or times out
     */
    private static String exec(String... command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectError(new File(NULL_DEVICE));
            process = pb.start();
            // nothing is ever written to the tool, closing spares it a wait
            process.getOutputStream().close();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream in = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    bos.write(buffer, 0, count);
                    if (bos.size() > MAX_OUTPUT_BYTES) {
                        // a tool this talkative is not the one we are parsing
                        process.destroyForcibly();
                        return null;
                    }
                }
            }
            if (!process.waitFor(NVIDIA_SMI_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            return new String(bos.toByteArray(), Charset.forName("UTF-8"));
        } catch (Exception ignored) {
            if (process != null) {
                process.destroyForcibly();
            }
            return null;
        }
    }
}
