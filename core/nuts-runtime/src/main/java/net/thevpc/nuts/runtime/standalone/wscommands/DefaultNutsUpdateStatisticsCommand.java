/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repos.NutsRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

/**
 * @author thevpc
 */
public class DefaultNutsUpdateStatisticsCommand extends AbstractNutsUpdateStatisticsCommand {

    public DefaultNutsUpdateStatisticsCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUpdateStatisticsCommand run() {
        boolean processed = false;
        NutsSession session = getValidWorkspaceSession();
        for (String repository : getRepositrories()) {
            processed = true;
            NutsRepository repo = ws.repos().getRepository(repository, session);
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
            repoSPI.updateStatistics()
                    .setSession(session)
//                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
        }
        for (Path repositoryPath : getPaths()) {
            processed = true;
            if (repositoryPath == null) {
                throw new NutsIllegalArgumentException(ws, "missing location " + repositoryPath);
            }
            if (!Files.isDirectory(repositoryPath)) {
                throw new NutsIllegalArgumentException(ws, "expected folder at location " + repositoryPath);
            }
            File[] mavenRepoRootFiles = repositoryPath.toFile().listFiles(x
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
            if (mavenRepoRootFiles != null && mavenRepoRootFiles.length > 3) {
                new MavenRepositoryFolderHelper(null, ws, repositoryPath).reindexFolder(getSession());
                if (session.isPlainTrace()) {
                    session.getTerminal().out().printf("[%s] updated maven index %s%n", getWorkspace().locations().getWorkspaceLocation(), repositoryPath);
                }
            } else {
                File[] nutsRepoRootFiles = repositoryPath.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles != null && nutsRepoRootFiles.length > 0) {
                    new NutsRepositoryFolderHelper(null, ws, repositoryPath,false).reindexFolder();
                } else {
                    throw new NutsIllegalArgumentException(ws, "unsupported repository Folder");
                }
                if (session.isPlainTrace()) {
                    session.out().printf("[%s] updated stats %s%n", getWorkspace().locations().getWorkspaceLocation(), repositoryPath);
                }
            }
        }
        NutsTextNodeFactory factory = getWorkspace().formats().text().factory();
        if (!processed) {
            if (session.isPlainTrace()) {
                session.out().printf("%s updating workspace stats%n", factory.styled(getWorkspace().locations().getWorkspaceLocation(),NutsTextNodeStyle.path()));
            }
            for (NutsRepository repo : getWorkspace().repos().getRepositories(session)) {
                if (session.isPlainTrace()) {
                    session.out().printf("%s updating stats %s%n", factory.styled(getWorkspace().locations().getWorkspaceLocation(),NutsTextNodeStyle.path()), repo);
                }
                NutsWorkspaceUtils.of(ws).repoSPI(repo).updateStatistics()
                        .setSession(session)
//                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run();
            }
        }
        return this;
    }

    @Override
    public void add(String repo) {
        if (repo == null) {
            throw new NutsIllegalArgumentException(getWorkspace(), "missing repo or path");
        }
        if (repo.equals(".") || repo.equals("..") || repo.contains("/") || repo.contains("\\")) {
            addPath(Paths.get(repo));
        } else {
            addRepo(repo);
        }
    }
}
