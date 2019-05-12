/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author vpc
 */
public interface NutsUpdateStatisticsCommand extends NutsWorkspaceCommand {

 NutsUpdateStatisticsCommand clearRepos();

    NutsUpdateStatisticsCommand repo(String s);

    NutsUpdateStatisticsCommand addRepo(String s);

    NutsUpdateStatisticsCommand removeRepo(String s);

    NutsUpdateStatisticsCommand addRepos(String... all);

    NutsUpdateStatisticsCommand addRepos(Collection<String> all);

    NutsUpdateStatisticsCommand clearPaths();

    NutsUpdateStatisticsCommand path(Path s);

    NutsUpdateStatisticsCommand addPath(Path s);

    NutsUpdateStatisticsCommand removePath(Path s);

    NutsUpdateStatisticsCommand addPaths(Path... all);

    NutsUpdateStatisticsCommand addPaths(Collection<Path> all);

    @Override
    NutsUpdateStatisticsCommand session(NutsSession session);

    @Override
    NutsUpdateStatisticsCommand setSession(NutsSession session);

    @Override
    NutsUpdateStatisticsCommand configure(String... args);

    @Override
    NutsUpdateStatisticsCommand run();
}
