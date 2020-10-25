package net.vpc.app.nuts;

import java.util.Map;

public interface NutsWorkspaceEnvManager {
    Map<String, String> toMap();

    String get(String property, String defaultValue);
    String get(String property);
    void set(String property, String value, NutsUpdateOptions options);

    NutsOsFamily getOsFamily();

    NutsId getPlatform();

    NutsId getOs();

    NutsId getOsDist();

    NutsId getArch();

}
