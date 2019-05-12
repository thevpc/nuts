/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsUpdateRepositoryStatisticsCommand extends NutsWorkspaceCommand {
    @Override
    NutsUpdateRepositoryStatisticsCommand session(NutsSession session);

    @Override
    NutsUpdateRepositoryStatisticsCommand setSession(NutsSession session);

    @Override
    NutsUpdateRepositoryStatisticsCommand configure(String... args);

    @Override
    NutsUpdateRepositoryStatisticsCommand run();
}
