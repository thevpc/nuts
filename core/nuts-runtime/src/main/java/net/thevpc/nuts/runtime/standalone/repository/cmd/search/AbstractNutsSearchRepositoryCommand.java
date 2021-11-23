/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositoryCommandBase;
import net.thevpc.nuts.spi.NutsSearchRepositoryCommand;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsSearchRepositoryCommand extends NutsRepositoryCommandBase<NutsSearchRepositoryCommand> implements NutsSearchRepositoryCommand {

    protected NutsIdFilter filter;
    protected NutsIterator<NutsId> result;

    public AbstractNutsSearchRepositoryCommand(NutsRepository repo) {
        super(repo, "search");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }


    @Override
    public NutsIterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsSearchRepositoryCommand setFilter(NutsIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getFilter() {
        return filter;
    }

}
