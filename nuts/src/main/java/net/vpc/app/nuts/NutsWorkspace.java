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
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vpc on 1/5/17.
 */
@Prototype
public interface NutsWorkspace extends NutsComponent<Object> {

    NutsWorkspace openWorkspace(NutsWorkspaceOptions options);

    String copyTo(String id, String localPath, NutsSession session);

    NutsDefinition fetchWithDependencies(NutsId id, NutsSession session);

    NutsDefinition fetch(String id, NutsSession session);

    NutsDefinition fetchWithDependencies(String id, NutsSession session);

    NutsDescriptor fetchDescriptor(String id, boolean effective, NutsSession session);

    NutsDescriptor fetchDescriptor(NutsId idString, boolean effective, NutsSession session);

    String fetchHash(String id, NutsSession session);

    String fetchDescriptorHash(String id, NutsSession session);

    boolean isFetched(String id, NutsSession session);

    NutsDefinition fetch(NutsId id, NutsSession session);

    NutsId resolveId(NutsId id, NutsSession session);

    NutsId resolveId(String id, NutsSession session);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session);

    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsDefinition checkout(NutsId id, String folder, NutsSession session);

    NutsUpdate[] checkWorkspaceUpdates(NutsWorkspaceUpdateOptions options, NutsSession session);

    NutsDefinition update(String id, NutsConfirmAction uptoDateAction, NutsSession session);

    NutsDefinition[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsConfirmAction foundAction, NutsSession session);

    NutsDefinition install(String id, String[] args, NutsConfirmAction foundAction, NutsSession session);

    NutsDefinition checkout(String id, String folder, NutsSession session);

    NutsDefinition install(NutsId id, String[] args, NutsConfirmAction foundAction, NutsSession session);

    NutsId commit(String folder, NutsSession session);

    NutsDefinition update(NutsId id, NutsConfirmAction uptoDateAction, NutsSession session);

    boolean isInstalled(String id, boolean checkDependencies, NutsSession session);

    boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session);

    boolean uninstall(String id, String[] args, NutsConfirmAction notFoundAction, boolean deleteData, NutsSession session);

    void push(String id, String repoId, NutsConfirmAction foundAction, NutsSession session);

    void push(NutsId id, String repositoryId, NutsConfirmAction foundAction, NutsSession session);

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
    NutsDefinition createBundle(String contentFolder, String destFile, NutsSession session);

    NutsId deploy(NutsDeployment deployment, NutsSession session);

    NutsWorkspaceRepositoryManager getRepositoryManager();

    Map<String, Object> getUserProperties();

    NutsTerminal getTerminal();

    void setTerminal(NutsTerminal newTerminal);

    NutsSession createSession();

    NutsWorkspaceExtensionManager getExtensionManager();

    NutsWorkspaceConfigManager getConfigManager();

    NutsWorkspaceSecurityManager getSecurityManager();

    String getStoreRoot(RootFolderType folderType);

    String getStoreRoot(NutsId id, RootFolderType folderType);

    void addUserPropertyListener(MapListener<String, Object> listener);

    void removeUserPropertyListener(MapListener<String, Object> listener);

    MapListener<String, Object>[] getUserPropertyListeners();

    boolean isFetched(NutsId id, NutsSession session);

    String getStoreRoot(String id, RootFolderType folderType);

    void installCompanionTools(boolean silent, NutsSession session);

    NutsDefinition fetchBootFile(NutsSession session);

    JsonIO getJsonIO();

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    String resolveDefaultHelpForClass(Class clazz);

    NutsId resolveIdForClass(Class clazz);

    NutsId[] resolveIdsForClass(Class clazz);

    NutsId getPlatformOs();

    NutsId getPlatformOsDist();

    String getPlatformOsLibPath();

    NutsId getPlatformArch();

    ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session);

    ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session);

    String resolvePath(String path);

    String resolveRepositoryPath(String location);

    NutsDescriptorBuilder createDescriptorBuilder();

    NutsIdBuilder createIdBuilder();

    NutsQuery createQuery();

    boolean uninstall(NutsId id, String[] args, NutsConfirmAction notFoundAction, boolean deleteData, NutsSession session);

    NutsCommandExecBuilder createExecBuilder();

    NutsId parseId(String id);

    NutsDescriptor parseDescriptor(URL url);

    NutsDescriptor parseDescriptor(File file);

    NutsDescriptor parseDescriptor(InputStream stream);

    NutsDescriptor parseDescriptor(String descriptorString);

    NutsDependency parseDependency(String dependency);

    NutsVersion parseVersion(String version);

    NutsId parseRequiredId(String nutFormat);


    String getFileName(NutsId id, String ext);



    /**
     * resolveExecutionEntries
     *
     * @param file
     * @return
     */
    ExecutionEntry[] resolveExecutionEntries(File file);

    ExecutionEntry[] resolveExecutionEntries(InputStream inputStream, String type);

    String createRegex(String pattern, boolean contains);

    String getResourceString(String resource, Class cls, String defaultValue);

    void updateRepositoryIndex(String path);

    void updateAllRepositoryIndices();

    void downloadPath(String from, File to, NutsSession session);

    String evalContentHash(InputStream input);

    void printVersion(PrintStream out, Properties extraProperties, String options);

    void printHelp(PrintStream out);

    void printLicense(PrintStream out);


    NutsTerminal createDefaultTerminal(InputStream in, PrintStream out, PrintStream err);

    NutsTerminal createTerminal();

    NutsTerminal createTerminal(NutsTerminal delegated, InputStream in, PrintStream out, PrintStream err);

    NutsTerminal createTerminal(InputStream in, PrintStream out, PrintStream err);

    PrintStream createPrintStream(OutputStream out, boolean inputFormatted);

    PrintStream createPrintStream(OutputStream out, boolean inputFormatted, boolean forceNoColors);

    PrintStream createPrintStream(File out);

    InputStream createNullInputStream();

    PrintStream createNullPrintStream();


    InputStream monitorInputStream(String path, String name, NutsSession session);

    InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session);

    boolean isStandardOutputStream(OutputStream out);

    boolean isStandardErrorStream(OutputStream out);

    boolean isStandardInputStream(InputStream in);

    /**
     * this method removes all  {@link NutsFormattedPrintStream}'s special formatting sequences and returns the raw
     * string to be printed on an ordinary {@link PrintStream}
     *
     * @param value input string
     * @return string without any escape sequences so that the text printed correctly on any non formatted {@link PrintStream}
     */
    String filterText(String value);

    /**
     * This method escapes all special characters that are interpreted by {@link NutsFormattedPrintStream} so that
     * this exact string is printed on such print streams
     * When str is null, an empty string is return
     *
     * @param value input string
     * @return string with escaped characters so that the text printed correctly on {@link NutsFormattedPrintStream}
     */
    String escapeText(String value);

    String formatId(NutsId id);

    String formatDependency(NutsDependency id);
}

