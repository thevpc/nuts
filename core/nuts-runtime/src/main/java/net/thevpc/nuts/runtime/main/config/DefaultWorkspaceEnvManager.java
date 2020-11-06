package net.thevpc.nuts.runtime.main.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultWorkspaceEnvManager implements NutsWorkspaceEnvManager {
    private NutsWorkspace ws;

    public DefaultWorkspaceEnvManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    NutsWorkspaceConfigMain getStoreModelMain(){
        return ((DefaultNutsWorkspaceConfigManager)ws.config()).getStoreModelMain();
    }
    @Override
    public Map<String, String> toMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
        return p;
    }

    @Override
    public String get(String property) {
        return get(property,null);
    }

    @Override
    public String get(String property, String defaultValue) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env == null) {
            return defaultValue;
        }
        String o = env.get(property);
        if (CoreStringUtils.isBlank(o)) {
            return defaultValue;
        }
        return o;
    }

    @Override
    public void set(String property, String value, NutsUpdateOptions options) {
        Map<String, String> env = getStoreModelMain().getEnv();
        options = CoreNutsUtils.validate(options, ws);
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        }
    }

    @Override
    public NutsOsFamily getOsFamily() {
        return current().getOsFamily();
    }

    private DefaultNutsWorkspaceCurrentConfig current() {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).current();
    }

    @Override
    public NutsId getPlatform() {
        return current().getPlatform();
    }

    @Override
    public NutsId getOs() {
        return current().getOs();
    }

    @Override
    public NutsId getOsDist() {
        return current().getOsDist();
    }

    @Override
    public NutsId getArch() {
        return current().getArch();
    }


}
