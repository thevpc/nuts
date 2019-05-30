/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommand;

/**
 *
 * @author vpc
 */
public abstract class DefaultNutsUpdateRepositoryStatisticsCommand extends NutsWorkspaceCommandBase<NutsUpdateRepositoryStatisticsCommand>
        implements NutsUpdateRepositoryStatisticsCommand {

    protected NutsRepository repo;

    public DefaultNutsUpdateRepositoryStatisticsCommand(NutsRepository repo) {
        super(repo.getWorkspace(),"update-repo-statistics");
        this.repo = repo;
    }

    @Override
    public boolean configureFirst(NutsCommand cmdLine) {
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
