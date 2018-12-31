package net.vpc.app.nuts;

import java.util.Properties;

public interface NutsWorkspaceVersionFormat {
    NutsWorkspaceVersionFormat addProperty(String key, String value);

    NutsWorkspaceVersionFormat addProperties(Properties p);

    NutsWorkspaceVersionFormat addOption(String o);

    String format();
}
