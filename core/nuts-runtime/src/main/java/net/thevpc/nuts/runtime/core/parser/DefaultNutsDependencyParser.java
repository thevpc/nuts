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
    public static final Pattern DEPENDENCY_NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");

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
            String protocol = m.group(2);
            String group = m.group(3);
            String name = m.group(5);
            String version = m.group(7);
            String face = CoreStringUtils.trim(m.group(9));
            Map<String, String> queryMap = QPARSER.parseMap(face);
            if (name == null) {
                name = group;
                group = null;
            }
            return session.getWorkspace().dependency().builder()
                    .setNamespace(protocol)
                    .setGroupId(group)
                    .setArtifactId(name)
                    .setVersion(version)
                    .setProperties(queryMap)
                    .build();
        }
        if (!isLenient()) {
            throw new NutsParseException(session, "invalid dependency format : " + dependency);
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
