package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.config.AbstractNRepositoryConfigModel;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InstalledRepositoryConfigModel extends AbstractNRepositoryConfigModel {

    private final NWorkspace ws;
    private final NRepository repo;

    public InstalledRepositoryConfigModel(NWorkspace ws, NRepository repo) {
        this.ws = ws;
        this.repo = repo;
    }

    @Override
    public boolean save(boolean force) {
        return false;
    }

    @Override
    public NRepository getRepository() {
        return repo;
    }

    @Override
    public NWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public void addMirror(NRepository repo) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : addMirror"));
    }

    @Override
    public NRepository addMirror(NAddRepositoryOptions options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : addMirror"));
    }

    @Override
    public NRepository findMirror(String repositoryIdOrName) {
        return null;
    }

    @Override
    public NRepository findMirrorById(String repositoryNameOrId) {
        return null;
    }

    @Override
    public NRepository findMirrorByName(String repositoryNameOrId) {
        return null;
    }

    @Override
    public int getDeployWeight() {
        return -1;
    }

    @Override
    public String getGlobalName() {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public String getGroups() {
        return null;
    }

    @Override
    public NPath getLocationPath() {
        return null;
    }

    @Override
    public NRepository getMirror(String repositoryIdOrName) {
        return null;
    }

    @Override
    public List<NRepository> getMirrors() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public NRepositoryRef getRepositoryRef() {
        return null;
    }

    @Override
    public NSpeedQualifier getSpeed() {
        return NSpeedQualifier.UNAVAILABLE;
    }

    @Override
    public NPath getStoreLocation() {
        return null;
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        return null;
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        return NLocations.of().getRepositoryStoreStrategy();
    }

    @Override
    public String getType() {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public String getUuid() {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public NRepositoryLocation getLocation() {
        return NRepositoryLocation.of(DefaultNInstalledRepository.INSTALLED_REPO_UUID);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isIndexEnabled() {
        return false;
    }

    @Override
    public boolean isPreview() {
        return false;
    }

    @Override
    public boolean isIndexSubscribed() {
        return false;
    }

    @Override
    public boolean isSupportedMirroring() {
        return false;
    }

    //        @Override
//        public void setEnv(String property, String value, NutsUpdateOptions options) {
//            //
//        }
    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public void removeMirror(String repositoryId) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : removeMirror"));
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void setIndexEnabled(boolean enabled) {
    }

    @Override
    public void setMirrorEnabled(String repoName, boolean enabled) {
    }

    @Override
    public void setTemporary(boolean enabled) {

    }

    @Override
    public void subscribeIndex() {
    }

    @Override
    public void unsubscribeIndex() {
    }

    @Override
    public NPath getTempMirrorsRoot() {
        return null;
    }

    @Override
    public NPath getMirrorsRoot() {
        return null;
    }

    @Override
    public NUserConfig[] findUsers() {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : findUsers"));
    }

    @Override
    public NOptional<NUserConfig> findUser(String userId) {
        return NOptional.ofError(()->NMsg.ofPlain("not supported : findUser"));
    }

    @Override
    public NRepositoryConfig getStoredConfig() {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : getStoredConfig"));
    }

    @Override
    public void fireConfigurationChanged(String configName) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : fireConfigurationChanged"));
    }

    @Override
    public void setUser(NUserConfig user) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : setUser"));
    }

    @Override
    public void removeUser(String userId) {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : removeUser"));
    }

    @Override
    public NRepositoryConfig getConfig() {
        throw new NIllegalArgumentException(NMsg.ofPlain("not supported : getConfig"));
    }

    @Override
    public Map<String, String> toMap(boolean inherit) {
        if (inherit) {
            return NConfigs.of().getConfigMap();
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> toMap() {
        return new HashMap<>();
    }

    @Override
    public NOptional<NLiteral> get(String key, boolean inherit) {
        NOptional<NLiteral> o = NOptional.ofEmpty(() -> NMsg.ofC("repo config property not found : %s", key));
        if (inherit) {
            return o.orElseUse(()->NConfigs.of().getConfigProperty(key));
        }
        return o;
    }


    @Override
    public void set(String property, String value) {

    }
}
