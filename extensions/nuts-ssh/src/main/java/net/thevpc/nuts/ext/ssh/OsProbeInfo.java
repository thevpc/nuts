package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmdExtension;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;

public interface OsProbeInfo {
    void setCommExec(NExecCmdExtension commExec);
    NOsFamily osFamily();
    NId osId();
    NId shellId();
    NShellFamily shellFamily();
    String userName();
    String rootUserName();
    String userHome();
}
