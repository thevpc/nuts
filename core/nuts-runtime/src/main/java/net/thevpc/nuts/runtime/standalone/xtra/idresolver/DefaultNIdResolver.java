package net.thevpc.nuts.runtime.standalone.xtra.idresolver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NPomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class DefaultNIdResolver implements NIdResolver {

    private NSession session;

    public DefaultNIdResolver(NSession session) {
        this.session = session;
    }

    @Override
    public NId resolveId(Class clazz) {
        List<NId> pomIds = resolveIds(clazz);
        NId defaultValue = null;
        if (pomIds.isEmpty()) {
            return null;
        }
        if (pomIds.size() > 1) {
            NLoggerOp.of(NPomXmlParser.class, session)
                    .verb(NLoggerVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofCstyle(
                            "multiple ids found : %s for class %s and id %s",
                            Arrays.asList(pomIds), clazz, defaultValue
                    ));
        }
        return pomIds.get(0);
    }

    @Override
    public List<NId> resolveIds(Class clazz) {
        NPomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz);
        LinkedHashSet<NId> all = new LinkedHashSet<>(
                Arrays.asList(new NMetaInfIdResolver(session).resolvePomIds(clazz))
        );
        for (NPomId uu : u) {
            all.add(NId.of(uu.getGroupId() + ":" + uu.getArtifactId() + "#" + uu.getVersion()).get(session));
        }
        return new ArrayList<>(all);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
