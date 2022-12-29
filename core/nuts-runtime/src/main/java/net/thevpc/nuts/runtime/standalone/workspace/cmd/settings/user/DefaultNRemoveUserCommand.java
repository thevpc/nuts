/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.NRemoveUserCommand;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigManagerExt;

/**
 *
 * @author thevpc
 */
public class DefaultNRemoveUserCommand extends AbstractNRemoveUserCommand {

    public DefaultNRemoveUserCommand(NSession ws) {
        super(ws);
    }

    @Override
    public NRemoveUserCommand run() {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        if (repository != null) {
            NRepositoryConfigManagerExt.of(repository.config().setSession(session)).getModel().removeUser(login, getSession());
        } else {
            NWorkspaceConfigManagerExt.of(session.config().setSession(session)).getModel().removeUser(login, getSession());
        }
        return this;
    }

}
