/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NAssert;

/**
 * @author thevpc
 */
public class DefaultNUpdateStatisticsCommand extends AbstractNUpdateStatisticsCommand {

    public DefaultNUpdateStatisticsCommand(NSession session) {
        super(session);
    }

    @Override
    public NUpdateStatisticsCommand run() {
        boolean processed = false;
        NSession session = getSession();
        checkSession();
        for (String repository : getRepositories()) {
            processed = true;
            NRepository repo = NRepositories.of(getSession()).getRepository(repository);
            NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
            repoSPI.updateStatistics()
                    .setSession(session)
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
        }
        for (Path repositoryPath : getPaths()) {
            processed = true;
            NAssert.requireNonBlank(repositoryPath, "location", session);
            if (!Files.isDirectory(repositoryPath)) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofC("expected folder at location %s",repositoryPath));
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
                new MavenRepositoryFolderHelper(null, getSession(), NPath.of(repositoryPath,session)).reindexFolder(getSession());
                if (session.isPlainTrace()) {
                    session.getTerminal().out().resetLine().println(NMsg.ofC("[%s] updated maven index %s", NLocations.of(getSession()).getWorkspaceLocation(), repositoryPath));
                }
            } else {
                File[] nutsRepoRootFiles = repositoryPath.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles != null && nutsRepoRootFiles.length > 0) {
                    new NRepositoryFolderHelper(null, session, NPath.of(repositoryPath,session), false,"stats",null).reindexFolder(session);
                } else {
                    throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("unsupported repository folder"));
                }
                if (session.isPlainTrace()) {
                    session.out().resetLine().println(NMsg.ofC("[%s] updated stats %s", NLocations.of(getSession()).getWorkspaceLocation(), repositoryPath));
                }
            }
        }
        if (!processed) {
            if (session.isPlainTrace()) {
                session.out().resetLine().println(NMsg.ofC("%s updating workspace stats", NLocations.of(getSession()).getWorkspaceLocation()));
            }
            for (NRepository repo : NRepositories.of(getSession()).getRepositories()) {
                if (session.isPlainTrace()) {
                    session.out().resetLine().println(NMsg.ofC("%s updating stats %s", NLocations.of(getSession()).getWorkspaceLocation(), repo));
                }
                NWorkspaceUtils.of(session).repoSPI(repo).updateStatistics()
                        .setSession(session)
                        //                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run();
            }
        }
        return this;
    }

    @Override
    public void add(String repo) {
        NAssert.requireNonBlank(repo, "repository or path", session);
        if (repo.equals(".") || repo.equals("..") || repo.contains("/") || repo.contains("\\")) {
            addPath(Paths.get(repo));
        } else {
            addRepo(repo);
        }
    }
}
