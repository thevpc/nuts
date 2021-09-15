package net.thevpc.nuts.runtime.standalone.repos;

import java.util.LinkedHashMap;

import net.thevpc.nuts.*;

import java.util.Map;

import net.thevpc.nuts.runtime.core.repos.NutsRepositoryConfigModel;

public class DefaultNutsRepositoryEnvModel {
    private NutsRepository repository;

    public DefaultNutsRepositoryEnvModel(NutsRepository repo) {
        this.repository = repo;
    }

    public NutsWorkspace getWorkspace() {
        return repository.getWorkspace();
    }
    
    public NutsRepository getRepository() {
        return repository;
    }
    
    

    
    public Map<String, String> toMap(boolean inherit,NutsSession session) {
        return config_getEnv(inherit, session);
    }
    
    public Map<String, String> toMap(NutsSession session) {
        return config_getEnv(true, session);
    }

    
    public String get(String key, String defaultValue, boolean inherit,NutsSession session) {
        return config_getEnv(key, defaultValue, inherit,session);
    }

    

    
    public String get(String property, String defaultValue,NutsSession session) {
        return config_getEnv(property, defaultValue,true,session);
    }

    
    public void set(String property, String value,NutsSession session) {
        config_setEnv(property, value, session);
    }

    private NutsRepositoryConfigModel getConfig0() {
        return ((DefaultNutsRepoConfigManager) repository.config()).getModel();
    }
    
    
    ////////////////////////////////////////////
    
    
    public String config_getEnv(String key, String defaultValue, boolean inherit,NutsSession session) {
        NutsRepositoryConfigModel model = ((DefaultNutsRepoConfigManager) repository.config()).getModel();
        NutsRepositoryConfig config = model.getConfig(session);
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().get(defaultValue);
        }
        if (!NutsUtilStrings.isBlank(t)) {
            return t;
        }
        if (inherit) {
            t = repository.getWorkspace().env().getEnv(key).getString();
            if (!NutsUtilStrings.isBlank(t)) {
                return t;
            }
        }
        return defaultValue;
    }

    private Map<String, String> config_getEnv(boolean inherit,NutsSession session) {
        NutsRepositoryConfigModel model = ((DefaultNutsRepoConfigManager) repository.config()).getModel();
        NutsRepositoryConfig config = model.getConfig(session);
        Map<String, String> p = new LinkedHashMap<>();
        if (inherit) {
            p.putAll(repository.getWorkspace().env().getEnvMap());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }


    private void config_setEnv(String property, String value, NutsSession session) {
        NutsRepositoryConfigModel model = ((DefaultNutsRepoConfigManager) repository.config()).getModel();
        NutsRepositoryConfig config = model.getConfig(session);
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NutsUtilStrings.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                model.fireConfigurationChanged("env", session);
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                model.fireConfigurationChanged("env", session);
            }
        }
    }
    
    
}
