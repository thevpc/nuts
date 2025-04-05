/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.text.NText;

import java.util.List;
import java.util.Stack;

/**
 * @author thevpc
 */
public interface NWorkspaceExt {


    static NWorkspaceExt of() {
        return ((NWorkspaceExt) NWorkspace.of());
    }

    static NWorkspaceExt of(NWorkspace ws) {
        return ((NWorkspaceExt) ws);
    }

    NWorkspaceStore store();

    NText getWelcomeText();

    NText getHelpText();

    NText getLicenseText();

    NText resolveDefaultHelp(Class<?> clazz);

    NId resolveEffectiveId(NDescriptor descriptor);

    NIdType resolveNutsIdType(NId id);

    NInstallerComponent getInstaller(NDefinition nutToInstall);

    void requireImpl(NDefinition def, boolean withDependencies, NId[] forId);

    void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion);

    void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion);

    void uninstallImpl(NDefinition def, String[] args, boolean runInstaller, boolean deleteFiles, boolean eraseFiles, boolean traceBeforeEvent);

    /**
     * true when runtime extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : runtime extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_RUNTIME_EXTENSION</code> is forced to
     * false
     *
     * @return true when runtime extension is required for running this
     * workspace
     */
    boolean requiresRuntimeExtension();

    NInstalledRepository getInstalledRepository();

    NInstallStatus getInstallStatus(NId id, boolean checkDependencies);

    NExecutionContextBuilder createExecutionContext();

    void deployBoot(NId def, boolean withDependencies);

    NSession defaultSession();

    NWorkspaceModel getModel();

    String getInstallationDigest();

    void setInstallationDigest(String value);

    Stack<NSession> sessionScopes();

    DefaultNRepositoryModel getRepositoryModel();

    public DefaultNWorkspaceEnvManagerModel getEnvModel();

    public DefaultCustomCommandsModel getCommandModel();

    public DefaultNWorkspaceConfigModel getConfigModel();

    public DefaultImportModel getImportModel();

    NDependencySolver createDependencySolver(String solverName);

    List<String> getDependencySolverNames();

    DefaultNWorkspaceLocationModel getLocationModel();
}
