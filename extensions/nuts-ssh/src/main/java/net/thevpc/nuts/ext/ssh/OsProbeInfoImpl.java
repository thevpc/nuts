package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecCmdExtension;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

public class OsProbeInfoImpl implements OsProbeInfo {
    private String target;
    private NExecCmdExtension commExec;
    private NOsFamily osFamily;
    private NShellFamily shellFamily;
    private NId osId;
    private NId shellId;
    private String userName;
    private String rootUserName;
    private String userHome;

    public OsProbeInfoImpl(String target, NExecCmdExtension commExec) {
        this.target = target;
        this.commExec = commExec;
    }

    public void setCommExec(NExecCmdExtension commExec) {
        this.commExec = commExec;
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

    private synchronized void tryUpdate(){
        if(osFamily==null){
            update();
        }
    }
    private synchronized void update(){
        //test for posix
        boolean ok=false;
        try{
            String cmd =
                    "sh -c 'echo -n \"$(uname -s)|$(uname -r)|$(whoami)|${HOME}|$SHELL|\"; " +
                            "v=$($SHELL --version 2>/dev/null | head -n1 || " +
                            "$SHELL -version 2>/dev/null | head -n1 || " +
                            "$SHELL version 2>/dev/null | head -n1 || echo unknown); " +
                            "echo \"$v\"'";
            String result=runOnceSystemGrab(target,cmd);
            if(NBlankable.isBlank(result)){
                List<String> cols = NStringUtils.split(result, "|", false, false);
                if(cols.size()>=6){
                    String luname = cols.get(0).toLowerCase();
                    osId =NId.of(null,cols.get(0),cols.get(1));
                    if(luname.startsWith("linux")){
                        osFamily =NOsFamily.LINUX;
                    }else if(luname.startsWith("darwin")){
                        osFamily =NOsFamily.MACOS;
                    }else if(luname.startsWith("sunos")){
                        osFamily =NOsFamily.UNIX;
                    }else if(
                            luname.startsWith("freebsd")
                            ||luname.startsWith("openbsd")
                            ||luname.startsWith("netbsd")
                    ){
                        osFamily =NOsFamily.UNIX;
                    }
                    userName=cols.get(2);
                    userHome=cols.get(3);
                    shellId =NId.of(null,cols.get(4),cols.get(5));
                    shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.SH);
                    rootUserName = "root";
                    ok=true;
                }
            }
        }catch (Exception ex){
            // not posix
        }
        if(!ok){
            try{
                String cmd =
                        "powershell -NoProfile -Command $os='Windows';$osver=[System.Environment]::OSVersion.Version.ToString();$user=$env:USERNAME;$home=$env:USERPROFILE;$shell=(Get-Command pwsh -ErrorAction SilentlyContinue).Name;$shellver=$PSVersionTable.PSVersion;Write-Output $os'|'$osver'|'$user'|'$home'|'$shell'|'$shellver";
                String result=runOnceSystemGrab(target,cmd);
                if(NBlankable.isBlank(result)){
                    List<String> cols = NStringUtils.split(result, "|", false, false);
                    if(cols.size()>=6){
                        String luname = cols.get(0).toLowerCase();
                        osId =NId.of(null,cols.get(0),cols.get(1));
                        if(luname.startsWith("linux")){
                            osFamily =NOsFamily.LINUX;
                        }else if(luname.startsWith("darwin")){
                            osFamily =NOsFamily.MACOS;
                        }else if(luname.startsWith("sunos")){
                            osFamily =NOsFamily.UNIX;
                        }else if(
                                luname.startsWith("freebsd")
                                        ||luname.startsWith("openbsd")
                                        ||luname.startsWith("netbsd")
                        ){
                            osFamily =NOsFamily.UNIX;
                        }
                        userName=cols.get(2);
                        userHome=cols.get(3);
                        shellId =NId.of(null,cols.get(4),cols.get(5));
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.SH);
                        rootUserName = "Administrator";
                        ok=true;
                    }
                }
            }catch (Exception ex){
                // not posix
            }
        }
        if(!ok){
            try{
                String cmd = "cmd /c \"echo Windows|%OS%|%USERNAME%|%USERPROFILE%|cmd|unknown\"";
                String result=runOnceSystemGrab(target,cmd);
                if(NBlankable.isBlank(result)){
                    List<String> cols = NStringUtils.split(result, "|", false, false);
                    if(cols.size()>=6){
                        String luname = cols.get(0).toLowerCase();
                        osId =NId.of(null,cols.get(0),cols.get(1));
                        if(luname.startsWith("linux")){
                            osFamily =NOsFamily.LINUX;
                        }else if(luname.startsWith("darwin")){
                            osFamily =NOsFamily.MACOS;
                        }else if(luname.startsWith("sunos")){
                            osFamily =NOsFamily.UNIX;
                        }else if(
                                luname.startsWith("freebsd")
                                        ||luname.startsWith("openbsd")
                                        ||luname.startsWith("netbsd")
                        ){
                            osFamily =NOsFamily.UNIX;
                        }
                        userName=cols.get(2);
                        userHome=cols.get(3);
                        shellId =NId.of(null,cols.get(4),cols.get(5));
                        shellFamily = NShellFamily.parse(cols.get(4)).orElse(NShellFamily.SH);
                        rootUserName = "Administrator";
                        ok=true;
                    }
                }
            }catch (Exception ex){
                // not posix
            }
        }
        if(!ok){
            osFamily=NOsFamily.UNKNOWN;
            osId =NId.BLANK;
            shellFamily=NShellFamily.UNKNOWN;
            shellId =NId.BLANK;
            userHome=null;
            userName=null;
            rootUserName = "root";
        }
    }

    public String runOnceSystemGrab(String target, String... cmd) {

        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int e;
        NSession session = NSession.of();
        try (RemoteConnexionStringInfo.MyNExecCmdExtensionContext d = new RemoteConnexionStringInfo.MyNExecCmdExtensionContext(
                NExecCmd.of().setConnexionString(target).system(),
                commExec, target, cmd, out, err)) {
            e = commExec.exec(d);
        } catch (RuntimeException ex) {
            throw new NExecutionException(NMsg.ofC("command failed :%s", ex), ex);
        }
        if (e != NExecutionException.SUCCESS) {
            session.err().println(out.toString());
            session.err().println(err.toString());
            throw new NExecutionException(NMsg.ofC("command exit with code :%s", e), e);
        }
        return out.toString();
    }

    @Override
    public String rootUserName() {
        tryUpdate();
        return rootUserName;
    }
}
