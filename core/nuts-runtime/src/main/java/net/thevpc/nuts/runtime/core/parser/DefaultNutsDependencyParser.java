package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.thevpc.nuts.runtime.bundles.parsers.StringMapParser;

public class DefaultNutsDependencyParser implements NutsDependencyParser {
    private static final StringMapParser QPARSER = new StringMapParser("=","&");
    private NutsSession session;
    private boolean lenient=false;
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}-]+)(:(?<artifact>[a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");

    public DefaultNutsDependencyParser(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsDependencyParser setLenient(boolean lenient) {
        this.lenient=lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDependency parseDependency(String dependency) {
        if (dependency == null) {
            return null;
        }
        Matcher m = DEPENDENCY_NUTS_DESCRIPTOR_PATTERN.matcher(dependency);
        if (m.find()) {
            String group = m.group("group");
            String name = m.group("artifact");
            String version = m.group("version");
            String face = CoreStringUtils.trim(m.group("face"));
            Map<String, String> queryMap = QPARSER.parseMap(face);
            if (name == null) {
                name = group;
                group = null;
            }
            return session.getWorkspace().dependency().builder()
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
    public NutsDependencyScope parseScope(String scope) {
        return NutsDependencyScopes.parseScope(scope,isLenient());
    }

    @Override
    public boolean parseOptional(String optional) {
        if(CoreStringUtils.isBlank(optional)){
            return false;
        }
        return "true".equals(optional.trim());
    }
}
