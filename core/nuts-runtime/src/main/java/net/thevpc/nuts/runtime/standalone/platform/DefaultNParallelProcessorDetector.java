package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NParallelProcessorFamily;
import net.thevpc.nuts.platform.NParallelProcessorRuntime;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles OS-level scanning and detection of available parallel processor runtimes.
 *
 * @author thevpc
 * @since 1.0.0
 */
public final class DefaultNParallelProcessorDetector {

    private static final String[] LINUX_LIBRARY_DIRS = {
            "/usr/lib64",
            "/usr/lib/x86_64-linux-gnu",
            "/usr/lib/aarch64-linux-gnu",
            "/usr/lib",
            "/usr/local/lib64",
            "/usr/local/lib"
    };

    private static final String[] LINUX_CUDA_TOOLKIT_DIRS = {
            "/usr/local/cuda",
            "/opt/cuda",
            "/usr/lib/cuda"
    };

    private DefaultNParallelProcessorDetector() {
    }

    public static boolean canProbe() {
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

    public static List<NParallelProcessorRuntime> detectAvailable() {
        List<NParallelProcessorRuntime> result = new ArrayList<>();
        if (!canProbe()) {
            return result;
        }
        _addIfAny(result, NParallelProcessorFamily.CUDA, _hasCudaRuntime(), _hasCudaToolkit(), _cudaToolkitVersion());
        _addIfAny(result, NParallelProcessorFamily.ROCM, _hasRocmRuntime(), _hasRocmToolkit(), _rocmVersion());
        _addIfAny(result, NParallelProcessorFamily.ONEAPI, _hasOneApi(), _hasOneApi(), null);
        _addIfAny(result, NParallelProcessorFamily.METAL, _hasMetal(), _hasMetal(), null);
        _addIfAny(result, NParallelProcessorFamily.DIRECTML, _hasDirectMl(), false, null);
        _addIfAny(result, NParallelProcessorFamily.VULKAN, _hasVulkan(), false, null);
        _addIfAny(result, NParallelProcessorFamily.OPENCL, _hasOpenCl(), false, null);
        return result;
    }

    public static NParallelProcessorFamily resolve(List<NParallelProcessorRuntime> runtimes, boolean detectionSupported) {
        if (runtimes == null || runtimes.isEmpty()) {
            return detectionSupported ? NParallelProcessorFamily.NONE : NParallelProcessorFamily.UNKNOWN;
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

    private static void _addIfAny(List<NParallelProcessorRuntime> result, NParallelProcessorFamily family,
                                  boolean runtime, boolean toolkit, String version) {
        if (runtime || toolkit) {
            result.add(NParallelProcessorRuntime.of(family, runtime, toolkit, version));
        }
    }

    private static boolean _hasCudaRuntime() {
        if (NOsFamily.current() == NOsFamily.MACOS) return false;
        if (_deviceExists("/dev/nvidia0")) return true;
        if (_anyFileExists(LINUX_LIBRARY_DIRS, "libcuda.so.1")) return true;
        if (_binaryOnPath("nvidia-smi")) return true;
        if (NOsFamily.current() == NOsFamily.WINDOWS) {
            String sysRoot = System.getenv("SystemRoot");
            if (sysRoot == null) sysRoot = "C:\\Windows";
            if (_fileExists(sysRoot + "\\System32\\nvcuda.dll")) return true;
        }
        return false;
    }

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

    private static boolean _hasRocmRuntime() {
        if (_deviceExists("/dev/kfd")) return true;
        if (_anyFileExists(LINUX_LIBRARY_DIRS, "libamdhip64.so")) return true;
        if (_binaryOnPath("rocm-smi")) return true;
        return false;
    }

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

    private static String _cudaToolkitVersion() {
        for (String dir : LINUX_CUDA_TOOLKIT_DIRS) {
            String txt = _firstLine(dir + "/version.txt");
            if (txt != null) {
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
}
