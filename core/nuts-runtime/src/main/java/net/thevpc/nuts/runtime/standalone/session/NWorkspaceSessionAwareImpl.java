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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 */
public class NWorkspaceSessionAwareImpl implements NWorkspace, NWorkspaceExt {

    private final NSession session;
    private final NWorkspace ws;

    public NWorkspaceSessionAwareImpl(NSession session, NWorkspace ws) {
        if (ws instanceof NWorkspaceSessionAwareImpl) {
            ws = ((NWorkspaceSessionAwareImpl) ws).ws;
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
    public NVersion getApiVersion() {
        return ws.getApiVersion();
    }

    @Override
    public NId getApiId() {
        return ws.getApiId();
    }

    @Override
    public NId getRuntimeId() {
        return ws.getRuntimeId();
    }

    @Override
    public NPath getLocation() {
        return ws.getLocation();
    }

//    @Override
//    public Set<NutsId> getCompanionIds(NutsSession session) {
//        return ws.getCompanionIds(session);
//    }

    @Override
    public NSession createSession() {
        return ws.createSession();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return ws.getSupportLevel(context);
    }


    public NSession getSession() {
        return session;
    }

    @Override
    public NText getWelcomeText(NSession session) {
        return ((NWorkspaceExt) ws).getWelcomeText(session);
    }

    @Override
    public NText getHelpText(NSession session) {
        return ((NWorkspaceExt) ws).getHelpText(session);
    }

    @Override
    public NText getLicenseText(NSession session) {
        return ((NWorkspaceExt) ws).getLicenseText(session);
    }

    @Override
    public NText resolveDefaultHelp(Class clazz, NSession session) {
        return ((NWorkspaceExt) ws).resolveDefaultHelp(clazz, session);
    }

    @Override
    public NId resolveEffectiveId(NDescriptor descriptor, NSession session) {
        return ((NWorkspaceExt) ws).resolveEffectiveId(descriptor, session);
    }

    @Override
    public NIdType resolveNutsIdType(NId id, NSession session) {
        return ((NWorkspaceExt) ws).resolveNutsIdType(id, session);
    }

    @Override
    public NInstallerComponent getInstaller(NDefinition nutToInstall, NSession session) {
        return ((NWorkspaceExt) ws).getInstaller(nutToInstall, session);
    }

    @Override
    public void requireImpl(NDefinition def, boolean withDependencies, NId[] forId, NSession session) {
        ((NWorkspaceExt) ws).requireImpl(def, withDependencies, forId, session);
    }

    @Override
    public void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session) {
        ((NWorkspaceExt) ws).installImpl(def, args, updateDefaultVersion, session);
    }

    @Override
    public void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion, NSession session) {
        ((NWorkspaceExt) ws).updateImpl(def, args, updateDefaultVersion, session);
    }

    public void uninstallImpl(NDefinition def, String[] args, boolean runInstaller, boolean deleteFiles, boolean eraseFiles, boolean traceBeforeEvent, NSession session) {
        ((NWorkspaceExt) ws).uninstallImpl(def, args, runInstaller, deleteFiles, eraseFiles, traceBeforeEvent, session);
    }


    @Override
    public boolean requiresRuntimeExtension(NSession session) {
        return ((NWorkspaceExt) ws).requiresRuntimeExtension(session);
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NSession session) {
        return ((NWorkspaceExt) ws).resolveEffectiveDescriptor(descriptor, session);
    }

    @Override
    public NInstalledRepository getInstalledRepository() {
        return ((NWorkspaceExt) ws).getInstalledRepository();
    }

    @Override
    public NInstallStatus getInstallStatus(NId id, boolean checkDependencies, NSession session) {
        return ((NWorkspaceExt) ws).getInstallStatus(id, checkDependencies, session);
    }

    @Override
    public NExecutionContextBuilder createExecutionContext() {
        return ((NWorkspaceExt) ws).createExecutionContext();
    }

    @Override
    public void deployBoot(NSession session, NId def, boolean withDependencies) {
        ((NWorkspaceExt) ws).deployBoot(session, def, withDependencies);
    }

    @Override
    public NSession defaultSession() {
        return ((NWorkspaceExt) ws).defaultSession();
    }

    @Override
    public NWorkspaceModel getModel() {
        return ((NWorkspaceExt) ws).getModel();
    }

    @Override
    public String toString() {
        return ws.toString();
    }

    @Override
    public String getInstallationDigest() {
        return ((NWorkspaceExt) ws).getInstallationDigest();
    }

    @Override
    public void setInstallationDigest(String value, NSession session) {
        ((NWorkspaceExt) ws).setInstallationDigest(value, session);
    }
}
