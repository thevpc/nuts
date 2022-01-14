package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsEnvConditionBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.expr.CommaStringParser;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringMapParser;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultNutsIdParser implements NutsIdParser {
    //(([a-zA-Z0-9_${}*-]+|<main>)://)?
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    private static final StringMapParser QPARSER = new StringMapParser("=", "&");
    private NutsSession session;
    private boolean lenient = false;
    private boolean acceptBlank = true;

    public DefaultNutsIdParser(NutsSession session) {
        this.session = session;
    }

    public boolean isAcceptBlank() {
        return acceptBlank;
    }

    public NutsIdParser setAcceptBlank(boolean acceptBlank) {
        this.acceptBlank = acceptBlank;
        return this;
    }

    @Override
    public NutsIdParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }
    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId nutsId
     * @return nutsId
     */
    @Override
    public NutsId parse(String nutsId) {
        if(NutsBlankable.isBlank(nutsId)){
            if(isAcceptBlank()){
                return null;
            }
            throw new NutsParseException(session, NutsMessage.plain("blank id"));
        }
        Matcher m = NUTS_ID_PATTERN.matcher(nutsId);
        if (m.find()) {
            NutsIdBuilder builder = NutsIdBuilder.of(session);
            String group = m.group("group");
            String artifact = m.group("artifact");
            builder.setArtifactId(artifact);
            builder.setVersion(m.group("version"));
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            builder.setArtifactId(artifact);
            builder.setGroupId(group);

            Map<String, String> queryMap = QPARSER.parseMap(m.group("query"),session);
            DefaultNutsEnvConditionBuilder sb=(DefaultNutsEnvConditionBuilder) NutsEnvConditionBuilder.of(session);

            Map<String, String> props=new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                switch (e.getKey()){
                    case NutsConstants.IdProperties.CLASSIFIER:{
                        builder.setClassifier(e.getValue());
                        break;
                    }
                    case NutsConstants.IdProperties.PROFILE:{
                        sb.setProfile(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
                        break;
                    }
                    case NutsConstants.IdProperties.PLATFORM:{
                        sb.setPlatform(StringTokenizerUtils.splitDefault(e.getValue()).toArray(new String[0]));
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
                        Map<String, String> mm = CommaStringParser.parseMap(e.getValue(), session);
                        sb.setProperties(mm);
                        break;
                    }
                    default:{
                        props.put(e.getKey(),e.getValue());
                    }
                }
            }

            return builder.setCondition(sb)
                    .setProperties(props).build();
        }
        if (!isLenient()) {
            throw new NutsParseException(session, NutsMessage.cstyle("invalid id format : %s", nutsId));
        }
        return null;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
