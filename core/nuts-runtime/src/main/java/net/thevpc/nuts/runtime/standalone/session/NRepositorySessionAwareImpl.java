///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// *
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
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
//import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
//import net.thevpc.nuts.spi.NDeployRepositoryCmd;
//import net.thevpc.nuts.spi.NFetchContentRepositoryCmd;
//import net.thevpc.nuts.spi.NFetchDescriptorRepositoryCmd;
//import net.thevpc.nuts.spi.NPushRepositoryCmd;
//import net.thevpc.nuts.spi.NRepositorySPI;
//import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
//import net.thevpc.nuts.spi.NSearchRepositoryCmd;
//import net.thevpc.nuts.spi.NSearchVersionsRepositoryCmd;
//import net.thevpc.nuts.spi.NUpdateRepositoryStatsCmd;
//import net.thevpc.nuts.util.NIterator;
//import net.thevpc.nuts.util.NObservableMapListener;
//
///**
// *
// * @author thevpc
// */
//public class NRepositorySessionAwareImpl implements NRepository, NRepositorySPI, NRepositoryExt {
//
//    private NRepository repo;
//    private NWorkspace ws;
//    private NSession session;
//
////    public static NutsRepositorySessionAwareImpl of(NutsRepository repo, NutsWorkspace ws) {
////        if (repo == null) {
////            return null;
////        }
////        if (repo instanceof NutsRepositorySessionAwareImpl) {
////            return (NutsRepositorySessionAwareImpl) repo;
////        }
////        return new NutsRepositorySessionAwareImpl(repo, ws);
////    }
//    public static NRepositorySessionAwareImpl of(NRepository repo, NWorkspace ws, NSession session) {
//        if (repo == null) {
//            return null;
//        }
//        if (repo instanceof NRepositorySessionAwareImpl) {
//            NRepositorySessionAwareImpl a = ((NRepositorySessionAwareImpl) repo);
//            NSession s2 = a.getSession();
//            if (s2 == session) {
//                return a;
//            }
//            return (NRepositorySessionAwareImpl) new NRepositorySessionAwareImpl(a.repo, session.getWorkspace(), session);
//        }
//        return (NRepositorySessionAwareImpl) new NRepositorySessionAwareImpl(repo, session.getWorkspace(), session);
//    }
//
//    private NRepositorySessionAwareImpl(NRepository repo, NWorkspace ws, NSession session) {
//        this.repo = repo;
//        this.ws = ws;
//        this.session = session;
//    }
//
//    public NSession getSession() {
//        return session;
//    }
//
////    public NutsRepository setSession(NSession session) {
////        this.session = session;
////        return this;
////    }
//
//
//    @Override
//    public boolean containsTags(String tag) {
//        return repo.containsTags(tag);
//    }
//
//    @Override
//    public Set<String> getTags() {
//        return repo.getTags();
//    }
//
//    @Override
//    public boolean isPreview() {
//        return repo.isPreview();
//    }
//
//    @Override
//    public NRepository addTag(String tag) {
//        return repo.addTag(tag);
//    }
//
//    @Override
//    public NRepository removeTag(String tag) {
//        return repo.removeTag(tag);
//    }
//
//    @Override
//    public String getRepositoryType() {
//        return repo.getRepositoryType();
//    }
//
//    @Override
//    public String getUuid() {
//        return repo.getUuid();
//    }
//
//    @Override
//    public String getName() {
//        return repo.getName();
//    }
//
//    @Override
//    public NWorkspace getWorkspace() {
//        return session == null
//                ? ws : session.getWorkspace();
//    }
//
//    @Override
//    public NRepository getParentRepository() {
//        return NRepositorySessionAwareImpl.of(repo.getParentRepository(), getWorkspace(), session);
//    }
//
//    @Override
//    public NRepositoryConfigManager config() {
//        return repo.config();
//    }
//
//    @Override
//    public NRepositorySecurityManager security() {
//        return repo.security();
//    }
//
//    @Override
//    public NRepository removeRepositoryListener(NRepositoryListener listener) {
//        repo.removeRepositoryListener(listener);
//        return this;
//    }
//
//    @Override
//    public NRepository addRepositoryListener(NRepositoryListener listener) {
//        repo.addRepositoryListener(listener);
//        return this;
//    }
//
//    @Override
//    public List<NRepositoryListener> getRepositoryListeners() {
//        return repo.getRepositoryListeners();
//    }
//
//    @Override
//    public Map<String, Object> getUserProperties() {
//        return repo.getUserProperties();
//    }
//
//    @Override
//    public NRepository addUserPropertyListener(NObservableMapListener<String, Object> listener) {
//        repo.addUserPropertyListener(listener);
//        return this;
//    }
//
//    @Override
//    public NRepository removeUserPropertyListener(NObservableMapListener<String, Object> listener) {
//        repo.removeUserPropertyListener(listener);
//        return this;
//    }
//
//    @Override
//    public List<NObservableMapListener<String, Object>> getUserPropertyListeners() {
//        return repo.getUserPropertyListeners();
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return repo.isEnabled();
//    }
//
//    @Override
//    public NRepository setEnabled(boolean enabled) {
//        return repo.setEnabled(enabled);
//    }
//
//    private NRepositorySPI repoSPI() {
//        return (NRepositorySPI) repo;
//    }
//
//    @Override
//    public NDeployRepositoryCmd deploy() {
//        return repoSPI().deploy();
//    }
//
//    @Override
//    public NRepositoryUndeployCmd undeploy() {
//        return repoSPI().undeploy();
//    }
//
//    @Override
//    public NPushRepositoryCmd push() {
//        return repoSPI().push();
//    }
//
//    @Override
//    public NFetchDescriptorRepositoryCmd fetchDescriptor() {
//        return repoSPI().fetchDescriptor();
//    }
//
//    @Override
//    public NFetchContentRepositoryCmd fetchContent() {
//        return repoSPI().fetchContent();
//    }
//
//    @Override
//    public NSearchRepositoryCmd search() {
//        return repoSPI().search();
//    }
//
//    @Override
//    public NSearchVersionsRepositoryCmd searchVersions() {
//        return repoSPI().searchVersions();
//    }
//
//    @Override
//    public NUpdateRepositoryStatsCmd updateStatistics() {
//        return repoSPI().updateStatistics();
//    }
//
//    @Override
//    public boolean isAcceptFetchMode(NFetchMode mode) {
//        return repoSPI().isAcceptFetchMode(mode);
//    }
//
//    @Override
//    public boolean isSupportedDeploy() {
//        return repo.isSupportedDeploy();
//    }
//
//    @Override
//    public boolean isSupportedDeploy(boolean force) {
//        return repo.isSupportedDeploy(force);
//    }
//
//    private NRepositoryExt repoExt() {
//        return ((NRepositoryExt) repo);
//    }
//
//    @Override
//    public NIndexStore getIndexStore() {
//        return repoExt().getIndexStore();
//    }
//
//    @Override
//    public void pushImpl(NPushRepositoryCmd command) {
//        repoExt().pushImpl(command);
//    }
//
//    @Override
//    public NDescriptor deployImpl(NDeployRepositoryCmd command) {
//        return repoExt().deployImpl(command);
//    }
//
//    @Override
//    public void undeployImpl(NRepositoryUndeployCmd command) {
//        repoExt().undeployImpl(command);
//    }
//
//    @Override
//    public void checkAllowedFetch(NId id) {
//        repoExt().checkAllowedFetch(id);
//    }
//
//    @Override
//    public NDescriptor fetchDescriptorImpl(NId id, NFetchMode fetchMode) {
//        return repoExt().fetchDescriptorImpl(id, fetchMode);
//    }
//
//    @Override
//    public NIterator<NId> searchVersionsImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode) {
//        return repoExt().searchVersionsImpl(id, idFilter, fetchMode);
//    }
//
//    @Override
//    public NPath fetchContentImpl(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
//        return repoExt().fetchContentImpl(id, descriptor, fetchMode);
//    }
//
//    @Override
//    public NIterator<NId> searchImpl(NIdFilter filter, NFetchMode fetchMode) {
//        return repoExt().searchImpl(filter, fetchMode);
//    }
//
//    @Override
//    public NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode) {
//        return repoExt().searchLatestVersion(id, filter, fetchMode);
//    }
//
//    @Override
//    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode) {
//        return repoExt().acceptAction(id, supportedAction, mode);
//    }
//
//    @Override
//    public NPath getIdBasedir(NId id) {
//        return repoExt().getIdBasedir(id);
//    }
//
//    @Override
//    public String getIdFilename(NId id) {
//        return repoExt().getIdFilename(id);
//    }
//
//    @Override
//    public boolean isAvailable() {
//        return repo.isAvailable();
//    }
//
//    @Override
//    public boolean isAvailable(boolean force) {
//        return repo.isAvailable(force);
//    }
//
//    @Override
//    public String toString() {
//        return repo.toString();
//    }
//
//    @Override
//    public boolean isRemote() {
//        return repo.isRemote();
//    }
//}
