/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Properties;

/**
 * @author vpc
 */
public interface NutsRepositoryConfigManager extends NutsEnvProvider {

    String getId();

    String getType();

    String getGroups();

    int getSpeed();

    void setEnv(String property, String value);


    String getLocation();

    String getComponentsLocation();

    void setComponentsLocation(String location);

    String getStoreLocation();

    void removeUser(String userId);

    void setUser(NutsUserConfig user);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

//    NutsRepositoryConfig getConfig();

    void removeMirror(String repositoryId);


    void addMirror(NutsRepositoryLocation c);


    NutsRepositoryLocation getMirror(String id);


    NutsRepositoryLocation[] getMirrors();

    boolean save();

    Properties getEnv(boolean inherit);

    String getEnv(String key, String defaultValue, boolean inherit);
}
