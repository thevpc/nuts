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
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vpc on 1/5/17.
 */
@Prototype
public interface NutsWorkspace extends NutsComponent<NutsBootWorkspace> {

    NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options);

    Iterator<NutsId> findIterator(NutsSearch search, NutsSession session);

    NutsId findFirst(NutsSearch search, NutsSession session);

    NutsId findOne(NutsSearch search, NutsSession session);

    List<NutsId> find(NutsSearch search, NutsSession session);

    String copyTo(String id, String localPath, NutsSession session);

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

    NutsFile updateWorkspace(String nutsVersion, NutsConfirmAction foundAction, NutsSession session);

    NutsUpdate[] checkWorkspaceUpdates(boolean applyUpdates, String[] args, NutsSession session);

    NutsUpdate checkUpdates(String id, NutsSession session);

    NutsFile update(String id, NutsConfirmAction uptoDateAction, NutsSession session);

    NutsFile[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsConfirmAction foundAction, NutsSession session);

    NutsFile install(String id, NutsConfirmAction foundAction, NutsSession session);

    NutsFile checkout(String id, String folder, NutsSession session);

    NutsId commit(String folder, NutsSession session);

    boolean isInstalled(String id, boolean checkDependencies, NutsSession session);

    boolean uninstall(String id, boolean deleteData, NutsSession session);

    void push(String id, String repoId, NutsConfirmAction foundAction, NutsSession session);

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile      created bundle file or null to create a file with the
     *                      very same name as the folder
     * @param session       current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
    NutsFile createBundle(String contentFolder, String destFile, NutsSession session);

    NutsId deploy(NutsDeployment deployment, NutsSession session);

    NutsCommandExecBuilder createExecBuilder();

    int exec(String[] cmd, Properties env, String dir, NutsSession session);

    /**
     * exec another instance of nuts
     *
     * @param nutsJarFile
     * @param args
     * @param copyCurrentToFile
     * @param waitFor
     * @param session
     * @return
     */
    int exec(String nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session);

    NutsWorkspaceRepositoryManager getRepositoryManager();

    Properties getProperties();

    NutsSession createSession();

    NutsWorkspaceExtensionManager getExtensionManager();

    NutsWorkspaceConfigManager getConfigManager();

    NutsWorkspaceSecurityManager getSecurityManager();

    String getStoreRoot(RootFolderType folderType);

    String getStoreRoot(NutsId id, RootFolderType folderType);

    String getStoreRoot(String id, RootFolderType folderType);

    NutsFile fetchBootFile(NutsSession session);

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    NutsId getBootId();

    NutsId getRuntimeId();

    NutsId resolveNutsIdForClass(Class clazz);

    NutsId[] resolveNutsIdsForClass(Class clazz);

    NutsId createNutsId(String id);

    NutsId createNutsId(String namespace, String group, String name, String version, String query);

    NutsId createNutsId(String namespace, String group, String name, String version, Map<String, String> query);

    NutsId createNutsId(String groupId, String name, String version);

    NutsId getPlatformOs();

    NutsId getPlatformOsDist();

    NutsId getPlatformOsLib();

    NutsId getPlatformArch();

    ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session);

    ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session);

    String resolvePath(String path);

    String resolveRepositoryPath(String location);

    NutsWorkspaceCreateOptions getOptions();

    NutsBootOptions getBootOptions();

    NutsDescriptorBuilder createDescriptorBuilder();

    NutsIdBuilder createIdBuilder();

    //    public NutsVesionBuilder createNutsVersionBuilder() {
//        return new DefaultVersionBuilder();
//    }
    NutsDescriptor parseDescriptor(URL url);

    NutsDescriptor parseDescriptor(File file);

    NutsDescriptor parseDescriptor(InputStream stream);

    NutsDescriptor parseDescriptor(String descriptorString);

    NutsDependency parseDependency(String dependency);

    NutsVersion createVersion(String version);

    NutsId parseOrErrorNutsId(String nutFormat);

    NutsSearchBuilder createSearchBuilder();

    String getNutsFileName(NutsId id, String ext);

    String filterText(String value);

    String escapeText(String str);

    String resolveJavaMainClass(File file);

    NutsFormattedPrintStream createsFormattedPrintStream(PrintStream out);

    NutsTerminal createTerminal();

    String simpexpToRegexp(String pattern, boolean contains);

    String getResourceString(String resource, Class cls, String defaultValue);

    void reindex(String path);

    void reindexAll();

    void downloadPath(String from, File to, NutsSession session);

    String evalContentHash(InputStream input);

}
