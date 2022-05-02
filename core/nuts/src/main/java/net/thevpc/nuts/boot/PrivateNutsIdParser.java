package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class PrivateNutsIdParser {
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
            return NutsOptional.of(NutsId.BLANK);
        }
        Matcher m = NutsId.PATTERN.matcher(nutsId);
        if (m.find()) {
            NutsIdBuilder idBuilder = NutsIdBuilder.of();
            String group = m.group("group");
            String artifact = m.group("artifact");
            idBuilder.setArtifactId(artifact);
            idBuilder.setVersion(m.group("version"));
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            idBuilder.setArtifactId(artifact);
            idBuilder.setGroupId(group);

            Map<String, String> queryMap = NutsUtilStrings.parseDefaultMap(m.group("query")).get();
            NutsEnvConditionBuilder conditionBuilder = new DefaultNutsEnvConditionBuilder();

            Map<String, String> idProperties = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, String>> iterator = queryMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> e = iterator.next();
                String key = e.getKey();
                String value = e.getValue();
                setProperty(key, value, idBuilder, conditionBuilder, idProperties);
            }

            return NutsOptional.of(idBuilder.setCondition(conditionBuilder)
                    .setProperties(idProperties).build());
        }
        return NutsOptional.ofError(session -> NutsMessage.cstyle("invalid id format : %s", nutsId));
    }

    private static void setProperty(String key, String value, NutsIdBuilder builder, NutsEnvConditionBuilder sb, Map<String, String> props) {
        switch (key) {
            case NutsConstants.IdProperties.CLASSIFIER: {
                builder.setClassifier(value);
                break;
            }
            case NutsConstants.IdProperties.PROFILE: {
                sb.setProfile(PrivateNutsUtilStrings.splitDefault(value));
                break;
            }
            case NutsConstants.IdProperties.PLATFORM: {
                sb.setPlatform(NutsUtilStrings.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.OS_DIST: {
                sb.setOsDist(NutsUtilStrings.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.ARCH: {
                sb.setArch(NutsUtilStrings.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.OS: {
                sb.setOs(NutsUtilStrings.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.DESKTOP: {
                sb.setDesktopEnvironment(NutsUtilStrings.parsePropertyIdList(value).get());
                break;
            }
            case NutsConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                Map<String, String> mm = NutsUtilStrings.parseMap(value, "=", ",").get();
                sb.setProperties(mm);
                break;
            }
            default: {
                props.put(key, value);
            }
        }
    }
}
