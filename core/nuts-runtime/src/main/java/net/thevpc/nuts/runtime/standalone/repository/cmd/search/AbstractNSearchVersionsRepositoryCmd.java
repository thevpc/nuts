/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NSearchVersionsRepositoryCmd;
import net.thevpc.nuts.util.NIterator;


/**
 *
 * @author thevpc
 */
public abstract class AbstractNSearchVersionsRepositoryCmd extends NRepositoryCmdBase<NSearchVersionsRepositoryCmd> implements NSearchVersionsRepositoryCmd {

    protected NId id;
    protected NIterator<NId> result;
    protected NIdFilter filter;

    public AbstractNSearchVersionsRepositoryCmd(NRepository repo) {
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
    public NSearchVersionsRepositoryCmd setFilter(NIdFilter filter) {
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
    public NSearchVersionsRepositoryCmd setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}
