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

import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
@NutsPrototype
public interface NutsWorkspace extends NutsComponent<Object> {

    String getUuid();

    /**
     * Open a new workspace with the provided options. When options is null, it
     * i considered as a new empty instance of the options class.
     *
     * @param options creation options
     * @return a new instance of workspace
     */
    NutsWorkspace openWorkspace(NutsWorkspaceOptions options);

    String copyTo(String id, String localPath, NutsSession session);

    NutsFetch fetch(NutsId id);

    NutsFetch fetch(String id);

    boolean isFetched(String id, NutsSession session);

    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsQueryOptions options, NutsSession session);

    NutsDefinition checkout(NutsId id, String folder, NutsSession session);

    NutsUpdate[] checkWorkspaceUpdates(NutsWorkspaceUpdateOptions options, NutsSession session);

    NutsUpdateResult update(String id, NutsUpdateOptions options, NutsSession session);

    NutsUpdateResult[] update(String[] toUpdateIds, String[] toRetainDependencies, NutsUpdateOptions options, NutsSession session);

    NutsUpdateResult update(NutsId id, NutsUpdateOptions options, NutsSession session);

    NutsDefinition install(String id, String[] args, NutsInstallOptions options, NutsSession session);

    NutsDefinition install(NutsId id, String[] args, NutsInstallOptions options, NutsSession session);

    boolean uninstall(String id, String[] args, NutsUninstallOptions options, NutsSession session);

    boolean uninstall(NutsId id, String[] args, NutsUninstallOptions options, NutsSession session);

    NutsDefinition checkout(String id, String folder, NutsSession session);

    NutsId commit(String folder, NutsSession session);

    void push(String id, String repositoryId, NutsPushOptions options, NutsSession session);

    void push(NutsId id, String repositoryId, NutsPushOptions options, NutsSession session);

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
    NutsDefinition createBundle(String contentFolder, String destFile, NutsQueryOptions queryOptions, NutsSession session);

    NutsId deploy(NutsDeployment deployment, NutsSession session);

    boolean isFetched(NutsId id, NutsSession session);

    NutsWorkspace setSystemTerminal(NutsSystemTerminalBase term);

    void installCompanionTools(NutsInstallCompanionOptions options, NutsSession session);

    NutsDefinition fetchApiDefinition(NutsSession session);

    NutsWorkspaceRepositoryManager getRepositoryManager();

    NutsWorkspaceExtensionManager getExtensionManager();

    NutsWorkspaceConfigManager getConfigManager();

    NutsWorkspaceSecurityManager getSecurityManager();

    NutsIOManager getIOManager();

    NutsParseManager getParseManager();

    NutsFormatManager getFormatManager();

    Map<String, Object> getUserProperties();

    NutsSystemTerminal getSystemTerminal();

    NutsSessionTerminal getTerminal();

    void setTerminal(NutsSessionTerminal newTerminal);

    NutsSession createSession();

    String copyTo(NutsId id, String localPath, NutsSession session);

    void addUserPropertyListener(NutsMapListener<String, Object> listener);

    void removeUserPropertyListener(NutsMapListener<String, Object> listener);

    NutsMapListener<String, Object>[] getUserPropertyListeners();

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    String resolveDefaultHelpForClass(Class clazz);

    NutsId resolveIdForClass(Class clazz);

    NutsId[] resolveIdsForClass(Class clazz);

    String getFileName(NutsId id, String ext);

    NutsQuery createQuery();

    void updateRepositoryIndex(String path);

    void updateAllRepositoryIndices();

    String getWelcomeText();

    String getHelpText();

    String getLicenseText();

    NutsClassLoaderBuilder createClassLoaderBuilder();

    NutsDescriptorBuilder createDescriptorBuilder();

    String createRegex(String pattern);

    NutsIdBuilder createIdBuilder();

    NutsCommandExecBuilder createExecBuilder();

    NutsDeploymentBuilder createDeploymentBuilder();

    NutsQueryOptions createQueryOptions();

    void save(boolean force);

    void save();

    boolean isGlobal();
}
