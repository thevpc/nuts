/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutionContextBuilder;
import net.thevpc.nuts.spi.NutsInstallerComponent;

/**
 * @author thevpc
 */
public interface NutsWorkspaceExt {

    static NutsWorkspaceExt of(NutsSession ws) {
        return of(ws.getWorkspace());
    }

    static NutsWorkspaceExt of(NutsWorkspace ws) {
        return (NutsWorkspaceExt) ws;
    }

    String getWelcomeText(NutsSession session);

    String getHelpText(NutsSession session);

    String getLicenseText(NutsSession session);

    String resolveDefaultHelp(Class clazz, NutsSession session);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession options);

    NutsIdType resolveNutsIdType(NutsId id, NutsSession session);

    NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session);

    void requireImpl(NutsDefinition def, NutsSession session, boolean withDependencies, NutsId[] forId);

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
     * @param session session
     * @return true when runtime extension is required for running this
     * workspace
     */
    boolean requiresRuntimeExtension(NutsSession session);

    //    @Override
    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsInstalledRepository getInstalledRepository();

    NutsInstallStatus getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session);

    NutsExecutionContextBuilder createExecutionContext();

    void deployBoot(NutsSession session, NutsId def, boolean withDependencies);

    NutsSession defaultSession();

}
