package net.vpc.app.nuts;

import java.util.Properties;

public interface EnvProvider {
    Properties getEnv();
    String getEnv(String property, String defaultValue);
}
