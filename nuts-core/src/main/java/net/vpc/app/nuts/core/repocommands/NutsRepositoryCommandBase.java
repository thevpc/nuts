/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repocommands;

import net.vpc.app.nuts.*;
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

//    @Override
//    public T session(NutsRepositorySession session) {
//        return setSession(session);
//    }

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

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, getRepo().getWorkspace(), skipUnsupported, args, getCommandName());
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
     * @return {@code this} instance
     */
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
//        switch (a.getStringKey()) {
//        }

        if (getValidWorkspaceSessionCopy().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public abstract T run();
}
