package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

class DefaultNutsRepositoryConfigManager implements NutsRepositoryConfigManager {

    private AbstractNutsRepository abstractNutsRepository;
    private int speed;
    private String locationFolder;
    private NutsRepositoryConfig config;
    private static final Logger log = Logger.getLogger(DefaultNutsRepositoryConfigManager.class.getName());

    public DefaultNutsRepositoryConfigManager(AbstractNutsRepository abstractNutsRepository, String locationFolder, NutsRepositoryConfig config, int speed) {
        this.abstractNutsRepository = abstractNutsRepository;
        this.locationFolder = locationFolder;
        this.config = config;
        this.speed = speed;
    }

    @Override
    public String getEnv(String key, String defaultValue, boolean inherit) {
        String t = getConfig().getEnv(key, null);
        if (!StringUtils.isEmpty(t)) {
            return t;
        }
        t = abstractNutsRepository.getWorkspace().getConfigManager().getEnv(key, null);
        if (!StringUtils.isEmpty(t)) {
            return t;
        }
        return defaultValue;
    }

    @Override
    public Properties getEnv(boolean inherit) {
        Properties p = new Properties();
        if (inherit) {
            p.putAll(abstractNutsRepository.getWorkspace().getConfigManager().getEnv());
        }
        p.putAll(getConfig().getEnv());
        return p;
    }

    @Override
    public void setEnv(String property, String value) {
        getConfig().setEnv(property, value);
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public String getType() {
        return getConfig().getType();
    }

    @Override
    public String getGroups() {
        return getConfig().getGroups();
    }

    @Override
    public String getId() {
        return getConfig().getId();
    }

    @Override
    public String getLocation() {
        return getConfig().getLocation();
    }

    @Override
    public String getComponentsLocation() {
        return getConfig().getComponentsLocation();
    }
    @Override
    public void setComponentsLocation(String location) {
        getConfig().setComponentsLocation(location);
    }


    //@Override
    public NutsRepositoryConfig getConfig() {
        return config;
    }

    public String getLocationFolder() {
        return locationFolder;
    }

    public void setConfig(NutsRepositoryConfig newConfig) {
        this.config = newConfig;
    }

    @Override
    public void setUser(NutsUserConfig user) {
        getConfig().setUser(user);
    }

    @Override
    public void removeUser(String userId) {
        getConfig().removeUser(userId);
    }

    @Override
    public NutsUserConfig getUser(String userId) {
        NutsUserConfig u = getConfig().getUser(userId);
        if (u == null) {
            if (NutsConstants.USER_ADMIN.equals(userId) || NutsConstants.USER_ANONYMOUS.equals(userId)) {
                u = new NutsUserConfig(userId, null, null, null, null);
                getConfig().setUser(u);
            }
        }
        return u;
    }

    @Override
    public NutsUserConfig[] getUsers() {
        return getConfig().getUsers();
    }

    public void removeMirror(String repositoryId) {
        getConfig().removeMirror(repositoryId);
    }


    public void addMirror(NutsRepositoryLocation c) {
        if (c != null) {
            getConfig().addMirror(c);
        }
    }


    public NutsRepositoryLocation getMirror(String id) {
        return getConfig().getMirror(id);
    }


    public NutsRepositoryLocation[] getMirrors() {
        return getConfig().getMirrors();
    }


    @Override
    public boolean save() {
        File file = CoreIOUtils.createFile(getLocationFolder(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean created = false;
        if (!file.exists()) {
            created = true;
        }
        boolean saved = false;
        new File(getLocationFolder()).mkdirs();
        try {
            CoreJsonUtils.storeJson(getConfig(), file, true);
            saved = true;
        } catch (NutsIOException ex) {
            //unable to store;
        }
        if(log.isLoggable(Level.CONFIG)) {
            if (created) {
                log.log(Level.CONFIG, StringUtils.alignLeft(abstractNutsRepository.getRepositoryId(), 20) + " Created repository " + abstractNutsRepository.getRepositoryId() + " at " + getLocationFolder());
            } else {
                log.log(Level.CONFIG, StringUtils.alignLeft(abstractNutsRepository.getRepositoryId(), 20) + " Updated repository " + abstractNutsRepository.getRepositoryId() + " at " + getLocationFolder());
            }
        }
        return saved;
    }

    @Override
    public Properties getEnv() {
        return getEnv(true);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        return getEnv(property,defaultValue,true);
    }
}
