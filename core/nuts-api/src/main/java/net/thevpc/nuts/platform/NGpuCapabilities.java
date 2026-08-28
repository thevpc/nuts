package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * Well known keys of {@link NGpu#capabilities()}, and typed readers for them.
 * <p>
 * {@link NGpu} describes a device with a name, a memory amount and a bag of
 * string keyed capabilities. That bag is the extension point, but it is
 * untyped : a misspelled key compiles and silently reads as absent, and every
 * caller has to agree on how a value is encoded. This class exists so that
 * neither has to happen. Keys are declared once as constants, and each reader
 * below owns the encoding of its own value.
 * <p>
 * Readers never throw. A capability that is absent, or present but not
 * parseable, reads as unknown : {@code false} for the boolean predicates and a
 * negative value for {@link #computeCapability(NGpu)}.
 * <p>
 * Which keys are actually populated depends on how the device was detected.
 * The linux probe fills all of them, other platforms and remote targets fill
 * what their vendor tooling reports, so callers must treat every key as
 * optional.
 *
 * @author thevpc
 * @app.category Base
 * @since 1.0.0
 */
public final class NGpuCapabilities {

    /**
     * Pci address, as an example {@code 0000:01:00.0}. Stable identity of a
     * device, and the key to use when merging readings from several sources.
     */
    public static final String PCI_BUS_ID = "pci.bus.id";

    /**
     * Raw pci device id, as an example {@code 0x28e1}.
     */
    public static final String PCI_DEVICE_ID = "pci.device.id";

    /**
     * Hardware vendor, holding an {@link NGpuVendor} id such as {@code nvidia}.
     */
    public static final String VENDOR = "vendor";

    /**
     * Device kind, holding an {@link NGpuDeviceType} id such as
     * {@code dedicated-gpu}.
     */
    public static final String DEVICE_TYPE = "device.type";

    /**
     * Kernel module currently bound to the device, as an example
     * {@code nvidia}, {@code nouveau} or {@code i915}.
     */
    public static final String KERNEL_DRIVER = "kernel.driver";

    /**
     * Vendor driver version, as an example {@code 580.159.03}.
     */
    public static final String DRIVER_VERSION = "driver.version";

    /**
     * Vendor assigned device uuid.
     */
    public static final String UUID = "uuid";

    /**
     * Compute capability as reported by the vendor tool, in the
     * {@code major.minor} form such as {@code 8.9}. Read it through
     * {@link #computeCapability(NGpu)} rather than parsing it, the encoding
     * being the very thing callers tend to disagree on.
     * <p>
     * This key is already published by the nvidia-smi based detection, so it is
     * available on every platform where that tool runs, not only on linux.
     */
    public static final String COMPUTE_CAPABILITY = "compute.capability";

    private NGpuCapabilities() {
    }

    /**
     * Hardware vendor of a device.
     *
     * @param gpu device to read
     * @return the vendor, {@link NGpuVendor#UNKNOWN} when not reported
     */
    public static NGpuVendor vendor(NGpu gpu) {
        return read(gpu, VENDOR).flatMap(NGpuVendor::parse).orElse(NGpuVendor.UNKNOWN);
    }

    /**
     * Kind of device.
     *
     * @param gpu device to read
     * @return the kind, {@link NGpuDeviceType#UNKNOWN} when not reported
     */
    public static NGpuDeviceType deviceType(NGpu gpu) {
        return read(gpu, DEVICE_TYPE).flatMap(NGpuDeviceType::parse).orElse(NGpuDeviceType.UNKNOWN);
    }

    /**
     * Compute capability encoded as {@code major * 100 + minor * 10}, so that
     * compute capability 8.9 reads as {@code 890} and capabilities stay
     * directly comparable against the {@code CC_} constants of this class.
     *
     * @param gpu device to read
     * @return encoded compute capability, negative when unknown
     */
    public static int computeCapability(NGpu gpu) {
        String value = read(gpu, COMPUTE_CAPABILITY).orNull();
        if (value == null) {
            return -1;
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0].trim()) * 100 + Integer.parseInt(parts[1].trim()) * 10;
        } catch (Exception ignored) {
            return -1;
        }
    }

    /**
     * Returns true when the vendor's own compute stack can actually drive this
     * device, which requires the vendor's kernel module to be the one bound.
     * <p>
     * An NVIDIA device bound to {@code nouveau} returns false : the hardware is
     * present and drives the display, yet no CUDA capability is available.
     * Returns false as well when the bound module was not reported, absence of
     * evidence being reported as absence of capability here.
     *
     * @param gpu device to read
     * @return true when the vendor compute stack is usable
     */
    public static boolean isComputeCapable(NGpu gpu) {
        String expected = vendor(gpu).getComputeKernelModule();
        return expected != null && expected.equals(read(gpu, KERNEL_DRIVER).orNull());
    }

    /**
     * Returns true when tensor cores are available.
     * <p>
     * Compute capability alone is not sufficient : a handful of Turing parts
     * reach {@link NGpuCapabilities#CC_TURING} without shipping tensor cores and
     * are excluded by model name. Deriving this from
     * {@link #COMPUTE_CAPABILITY} alone, as a caller reading the raw capability
     * would, overstates those models.
     *
     * @param gpu device to read
     * @return true when tensor cores are available
     */
    public static boolean hasTensorCores(NGpu gpu) {
        if (vendor(gpu) != NGpuVendor.NVIDIA || computeCapability(gpu) < CC_TURING) {
            return false;
        }
        String modelName = gpu == null ? null : gpu.name();
        if (modelName != null) {
            for (String s : TURING_WITHOUT_TENSOR_CORES_EXACT) {
                if (s.equals(modelName)) {
                    return false;
                }
            }
            if (modelName.startsWith(TURING_WITHOUT_TENSOR_CORES_PREFIX)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true when half precision arithmetic is supported. Only NVIDIA
     * devices are concluded upon, detection for other vendors not being
     * implemented, so those report false rather than a guess.
     *
     * @param gpu device to read
     * @return true when fp16 is supported
     */
    public static boolean isSupportedFp16(NGpu gpu) {
        return vendor(gpu) == NGpuVendor.NVIDIA && computeCapability(gpu) >= CC_PASCAL;
    }

    /**
     * Returns true when bfloat16 arithmetic is supported. NVIDIA only, see
     * {@link #isSupportedFp16(NGpu)}.
     *
     * @param gpu device to read
     * @return true when bf16 is supported
     */
    public static boolean isSupportedBf16(NGpu gpu) {
        return vendor(gpu) == NGpuVendor.NVIDIA && computeCapability(gpu) >= CC_AMPERE;
    }

    /**
     * Returns true when integer 8 bits arithmetic is supported, which the
     * {@code __dp4a} intrinsic provides from Pascal onwards. NVIDIA only, see
     * {@link #isSupportedFp16(NGpu)}.
     *
     * @param gpu device to read
     * @return true when int8 is supported
     */
    public static boolean isSupportedInt8(NGpu gpu) {
        return vendor(gpu) == NGpuVendor.NVIDIA && computeCapability(gpu) >= CC_DP4A;
    }

    /**
     * Returns true when integer 4 bits arithmetic is supported, which is a
     * tensor core only capability.
     *
     * @param gpu device to read
     * @return true when int4 is supported
     */
    public static boolean isSupportedInt4(NGpu gpu) {
        return hasTensorCores(gpu);
    }

    /**
     * The device to use when a single one has to be picked.
     * <p>
     * A dedicated device wins over an integrated one, the largest memory being
     * the tie breaker, and only compute capable devices are eligible. The choice
     * is deterministic so that resolution stays reproducible in unattended runs.
     * <p>
     * The system property {@value #PRIMARY_GPU_PROPERTY} forces a device by pci
     * address, in the spirit of {@code CUDA_VISIBLE_DEVICES}. It is ignored when
     * it matches no listed device.
     *
     * @param gpus devices to choose from, as returned by {@link NEnv#gpus()}
     * @return the primary device, empty when none is eligible
     */
    public static NOptional<NGpu> primary(List<NGpu> gpus) {
        if (gpus == null || gpus.isEmpty()) {
            return NOptional.ofEmpty();
        }
        String forced = System.getProperty(PRIMARY_GPU_PROPERTY);
        if (forced != null && !forced.trim().isEmpty()) {
            String f = forced.trim();
            for (NGpu g : gpus) {
                if (f.equals(read(g, PCI_BUS_ID).orNull())) {
                    return NOptional.of(g);
                }
            }
        }
        NGpu best = null;
        for (NGpu g : gpus) {
            if (!isComputeCapable(g)) {
                continue;
            }
            if (best == null || isBetterPrimary(g, best)) {
                best = g;
            }
        }
        return best == null ? NOptional.<NGpu>ofEmpty() : NOptional.of(best);
    }

    private static boolean isBetterPrimary(NGpu candidate, NGpu current) {
        boolean candidateDedicated = deviceType(candidate) == NGpuDeviceType.DEDICATED_GPU;
        boolean currentDedicated = deviceType(current) == NGpuDeviceType.DEDICATED_GPU;
        if (candidateDedicated != currentDedicated) {
            return candidateDedicated;
        }
        return totalMemory(candidate) > totalMemory(current);
    }

    private static long totalMemory(NGpu gpu) {
        return gpu == null || gpu.vram() == null ? -1 : gpu.vram().total();
    }

    private static NOptional<String> read(NGpu gpu, String key) {
        return gpu == null ? NOptional.<String>ofEmpty() : gpu.capability(key);
    }

    /**
     * System property forcing the primary gpu device, holding a pci address
     * such as {@code 0000:01:00.0}.
     */
    public static final String PRIMARY_GPU_PROPERTY = "nuts.gpu.device";

    /**
     * Minimum compute capability for fast half precision arithmetic, NVIDIA Pascal.
     */
    public static final int CC_PASCAL = 600;

    /**
     * Minimum compute capability for {@code __dp4a}, the byte-wise dot product
     * intrinsic backing integer 8 bits arithmetic.
     */
    public static final int CC_DP4A = 610;

    /**
     * Minimum compute capability for tensor core matrix operations, NVIDIA Turing.
     */
    public static final int CC_TURING = 750;

    /**
     * Minimum compute capability for bfloat16, NVIDIA Ampere.
     */
    public static final int CC_AMPERE = 800;

    /**
     * Turing parts that reach {@link #CC_TURING} yet ship without tensor cores,
     * so that the compute capability alone would overstate their capabilities.
     * <p>
     * impl-note: list borrowed from llama.cpp, which special cases the very same
     * models by marketing name because no property exposed by the driver
     * distinguishes them.
     */
    private static final String[] TURING_WITHOUT_TENSOR_CORES_EXACT = {
            "NVIDIA GeForce MX450",
            "NVIDIA GeForce MX550"
    };

    /**
     * Model name prefix of Turing parts shipped without tensor cores, covering
     * the whole GeForce GTX 16 series.
     */
    private static final String TURING_WITHOUT_TENSOR_CORES_PREFIX = "NVIDIA GeForce GTX 16";
}
