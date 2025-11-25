package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmdExtension;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;

import java.util.function.Function;
import java.util.function.Supplier;

public interface OsProbeInfo {
    NOsFamily osFamily();
    NId osId();
    NId shellId();
    NShellFamily shellFamily();
    String userName();
    String rootUserName();
    String userHome();
    boolean tryUpdate();
}
