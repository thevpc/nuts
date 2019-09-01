/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repocommands;

import net.vpc.app.nuts.*;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public abstract class AbstractNutsSearchRepositoryCommand extends NutsRepositoryCommandBase<NutsSearchRepositoryCommand> implements NutsSearchRepositoryCommand {

    protected NutsIdFilter filter;
    protected Iterator<NutsId> result;

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
    public Iterator<NutsId> getResult() {
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
