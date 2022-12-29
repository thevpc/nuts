/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.NUpdateStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author thevpc
 */
public abstract class AbstractNUpdateStatisticsCommand extends NWorkspaceCommandBase<NUpdateStatisticsCommand> implements NUpdateStatisticsCommand {

    private LinkedHashSet<Path> paths = new LinkedHashSet<>();
    private LinkedHashSet<String> repositrories = new LinkedHashSet<>();

    public AbstractNUpdateStatisticsCommand(NSession ws) {
        super(ws, "update-statistics");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NUpdateStatisticsCommand clearRepos() {
        repositrories.clear();
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addRepo(String s) {
        if (s != null) {
            repositrories.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand removeRepo(String s) {
        if (s != null) {
            repositrories.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addRepos(String... all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addRepos(Collection<String> all) {
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
    public NUpdateStatisticsCommand clearPaths() {
        paths.clear();
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addPath(Path s) {
        if (s != null) {
            paths.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand removePath(Path s) {
        if (s != null) {
            paths.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addPaths(Path... all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStatisticsCommand addPaths(Collection<Path> all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    public Path[] getPaths() {
        return paths.toArray(new Path[0]);
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }
}
