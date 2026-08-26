/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * uniform parallel processing runtime family, the <em>software</em> stack able
 * to drive a gpu.
 * <p>
 * This is the software axis, deliberately kept distinct from {@link NGpuVendor}
 * and {@link NGpuDevice} which describe the hardware. The relation is not one to
 * one : {@link #OPENCL}, {@link #VULKAN} and {@link #DIRECTML} run on any
 * vendor's hardware, and a single physical device is usually reachable through
 * several runtimes at once.
 * <p>
 * The list is restricted to stacks that artifacts actually ship variants for,
 * so that every entry stays testable on commonly available hardware. Dedicated
 * ai accelerators, Google TPU, AWS Neuron, Huawei Ascend, Qualcomm QNN and the
 * like, are intentionally absent : no distributable artifact targets them on the
 * jvm, so they cannot take part in artifact resolution. Their names remain
 * accepted by {@link #parse(String)} and resolve to {@link #UNKNOWN}, so that a
 * descriptor mentioning one degrades gracefully instead of failing.
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

    /**
     * Directories holding shared libraries on linux. The layout varies across
     * distributions, openSUSE and Fedora use {@code /usr/lib64}, Debian a
     * multiarch directory, Arch {@code /usr/lib}, so the whole list is stat'ed
     * rather than trusting any single one. {@code ldconfig} is deliberately not
     * used, it has been observed failing to report libraries present on disk.
     */
    private static final String[] LINUX_LIBRARY_DIRS = {
            "/usr/lib64",
            "/usr/lib/x86_64-linux-gnu",
            "/usr/lib/aarch64-linux-gnu",
            "/usr/lib",
            "/usr/local/lib64",
            "/usr/local/lib"
    };

    /**
     * Well known cuda toolkit locations on linux. The nvidia installer uses
     * {@code /usr/local/cuda}, Arch packages {@code /opt/cuda}, Debian
     * {@code /usr/lib/cuda}.
     */
    private static final String[] LINUX_CUDA_TOOLKIT_DIRS = {
            "/usr/local/cuda",
            "/opt/cuda",
            "/usr/lib/cuda"
    };

    private static final NParallelProcessorFamily _curr = _resolveCurrent();

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NParallelProcessorFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    private static NParallelProcessorFamily _resolveCurrent() {
        if (!_canProbe()) return UNKNOWN;
        // vendor native stacks first, they are the most specific
        if (_hasCuda())     return CUDA;
        if (_hasRocm())     return ROCM;
        if (_hasOneApi())   return ONEAPI;
        if (_hasMetal())    return METAL;
        // then cross vendor layers, from most to least capable
        if (_hasDirectMl()) return DIRECTML;
        if (_hasVulkan())   return VULKAN;
        if (_hasOpenCl())   return OPENCL;
        return NONE;
    }

    /**
     * Checks that the environment and filesystem reads the probes rely on are
     * actually permitted. When they are not, probing cannot tell "absent" from
     * "unreadable" and {@link #UNKNOWN} has to be reported instead of
     * {@link #NONE}.
     */
    private static boolean _canProbe() {
        try {
            System.getenv("PATH");
            String home = System.getProperty("user.home");
            if (home != null) {
                Files.isDirectory(Paths.get(home));
            }
            return true;
        } catch (Exception | LinkageError ignored) {
            return false;
        }
    }

    private static boolean _hasCuda() {
        return _hasCudaRuntime() || _hasCudaToolkit();
    }

    /**
     * Whether cuda code can be <em>executed</em>. This needs the driver and the
     * driver api library it ships, never the development toolkit.
     */
    private static boolean _hasCudaRuntime() {
        if (NOsFamily.current() == NOsFamily.MACOS) return false;
        // Most reliable: NVIDIA device file (driver + hardware present)
        if (_deviceExists("/dev/nvidia0")) return true;
        // Driver api library, its directory varies across distributions
        if (_anyFileExists(LINUX_LIBRARY_DIRS, "libcuda.so.1")) return true;
        // Driver binary on PATH
        if (_binaryOnPath("nvidia-smi")) return true;
        if (NOsFamily.current() == NOsFamily.WINDOWS) {
            String sysRoot = System.getenv("SystemRoot");
            if (sysRoot == null) sysRoot = "C:\\Windows";
            if (_fileExists(sysRoot + "\\System32\\nvcuda.dll")) return true;
        }
        return false;
    }

    /**
     * Whether cuda code can be <em>compiled</em>. This needs the toolkit, which
     * is installed separately from the driver and is commonly absent on machines
     * that run cuda binaries perfectly well.
     */
    private static boolean _hasCudaToolkit() {
        if (NOsFamily.current() == NOsFamily.MACOS) return false;
        if (_binaryOnPath("nvcc")) return true;
        if (_envDirExists("CUDA_HOME")) return true;
        if (_envDirExists("CUDA_PATH")) return true;
        if (NOsFamily.current() == NOsFamily.LINUX) {
            for (String d : LINUX_CUDA_TOOLKIT_DIRS) {
                if (_dirExists(d)) return true;
            }
        }
        if (NOsFamily.current() == NOsFamily.WINDOWS) {
            String pf = System.getenv("ProgramFiles");
            if (pf != null && _dirExists(pf + "\\NVIDIA GPU Computing Toolkit\\CUDA")) return true;
        }
        return false;
    }

    private static boolean _hasRocm() {
        return _hasRocmRuntime() || _hasRocmToolkit();
    }

    /**
     * Whether rocm code can be executed. The KFD node is the gate, the amdgpu
     * module alone is not enough.
     */
    private static boolean _hasRocmRuntime() {
        if (_deviceExists("/dev/kfd")) return true;
        if (_anyFileExists(LINUX_LIBRARY_DIRS, "libamdhip64.so")) return true;
        if (_binaryOnPath("rocm-smi")) return true;
        return false;
    }

    /**
     * Whether rocm code can be compiled, which needs the HIP compiler.
     */
    private static boolean _hasRocmToolkit() {
        if (_binaryOnPath("hipcc")) return true;
        if (_envDirExists("ROCM_PATH")) return true;
        if (_envDirExists("HIP_PATH")) return true;
        if (NOsFamily.current() == NOsFamily.LINUX && _dirExists("/opt/rocm")) return true;
        if (NOsFamily.current() == NOsFamily.WINDOWS) {
            String pf = System.getenv("ProgramFiles");
            if (pf != null && _dirExists(pf + "\\AMD\\ROCm")) return true;
        }
        return false;
    }

    private static boolean _hasOneApi() {
        if (_binaryOnPath("sycl-ls")) return true;
        if (_envDirExists("ONEAPI_ROOT")) return true;
        if (_envDirExists("INTEL_OPENVINO_DIR")) return true;
        if (NOsFamily.current() == NOsFamily.LINUX && _dirExists("/opt/intel/oneapi")) return true;
        if (NOsFamily.current() == NOsFamily.WINDOWS) {
            String pf86 = System.getenv("ProgramFiles(x86)");
            if (pf86 != null && _dirExists(pf86 + "\\Intel\\oneAPI")) return true;
        }
        if (NOsFamily.current() == NOsFamily.MACOS && _dirExists("/opt/intel/oneapi")) return true;
        return false;
    }

    private static boolean _hasMetal() {
        // Metal is available on all macOS 10.11+ and iOS 8+ — no separate install
        return NOsFamily.current() == NOsFamily.MACOS;
    }

    private static boolean _hasOpenCl() {
        switch (NOsFamily.current()) {
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
        switch (NOsFamily.current()) {
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
        if (NOsFamily.current() != NOsFamily.WINDOWS) return false;
        String sysRoot = System.getenv("SystemRoot");
        if (sysRoot == null) sysRoot = "C:\\Windows";
        return _fileExists(sysRoot + "\\System32\\DirectML.dll");
    }

    // -------------------------------------------------------------------------
    // Detection helpers — all fail-safe (never throw)
    // -------------------------------------------------------------------------

    private static boolean _binaryOnPath(String name) {
        String binary = (NOsFamily.current() == NOsFamily.WINDOWS) ? name + ".exe" : name;
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

    /**
     * Looks a file up in several candidate directories, the layout of shared
     * libraries not being portable across linux distributions.
     */
    private static boolean _anyFileExists(String[] dirs, String fileName) {
        for (String dir : dirs) {
            try {
                if (Files.exists(Paths.get(dir, fileName))) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static String _firstLine(String path) {
        try {
            Path p = Paths.get(path);
            if (!Files.isReadable(p)) return null;
            for (String line : Files.readAllLines(p)) {
                String s = line.trim();
                if (!s.isEmpty()) return s;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Best effort cuda toolkit version. Recent toolkits publish
     * {@code version.json}, older ones {@code version.txt}, and neither is
     * guaranteed to be present.
     */
    private static String _cudaToolkitVersion() {
        for (String dir : LINUX_CUDA_TOOLKIT_DIRS) {
            String txt = _firstLine(dir + "/version.txt");
            if (txt != null) {
                // "CUDA Version 11.8.0"
                int i = txt.lastIndexOf(' ');
                return i >= 0 ? txt.substring(i + 1) : txt;
            }
            String json = _readVersionFromJson(dir + "/version.json");
            if (json != null) return json;
        }
        return null;
    }

    private static String _readVersionFromJson(String path) {
        try {
            Path p = Paths.get(path);
            if (!Files.isReadable(p)) return null;
            String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            // the cuda block carries the toolkit version, take the first version entry after it
            int at = content.indexOf("\"cuda\"");
            int from = at < 0 ? 0 : at;
            int v = content.indexOf("\"version\"", from);
            if (v < 0) return null;
            int q1 = content.indexOf('"', content.indexOf(':', v) + 1);
            int q2 = q1 < 0 ? -1 : content.indexOf('"', q1 + 1);
            return (q1 < 0 || q2 < 0) ? null : content.substring(q1 + 1, q2);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String _rocmVersion() {
        String v = _firstLine("/opt/rocm/.info/version");
        if (v == null) {
            v = _firstLine("/opt/rocm/.info/version-dev");
        }
        return v;
    }

    // -------------------------------------------------------------------------
    // Availability
    // -------------------------------------------------------------------------

    /**
     * Detects, for every family, whether code targeting it can be executed and
     * whether it can be compiled on this machine.
     * <p>
     * Families that are neither runnable nor buildable are omitted, so an empty
     * list means no parallel processing runtime was found at all. Only
     * {@link #CUDA} and {@link #ROCM} report a distinct toolkit today, the cross
     * vendor layers expose no development kit that can be detected the same way.
     *
     * @return available runtimes, never null
     * @since 1.0.0
     */
    /**
     * Whether the environment and filesystem reads the probes rely on are
     * permitted at all, hence whether an empty {@link #detectAvailable()} means
     * "no runtime is installed" rather than "nothing could be read".
     * <p>
     * This is what lets a caller tell {@link #NONE} from {@link #UNKNOWN}, the
     * two being a positive answer and an absence of information respectively.
     *
     * @return true when probing is possible
     * @since 1.0.0
     */
    public static boolean canDetect() {
        return _canProbe();
    }

    public static List<NParallelProcessorRuntime> detectAvailable() {
        List<NParallelProcessorRuntime> result = new ArrayList<>();
        if (!_canProbe()) {
            return result;
        }
        _addIfAny(result, CUDA, _hasCudaRuntime(), _hasCudaToolkit(), _cudaToolkitVersion());
        _addIfAny(result, ROCM, _hasRocmRuntime(), _hasRocmToolkit(), _rocmVersion());
        _addIfAny(result, ONEAPI, _hasOneApi(), _hasOneApi(), null);
        _addIfAny(result, METAL, _hasMetal(), _hasMetal(), null);
        _addIfAny(result, DIRECTML, _hasDirectMl(), false, null);
        _addIfAny(result, VULKAN, _hasVulkan(), false, null);
        _addIfAny(result, OPENCL, _hasOpenCl(), false, null);
        return result;
    }

    private static void _addIfAny(List<NParallelProcessorRuntime> result, NParallelProcessorFamily family,
                                  boolean runtime, boolean toolkit, String version) {
        if (runtime || toolkit) {
            result.add(NParallelProcessorRuntime.of(family, runtime, toolkit, version));
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
                // industry wide, "xpu" denotes an Intel gpu, it is the pytorch device name
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

                // dedicated accelerators are not resolution targets, their names
                // stay parseable so that descriptors degrade instead of failing
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

    // --- Category helpers ---

    /**
     * Returns true for cross-vendor compute layers that work across multiple gpu
     * brands: {@link #OPENCL}, {@link #VULKAN}, {@link #DIRECTML}.
     *
     * @return true for cross vendor layers
     */
    public boolean isCrossVendor() {
        return this == OPENCL || this == VULKAN || this == DIRECTML;
    }

    /**
     * Returns true only for {@link #NONE}, meaning probing succeeded and found
     * no gpu compute runtime.
     *
     * @return true for {@link #NONE}
     */
    public boolean isNone() {
        return this == NONE;
    }

    /**
     * Returns true only for {@link #UNKNOWN}.
     *
     * @return true for {@link #UNKNOWN}
     */
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
