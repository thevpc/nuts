package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringMapParser;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsDependencyParser implements NutsDependencyParser {
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}-]+)(:(?<artifact>[a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    private static final StringMapParser QPARSER = new StringMapParser("=", "&");
    private final NutsSession session;
    private boolean lenient = false;

    public DefaultNutsDependencyParser(NutsSession session) {
        this.session = session;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDependencyParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NutsDependency parse(String dependency) {
        if (dependency == null) {
            return null;
        }
        Matcher m = DEPENDENCY_NUTS_DESCRIPTOR_PATTERN.matcher(dependency);
        if (m.find()) {
            String group = m.group("group");
            String name = m.group("artifact");
            String version = m.group("version");
            String face = NutsUtilStrings.trim(m.group("face"));
            Map<String, String> queryMap = QPARSER.parseMap(face,session);
            if (name == null) {
                name = group;
                group = null;
            }
            return NutsDependencyBuilder.of(session)
                    .setGroupId(group)
                    .setArtifactId(name)
                    .setVersion(version)
                    .setProperties(queryMap)
                    .build();
        }
        if (!isLenient()) {
            throw new NutsParseException(session, NutsMessage.cstyle("invalid dependency format : %s", dependency));
        }
        return null;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
