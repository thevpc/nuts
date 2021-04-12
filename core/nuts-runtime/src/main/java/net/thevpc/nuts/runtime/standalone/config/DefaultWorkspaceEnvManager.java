package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import java.util.Map;
import java.util.function.Supplier;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultWorkspaceEnvManager implements NutsWorkspaceEnvManager {

    private DefaultWorkspaceEnvManagerModel model;
    private NutsSession session;

    public DefaultWorkspaceEnvManager(DefaultWorkspaceEnvManagerModel model) {
        this.model = model;
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        checkSession();
        return model.getStoreModelMain();
    }

    @Override
    public Map<String, String> getEnvMap() {
        checkSession();
        return model.getEnvMap();
    }

    @Override
    public String getOption(String property) {
        checkSession();
        return model.getOption(property);
    }

    @Override
    public String getEnv(String property) {
        checkSession();
        return model.getOption(property);
    }

    @Override
    public Integer getEnvAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getEnvAsInt(property, defaultValue);
    }

    @Override
    public Integer getOptionAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getOptionAsInt(property, defaultValue);
    }

    @Override
    public Boolean getEnvAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getEnvAsBoolean(property, defaultValue);
    }

    @Override
    public Boolean getOptionAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getOptionAsBoolean(property, defaultValue);
    }

    @Override
    public String getOption(String property, String defaultValue) {
        checkSession();
        return model.getOption(property, defaultValue);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        checkSession();
        return model.getEnv(property, defaultValue);
    }

    @Override
    public NutsWorkspaceEnvManager setEnv(String property, String value) {
        checkSession();
        model.setEnv(property, value, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsId getArch() {
//        checkSession();
        return model.getArch();
    }

    @Override
    public NutsArchFamily getArchFamily() {
//        checkSession();
        return model.getArchFamily();
    }

    @Override
    public NutsOsFamily getOsFamily() {
//        checkSession();
        return model.getOsFamily();
    }

    @Override
    public NutsId getOs() {
//        checkSession();
        return model.getOs();
    }

    @Override
    public NutsId getPlatform() {
//        checkSession();
        return model.getPlatform(session);
    }

    public NutsId getOsDist() {
//        checkSession();
        return model.getOsDist();
    }

    @Override
    public Map<String, Object> getProperties() {
        checkSession();
        return model.getProperties();
    }

    @Override
    public Object getProperty(String property, Object defaultValue) {
        checkSession();
        return model.getProperty(property, defaultValue);
    }

    @Override
    public Integer getPropertyAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getPropertyAsInt(property, defaultValue);
    }

    @Override
    public String getPropertyAsString(String property, String defaultValue) {
        checkSession();
        return model.getPropertyAsString(property, defaultValue);
    }

    @Override
    public Boolean getPropertyAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getPropertyAsBoolean(property, defaultValue);
    }

    @Override
    public Object getProperty(String property) {
        checkSession();
        return model.getProperty(property);
    }

    @Override
    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        checkSession();
        return model.getOrCreateProperty(property, supplier);
    }

    @Override
    public <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        checkSession();
        return model.getOrCreateProperty(property, supplier);
    }

    @Override
    public NutsWorkspaceEnvManager setProperty(String property, Object value) {
        checkSession();
        model.setProperty(property, value);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceEnvManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public DefaultWorkspaceEnvManagerModel getModel() {
        return model;
    }

}
