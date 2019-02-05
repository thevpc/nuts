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

    String getUuid();

    String getName();

    String getType();

    String getGroups();

    int getSpeed();

    void setEnv(String property, String value);


    /**
     * return repository location
     *
     * @param expand when true, location will be expanded (~ and $ params will be expanded)
     * @return repository location
     */
    String getLocation(boolean expand);

    /**
     * equivalent to {{@link #getLocation(boolean)}} with false value
     *
     * @return repository location
     */
    String getLocation();

    String getStoreLocation();

    String getStoreLocation(NutsStoreFolder folderType);

    void removeUser(String userId);

    void setUser(NutsUserConfig user);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

//    NutsRepositoryConfig getConfig();

    void removeMirror(String repositoryId);


    void addMirror(NutsRepositoryLocation c);

    NutsRepositoryLocation getMirror(String id);


    NutsRepositoryLocation[] getMirrors();

    boolean save(boolean force);

    void save();

    Properties getEnv(boolean inherit);

    String getEnv(String key, String defaultValue, boolean inherit);
}
