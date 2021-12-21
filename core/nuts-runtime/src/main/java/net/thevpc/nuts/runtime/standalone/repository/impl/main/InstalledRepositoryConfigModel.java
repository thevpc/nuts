package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.config.NutsRepositoryConfigModel;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

import java.util.HashMap;
import java.util.Map;

class InstalledRepositoryConfigModel implements NutsRepositoryConfigModel {

    private final NutsWorkspace ws;
    private final NutsRepository repo;

    public InstalledRepositoryConfigModel(NutsWorkspace ws, NutsRepository repo) {
        this.ws = ws;
        this.repo = repo;
    }

    @Override
    public boolean save(boolean force, NutsSession session) {
        return false;
    }

    @Override
    public NutsRepository getRepository() {
        return repo;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public void addMirror(NutsRepository repo, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : addMirror"));
    }

    @Override
    public NutsRepository addMirror(NutsAddRepositoryOptions options, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : addMirror"));
    }

    @Override
    public NutsRepository findMirror(String repositoryIdOrName, NutsSession session) {
        return null;
    }

    @Override
    public NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session) {
        return null;
    }

    @Override
    public NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session) {
        return null;
    }

    @Override
    public int getDeployWeight(NutsSession session) {
        return -1;
    }

    @Override
    public String getGlobalName(NutsSession session) {
        return DefaultNutsInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public String getGroups(NutsSession session) {
        return null;
    }

    @Override
    public NutsPath getLocation(boolean expand, NutsSession session) {
        return null;
    }

    @Override
    public NutsRepository getMirror(String repositoryIdOrName, NutsSession session) {
        return null;
    }

    @Override
    public NutsRepository[] getMirrors(NutsSession session) {
        return new NutsRepository[0];
    }

    @Override
    public String getName() {
        return DefaultNutsInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public NutsRepositoryRef getRepositoryRef(NutsSession session) {
        return null;
    }

    @Override
    public NutsSpeedQualifier getSpeed(NutsSession session) {
        return NutsSpeedQualifier.UNAVAILABLE;
    }

    @Override
    public NutsPath getStoreLocation() {
        return null;
    }

    @Override
    public NutsPath getStoreLocation(NutsStoreLocation folderType, NutsSession session) {
        return null;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy(NutsSession session) {
        return session.locations().getRepositoryStoreLocationStrategy();
    }

    @Override
    public String getType(NutsSession session) {
        return DefaultNutsInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public String getUuid() {
        return DefaultNutsInstalledRepository.INSTALLED_REPO_UUID;
    }

    @Override
    public NutsRepositoryLocation getLocation() {
        return NutsRepositoryLocation.of(DefaultNutsInstalledRepository.INSTALLED_REPO_UUID);
    }

    @Override
    public boolean isEnabled(NutsSession session) {
        return false;
    }

    @Override
    public boolean isIndexEnabled(NutsSession session) {
        return false;
    }

    @Override
    public boolean isIndexSubscribed(NutsSession session) {
        return false;
    }

    @Override
    public boolean isSupportedMirroring(NutsSession session) {
        return false;
    }

    //        @Override
//        public void setEnv(String property, String value, NutsUpdateOptions options) {
//            //
//        }
    @Override
    public boolean isTemporary(NutsSession session) {
        return false;
    }

    @Override
    public void removeMirror(String repositoryId, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : removeMirror"));
    }

    @Override
    public void setEnabled(boolean enabled, NutsSession options) {
    }

    //        @Override
//        public Map<String, String> getEnv(boolean inherit) {
//            return Collections.emptyMap();
//        }
//
//        @Override
//        public String getEnv(String key, String defaultValue, boolean inherit) {
//            return null;
//        }
    @Override
    public void setIndexEnabled(boolean enabled, NutsSession session) {
    }

    @Override
    public void setMirrorEnabled(String repoName, boolean enabled, NutsSession session) {
    }

    @Override
    public void setTemporary(boolean enabled, NutsSession options) {

    }

    @Override
    public void subscribeIndex(NutsSession session) {
    }

    @Override
    public void unsubscribeIndex(NutsSession session) {
    }

    @Override
    public NutsPath getTempMirrorsRoot(NutsSession session) {
        return null;
    }

    @Override
    public NutsPath getMirrorsRoot(NutsSession session) {
        return null;
    }

    @Override
    public NutsUserConfig[] getUsers(NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getUsers"));
    }

    @Override
    public NutsUserConfig getUser(String userId, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getUser"));
    }

    @Override
    public NutsRepositoryConfig getStoredConfig(NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getStoredConfig"));
    }

    @Override
    public void fireConfigurationChanged(String configName, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : fireConfigurationChanged"));
    }

    @Override
    public void setUser(NutsUserConfig user, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : setUser"));
    }

    @Override
    public void removeUser(String userId, NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : removeUser"));
    }

    @Override
    public NutsRepositoryConfig getConfig(NutsSession session) {
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("not supported : getConfig"));
    }

    @Override
    public Map<String, String> toMap(boolean inherit, NutsSession session) {
        if (inherit) {
            return session.config().getConfigMap();
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, String> toMap(NutsSession session) {
        return new HashMap<>();
    }

    @Override
    public String get(String key, String defaultValue, boolean inherit, NutsSession session) {
        if (inherit) {
            return session.config().getConfigProperty(key).getString(defaultValue);
        }
        return null;
    }

    @Override
    public String get(String property, String defaultValue, NutsSession session) {
        return defaultValue;
    }

    @Override
    public void set(String property, String value, NutsSession session) {

    }
}
