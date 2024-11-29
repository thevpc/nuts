/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmd;

/**
 *
 * @author thevpc
 */
public class DefaultNVersionInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNVersionInternalExecutable(NWorkspace workspace,String[] args, final DefaultNExecCmd execCommand) {
        super(workspace,"version", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = workspace.currentSession();
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NWorkspace ws = session.getWorkspace();
        NPrintStream out = session.out();
        NVersionFormat.of().configure(false, args).println(out);
        return NExecutionException.SUCCESS;
    }

}
