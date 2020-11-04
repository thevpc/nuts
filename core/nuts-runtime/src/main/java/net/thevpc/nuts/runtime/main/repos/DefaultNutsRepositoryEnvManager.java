package net.thevpc.nuts.runtime.main.repos;

import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryEnvManager;
import net.thevpc.nuts.NutsUpdateOptions;

import java.util.Map;

public class DefaultNutsRepositoryEnvManager implements NutsRepositoryEnvManager {
    private NutsRepository repo;

    public DefaultNutsRepositoryEnvManager(NutsRepository repo) {
        this.repo = repo;
    }

    @Override
    public Map<String, String> toMap(boolean inherit) {
        return getConfig0().getEnv(inherit);
    }

    @Override
    public String get(String key, String defaultValue, boolean inherit) {
        return getConfig0().getEnv(key, defaultValue, inherit);
    }

    @Override
    public Map<String, String> toMap() {
        return getConfig0().getEnv(true);
    }

    @Override
    public String get(String property, String defaultValue) {
        return getConfig0().getEnv(property, defaultValue,true);
    }

    @Override
    public void set(String property, String value, NutsUpdateOptions options) {
        getConfig0().setEnv(property, value, options);
    }

    private DefaultNutsRepoConfigManager getConfig0() {
        return (DefaultNutsRepoConfigManager) repo.config();
    }
}
