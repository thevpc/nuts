/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * @author vpc
 */
public interface NutsWorkspaceConfigManager extends EnvProvider{

    NutsBootWorkspace getBoot();

    NutsId getWorkspaceBootId();

    NutsId getWorkspaceRuntimeId();

    String getWorkspaceLocation();

    /**
     * nuts root folder. It defaults to "~/.nuts"
     *
     * @return nuts root folder
     */
    String getNutsHomeLocation();

    Properties getEnv();

    void setEnv(String property, String value);

    Map<String, String> getRuntimeProperties();

    String getCwd();

    void setCwd(String file);

    String resolveNutsJarFile();

    void addImports(String... importExpression);

    void removeAllImports();

    void removeImports(String... importExpression);

    void setImports(String[] imports);

    String[] getImports();

    NutsId[] getExtensions();

    void setRepositoryEnabled(String repoId, boolean enabled);

    boolean isRepositoryEnabled(String repoId);

    boolean addExtension(NutsId extensionId);

    boolean removeExtension(NutsId extensionId);

    Map<String, Object> getSharedObjects();

    void addSharedObjectsListener(MapListener<String, Object> listener);

    void removeSharedObjectsListener(MapListener<String, Object> listener);

    MapListener<String, Object>[] getSharedObjectsListeners();

    void save();

    URL[] getBootClassWorldURLs();

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    void setUser(NutsUserConfig config);

    boolean isSecure();

    void setSecure(boolean secure);


    void addRepository(NutsRepositoryLocation repository);


    void removeRepository(String repositoryId);

    NutsRepositoryLocation getRepository(String repositoryId);

    boolean containsExtension(NutsId extensionId);

    void removeUser(String userId);

    void setUsers(NutsUserConfig[] users);

    String getComponentsLocation();
}

