/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;

/**
 *
 * @author vpc
 */
public interface NutsWorkspaceExt {

    static NutsWorkspaceExt of(NutsWorkspace ws) {
        return (NutsWorkspaceExt) ws;
    }

    String getWelcomeText();

    String getHelpText();

    String getLicenseText();

    String resolveDefaultHelp(Class clazz);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsFetchCommand options);

    /**
     * return installed version
     *
     * @param id id
     * @param session session
     * @return installed version
     */
    String[] getInstalledVersions(NutsId id, NutsSession session);

    NutsIdType resolveNutsIdType(NutsId id);

    String[] getCompanionIds();

    NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session);

    void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    boolean requiresCoreExtension();

    //    @Override
    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsInstalledRepository getInstalledRepository();

    boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session);

    NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, NutsSession session, boolean failFast, boolean temporary, NutsExecutionType executionType, String commandName);

    @Deprecated
    void deployBoot(NutsSession session, NutsId def,boolean withDependencies);
}
