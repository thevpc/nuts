/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NUpdateStatsCmd;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author thevpc
 */
public abstract class AbstractNUpdateStatsCmd extends NWorkspaceCmdBase<NUpdateStatsCmd> implements NUpdateStatsCmd {

    private LinkedHashSet<Path> paths = new LinkedHashSet<>();
    private LinkedHashSet<String> repositories = new LinkedHashSet<>();

    public AbstractNUpdateStatsCmd(NWorkspace workspace) {
        super(workspace, "update-statistics");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NUpdateStatsCmd clearRepos() {
        repositories.clear();
        return this;
    }

    @Override
    public NUpdateStatsCmd addRepo(String s) {
        if (s != null) {
            repositories.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd removeRepo(String s) {
        if (s != null) {
            repositories.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd addRepos(String... all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd addRepos(Collection<String> all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    public String[] getRepositories() {
        return repositories.toArray(new String[0]);
    }

    @Override
    public NUpdateStatsCmd clearPaths() {
        paths.clear();
        return this;
    }

    @Override
    public NUpdateStatsCmd addPath(Path s) {
        if (s != null) {
            paths.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd removePath(Path s) {
        if (s != null) {
            paths.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd addPaths(Path... all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStatsCmd addPaths(Collection<Path> all) {
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
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session=getWorkspace().currentSession();
        NArg a = cmdLine.peek().get();
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
