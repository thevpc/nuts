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

import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author vpc
 */
public interface NutsWorkspaceConfigManager extends EnvProvider {

    NutsBootWorkspace getBoot();

    /**
     * Actual (Runtime) Boot API Id
     * @return Actual (Runtime) Boot API Id
     */
    NutsId getBootAPI();

    /**
     * Actual (Runtime) Boot Runtime Id
     * @return Actual (Runtime) Boot Runtime Id
     */
    NutsId getBootRuntime();

    /**
     * Configured Boot API Id
     * @return Configured Boot API Id
     */
    NutsId getWorkspaceBootAPI();

    /**
     * Configured Boot Runtime Id
     * @return Configured Boot Runtime Id
     */
    NutsId getWorkspaceBootRuntime();

    String getWorkspaceLocation();

    /**
     * nuts root folder. It defaults to "~/.nuts"
     *
     * @return nuts root folder
     */
    String getHomeLocation();

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

    boolean updateExtension(NutsId extensionId);

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

    boolean addSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdkByName(String name, String locationName);

    NutsSdkLocation findSdkByPath(String name, String path);

    NutsSdkLocation findSdkByVersion(String name, String version);

    NutsSdkLocation removeSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdk(String name, NutsSdkLocation location);

    NutsBootConfig getBootConfig();
    NutsBootConfig getWorkspaceBootConfig();

    void setBootConfig(NutsBootConfig other);

    String[] getSdkTypes();

    NutsSdkLocation getSdk(String type, String requestedVersion);

    NutsSdkLocation[] getSdks(String type);

    void setLogLevel(Level levek);

    NutsSdkLocation[] searchJdkLocations(PrintStream out);

    NutsSdkLocation[] searchJdkLocations(String path, PrintStream out);

    NutsSdkLocation resolveJdkLocation(String path);

    byte[] decryptString(byte[] input);

    byte[] encryptString(byte[] input);
}

