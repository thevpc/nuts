package net.vpc.app.nuts;

import java.util.Properties;

public interface NutsWorkspaceInfoFormat {
    NutsWorkspaceInfoFormat addProperty(String key, String value);

    NutsWorkspaceInfoFormat addProperties(Properties p);

    NutsWorkspaceInfoFormat addOption(String o);

    NutsWorkspaceInfoFormat addOptions(String... o);

    String format();
}
