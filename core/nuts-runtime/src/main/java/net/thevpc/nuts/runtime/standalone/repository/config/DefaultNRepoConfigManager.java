package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

public class DefaultNRepoConfigManager implements NRepositoryConfigManager, NRepositoryConfigManagerExt {

    private final NRepositoryConfigModel model;
    private NSession session;

    public DefaultNRepoConfigManager(NRepositoryConfigModel model) {
        this.model = model;
    }

    private void checkSession() {
        NSessionUtils.checkSession(getModel().getWorkspace(), session);
    }

    @Override
    public String getGlobalName() {
        checkSession();
        return getModel().getGlobalName(session);
    }

    public NRepositoryRef getRepositoryRef() {
        checkSession();
        return getModel().getRepositoryRef(session);
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
    public NSpeedQualifier getSpeed() {
        checkSession();
        return getModel().getSpeed(session);
    }

    @Override
    public boolean isTemporary() {
        checkSession();
        return getModel().isTemporary(session);
    }

    @Override
    public NRepositoryConfigManager setTemporary(boolean enabled) {
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
    public NRepositoryLocation getLocation() {
        checkSession();
        return getModel().getLocation(session);
    }

    @Override
    public NPath getLocationPath() {
        checkSession();
        return getModel().getLocationPath(session);
    }

    @Override
    public NPath getStoreLocation() {
        checkSession();
        return getModel().getStoreLocation();
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        checkSession();
        return getModel().getStoreLocation(folderType, session);
    }

    @Override
    public boolean isIndexEnabled() {
        checkSession();
        return getModel().isIndexEnabled(session);
    }

    @Override
    public NRepositoryConfigManager setIndexEnabled(boolean enabled) {
        checkSession();
        getModel().setIndexEnabled(enabled, session);
        return this;
    }

    @Override
    public NRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
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
        checkSession();
        return getModel().isEnabled(session);
    }

    @Override
    public NRepositoryConfigManager setEnabled(boolean enabled) {
        checkSession();
        getModel().setEnabled(enabled, session);
        return this;
    }

    @Override
    public NRepositoryConfigManager subscribeIndex() {
        checkSession();
        getModel().subscribeIndex(session);
        return this;
    }

    @Override
    public NRepositoryConfigManager unsubscribeIndex() {
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
    public NRepository findMirrorById(String repositoryNameOrId) {
        checkSession();
        return getModel().findMirrorById(repositoryNameOrId, session);
    }

    @Override
    public NRepository findMirrorByName(String repositoryName) {
        checkSession();
        return getModel().findMirrorById(repositoryName, session);
    }

    @Override
    public List<NRepository> getMirrors() {
        checkSession();
        return getModel().getMirrors(session);
    }

    @Override
    public NRepository getMirror(String repositoryIdOrName) {
        checkSession();
        return getModel().getMirror(repositoryIdOrName, session);
    }

    @Override
    public NRepository findMirror(String repositoryIdOrName) {
        checkSession();
        return getModel().findMirror(repositoryIdOrName, session);
    }

    @Override
    public NRepository addMirror(NAddRepositoryOptions options) {
        checkSession();
        return getModel().addMirror(options, session);
    }

    @Override
    public NRepositoryConfigManager removeMirror(String repositoryId) {
        checkSession();
        getModel().removeMirror(repositoryId, session);
        return this;
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        checkSession();
        return getModel().getStoreStrategy(session);
    }

    public NSession getSession() {
        return session;
    }

    public NRepositoryConfigManager setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public Map<String, String> getConfigMap(boolean inherit) {
        NSessionUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(inherit, getSession());
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String key, boolean inherit) {
        NSessionUtils.checkSession(model.getWorkspace(), session);
        return model.get(key, inherit, getSession());
    }
    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        NSessionUtils.checkSession(model.getWorkspace(), session);
        return getConfigProperty(property, true);
    }

    @Override
    public Map<String, String> getConfigMap() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(getSession());
    }


    @Override
    public NRepositoryConfigManager setConfigProperty(String property, String value) {
        NSessionUtils.checkSession(model.getWorkspace(), session);
        model.set(property, value, session);
        return this;
    }

    @Override
    public NRepositoryConfigModel getModel() {
        return model;
    }

}
