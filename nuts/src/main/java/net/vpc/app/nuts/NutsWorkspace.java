/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.security.auth.callback.CallbackHandler;

/**
 * Created by vpc on 1/5/17.
 */
@Prototype
public interface NutsWorkspace extends NutsComponent<NutsBootWorkspace> {

    /////////////////////////////////////////////////////////////////
    // CONFIG
    NutsBootWorkspace getBoot();

    NutsId getWorkspaceBootId();

    NutsId getWorkspaceRuntimeId();

    String getWorkspaceLocation();

    String getWorkspaceRootLocation();

    Properties getEnv();
    
    String getEnv(String property, String defaultValue);
    
    void setEnv(String property, String value);

    void addImports(String ... importExpression);

    void removeAllImports();

    void removeImports(String ... importExpression);

    void setImports(String[] imports);

    String[] getImports();

    NutsWorkspaceConfig getConfig();

    Set<String> getAvailableArchetypes();

    NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options);

    /////////////////////////////////////////////////////////////////
    // NUTS RETRIEVAL
    Iterator<NutsId> findIterator(NutsSearch search, NutsSession session);

    List<NutsId> find(NutsSearch search, NutsSession session);

    File copyTo(String id, File localPath, NutsSession session);

    NutsFile fetch(String id, NutsSession session);

    NutsFile[] fetchDependencies(NutsDependencySearch search, NutsSession session);

    NutsFile fetchWithDependencies(String id, NutsSession session);

    NutsDescriptor fetchDescriptor(String id, boolean effective, NutsSession session);

    String fetchHash(String id, NutsSession session);

    String fetchDescriptorHash(String id, NutsSession session);

    boolean isFetched(String id, NutsSession session);

    NutsId resolveId(String id, NutsSession session);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session);

    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    /////////////////////////////////////////////////////////////////
    // NUTS MANAGEMENTS
    NutsFile updateWorkspace(NutsSession session);

    NutsUpdate[] checkWorkspaceUpdates(boolean applyUpdates, String[] args, NutsSession session);

    NutsUpdate checkUpdates(String id, NutsSession session);

    NutsFile update(String id, NutsSession session);

    NutsFile[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsSession session);

    NutsFile install(String id,boolean force, NutsSession session);

    NutsFile checkout(String id, File folder, NutsSession session);

    NutsId commit(File folder, NutsSession session);

    boolean isInstalled(String id, boolean checkDependencies, NutsSession session);

    boolean uninstall(String id, NutsSession session);

    void push(String id, String repoId, NutsSession session);

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile created bundle file or null to create a file with the
     * very same name as the folder
     * @param session current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
    NutsFile createBundle(File contentFolder, File destFile, NutsSession session);

    NutsId deploy(NutsDeployment deployment, NutsSession session);

    /////////////////////////////////////////////////////////////////
    // REPOSITORY MANAGEMENT
    boolean isSupportedRepositoryType(String repositoryType);

    NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate);

    NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate);

    NutsRepository openRepository(String repositoryId, File repositoryRoot, String location, String type, boolean autoCreate);

    NutsRepository findRepository(String repositoryIdPath);

    void removeRepository(String locationOrRepositoryId);

    NutsRepository[] getRepositories();

    NutsRepositoryDefinition[] getDefaultRepositories();

    /////////////////////////////////////////////////////////////////
    // EXEC SUPPORT
    int exec(String[] cmd, Properties env, NutsSession session);

    int exec(String id, String[] args, Properties env, NutsSession session);

    /**
     * exec another instance of nuts
     *
     * @param nutsJarFile
     * @param args
     * @param copyCurrentToFile
     * @param waitFor
     * @param session
     * @return
     * @throws InterruptedException
     */
    int execExternalNuts(File nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session);

    /////////////////////////////////////////////////////////////////
    // EXTENSION MANAGEMENT
    NutsWorkspaceFactory getFactory();

    NutsWorkspaceExtension addExtension(String id, NutsSession session);

    boolean installExtensionComponent(Class extensionPointType, Object extensionImpl);

    NutsWorkspaceExtension[] getExtensions();

    /////////////////////////////////////////////////////////////////
    // SECURITY MANAGEMENT
    String getCurrentLogin();
    
    String[] getCurrentLoginStack();

    void login(String login, String password);

    String login(CallbackHandler handler);

    void logout();

    void setUserCredentials(String login, String password, String oldPassword);

    void addUser(String user, String password, String... rights);

    void setUserRights(String user, String... rights);

    void addUserRights(String user, String... rights);

    void removeUserRights(String user, String... rights);

    void setUserRemoteIdentity(String user, String mappedIdentity);

    void setUserCredentials(String user, String credentials);

    void setUserGroups(String user, String... groups);

    void addUserGroups(String user, String... groups);

    void removeUserGroups(String user, String... groups);
    
    NutsUserInfo[] findUsers();
    
    NutsUserInfo findUser(String username);

    boolean isAllowed(String right);

    boolean switchUnsecureMode(String adminPassword);

    boolean switchSecureMode(String adminPassword);

    boolean isAdmin();

    /////////////////////////////////////////////////////////////////
    // SERVER MANAGEMENT
    NutsServer startServer(ServerConfig serverConfig);

    NutsServer getServer(String serverId);

    void stopServer(String serverId);

    boolean isServerRunning(String serverId);

    List<NutsServer> getServers();

    /////////////////////////////////////////////////////////////////
    // CONFIG MANAGEMENT
    void save();

    Map<String, Object> getSharedObjects();

    /////////////////////////////////////////////////////////////////
    // OBSERVERS
    void addSharedObjectsListener(MapListener<String, Object> listener);

    void removeSharedObjectsListener(MapListener<String, Object> listener);

    MapListener<String, Object>[] getSharedObjectsListeners();

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    void removeRepositoryListener(NutsRepositoryListener listener);

    void addRepositoryListener(NutsRepositoryListener listener);

    NutsRepositoryListener[] getRepositoryListeners();

    /////////////////////////////////////////////////////////////////
    // RUNTIME INFO
    NutsFile fetchBoot(NutsSession session);

    File resolveNutsJarFile();

    Map<String, String> getRuntimeProperties();

    /////////////////////////////////////////////////////////////////
    // UTILITIES
    NutsSession createSession();

    ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session);

    NutsConsole createConsole(NutsSession session);

    NutsTerminal createTerminal();

    NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err);

    NutsPrintStream createEnhancedPrintStream(OutputStream out);

    File getCwd();

    void setCwd(File file);

    String getHelpString();

    NutsId parseNutsId(String nutsId);

}
