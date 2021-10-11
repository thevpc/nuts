package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsIdBuilder;
import net.thevpc.nuts.runtime.bundles.mvn.PomId;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIdFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsIdParser;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsIdManager implements NutsIdManager {

    private NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsIdManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIdManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsIdFormat formatter() {
        return new DefaultNutsIdFormat(ws).setSession(getSession());
    }

    @Override
    public NutsIdFormat formatter(NutsId id) {
        return formatter().setValue(id);
    }

    @Override
    public NutsIdFilterManager filter() {
        return getSession().filters().id().setSession(getSession());
    }

    @Override
    public NutsIdBuilder builder() {
        checkSession();
        return new DefaultNutsIdBuilder(getSession());
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = MavenUtils.createPomIdResolver(session).resolvePomId(clazz, null, session);
        if (u == null) {
            return null;
        }
        return parser().parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz) {
        PomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz, session);
        NutsId[] all = new NutsId[u.length];
        NutsIdParser parser = parser();
        for (int i = 0; i < all.length; i++) {
            all[i] = parser.parse(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }


    @Override
    public NutsIdParser parser() {
        checkSession();
        return new DefaultNutsIdParser(getSession()).setAcceptBlank(true).setLenient(false);
    }

    @Override
    public NutsId parse(String id) {
        return parser().setAcceptBlank(true).setLenient(false).parse(id);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }
}
