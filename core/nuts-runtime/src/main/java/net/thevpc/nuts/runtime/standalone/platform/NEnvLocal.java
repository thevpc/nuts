package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNUtilGui;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.collections.NMaps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

@NComponentScope(NScopeType.WORKSPACE)
public class NEnvLocal extends NEnvBase {

    protected boolean initialized;
    protected boolean nativeImage;
    protected boolean gui;
    private static final MethodHandle TOTAL_MEM_HANDLE;
    private static final MethodHandle FREE_MEM_HANDLE;

    static {
        TOTAL_MEM_HANDLE = resolve("getTotalMemorySize", "getTotalPhysicalMemorySize");
        FREE_MEM_HANDLE = resolve("getFreeMemorySize", "getFreePhysicalMemorySize");
    }

    private static MethodHandle resolve(String preferredName, String fallbackName) {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        for (String name : new String[]{preferredName, fallbackName}) {
            try {
                Method m = OperatingSystemMXBean.class.getMethod(name);
                return lookup.unreflect(m);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
                // try next
            }
        }
        return null; // neither available — shouldn't happen, but be defensive
    }

    private static long invoke(MethodHandle handle) {
        if (handle == null) return -1L;
        try {
            OperatingSystemMXBean os =
                    ManagementFactory.getOperatingSystemMXBean();
            return (long) handle.invoke(os);
        } catch (Throwable t) {
            return -1L;
        }
    }

    @Override
    public NEnv refresh() {
        return new NEnvLocal();
    }

    @Override
    public String pid() {
        return NWorkspaceExt.of().getModel().getPid();
    }

