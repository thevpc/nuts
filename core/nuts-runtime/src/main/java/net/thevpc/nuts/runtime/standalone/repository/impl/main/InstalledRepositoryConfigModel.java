package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.config.NRepositoryConfigModel;
import net.thevpc.nuts.spi.NRepositoryLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InstalledRepositoryConfigModel implements NRepositoryConfigModel {

    private final NWorkspace ws;
    private final NRepository repo;

    public InstalledRepositoryConfigModel(NWorkspace ws, NRepository repo) {
        this.ws = ws;
        this.repo = repo;
    }

    @Override
    public boolean save(boolean force, NSession session) {
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
    public void addMirror(NRepository repo, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : addMirror"));
    }

    @Override
    public NRepository addMirror(NAddRepositoryOptions options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : addMirror"));
    }

    @Override
    public NRepository findMirror(String repositoryIdOrName, NSession session) {
        return null;
    }

    @Override
    public NRepository findMirrorById(String repositoryNameOrId, NSession session) {
        return null;
    }

    @Override
    public NRepository findMirrorByName(String repositoryNameOrId, NSession session) {
        return null;
    }

    @Override
    public int getDeployWeight(NSession session) {
        return -1;
    }

    @Override
    public String getGlobalName(NSession session) {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public String getGroups(NSession session) {
        return null;
    }

    @Override
    public NPath getLocationPath(NSession session) {
        return null;
    }

    @Override
    public NRepositoryLocation getLocation(NSession session) {
        return null;
    }
    

    @Override
    public NRepository getMirror(String repositoryIdOrName, NSession session) {
        return null;
    }

    @Override
    public List<NRepository> getMirrors(NSession session) {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return DefaultNInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public NRepositoryRef getRepositoryRef(NSession session) {
        return null;
    }

    @Override
    public NSpeedQualifier getSpeed(NSession session) {
        return NSpeedQualifier.UNAVAILABLE;
    }

    @Override
    public NPath getStoreLocation() {
        return null;
    }

    @Override
    public NPath getStoreLocation(NStoreLocation folderType, NSession session) {
        return null;
    }

    @Override
    public NStoreLocationStrategy getStoreLocationStrategy(NSession session) {
        return NLocations.of(session).getRepositoryStoreLocationStrategy();
    }

    @Override
    public String getType(NSession session) {
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
    public boolean isEnabled(NSession session) {
        return false;
    }

    @Override
    public boolean isIndexEnabled(NSession session) {
        return false;
    }

    @Override
    public boolean isIndexSubscribed(NSession session) {
        return false;
    }

    @Override
    public boolean isSupportedMirroring(NSession session) {
        return false;
    }

    //        @Override
//        public void setEnv(String property, String value, NutsUpdateOptions options) {
//            //
//        }
    @Override
    public boolean isTemporary(NSession session) {
        return false;
    }

    @Override
    public void removeMirror(String repositoryId, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : removeMirror"));
    }

    @Override
    public void setEnabled(boolean enabled, NSession options) {
    }

    @Override
    public void setIndexEnabled(boolean enabled, NSession session) {
    }

    @Override
    public void setMirrorEnabled(String repoName, boolean enabled, NSession session) {
    }

    @Override
    public void setTemporary(boolean enabled, NSession options) {

    }

    @Override
    public void subscribeIndex(NSession session) {
    }

    @Override
    public void unsubscribeIndex(NSession session) {
    }

    @Override
    public NPath getTempMirrorsRoot(NSession session) {
        return null;
    }

    @Override
    public NPath getMirrorsRoot(NSession session) {
        return null;
    }

    @Override
    public NUserConfig[] getUsers(NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : getUsers"));
    }

    @Override
    public NUserConfig getUser(String userId, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : getUser"));
    }

    @Override
    public NRepositoryConfig getStoredConfig(NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : getStoredConfig"));
    }

    @Override
    public void fireConfigurationChanged(String configName, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : fireConfigurationChanged"));
    }

    @Override
    public void setUser(NUserConfig user, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : setUser"));
    }

    @Override
    public void removeUser(String userId, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : removeUser"));
    }

    @Override
    public NRepositoryConfig getConfig(NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not supported : getConfig"));
    }

    @Override
    public Map<String, String> toMap(boolean inherit, NSession session) {
        if (inherit) {
            return NConfigs.of(session).getConfigMap();
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> toMap(NSession session) {
        return new HashMap<>();
    }

    @Override
    public NOptional<NValue> get(String key, boolean inherit, NSession session) {
        NOptional<NValue> o = NOptional.ofEmpty(s -> NMsg.ofCstyle("repo config property not found : %s", key));
        if (inherit) {
            return o.orElseUse(()->NConfigs.of(session).getConfigProperty(key));
        }
        return o;
    }


    @Override
    public void set(String property, String value, NSession session) {

    }
}
