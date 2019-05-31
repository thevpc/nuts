/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryCommand;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class NutsRepositoryCommandBase<T extends NutsRepositoryCommand> implements NutsRepositoryCommand {

    protected NutsRepository repo;
    private NutsRepositorySession session;
    private String commandName;
    private NutsSession validSession;
    private boolean sessionCopy = false;

    public NutsRepositoryCommandBase(NutsRepository repo, String commandName) {
        this.repo = repo;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsRepositoryCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
        }
        return (T) this;
    }

    @Override
    public NutsRepositorySession getSession() {
        return session;
    }

    @Override
    public T session(NutsRepositorySession session) {
        return setSession(session);
    }

    @Override
    public T setSession(NutsRepositorySession session) {
        this.session = session;
        return (T) this;
    }

    protected void invalidateResult() {

    }

    public NutsSession getValidWorkspaceSessionCopy() {
        NutsSession s = getValidWorkspaceSession();
        if (!sessionCopy) {
            s = validSession = s.copy();
            sessionCopy = true;
        }
        return s;
    }

    public NutsSession getValidWorkspaceSession() {
        if (validSession == null) {
            validSession = NutsWorkspaceUtils.validateSession(getRepo().getWorkspace(), getSession().getSession());
            sessionCopy = true;
        }
        return validSession;
    }
    
    protected NutsRepository getRepo() {
        return repo;
    }

    @Override
    public T configure(String... args) {
        return NutsConfigurableHelper.configure(this, getRepo().getWorkspace(), args, getCommandName());
    }

    @Override
    public boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        return NutsConfigurableHelper.configure(this, getRepo().getWorkspace(), commandLine, skipIgnored);
    }

    @Override
    public boolean configureFirst(NutsCommand cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "--trace": {
                getValidWorkspaceSessionCopy().setTrace(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
            case "--ask": {
                getValidWorkspaceSessionCopy().setAsk(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
            case "--force": {
                getValidWorkspaceSessionCopy().setForce(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
        }

        if (getValidWorkspaceSessionCopy().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public abstract T run();
}
