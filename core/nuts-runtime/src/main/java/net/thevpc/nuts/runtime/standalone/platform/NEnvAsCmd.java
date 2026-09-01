package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NEnvCmdSPI;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.util.*;

@NComponentScope(NScopeType.PROTOTYPE)
public class NEnvAsCmd extends NEnvBase {

    private final NEnvCmdSPI envCmdSPI;
    private boolean valid;
    private Map<String, String> envSnapshot;

    public NEnvAsCmd(NScorableContext context) {
        Object criteria = context.criteria();
        this.envCmdSPI = (NEnvCmdSPI) criteria;
    }

    private NEnvAsCmd(NEnvCmdSPI envCmdSPI) {
        this.envCmdSPI = envCmdSPI;
    }

    @Override
    public NEnv refresh() {
        return new NEnvAsCmd(envCmdSPI);
    }

    public NConnectionString connectionString() {
        return envCmdSPI.targetConnectionString();
    }

    public synchronized boolean tryUpdate() {
        if (!valid) {
            update();
            valid = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isNativeImage() {
        return false;
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object criteria = context.criteria();
        if (criteria instanceof NEnvCmdSPI) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public String pid() {
        return null;
    }

    private synchronized void update() {
        //test for posix
        boolean ok = false;
        try {
            String cmd =
                    "sh -c 'echo -n \"$(uname -s)|$(uname -r)|$(uname -m)|$(whoami)|${HOME}|$SHELL\"; " +
                            "v=$($SHELL --version 2>/dev/null | head -n1 || " +
                            "$SHELL -version 2>/dev/null | head -n1 || " +
                            "$SHELL version 2>/dev/null | head -n1 || echo unknown); " +
                            "echo \"$v\"'";
            String result = runOnceSystemGrab(cmd);
            if (!NBlankable.isBlank(result)) {
                List<String> cols = NStringUtils.split(result, "|", false, false);
                if (cols.size() >= 6) {
                    String luname = cols.get(0).toLowerCase();
                    os = NId.of(null, cols.get(0), cols.get(1));
                    if (luname.startsWith("linux")) {
                        osFamily = NOsFamily.LINUX;
                    } else if (luname.startsWith("darwin")) {
                        osFamily = NOsFamily.MACOS;
                    } else if (luname.startsWith("sunos")) {
                        osFamily = NOsFamily.UNIX;
                    } else if (
                            luname.startsWith("freebsd")
                                    || luname.startsWith("openbsd")
                                    || luname.startsWith("netbsd")
                    ) {
                        osFamily = NOsFamily.UNIX;
                    }
                    arch = NId.of(null, cols.get(2));
                    archFamily = NArchFamily.parse(cols.get(2)).orElse(NArchFamily.UNKNOWN);
                    userName = cols.get(3);
                    userHome = cols.get(4);
                    shellFamily = NShellFamily.parse(cols.get(5)).orElse(NShellFamily.SH);
                    shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(5), shellFamily.id()), cols.get(6));
                    rootUserName = "root";
                    ok = true;
                }
            }
        } catch (Exception ex) {
            // not posix
        }
        if (!ok) {
            try {
                String cmd =
                        "powershell -NoProfile -Command \"& {" +
                                "$os='Windows'; " +
                                "$osver=[System.Environment]::OSVersion.Version.ToString(); " +
                                "$arch=$env:PROCESSOR_ARCHITECTURE; " +
                                "$user=$env:USERNAME; $homedir=$env:USERPROFILE; " +
                                "$shell=(Get-Command pwsh -ErrorAction SilentlyContinue).Name; " +
                                "$shellver=$PSVersionTable.PSVersion.ToString(); " +
                                "Write-Output ($os+'|'+$osver+'|'+$user+'|'+$homedir+'|'+$shell+'|'+$shellver+'|'+$arch)}\"";
                String result = runOnceSystemGrab(cmd);
                if (!NBlankable.isBlank(result)) {
                    List<String> cols = NStringUtils.split(result, "|", true, false);
                    if (cols.size() >= 6) {
                        String luname = cols.get(0).toLowerCase();
                        os = NId.of(null, cols.get(0), cols.get(1));
                        osFamily = NOsFamily.WINDOWS;
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_POWER_SHELL);
                        shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
                        arch = NId.of(null, cols.get(6));
                        archFamily = NArchFamily.parse(cols.get(6)).orElse(NArchFamily.UNKNOWN);
                        rootUserName=getWindowsAdminName(NShellFamily.WIN_CMD);
                        ok = true;
                    }
                }
            } catch (Exception ex) {
                // not posix
            }
        }
        if (!ok) {
            try {
                String cmd = "cmd /c \"echo Windows|%OS%|%USERNAME%|%USERPROFILE%|cmd|unknown|%PROCESSOR_ARCHITECTURE%\"";
                String result = runOnceSystemGrab(cmd);
                if (!NBlankable.isBlank(result)) {
                    List<String> cols = NStringUtils.split(result, "|", false, false);
                    if (cols.size() >= 6) {
                        String luname = cols.get(0).toLowerCase();
                        os = NId.of(null, cols.get(0), cols.get(1));
                        osFamily = NOsFamily.WINDOWS;
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_CMD);
                        shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
                        arch = NId.of(null, cols.get(6));
                        archFamily = NArchFamily.parse(cols.get(6)).orElse(NArchFamily.UNKNOWN);
                        rootUserName=getWindowsAdminName(NShellFamily.WIN_CMD);
                        ok = true;
                    }
                }
            } catch (Exception ex) {
                // not posix
            }
        }
        if (!ok) {
            osFamily = NOsFamily.UNKNOWN;
            os = NId.BLANK;
            shellFamily = NShellFamily.UNKNOWN;
            shell = NId.BLANK;
            userHome = null;
            userName = null;
            rootUserName = "root";
        }

    }

    public static String getWindowsAdminName(NShellFamily sf) {
        try {
            String cmd;
            if(sf==NShellFamily.WIN_CMD){
                // Query WMI via PowerShell for the user account with SID ending in -500
                cmd="wmic useraccount where \"SID like 'S-1-5-%-500' and LocalAccount=true\" get Name /value";
            }else {
                cmd = "powershell -NoProfile -Command (Get-WmiObject Win32_UserAccount -Filter \"SID LIKE 'S-1-5-%-500' AND LocalAccount=TRUE\").Name";
            }
            for (String line : NStringUtils.splitLines(NExec.ofSystem(cmd).grabbedAll())) {
                if (!NBlankable.isBlank(line)) {
                    return line.trim();
                }
            }
        } catch (Exception ignored) {}
        // Fallback to English standard if detection fails
        return "Administrator";
    }

