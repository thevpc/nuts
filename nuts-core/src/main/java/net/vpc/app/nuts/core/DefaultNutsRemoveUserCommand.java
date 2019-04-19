/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.NutsRemoveUserCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;

/**
 *
 * @author vpc
 */
public class DefaultNutsRemoveUserCommand implements NutsRemoveUserCommand {

    private boolean trace = true;
    private NutsSession session;
    private NutsWorkspace ws;
    private NutsRepository repo;
    private String login;

    public DefaultNutsRemoveUserCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsRemoveUserCommand(NutsRepository repo) {
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
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsRemoveUserCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public NutsRemoveUserCommand trace() {
        return trace(true);
    }

    @Override
    public NutsRemoveUserCommand trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRemoveUserCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsRemoveUserCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsRemoveUserCommand run() {
        if (repo != null) {
            NutsRepositoryConfigManagerExt.of(repo.config()).removeUser(login);
        } else {
            NutsWorkspaceConfigManagerExt.of(ws.config()).removeUser(login);
        }
        return this;
    }
}
