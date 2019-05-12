/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsUpdateRepositoryStatisticsCommand;
import net.vpc.app.nuts.NutsArgument;

/**
 *
 * @author vpc
 */
public abstract class DefaultNutsUpdateRepositoryStatisticsCommand extends NutsWorkspaceCommandBase<NutsUpdateRepositoryStatisticsCommand>
        implements NutsUpdateRepositoryStatisticsCommand {

    protected NutsRepository repo;

    public DefaultNutsUpdateRepositoryStatisticsCommand(NutsRepository repo) {
        super(repo.getWorkspace());
        this.repo = repo;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.strKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
