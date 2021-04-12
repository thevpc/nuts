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
package net.thevpc.nuts.runtime.core.sessionaware;

import java.util.Iterator;
import java.util.Map;
import net.thevpc.nuts.NutsContent;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsFetchMode;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsIndexStore;
import net.thevpc.nuts.NutsMapListener;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryConfigManager;
import net.thevpc.nuts.NutsRepositoryEnvManager;
import net.thevpc.nuts.NutsRepositoryListener;
import net.thevpc.nuts.NutsRepositorySecurityManager;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsFetchContentRepositoryCommand;
import net.thevpc.nuts.spi.NutsFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;
import net.thevpc.nuts.spi.NutsSearchRepositoryCommand;
import net.thevpc.nuts.spi.NutsSearchVersionsRepositoryCommand;
import net.thevpc.nuts.spi.NutsUpdateRepositoryStatisticsCommand;

/**
 *
 * @author vpc
 */
public class NutsRepositorySessionAwareImpl implements NutsRepository , NutsRepositorySPI, NutsRepositoryExt{

    private NutsRepository repo;
    private NutsWorkspace ws;
    private NutsSession session;

//    public static NutsRepositorySessionAwareImpl of(NutsRepository repo, NutsWorkspace ws) {
//        if (repo == null) {
//            return null;
//        }
//        if (repo instanceof NutsRepositorySessionAwareImpl) {
//            return (NutsRepositorySessionAwareImpl) repo;
//        }
//        return new NutsRepositorySessionAwareImpl(repo, ws);
//    }
    public static NutsRepositorySessionAwareImpl of(NutsRepository repo, NutsWorkspace ws, NutsSession session) {
        if (repo == null) {
            return null;
        }
        if (repo instanceof NutsRepositorySessionAwareImpl) {
            NutsRepositorySessionAwareImpl a = ((NutsRepositorySessionAwareImpl) repo);
            NutsSession s2 = a.getSession();
            if (s2 == session) {
                return a;
            }
            return (NutsRepositorySessionAwareImpl) new NutsRepositorySessionAwareImpl(a.repo, session.getWorkspace(), session);
        }
        return (NutsRepositorySessionAwareImpl) new NutsRepositorySessionAwareImpl(repo, session.getWorkspace(), session);
    }

    private NutsRepositorySessionAwareImpl(NutsRepository repo, NutsWorkspace ws, NutsSession session) {
        this.repo = repo;
        this.ws = ws;
        this.session = session;
    }

    public NutsSession getSession() {
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
    public NutsRepositoryEnvManager env() {
        return repo.env().setSession(session);
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return session == null
                ? ws : session.getWorkspace();
    }

    @Override
    public NutsRepository getParentRepository() {
        return NutsRepositorySessionAwareImpl.of(repo.getParentRepository(), getWorkspace(), session);
    }

    @Override
    public NutsRepositoryConfigManager config() {
        return repo.config().setSession(session);
    }

    @Override
    public NutsRepositorySecurityManager security() {
        return repo.security().setSession(session);
    }

    @Override
    public NutsRepository removeRepositoryListener(NutsRepositoryListener listener) {
        repo.removeRepositoryListener(listener);
        return this;
    }

    @Override
    public NutsRepository addRepositoryListener(NutsRepositoryListener listener) {
        repo.addRepositoryListener(listener);
        return this;
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repo.getRepositoryListeners();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return repo.getUserProperties();
    }

    @Override
    public NutsRepository addUserPropertyListener(NutsMapListener<String, Object> listener) {
        repo.addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NutsRepository removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        repo.removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return repo.getUserPropertyListeners();
    }

    @Override
    public boolean isEnabled() {
        return repo.isEnabled();
    }

    @Override
    public NutsRepository setEnabled(boolean enabled) {
        return repo.setEnabled(enabled);
    }

    private NutsRepositorySPI repoSPI(){
        return (NutsRepositorySPI)repo;
    } 
    
    @Override
    public NutsDeployRepositoryCommand deploy() {
        return repoSPI().deploy().setSession(getSession());
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return repoSPI().undeploy().setSession(getSession());
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return repoSPI().push().setSession(getSession());
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return repoSPI().fetchDescriptor().setSession(getSession());
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return repoSPI().fetchContent().setSession(getSession());
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return repoSPI().search().setSession(getSession());
    }

    @Override
    public NutsSearchVersionsRepositoryCommand searchVersions() {
        return repoSPI().searchVersions().setSession(getSession());
    }

    @Override
    public NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return repoSPI().updateStatistics().setSession(getSession());
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return repoSPI().isAcceptFetchMode(mode,session);
    }

    private NutsRepositoryExt repoExt(){
        return ((NutsRepositoryExt)repo);
    }
    @Override
    public NutsIndexStore getIndexStore() {
        return repoExt().getIndexStore();
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        repoExt().pushImpl(command);
    }

    @Override
    public NutsDescriptor deployImpl(NutsDeployRepositoryCommand command) {
        return repoExt().deployImpl(command);
    }

    @Override
    public void undeployImpl(NutsRepositoryUndeployCommand command) {
        repoExt().undeployImpl(command);
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsSession session) {
        repoExt().checkAllowedFetch(id, session);
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        return repoExt().fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public Iterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        return repoExt().searchVersionsImpl(id, idFilter, fetchMode, session);
    }

    @Override
    public NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        return repoExt().fetchContentImpl(id, descriptor, localPath, fetchMode, session);
    }

    @Override
    public Iterator<NutsId> searchImpl(NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return repoExt().searchImpl(filter, fetchMode, session);
    }

    @Override
    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return repoExt().searchLatestVersion(id, filter, fetchMode, session);
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session) {
        return repoExt().acceptAction(id, supportedAction, mode, session);
    }

    @Override
    public String getIdBasedir(NutsId id, NutsSession session) {
        return repoExt().getIdBasedir(id, session);
    }

    @Override
    public String getIdFilename(NutsId id, NutsSession session) {
        return repoExt().getIdFilename(id, session);
    }

    
}
