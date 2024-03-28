/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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

import java.util.List;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.spi.NFetchContentRepositoryCmd;
import net.thevpc.nuts.spi.NFetchDescriptorRepositoryCmd;
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
import net.thevpc.nuts.spi.NSearchRepositoryCmd;
import net.thevpc.nuts.spi.NSearchVersionsRepositoryCmd;
import net.thevpc.nuts.spi.NUpdateRepositoryStatsCmd;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NMapListener;

/**
 *
 * @author thevpc
 */
public class NRepositorySessionAwareImpl implements NRepository, NRepositorySPI, NRepositoryExt {

    private NRepository repo;
    private NWorkspace ws;
    private NSession session;

//    public static NutsRepositorySessionAwareImpl of(NutsRepository repo, NutsWorkspace ws) {
//        if (repo == null) {
//            return null;
//        }
//        if (repo instanceof NutsRepositorySessionAwareImpl) {
//            return (NutsRepositorySessionAwareImpl) repo;
//        }
//        return new NutsRepositorySessionAwareImpl(repo, ws);
//    }
    public static NRepositorySessionAwareImpl of(NRepository repo, NWorkspace ws, NSession session) {
        if (repo == null) {
            return null;
        }
        if (repo instanceof NRepositorySessionAwareImpl) {
            NRepositorySessionAwareImpl a = ((NRepositorySessionAwareImpl) repo);
            NSession s2 = a.getSession();
            if (s2 == session) {
                return a;
            }
            return (NRepositorySessionAwareImpl) new NRepositorySessionAwareImpl(a.repo, session.getWorkspace(), session);
        }
        return (NRepositorySessionAwareImpl) new NRepositorySessionAwareImpl(repo, session.getWorkspace(), session);
    }

    private NRepositorySessionAwareImpl(NRepository repo, NWorkspace ws, NSession session) {
        this.repo = repo;
        this.ws = ws;
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }

//    public NutsRepository setSession(NutsSession session) {
//        this.session = session;
//        return this;
//    }
    @Override
    public String getRepositoryType() {
        return repo.getRepositoryType();
    }

    @Override
    public String getUuid() {
        return repo.getUuid();
    }

    @Override
    public String getName() {
        return repo.getName();
    }

    @Override
    public NWorkspace getWorkspace() {
        return session == null
                ? ws : session.getWorkspace();
    }

    @Override
    public NRepository getParentRepository() {
        return NRepositorySessionAwareImpl.of(repo.getParentRepository(), getWorkspace(), session);
    }

    @Override
    public NRepositoryConfigManager config() {
        return repo.config().setSession(session);
    }

    @Override
    public NRepositorySecurityManager security() {
        return repo.security().setSession(session);
    }

    @Override
    public NRepository removeRepositoryListener(NRepositoryListener listener) {
        repo.removeRepositoryListener(listener);
        return this;
    }

    @Override
    public NRepository addRepositoryListener(NRepositoryListener listener) {
        repo.addRepositoryListener(listener);
        return this;
    }

    @Override
    public List<NRepositoryListener> getRepositoryListeners() {
        return repo.getRepositoryListeners();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return repo.getUserProperties();
    }

    @Override
    public NRepository addUserPropertyListener(NMapListener<String, Object> listener) {
        repo.addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NRepository removeUserPropertyListener(NMapListener<String, Object> listener) {
        repo.removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public List<NMapListener<String, Object>> getUserPropertyListeners() {
        return repo.getUserPropertyListeners();
    }

    @Override
    public boolean isEnabled(NSession session) {
        return repo.isEnabled(session);
    }

    @Override
    public NRepository setEnabled(boolean enabled, NSession session) {
        return repo.setEnabled(enabled, session);
    }

    private NRepositorySPI repoSPI() {
        return (NRepositorySPI) repo;
    }

    @Override
    public NDeployRepositoryCmd deploy() {
        return repoSPI().deploy().setSession(getSession());
    }

    @Override
    public NRepositoryUndeployCmd undeploy() {
        return repoSPI().undeploy().setSession(getSession());
    }

    @Override
    public NPushRepositoryCmd push() {
        return repoSPI().push().setSession(getSession());
    }

    @Override
    public NFetchDescriptorRepositoryCmd fetchDescriptor() {
        return repoSPI().fetchDescriptor().setSession(getSession());
    }

    @Override
    public NFetchContentRepositoryCmd fetchContent() {
        return repoSPI().fetchContent().setSession(getSession());
    }

    @Override
    public NSearchRepositoryCmd search() {
        return repoSPI().search().setSession(getSession());
    }

    @Override
    public NSearchVersionsRepositoryCmd searchVersions() {
        return repoSPI().searchVersions().setSession(getSession());
    }

    @Override
    public NUpdateRepositoryStatsCmd updateStatistics() {
        return repoSPI().updateStatistics().setSession(getSession());
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return repoSPI().isAcceptFetchMode(mode, session);
    }

    @Override
    public boolean isSupportedDeploy(NSession session) {
        return repo.isSupportedDeploy(session);
    }

    @Override
    public boolean isSupportedDeploy(boolean force, NSession session) {
        return repo.isSupportedDeploy(force, session);
    }

    private NRepositoryExt repoExt() {
        return ((NRepositoryExt) repo);
    }

    @Override
    public NIndexStore getIndexStore() {
        return repoExt().getIndexStore();
    }

    @Override
    public void pushImpl(NPushRepositoryCmd command) {
        repoExt().pushImpl(command);
    }

    @Override
    public NDescriptor deployImpl(NDeployRepositoryCmd command) {
        return repoExt().deployImpl(command);
    }

    @Override
    public void undeployImpl(NRepositoryUndeployCmd command) {
        repoExt().undeployImpl(command);
    }

    @Override
    public void checkAllowedFetch(NId id, NSession session) {
        repoExt().checkAllowedFetch(id, session);
    }

    @Override
    public NDescriptor fetchDescriptorImpl(NId id, NFetchMode fetchMode, NSession session) {
        return repoExt().fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public NIterator<NId> searchVersionsImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        return repoExt().searchVersionsImpl(id, idFilter, fetchMode, session);
    }

    @Override
    public NPath fetchContentImpl(NId id, NDescriptor descriptor, NFetchMode fetchMode, NSession session) {
        return repoExt().fetchContentImpl(id, descriptor, fetchMode, session);
    }

    @Override
    public NIterator<NId> searchImpl(NIdFilter filter, NFetchMode fetchMode, NSession session) {
        return repoExt().searchImpl(filter, fetchMode, session);
    }

    @Override
    public NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        return repoExt().searchLatestVersion(id, filter, fetchMode, session);
    }

    @Override
    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode, NSession session) {
        return repoExt().acceptAction(id, supportedAction, mode, session);
    }

    @Override
    public NPath getIdBasedir(NId id, NSession session) {
        return repoExt().getIdBasedir(id, session);
    }

    @Override
    public String getIdFilename(NId id, NSession session) {
        return repoExt().getIdFilename(id, session);
    }

    @Override
    public boolean isAvailable(NSession session) {
        return repo.isAvailable(session);
    }

    @Override
    public boolean isAvailable(boolean force, NSession session) {
        return repo.isAvailable(force, session);
    }

    @Override
    public String toString() {
        return repo.toString();
    }

    @Override
    public boolean isRemote() {
        return repo.isRemote();
    }
}
