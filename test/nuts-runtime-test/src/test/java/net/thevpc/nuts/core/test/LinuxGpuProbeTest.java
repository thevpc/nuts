package net.thevpc.nuts.core.test;

import net.thevpc.nuts.platform.NGpu;
import net.thevpc.nuts.platform.NGpuDeviceType;
import net.thevpc.nuts.platform.NGpuUtils;
import net.thevpc.nuts.platform.NGpuVendor;
import net.thevpc.nuts.runtime.standalone.platform.NLinuxGpuProbe;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Covers the linux probe itself, against fixture directories standing in for
 * {@code /sys} and {@code /proc} and a stub standing in for the vendor tool.
 * <p>
 * The suite in {@code GpuDetectionTest} exercises the capability bag over hand
 * built devices, which says nothing about the reading that fills it. This one
 * covers the reading : machines the developer's own hardware cannot represent,
 * an AMD card, a card left to the open source driver, a driver too old for the
 * pcie fields, and a container with no device at all.
 *
 * @author thevpc
 */
public class LinuxGpuProbeTest {

    private static final String NVIDIA_VENDOR = "0x10de";
    private static final String AMD_VENDOR = "0x1002";
    private static final String INTEL_VENDOR = "0x8086";
    private static final String DISPLAY_CLASS = "0x030000";

    // -------------------------------------------------------------------------
    // readings no vendor tool takes part in
    // -------------------------------------------------------------------------

    @Test
    public void testAmdVramIsReadFromSysfsWithoutAnyVendorTool(@TempDir Path root) throws IOException {
        // the regression this guards : an AMD only machine used to report
        // unknown memory, the probe answering before the sysfs read happened
        Path device = pciDevice(root, "0000:03:00.0", AMD_VENDOR, "0x744c");
        write(device.resolve("mem_info_vram_total"), "17163091968");
        write(device.resolve("mem_info_vram_used"), "1073741824");

        List<NGpu> gpus = probe(root, refuseEveryCommand()).detect();

        Assertions.assertEquals(1, gpus.size());
        NGpu amd = gpus.get(0);
        Assertions.assertEquals(NGpuVendor.AMD, NGpuUtils.vendor(amd));
        Assertions.assertEquals(17163091968L, amd.vram().total());
        Assertions.assertEquals(1073741824L, amd.vram().used());
        Assertions.assertEquals(17163091968L - 1073741824L, amd.vram().free());
    }

    @Test
    public void testVramStaysUnknownWhenSysfsPublishesNone(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:00:02.0", INTEL_VENDOR, "0xa78b");

        List<NGpu> gpus = probe(root, refuseEveryCommand()).detect();

        Assertions.assertEquals(1, gpus.size());
        Assertions.assertEquals(-1, gpus.get(0).vram().total());
        // an unknown amount must not be mistaken for a device that is absent
        Assertions.assertEquals(NGpuVendor.INTEL, NGpuUtils.vendor(gpus.get(0)));
    }

