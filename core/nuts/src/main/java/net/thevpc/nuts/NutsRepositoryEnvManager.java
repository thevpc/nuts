package net.thevpc.nuts;

import java.util.Map;

public interface NutsRepositoryEnvManager {
    Map<String, String> toMap(boolean inherit);

    String get(String key, String defaultValue, boolean inherit);

    Map<String, String> toMap();

    String get(String property, String defaultValue);

    void set(String property, String value, NutsUpdateOptions options);

}
