/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 */
public interface NWorkspaceExt {

    static NWorkspaceExt of(NSession session) {
        return of(session.getWorkspace());
    }

    static NWorkspaceExt of(NWorkspace ws) {
        return (NWorkspaceExt) ws;
    }

    NText getWelcomeText(NSession session);

    NText getHelpText(NSession session);

    NText getLicenseText(NSession session);

    NText resolveDefaultHelp(Class clazz, NSession session);

    NId resolveEffectiveId(NDescriptor descriptor, NSession options);

    NIdType resolveNutsIdType(NId id, NSession session);

    NInstallerComponent getInstaller(NDefinition nutToInstall, NSession session);

    void requireImpl(NDefinition def, boolean withDependencies, NId[] forId, NSession session);

    void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session);

    void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session);
    void uninstallImpl(NDefinition def, String[] args, boolean runInstaller, boolean deleteFiles, boolean eraseFiles, boolean traceBeforeEvent, NSession session);

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
    boolean requiresRuntimeExtension(NSession session);

    //    @Override
    NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NSession session);

    NInstalledRepository getInstalledRepository();

    NInstallStatus getInstallStatus(NId id, boolean checkDependencies, NSession session);

    NExecutionContextBuilder createExecutionContext();

    void deployBoot(NSession session, NId def, boolean withDependencies);

    NSession defaultSession();

    NWorkspaceModel getModel();
}
