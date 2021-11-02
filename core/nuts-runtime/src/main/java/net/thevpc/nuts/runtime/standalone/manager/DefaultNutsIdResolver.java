package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.mvn.PomId;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIdFormat;
import net.thevpc.nuts.runtime.core.model.DefaultNutsIdBuilder;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsIdParser;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsIdResolver implements NutsIdResolver {

    private NutsSession session;

    public DefaultNutsIdResolver(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = MavenUtils.createPomIdResolver(session).resolvePomId(clazz, null, session);
        if (u == null) {
            return null;
        }
        return NutsIdParser.of(session).parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz) {
        PomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz, session);
        NutsId[] all = new NutsId[u.length];
        NutsIdParser parser = NutsIdParser.of(session);
        for (int i = 0; i < all.length; i++) {
            all[i] = parser.parse(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}
