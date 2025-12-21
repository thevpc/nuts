/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;

import net.thevpc.nuts.command.NUpdateStats;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNUpdateStats extends NWorkspaceCmdBase<NUpdateStats> implements NUpdateStats {

    private LinkedHashSet<Path> paths = new LinkedHashSet<>();
    private LinkedHashSet<String> repositories = new LinkedHashSet<>();

    public AbstractNUpdateStats() {
        super("update-statistics");
    }

    @Override
    public NUpdateStats clearRepos() {
        repositories.clear();
        return this;
    }

    @Override
    public NUpdateStats addRepo(String s) {
        if (s != null) {
            repositories.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStats removeRepo(String s) {
        if (s != null) {
            repositories.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStats addRepos(String... all) {
        if (all != null) {
            for (String string : all) {
                addRepo(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStats addRepos(Collection<String> all) {
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
    public NUpdateStats clearPaths() {
        paths.clear();
        return this;
    }

    @Override
    public NUpdateStats addPath(Path s) {
        if (s != null) {
            paths.add(s);
        }
        return this;
    }

    @Override
    public NUpdateStats removePath(Path s) {
        if (s != null) {
            paths.remove(s);
        }
        return this;
    }

    @Override
    public NUpdateStats addPaths(Path... all) {
        if (all != null) {
            for (Path string : all) {
                addPath(string);
            }
        }
        return this;
    }

    @Override
    public NUpdateStats addPaths(Collection<Path> all) {
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
