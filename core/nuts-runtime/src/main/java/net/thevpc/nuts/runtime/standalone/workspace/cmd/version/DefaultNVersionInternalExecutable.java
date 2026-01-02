/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.version;

import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExec;
import net.thevpc.nuts.text.NVersionWriter;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNVersionInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNVersionInternalExecutable(String[] args, final DefaultNExec execCommand) {
        super("version", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = NSession.of();
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().getDry());
        if(dry){
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NPrintStream out = session.out();
        NVersionWriter.of().configure(false, args).println(NWorkspace.of().getRuntimeId().getVersion(), out);
        return NExecutionException.SUCCESS;
    }

}
