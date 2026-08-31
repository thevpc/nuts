package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.platform.NGpu;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.platform.NGpuUtils;
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
import java.util.function.Function;

/**
 * Discovers GPU devices on linux, as {@link NGpu} instances carrying the
 * capabilities declared by {@link NGpuUtils}.
 * <p>
 * Detection is layered by cost. The kernel exposes vendor, pci identity, bound
 * module, device count, model name and uuid as plain files, and those readings
 * are identical on every distribution because they are kernel interfaces rather
 * than a userspace layout. Only compute capability and NVIDIA memory amounts
 * require running {@code nvidia-smi}.
 * <p>
 * The identity of a device cannot change while the machine is up, so it is
 * probed once and cached. Memory amounts can and do change constantly, so they
 * are re-read on every call and never cached : a caller asking how much video
 * memory is free must not be handed a figure from process startup.
 * <p>
 * The filesystem root and the command runner are injectable, so that the whole
 * reading can be exercised against fixture directories rather than against the
 * machine the tests happen to run on.
 * <p>
 * Every probe is fail-safe, an unreadable or missing source degrades the
 * corresponding property to unknown and never propagates an error : a machine
 * with no GPU, a container without device passthrough and a card left to
 * {@code nouveau} are all normal outcomes rather than failures.
 */
public class NLinuxGpuProbe {

    /**
     * Kernel paths, relative to the probe root so that a fixture directory can
     * stand in for the machine. Resolving an absolute path against a root would
     * silently discard that root.
     */
    private static final String SYS_PCI_DEVICES = "sys/bus/pci/devices";
    private static final String PROC_NVIDIA_GPUS = "proc/driver/nvidia/gpus";
    private static final String SYS_NVIDIA_VERSION = "sys/module/nvidia/version";

    /**
     * Video memory amounts published by {@code amdgpu} on the pci device node,
     * in bytes. This is the same reading the {@code /sys/class/drm/cardN/device}
     * path exposes, that symbolic link resolving to the very same node.
     */
    private static final String SYS_VRAM_TOTAL = "mem_info_vram_total";
    private static final String SYS_VRAM_USED = "mem_info_vram_used";

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

    /**
     * Fields of the identity query, in the order the returned rows hold them.
     */
    private static final String[] SMI_IDENTITY_FIELDS = {
            "compute_cap",
            "pcie.link.gen.current",
            "pcie.link.gen.max",
            "pcie.link.width.current",
            "pcie.link.width.max"
    };

    /**
     * Fields of the memory query, in the order the returned rows hold them.
     */
    private static final String[] SMI_MEMORY_FIELDS = {
            "memory.total",
            "memory.used",
            "memory.free"
    };

    /**
     * The probe reading the real machine, running vendor tools as processes.
     */
    private static final NLinuxGpuProbe LOCAL =
            new NLinuxGpuProbe(Paths.get("/"), new Function<String[], String>() {
                @Override
                public String apply(String[] command) {
                    return exec(command);
                }
            });

    private final Path root;
    private final Function<String[], String> commandRunner;

    /**
     * volatile, the read below is a double checked lock and a plain field would
     * let a caller observe a partially published list.
     */
    private volatile List<Device> cachedDevices;

    private NLinuxGpuProbe(Path root, Function<String[], String> commandRunner) {
        this.root = root;
        this.commandRunner = commandRunner;
    }

    /**
     * Builds a probe reading an arbitrary root through an arbitrary command
     * runner, which is what makes the reading testable : a fixture directory
     * standing in for {@code /sys} and {@code /proc}, and a stub standing in for
     * the vendor tool.
     * <p>
     * The runner is handed the command and its arguments and returns the
     * standard output, or null when the tool is missing, fails or times out,
     * which is the contract {@link #exec(String...)} honours.
     *
     * @param root          directory standing in for the filesystem root
     * @param commandRunner runner invoking a command, null result meaning no answer
     * @return a probe reading that root
     */
    public static NLinuxGpuProbe of(Path root, Function<String[], String> commandRunner) {
        return new NLinuxGpuProbe(root, commandRunner);
    }

    /**
     * Identity of a device, everything about it that cannot change while the
     * machine is up and is therefore worth caching.
     */
    private static final class Device {

        final String pciBusId;
        final String modelName;
        final NGpuVendor vendor;
        final Map<String, String> capabilities;

        Device(String pciBusId, String modelName, NGpuVendor vendor, Map<String, String> capabilities) {
            this.pciBusId = pciBusId;
            this.modelName = modelName;
            this.vendor = vendor;
            this.capabilities = capabilities;
        }
    }

