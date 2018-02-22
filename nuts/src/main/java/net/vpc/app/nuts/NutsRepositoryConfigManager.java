/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryConfigManager {

    int getSpeed();

    Properties getEnv(boolean inherit);

    void setEnv(String property, String value);

    String getEnv(String key, String defaultValue, boolean inherit);

    String getLocation();

    File getLocationFolder();

    NutsRepositoryConfig getConfig();
}
