package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

import java.util.List;
import java.util.Map;

public class DefaultNutsRepoConfigManager implements NutsRepositoryConfigManager, NutsRepositoryConfigManagerExt {

    private final NutsRepositoryConfigModel model;
    private NutsSession session;

    public DefaultNutsRepoConfigManager(NutsRepositoryConfigModel model) {
        this.model = model;
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(getModel().getWorkspace(), session);
    }

    @Override
    public String getGlobalName() {
        checkSession();
        return getModel().getGlobalName(session);
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
    public NutsSpeedQualifier getSpeed() {
        checkSession();
        return getModel().getSpeed(session);
    }

    @Override
    public boolean isTemporary() {
        checkSession();
        return getModel().isTemporary(session);
    }

    @Override
    public NutsRepositoryConfigManager setTemporary(boolean enabled) {
        checkSession();
        getModel().setTemporary(enabled, session);
        return this;
    }

    @Override
    public boolean isIndexSubscribed() {
        checkSession();
        return getModel().isIndexSubscribed(session);
    }

    @Override
    public NutsRepositoryLocation getLocation() {
        checkSession();
        return getModel().getLocation(session);
    }

    @Override
    public NutsPath getLocationPath() {
        checkSession();
        return getModel().getLocationPath(session);
    }

    @Override
    public NutsPath getStoreLocation() {
        checkSession();
        return getModel().getStoreLocation();
    }

    @Override
    public NutsPath getStoreLocation(NutsStoreLocation folderType) {
        checkSession();
        return getModel().getStoreLocation(folderType, session);
    }

    @Override
    public boolean isIndexEnabled() {
        checkSession();
        return getModel().isIndexEnabled(session);
    }

    @Override
    public NutsRepositoryConfigManager setIndexEnabled(boolean enabled) {
        checkSession();
        getModel().setIndexEnabled(enabled, session);
        return this;
    }

    @Override
    public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
        checkSession();
        getModel().setMirrorEnabled(repoName, enabled, session);
        return this;
    }

    @Override
    public int getDeployWeight() {
        checkSession();
        return getModel().getDeployWeight(session);
    }

    @Override
    public boolean isEnabled() {
//        checkSession();
        return getModel().isEnabled(session);
    }

    @Override
    public NutsRepositoryConfigManager setEnabled(boolean enabled) {
        checkSession();
        getModel().setEnabled(enabled, session);
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
    public boolean isSupportedMirroring() {
        checkSession();
        return getModel().isSupportedMirroring(session);
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
    public List<NutsRepository> getMirrors() {
        checkSession();
        return getModel().getMirrors(session);
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
    public NutsRepository addMirror(NutsAddRepositoryOptions options) {
        checkSession();
        return getModel().addMirror(options, session);
    }

    @Override
    public NutsRepositoryConfigManager removeMirror(String repositoryId) {
        checkSession();
        getModel().removeMirror(repositoryId, session);
        return this;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        checkSession();
        return getModel().getStoreLocationStrategy(session);
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsRepositoryConfigManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public Map<String, String> getConfigMap(boolean inherit) {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(inherit, getSession());
    }

    @Override
    public String getConfigProperty(String key, String defaultValue, boolean inherit) {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
        return model.get(key, defaultValue, inherit, getSession());
    }

    @Override
    public Map<String, String> getConfigMap() {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(getSession());
    }

    @Override
    public String getConfigProperty(String property, String defaultValue) {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
        return model.get(property, defaultValue, getSession());
    }

    @Override
    public NutsRepositoryConfigManager setConfigProperty(String property, String value) {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
        model.set(property, value, session);
        return this;
    }

    @Override
    public NutsRepositoryConfigModel getModel() {
        return model;
    }

}
