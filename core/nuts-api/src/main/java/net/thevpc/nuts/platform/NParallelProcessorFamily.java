package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum NParallelProcessorFamily implements NEnum {

    CUDA,
    ROCM,
    ONEAPI,
    METAL,
    OPENCL,
    VULKAN,
    DIRECTML,
    CANN,
    HABANA,
    NEURON,
    TPU,
    QNN,
    XPU,
    MLU,
    IPU,
    VITIS,
    UNKNOWN;

    private static final NParallelProcessorFamily _curr = _resolveCurrent();
    private final String id;

    NParallelProcessorFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    private static NParallelProcessorFamily _resolveCurrent() {
        if (_hasCuda())     return CUDA;
        if (_hasRocm())     return ROCM;
        if (_hasOneApi())   return ONEAPI;
        if (_hasCann())     return CANN;
        if (_hasHabana())   return HABANA;
        if (_hasNeuron())   return NEURON;
        if (_hasTpu())      return TPU;
        if (_hasQnn())      return QNN;
        if (_hasXpu())      return XPU;
        if (_hasMlu())      return MLU;
        if (_hasIpu())      return IPU;
        if (_hasVitis())    return VITIS;
        if (_hasMetal())    return METAL;
        if (_hasDirectMl()) return DIRECTML;
        if (_hasVulkan())   return VULKAN;
        if (_hasOpenCl())   return OPENCL;
        return UNKNOWN;
    }

    private static boolean _hasCuda() {
        if (NOsFamily.getCurrent() == NOsFamily.MACOS) return false;
        // Most reliable: NVIDIA device file (driver + hardware present)
        if (_deviceExists("/dev/nvidia0")) return true;
        // Driver binary on PATH
        if (_binaryOnPath("nvidia-smi")) return true;
        // Toolkit directory / env var
        if (_envDirExists("CUDA_HOME")) return true;
        if (_envDirExists("CUDA_PATH")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.LINUX && _dirExists("/usr/local/cuda")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.WINDOWS) {
            String pf = System.getenv("ProgramFiles");
            if (pf != null && _dirExists(pf + "\\NVIDIA GPU Computing Toolkit\\CUDA")) return true;
        }
        return false;
    }

    private static boolean _hasRocm() {
        // Most reliable: KFD (Kernel Fusion Driver) device node
        if (_deviceExists("/dev/kfd")) return true;
        if (_binaryOnPath("rocm-smi")) return true;
        if (_envDirExists("ROCM_PATH")) return true;
        if (_envDirExists("HIP_PATH")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.LINUX && _dirExists("/opt/rocm")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.WINDOWS) {
            String pf = System.getenv("ProgramFiles");
            if (pf != null && _dirExists(pf + "\\AMD\\ROCm")) return true;
        }
        return false;
    }

    private static boolean _hasOneApi() {
        if (_binaryOnPath("sycl-ls")) return true;
        if (_envDirExists("ONEAPI_ROOT")) return true;
        if (_envDirExists("INTEL_OPENVINO_DIR")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.LINUX && _dirExists("/opt/intel/oneapi")) return true;
        if (NOsFamily.getCurrent() == NOsFamily.WINDOWS) {
            String pf86 = System.getenv("ProgramFiles(x86)");
            if (pf86 != null && _dirExists(pf86 + "\\Intel\\oneAPI")) return true;
        }
        if (NOsFamily.getCurrent() == NOsFamily.MACOS && _dirExists("/opt/intel/oneapi")) return true;
        return false;
    }

    private static boolean _hasMetal() {
        // Metal is available on all macOS 10.11+ and iOS 8+ — no separate install
        return NOsFamily.getCurrent() == NOsFamily.MACOS;
    }

    private static boolean _hasOpenCl() {
        switch (NOsFamily.getCurrent()) {
            case LINUX:
                return _dirExists("/etc/OpenCL/vendors");
            case WINDOWS: {
                String sysRoot = System.getenv("SystemRoot");
                if (sysRoot == null) sysRoot = "C:\\Windows";
                return _fileExists(sysRoot + "\\System32\\OpenCL.dll");
            }
            case MACOS:
                return _dirExists("/System/Library/Frameworks/OpenCL.framework");
            default:
                return false;
        }
    }

    private static boolean _hasVulkan() {
        switch (NOsFamily.getCurrent()) {
            case LINUX: {
                if (_dirExists("/usr/share/vulkan/icd.d")) return true;
                String home = System.getProperty("user.home");
                return home != null && _dirExists(home + "/.local/share/vulkan/icd.d");
            }
            case WINDOWS: {
                String pd = System.getenv("ProgramData");
                return pd != null && _dirExists(pd + "\\Vulkan\\icd.d");
            }
            case MACOS:
                // MoltenVK
                return _dirExists("/usr/local/share/vulkan/icd.d");
            default:
                return false;
        }
    }

    private static boolean _hasDirectMl() {
        if (NOsFamily.getCurrent() != NOsFamily.WINDOWS) return false;
        String sysRoot = System.getenv("SystemRoot");
        if (sysRoot == null) sysRoot = "C:\\Windows";
        return _fileExists(sysRoot + "\\System32\\DirectML.dll");
    }

    private static boolean _hasCann() {
        if (_deviceExists("/dev/davinci0")) return true;
        if (_binaryOnPath("npu-smi")) return true;
        if (_envDirExists("ASCEND_HOME")) return true;
        if (_envDirExists("ASCEND_TOOLKIT_HOME")) return true;
        if (_dirExists("/usr/local/Ascend")) return true;
        return false;
    }

    private static boolean _hasHabana() {
        if (_deviceExists("/dev/hl0")) return true;
        if (_binaryOnPath("hl-smi")) return true;
        if (_dirExists("/opt/habanalabs")) return true;
        // SynapseAI sets this env var when initialized
        return System.getenv("HABANA_LOGS") != null;
    }

    private static boolean _hasNeuron() {
        if (_deviceExists("/dev/neuron0")) return true;
        if (_binaryOnPath("neuron-ls")) return true;
        if (_dirExists("/opt/aws/neuron")) return true;
        return false;
    }

    private static boolean _hasTpu() {
        // TPU character device (Google Cloud)
        if (_deviceExists("/dev/accel0")) return true;
        String tpuName = System.getenv("TPU_NAME");
        return tpuName != null && !tpuName.trim().isEmpty();
    }

    private static boolean _hasQnn() {
        if (_envDirExists("QNN_SDK_ROOT")) return true;
        // Qualcomm QNN shared library on Linux/Android
        if (_fileExists("/usr/lib/libQnnSystem.so")) return true;
        if (_fileExists("/usr/lib64/libQnnSystem.so")) return true;
        return false;
    }

    private static boolean _hasXpu() {
        if (_envDirExists("XPU_RUNTIME_ROOT")) return true;
        if (_dirExists("/usr/local/xpu_runtime")) return true;
        return false;
    }

    private static boolean _hasMlu() {
        if (_deviceExists("/dev/cambricon_dev0")) return true;
        if (_binaryOnPath("cnmon")) return true;
        if (_dirExists("/usr/local/neuware")) return true;
        return false;
    }

    private static boolean _hasIpu() {
        String poplar = System.getenv("POPLAR_SDK_ENABLED");
        if (poplar != null && !poplar.trim().isEmpty()) return true;
        if (_binaryOnPath("gc-info")) return true;
        if (_dirExists("/opt/gc/poplar")) return true;
        return false;
    }

    private static boolean _hasVitis() {
        if (_deviceExists("/dev/xclmgmt0")) return true;
        if (_binaryOnPath("xbutil")) return true;
        if (_envDirExists("XILINX_XRT")) return true;
        if (_dirExists("/opt/xilinx/xrt")) return true;
        return false;
    }

    // -------------------------------------------------------------------------
    // Detection helpers — all fail-safe (never throw)
    // -------------------------------------------------------------------------

    private static boolean _binaryOnPath(String name) {
        String binary = (NOsFamily.getCurrent() == NOsFamily.WINDOWS) ? name + ".exe" : name;
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return false;
        for (String dir : pathEnv.split(File.pathSeparator)) {
            try {
                if (Files.isRegularFile(Paths.get(dir, binary))) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static boolean _envDirExists(String envVar) {
        String v = System.getenv(envVar);
        if (v == null || v.trim().isEmpty()) return false;
        try {
            return Files.isDirectory(Paths.get(v));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean _dirExists(String path) {
        try {
            return Files.isDirectory(Paths.get(path));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean _fileExists(String path) {
        try {
            return Files.isRegularFile(Paths.get(path));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean _deviceExists(String path) {
        try {
            return Files.exists(Paths.get(path));
        } catch (Exception ignored) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Parse
    // -------------------------------------------------------------------------

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

                case "CANN":
                case "ASCEND":
                case "HUAWEI":
                    return NOptional.of(CANN);

                case "HABANA":
                case "GAUDI":
                case "SYNAPSE":
                case "SYNAPSEAI":
                case "HPU":
                    return NOptional.of(HABANA);

                case "NEURON":
                case "TRAINIUM":
                case "INFERENTIA":
                    return NOptional.of(NEURON);

                case "TPU":
                case "XLA":
                case "GOOGLE_TPU":
                    return NOptional.of(TPU);

                case "QNN":
                case "SNPE":
                case "HEXAGON":
                case "QUALCOMM":
                    return NOptional.of(QNN);

                case "XPU":
                case "KUNLUN":
                case "BAIDU":
                    return NOptional.of(XPU);

                case "MLU":
                case "BANG":
                case "CAMBRICON":
                    return NOptional.of(MLU);

                case "IPU":
                case "POPLAR":
                case "GRAPHCORE":
                    return NOptional.of(IPU);

                case "VITIS":
                case "XRT":
                case "FPGA":
                case "XILINX":
                    return NOptional.of(VITIS);

                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
            }
            // Handle versioned strings: "cuda11", "cuda12", "rocm5", "rocm6", etc.
            if (n.startsWith("CUDA"))   return NOptional.of(CUDA);
            if (n.startsWith("ROCM"))   return NOptional.of(ROCM);
            if (n.startsWith("ONEAPI")) return NOptional.of(ONEAPI);
            if (n.startsWith("OPENCL")) return NOptional.of(OPENCL);
            return null;
        });
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public static NParallelProcessorFamily getCurrent() {
        return _curr;
    }

    // --- Vendor helpers ---

    /** NVIDIA family: {@link #CUDA} */
    public boolean isNvidiaFamily() {
        return this == CUDA;
    }

    /** AMD family: {@link #ROCM}, {@link #VITIS} */
    public boolean isAmdFamily() {
        return this == ROCM || this == VITIS;
    }

    /** Intel family: {@link #ONEAPI}, {@link #HABANA} */
    public boolean isIntelFamily() {
        return this == ONEAPI || this == HABANA;
    }

    /** Apple family: {@link #METAL} */
    public boolean isAppleFamily() {
        return this == METAL;
    }

    /** Qualcomm family: {@link #QNN} */
    public boolean isQualcommFamily() {
        return this == QNN;
    }

    /** Google family: {@link #TPU} */
    public boolean isGoogleFamily() {
        return this == TPU;
    }

    /** Huawei family: {@link #CANN} */
    public boolean isHuaweiFamily() {
        return this == CANN;
    }

    /** AWS family: {@link #NEURON} */
    public boolean isAwsFamily() {
        return this == NEURON;
    }

    /** Baidu family: {@link #XPU} */
    public boolean isBaiduFamily() {
        return this == XPU;
    }

    /** Cambricon family: {@link #MLU} */
    public boolean isCambriconFamily() {
        return this == MLU;
    }

    /** Graphcore family: {@link #IPU} */
    public boolean isGraphcoreFamily() {
        return this == IPU;
    }

    // --- Category helpers ---

    /**
     * Returns true for GPU-based accelerators:
     * {@link #CUDA}, {@link #ROCM}, {@link #ONEAPI}, {@link #METAL},
     * {@link #OPENCL}, {@link #VULKAN}, {@link #DIRECTML}.
     */
    public boolean isGpu() {
        switch (this) {
            case CUDA:
            case ROCM:
            case ONEAPI:
            case METAL:
            case OPENCL:
            case VULKAN:
            case DIRECTML:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true for dedicated AI/ML chips (NPU, TPU, HPU, IPU):
     * {@link #CANN}, {@link #HABANA}, {@link #NEURON}, {@link #TPU},
     * {@link #QNN}, {@link #XPU}, {@link #MLU}, {@link #IPU}.
     */
    public boolean isNpu() {
        switch (this) {
            case CANN:
            case HABANA:
            case NEURON:
            case TPU:
            case QNN:
            case XPU:
            case MLU:
            case IPU:
                return true;
            default:
                return false;
        }
    }

    /** Returns true for FPGA accelerators: {@link #VITIS}. */
    public boolean isFpga() {
        return this == VITIS;
    }

    /**
     * Returns true for cross-vendor compute layers that work across multiple
     * GPU brands: {@link #OPENCL}, {@link #VULKAN}, {@link #DIRECTML}.
     */
    public boolean isCrossVendor() {
        return this == OPENCL || this == VULKAN || this == DIRECTML;
    }

    /**
     * Returns true for accelerators that are only available inside a specific
     * cloud provider's infrastructure: {@link #TPU}, {@link #NEURON}.
     */
    public boolean isCloudOnly() {
        return this == TPU || this == NEURON;
    }

    /** Returns true only for {@link #UNKNOWN}. */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    @Override
    public String id() {
        return id;
    }
}
