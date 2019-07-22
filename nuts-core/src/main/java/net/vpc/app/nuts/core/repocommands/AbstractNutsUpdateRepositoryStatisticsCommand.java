/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repocommands;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsUpdateRepositoryStatisticsCommand;

/**
 *
 * @author vpc
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
        switch (a.getStringKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
