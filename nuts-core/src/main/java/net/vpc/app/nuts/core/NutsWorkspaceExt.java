/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;

import java.util.Set;

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

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession options);

    NutsIdType resolveNutsIdType(NutsId id);

    NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session);

    void requireImpl(NutsDefinition def, NutsSession session, boolean withDependencies,NutsId[] forId);
    void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    /**
     * true when runtime extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : runtime extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_RUNTIME_EXTENSION</code> is forced to
     * false
     *
     * @return true when runtime extension is required for running this workspace
     * @param session
     */
    boolean requiresRuntimeExtension(NutsSession session);

    //    @Override
    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsInstalledRepository getInstalledRepository();

    Set<NutsInstallStatus> getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session);

    NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, 
            NutsSession traceSession, 
            NutsSession execSession, 
            boolean failFast, boolean temporary, NutsExecutionType executionType, String commandName);

    @Deprecated
    void deployBoot(NutsSession session, NutsId def,boolean withDependencies);
}
