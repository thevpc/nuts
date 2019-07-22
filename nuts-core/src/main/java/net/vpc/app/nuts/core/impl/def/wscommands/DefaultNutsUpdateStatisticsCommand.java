/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.wscommands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsUpdateStatisticsCommand;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.wscommands.AbstractNutsUpdateStatisticsCommand;
import net.vpc.app.nuts.core.bridges.maven.MavenRepositoryFolderHelper;
import net.vpc.app.nuts.core.impl.def.repos.NutsRepositoryFolderHelper;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateStatisticsCommand extends AbstractNutsUpdateStatisticsCommand {

    public DefaultNutsUpdateStatisticsCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUpdateStatisticsCommand run() {
        for (String repository : getRepositrories()) {
            NutsRepository repo = ws.config().getRepository(repository, true);
            repo.updateStatistics()
                    .setSession(NutsWorkspaceHelper.createRepositorySession(                                    getValidSession(), repo, NutsFetchMode.LOCAL, null)
                    )
                    .run();
        }
        for (String repositoryPath : getPaths()) {
            if (repositoryPath == null) {
                throw new NutsIllegalArgumentException(ws, "Missing location " + repositoryPath);
            }
            Path pp = Paths.get(repositoryPath);
            if (!Files.isDirectory(pp)) {
                throw new NutsIllegalArgumentException(ws, "Expected folder at location " + repositoryPath);
            }
            File[] mavenRepoRootFiles = pp.toFile().listFiles(x
                    -> x.getName().equals("index.html")
                    || x.getName().equals("plugin-management.html")
                    || x.getName().equals("distribution-management.html")
                    || x.getName().equals("dependency-info.html")
                    || x.getName().equals("dependency-convergence.html")
                    || x.getName().equals("dependencies.html")
                    || x.getName().equals("plugins.html")
                    || x.getName().equals("project-info.html")
                    || x.getName().equals("project-summary.html")
            );
            if (mavenRepoRootFiles.length > 3) {
                new MavenRepositoryFolderHelper(null, ws, pp).reindexFolder();
                if (getValidSession().isPlainTrace()) {
                    getValidSession().getTerminal().out().printf("Updated maven index %s%n", getWorkspace().config().getWorkspaceLocation(), pp);
                }
            } else {
                File[] nutsRepoRootFiles = pp.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles.length > 0) {
                    new NutsRepositoryFolderHelper(null, ws, pp).reindexFolder();
                } else {
                    throw new NutsIllegalArgumentException(ws, "Unsupported repository Folder");
                }
                if (getValidSession().isPlainTrace()) {
                    getValidSession().getTerminal().out().printf("Updated nuts index %s%n", getWorkspace().config().getWorkspaceLocation(), pp);
                }
            }
        }
        return this;
    }

}
