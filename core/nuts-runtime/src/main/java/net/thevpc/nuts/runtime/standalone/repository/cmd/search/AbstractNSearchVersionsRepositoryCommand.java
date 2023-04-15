/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NSearchVersionsRepositoryCommand;
import net.thevpc.nuts.util.NIterator;


/**
 *
 * @author thevpc
 */
public abstract class AbstractNSearchVersionsRepositoryCommand extends NRepositoryCommandBase<NSearchVersionsRepositoryCommand> implements NSearchVersionsRepositoryCommand {

    protected NId id;
    protected NIterator<NId> result;
    protected NIdFilter filter;

    public AbstractNSearchVersionsRepositoryCommand(NRepository repo) {
        super(repo, "search-versions");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

//    @Override
//    public NutsSearchVersionsRepositoryCommand filter(NutsIdFilter id) {
//        return setFilter(id);
//    }

    @Override
    public NSearchVersionsRepositoryCommand setFilter(NIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NIdFilter getFilter() {
        return filter;
    }

    @Override
    public NIterator<NId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NSearchVersionsRepositoryCommand setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}
