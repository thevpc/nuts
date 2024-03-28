/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.NConfigs;
import net.thevpc.nuts.NRemoveUserCmd;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;

/**
 *
 * @author thevpc
 */
public class DefaultNRemoveUserCmd extends AbstractNRemoveUserCmd {

    public DefaultNRemoveUserCmd(NSession session) {
        super(session);
    }

    @Override
    public NRemoveUserCmd run() {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        if (repository != null) {
            NRepositoryConfigManagerExt.of(repository.config().setSession(session)).getModel().removeUser(login, getSession());
        } else {
            NConfigsExt.of(NConfigs.of(session).setSession(session)).getModel().removeUser(login, getSession());
        }
        return this;
    }

}
