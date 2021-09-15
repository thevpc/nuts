/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repocommands;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.spi.NutsUpdateRepositoryStatisticsCommand;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsUpdateRepositoryStatisticsCommand extends NutsRepositoryCommandBase<NutsUpdateRepositoryStatisticsCommand>
        implements NutsUpdateRepositoryStatisticsCommand {

    protected NutsRepository repo;

    public AbstractNutsUpdateRepositoryStatisticsCommand(NutsRepository repo) {
        super(repo, "update-repo-statistics");
        this.repo = repo;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
