/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
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
     * return repository configured location as string
     *
     * @param expand when true, location will be expanded (~ and $ params will be expanded)
     * @return repository location
     */
    String getLocation(boolean expand);

    Path getRepositoryLocation();

    Path getStoreLocation();

    Path getStoreLocation(NutsStoreLocation folderType);

    void removeUser(String userId);

    void setUser(NutsUserConfig user);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

//    NutsRepositoryConfig getConfig();

    void removeMirror(String repositoryId);


    void addMirror(NutsRepositoryRef c);

    NutsRepositoryRef getMirror(String id);


    NutsRepositoryRef[] getMirrors();

    boolean save(boolean force);

    void save();

    Properties getEnv(boolean inherit);

    String getEnv(String key, String defaultValue, boolean inherit);

    void setIndexEnabled(boolean enabled);

    boolean isIndexEnabled();

    void setMirrorEnabled(String repoName, boolean enabled);

    public int getDeployOrder();

}
