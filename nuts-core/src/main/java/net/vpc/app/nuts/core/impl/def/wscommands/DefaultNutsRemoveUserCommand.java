/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.wscommands.AbstractNutsRemoveUserCommand;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;

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
            NutsRepositoryConfigManagerExt.of(repo.config()).removeUser(login, new NutsRemoveOptions().session(getValidSession()));
        } else {
            NutsWorkspaceConfigManagerExt.of(ws.config()).removeUser(login, new NutsRemoveOptions().session(getValidSession()));
        }
        return this;
    }



}
