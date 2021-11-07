/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultNutsExecCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsVersionInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final DefaultNutsExecCommand execCommand;

    public DefaultNutsVersionInternalExecutable(String[] args, NutsSession session, final DefaultNutsExecCommand execCommand) {
        super("version", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NutsWorkspace ws = getSession().getWorkspace();
        NutsPrintStream out = getSession().out();
        NutsVersionFormat.of(getSession()).configure(false, args).println(out);
    }

}
