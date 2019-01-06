package net.vpc.app.nuts;

import java.util.Properties;

public interface NutsEnvProvider {
    Properties getEnv();
    String getEnv(String property, String defaultValue);
}
