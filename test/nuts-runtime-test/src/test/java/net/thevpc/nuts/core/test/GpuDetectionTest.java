package net.thevpc.nuts.core.test;

import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.platform.DefaultNGpuDevice;
import net.thevpc.nuts.runtime.standalone.platform.NEnvBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Covers the gpu capabilities published by {@link NGpuDevice} and the
 * parallel processing runtime resolution.
 *
 * @author thevpc
 */
public class GpuDetectionTest {

    private static final long GIB = 1024L * 1024L * 1024L;

    @Test
    public void testVendorAndDeviceTypeAreReadBackTyped() {
        NGpuDevice nv = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        Assertions.assertEquals(NGpuVendor.NVIDIA, nv.vendor());
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, nv.deviceType());
        Assertions.assertEquals(NGpuVendor.NVIDIA, nv.vendor());
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, nv.deviceType());

        NGpuDevice igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        Assertions.assertEquals(NGpuVendor.INTEL, igpu.vendor());
        Assertions.assertEquals(NGpuDeviceType.INTEGRATED_GPU, igpu.deviceType());
    }

    @Test
    public void testMissingCapabilitiesReadAsUnknownNotAsFailure() {
        NGpuDevice bare = new DefaultNGpuDevice("some gpu", new NRam("some gpu", -1, -1, -1), Collections.<String, String>emptyMap());
        Assertions.assertEquals(NGpuVendor.UNKNOWN, bare.vendor());
        Assertions.assertEquals(NGpuDeviceType.UNKNOWN, bare.deviceType());
        Assertions.assertTrue(bare.computeCapability() < 0);
        Assertions.assertFalse(bare.isComputeCapable());
        Assertions.assertFalse(bare.hasTensorCores());
        Assertions.assertEquals(NGpuVendor.UNKNOWN, bare.vendor());
        Assertions.assertFalse(bare.hasTensorCores());
    }

    @Test
    public void testComputeCapabilityHasOneCanonicalEncoding() {
        Assertions.assertEquals(890, named("x", 890).computeCapability());
        Assertions.assertEquals(750, named("x", 750).computeCapability());
        Assertions.assertEquals(NGpuDevice.CC_TURING, named("x", 750).computeCapability());
        Assertions.assertTrue(withCapability("compute.capability", "not-a-number").computeCapability() < 0);
        Assertions.assertTrue(withCapability("compute.capability", "8").computeCapability() < 0);
    }

    @Test
    public void testComputeCapableRequiresTheVendorModule() {
        Assertions.assertTrue(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB).isComputeCapable());
        Assertions.assertFalse(nvidia("0000:01:00.0", "nouveau", 890, 6 * GIB).isComputeCapable());
        Assertions.assertFalse(nvidia("0000:01:00.0", null, 890, 6 * GIB).isComputeCapable());
        Assertions.assertFalse(device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1).isComputeCapable());
    }

    @Test
    public void testTuringPartsWithoutTensorCoresAreExcludedByModelName() {
        Assertions.assertFalse(named("NVIDIA GeForce MX450", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce MX550", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1650", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1660 Ti", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertTrue(named("NVIDIA GeForce RTX 2060", NGpuDevice.CC_TURING).hasTensorCores());
        Assertions.assertFalse(named("NVIDIA GeForce GTX 1660 Ti", NGpuDevice.CC_TURING).isSupportedInt4());
        Assertions.assertTrue(named("NVIDIA GeForce RTX 2060", NGpuDevice.CC_TURING).isSupportedInt4());
    }

    @Test
    public void testDataTypeSupportFollowsComputeCapabilityThresholds() {
        NGpuDevice maxwell = named("NVIDIA GeForce GTX 970", 520);
        Assertions.assertFalse(maxwell.isSupportedFp16());
        Assertions.assertFalse(maxwell.isSupportedInt8());
        Assertions.assertFalse(maxwell.isSupportedBf16());

        Assertions.assertTrue(named("p", NGpuDevice.CC_PASCAL).isSupportedFp16());
        Assertions.assertFalse(named("p", NGpuDevice.CC_PASCAL).isSupportedInt8());
        Assertions.assertTrue(named("p", NGpuDevice.CC_DP4A).isSupportedInt8());

        NGpuDevice ampere = named("a", NGpuDevice.CC_AMPERE);
        Assertions.assertTrue(ampere.isSupportedBf16());
        Assertions.assertTrue(ampere.hasTensorCores());
    }

    @Test
    public void testDataTypeSupportIsNvidiaOnlyForNow() {
        NGpuDevice amd = device(NGpuVendor.AMD, NGpuDeviceType.DEDICATED_GPU,
                "0000:01:00.0", "amdgpu", NGpuDevice.CC_AMPERE, 16 * GIB);
        Assertions.assertFalse(amd.isSupportedFp16());
        Assertions.assertFalse(amd.isSupportedBf16());
        Assertions.assertFalse(amd.hasTensorCores());
        Assertions.assertTrue(amd.isComputeCapable());
    }

    @Test
    public void testVendorFromPciVendorId() {
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("0x10de"));
        Assertions.assertEquals(NGpuVendor.AMD, NGpuVendor.ofPciVendorId("0x1002"));
        Assertions.assertEquals(NGpuVendor.AMD, NGpuVendor.ofPciVendorId("0x1022"));
        Assertions.assertEquals(NGpuVendor.INTEL, NGpuVendor.ofPciVendorId("0x8086"));
        Assertions.assertEquals(NGpuVendor.APPLE, NGpuVendor.ofPciVendorId("0x106b"));
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("10DE"));
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("  0x10DE  "));
    }

    @Test
    public void testVendorFromPciVendorIdDegradesInsteadOfFailing() {
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId(null));
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId("0x"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x1414"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x15ad"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0x1af4"));
        Assertions.assertEquals(NGpuVendor.OTHER, NGpuVendor.ofPciVendorId("0xbeef"));
    }

    @Test
    public void testComputeKernelModule() {
        Assertions.assertEquals("nvidia", NGpuVendor.NVIDIA.getComputeKernelModule());
        Assertions.assertEquals("amdgpu", NGpuVendor.AMD.getComputeKernelModule());
        Assertions.assertNull(NGpuVendor.INTEL.getComputeKernelModule());
        Assertions.assertNull(NGpuVendor.UNKNOWN.getComputeKernelModule());
    }

    @Test
    public void testPrimaryPrefersDedicatedThenLargestMemory() {
        NGpuDevice igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpuDevice small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);

        Assertions.assertSame(small, NGpuDevice.primary(Arrays.asList(igpu, small)).get());
        Assertions.assertSame(big, NGpuDevice.primary(Arrays.asList(small, big)).get());
        Assertions.assertSame(big, NGpuDevice.primary(Arrays.asList(big, small)).get());

        List<NGpuDevice> reordered = DefaultNGpuDevice.orderWithPrimaryFirst(Arrays.asList(igpu, small, big));
        Assertions.assertEquals(3, reordered.size());
        Assertions.assertSame(big, reordered.get(0));
    }

    @Test
    public void testPrimarySkipsDevicesTheVendorStackCannotDrive() {
        NGpuDevice igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpuDevice nouveau = nvidia("0000:01:00.0", "nouveau", 890, 8 * GIB);
        Assertions.assertFalse(NGpuDevice.primary(Arrays.asList(igpu, nouveau)).isPresent());
        Assertions.assertFalse(NGpuDevice.primary(Collections.<NGpuDevice>emptyList()).isPresent());
        Assertions.assertFalse(NGpuDevice.primary(null).isPresent());
    }

    @Test
    public void testPrimaryCanBeForcedByProperty() {
        NGpuDevice small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpuDevice big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);
        List<NGpuDevice> gpus = Arrays.asList(small, big);
        String saved = System.getProperty(NGpuDevice.PRIMARY_GPU_PROPERTY);
        try {
            System.setProperty(NGpuDevice.PRIMARY_GPU_PROPERTY, "0000:01:00.0");
            Assertions.assertSame(small, NGpuDevice.primary(gpus).get());
            System.setProperty(NGpuDevice.PRIMARY_GPU_PROPERTY, "0000:99:99.9");
            Assertions.assertSame(big, NGpuDevice.primary(gpus).get());
            System.setProperty(NGpuDevice.PRIMARY_GPU_PROPERTY, "   ");
            Assertions.assertSame(big, NGpuDevice.primary(gpus).get());
        } finally {
            if (saved == null) {
                System.clearProperty(NGpuDevice.PRIMARY_GPU_PROPERTY);
            } else {
                System.setProperty(NGpuDevice.PRIMARY_GPU_PROPERTY, saved);
            }
        }
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
        Assertions.assertFalse(neither.isRuntimeAvailable());
        Assertions.assertFalse(neither.isToolkitAvailable());
        Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN,
                NParallelProcessorRuntime.of(null, false, false, null).getFamily());
    }

    @Test
    public void testFamilyParsesVendorAliases() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda").get());
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("nvidia").get());
        Assertions.assertEquals(NParallelProcessorFamily.ROCM, NParallelProcessorFamily.parse("hip").get());
        Assertions.assertEquals(NParallelProcessorFamily.ONEAPI, NParallelProcessorFamily.parse("sycl").get());
        Assertions.assertEquals(NParallelProcessorFamily.ONEAPI, NParallelProcessorFamily.parse("xpu").get());
        Assertions.assertEquals(NParallelProcessorFamily.METAL, NParallelProcessorFamily.parse("mps").get());
        Assertions.assertEquals(NParallelProcessorFamily.DIRECTML, NParallelProcessorFamily.parse("dml").get());
        Assertions.assertEquals(NParallelProcessorFamily.NONE, NParallelProcessorFamily.parse("cpu").get());
    }

    @Test
    public void testFamilyParsesVersionedNames() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda11").get());
        Assertions.assertEquals(NParallelProcessorFamily.CUDA, NParallelProcessorFamily.parse("cuda12").get());
        Assertions.assertEquals(NParallelProcessorFamily.ROCM, NParallelProcessorFamily.parse("rocm6").get());
        Assertions.assertEquals(NParallelProcessorFamily.OPENCL, NParallelProcessorFamily.parse("opencl3").get());
    }

    @Test
    public void testDedicatedAcceleratorsParseToUnknownInsteadOfFailing() {
        for (String s : new String[]{"tpu", "xla", "neuron", "trainium", "ascend", "cann",
                "gaudi", "hpu", "qnn", "hexagon", "ipu", "poplar", "fpga", "xilinx"}) {
            Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN,
                    NParallelProcessorFamily.parse(s).get(), "parsing " + s);
        }
    }

    @Test
    public void testUnrecognizedValueIsEmptyNotUnknown() {
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

    @Test
    public void testFamilyPrefersVendorNativeStackOverCrossVendorLayer() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA,
                env(runtime(NParallelProcessorFamily.VULKAN, true, false),
                        runtime(NParallelProcessorFamily.CUDA, true, false)).parallelProcessorFamily());
        Assertions.assertEquals(NParallelProcessorFamily.VULKAN,
                env(runtime(NParallelProcessorFamily.VULKAN, true, false),
                        runtime(NParallelProcessorFamily.OPENCL, true, false)).parallelProcessorFamily());
    }

    @Test
    public void testFamilyFallsBackToABuildOnlyRuntime() {
        Assertions.assertEquals(NParallelProcessorFamily.CUDA,
                env(runtime(NParallelProcessorFamily.CUDA, false, true)).parallelProcessorFamily());
    }

    @Test
    public void testNoneMeansProbedAndUnknownMeansUnprobable() {
        TestEnv probed = new TestEnv(Collections.<NParallelProcessorRuntime>emptyList(), true);
        TestEnv notProbed = new TestEnv(Collections.<NParallelProcessorRuntime>emptyList(), false);
        Assertions.assertEquals(NParallelProcessorFamily.NONE, probed.parallelProcessorFamily());
        Assertions.assertEquals(NParallelProcessorFamily.UNKNOWN, notProbed.parallelProcessorFamily());
    }

    @Test
    public void testRuntimeListIsNeverNull() {
        TestEnv e = new TestEnv(null, true);
        Assertions.assertTrue(e.parallelProcessorRuntimes().isEmpty());
        Assertions.assertSame(e.parallelProcessorRuntimes(), e.parallelProcessorRuntimes());
    }

    private static NParallelProcessorRuntime runtime(NParallelProcessorFamily family, boolean run, boolean build) {
        return NParallelProcessorRuntime.of(family, run, build, null);
    }

    private static TestEnv env(NParallelProcessorRuntime... runtimes) {
        return new TestEnv(Arrays.asList(runtimes), true);
    }

    private static NGpuDevice withCapability(String key, String value) {
        Map<String, String> caps = new LinkedHashMap<>();
        caps.put(NGpuDevice.VENDOR, NGpuVendor.NVIDIA.id());
        caps.put(key, value);
        return new DefaultNGpuDevice("x", new NRam("x", -1, -1, -1), caps);
    }

    private static NGpuDevice gpu(String name, NGpuVendor vendor, NGpuDeviceType type, String pciBusId,
                                  String kernelDriver, int computeCapability, long memory) {
        Map<String, String> caps = new LinkedHashMap<>();
        caps.put(NGpuDevice.PCI_BUS_ID, pciBusId);
        caps.put(NGpuDevice.PCI_DEVICE_ID, "0x28e1");
        caps.put(NGpuDevice.VENDOR, vendor.id());
        caps.put(NGpuDevice.DEVICE_TYPE, type.id());
        if (kernelDriver != null) {
            caps.put(NGpuDevice.KERNEL_DRIVER, kernelDriver);
        }
        if (computeCapability >= 0) {
            caps.put(NGpuDevice.COMPUTE_CAPABILITY,
                    (computeCapability / 100) + "." + ((computeCapability / 10) % 10));
        }
        return new DefaultNGpuDevice(name, new NRam(name, memory, -1, -1), caps);
    }

    private static NGpuDevice device(NGpuVendor vendor, NGpuDeviceType type, String pciBusId,
                                    String kernelDriver, int computeCapability, long memory) {
        return gpu("model-" + pciBusId, vendor, type, pciBusId, kernelDriver, computeCapability, memory);
    }

    private static NGpuDevice nvidia(String pciBusId, String kernelDriver, int computeCapability, long memory) {
        return device(NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU, pciBusId, kernelDriver, computeCapability, memory);
    }

    private static NGpuDevice named(String modelName, int computeCapability) {
        return gpu(modelName, NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU,
                "0000:01:00.0", "nvidia", computeCapability, 6 * GIB);
    }

    private static class TestEnv extends NEnvBase {
        private final List<NParallelProcessorRuntime> runtimes;
        private final boolean detectionSupported;

        TestEnv(List<NParallelProcessorRuntime> runtimes, boolean detectionSupported) {
            this.runtimes = runtimes;
            this.detectionSupported = detectionSupported;
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
        public boolean isGraphicalDesktopEnvironment0(){ return false; }
        @Override
        public net.thevpc.nuts.artifact.NId getOsDist0(){ return null; }
        @Override
        public net.thevpc.nuts.artifact.NId getArch0(){ return null; }
        @Override
        public net.thevpc.nuts.platform.NArchFamily getArchFamily0(){ return null; }
        @Override
        public String getRootUserName0(){ return null; }
        @Override
        public String getUserName0(){ return null; }
        @Override
        public net.thevpc.nuts.artifact.NId getJava0(){ return null; }
        @Override
        public String getUserHome0(){ return null; }
        @Override
        public net.thevpc.nuts.artifact.NId getOs0(){ return null; }
        @Override
        public net.thevpc.nuts.platform.NOsFamily getOsFamily0(){ return null; }
        @Override
        public net.thevpc.nuts.artifact.NId getShell0(){ return null; }
        @Override
        public net.thevpc.nuts.platform.NShellFamily getShellFamily0(){ return null; }
        @Override
        public String getMachineName0(){ return null; }
        @Override
        public String getHostName0(){ return null; }
        @Override
        public String pid(){ return null; }
        @Override
        public Map<String, String> env(){ return Collections.emptyMap(); }
        @Override
        public net.thevpc.nuts.net.NConnectionString connectionString(){ return null; }
        @Override
        public boolean isNativeImage(){ return false; }
        @Override
        public net.thevpc.nuts.util.NOptional<String> getEnv(String p0){ return net.thevpc.nuts.util.NOptional.ofEmpty(); }
        @Override
        public net.thevpc.nuts.platform.NRam ram(){ return null; }
        @Override
        public List<NGpuDevice> gpus(){ return Collections.emptyList(); }
        @Override
        public net.thevpc.nuts.platform.NEnv refresh(){ return null; }
    }
}
