/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.deploy;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsDeployInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsDeployInternalExecutable(String[] args, NutsSession session) {
        super("deploy", args, session);
    }

    @Override
    public void execute() {
//        if (CoreNutsUtils.isIncludesHelpOption(args)) {
//            showDefaultHelp();
//            return;
//        }
        getSession().getWorkspace().deploy().configure(false, args).run();
    }

}
