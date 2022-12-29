/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NSearchRepositoryCommand;
import net.thevpc.nuts.util.NIterator;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNSearchRepositoryCommand extends NRepositoryCommandBase<NSearchRepositoryCommand> implements NSearchRepositoryCommand {

    protected NIdFilter filter;
    protected NIterator<NId> result;

    public AbstractNSearchRepositoryCommand(NRepository repo) {
        super(repo, "search");
    }

    @Override
    public boolean configureFirst(NCommandLine cmd) {
        if (super.configureFirst(cmd)) {
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
    public NSearchRepositoryCommand setFilter(NIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NIdFilter getFilter() {
        return filter;
    }

}
