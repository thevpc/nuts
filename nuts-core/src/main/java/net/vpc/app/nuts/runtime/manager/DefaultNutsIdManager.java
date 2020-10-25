package net.vpc.app.nuts.runtime.manager;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsIdBuilder;
import net.vpc.app.nuts.runtime.bridges.maven.mvnutil.PomId;
import net.vpc.app.nuts.runtime.bridges.maven.mvnutil.PomIdResolver;
import net.vpc.app.nuts.runtime.format.DefaultNutsIdFormat;
import net.vpc.app.nuts.runtime.parser.DefaultNutsIdParser;

public class DefaultNutsIdManager implements NutsIdManager {
    private NutsWorkspace ws;

    public DefaultNutsIdManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsIdFormat formatter() {
        return new DefaultNutsIdFormat(ws);
    }

    @Override
    public NutsIdFormat formatter(NutsId id) {
        return formatter().set(id);
    }

    @Override
    public NutsIdFilterManager filter() {
        return ws.filters().id();
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = PomIdResolver.of(getWorkspace()).resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return parser().parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz) {
        PomId[] u = PomIdResolver.of(getWorkspace()).resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        NutsIdParser parser = parser();
        for (int i = 0; i < all.length; i++) {
            all[i] = parser.parse(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public NutsIdParser parser() {
        return new DefaultNutsIdParser(getWorkspace());
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }
}
