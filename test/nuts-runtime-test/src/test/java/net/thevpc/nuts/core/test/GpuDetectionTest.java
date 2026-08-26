package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.platform.NEnvBase;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Covers gpu detection and parallel processing runtime resolution.
 * <p>
 * Everything asserted here is hardware independent : device descriptions are
 * built by hand and fed to the resolution logic, so the suite behaves the same
 * on a machine with no gpu at all. Probing the real machine is deliberately not
 * asserted, its outcome being a property of the host rather than of the code.
 *
 * @author thevpc
 */
public class GpuDetectionTest {

    private static final long GIB = 1024L * 1024L * 1024L;

    // -------------------------------------------------------------------------
    // vendor resolution from the raw pci vendor id
    // -------------------------------------------------------------------------

    @Test
    public void testVendorFromPciVendorId() {
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("0x10de"));
        Assertions.assertEquals(NGpuVendor.AMD, NGpuVendor.ofPciVendorId("0x1002"));
        Assertions.assertEquals(NGpuVendor.AMD, NGpuVendor.ofPciVendorId("0x1022"));
        Assertions.assertEquals(NGpuVendor.INTEL, NGpuVendor.ofPciVendorId("0x8086"));
        Assertions.assertEquals(NGpuVendor.APPLE, NGpuVendor.ofPciVendorId("0x106b"));
    }

    @Test
    public void testVendorFromPciVendorIdIsCaseAndPrefixInsensitive() {
        // the kernel writes "0x10de", other sources drop the prefix or upper case it
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("10de"));
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("0X10DE"));
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("  0x10DE  "));
    }

    @Test
    public void testVendorFromPciVendorIdDegradesInsteadOfFailing() {
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId(null));
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId("0x"));
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId(""));
        // paravirtualized adapters are recognized, just not tracked individually
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x1414"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x15ad"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x1af4"));
        // an unlisted vendor is still a display adapter
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0xbeef"));
    }

    @Test
    public void testComputeKernelModule() {
        Assertions.assertEquals("nvidia", NGpuVendor.NVIDIA.getComputeKernelModule());
        Assertions.assertEquals("amdgpu", NGpuVendor.AMD.getComputeKernelModule());
        // i915 is a display driver, intel compute goes through level zero
        Assertions.assertNull(NGpuVendor.INTEL.getComputeKernelModule());
        Assertions.assertNull(NGpuVendor.UNKNOWN.getComputeKernelModule());
    }

    // -------------------------------------------------------------------------
    // the bound kernel module is the capability gate
    // -------------------------------------------------------------------------

    @Test
    public void testComputeCapableRequiresTheVendorModule() {
        Assertions.assertTrue(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB).isComputeCapable());
        // same hardware, left to the open source driver : no cuda capability
        Assertions.assertFalse(nvidia("0000:01:00.0", "nouveau", 890, 6 * GIB).isComputeCapable());
        // no module bound at all
        Assertions.assertFalse(nvidia("0000:01:00.0", null, 890, 6 * GIB).isComputeCapable());
        // intel reports no compute module, so it never claims capability
        Assertions.assertFalse(device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU,
                "0000:00:02.0", "i915", -1, -1).isComputeCapable());
    }

    // -------------------------------------------------------------------------
    // compute capability derived properties
    // -------------------------------------------------------------------------

    @Test
    public void testDataTypeSupportFollowsComputeCapabilityThresholds() {
        NGpuDevice maxwell = nvidia("0000:01:00.0", "nvidia", 520, 4 * GIB);
        Assertions.assertFalse(maxwell.isSupportedFp16());
        Assertions.assertFalse(maxwell.isSupportedInt8());
        Assertions.assertFalse(maxwell.isSupportedBf16());

        NGpuDevice pascal = nvidia("0000:01:00.0", "nvidia", NGpuDevice.CC_PASCAL, 8 * GIB);
        Assertions.assertTrue(pascal.isSupportedFp16());
        // dp4a lands one step above pascal proper
        Assertions.assertFalse(pascal.isSupportedInt8());
        Assertions.assertTrue(nvidia("0000:01:00.0", "nvidia", NGpuDevice.CC_DP4A, 8 * GIB).isSupportedInt8());

        NGpuDevice ampere = nvidia("0000:01:00.0", "nvidia", NGpuDevice.CC_AMPERE, 24 * GIB);
        Assertions.assertTrue(ampere.isSupportedBf16());
        Assertions.assertTrue(ampere.hasTensorCores());
    }

    @Test
    public void testDataTypeSupportIsNvidiaOnlyForNow() {
        // detection for other vendors is not implemented, so nothing is claimed
        NGpuDevice amd = device(NGpuVendor.AMD, NGpuDeviceType.DEDICATED_GPU,
                "0000:01:00.0", "amdgpu", NGpuDevice.CC_AMPERE, 16 * GIB);
        Assertions.assertFalse(amd.isSupportedFp16());
        Assertions.assertFalse(amd.isSupportedBf16());
        Assertions.assertFalse(amd.hasTensorCores());
    }

    @Test
    public void testTuringPartsWithoutTensorCoresAreExcludedByModelName() {
        // these reach CC_TURING yet ship no tensor cores, nothing but the
        // marketing name distinguishes them
        Assertions.assertFalse(named("NVIDIA GeForce MX450", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce MX550", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1650", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1660 Ti", NGpuDevice.CC_TURING).hasTensorCores());
        // a regular turing part is unaffected
        Assertions.assertTrue(named("NVIDIA GeForce RTX 2060", NGpuDevice.CC_TURING).hasTensorCores());
        // int4 is a tensor core only capability and must follow the exclusion
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1660 Ti", NGpuDevice.CC_TURING).isSupportedInt4());
        Assertions.assertTrue(named("NVIDIA GeForce RTX 2060", NGpuDevice.CC_TURING).isSupportedInt4());
    }

    @Test
    public void testUnknownComputeCapabilityIsReportedAsSuch() {
        NGpuDevice d = nvidia("0000:01:00.0", "nvidia", -1, 6 * GIB);
        Assertions.assertFalse(d.hasComputeCapability());
        Assertions.assertFalse(d.hasTensorCores());
        Assertions.assertFalse(d.isSupportedFp16());
        Assertions.assertTrue(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB).hasComputeCapability());
    }

    // -------------------------------------------------------------------------
    // value semantics
    // -------------------------------------------------------------------------

    @Test
    public void testDeviceValueSemantics() {
        NGpuDevice a = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice b = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice c = nvidia("0000:02:00.0", "nvidia", 890, 6 * GIB);
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, null);
    }

    @Test
    public void testNullsDegradeToUnknownRatherThanBeingStored() {
        NGpuDevice d = NGpuDevice.of(null, null, null, null, null, null, null, -1, -1, null);
        Assertions.assertEquals(NGpuVendor.UNKNOWN, d.getVendor());
        Assertions.assertEquals(NGpuDeviceType.UNKNOWN, d.getDeviceType());
        Assertions.assertTrue(d.isBlank());
        Assertions.assertFalse(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB).isBlank());
    }

    @Test
    public void testRuntimeDescribesRunAndBuildSeparately() {
        NParallelProcessorRuntime runOnly = NParallelProcessorRuntime.of(NParallelProcessorFamily.CUDA, true, false, null);
        NParallelProcessorRuntime buildOnly = NParallelProcessorRuntime.of(NParallelProcessorFamily.CUDA, false, true, "12.4");
        NParallelProcessorRuntime neither = NParallelProcessorRuntime.of(NParallelProcessorFamily.CUDA, false, false, null);
        Assertions.assertTrue(runOnly.isRuntimeAvailable());
        Assertions.assertFalse(runOnly.isToolkitAvailable());
        Assertions.assertFalse(buildOnly.isRuntimeAvailable());
        Assertions.assertTrue(buildOnly.isToolkitAvailable());
        Assertions.assertEquals("12.4", buildOnly.getVersion());
        Assertions.assertTrue(neither.isBlank());
        Assertions.assertFalse(runOnly.isBlank());
        Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN,
                NParallelProcessorRuntime.of(null, false, false, null).getFamily());
    }

    // -------------------------------------------------------------------------
    // parsing, which has to degrade rather than fail
    // -------------------------------------------------------------------------

    @Test
    public void testFamilyParsesVendorAliases() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda").get());
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("nvidia").get());
        Assertions.assertEquals(NParallelProcessorFamily.ROCM, NParallelProcessorFamily.parse("hip").get());
        Assertions.assertEquals(NParallelProcessorFamily.ONEAPI, NParallelProcessorFamily.parse("sycl").get());
        // "xpu" is the pytorch device name for an intel gpu
        Assertions.assertEquals(NParallelProcessorFamily.ONEAPI, NParallelProcessorFamily.parse("xpu").get());
        Assertions.assertEquals(NParallelProcessorFamily.METAL, NParallelProcessorFamily.parse("mps").get());
        Assertions.assertEquals(NParallelProcessorFamily.DIRECTML, NParallelProcessorFamily.parse("dml").get());
        Assertions.assertEquals(NParallelProcessorFamily.NONE, NParallelProcessorFamily.parse("cpu").get());
    }

    @Test
    public void testFamilyParsesVersionedNames() {
        // descriptors commonly carry the toolkit generation in the name
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda11").get());
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda12").get());
        Assertions.assertEquals(NParallelProcessorFamily.ROCM, NParallelProcessorFamily.parse("rocm6").get());
        Assertions.assertEquals(NParallelProcessorFamily.OPENCL, NParallelProcessorFamily.parse("opencl3").get());
    }

    @Test
    public void testDedicatedAcceleratorsParseToUnknownInsteadOfFailing() {
        // no jvm artifact targets these, but a descriptor naming one must not blow up
        for (String s : new String[]{"tpu", "xla", "neuron", "trainium", "ascend", "cann",
                "gaudi", "hpu", "qnn", "hexagon", "ipu", "poplar", "fpga", "xilinx"}) {
            Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN,
                    NParallelProcessorFamily.parse(s).get(), "parsing " + s);
        }
    }

    @Test
    public void testUnrecognizedValueIsEmptyNotUnknown() {
        // an outright unknown token is an absence of a result, not the UNKNOWN entry
        Assertions.assertFalse(NParallelProcessorFamily.parse("totally-bogus-value").isPresent());
        Assertions.assertFalse(NGpuVendor.parse("totally-bogus-value").isPresent());
        Assertions.assertFalse(NGpuDeviceType.parse("totally-bogus-value").isPresent());
    }

    @Test
    public void testDeviceTypeAndVendorAliases() {
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, NGpuDeviceType.parse("discrete").get());
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, NGpuDeviceType.parse("dgpu").get());
        Assertions.assertEquals(NGpuDeviceType.INTEGRATED_GPU, NGpuDeviceType.parse("igpu").get());
        Assertions.assertEquals(NGpuDeviceType.INTEGRATED_GPU, NGpuDeviceType.parse("unified").get());
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.parse("geforce").get());
        Assertions.assertEquals(NGpuVendor.AMD, NGpuVendor.parse("radeon").get());
        Assertions.assertEquals(NGpuVendor.INTEL, NGpuVendor.parse("arc").get());
    }

    @Test
    public void testCrossVendorClassification() {
        Assertions.assertTrue(NParallelProcessorFamily.OPENCL.isCrossVendor());
        Assertions.assertTrue(NParallelProcessorFamily.VULKAN.isCrossVendor());
        Assertions.assertTrue(NParallelProcessorFamily.DIRECTML.isCrossVendor());
        Assertions.assertFalse(NParallelProcessorFamily.CUDA.isCrossVendor());
        Assertions.assertFalse(NParallelProcessorFamily.ROCM.isCrossVendor());
        Assertions.assertTrue(NParallelProcessorFamily.NONE.isNone());
        Assertions.assertTrue(NParallelProcessorFamily.UNKNOWN.isUnknown());
        Assertions.assertFalse(NParallelProcessorFamily.NONE.isUnknown());
    }

    // -------------------------------------------------------------------------
    // primary device selection
    // -------------------------------------------------------------------------

    @Test
    public void testPrimaryDevicePrefersDedicatedThenLargestMemory() {
        NGpuDevice igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpuDevice small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);

        Assertions.assertEquals(small, env(Arrays.asList(igpu, small)).gpuDevice().get());
        Assertions.assertEquals(big, env(Arrays.asList(small, big)).gpuDevice().get());
        // the answer must not depend on the order devices were discovered in
        Assertions.assertEquals(big, env(Arrays.asList(big, small)).gpuDevice().get());
    }

    @Test
    public void testPrimaryDeviceSkipsDevicesTheVendorStackCannotDrive() {
        NGpuDevice igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpuDevice nouveau = nvidia("0000:01:00.0", "nouveau", 890, 8 * GIB);
        Assertions.assertFalse(env(Arrays.asList(igpu, nouveau)).gpuDevice().isPresent());
        Assertions.assertFalse(env(Collections.<NGpuDevice>emptyList()).gpuDevice().isPresent());
    }

    @Test
    public void testPrimaryDeviceCanBeForcedByProperty() {
        NGpuDevice small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);
        List<NGpuDevice> devices = Arrays.asList(small, big);
        String saved = System.getProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY);
        try {
            System.setProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY, "0000:01:00.0");
            Assertions.assertEquals(small, env(devices).gpuDevice().get());
            // an address matching nothing must not defeat the heuristic
            System.setProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY, "0000:99:99.9");
            Assertions.assertEquals(big, env(devices).gpuDevice().get());
            System.setProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY, "   ");
            Assertions.assertEquals(big, env(devices).gpuDevice().get());
        } finally {
            if (saved == null) {
                System.clearProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY);
            } else {
                System.setProperty(NEnvBase.PRIMARY_GPU_DEVICE_PROPERTY, saved);
            }
        }
    }

    @Test
    public void testDeviceListsAreNeverNull() {
        TestEnv e = new TestEnv(null, null, true);
        Assertions.assertTrue(e.gpuDevices().isEmpty());
        Assertions.assertTrue(e.parallelProcessorRuntimes().isEmpty());
        // the resolved lists are computed once
        Assertions.assertSame(e.gpuDevices(), e.gpuDevices());
    }

    // -------------------------------------------------------------------------
    // runtime family resolution
    // -------------------------------------------------------------------------

    @Test
    public void testFamilyPrefersVendorNativeStackOverCrossVendorLayer() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA,
                envRuntimes(runtime(NParallelProcessorFamily.VULKAN, true, false),
                        runtime(NParallelProcessorFamily.CUDA, true, false)).parallelProcessorFamily());
        Assertions.assertEquals(NParallelProcessorFamily.VULKAN,
                envRuntimes(runtime(NParallelProcessorFamily.VULKAN, true, false),
                        runtime(NParallelProcessorFamily.OPENCL, true, false)).parallelProcessorFamily());
    }

    @Test
    public void testFamilyFallsBackToABuildOnlyRuntime() {
        // a build container holds the toolkit and no device, which is still cuda
        Assertions.assertEquals(NParallelProcessorFamily.CUDA,
                envRuntimes(runtime(NParallelProcessorFamily.CUDA, false, true)).parallelProcessorFamily());
    }

    @Test
    public void testNoneMeansProbedAndUnknownMeansUnprobable() {
        // the whole point of having both entries : one is an answer, the other
        // is the absence of one
        TestEnv probed = new TestEnv(Collections.<NGpuDevice>emptyList(),
                Collections.<NParallelProcessorRuntime>emptyList(), true);
        TestEnv notProbed = new TestEnv(Collections.<NGpuDevice>emptyList(),
                Collections.<NParallelProcessorRuntime>emptyList(), false);
        Assertions.assertEquals(NParallelProcessorFamily.NONE, probed.parallelProcessorFamily());
        Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN, notProbed.parallelProcessorFamily());
    }

    @Test
    public void testFreeMemoryIsUnknownWhenTheTargetCannotBeInspected() {
        TestEnv e = env(Collections.<NGpuDevice>emptyList());
        Assertions.assertTrue(e.queryGpuFreeMemoryBytes(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB)) < 0);
        Assertions.assertTrue(e.queryGpuFreeMemoryBytes(null) < 0);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static NGpuDevice device(NGpuVendor vendor, NGpuDeviceType type, String pciBusId,
                                     String kernelDriver, int computeCapability, long memory) {
        return NGpuDevice.of(vendor, type, pciBusId, "0x28e1", kernelDriver,
                "model-" + pciBusId, null, computeCapability, memory, null);
    }

    private static NGpuDevice nvidia(String pciBusId, String kernelDriver, int computeCapability, long memory) {
        return device(NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU, pciBusId, kernelDriver, computeCapability, memory);
    }

    private static NGpuDevice named(String modelName, int computeCapability) {
        return NGpuDevice.of(NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU, "0000:01:00.0", "0x28e1",
                "nvidia", modelName, null, computeCapability, 6 * GIB, null);
    }

    private static NParallelProcessorRuntime runtime(NParallelProcessorFamily family, boolean run, boolean build) {
        return NParallelProcessorRuntime.of(family, run, build, null);
    }

    private static TestEnv env(List<NGpuDevice> devices) {
        return new TestEnv(devices, Collections.<NParallelProcessorRuntime>emptyList(), true);
    }

    private static TestEnv envRuntimes(NParallelProcessorRuntime... runtimes) {
        return new TestEnv(Collections.<NGpuDevice>emptyList(), Arrays.asList(runtimes), true);
    }

    /**
     * Feeds hand built devices to the resolution logic of {@link NEnvBase},
     * everything unrelated to the gpu axis being stubbed out.
     */
    private static class TestEnv extends NEnvBase {
        private final List<NGpuDevice> devices;
        private final List<NParallelProcessorRuntime> runtimes;
        private final boolean detectionSupported;

        TestEnv(List<NGpuDevice> devices, List<NParallelProcessorRuntime> runtimes, boolean detectionSupported) {
            this.devices = devices;
            this.runtimes = runtimes;
            this.detectionSupported = detectionSupported;
        }

        @Override
        protected List<NGpuDevice> getGpuDevices0() {
            return devices;
        }

        @Override
        protected List<NParallelProcessorRuntime> getParallelProcessorRuntimes0() {
            return runtimes;
        }

        @Override
        protected boolean isParallelProcessorDetectionSupported() {
            return detectionSupported;
        }

        @Override
        public boolean isGraphicalDesktopEnvironment0() {
            return false;
        }

        @Override
        public NId getOsDist0() {
            return null;
        }

        @Override
        public NId getArch0() {
            return null;
        }

        @Override
        public NArchFamily getArchFamily0() {
            return null;
        }

        @Override
        public String getRootUserName0() {
            return "root";
        }

        @Override
        public String getUserName0() {
            return "test";
        }

        @Override
        public NId getJava0() {
            return null;
        }

        @Override
        public String getUserHome0() {
            return "/home/test";
        }

        @Override
        public NId getOs0() {
            return null;
        }

        @Override
        public NOsFamily getOsFamily0() {
            return NOsFamily.LINUX;
        }

        @Override
        public NId getShell0() {
            return null;
        }

        @Override
        public NShellFamily getShellFamily0() {
            return NShellFamily.BASH;
        }

        @Override
        public String getMachineName0() {
            return "test-machine";
        }

        @Override
        public String getHostName0() {
            return "test-host";
        }

        @Override
        public String pid() {
            return "0";
        }

        @Override
        public Map<String, String> env() {
            return Collections.emptyMap();
        }

        @Override
        public NConnectionString connectionString() {
            return null;
        }

        @Override
        public boolean isNativeImage() {
            return false;
        }

        @Override
        public NOptional<String> getEnv(String name) {
            return NOptional.ofEmpty();
        }

        @Override
        public NRam ram() {
            return null;
        }

        @Override
        public NEnv refresh() {
            return this;
        }
    }
}
