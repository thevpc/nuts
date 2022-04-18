package net.thevpc.nuts.runtime.standalone.xtra.idresolver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.PomId;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.Arrays;
import java.util.List;

public class DefaultNutsIdResolver implements NutsIdResolver {

    private NutsSession session;

    public DefaultNutsIdResolver(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = MavenUtils.createPomIdResolver(session).resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return NutsId.of(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion()).get(session);
    }

    @Override
    public List<NutsId> resolveIds(Class clazz) {
        PomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = NutsId.of(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion()).get(session);
        }
        return Arrays.asList(all);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
