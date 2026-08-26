package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.core.NSpeedQualifier;
import net.thevpc.nuts.core.NStoreStrategy;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfigManager;
import net.thevpc.nuts.core.NRepositoryRef;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultNRepoConfigManager implements NRepositoryConfigManager, NRepositoryConfigManagerExt {

    private final NRepositoryConfigModel model;

    public DefaultNRepoConfigManager(NRepositoryConfigModel model) {
        this.model = model;
    }


    @Override
    public String globalName() {
        return getModel().getGlobalName();
    }

    public NRepositoryRef repositoryRef() {
        return getModel().getRepositoryRef();
    }

    @Override
    public String type() {
        return getModel().getType();
    }

    @Override
    public String groups() {
        return getModel().getGroups();
    }

    @Override
    public NSpeedQualifier speed() {
        return getModel().getSpeed();
    }

    @Override
    public boolean isTemporary() {
        return getModel().isTemporary();
    }

    @Override
    public NRepositoryConfigManager temporary(boolean enabled) {
        getModel().setTemporary(enabled);
        return this;
    }

    @Override
    public boolean isIndexSubscribed() {
        return getModel().isIndexSubscribed();
    }

    @Override
    public NRepositoryLocation location() {
        return getModel().getLocation();
    }

    @Override
    public NPath locationPath() {
        return getModel().getLocationPath();
    }

    @Override
    public NPath storeLocation() {
        return getModel().getStoreLocation();
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        return getModel().getStoreLocation(folderType);
    }

    @Override
    public boolean isIndexEnabled() {
        return getModel().isIndexEnabled();
    }

    @Override
    public boolean isPreview() {
        return getModel().isPreview();
    }

    @Override
    public Set<String> tags() {
        return getModel().getTags();
    }

    @Override
    public NRepositoryConfigManager indexEnabled(boolean enabled) {
        getModel().setIndexEnabled(enabled);
        return this;
    }

    @Override
    public NRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
        getModel().setMirrorEnabled(repoName, enabled);
        return this;
    }

    @Override
    public int deployWeight() {
        return getModel().getDeployWeight();
    }

    @Override
    public boolean isEnabled() {
        return getModel().isEnabled();
    }

    @Override
    public NRepositoryConfigManager enabled(boolean enabled) {
        getModel().setEnabled(enabled);
        return this;
    }

    @Override
    public NRepositoryConfigManager subscribeIndex() {
        getModel().subscribeIndex();
        return this;
    }

    @Override
    public NRepositoryConfigManager unsubscribeIndex() {
        getModel().unsubscribeIndex();
        return this;
    }

    @Override
    public boolean isSupportedMirroring() {
        return getModel().isSupportedMirroring();
    }

    @Override
    public NRepository findMirrorById(String repositoryNameOrId) {
        return getModel().findMirrorById(repositoryNameOrId);
    }

    @Override
    public NRepository findMirrorByName(String repositoryName) {
        return getModel().findMirrorById(repositoryName);
    }

    @Override
    public List<NRepository> mirrors() {
        return getModel().getMirrors();
    }

    @Override
    public NOptional<NRepository> getMirror(String repositoryIdOrName) {
        return getModel().getMirror(repositoryIdOrName);
    }

    @Override
    public NRepository addMirror(NRepositorySpec options) {
        return getModel().addMirror(options);
    }

    @Override
    public NRepositoryConfigManager removeMirror(String repositoryId) {
        getModel().removeMirror(repositoryId);
        return this;
    }

    @Override
    public NStoreStrategy storeStrategy() {
        return getModel().getStoreStrategy();
    }


    @Override
    public Map<String, String> getConfigMap(boolean inherit) {
        return model.toMap(inherit);
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String key, boolean inherit) {
        return model.get(key, inherit);
    }
    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        return getConfigProperty(property, true);
    }

    @Override
    public Map<String, String> configMap() {
        return model.toMap();
    }


    @Override
    public NRepositoryConfigManager setConfigProperty(String property, String value) {
        model.set(property, value);
        return this;
    }

    @Override
    public NRepositoryConfigModel getModel() {
        return model;
    }

}
