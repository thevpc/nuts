package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.File;
import java.net.URL;
import java.util.function.Function;

public interface NutsPathBuilder extends NutsFormattable {
    static NutsPathBuilder of(URL path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    static NutsPathBuilder of(String path, ClassLoader classLoader, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path, classLoader).builder();
    }

    static NutsPathBuilder of(File path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    static NutsPathBuilder of(String path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().io().path(path).builder();
    }

    Function<String, String> getVarResolver();

    NutsPathBuilder setVarResolver(Function<String, String> r);

    NutsPathBuilder withWorkspaceBaseDir();

    NutsPathBuilder withAppBaseDir();

    String getBaseDir();

    NutsPathBuilder setBaseDir(String baseDir);

    /**
     * expand path to {@code baseFolder}. Expansion mechanism supports '~'
     * prefix (linux like) and will expand path to {@code baseFolder} if it was
     * resolved as a relative path.
     *
     * @return expanded path
     */
    NutsPath build();

    boolean isExpandVars();

    NutsPathBuilder setExpandVars(boolean expandVars);
}