    /**
     * Probes the machine.
     * <p>
     * Device identity is cached, memory amounts are read afresh on every call,
     * so that the returned {@link NRam} describes the machine now rather than at
     * first probe.
     *
     * @return detected devices sorted by pci address, empty when none is found
     */
    public static List<NGpu> gpus() {
        if (NOsFamily.current() != NOsFamily.LINUX) {
            return Collections.emptyList();
        }
        return LOCAL.detect();
    }

    /**
     * Reads the devices under this probe's root. Unlike {@link #gpus()} it does
     * not require the host to be linux, the root being whatever it was built
     * with.
     *
     * @return detected devices sorted by pci address, empty when none is found
     */
    public List<NGpu> detect() {
        List<Device> devices = identities();
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, long[]> memory = readMemory(devices);
        List<NGpu> all = new ArrayList<>();
        for (Device device : devices) {
            long[] m = memory.get(device.pciBusId);
            NRam vram = m == null
                    ? new NRam(device.modelName, -1, -1, -1)
                    : new NRam(device.modelName, m[0], m[1], m[2]);
            all.add(new NGpu(device.modelName, vram, device.capabilities));
        }
        return Collections.unmodifiableList(all);
    }

    private List<Device> identities() {
        List<Device> c = cachedDevices;
        if (c == null) {
            synchronized (this) {
                c = cachedDevices;
                if (c == null) {
                    c = Collections.unmodifiableList(probeIdentities());
                    cachedDevices = c;
                }
            }
        }
        return c;
    }

