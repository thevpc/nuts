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

import net.vpc.app.nuts.util.MapListener;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 */
@Prototype
public interface NutsWorkspace extends NutsComponent<Object> {

    String getWorkspaceVersion();

    String getWorkspaceLocation();

    NutsWorkspaceConfig getConfig();

    NutsServer getServer(String serverId);

    void stopServer(String serverId) throws IOException;

    boolean isRunningServer(String serverId);

    List<NutsServer> getServers();

    Set<String> getAvailableArchetypes();

    boolean initializeWorkspace(String workspace, NutsWorkspace bootstrapWorkspace, ClassLoader workspaceClassLoader, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException;

    NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException;

    NutsFile[] fetchNutsIdWithDependencies(NutsSession session);

    NutsFile fetchNutsId(NutsSession session) throws IOException;

    /////////////////////////////////////////////////////////////////
    // NUTS MANAGEMENTS
    NutsFile updateWorkspace(NutsSession session) throws IOException;

    NutsUpdate[] checkWorkspaceUpdates(NutsSession session) throws IOException;

    NutsUpdate checkUpdates(String id, NutsSession session) throws IOException;

    NutsFile update(String id, NutsSession session) throws IOException;

    List<NutsFile> update(Set<String> toUpdateIds, Set<String> toRetainDependencies, NutsSession session) throws IOException;

    NutsFile fetch(String id, NutsSession session) throws IOException;

    boolean isFetched(String id, NutsSession session) throws IOException;

    NutsFile install(String id, NutsSession session) throws IOException;

    NutsFile checkout(String id, File folder, NutsSession session) throws IOException;

    NutsId commit(File folder, NutsSession session) throws IOException;

    boolean isInstalled(String id, boolean checkDependencies, NutsSession session) throws IOException;

    boolean uninstall(String id, NutsSession session) throws IOException;

    void push(String id, String repoId, NutsSession session) throws IOException;

    /**
     * creates a zip file based on the folder.
     * The folder should contain a descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile      created bundle file or null to create a file with the very same name as the folder
     * @param session       current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     * @throws IOException on I/O error
     */
    NutsFile createBundle(File contentFolder, File destFile, NutsSession session) throws IOException;

    /**
     * @param contentInputStream content stream to be deployed
     * @param sha1               if available, stream hash will be evaluated and compared
     *                           against this SHA1 hash
     * @param descriptor         stream descriptor, if null, default descriptor will be
     *                           looked for
     * @param repositoryId
     * @return
     * @throws IOException
     */
    NutsId deploy(InputStream contentInputStream, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException;

    NutsId deploy(File contentFile, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException;

    NutsId deploy(File contentFile, String contentFileSHA1, File descriptor, String descSHA1, String repositoryId, NutsSession session) throws IOException;

    NutsId deploy(String contentFile, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException;

    NutsId deploy(String contentURL, String sha1, String descriptorURL, String descSHA1, String repositoryId, NutsSession session) throws IOException;

    NutsId resolveId(String id, NutsSession session) throws IOException;

    Iterator<NutsId> findIterator(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) throws IOException;

    List<NutsId> find(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) throws IOException;

    /**
     * finds all Ids for the group and name (aka finds all versions)
     *
     * @param id
     * @param versionFilter
     * @param repositoryFilter
     * @param session
     * @return
     * @throws IOException
     */
    Iterator<NutsId> findVersions(String id, NutsVersionFilter versionFilter, NutsRepositoryFilter repositoryFilter, NutsSession session) throws IOException;

    NutsFile fetch(String id, boolean dependencies, NutsSession session) throws IOException;

    List<NutsFile> fetchWithDependencies(String id, boolean includeMain, NutsDependencyFilter dependencyFilter, NutsSession session) throws IOException;

    File fetch(String id, File localPath, NutsSession session) throws IOException;

    NutsDescriptor fetchDescriptor(String id, boolean effective, NutsSession session) throws IOException;

    String fetchHash(String id, NutsSession session) throws IOException;

    String fetchDescriptorHash(String id, NutsSession session) throws IOException;

    NutsId fetchEffectiveId(NutsId id, NutsSession session) throws IOException;

    NutsId fetchEffectiveId(NutsDescriptor descriptor, NutsSession session) throws IOException;

    NutsDescriptor fetchEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) throws IOException;

    /////////////////////////////////////////////////////////////////
    // REPOSITORY MANAGEMENT
    boolean isSupportedRepositoryType(String repositoryType) throws IOException;

    NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException;

    NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException;

    NutsRepository openRepository(String repositoryId, File repositoryRoot, String location, String type, boolean autoCreate) throws IOException;

    NutsRepository findRepository(String repositoryIdPath) throws IOException;

    void removeRepository(String locationOrRepositoryId) throws IOException;

    NutsRepository[] getRepositories();

    /////////////////////////////////////////////////////////////////
    // EXEC SUPPORT
    void exec(String[] cmd, NutsSession session) throws IOException, InterruptedException;

    void exec(String id, String[] args, NutsSession session) throws IOException, InterruptedException;

    /////////////////////////////////////////////////////////////////
    // EXTENSION MANAGEMENT
    NutsWorkspaceFactory getFactory();

    NutsWorkspaceExtension addExtension(String id, NutsSession session) throws IOException;

    boolean installExtensionComponent(Class extensionPointType, Object extensionImpl) throws IOException;

    NutsWorkspaceExtension[] getExtensions() throws IOException;

    /////////////////////////////////////////////////////////////////
    // CONFIG MANAGEMENT
    String getCurrentLogin();

    void login(String login, String password) throws LoginException;

    String login(CallbackHandler handler) throws LoginException;

    void logout() throws LoginException;

    void setUserCredentials(String login, String password, String oldPassword) throws IOException;

    void addUser(String user);

    void setUserCredentials(String user, String credentials) throws IOException;

    boolean isAllowed(String right);

    /////////////////////////////////////////////////////////////////
    // SERVER MANAGEMENT
    NutsServer startServer(ServerConfig serverConfig) throws IOException;

    /////////////////////////////////////////////////////////////////
    // CONFIG MANAGEMENT
    void save() throws IOException;

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

    NutsCommandLineConsoleComponent createCommandLineConsole(String[] bootstrapArguments, NutsSession session) throws IOException;

    NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException;

    NutsPrintStream createEnhancedPrintStream(OutputStream out) throws IOException;


}
