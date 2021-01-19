package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsIdBuilder;
import net.thevpc.nuts.runtime.bundles.mvn.PomId;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIdFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsIdParser;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;

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
        return formatter().setValue(id);
    }

    @Override
    public NutsIdFilterManager filter() {
        return ws.filters().id();
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(ws);
    }

    @Override
    public NutsId resolveId(Class clazz, NutsSession session) {
        PomId u = MavenUtils.createPomIdResolver(session).resolvePomId(clazz, null, session);
        if (u == null) {
            return null;
        }
        return parser().parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz, NutsSession session) {
        PomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz,session);
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