    private List<Device> probeIdentities() {
        List<Device> all = new ArrayList<>();
        List<String[]> pciDevices = scanPciDisplayDevices();
        if (pciDevices.isEmpty()) {
            return all;
        }
        String nvidiaDriverVersion = readNvidiaDriverVersion();
        Map<String, String[]> smi = queryNvidiaSmiIdentity();
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
            put(caps, NGpuUtils.PCI_BUS_ID, pciBusId);
            put(caps, NGpuUtils.PCI_DEVICE_ID, pci[2]);
            put(caps, NGpuUtils.VENDOR, vendor.id());
            put(caps, NGpuUtils.DEVICE_TYPE, resolveDeviceType(vendor, pciBusId).id());
            put(caps, NGpuUtils.KERNEL_DRIVER, kernelDriver);

            String modelName = null;

            if (vendor == NGpuVendor.NVIDIA) {
                String[] info = readNvidiaProcInformation(pciBusId);
                modelName = info[0];
                put(caps, NGpuUtils.UUID, info[1]);
                if ("nvidia".equals(kernelDriver)) {
                    put(caps, NGpuUtils.DRIVER_VERSION, nvidiaDriverVersion);
                }
                String[] smiRow = smi.get(pciBusId);
                if (smiRow != null) {
                    // published in the major.minor form the vendor tool reports,
                    // matching the key the nvidia-smi based detection already fills
                    put(caps, NGpuUtils.COMPUTE_CAPABILITY, smiRow[0]);
                    put(caps, NGpuUtils.PCIE_GEN_CURRENT, smiRow[1]);
                    put(caps, NGpuUtils.PCIE_GEN_MAX, smiRow[2]);
                    put(caps, NGpuUtils.PCIE_WIDTH_CURRENT, smiRow[3]);
                    put(caps, NGpuUtils.PCIE_WIDTH_MAX, smiRow[4]);
                }
            }
            if (modelName == null) {
                modelName = vendor == NGpuVendor.UNKNOWN ? pciBusId : vendor.id();
            }
            // the table is keyed by model name alone, so it is readable whether
            // or not a vendor tool answered
            Double bandwidth = NEnvLocal.KNOWN_BANDWIDTH_GBPS.get(modelName);
            if (bandwidth != null) {
                put(caps, NGpuUtils.MEMORY_BANDWIDTH_GBPS, String.valueOf(bandwidth));
            }
            all.add(new Device(pciBusId, modelName, vendor, caps));
        }
        return all;
    }

    private static void put(Map<String, String> caps, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            caps.put(key, value.trim());
        }
    }

    // -------------------------------------------------------------------------
    // memory, re-read on every call
    // -------------------------------------------------------------------------

    /**
     * Reads the current memory of every device, as
     * {@code {total, used, free}} in bytes keyed by pci address.
     * <p>
     * {@code amdgpu} publishes its amounts as kernel files, so they are read
     * without a process. NVIDIA publishes none, so a vendor tool is run, once
     * for all devices and only when the machine actually holds an NVIDIA card.
     * Devices reporting nothing are absent from the result rather than present
     * with negative amounts.
     */
    private Map<String, long[]> readMemory(List<Device> devices) {
        Map<String, long[]> result = new LinkedHashMap<>();
        boolean anyNvidia = false;
        for (Device device : devices) {
            if (device.vendor == NGpuVendor.NVIDIA) {
                anyNvidia = true;
            } else {
                long[] vram = readSysfsVram(device.pciBusId);
                if (vram != null) {
                    result.put(device.pciBusId, vram);
                }
            }
        }
        if (anyNvidia) {
            for (Map.Entry<String, String[]> e : queryNvidiaSmi(SMI_MEMORY_FIELDS).entrySet()) {
                String[] row = e.getValue();
                long total = parseMemoryMib(row[0]);
                if (total >= 0) {
                    result.put(e.getKey(), new long[]{total, parseMemoryMib(row[1]), parseMemoryMib(row[2])});
                }
            }
        }
        return result;
    }

    /**
     * Reads video memory from the pci device node, which {@code amdgpu}
     * publishes in bytes. This is the only memory reading available without a
     * vendor tool, and it is what keeps an AMD only machine from reporting
     * unknown amounts.
     *
     * @param pciBusId pci address of the device, such as {@code 0000:03:00.0}
     * @return {@code {total, used, free}} in bytes, null when not published
     */
    private long[] readSysfsVram(String pciBusId) {
        Path deviceDir = root.resolve(SYS_PCI_DEVICES).resolve(pciBusId);
        long total = parseBytes(readFirstLine(deviceDir.resolve(SYS_VRAM_TOTAL)));
        if (total < 0) {
            return null;
        }
        long used = parseBytes(readFirstLine(deviceDir.resolve(SYS_VRAM_USED)));
        return new long[]{total, used, used >= 0 ? total - used : -1};
    }

    // -------------------------------------------------------------------------
    // kernel interfaces, identical on every distribution
    // -------------------------------------------------------------------------

    /**
     * Lists display controllers as {@code {pciBusId, vendorId, deviceId, kernelDriver}}.
     */
    private List<String[]> scanPciDisplayDevices() {
        List<String[]> result = new ArrayList<>();
        File[] entries = root.resolve(SYS_PCI_DEVICES).toFile().listFiles();
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
    private String[] readNvidiaProcInformation(String pciBusId) {
        String[] result = new String[]{null, null};
        String content = readFile(root.resolve(PROC_NVIDIA_GPUS).resolve(pciBusId).resolve("information"));
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

    private String readNvidiaDriverVersion() {
        return readFirstLine(root.resolve(SYS_NVIDIA_VERSION));
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
     * Queries the identity fields of every NVIDIA device at once.
     * <p>
     * The pcie fields are not understood by every driver, and nvidia-smi rejects
     * a whole query over one unknown field rather than skipping it. Losing the
     * compute capability, which every driver reports, to a pcie field an old one
     * does not would be the wrong trade, so the query is retried without them.
     *
     * @return map of pci address to a row holding {@link #SMI_IDENTITY_FIELDS}
     * in order, unreported fields being null
     */
    private Map<String, String[]> queryNvidiaSmiIdentity() {
        Map<String, String[]> full = queryNvidiaSmi(SMI_IDENTITY_FIELDS);
        if (!full.isEmpty()) {
            return full;
        }
        Map<String, String[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : queryNvidiaSmi(new String[]{"compute_cap"}).entrySet()) {
            String[] row = new String[SMI_IDENTITY_FIELDS.length];
            row[0] = e.getValue()[0];
            result.put(e.getKey(), row);
        }
        return result;
    }

    /**
     * Runs one nvidia-smi query for every device, keyed by normalized pci
     * address. Rows always hold one entry per requested field, in order, so that
     * a caller indexes them by field position no matter how many columns the
     * tool actually wrote.
     *
     * @param fields fields to query, {@code pci.bus_id} being added as the key
     * @return map of pci address to row, empty when the tool did not answer
     */
    private Map<String, String[]> queryNvidiaSmi(String[] fields) {
        Map<String, String[]> result = new LinkedHashMap<>();
        StringBuilder query = new StringBuilder("--query-gpu=pci.bus_id");
        for (String field : fields) {
            query.append(',').append(field);
        }
        String out = commandRunner.apply(new String[]{
                "nvidia-smi", query.toString(), "--format=csv,noheader,nounits"});
        if (out == null) {
            return result;
        }
        for (String line : out.split("\n")) {
            String[] cols = line.split(",");
            if (cols.length < 2) {
                continue;
            }
            String[] row = new String[fields.length];
            for (int i = 0; i < fields.length && i + 1 < cols.length; i++) {
                row[i] = cols[i + 1].trim();
            }
            result.put(normalizePciBusId(cols[0].trim()), row);
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

    private static long parseMemoryMib(String value) {
        long v = parseBytes(value);
        return v < 0 ? -1 : v * 1024L * 1024L;
    }

    private static long parseBytes(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value.trim());
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
