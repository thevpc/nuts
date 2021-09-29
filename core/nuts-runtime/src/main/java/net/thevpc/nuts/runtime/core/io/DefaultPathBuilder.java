package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class DefaultPathBuilder implements NutsPathBuilder {
    EffectiveResolver effectiveResolver;
    private Function<String, String> resolver;
    private NutsSession session;
    private String baseDir;
    private NutsPath base;
    private boolean expandVars = true;

    public DefaultPathBuilder(NutsSession session, NutsPath base) {
        this.session = session;
        this.base = base;
        this.effectiveResolver = new EffectiveResolver(session);
    }

    @Override
    public NutsFormat formatter() {
        return build().formatter();
    }

    @Override
    public Function<String, String> getVarResolver() {
        return resolver;
    }

    @Override
    public NutsPathBuilder setVarResolver(Function<String, String> resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public NutsPathBuilder withWorkspaceBaseDir() {
        setBaseDir(session.locations().getWorkspaceLocation());
        return this;
    }

    @Override
    public NutsPathBuilder withAppBaseDir() {
        setBaseDir(System.getProperty("user.dir"));
        return this;
    }

    @Override
    public String getBaseDir() {
        if (baseDir == null) {
            return System.getProperty("user.dir");
        }
        return baseDir;
    }

    @Override
    public NutsPathBuilder setBaseDir(String baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    @Override
    public NutsPath build() {
        NutsPath p = expandVars ? expandVars(base) : base;
        if (baseDir != null) {
            if (p.isFilePath()) {
                return expandFile(p);
            }
        }
        return base;
    }

    public boolean isExpandVars() {
        return expandVars;
    }

    public NutsPathBuilder setExpandVars(boolean expandVars) {
        this.expandVars = expandVars;
        return this;
    }

    @Override
    public String toString() {
        try {
            return build().toString();
        } catch (Exception e) {
            return base.toString();
        }
    }

    private NutsPath expandVars(NutsPath path) {
        NutsIOManager io = session.io();
        return io.path(StringPlaceHolderParser.replaceDollarPlaceHolders(path.toString(), effectiveResolver));
    }

    private NutsPath expandFile(NutsPath npath) {
        Path fp = npath.toFilePath();
        NutsIOManager io = session.io();
        if (fp != null && fp.toString().length() > 0) {
            Path ppath = fp;
            String path = fp.toString();
//            if (path.startsWith("file:") || path.startsWith("http://") || path.startsWith("https://")) {
//                return path;
//            }
            if (path.startsWith("~")) {
                NutsWorkspaceLocationManager locations = session.locations();
                if (path.equals("~~")) {
                    Path nutsHome = Paths.get(locations.getHomeLocation(NutsStoreLocation.CONFIG));
                    return io.path(nutsHome.normalize().toString());
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = Paths.get(locations.getHomeLocation(NutsStoreLocation.CONFIG));
                    return io.path(nutsHome.resolve(path.substring(3)).normalize().toString());
                } else if (path.equals("~")) {
                    return io.path(System.getProperty("user.home"));
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return io.path(System.getProperty("user.home") + File.separator + path.substring(2));
                } else if (baseDir != null) {
                    if (CoreIOUtils.isURL(baseDir)) {
                        return io.path(baseDir + "/" + path);
                    }
                    return io.path(Paths.get(baseDir).resolve(path).toAbsolutePath().normalize());
                } else {
                    if (CoreIOUtils.isURL(path)) {
                        return io.path(path);
                    }
                    return io.path(ppath.toAbsolutePath().normalize());
                }
            } else if (path.equals(".") || path.equals("..")) {
                return io.path(ppath.toAbsolutePath().normalize());
            } else if (ppath.isAbsolute()) {
                return io.path(ppath.normalize());
            } else if (baseDir != null) {
                if (CoreIOUtils.isURL(baseDir)) {
                    return io.path(baseDir + "/" + path);
                }
                return io.path(Paths.get(baseDir).resolve(path).toAbsolutePath().normalize());
            } else {
                return io.path(ppath.toAbsolutePath().normalize());
            }
        }
        return npath;
    }

    private class EffectiveResolver implements Function<String, String> {
        NutsWorkspaceVarExpansionFunction fallback;
        NutsSession session;

        public EffectiveResolver(NutsSession session) {
            this.session = session;
            fallback = new NutsWorkspaceVarExpansionFunction(session.getWorkspace());
        }

        @Override
        public String apply(String s) {
            if (resolver != null) {
                String v = resolver.apply(s);
                if (v != null) {
                    return v;
                }
            }
            return fallback.apply(s);
        }
    }
}
