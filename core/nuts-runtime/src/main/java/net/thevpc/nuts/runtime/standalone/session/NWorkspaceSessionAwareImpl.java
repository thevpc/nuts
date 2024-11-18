///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// * <p>
// * Copyright [2020] [thevpc]
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
// * you may  not use this file except in compliance with the License. You may obtain
// * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
//package net.thevpc.nuts.runtime.standalone.session;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
//import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
//import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
//import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
//import net.thevpc.nuts.spi.NInstallerComponent;
//import net.thevpc.nuts.spi.NSupportLevelContext;
//import net.thevpc.nuts.text.NText;
//
//import java.util.Stack;
//
///**
// * @author thevpc
// */
//public class NWorkspaceSessionAwareImpl implements NWorkspace, NWorkspaceExt {
//
//    private final NSession session;
//    private final NWorkspace ws;
//
//    public NWorkspaceSessionAwareImpl(NSession session, NWorkspace ws) {
//        if (ws instanceof NWorkspaceSessionAwareImpl) {
//            ws = ((NWorkspaceSessionAwareImpl) ws).ws;
//        }
//        this.ws = ws;
//        this.session = session;
//    }
//
//    @Override
//    public NSession currentSession() {
//        return ws.currentSession();
//    }
//
//    @Override
//    public Stack<NSession> sessionScopes() {
//        return ((NWorkspaceExt) ws).sessionScopes();
//    }
//
//    @Override
//    public String getUuid() {
//        return ws.getUuid();
//    }
//
//    @Override
//    public String getName() {
//        return ws.getName();
//    }
//
//    @Override
//    public String getHashName() {
//        return ws.getHashName();
//    }
//
//    @Override
//    public NVersion getApiVersion() {
//        return ws.getApiVersion();
//    }
//
//    @Override
//    public NId getApiId() {
//        return ws.getApiId();
//    }
//
//    @Override
//    public NId getRuntimeId() {
//        return ws.getRuntimeId();
//    }
//
//    @Override
//    public NPath getLocation() {
//        return ws.getLocation();
//    }
//
////    @Override
////    public Set<NutsId> getCompanionIds(NutsSession session) {
////        return ws.getCompanionIds(session);
////    }
//
//    @Override
//    public NSession createSession() {
//        return ws.createSession();
//    }
//
//    @Override
//    public int getSupportLevel(NSupportLevelContext context) {
//        return ws.getSupportLevel(context);
//    }
//
//
//    public NSession getSession() {
//        return session;
//    }
//
//    @Override
//    public NText getWelcomeText() {
//        return ((NWorkspaceExt) ws).getWelcomeText();
//    }
//
//    @Override
//    public NText getHelpText() {
//        return ((NWorkspaceExt) ws).getHelpText();
//    }
//
//    @Override
//    public NText getLicenseText() {
//        return ((NWorkspaceExt) ws).getLicenseText();
//    }
//
//    @Override
//    public NText resolveDefaultHelp(Class clazz) {
//        return ((NWorkspaceExt) ws).resolveDefaultHelp(clazz);
//    }
//
//    @Override
//    public NId resolveEffectiveId(NDescriptor descriptor) {
//        return ((NWorkspaceExt) ws).resolveEffectiveId(descriptor);
//    }
//
//    @Override
//    public NIdType resolveNutsIdType(NId id) {
//        return ((NWorkspaceExt) ws).resolveNutsIdType(id);
//    }
//
//    @Override
//    public NInstallerComponent getInstaller(NDefinition nutToInstall) {
//        return ((NWorkspaceExt) ws).getInstaller(nutToInstall);
//    }
//
//    @Override
//    public void requireImpl(NDefinition def, boolean withDependencies, NId[] forId) {
//        ((NWorkspaceExt) ws).requireImpl(def, withDependencies, forId);
//    }
//
//    @Override
//    public void installImpl(NDefinition def, String[] args, boolean updateDefaultVersion) {
//        ((NWorkspaceExt) ws).installImpl(def, args, updateDefaultVersion);
//    }
//
//    @Override
//    public void updateImpl(NDefinition def, String[] args, boolean updateDefaultVersion) {
//        ((NWorkspaceExt) ws).updateImpl(def, args, updateDefaultVersion);
//    }
//
//    public void uninstallImpl(NDefinition def, String[] args, boolean runInstaller, boolean deleteFiles, boolean eraseFiles, boolean traceBeforeEvent) {
//        ((NWorkspaceExt) ws).uninstallImpl(def, args, runInstaller, deleteFiles, eraseFiles, traceBeforeEvent);
//    }
//
//
//    @Override
//    public boolean requiresRuntimeExtension() {
//        return ((NWorkspaceExt) ws).requiresRuntimeExtension();
//    }
//
//    @Override
//    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor) {
//        return ((NWorkspaceExt) ws).resolveEffectiveDescriptor(descriptor);
//    }
//
//    @Override
//    public NInstalledRepository getInstalledRepository() {
//        return ((NWorkspaceExt) ws).getInstalledRepository();
//    }
//
//    @Override
//    public NInstallStatus getInstallStatus(NId id, boolean checkDependencies) {
//        return ((NWorkspaceExt) ws).getInstallStatus(id, checkDependencies);
//    }
//
//    @Override
//    public NExecutionContextBuilder createExecutionContext() {
//        return ((NWorkspaceExt) ws).createExecutionContext();
//    }
//
//    @Override
//    public void deployBoot(NId def, boolean withDependencies) {
//        ((NWorkspaceExt) ws).deployBoot(def, withDependencies);
//    }
//
//    @Override
//    public NSession defaultSession() {
//        return ((NWorkspaceExt) ws).defaultSession();
//    }
//
//    @Override
//    public NWorkspaceModel getModel() {
//        return ((NWorkspaceExt) ws).getModel();
//    }
//
//    @Override
//    public String toString() {
//        return ws.toString();
//    }
//
//    @Override
//    public String getInstallationDigest() {
//        return ((NWorkspaceExt) ws).getInstallationDigest();
//    }
//
//    @Override
//    public void setInstallationDigest(String value) {
//        ((NWorkspaceExt) ws).setInstallationDigest(value);
//    }
//}
