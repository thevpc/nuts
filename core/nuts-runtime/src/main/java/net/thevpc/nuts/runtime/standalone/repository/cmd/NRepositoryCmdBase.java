/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.spi.NRepositoryCmd;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NRepositoryCmdBase<T extends NRepositoryCmd> implements NRepositoryCmd {

    protected NRepository repo;
    private NFetchMode fetchMode = NFetchMode.LOCAL;
    private String commandName;

    public NRepositoryCmdBase(NRepository repo, String commandName) {
        this.repo = repo;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
//    protected T copyFromWorkspaceCommandBase(NRepositoryCmdBase other) {
//        if (other != null) {
//            this.session = other.getSession();
//        }
//        return (T) this;
//    }

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
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NCmdLineConfigurable.configure(this, skipUnsupported, args,getCommandName());
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
//        switch(a.key()) {
//        }
        NSession session = repo.getWorkspace().currentSession();
        if (configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public abstract T run();

}
