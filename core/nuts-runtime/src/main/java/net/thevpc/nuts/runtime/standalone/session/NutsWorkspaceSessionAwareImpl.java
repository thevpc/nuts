/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceEnvManager;
import net.thevpc.nuts.spi.NutsInstallerComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

/**
 * @author thevpc
 */
public class NutsWorkspaceSessionAwareImpl implements NutsWorkspace, NutsWorkspaceExt {

    private final NutsSession session;
    private final NutsWorkspace ws;

    public NutsWorkspaceSessionAwareImpl(NutsSession session, NutsWorkspace ws) {
        if (ws instanceof NutsWorkspaceSessionAwareImpl) {
            ws = ((NutsWorkspaceSessionAwareImpl) ws).ws;
        }
        this.ws = ws;
        this.session = session;
    }

    @Override
    public String getUuid() {
        return ws.getUuid();
    }

    @Override
    public String getName() {
        return ws.getName();
    }

    @Override
    public String getHashName() {
        return ws.getHashName();
    }

    @Override
    public NutsVersion getApiVersion() {
        return ws.getApiVersion();
    }

    @Override
    public NutsId getApiId() {
        return ws.getApiId();
    }

    @Override
    public NutsId getRuntimeId() {
        return ws.getRuntimeId();
    }

    @Override
    public NutsPath getLocation() {
        return ws.getLocation();
    }

//    @Override
//    public Set<NutsId> getCompanionIds(NutsSession session) {
//        return ws.getCompanionIds(session);
//    }

    @Override
    public NutsSession createSession() {
        return ws.createSession();
    }

    @Override
    public NutsSearchCommand search() {
        return ws.search().setSession(getSession());
    }

    @Override
    public NutsFetchCommand fetch() {
        return ws.fetch().setSession(getSession());
    }

    @Override
    public NutsDeployCommand deploy() {
        return ws.deploy().setSession(getSession());
    }

    @Override
    public NutsUndeployCommand undeploy() {
        return ws.undeploy().setSession(getSession());
    }

    @Override
    public NutsExecCommand exec() {
        return ws.exec().setSession(getSession());
    }

    @Override
    public NutsInstallCommand install() {
        return ws.install().setSession(getSession());
    }

    @Override
    public NutsUninstallCommand uninstall() {
        return ws.uninstall().setSession(getSession());
    }

    @Override
    public NutsUpdateCommand update() {
        return ws.update().setSession(getSession());
    }

    @Override
    public NutsPushCommand push() {
        return ws.push().setSession(getSession());
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return ws.updateStatistics().setSession(getSession());
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return ws.extensions().setSession(getSession());
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return ws.config().setSession(getSession());
    }

    @Override
    public NutsRepositoryManager repos() {
        return ws.repos().setSession(getSession());
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return ws.security().setSession(getSession());
    }

    @Override
    public NutsWorkspaceEventManager events() {
        return ws.events().setSession(getSession());
    }

    @Override
    public NutsInfoCommand info() {
        return ws.info().setSession(getSession());
    }

    @Override
    public NutsImportManager imports() {
        return ws.imports().setSession(getSession());
    }

    @Override
    public NutsCustomCommandManager commands() {
        return ws.commands().setSession(getSession());
    }

    @Override
    public NutsWorkspaceLocationManager locations() {
        return ws.locations().setSession(getSession());
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        DefaultNutsWorkspaceEnvManager e = (DefaultNutsWorkspaceEnvManager) ws.env();
        return new DefaultNutsWorkspaceEnvManager(e.getModel()).setSession(getSession());
    }

    @Override
    public NutsBootManager boot() {
        return ws.boot().setSession(session);
    }


    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return ws.getSupportLevel(context);
    }


    public NutsSession getSession() {
        return session;
    }

    @Override
    public String getWelcomeText(NutsSession session) {
        return ((NutsWorkspaceExt) ws).getWelcomeText(session);
    }

    @Override
    public String getHelpText(NutsSession session) {
        return ((NutsWorkspaceExt) ws).getHelpText(session);
    }

    @Override
    public String getLicenseText(NutsSession session) {
        return ((NutsWorkspaceExt) ws).getLicenseText(session);
    }

    @Override
    public String resolveDefaultHelp(Class clazz, NutsSession session) {
        return ((NutsWorkspaceExt) ws).resolveDefaultHelp(clazz, session);
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session) {
        return ((NutsWorkspaceExt) ws).resolveEffectiveId(descriptor, session);
    }

    @Override
    public NutsIdType resolveNutsIdType(NutsId id, NutsSession session) {
        return ((NutsWorkspaceExt) ws).resolveNutsIdType(id, session);
    }

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        return ((NutsWorkspaceExt) ws).getInstaller(nutToInstall, session);
    }

    @Override
    public void requireImpl(NutsDefinition def, NutsSession session, boolean withDependencies, NutsId[] forId) {
        ((NutsWorkspaceExt) ws).requireImpl(def, session, withDependencies, forId);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        ((NutsWorkspaceExt) ws).installImpl(def, args, installerComponent, session, updateDefaultVersion);
    }

    @Override
    public void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        ((NutsWorkspaceExt) ws).updateImpl(def, args, installerComponent, session, updateDefaultVersion);
    }

    @Override
    public boolean requiresRuntimeExtension(NutsSession session) {
        return ((NutsWorkspaceExt) ws).requiresRuntimeExtension(session);
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        return ((NutsWorkspaceExt) ws).resolveEffectiveDescriptor(descriptor, session);
    }

    @Override
    public NutsInstalledRepository getInstalledRepository() {
        return ((NutsWorkspaceExt) ws).getInstalledRepository();
    }

    @Override
    public NutsInstallStatus getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session) {
        return ((NutsWorkspaceExt) ws).getInstallStatus(id, checkDependencies, session);
    }

    @Override
    public NutsExecutionContextBuilder createExecutionContext() {
        return ((NutsWorkspaceExt) ws).createExecutionContext();
    }

    @Override
    public void deployBoot(NutsSession session, NutsId def, boolean withDependencies) {
        ((NutsWorkspaceExt) ws).deployBoot(session, def, withDependencies);
    }

    @Override
    public NutsSession defaultSession() {
        return ((NutsWorkspaceExt) ws).defaultSession();
    }

    @Override
    public NutsWorkspaceModel getModel() {
        return ((NutsWorkspaceExt) ws).getModel();
    }

    @Override
    public String toString() {
        return ws.toString();
    }
}
