/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NSearchRepositoryCmd;
import net.thevpc.nuts.util.NIterator;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNSearchRepositoryCmd extends NRepositoryCmdBase<NSearchRepositoryCmd> implements NSearchRepositoryCmd {

    protected NDefinitionFilter filter;
    protected NIterator<NId> result;

    public AbstractNSearchRepositoryCmd(NRepository repo) {
        super(repo, "search");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }


    @Override
    public NIterator<NId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NSearchRepositoryCmd setFilter(NDefinitionFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NDefinitionFilter getFilter() {
        return filter;
    }

}
