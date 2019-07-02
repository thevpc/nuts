/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * @author vpc
 * @since 0.5.4
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

    /**
     * @param repositoryIdOrName
     * @param transitive
     * @return
     */
    NutsRepository getMirror(String repositoryIdOrName, boolean transitive);

    NutsRepository getMirror(String repositoryIdOrName);

    NutsRepository findMirror(String repositoryIdOrName, boolean transitive);

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
     * @param options
     * @return
     */
    NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options);

    int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive);

    NutsStoreLocationStrategy getStoreLocationStrategy();
}
