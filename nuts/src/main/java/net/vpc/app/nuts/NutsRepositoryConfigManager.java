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

    String uuid();

    /**
     * name is the name attributed by the containing workspace. It is defined in
     * NutsRepositoryRef
     *
     * @return local name
     */
    String getName();

    String name();

    /**
     * global name is independent from workspace
     *
     * @return
     */
    String getGlobalName();

    String getType();

    String getGroups();

    int getSpeed();

    int getSpeed(boolean transitive);

    void setEnv(String property, String value);

    boolean isTemporary();

    boolean isIndexSubscribed();

    /**
     * return repository configured location as string
     *
     * @param expand when true, location will be expanded (~ and $ params will
     * be expanded)
     * @return repository location
     */
    String getLocation(boolean expand);

    Path getStoreLocation();

    Path getStoreLocation(NutsStoreLocation folderType);

////    NutsRepositoryConfig getConfig();
//    NutsRepositoryConfigManager removeMirrorRef(String repositoryId);
//
//    NutsRepositoryConfigManager addMirrorRef(NutsRepositoryRef c);
//
//    NutsRepositoryRef getMirrorRef(String id);
//
//    NutsRepositoryRef[] getMirrorRefs();
    boolean save(boolean force);

    void save();

    Properties getEnv(boolean inherit);

    String getEnv(String key, String defaultValue, boolean inherit);

    NutsRepositoryConfigManager setIndexEnabled(boolean enabled);

    boolean isIndexEnabled();

    NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled);

    public int getDeployOrder();

    NutsRepositoryConfigManager setEnabled(boolean enabled);

    NutsRepositoryConfigManager setTemporary(boolean enabled);

    boolean isEnabled();

    boolean subscribeIndex();

    NutsRepositoryConfigManager unsubscribeIndex();

    boolean isSupportedMirroring();

    NutsRepository[] getMirrors();

    boolean containsMirror(String repositoryIdPath);

    /**
     * @param repositoryIdPath
     * @return
     */
    NutsRepository getMirror(String repositoryIdPath);

    NutsRepository findMirror(String repositoryIdPath);

    /**
     *
     * @param definition
     * @return
     */
    NutsRepository addMirror(NutsRepositoryDefinition definition);

    /**
     * @param options
     * @return
     */
    NutsRepository addMirror(NutsCreateRepositoryOptions options);

    /**
     * @param repositoryId
     * @return
     */
    NutsRepositoryConfigManager removeMirror(String repositoryId);

    int getFindSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive);

    NutsRepositoryConfigManager removeUser(String userId);

    NutsRepositoryConfigManager setUser(NutsUserConfig user);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    NutsStoreLocationStrategy getStoreLocationStrategy();
}
