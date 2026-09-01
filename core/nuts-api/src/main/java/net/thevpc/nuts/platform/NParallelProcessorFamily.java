package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * Uniform parallel processing runtime family, the <em>software</em> stack able
 * to drive a GPU.
 *
 * @author thevpc
 * @app.category Base
 * @since 1.0.0
 */
public enum NParallelProcessorFamily implements NEnum {

    /**
     * NVIDIA CUDA.
     */
    CUDA,

    /**
     * AMD ROCm, and its HIP programming interface.
     */
    ROCM,

    /**
     * Intel oneAPI, covering SYCL and Level Zero.
     */
    ONEAPI,

    /**
     * Apple Metal, and Metal Performance Shaders.
     */
    METAL,

    /**
     * Khronos Vulkan compute, cross vendor.
     */
    VULKAN,

    /**
     * Khronos OpenCL, cross vendor.
     */
    OPENCL,

    /**
     * Microsoft DirectML, cross vendor, windows only.
     */
    DIRECTML,

    /**
     * Probing succeeded and no gpu compute runtime is available on this machine.
     * This is a positive answer, as opposed to {@link #UNKNOWN}.
     */
    NONE,

    /**
     * The available runtime could not be determined, either because probing was
     * not possible or because a parsed value was not recognized. This is an
     * absence of information, as opposed to {@link #NONE}.
     */
    UNKNOWN;

    private final String id;

    NParallelProcessorFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Whether probing is supported in the current environment.
     *
     * @return true when probing is possible
     * @since 1.0.0
     */
    public static boolean canDetect() {
        try {
            return NEnv.of().parallelProcessorRuntimes() != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Detects available parallel processing runtimes in the current environment.
     *
     * @return available runtimes, never null
     * @since 1.0.0
     */
    public static List<NParallelProcessorRuntime> detectAvailable() {
        try {
            return NEnv.of().parallelProcessorRuntimes();
        } catch (Exception ignored) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Picks the single family to use out of a detected runtime list.
     *
     * @param runtimes           detected runtimes
     * @param detectionSupported whether probing was possible at all
     * @return the resolved family, {@link #NONE} or {@link #UNKNOWN} when the list is empty
     * @since 1.0.0
     */
    public static NParallelProcessorFamily resolve(List<NParallelProcessorRuntime> runtimes, boolean detectionSupported) {
        if (runtimes == null || runtimes.isEmpty()) {
            return detectionSupported ? NONE : UNKNOWN;
        }
        for (NParallelProcessorRuntime r : runtimes) {
            if (r.isRuntimeAvailable() && !r.getFamily().isCrossVendor()) {
                return r.getFamily();
            }
        }
        for (NParallelProcessorRuntime r : runtimes) {
            if (r.isRuntimeAvailable()) {
                return r.getFamily();
            }
        }
        return runtimes.get(0).getFamily();
    }

    public static NOptional<NParallelProcessorFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NParallelProcessorFamily.class, s -> {
            String n = s.normalizedValue();
            switch (n) {
                case "CUDA":
                case "NVIDIA":
                    return NOptional.of(CUDA);

                case "ROCM":
                case "HIP":
                case "AMD_GPU":
                    return NOptional.of(ROCM);

                case "ONEAPI":
                case "ONE_API":
                case "SYCL":
                case "LEVEL_ZERO":
                case "LEVELZERO":
                case "INTEL_GPU":
                case "XPU":
                    return NOptional.of(ONEAPI);

                case "METAL":
                case "MPS":
                case "METAL_PERFORMANCE_SHADERS":
                    return NOptional.of(METAL);

                case "OPENCL":
                case "OPEN_CL":
                    return NOptional.of(OPENCL);

                case "VULKAN":
                    return NOptional.of(VULKAN);

                case "DIRECTML":
                case "DIRECT_ML":
                case "DML":
                    return NOptional.of(DIRECTML);

                case "NONE":
                case "CPU":
                    return NOptional.of(NONE);

                case "CANN":
                case "ASCEND":
                case "HUAWEI":
                case "HABANA":
                case "GAUDI":
                case "SYNAPSE":
                case "SYNAPSEAI":
                case "HPU":
                case "NEURON":
                case "TRAINIUM":
                case "INFERENTIA":
                case "TPU":
                case "XLA":
                case "GOOGLE_TPU":
                case "QNN":
                case "SNPE":
                case "HEXAGON":
                case "QUALCOMM":
                case "KUNLUN":
                case "BAIDU":
                case "MLU":
                case "BANG":
                case "CAMBRICON":
                case "IPU":
                case "POPLAR":
                case "GRAPHCORE":
                case "VITIS":
                case "XRT":
                case "FPGA":
                case "XILINX":
                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
            }
            if (n.startsWith("CUDA"))   return NOptional.of(CUDA);
            if (n.startsWith("ROCM"))   return NOptional.of(ROCM);
            if (n.startsWith("ONEAPI")) return NOptional.of(ONEAPI);
            if (n.startsWith("OPENCL")) return NOptional.of(OPENCL);
            return null;
        });
    }

    public boolean isCrossVendor() {
        return this == OPENCL || this == VULKAN || this == DIRECTML;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    @Override
    public String id() {
        return id;
    }
}
