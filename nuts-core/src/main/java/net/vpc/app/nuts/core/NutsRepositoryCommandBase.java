/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryCommand;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsCommandLine;

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
    public T configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, getRepo().getWorkspace(), skipUnsupported, args, getCommandName());
    }

    @Override
    public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, getRepo().getWorkspace(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--trace": {
                getValidWorkspaceSessionCopy().setTrace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--ask": {
                getValidWorkspaceSessionCopy().setAsk(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--force": {
                getValidWorkspaceSessionCopy().setForce(cmdLine.nextBoolean().getBooleanValue());
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
