/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsDefaultCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.NutsRemoveUserCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.NutsArgument;

/**
 *
 * @author vpc
 */
public class DefaultNutsRemoveUserCommand extends NutsWorkspaceCommandBase<NutsRemoveUserCommand> implements NutsRemoveUserCommand {

    private NutsRepository repo;
    private String login;

    public DefaultNutsRemoveUserCommand(NutsWorkspace ws) {
        super(ws);
    }

    public DefaultNutsRemoveUserCommand(NutsRepository repo) {
        super(repo.getWorkspace());
        this.repo = repo;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public NutsRemoveUserCommand login(String login) {
        return setLogin(login);
    }

    @Override
    public NutsRemoveUserCommand setLogin(String login) {
        this.login = login;
        return this;
    }

    @Override
    public NutsRemoveUserCommand run() {
        if (repo != null) {
            NutsRepositoryConfigManagerExt.of(repo.config()).removeUser(login);
        } else {
            NutsWorkspaceConfigManagerExt.of(ws.config()).removeUser(login);
        }
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.strKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }
   
}
