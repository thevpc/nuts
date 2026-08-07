package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.platform.NGpuDevice;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.platform.NGpuVendor;
import net.thevpc.nuts.platform.NOsFamily;

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
 * Discovers GPU devices on linux.
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

    private static List<NGpuDevice> cached;

    private NLinuxGpuProbe() {
    }

    /**
     * Probes the machine, caching the result. Memory amounts held by the
     * returned devices are the totals, free memory is volatile by nature and is
     * served by {@link #queryFreeMemoryBytes(String)} instead.
     *
     * @return detected devices sorted by pci address, empty when none is found
     */
    public static List<NGpuDevice> probe() {
        List<NGpuDevice> c = cached;
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

    private static List<NGpuDevice> probe0() {
        List<NGpuDevice> all = new ArrayList<>();
        if (NOsFamily.getCurrent() != NOsFamily.LINUX) {
            return all;
        }
        List<String[]> pciDevices = scanPciDisplayDevices();
        if (pciDevices.isEmpty()) {
            return all;
        }
        String nvidiaDriverVersion = readNvidiaDriverVersion();
        Map<String, String[]> smi = queryNvidiaSmi();
        for (String[] pci : pciDevices) {
            String pciBusId = pci[0];
            NGpuVendor vendor = NGpuVendor.ofPciVendorId(pci[1]);
            String pciDeviceId = pci[2];
            String kernelDriver = pci[3];

            String modelName = null;
            String uuid = null;
            String driverVersion = null;
            int computeCapability = -1;
            long totalMemoryBytes = -1;

            if (vendor == NGpuVendor.NVIDIA) {
                String[] info = readNvidiaProcInformation(pciBusId);
                modelName = info[0];
                uuid = info[1];
                if ("nvidia".equals(kernelDriver)) {
                    driverVersion = nvidiaDriverVersion;
                }
                String[] smiRow = smi.get(pciBusId);
                if (smiRow != null) {
                    computeCapability = parseComputeCapability(smiRow[0]);
                    totalMemoryBytes = parseMemoryMib(smiRow[1]);
                }
            }
            all.add(NGpuDevice.of(vendor, resolveDeviceType(vendor, pciBusId), pciBusId, pciDeviceId,
                    kernelDriver, modelName, uuid, computeCapability, totalMemoryBytes, driverVersion));
        }
        Collections.sort(all, new Comparator<NGpuDevice>() {
            @Override
            public int compare(NGpuDevice a, NGpuDevice b) {
                return String.valueOf(a.getPciBusId()).compareTo(String.valueOf(b.getPciBusId()));
            }
        });
        return all;
    }

    /**
     * Reads the currently free memory of a device. This value is volatile and is
     * deliberately never cached, it must not take part in dependency resolution.
     *
     * @param pciBusId pci address of the device, such as {@code 0000:01:00.0}
     * @return free memory in bytes, negative when unknown
     */
    public static long queryFreeMemoryBytes(String pciBusId) {
        if (pciBusId == null || NOsFamily.getCurrent() != NOsFamily.LINUX) {
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
     * @return map of pci address to {@code {computeCapability, totalMemoryMib}}
     */
    private static Map<String, String[]> queryNvidiaSmi() {
        Map<String, String[]> result = new LinkedHashMap<>();
        String out = exec("nvidia-smi", "--query-gpu=pci.bus_id,compute_cap,memory.total",
                "--format=csv,noheader,nounits");
        if (out == null) {
            return result;
        }
        for (String line : out.split("\n")) {
            String[] cols = line.split(",");
            if (cols.length >= 3) {
                result.put(normalizePciBusId(cols[0].trim()), new String[]{cols[1].trim(), cols[2].trim()});
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
     *
     * @return standard output, or null when the tool is missing, fails or times out
     */
    private static String exec(String... command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            process = pb.start();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream in = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    bos.write(buffer, 0, count);
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
