/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repocommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSearchVersionsRepositoryCommand;

import java.util.Iterator;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsSearchVersionsRepositoryCommand extends NutsRepositoryCommandBase<NutsSearchVersionsRepositoryCommand> implements NutsSearchVersionsRepositoryCommand {

    protected NutsId id;
    protected Iterator<NutsId> result;
    protected NutsIdFilter filter;

    public AbstractNutsSearchVersionsRepositoryCommand(NutsRepository repo) {
        super(repo, "search-versions");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

//    @Override
//    public NutsSearchVersionsRepositoryCommand filter(NutsIdFilter id) {
//        return setFilter(id);
//    }

    @Override
    public NutsSearchVersionsRepositoryCommand setFilter(NutsIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getFilter() {
        return filter;
    }

    @Override
    public Iterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsSearchVersionsRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