    private void init() {
        if (!this.initialized) {
            this.os = NId.get(CorePlatformUtils.getPlatformOs()).get();
            NId platformOsDist = CorePlatformUtils.getPlatformOsDist();
            if (platformOsDist == null) {
                platformOsDist = NId.of("default");
            }
            this.osDist = platformOsDist;
            this.java = NJavaSdkUtils.of().createJdkId(System.getProperty("java.version"));
            this.arch = NId.get(System.getProperty("os.arch")).get();
            this.archFamily = NArchFamily.current();
            this.gui = CoreNUtilGui.isGraphicalDesktopEnvironment();
            this.initialized = true;
            this.userHome = System.getProperty("user.home");
            this.userName = System.getProperty("user.name");
            this.nativeImage = "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));
            NOsFamily nOsFamily = osFamily();
            if(nOsFamily==NOsFamily.WINDOWS) {
                boolean ok=false;
                if (!ok) {
                    try {
                        String[] cmd = {
                                "powershell", "-NoProfile", "-Command", "& {" +
                                "$os='Windows'; " +
                                "$osver=[System.Environment]::OSVersion.Version.ToString(); " +
                                "$arch=$env:PROCESSOR_ARCHITECTURE; " +
                                "$user=$env:USERNAME; $homedir=$env:USERPROFILE; " +
                                "$shell=(Get-Process -Id $PID).Name; " +
                                "$shellver=$PSVersionTable.PSVersion.ToString(); " +
                                "Write-Output ($os+'|'+$osver+'|'+$user+'|'+$homedir+'|'+$shell+'|'+$shellver+'|'+$arch)}"
                        };
                        String result = NExec.ofSystem(cmd).grabbedAll();
                        if (!NBlankable.isBlank(result)) {
                            List<String> cols = NStringUtils.split(result.trim(), "|", true, false);
                            if (cols.size() >= 6) {
                                String luname = cols.get(0).toLowerCase();
                                os = NId.of(null, cols.get(0), cols.get(1));
                                shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_POWER_SHELL);
                                shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
                                rootUserName = getWindowsAdminName(NShellFamily.WIN_POWER_SHELL);
                                ok = true;
                            }
                        }
                    } catch (Exception ex) {
                        // not posix
                    }
                }
                if (!ok) {
                    try {
                        String[] cmd = {"cmd", "/c", "echo Windows^|%OS%^|%USERNAME%^|%USERPROFILE%^|cmd^|unknown^|%PROCESSOR_ARCHITECTURE%"};
                        String result = NExec.ofSystem(cmd).grabbedAll();
                        if (!NBlankable.isBlank(result)) {
                            List<String> cols = NStringUtils.split(result, "|", false, false);
                            if (cols.size() >= 6) {
                                shellFamily = NShellFamily.WIN_CMD;
                                shell = NId.of(null, shellFamily.id(), os.version());
                                rootUserName = getWindowsAdminName(NShellFamily.WIN_CMD);
                                ok = true;
                            }
                        }
                    } catch (Exception ex) {
                        // not posix
                    }
                }
                if (!ok) {
                    os = NId.BLANK;
                    shellFamily = NShellFamily.WIN_CMD;
                    shell = NId.of(null, shellFamily.id(), os.version());
                    rootUserName = "Administrator";
                }
                rootUserName = getWindowsAdminName(shellFamily());
            }else if(osFamily.isPosix()){
                rootUserName = "root";
                //test for posix
                try {
                    String result = NExec.ofSystem("sh","-c",
                            "echo -n \"$(uname -s)|$(uname -r)|$(uname -m)|$(whoami)|${HOME}|$SHELL\"; " +
                                    "echo -n \"|${BASH_VERSION:-${ZSH_VERSION:-${FISH_VERSION:-$KSH_VERSION}}}|\";"+
                                    "v=$($SHELL --version 2>/dev/null | head -n1 || " +
                                    "$SHELL -version 2>/dev/null | head -n1 || " +
                                    "$SHELL version 2>/dev/null | head -n1 || echo unknown); " +
                                    "echo \"$v\""
                            ).grabbedAll();

                    if (!NBlankable.isBlank(result)) {
                        List<String> cols = NStringUtils.split(result, "|", false, false);
                        if (cols.size() >= 6) {
                            shellFamily = NShellFamily.parse(cols.get(5)).orElse(NShellFamily.SH);
                            shell = NId.of(null, shellFamily.id(), cols.get(6));
                        }
                    }
                } catch (Exception ex) {
                    // not posix
                }
            }
            if(shell()==null){
                shell=NId.BLANK;
            }
        }
    }

    @Override
    public NRam ram() {
        long total = invoke(TOTAL_MEM_HANDLE);
        long free = invoke(FREE_MEM_HANDLE);
        long used = total - free;
        return new NRam("RAM", total, used, free);
    }

    @Override
    public boolean isNativeImage() {
        return "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));
    }

    @Override
    public String getUserName0() {
        return userName;
    }

    @Override
    public String getUserHome0() {
        return userHome;
    }

    @Override
    protected String getRootUserName0() {
        return rootUserName;
    }

    @Override
    public NId getShell0() {
        return shell;
    }


    public static String getWindowsAdminName(NShellFamily sf) {
        try {
            String[] cmd;
            if(sf==NShellFamily.WIN_CMD){
                // Query WMI via PowerShell for the user account with SID ending in -500
                cmd=new String[]{"cmd.exe", "/c",
                        "wmic useraccount where \"SID like 'S-1-5-%-500' and LocalAccount=true\" get Name /value"};
                for (String line : NStringUtils.splitLines(NExec.ofSystem(cmd).grabbedAll().trim())) {
                    if (!NBlankable.isBlank(line)) {
                        String trimmed = line.trim();
                        if (trimmed.startsWith("Name=")) {
                            String name = trimmed.substring(5).trim();
                            if (!NBlankable.isBlank(name)) {
                                return name;
                            }
                        }
                    }
                }
            }else {
                cmd = new String[]{"powershell", "-NoProfile", "-Command",
                        "(Get-CimInstance Win32_UserAccount -Filter 'LocalAccount=True AND SID LIKE ''%-500''').Name"};
                for (String line : NStringUtils.splitLines(NExec.ofSystem(cmd).grabbedAll())) {
                    if (!NBlankable.isBlank(line)) {
                        return line.trim();
                    }
                }
            }
        } catch (Exception ignored) {}
        // Fallback to English standard if detection fails
        return "Administrator";
    }

    public NConnectionString connectionString() {
        return null;
    }

    @Override
    protected NOsFamily getOsFamily0() {
        return NOsFamily.current();
    }

    @Override
    public NShellFamily getShellFamily0() {
        return NShellFamily.current();
    }

    @Override
    public NId getJava0() {
        init();
        return java;
    }

    @Override
    public NId getOs0() {
        init();
        return os;
    }

    public NId getOsDist0() {
        init();
        return osDist;
    }

    @Override
    public NId getArch0() {
        init();
        return arch;
    }

    @Override
    public NArchFamily getArchFamily0() {
        init();
        return archFamily;
    }

    @Override
    public boolean isGraphicalDesktopEnvironment0() {
        return gui;
    }

    @Override
    protected List<NGpuDevice> getGpuDevices0() {
        return NLinuxGpuProbe.probe();
    }

    @Override
    public long queryGpuFreeMemoryBytes(NGpuDevice device) {
        return device == null ? -1 : NLinuxGpuProbe.queryFreeMemoryBytes(device.getPciBusId());
    }

    @Override
    protected List<NParallelProcessorRuntime> getParallelProcessorRuntimes0() {
        return NParallelProcessorFamily.detectAvailable();
    }

    @Override
    protected boolean isParallelProcessorDetectionSupported() {
        return NParallelProcessorFamily.canDetect();
    }


    @Override
    public NOptional<String> getEnv(String name) {
        return NOptional.of(env().get(name));
    }

    @Override
    public Map<String, String> env() {
        return NWorkspaceExt.of().getSysEnv();
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        Object criteria = context.criteria();
        if (NBlankable.isBlank(criteria)) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public String getMachineName0() {
        return NEnvUtils.getMachineName(this, new Function<String[], String>() {
            @Override
            public String apply(String[] cmd) {
                return NExec.ofSystem(cmd)
                        .failFast(true)
                        .grabbedOutOnly();
            }
        });
    }

    @Override
    public String getHostName0() {
        return NEnvUtils.getHostName(this, new Function<String[], String>() {
            @Override
            public String apply(String[] strings) {
                return NExec.ofSystem(strings)
                        .failFast(true)
                        .grabbedOutOnly();
            }
        }, null);
    }
}
