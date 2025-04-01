/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.NRemoveUserCmd;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;

/**
 *
 * @author thevpc
 */
public class DefaultNRemoveUserCmd extends AbstractNRemoveUserCmd {

    public DefaultNRemoveUserCmd() {
        super();
    }

    @Override
    public NRemoveUserCmd run() {
        if (repository != null) {
            NRepositoryConfigManagerExt.of(repository.config()).getModel().removeUser(login);
        } else {
            NWorkspaceExt.of().getConfigModel().removeUser(login);
        }
        return this;
    }

}
