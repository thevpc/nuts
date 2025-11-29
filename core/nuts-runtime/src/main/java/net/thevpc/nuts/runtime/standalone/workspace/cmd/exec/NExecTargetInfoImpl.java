package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecTargetInfo;
import net.thevpc.nuts.spi.NExecTargetInfoRunner;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;

public class NExecTargetInfoImpl implements NExecTargetInfo {
    private NConnectionString target;
    private NOsFamily osFamily;
    private NShellFamily shellFamily;
    private NId osId;
    private NId shellId;
    private String userName;
    private String rootUserName;
    private String userHome;
    private NExecTargetInfoRunner commandRunner;

    public NExecTargetInfoImpl(NConnectionString target, NExecTargetInfoRunner commandRunner) {
        this.target = target;
        this.commandRunner = commandRunner;
    }

    @Override
    public NOsFamily getOsFamily() {
        tryUpdate();
        return osFamily;
    }

    @Override
    public NId getOsId() {
        tryUpdate();
        return osId;
    }

    @Override
    public NId getShellId() {
        tryUpdate();
        return shellId;
    }

    @Override
    public NShellFamily getShellFamily() {
        tryUpdate();
        return shellFamily;
    }

    @Override
    public String getUserName() {
        tryUpdate();
        return userName;
    }

    @Override
    public String getUserHome() {
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
                    shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.SH);
                    shellId = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
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
                        resolveWindowsOfFamilyFromOsId(luname);
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_POWER_SHELL);
                        shellId = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
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
                String cmd = "cmd /c \"echo Windows|%OS%|%USERNAME%|%USERPROFILE%|cmd|unknown\"";
                String result = runOnceSystemGrab(cmd);
                if (!NBlankable.isBlank(result)) {
                    List<String> cols = NStringUtils.split(result, "|", false, false);
                    if (cols.size() >= 6) {
                        String luname = cols.get(0).toLowerCase();
                        osId = NId.of(null, cols.get(0), cols.get(1));
                        resolveWindowsOfFamilyFromOsId(luname);
                        userName = cols.get(2);
                        userHome = cols.get(3);
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.WIN_CMD);
                        shellId = NId.of(null, NStringUtils.firstNonBlank(cols.get(4), shellFamily.id()), cols.get(5));
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
            osId = NId.BLANK;
            shellFamily = NShellFamily.UNKNOWN;
            shellId = NId.BLANK;
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
        switch (NStringUtils.trim(target.getUserName()).toLowerCase()) {
            case "adminitrateur": {
                rootUserName = "Administrateur";
                break;
            }
            case "administrador": {
                rootUserName = "Administrador";
                break;
            }
            case "administratör": {
                rootUserName = "Administratör";
                break;
            }
            case "järjestelmänvalvoja": {
                rootUserName = "Järjestelmänvalvoja";
                break;
            }
            case "rendszergazda": {
                rootUserName = "Rendszergazda";
                break;
            }
            case "администратор": {
                rootUserName = "Администратор";
                break;
            }
            default: {
                rootUserName = "Administrator";
            }
        }
    }

    public String runOnceSystemGrab(String cmd) {
        return commandRunner.run(cmd, target);
    }

    @Override
    public String getRootUserName() {
        tryUpdate();
        return rootUserName;
    }
}
