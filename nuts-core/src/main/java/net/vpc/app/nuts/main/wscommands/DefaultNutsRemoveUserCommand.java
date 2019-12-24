/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsRemoveUserCommand;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.config.NutsRepositoryConfigManagerExt;

/**
 *
 * @author vpc
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
        if (repo != null) {
            NutsRepositoryConfigManagerExt.of(repo.config()).removeUser(login, new NutsRemoveOptions().session(getSession()));
        } else {
            NutsWorkspaceConfigManagerExt.of(ws.config()).removeUser(login, new NutsRemoveOptions().session(getSession()));
        }
        return this;
    }



}
