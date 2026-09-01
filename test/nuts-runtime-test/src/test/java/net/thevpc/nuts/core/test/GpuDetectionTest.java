package net.thevpc.nuts.core.test;

import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.platform.NEnvBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Covers the gpu capabilities published by {@link NGpuUtils} and the
 * parallel processing runtime resolution.
 * <p>
 * Devices are built by hand rather than probed, so the suite behaves the same on
 * a machine with no gpu at all. Probing the real machine is deliberately not
 * asserted, its outcome being a property of the host rather than of the code.
 *
 * @author thevpc
 */
public class GpuDetectionTest {

    private static final long GIB = 1024L * 1024L * 1024L;

    // -------------------------------------------------------------------------
    // typed reads of the capability bag
    // -------------------------------------------------------------------------

    @Test
    public void testVendorAndDeviceTypeAreReadBackTyped() {
        NGpu nv = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuUtils.vendor(nv));
        Assertions.assertEquals(NGpuDeviceType.DEDICATED_GPU, NGpuUtils.deviceType(nv));

        NGpu igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        Assertions.assertEquals(NGpuVendor.INTEL, NGpuUtils.vendor(igpu));
        Assertions.assertEquals(NGpuDeviceType.INTEGRATED_GPU, NGpuUtils.deviceType(igpu));
    }

    @Test
    public void testMissingCapabilitiesReadAsUnknownNotAsFailure() {
        // a device detected by tooling that publishes none of these keys
        NGpu bare = new NGpu("some gpu", new NRam("some gpu", -1, -1, -1), Collections.<String, String>emptyMap());
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuUtils.vendor(bare));
        Assertions.assertEquals(NGpuDeviceType.UNKNOWN, NGpuUtils.deviceType(bare));
        Assertions.assertTrue(NGpuUtils.computeCapability(bare) < 0);
        Assertions.assertFalse(NGpuUtils.isComputeCapable(bare));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(bare));
        // and a null device must not blow up either
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuUtils.vendor(null));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(null));
    }

    @Test
    public void testComputeCapabilityHasOneCanonicalEncoding() {
        // the vendor tool reports "8.9"; callers get a comparable int, so that
        // no two call sites can disagree on the encoding
        Assertions.assertEquals(890, NGpuUtils.computeCapability(named("x", 890)));
        Assertions.assertEquals(750, NGpuUtils.computeCapability(named("x", 750)));
        Assertions.assertEquals(NGpuUtils.CC_TURING, NGpuUtils.computeCapability(named("x", 750)));
        // garbage in the bag degrades to unknown rather than throwing
        Assertions.assertTrue(NGpuUtils.computeCapability(withCapability("compute.capability", "not-a-number")) < 0);
        Assertions.assertTrue(NGpuUtils.computeCapability(withCapability("compute.capability", "8")) < 0);
    }

    // -------------------------------------------------------------------------
    // the bound kernel module is the capability gate
    // -------------------------------------------------------------------------

    @Test
    public void testComputeCapableRequiresTheVendorModule() {
        Assertions.assertTrue(NGpuUtils.isComputeCapable(nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB)));
        // same hardware left to the open source driver : no cuda capability
        Assertions.assertFalse(NGpuUtils.isComputeCapable(nvidia("0000:01:00.0", "nouveau", 890, 6 * GIB)));
        Assertions.assertFalse(NGpuUtils.isComputeCapable(nvidia("0000:01:00.0", null, 890, 6 * GIB)));
        // i915 is a display driver, intel compute goes through level zero
        Assertions.assertFalse(NGpuUtils.isComputeCapable(
                device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1)));
    }

    // -------------------------------------------------------------------------
    // rules a caller reading the raw capability would get wrong
    // -------------------------------------------------------------------------

    @Test
    public void testTuringPartsWithoutTensorCoresAreExcludedByModelName() {
        // these reach CC_TURING yet ship no tensor cores. A caller deriving the
        // answer from compute.capability alone would say true for all of them,
        // which is exactly why the rule lives here and not at call sites.
        Assertions.assertFalse(NGpuUtils.hasTensorCores(named("NVIDIA GeForce MX450", NGpuUtils.CC_TURING)));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(named("NVIDIA GeForce MX550", NGpuUtils.CC_TURING)));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(named("NVIDIA GeForce GTX 1650", NGpuUtils.CC_TURING)));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(named("NVIDIA GeForce GTX 1660 Ti", NGpuUtils.CC_TURING)));
        // a regular turing part is unaffected
        Assertions.assertTrue(NGpuUtils.hasTensorCores(named("NVIDIA GeForce RTX 2060", NGpuUtils.CC_TURING)));
        // int4 is tensor core only and must follow the exclusion
        Assertions.assertFalse(NGpuUtils.isSupportedInt4(named("NVIDIA GeForce GTX 1660 Ti", NGpuUtils.CC_TURING)));
        Assertions.assertTrue(NGpuUtils.isSupportedInt4(named("NVIDIA GeForce RTX 2060", NGpuUtils.CC_TURING)));
    }

    @Test
    public void testDataTypeSupportFollowsComputeCapabilityThresholds() {
        NGpu maxwell = named("NVIDIA GeForce GTX 970", 520);
        Assertions.assertFalse(NGpuUtils.isSupportedFp16(maxwell));
        Assertions.assertFalse(NGpuUtils.isSupportedInt8(maxwell));
        Assertions.assertFalse(NGpuUtils.isSupportedBf16(maxwell));

        Assertions.assertTrue(NGpuUtils.isSupportedFp16(named("p", NGpuUtils.CC_PASCAL)));
        // dp4a lands one step above pascal proper
        Assertions.assertFalse(NGpuUtils.isSupportedInt8(named("p", NGpuUtils.CC_PASCAL)));
        Assertions.assertTrue(NGpuUtils.isSupportedInt8(named("p", NGpuUtils.CC_DP4A)));

        NGpu ampere = named("a", NGpuUtils.CC_AMPERE);
        Assertions.assertTrue(NGpuUtils.isSupportedBf16(ampere));
        Assertions.assertTrue(NGpuUtils.hasTensorCores(ampere));
    }

    @Test
    public void testDataTypeSupportIsNvidiaOnlyForNow() {
        NGpu amd = device(NGpuVendor.AMD, NGpuDeviceType.DEDICATED_GPU,
                "0000:01:00.0", "amdgpu", NGpuUtils.CC_AMPERE, 16 * GIB);
        Assertions.assertFalse(NGpuUtils.isSupportedFp16(amd));
        Assertions.assertFalse(NGpuUtils.isSupportedBf16(amd));
        Assertions.assertFalse(NGpuUtils.hasTensorCores(amd));
        // but the amdgpu module still gates its own compute stack
        Assertions.assertTrue(NGpuUtils.isComputeCapable(amd));
    }

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
        // the kernel writes "0x10de", other sources drop the prefix or upper case it
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("10DE"));
        Assertions.assertEquals(NGpuVendor.NVIDIA, NGpuVendor.ofPciVendorId("  0x10DE  "));
    }

    @Test
    public void testVendorFromPciVendorIdDegradesInsteadOfFailing() {
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId(null));
        Assertions.assertEquals(NGpuVendor.UNKNOWN, NGpuVendor.ofPciVendorId("0x"));
        // paravirtualized adapters are recognized, just not tracked individually
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

    // -------------------------------------------------------------------------
    // primary device selection
    // -------------------------------------------------------------------------

    @Test
    public void testPrimaryPrefersDedicatedThenLargestMemory() {
        NGpu igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpu small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpu big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);

        Assertions.assertSame(small, NGpuUtils.primary(Arrays.asList(igpu, small)).get());
        Assertions.assertSame(big, NGpuUtils.primary(Arrays.asList(small, big)).get());
        // the answer must not depend on discovery order
        Assertions.assertSame(big, NGpuUtils.primary(Arrays.asList(big, small)).get());
    }

    @Test
    public void testPrimarySkipsDevicesTheVendorStackCannotDrive() {
        NGpu igpu = device(NGpuVendor.INTEL, NGpuDeviceType.INTEGRATED_GPU, "0000:00:02.0", "i915", -1, -1);
        NGpu nouveau = nvidia("0000:01:00.0", "nouveau", 890, 8 * GIB);
        Assertions.assertFalse(NGpuUtils.primary(Arrays.asList(igpu, nouveau)).isPresent());
        Assertions.assertFalse(NGpuUtils.primary(Collections.<NGpu>emptyList()).isPresent());
        Assertions.assertFalse(NGpuUtils.primary(null).isPresent());
    }

    @Test
    public void testPrimaryCanBeForcedByProperty() {
        NGpu small = nvidia("0000:01:00.0", "nvidia", 890, 6 * GIB);
        NGpu big = nvidia("0000:02:00.0", "nvidia", 890, 24 * GIB);
        List<NGpu> gpus = Arrays.asList(small, big);
        String saved = System.getProperty(NGpuUtils.PRIMARY_GPU_PROPERTY);
        try {
            System.setProperty(NGpuUtils.PRIMARY_GPU_PROPERTY, "0000:01:00.0");
            Assertions.assertSame(small, NGpuUtils.primary(gpus).get());
            // an address matching nothing must not defeat the heuristic
            System.setProperty(NGpuUtils.PRIMARY_GPU_PROPERTY, "0000:99:99.9");
            Assertions.assertSame(big, NGpuUtils.primary(gpus).get());
            System.setProperty(NGpuUtils.PRIMARY_GPU_PROPERTY, "   ");
            Assertions.assertSame(big, NGpuUtils.primary(gpus).get());
        } finally {
            if (saved == null) {
                System.clearProperty(NGpuUtils.PRIMARY_GPU_PROPERTY);
            } else {
                System.setProperty(NGpuUtils.PRIMARY_GPU_PROPERTY, saved);
            }
        }
    }

    // -------------------------------------------------------------------------
    // parallel processing runtimes, the software axis
    // -------------------------------------------------------------------------

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
        // "xpu" is the pytorch device name for an intel gpu
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
        // no jvm artifact targets these, but a descriptor naming one must not blow up
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

    // -------------------------------------------------------------------------
    // runtime family resolution
    // -------------------------------------------------------------------------

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
        // a build container holds the toolkit and no device, which is still cuda
        Assertions.assertEquals(NParallelProcessorFamily.CUDA,
                env(runtime(NParallelProcessorFamily.CUDA, false, true)).parallelProcessorFamily());
    }

    @Test
    public void testNoneMeansProbedAndUnknownMeansUnprobable() {
        // the whole point of having both entries : one is an answer, the other
        // is the absence of one
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

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static NParallelProcessorRuntime runtime(NParallelProcessorFamily family, boolean run, boolean build) {
        return NParallelProcessorRuntime.of(family, run, build, null);
    }

    private static TestEnv env(NParallelProcessorRuntime... runtimes) {
        return new TestEnv(Arrays.asList(runtimes), true);
    }

    private static NGpu withCapability(String key, String value) {
        Map<String, String> caps = new LinkedHashMap<>();
        caps.put(NGpuUtils.VENDOR, NGpuVendor.NVIDIA.id());
        caps.put(key, value);
        return new NGpu("x", new NRam("x", -1, -1, -1), caps);
    }

    /** builds an NGpu carrying the capabilities the linux probe publishes */
    private static NGpu gpu(String name, NGpuVendor vendor, NGpuDeviceType type, String pciBusId,
                            String kernelDriver, int computeCapability, long memory) {
        Map<String, String> caps = new LinkedHashMap<>();
        caps.put(NGpuUtils.PCI_BUS_ID, pciBusId);
        caps.put(NGpuUtils.PCI_DEVICE_ID, "0x28e1");
        caps.put(NGpuUtils.VENDOR, vendor.id());
        caps.put(NGpuUtils.DEVICE_TYPE, type.id());
        if (kernelDriver != null) {
            caps.put(NGpuUtils.KERNEL_DRIVER, kernelDriver);
        }
        if (computeCapability >= 0) {
            // stored the way the vendor tool reports it, major.minor
            caps.put(NGpuUtils.COMPUTE_CAPABILITY,
                    (computeCapability / 100) + "." + ((computeCapability / 10) % 10));
        }
        return new NGpu(name, new NRam(name, memory, -1, -1), caps);
    }

    private static NGpu device(NGpuVendor vendor, NGpuDeviceType type, String pciBusId,
                              String kernelDriver, int computeCapability, long memory) {
        return gpu("model-" + pciBusId, vendor, type, pciBusId, kernelDriver, computeCapability, memory);
    }

    private static NGpu nvidia(String pciBusId, String kernelDriver, int computeCapability, long memory) {
        return device(NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU, pciBusId, kernelDriver, computeCapability, memory);
    }

    private static NGpu named(String modelName, int computeCapability) {
        return gpu(modelName, NGpuVendor.NVIDIA, NGpuDeviceType.DEDICATED_GPU,
                "0000:01:00.0", "nvidia", computeCapability, 6 * GIB);
    }
    /**
     * Feeds hand built runtime lists to the resolution logic of
     * {@link NEnvBase}, everything unrelated to that axis being stubbed out.
     */
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
        public List<NGpu> gpus(){ return Collections.emptyList(); }
        @Override
        public net.thevpc.nuts.platform.NEnv refresh(){ return null; }
    }
}
