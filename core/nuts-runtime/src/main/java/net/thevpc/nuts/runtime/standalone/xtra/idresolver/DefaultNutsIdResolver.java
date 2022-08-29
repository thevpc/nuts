package net.thevpc.nuts.runtime.standalone.xtra.idresolver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NutsPomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomId;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class DefaultNutsIdResolver implements NutsIdResolver {

    private NutsSession session;

    public DefaultNutsIdResolver(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsId resolveId(Class clazz) {
        List<NutsId> pomIds = resolveIds(clazz);
        NutsId defaultValue = null;
        if (pomIds.isEmpty()) {
            return null;
        }
        if (pomIds.size() > 1) {
            NutsLoggerOp.of(NutsPomXmlParser.class, session)
                    .verb(NutsLoggerVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NutsMessage.ofCstyle(
                            "multiple ids found : %s for class %s and id %s",
                            Arrays.asList(pomIds), clazz, defaultValue
                    ));
        }
        return pomIds.get(0);
    }

    @Override
    public List<NutsId> resolveIds(Class clazz) {
        NutsPomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz);
        LinkedHashSet<NutsId> all = new LinkedHashSet<>(
                Arrays.asList(new NutsMetaInfIdResolver(session).resolvePomIds(clazz))
        );
        for (NutsPomId uu : u) {
            all.add(NutsId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get(session));
        }
        return new ArrayList<>(all);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