//    private void resolveWindowsOfFamilyFromOsId(String luname) {
//        if (luname.startsWith("linux")) {
//            osFamily = NOsFamily.LINUX;
//        } else if (luname.startsWith("darwin")) {
//            osFamily = NOsFamily.MACOS;
//        } else if (luname.startsWith("sunos")) {
//            osFamily = NOsFamily.UNIX;
//        } else if (
//                luname.startsWith("freebsd")
//                        || luname.startsWith("openbsd")
//                        || luname.startsWith("netbsd")
//        ) {
//        } else if (
//                luname.contains("windows")
//        ) {
//            osFamily = NOsFamily.WINDOWS;
//        } else {
//            osFamily = NOsFamily.WINDOWS;
//        }
//    }
//
//    private void resolveWindowAdminName() {
//        rootUserName = resolveWindowAdminName(envCmdSPI.targetConnectionString().userName(), rootUserName);
//    }

    public String runOnceSystemGrab(String cmd) {
        return envCmdSPI.exec(cmd);
    }

    public String getRootUserName0() {
        tryUpdate();
        return rootUserName;
    }

    @Override
    public NOsFamily getOsFamily0() {
        tryUpdate();
        return osFamily;
    }

    @Override
    public NId getOs0() {
        tryUpdate();
        return os;
    }

    @Override
    public NId getShell0() {
        tryUpdate();
        return shell;
    }

    @Override
    public NShellFamily getShellFamily0() {
        tryUpdate();
        return shellFamily;
    }

    @Override
    public String getUserName0() {
        tryUpdate();
        return userName;
    }

    @Override
    public String getUserHome0() {
        tryUpdate();
        return userHome;
    }

    @Override
    protected NArchFamily getArchFamily0() {
        tryUpdate();
        return archFamily;
    }

    @Override
    protected NId getOsDist0() {
        try {
            if (osFamily() == NOsFamily.LINUX) {
                // POSIX-safe: cat may fail, that's OK
                String r = runOnceSystemGrab("cat /etc/os-release");
                if (!NBlankable.isBlank(r)) {
                    Map<String, String> m = parseKeyValue(r);
                    String id = m.get("ID");
                    if (!NBlankable.isBlank(id)) {
                        NIdBuilder b = NIdBuilder.of(null, id);
                        b.version(m.get("VERSION_ID"));
                        b.setProperty("name", m.get("NAME"));
                        b.setProperty("like", m.get("ID_LIKE"));
                        b.setProperty("codename", m.get("VERSION_CODENAME"));
                        if (isWsl()) {
                            b.setProperty("wsl", "true");
                        }
                        if (isContainer()) {
                            b.setProperty("container", "true");
                        }
                        if (isDocker()) {
                            b.setProperty("docker", "true");
                        }
                        if (isKubernetes()) {
                            b.setProperty("kubernetes", "true");
                        }
                        if (isSsh()) {
                            b.setProperty("ssh", "true");
                        }
                        return b.build();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return NId.of("unknown");
    }

    private boolean isContainer() {
        try {
            // works for docker, k8s, containerd
            String r = runOnceSystemGrab("cat /proc/1/cgroup");
            if (!NBlankable.isBlank(r)) {
                String s = r.toLowerCase();
                return s.contains("docker")
                        || s.contains("kubepods")
                        || s.contains("containerd");
            }
        } catch (Exception ignored) {
        }
        try {
            // podman / newer runtimes
            String r = runOnceSystemGrab("test -f /run/.containerenv && echo yes");
            return !NBlankable.isBlank(r);
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isDocker() {
        try {
            String r = runOnceSystemGrab("cat /proc/1/cgroup");
            return !NBlankable.isBlank(r) && r.toLowerCase().contains("docker");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isSsh() {
        try {
            String r = runOnceSystemGrab("echo $SSH_CONNECTION$SSH_CLIENT");
            return !NBlankable.isBlank(r);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isKubernetes() {
        try {
            String r = runOnceSystemGrab("echo $KUBERNETES_SERVICE_HOST");
            return !NBlankable.isBlank(r);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Map<String, String> parseKeyValue(String s) {
        Map<String, String> m = new HashMap<>();
        for (String line : s.split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int i = line.indexOf('=');
            if (i > 0) {
                String k = line.substring(0, i);
                String v = line.substring(i + 1);
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() > 1) {
                    v = v.substring(1, v.length() - 1);
                }
                m.put(k, v);
            }
        }
        return m;
    }

    @Override
    protected NId getJava0() {
        try {
            // java -version always prints version info and exits immediately
            String r = runOnceSystemGrab("java -version");
            if (!NBlankable.isBlank(r)) {
                for (String line : r.split("\\R")) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        return NId.of("java", line);
                    }
                }
            }
        } catch (Exception ignored) {
            // java not found or not executable
        }
        return NId.of("java", "unknown");
    }

    @Override
    protected NId getArch0() {
        tryUpdate();
        return arch;
    }

    @Override
    protected boolean isGraphicalDesktopEnvironment0() {
        return false;
    }

    @Override
    public NOptional<String> getEnv(String name) {
        return NOptional.ofNamed(env().get(name), name);
    }

    @Override
    public Map<String, String> env() {
        if (envSnapshot == null) {
            synchronized (this) {
                if (envSnapshot == null) {
                    Map<String, String> m = new LinkedHashMap<>();
                    try {
                        String result;
                        if (osFamily() == NOsFamily.WINDOWS) {
                            result = runOnceSystemGrab("cmd /c set");
                        } else {
                            result = runOnceSystemGrab("env");
                        }
                        if (!NBlankable.isBlank(result)) {
                            for (String line : result.split("\\R")) {
                                int i = line.indexOf('=');
                                if (i > 0) {
                                    String k = line.substring(0, i);
                                    String v = line.substring(i + 1);
                                    m.put(k, v);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // intentionally swallow: env is best-effort
                    }
                    envSnapshot = Collections.unmodifiableMap(m);
                }
            }
        }
        return envSnapshot;
    }


    private boolean isWsl() {
        try {
            String r = runOnceSystemGrab("uname -r");
            if (!NBlankable.isBlank(r) && r.toLowerCase().contains("microsoft")) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            String r = runOnceSystemGrab("cat /proc/version");
            if (!NBlankable.isBlank(r) && r.toLowerCase().contains("microsoft")) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            String r = runOnceSystemGrab("echo $WSL_DISTRO_NAME");
            if (!NBlankable.isBlank(r)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public String getMachineName0() {
        return NEnvUtils.getMachineName(this, strings -> envCmdSPI.exec(NCmdLine.of(strings).toString()));
    }

    @Override
    public String getHostName0() {
        return NEnvUtils.getHostName(this, strings -> envCmdSPI.exec(NCmdLine.of(strings).toString()), connectionString());
    }


    public NRam ram() {
        switch (osFamily()) {
            case UNIX:
            case LINUX:
            case MACOS: {
                String LINUX_OR_MAC_PROBE_SCRIPT =
                        "if [ -f /proc/meminfo ]; then " +
                                "awk '/MemTotal/{t=$2} /MemAvailable/{a=$2} /MemFree/{f=$2} " +
                                "END{print t*1024\",\"f*1024\",\"a*1024}' /proc/meminfo; " +
                                "elif command -v sysctl >/dev/null 2>&1; then " +
                                "total=$(sysctl -n hw.memsize); " +
                                "pagesize=$(sysctl -n hw.pagesize); " +
                                "free=$(vm_stat | awk -v ps=\"$pagesize\" '/Pages free/{gsub(\"\\\\.\",\"\");print $3*ps}'); " +
                                "inactive=$(vm_stat | awk -v ps=\"$pagesize\" '/Pages inactive/{gsub(\"\\\\.\",\"\");print $3*ps}'); " +
                                "echo \"$total,$free,$((free+inactive))\"; " +
                                "fi";
                String result = envCmdSPI.exec(LINUX_OR_MAC_PROBE_SCRIPT);
                return parseRam(result);
            }
            case WINDOWS: {
                String WINDOWS_PROBE_SCRIPT =
                        "$os = Get-CimInstance Win32_OperatingSystem; " +
                                "$total = [int64]$os.TotalVisibleMemorySize * 1024; " +
                                "$free = [int64]$os.FreePhysicalMemory * 1024; " +
                                "Write-Output \"$total,$free,$free\"";
                String result = envCmdSPI.exec(WINDOWS_PROBE_SCRIPT);
                return parseRam(result);
            }
        }
        return new NRam("NOT_FOUND", 0, 0, 0);
    }

    /**
     * Parses "total,free,available" (bytes) into an NRam, tolerating extra output lines.
     */
    private NRam parseRam(String result) {
        if (result == null || result.trim().isEmpty()) {
            return new NRam("NOT_FOUND", 0, 0, 0);
        }
        String[] lines = result.trim().split("\\r?\\n");
        String line = lines[lines.length - 1].trim(); // last non-blank line is the actual data
        String[] parts = line.split(",");
        if (parts.length < 3) {
            return new NRam("NOT_FOUND", 0, 0, 0);
        }
        try {
            long total = Long.parseLong(parts[0].trim());
            long free = Long.parseLong(parts[1].trim());
            long available = Long.parseLong(parts[2].trim());
            return new NRam(osFamily().name(), total, free, available);
        } catch (NumberFormatException e) {
            return new NRam("NOT_FOUND", 0, 0, 0);
        }
    }

    @Override
    protected List<NParallelProcessorRuntime> getParallelProcessorRuntimes0() {
        // detecting a compute runtime on a target reachable only through
        // commands is not implemented yet; reporting the local machine's
        // runtimes here would describe the wrong environment
        return Collections.emptyList();
    }

    @Override
    public List<NGpuDevice> gpus() {
        switch (osFamily()) {
            case UNIX:
            case LINUX:
                return gpusLinux();
            case MACOS:
                return gpusMacos();
            case WINDOWS:
                return gpusWindows();
        }
        return new ArrayList<>();
    }

    private List<NGpuDevice> gpusLinux() {
        List<NGpuDevice> result = new ArrayList<>();
        // Prefer nvidia-smi: gives real name + total/used/free VRAM in MiB
        String NVIDIA_SMI_SCRIPT =
                "if command -v nvidia-smi >/dev/null 2>&1; then " +
                        "nvidia-smi --query-gpu=name,memory.total,memory.used,memory.free --format=csv,noheader,nounits; " +
                        "fi";
        String out = envCmdSPI.exec(NVIDIA_SMI_SCRIPT);
        if (out != null && !out.trim().isEmpty()) {
            for (String line : out.trim().split("\\r?\\n")) {
                String[] p = line.split(",");
                if (p.length >= 4) {
                    try {
                        String name = p[0].trim();
                        long totalMb = Long.parseLong(p[1].trim());
                        long usedMb = Long.parseLong(p[2].trim());
                        long freeMb = Long.parseLong(p[3].trim());
                        result.add(new DefaultNGpuDevice(name, new NRam(name,
                                totalMb * 1024L * 1024L,
                                freeMb * 1024L * 1024L,
                                usedMb * 1024L * 1024L),new HashMap<>()));
                    } catch (NumberFormatException ignored) {
                        // skip malformed line
                    }
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        // Fallback for non-NVIDIA (AMD/Intel/etc.): name only, no memory info via lspci
        String LSPCI_SCRIPT =
                "if command -v lspci >/dev/null 2>&1; then lspci | grep -Ei 'vga|3d|display'; fi";
        String lspciOut = envCmdSPI.exec(LSPCI_SCRIPT);
        if (lspciOut != null && !lspciOut.trim().isEmpty()) {
            for (String line : lspciOut.trim().split("\\r?\\n")) {
                int idx = line.indexOf(": ");
                String name = idx >= 0 ? line.substring(idx + 2).trim() : line.trim();
                result.add(new DefaultNGpuDevice(name, new NRam(name, 0, 0, 0),new HashMap<>()));
            }
        }
        return result;
    }

    private List<NGpuDevice> gpusMacos() {
        List<NGpuDevice> result = new ArrayList<>();
        String out = envCmdSPI.exec("system_profiler SPDisplaysDataType");
        if (out != null) {
            String currentName = null;
            Long vramBytes = null;
            for (String raw : out.split("\\r?\\n")) {
                String line = raw.trim();
                if (line.startsWith("Chipset Model:")) {
                    if (currentName != null) {
                        result.add(new DefaultNGpuDevice(currentName, new NRam(currentName, vramBytes == null ? 0 : vramBytes, 0, 0),new HashMap<>()));
                    }
                    currentName = line.substring("Chipset Model:".length()).trim();
                    vramBytes = null;
                } else if (line.startsWith("VRAM")) {
                    // e.g. "VRAM (Total): 8 GB" or "VRAM (Dynamic, Max): 1536 MB"
                    int colon = line.indexOf(':');
                    if (colon >= 0) {
                        vramBytes = parseHumanSizeToBytes(line.substring(colon + 1).trim());
                    }
                }
            }
            if (currentName != null) {
                result.add(new DefaultNGpuDevice(currentName, new NRam(currentName, vramBytes == null ? 0 : vramBytes, 0, 0),new HashMap<>()));
            }
        }
        return result;
    }

    private List<NGpuDevice> gpusWindows() {
        List<NGpuDevice> result = new ArrayList<>();
        // AdapterRAM in WMI is a 32-bit field and often wrong/capped at 4GB for modern GPUs,
        // so try nvidia-smi first for accurate figures.
        String nvidiaOut = envCmdSPI.exec(
                "nvidia-smi --query-gpu=name,memory.total,memory.used,memory.free --format=csv,noheader,nounits");
        if (nvidiaOut != null && !nvidiaOut.trim().isEmpty()
                && !nvidiaOut.toLowerCase().contains("not recognized")) {
            for (String line : nvidiaOut.trim().split("\\r?\\n")) {
                String[] p = line.split(",");
                if (p.length >= 4) {
                    try {
                        String name = p[0].trim();
                        long totalMb = Long.parseLong(p[1].trim());
                        long usedMb = Long.parseLong(p[2].trim());
                        long freeMb = Long.parseLong(p[3].trim());
                        result.add(new DefaultNGpuDevice(name,
                                new NRam(name, totalMb * 1024L * 1024L,
                                        freeMb * 1024L * 1024L,
                                        usedMb * 1024L * 1024L), new LinkedHashMap<>()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        // Fallback: WMI, name + possibly-inaccurate AdapterRAM, no free/used breakdown
        String wmiOut = envCmdSPI.exec(
                "Get-CimInstance Win32_VideoController | ForEach-Object { Write-Output \"$($_.Name),$($_.AdapterRAM)\" }");
        if (wmiOut != null) {
            for (String line : wmiOut.trim().split("\\r?\\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                int lastComma = trimmed.lastIndexOf(',');
                if (lastComma < 0) continue;
                String name = trimmed.substring(0, lastComma).trim();
                long ram = 0;
                try {
                    ram = Long.parseLong(trimmed.substring(lastComma + 1).trim());
                } catch (NumberFormatException ignored) {
                }
                result.add(new DefaultNGpuDevice(name, new NRam(name, ram, 0, 0),new HashMap<>()));
            }
        }
        return result;
    }

    /**
     * Parses strings like "8 GB" / "1536 MB" into bytes.
     */
    private long parseHumanSizeToBytes(String value) {
        try {
            String[] tokens = value.trim().split("\\s+");
            double amount = Double.parseDouble(tokens[0]);
            String unit = tokens.length > 1 ? tokens[1].toUpperCase() : "";
            switch (unit) {
                case "GB":
                    return (long) (amount * 1024L * 1024L * 1024L);
                case "MB":
                    return (long) (amount * 1024L * 1024L);
                case "KB":
                    return (long) (amount * 1024L);
                default:
                    return (long) amount;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
