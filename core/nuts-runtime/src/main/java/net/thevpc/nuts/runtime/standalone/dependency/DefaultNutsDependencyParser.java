package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsEnvConditionBuilder;
import net.thevpc.nuts.runtime.standalone.id.NutsIdListHelper;
import net.thevpc.nuts.runtime.standalone.xtra.expr.CommaStringParser;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringMapParser;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
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
            NutsDependencyBuilder b = NutsDependencyBuilder.of(session)
                    .setGroupId(group)
                    .setArtifactId(name)
                    .setVersion(version);
            DefaultNutsEnvConditionBuilder sb=(DefaultNutsEnvConditionBuilder) NutsEnvConditionBuilder.of(session);
            Map<String, String> props=new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                switch (e.getKey()){
                    case NutsConstants.IdProperties.CLASSIFIER:{
                        b.setClassifier(e.getValue());
                        break;
                    }
                    case NutsConstants.IdProperties.PROFILE:{
                        sb.setProfile(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case NutsConstants.IdProperties.PLATFORM:{
                        sb.setPlatform(NutsIdListHelper.parseIdListStrings(e.getValue(),session));
                        break;
                    }
                    case NutsConstants.IdProperties.OS_DIST:{
                        sb.setOsDist(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case NutsConstants.IdProperties.ARCH:{
                        sb.setArch(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case NutsConstants.IdProperties.OS:{
                        sb.setOs(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT:{
                        sb.setDesktopEnvironment(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case /*NutsConstants.IdProperties.PROPERTIES*/"properties":{
                        sb.setProperties(CommaStringParser.parseMap(e.getValue(), session));
                        break;
                    }
                    case NutsConstants.IdProperties.EXCLUSIONS:{
                        b.setExclusions(
                                StringTokenizerUtils.splitDefault(e.getValue())
                                        .stream().map(x->NutsId.of(x,session))
                                        .toArray(NutsId[]::new));
                        break;
                    }
                    default:{
                        props.put(e.getKey(),e.getValue());
                    }
                }
            }
            return b
                    .setCondition(sb)
                    .setProperties(props)
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
