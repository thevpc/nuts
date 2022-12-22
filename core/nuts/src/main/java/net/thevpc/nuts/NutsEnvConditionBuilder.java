package net.thevpc.nuts;

import java.util.List;
import java.util.Map;

public interface NutsEnvConditionBuilder extends NutsEnvCondition {
    NutsEnvConditionBuilder setArch(List<String> arch);

    NutsEnvConditionBuilder setOs(List<String> os);

    NutsEnvConditionBuilder setOsDist(List<String> osDist);

    NutsEnvConditionBuilder setPlatform(List<String> platform);

    NutsEnvConditionBuilder setDesktopEnvironment(List<String> desktopEnvironment);

    NutsEnvConditionBuilder setProfile(List<String> profiles);

    NutsEnvConditionBuilder setAll(NutsEnvCondition other);

    NutsEnvConditionBuilder addAll(NutsEnvCondition other);

    NutsEnvConditionBuilder clear();

    NutsEnvCondition build();
    NutsEnvCondition copy();

    NutsEnvConditionBuilder setProperties(Map<String, String> properties);

    NutsEnvConditionBuilder addProperties(Map<String, String> properties);

    NutsEnvConditionBuilder addProperty(String key, String value);

    NutsEnvConditionBuilder addDesktopEnvironment(String value);

    NutsEnvConditionBuilder addDesktopEnvironments(String... values);

    NutsEnvConditionBuilder addArchs(String value);

    NutsEnvConditionBuilder addArchs(String... values);

    NutsEnvConditionBuilder addOs(String value);

    NutsEnvConditionBuilder addOses(String... values);

    NutsEnvConditionBuilder addOsDist(String value);

    NutsEnvConditionBuilder addOsDists(String... values);

    NutsEnvConditionBuilder addPlatform(String value);

    NutsEnvConditionBuilder addPlatforms(String... values);

    NutsEnvConditionBuilder addProfile(String value);

    NutsEnvConditionBuilder addProfiles(String... values);
}
