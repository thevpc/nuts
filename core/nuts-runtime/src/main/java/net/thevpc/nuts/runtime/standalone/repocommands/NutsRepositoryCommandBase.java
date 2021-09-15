/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repocommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositoryCommand;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NutsRepositoryCommandBase<T extends NutsRepositoryCommand> implements NutsRepositoryCommand {

    protected NutsRepository repo;
    private NutsSession session;
    private NutsFetchMode fetchMode = NutsFetchMode.LOCAL;
    private String commandName;

    public NutsRepositoryCommandBase(NutsRepository repo, String commandName) {
        this.repo = repo;
        this.commandName = commandName;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(repo.getWorkspace(), getSession());
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
    public NutsSession getSession() {
        return session;
    }

    @Override
    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    protected void invalidateResult() {

    }

    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

//    @Override
    public T setFetchMode(NutsFetchMode fetchMode) {
        this.fetchMode = fetchMode;
        return (T) this;
    }

    protected NutsRepository getRepo() {
        return repo;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, getCommandName());
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
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        checkSession();
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
//        switch (a.getKey().getString()) {
//        }

        if (getSession().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public abstract T run();
}
