package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class PrivateNutsIdParser {
    private static final PrivateNutsStringMapParser QPARSER = new PrivateNutsStringMapParser("=", "&");

    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId nutsId
     * @return nutsId
     */
    public static NutsOptional<NutsId> parse(String nutsId) {
        if (NutsBlankable.isBlank(nutsId)) {
            return NutsOptional.ofBlank(NutsId.BLANK, s->NutsMessage.plain("blank id"));
        }
        Matcher m = NutsId.PATTERN.matcher(nutsId);
        if (m.find()) {
            NutsIdBuilder builder = new DefaultNutsIdBuilder();
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

            Map<String, String> queryMap = QPARSER.parseMap(m.group("query"));
            NutsEnvConditionBuilder sb = new DefaultNutsEnvConditionBuilder();

            Map<String, String> props = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                switch (e.getKey()) {
                    case NutsConstants.IdProperties.CLASSIFIER: {
                        builder.setClassifier(e.getValue());
                        break;
                    }
                    case NutsConstants.IdProperties.PROFILE: {
                        sb.setProfile(PrivateNutsUtilStrings.splitDefault(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.PLATFORM: {
                        sb.setPlatform(PrivateNutsIdListParser.parseStringIdList(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.OS_DIST: {
                        sb.setOsDist(PrivateNutsUtilStrings.splitDefault(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.ARCH: {
                        sb.setArch(PrivateNutsUtilStrings.splitDefault(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.OS: {
                        sb.setOs(PrivateNutsUtilStrings.splitDefault(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT: {
                        sb.setDesktopEnvironment(PrivateNutsUtilStrings.splitDefault(e.getValue()));
                        break;
                    }
                    case NutsConstants.IdProperties.PROPERTIES: {
                        Map<String, String> mm = PrivateNutsCommaStringParser.parseMap(e.getValue());
                        sb.setProperties(mm);
                        break;
                    }
                    default: {
                        props.put(e.getKey(), e.getValue());
                    }
                }
            }

            return NutsOptional.of(builder.setCondition(sb)
                    .setProperties(props).build());
        }
        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid id format : %s", nutsId));
    }
}
