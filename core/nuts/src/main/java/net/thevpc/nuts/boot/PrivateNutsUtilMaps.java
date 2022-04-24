package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsEnvCondition;
import net.thevpc.nuts.NutsUtilStrings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PrivateNutsUtilMaps {
    public static Map<String, String> toMap(NutsEnvCondition condition) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;
        if (condition.getArch() != null) {
            s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.ARCH, s);
            }
        }
        if (condition.getOs() != null) {
            s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS, s);
            }
        }
        if (condition.getOsDist() != null) {
            s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.OS_DIST, s);
            }
        }
        if (condition.getPlatform() != null) {
            s = PrivateNutsIdListParser.formatStringIdList(condition.getPlatform());
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PLATFORM, s);
            }
        }
        if (condition.getDesktopEnvironment() != null) {
            s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.DESKTOP, s);
            }
        }
        if (condition.getProfile() != null) {
            s = condition.getProfile().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NutsBlankable.isBlank(s)) {
                m.put(NutsConstants.IdProperties.PROFILE, s);
            }
        }
        if (condition.getProperties() != null) {
            Map<String, String> properties = condition.getProperties();
            if (!properties.isEmpty()) {
                m.put(NutsConstants.IdProperties.CONDITIONAL_PROPERTIES, NutsUtilStrings.formatDefaultMap(properties));
            }
        }
        return m;
    }
}
