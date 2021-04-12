/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.NutsRemoveOptions;
import net.thevpc.nuts.NutsRemoveUserCommand;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;

/**
 *
 * @author thevpc
 */
public class DefaultNutsRemoveUserCommand extends AbstractNutsRemoveUserCommand {

    public DefaultNutsRemoveUserCommand(NutsWorkspace ws) {
        super(ws);
    }

    public DefaultNutsRemoveUserCommand(NutsRepository repo) {
        super(repo.getWorkspace());
    }

    @Override
    public NutsRemoveUserCommand run() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (repo != null) {
            NutsRepositoryConfigManagerExt.of(repo.config().setSession(session)).getModel().removeUser(login, getSession());
        } else {
            NutsWorkspaceConfigManagerExt.of(ws.config().setSession(session)).getModel().removeUser(login, getSession());
        }
        return this;
    }

}
