package net.thevpc.nuts;

import java.util.List;
import java.util.Map;

public interface NEnvConditionBuilder extends NEnvCondition {
    NEnvConditionBuilder setArch(List<String> arch);

    NEnvConditionBuilder setOs(List<String> os);

    NEnvConditionBuilder setOsDist(List<String> osDist);

    NEnvConditionBuilder setPlatform(List<String> platform);

    NEnvConditionBuilder setDesktopEnvironment(List<String> desktopEnvironment);

    NEnvConditionBuilder setProfile(List<String> profiles);

    NEnvConditionBuilder setAll(NEnvCondition other);

    NEnvConditionBuilder addAll(NEnvCondition other);

    NEnvConditionBuilder clear();

    NEnvCondition build();
    NEnvCondition copy();

    NEnvConditionBuilder setProperties(Map<String, String> properties);

    NEnvConditionBuilder addProperties(Map<String, String> properties);

    NEnvConditionBuilder addProperty(String key, String value);

    NEnvConditionBuilder addDesktopEnvironment(String value);

    NEnvConditionBuilder addDesktopEnvironments(String... values);

    NEnvConditionBuilder addArchs(String value);

    NEnvConditionBuilder addArchs(String... values);

    NEnvConditionBuilder addOs(String value);

    NEnvConditionBuilder addOses(String... values);

    NEnvConditionBuilder addOsDist(String value);

    NEnvConditionBuilder addOsDists(String... values);

    NEnvConditionBuilder addPlatform(String value);

    NEnvConditionBuilder addPlatforms(String... values);

    NEnvConditionBuilder addProfile(String value);

    NEnvConditionBuilder addProfiles(String... values);
}
