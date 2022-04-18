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
}
