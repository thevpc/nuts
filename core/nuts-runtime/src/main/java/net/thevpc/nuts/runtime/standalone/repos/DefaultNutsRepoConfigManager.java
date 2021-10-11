package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsRepoConfigManager implements NutsRepositoryConfigManager, NutsRepositoryConfigManagerExt {

    private NutsRepositoryConfigModel model;
    private NutsSession session;

    public DefaultNutsRepoConfigManager(NutsRepositoryConfigModel model) {
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsRepositoryConfigManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    public NutsRepositoryRef getRepositoryRef() {
        checkSession();
        return getModel().getRepositoryRef(session);
    }

//    @Override
//    public String getName() {
////        checkSession();
//        return getModel().getName();
//    }

    @Override
    public int getDeployOrder() {
        checkSession();
        return getModel().getDeployOrder(session);
    }

    @Override
    public int getSpeed() {
        checkSession();
        return getModel().getSpeed(session);
    }

    @Override
    public String getType() {
        checkSession();
        return getModel().getType(session);
    }

    @Override
    public String getGroups() {
        checkSession();
        return getModel().getGroups(session);
    }

    @Override
    public String getLocation(boolean expand) {
        checkSession();
        return getModel().getLocation(expand,session);
    }

    @Override
    public String getStoreLocation() {
        checkSession();
        return getModel().getStoreLocation();
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        checkSession();
        return getModel().getStoreLocationStrategy(session);
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType) {
        checkSession();
        return getModel().getStoreLocation(folderType,session);
    }

//    @Override
//    public String getUuid() {
////        checkSession();
//        return getModel().getUuid();
//    }

    @Override
    public NutsRepositoryConfigManager setIndexEnabled(boolean enabled) {
        checkSession();
        getModel().setIndexEnabled(enabled, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(getModel().getWorkspace(), session);
    }

    @Override
    public boolean isIndexEnabled() {
        checkSession();
        return getModel().isIndexEnabled(session);
    }

    @Override
    public boolean isEnabled() {
//        checkSession();
        return getModel().isEnabled(session);
    }

    @Override
    public boolean isTemporary() {
        checkSession();
        return getModel().isTemporary(session);
    }

    @Override
    public String getGlobalName() {
        checkSession();
        return getModel().getGlobalName(session);
    }

    @Override
    public boolean isSupportedMirroring() {
        checkSession();
        return getModel().isSupportedMirroring(session);
    }

    @Override
    public NutsRepository[] getMirrors() {
        checkSession();
        return getModel().getMirrors(session);
    }

    @Override
    public NutsRepository addMirror(NutsAddRepositoryOptions options) {
        checkSession();
        return getModel().addMirror(options, session);
    }

    @Override
    public NutsRepositoryConfigModel getModel() {
        return model;
    }

    @Override
    public boolean isIndexSubscribed() {
        checkSession();
        return getModel().isIndexSubscribed(session);
    }

    @Override
    public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
        checkSession();
        getModel().setMirrorEnabled(repoName, enabled, session);
        return this;
    }

    @Override
    public NutsRepositoryConfigManager setEnabled(boolean enabled) {
        checkSession();
        getModel().setEnabled(enabled, session);
        return this;
    }

    @Override
    public NutsRepositoryConfigManager setTemporary(boolean enabled) {
        checkSession();
        getModel().setTemporary(enabled, session);
        return this;
    }

    @Override
    public NutsRepositoryConfigManager subscribeIndex() {
        checkSession();
        getModel().subscribeIndex(session);
        return this;
    }

    @Override
    public NutsRepositoryConfigManager unsubscribeIndex() {
        checkSession();
        getModel().unsubscribeIndex(session);
        return this;
    }

    @Override
    public NutsRepository findMirrorById(String repositoryNameOrId) {
        checkSession();
        return getModel().findMirrorById(repositoryNameOrId, session);
    }

    @Override
    public NutsRepository findMirrorByName(String repositoryName) {
        checkSession();
        return getModel().findMirrorById(repositoryName, session);
    }

    @Override
    public NutsRepository getMirror(String repositoryIdOrName) {
        checkSession();
        return getModel().getMirror(repositoryIdOrName, session);
    }

    @Override
    public NutsRepository findMirror(String repositoryIdOrName) {
        checkSession();
        return getModel().findMirror(repositoryIdOrName, session);
    }

    @Override
    public NutsRepositoryConfigManager removeMirror(String repositoryId) {
        checkSession();
        getModel().removeMirror(repositoryId, session);
        return this;
    }

    
}
