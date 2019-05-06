/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsUpdateRepositoryStatisticsCommand;

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
    public NutsUpdateRepositoryStatisticsCommand parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("Unsupported option " + a);
                        } else {
                            //id(a.getString());
                        }
                    }
                }
            }
        }
        return this;
    }

}
