/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsUpdateStatisticsCommand;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.bridges.maven.MavenRepositoryFolderHelper;
import net.vpc.app.nuts.core.repos.NutsRepositoryFolderHelper;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateStatisticsCommand extends NutsWorkspaceCommandBase<NutsUpdateStatisticsCommand> implements NutsUpdateStatisticsCommand {

    private LinkedHashSet<Path> paths = new LinkedHashSet<>();
    private LinkedHashSet<String> repositrories = new LinkedHashSet<>();

    public DefaultNutsUpdateStatisticsCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUpdateStatisticsCommand clearRepos() {
        repositrories.clear();
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand repo(String s) {
        return addRepo(s);
    }

    @Override
    public NutsUpdateStatisticsCommand addRepo(String s) {
        if (s != null) {
            repositrories.add(s);
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand removeRepo(String s) {
        if (s != null) {
            repositrories.remove(s);
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand addRepos(String... all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand addRepos(Collection<String> all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    public String[] getRepositrories() {
        return repositrories.toArray(new String[0]);
    }

    @Override
    public NutsUpdateStatisticsCommand clearPaths() {
        paths.clear();
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand path(Path s) {
        return addPath(s);
    }

    @Override
    public NutsUpdateStatisticsCommand addPath(Path s) {
        if (s != null) {
            paths.add(s);
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand removePath(Path s) {
        if (s != null) {
            paths.remove(s);
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand addPaths(Path... all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateStatisticsCommand addPaths(Collection<Path> all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    public String[] getPaths() {
        return repositrories.toArray(new String[0]);
    }

    @Override
    public NutsUpdateStatisticsCommand parseOptions(String... args) {
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

    @Override
    public NutsUpdateStatisticsCommand run() {
        for (String repository : getRepositrories()) {
            ws.config().getRepository(repository).updateStatistics()
                    .trace(isTrace())
                    .run();
        }
        for (String repositoryPath : getPaths()) {
            if (repositoryPath == null) {
                throw new NutsIllegalArgumentException("Missing location " + repositoryPath);
            }
            Path pp = Paths.get(repositoryPath);
            if (!Files.isDirectory(pp)) {
                throw new NutsIllegalArgumentException("Expected folder at location " + repositoryPath);
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
                if (isTrace()) {
                    getValidSession().getTerminal().out().printf("Updated maven index %s%n", getWs().config().getWorkspaceLocation(), pp);
                }
            } else {
                File[] nutsRepoRootFiles = pp.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles.length > 0) {
                    new NutsRepositoryFolderHelper(null, ws, pp).reindexFolder();
                } else {
                    throw new NutsIllegalArgumentException("Unsupported repository Folder");
                }
                if (isTrace()) {
                    getValidSession().getTerminal().out().printf("Updated nuts index %s%n", getWs().config().getWorkspaceLocation(), pp);
                }
            }
        }
        return this;
    }

}
