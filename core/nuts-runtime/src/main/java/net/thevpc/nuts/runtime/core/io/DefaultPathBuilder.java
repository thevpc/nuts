package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.expr.StringPlaceHolderParser;
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
    private NutsPath baseDir;
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
        setBaseDir(NutsPath.of(System.getProperty("user.dir"),session));
        return this;
    }

    @Override
    public NutsPath getBaseDir() {
        if (baseDir == null) {
            return NutsPath.of(System.getProperty("user.dir"),session);
        }
        return baseDir;
    }

    @Override
    public NutsPathBuilder setBaseDir(NutsPath baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    @Override
    public NutsPath build() {
        NutsPath p = expandVars ? expandVars(base) : base;
        if (baseDir != null) {
            if (p.isFile()) {
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
        return NutsPath.of(StringPlaceHolderParser.replaceDollarPlaceHolders(path.toString(), effectiveResolver),session);
    }

    private NutsPath expandFile(NutsPath npath) {
        Path fp = npath.toFile();
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
                    return NutsPath.of(nutsHome.normalize(),session);
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = Paths.get(locations.getHomeLocation(NutsStoreLocation.CONFIG));
                    return NutsPath.of(nutsHome.resolve(path.substring(3)).normalize(),session);
                } else if (path.equals("~")) {
                    return NutsPath.of(System.getProperty("user.home"),session);
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return NutsPath.of(System.getProperty("user.home") + File.separator + path.substring(2),session);
                } else if (baseDir != null) {
                    if (baseDir.isURL()) {
                        return baseDir.resolve(path);
                    }
                    return baseDir.resolve(path).toAbsolute().normalize();
                } else {
                    if (CoreIOUtils.isURL(path)) {
                        return NutsPath.of(path,session);
                    }
                    return NutsPath.of(ppath.toAbsolutePath().normalize(),session);
                }
            } else if (path.equals(".") || path.equals("..")) {
                return NutsPath.of(ppath.toAbsolutePath().normalize(),session);
            } else if (ppath.isAbsolute()) {
                return NutsPath.of(ppath.normalize(),session);
            } else if (baseDir != null) {
                if (baseDir.isURL()) {
                    return baseDir.resolve( path);
                }
                return baseDir.resolve(path).toAbsolute().normalize();
            } else {
                return NutsPath.of(ppath.toAbsolutePath().normalize(),session);
            }
        }
        return npath;
    }

    private class EffectiveResolver implements Function<String, String> {
        NutsWorkspaceVarExpansionFunction fallback;
        NutsSession session;

        public EffectiveResolver(NutsSession session) {
            this.session = session;
            fallback = new NutsWorkspaceVarExpansionFunction(session);
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