    @Test
    public void testIntegratedAndDedicatedAreToldApartByPciBus(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:00:02.0", INTEL_VENDOR, "0xa78b");
        pciDevice(root, "0000:03:00.0", AMD_VENDOR, "0x744c");

        List<NGpu> gpus = probe(root, refuseEveryCommand()).detect();

        Assertions.assertEquals(2, gpus.size());
        // sorted by pci address, so the root bus device comes first
        Assertions.assertEquals(NGpuDeviceType.INTEGRATED_GPU, NGpuUtils.deviceType(gpus.get(0)));
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, NGpuUtils.deviceType(gpus.get(1)));
    }

    @Test
    public void testNonDisplayPciDevicesAreIgnored(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:03:00.0", AMD_VENDOR, "0x744c");
        // a network controller, class 0x02, sitting next to the gpu
        Path nic = root.resolve("sys/bus/pci/devices").resolve("0000:04:00.0");
        Files.createDirectories(nic);
        write(nic.resolve("class"), "0x020000");
        write(nic.resolve("vendor"), "0x8086");
        write(nic.resolve("device"), "0x1572");

        Assertions.assertEquals(1, probe(root, refuseEveryCommand()).detect().size());
    }

    @Test
    public void testAMachineWithNoDisplayDeviceReportsNothing(@TempDir Path root) throws IOException {
        Files.createDirectories(root.resolve("sys/bus/pci/devices"));
        Assertions.assertTrue(probe(root, refuseEveryCommand()).detect().isEmpty());
    }

    @Test
    public void testAnUnreadableSysfsIsNotAFailure(@TempDir Path root) {
        // a container without the pci filesystem mounted at all
        Assertions.assertTrue(probe(root, refuseEveryCommand()).detect().isEmpty());
    }

    // -------------------------------------------------------------------------
    // the bound kernel module, which is the capability gate
    // -------------------------------------------------------------------------

    @Test
    public void testNvidiaCardOnTheProprietaryModuleIsComputeCapable(@TempDir Path root) throws IOException {
        Path device = pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        Assumptions.assumeTrue(bindDriver(device, "nvidia"), "symbolic links unsupported");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4050 Laptop GPU", "GPU-abcdef");
        write(root.resolve("sys/module/nvidia/version"), "580.159.03");

        NGpu gpu = probe(root, nvidiaSmi(6144, 512)).detect().get(0);

        Assertions.assertTrue(NGpuUtils.isComputeCapable(gpu));
        Assertions.assertEquals("NVIDIA GeForce RTX 4050 Laptop GPU", gpu.name());
        Assertions.assertEquals("580.159.03", gpu.capability(NGpuUtils.DRIVER_VERSION).get());
        Assertions.assertEquals("GPU-abcdef", gpu.capability(NGpuUtils.UUID).get());
        Assertions.assertEquals(890, NGpuUtils.computeCapability(gpu));
    }

    @Test
    public void testSameCardOnNouveauIsNotComputeCapable(@TempDir Path root) throws IOException {
        Path device = pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        Assumptions.assumeTrue(bindDriver(device, "nouveau"), "symbolic links unsupported");

        NGpu gpu = probe(root, refuseEveryCommand()).detect().get(0);

        // the hardware is present and drives the display, cuda is not available
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuUtils.vendor(gpu));
        Assertions.assertEquals("nouveau", gpu.capability(NGpuUtils.KERNEL_DRIVER).get());
        Assertions.assertFalse(NGpuUtils.isComputeCapable(gpu));
        // the driver version belongs to the proprietary module, not to this one
        Assertions.assertFalse(gpu.capability(NGpuUtils.DRIVER_VERSION).isPresent());
    }

    // -------------------------------------------------------------------------
    // vendor tool, present, absent and outdated
    // -------------------------------------------------------------------------

    @Test
    public void testOldDriverRejectingPcieFieldsStillYieldsComputeCapabilityAndMemory(@TempDir Path root)
            throws IOException {
        pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce GTX 1060", null);

        RecordingRunner runner = new RecordingRunner() {
            @Override
            protected String answer(String query) {
                if (query.contains("pcie")) {
                    // nvidia-smi rejects a whole query over one unknown field
                    return null;
                }
                if (query.contains("memory.total")) {
                    return "00000000:01:00.0, 6144, 512, 5632\n";
                }
                return "00000000:01:00.0, 6.1\n";
            }
        };
        NGpu gpu = probe(root, runner).detect().get(0);

        Assertions.assertEquals(610, NGpuUtils.computeCapability(gpu));
        Assertions.assertEquals(6144L * 1024 * 1024, gpu.vram().total());
        // only the pcie keys are lost, and losing them costs nothing else
        Assertions.assertFalse(gpu.capability(NGpuUtils.PCIE_GEN_MAX).isPresent());
        Assertions.assertEquals(3, runner.queries.size());
    }

    @Test
    public void testMissingVendorToolLeavesIdentityIntact(@TempDir Path root) throws IOException {
        Path device = pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        Assumptions.assumeTrue(bindDriver(device, "nvidia"), "symbolic links unsupported");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4050 Laptop GPU", "GPU-abcdef");

        NGpu gpu = probe(root, refuseEveryCommand()).detect().get(0);

        // nothing the kernel publishes depends on the tool being installed
        Assertions.assertEquals("NVIDIA GeForce RTX 4050 Laptop GPU", gpu.name());
        Assertions.assertTrue(NGpuUtils.isComputeCapable(gpu));
        Assertions.assertEquals("0000:01:00.0", gpu.capability(NGpuUtils.PCI_BUS_ID).get());
        // and what only the tool reports degrades to unknown rather than failing
        Assertions.assertTrue(NGpuUtils.computeCapability(gpu) < 0);
        Assertions.assertEquals(-1, gpu.vram().total());
    }

    @Test
    public void testEightDigitPciDomainFromTheToolMatchesTheKernelsFourDigitOne(@TempDir Path root)
            throws IOException {
        pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4050 Laptop GPU", null);

        NGpu gpu = probe(root, nvidiaSmi(6144, 512)).detect().get(0);

        // the reading is only merged at all if the two forms are reconciled
        Assertions.assertEquals(890, NGpuUtils.computeCapability(gpu));
        Assertions.assertEquals(6144L * 1024 * 1024, gpu.vram().total());
    }

    @Test
    public void testBandwidthComesFromTheTableEvenWithNoVendorTool(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4090", null);

        NGpu gpu = probe(root, refuseEveryCommand()).detect().get(0);

        Assertions.assertEquals("1008.0", gpu.capability(NGpuUtils.MEMORY_BANDWIDTH_GBPS).get());
    }

    // -------------------------------------------------------------------------
    // identity is cached, memory is not
    // -------------------------------------------------------------------------

    @Test
    public void testMemoryIsRereadOnEveryCallWhileIdentityIsProbedOnce(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4050 Laptop GPU", null);

        final long[] used = {512};
        RecordingRunner runner = new RecordingRunner() {
            @Override
            protected String answer(String query) {
                if (query.contains("memory.total")) {
                    return "00000000:01:00.0, 6144, " + used[0] + ", " + (6144 - used[0]) + "\n";
                }
                return "00000000:01:00.0, 8.9, 1, 4, 8, 8\n";
            }
        };
        NLinuxGpuProbe probe = probe(root, runner);

        Assertions.assertEquals(512L * 1024 * 1024, probe.detect().get(0).vram().used());
        used[0] = 4096;
        // the whole point : a caller asking twice must not get a stale figure
        Assertions.assertEquals(4096L * 1024 * 1024, probe.detect().get(0).vram().used());
        probe.detect();

        Assertions.assertEquals(1, count(runner.queries, "compute_cap"));
        Assertions.assertEquals(3, count(runner.queries, "memory.total"));
    }

    @Test
    public void testIdentityIsStableAcrossCalls(@TempDir Path root) throws IOException {
        pciDevice(root, "0000:01:00.0", NVIDIA_VENDOR, "0x28e1");
        nvidiaProc(root, "0000:01:00.0", "NVIDIA GeForce RTX 4050 Laptop GPU", "GPU-abcdef");
        NLinuxGpuProbe probe = probe(root, nvidiaSmi(6144, 512));

        Assertions.assertEquals(probe.detect().get(0).capabilities(), probe.detect().get(0).capabilities());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static NLinuxGpuProbe probe(Path root, Function<String[], String> runner) {
        return NLinuxGpuProbe.of(root, runner);
    }

    /** creates a pci display controller node under the fixture root */
    private static Path pciDevice(Path root, String pciBusId, String vendorId, String deviceId) throws IOException {
        Path device = root.resolve("sys/bus/pci/devices").resolve(pciBusId);
        Files.createDirectories(device);
        write(device.resolve("class"), DISPLAY_CLASS);
        write(device.resolve("vendor"), vendorId);
        write(device.resolve("device"), deviceId);
        return device;
    }

    /**
     * Binds a kernel module the way the kernel does, as a {@code driver}
     * symbolic link whose target name is the module.
     *
     * @return false when the filesystem refuses symbolic links
     */
    private static boolean bindDriver(Path device, String moduleName) {
        try {
            Path module = device.getParent().getParent().resolve("modules").resolve(moduleName);
            Files.createDirectories(module);
            Files.createSymbolicLink(device.resolve("driver"), module);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void nvidiaProc(Path root, String pciBusId, String model, String uuid) throws IOException {
        Path dir = root.resolve("proc/driver/nvidia/gpus").resolve(pciBusId);
        Files.createDirectories(dir);
        StringBuilder sb = new StringBuilder();
        sb.append("Model: \t\t ").append(model).append('\n');
        sb.append("IRQ:   \t\t 175\n");
        if (uuid != null) {
            sb.append("GPU UUID: \t ").append(uuid).append('\n');
        }
        write(dir.resolve("information"), sb.toString());
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    /** a machine where the vendor tool is not installed at all */
    private static Function<String[], String> refuseEveryCommand() {
        return new RecordingRunner() {
            @Override
            protected String answer(String query) {
                return null;
            }
        };
    }

    /** a working nvidia-smi, reporting mebibytes as the real one does */
    private static Function<String[], String> nvidiaSmi(final long totalMib, final long usedMib) {
        return new RecordingRunner() {
            @Override
            protected String answer(String query) {
                if (query.contains("memory.total")) {
                    return "00000000:01:00.0, " + totalMib + ", " + usedMib + ", " + (totalMib - usedMib) + "\n";
                }
                return "00000000:01:00.0, 8.9, 1, 4, 8, 8\n";
            }
        };
    }

    private static int count(List<String> queries, String needle) {
        int n = 0;
        for (String q : queries) {
            if (q.contains(needle)) {
                n++;
            }
        }
        return n;
    }

    /** records the queries issued, so that caching can be asserted on */
    private abstract static class RecordingRunner implements Function<String[], String> {

        final List<String> queries = new ArrayList<>();

        protected abstract String answer(String query);

        @Override
        public String apply(String[] command) {
            Assertions.assertEquals("nvidia-smi", command[0]);
            queries.add(command[1]);
            return answer(command[1]);
        }
    }
}
