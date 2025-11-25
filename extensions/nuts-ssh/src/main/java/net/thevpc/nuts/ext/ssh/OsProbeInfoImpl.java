package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmdExtension;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;

public class OsProbeInfoImpl implements OsProbeInfo {
    private String target;
    private NOsFamily osFamily;
    private NShellFamily shellFamily;
    private NId osId;
    private NId shellId;
    private String userName;
    private String rootUserName;
    private String userHome;

    public OsProbeInfoImpl(String target) {
        this.target = target;
    }

    @Override
    public NOsFamily osFamily() {
        tryUpdate();
        return osFamily;
    }

    @Override
    public NId osId() {
        tryUpdate();
        return osId;
    }

    @Override
    public NId shellId() {
        tryUpdate();
        return shellId;
    }

    @Override
    public NShellFamily shellFamily() {
        tryUpdate();
        return shellFamily;
    }

    @Override
    public String userName() {
        tryUpdate();
        return userName;
    }

    @Override
    public String userHome() {
        tryUpdate();
        return userHome;
    }

    public synchronized boolean tryUpdate() {
        if (osFamily == null) {
            update();
            return true;
        }
        return false;
    }

    private synchronized void update() {
        //test for posix

        NSession session = NSession.of();
        boolean ok = false;
        try {
            String cmd =
                    "sh -c 'echo -n \"$(uname -s)|$(uname -r)|$(whoami)|${HOME}|$SHELL|\"; " +
                            "v=$($SHELL --version 2>/dev/null | head -n1 || " +
                            "$SHELL -version 2>/dev/null | head -n1 || " +
                            "$SHELL version 2>/dev/null | head -n1 || echo unknown); " +
                            "echo \"$v\"'";
            String result = runOnceSystemGrab(cmd);
            if (!NBlankable.isBlank(result)) {
                List<String> cols = NStringUtils.split(result, "|", false, false);
                if (cols.size() >= 6) {
                    String luname = cols.get(0).toLowerCase();
                    osId = NId.of(null, cols.get(0), cols.get(1));
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
                    userName = cols.get(2);
                    userHome = cols.get(3);
                    shellId = NId.of(null, cols.get(4), cols.get(5));
                    shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.SH);
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
                        "powershell -NoProfile -Command \"& {$os='Windows'; $osver=[System.Environment]::OSVersion.Version.ToString(); $user=$env:USERNAME; $homedir=$env:USERPROFILE; $shell=(Get-Command pwsh -ErrorAction SilentlyContinue).Name; $shellver=$PSVersionTable.PSVersion.ToString(); Write-Output ($os+'|'+$osver+'|'+$user+'|'+$homedir+'|'+$shell+'|'+$shellver)}\"";
                String result = runOnceSystemGrab(cmd);
                if (!NBlankable.isBlank(result)) {
                    List<String> cols = NStringUtils.split(result, "|", true, false);
                    if (cols.size() >= 6) {
                        String luname = cols.get(0).toLowerCase();
                        osId = NId.of(null, cols.get(0), cols.get(1));
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
                        }else{
                            osFamily = NOsFamily.WINDOWS;
                        }
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellId = NId.of(null, cols.get(4), cols.get(5));
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_POWER_SHELL);
                        rootUserName = "Administrator";
                        ok = true;
                    }
                }
            } catch (Exception ex) {
                // not posix
            }
        }
        if (!ok) {
            try {
                String cmd = "cmd /c \"echo Windows|%OS%|%USERNAME%|%USERPROFILE%|cmd|unknown\"";
                String result = runOnceSystemGrab(cmd);
                if (!NBlankable.isBlank(result)) {
                    List<String> cols = NStringUtils.split(result, "|", false, false);
                    if (cols.size() >= 6) {
                        String luname = cols.get(0).toLowerCase();
                        osId = NId.of(null, cols.get(0), cols.get(1));
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
                        } else if (
                                luname.contains("windows")
                        ) {
                            osFamily = NOsFamily.WINDOWS;
                        }else{
                            osFamily = NOsFamily.WINDOWS;
                        }
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellId = NId.of(null, cols.get(4), cols.get(5));
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_CMD);
                        rootUserName = "Administrator";
                        ok = true;
                    }
                }
            } catch (Exception ex) {
                // not posix
            }
        }
        if (!ok) {
            osFamily = NOsFamily.UNKNOWN;
            osId = NId.BLANK;
            shellFamily = NShellFamily.UNKNOWN;
            shellId = NId.BLANK;
            userHome = null;
            userName = null;
            rootUserName = "root";
        }

    }

    public String runOnceSystemGrab(String cmd) {
        try (SShConnection sshc = new SShConnection(target)) {
            return sshc.execStringCommandGrabbed(cmd).outString();
        }
    }

    @Override
    public String rootUserName() {
        tryUpdate();
        return rootUserName;
    }
}
