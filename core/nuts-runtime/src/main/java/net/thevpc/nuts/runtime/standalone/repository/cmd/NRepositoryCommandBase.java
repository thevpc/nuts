/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.cmdline.NCommandLineConfigurable;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositoryCommand;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NRepositoryCommandBase<T extends NRepositoryCommand> implements NRepositoryCommand {

    protected NRepository repo;
    private NSession session;
    private NFetchMode fetchMode = NFetchMode.LOCAL;
    private String commandName;

    public NRepositoryCommandBase(NRepository repo, String commandName) {
        this.repo = repo;
        this.commandName = commandName;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(repo.getWorkspace(), getSession());
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NRepositoryCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
        }
        return (T) this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public T setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(repo.getWorkspace(), session);
        return (T) this;
    }

    protected void invalidateResult() {

    }

    public NFetchMode getFetchMode() {
        return fetchMode;
    }

//    @Override
    public T setFetchMode(NFetchMode fetchMode) {
        this.fetchMode = fetchMode;
        return (T) this;
    }

    protected NRepository getRepo() {
        return repo;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        checkSession();
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, args, getCommandName());
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
    public boolean configure(boolean skipUnsupported, NCommandLine commandLine) {
        checkSession();
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        checkSession();
        NArgument a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
//        switch(a.key()) {
//        }

        if (getSession().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public abstract T run();

    @Override
    public void configureLast(NCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument();
        }
    }
}
