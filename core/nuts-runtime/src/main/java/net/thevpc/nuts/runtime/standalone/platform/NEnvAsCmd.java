package net.thevpc.nuts.runtime.standalone.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.util.*;

@NComponentScope(NScopeType.PROTOTYPE)
public class NEnvAsCmd extends NEnvBase {

    private NEnvCmdSPI envCmdSPI;
    private boolean valid;
    private Map<String,String> envSnapshot;

    public NEnvAsCmd(NScorableContext context) {
        Object criteria = context.getCriteria();
        this.envCmdSPI = (NEnvCmdSPI) criteria;
    }

    private NEnvAsCmd(NEnvCmdSPI envCmdSPI) {
        this.envCmdSPI = envCmdSPI;
    }

    @Override
    public NEnv refresh() {
        return new NEnvAsCmd(envCmdSPI);
    }

    public NConnectionString getConnectionString() {
        return envCmdSPI.getTargetConnectionString();
    }

    public synchronized boolean tryUpdate() {
        if (!valid) {
            update();
            valid = true;
            return true;
        }
        return false;
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object criteria = context.getCriteria();
        if (criteria instanceof NEnvCmdSPI) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }


    private synchronized void update() {
        //test for posix
        boolean ok = false;
        try {
            String cmd =
                    "sh -c 'echo -n \"$(uname -s)|$(uname -r)|uname -m|$(whoami)|${HOME}|$SHELL\"; " +
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
                        resolveWindowsOfFamilyFromOsId(luname);
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_POWER_SHELL);
                        shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
                        arch = NId.of(null, cols.get(6));
                        archFamily = NArchFamily.parse(cols.get(6)).orElse(NArchFamily.UNKNOWN);
                        resolveWindowAdminName();
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
                        resolveWindowsOfFamilyFromOsId(luname);
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_CMD);
                        shell = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
                        arch = NId.of(null, cols.get(6));
                        archFamily = NArchFamily.parse(cols.get(6)).orElse(NArchFamily.UNKNOWN);
                        resolveWindowAdminName();
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

    private void resolveWindowsOfFamilyFromOsId(String luname) {
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
        } else if (
                luname.contains("windows")
        ) {
            osFamily = NOsFamily.WINDOWS;
        } else {
            osFamily = NOsFamily.WINDOWS;
        }
    }

    private void resolveWindowAdminName() {
        rootUserName = resolveWindowAdminName(envCmdSPI.getTargetConnectionString().getUserName(), rootUserName);
    }

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
            if (getOsFamily() == NOsFamily.LINUX) {
                // POSIX-safe: cat may fail, that's OK
                String r = runOnceSystemGrab("cat /etc/os-release");
                if (!NBlankable.isBlank(r)) {
                    Map<String, String> m = parseKeyValue(r);
                    String id = m.get("ID");
                    if (!NBlankable.isBlank(id)) {
                        NIdBuilder b = NIdBuilder.of(null, id);
                        b.setVersion(m.get("VERSION_ID"));
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
        return NOptional.ofNamed(getEnv().get(name),name);
    }

    @Override
    public Map<String, String> getEnv() {
        if(envSnapshot==null){
            synchronized (this) {
                if (envSnapshot == null) {
                    Map<String, String> m = new LinkedHashMap<>();
                    try {
                        String result;
                        if (getOsFamily() == NOsFamily.WINDOWS) {
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
}
