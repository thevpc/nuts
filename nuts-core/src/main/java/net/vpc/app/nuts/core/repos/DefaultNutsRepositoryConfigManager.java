package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class DefaultNutsRepositoryConfigManager implements NutsRepositoryConfigManager {
    private static final Logger log = Logger.getLogger(DefaultNutsRepositoryConfigManager.class.getName());

    private final AbstractNutsRepository repository;
    private final int speed;
    private final String storeLocation;
    private NutsRepositoryConfig config;
    private final Map<String, NutsRepositoryRef> configMirrors = new LinkedHashMap<>();
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;

    public DefaultNutsRepositoryConfigManager(AbstractNutsRepository repository, String storeLocation, NutsRepositoryConfig config, int speed) {
        this.repository = repository;
        this.storeLocation = storeLocation;
        this.speed = speed;
        if(storeLocation.contains("system-ref/system-ref")){
            System.out.print("==================================================");
        }
        setConfig(config);
    }

    @Override
    public String getEnv(String key, String defaultValue, boolean inherit) {
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().getProperty(defaultValue);
        }
        if (!StringUtils.isEmpty(t)) {
            return t;
        }
        t = repository.getWorkspace().getConfigManager().getEnv(key, null);
        if (!StringUtils.isEmpty(t)) {
            return t;
        }
        return defaultValue;
    }

    @Override
    public Properties getEnv(boolean inherit) {
        Properties p = new Properties();
        if (inherit) {
            p.putAll(repository.getWorkspace().getConfigManager().getEnv());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }

    @Override
    public void setEnv(String property, String value) {
        if (StringUtils.isEmpty(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged();
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new Properties());
            }
            if (!value.equals(config.getEnv().getProperty(property))) {
                config.getEnv().setProperty(property, value);
                fireConfigurationChanged();
            }
        }
    }


    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public String getType() {
        return config.getType();
    }

    @Override
    public String getGroups() {
        return config.getGroups();
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getLocation() {
        return config.getLocation();
    }

    @Override
    public String getLocation(boolean expand) {
        String s = config.getLocation();
        if (s != null && expand) {
            s = repository.getWorkspace().getIOManager().expandPath(s);
        }
        return s;
    }

    @Override
    public String getStoreLocation() {
        return storeLocation;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType) {
        String n = "";
        switch (folderType) {
            case PROGRAMS: {
                n = config.getProgramsStoreLocation();
                break;
            }
            case TEMP: {
                n = config.getTempStoreLocation();
                break;
            }
            case CACHE: {
                n = config.getCacheStoreLocation();
                break;
            }
            case CONFIG: {
                n = config.getConfigStoreLocation();
                break;
            }
            case LOGS: {
                n = config.getLogsStoreLocation();
                break;
            }
            case VAR: {
                n = config.getVarStoreLocation();
                break;
            }
            case LIB: {
                n = config.getLibStoreLocation();
                break;
            }
        }
        NutsStoreLocationStrategy strategy = config.getStoreLocationStrategy();
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.values()[0];
        }
        switch (strategy) {
            case STANDALONE: {
                if (StringUtils.isEmpty(n)) {
                    n = folderType.toString().toLowerCase();
                }
                n = n.trim();
                return FileUtils.getAbsoluteFile(new File(getStoreLocation(), n)).getPath();
            }
            case EXPLODED: {
                String storeLocation = repository.getWorkspace().getConfigManager().getStoreLocation(folderType);
                //uuid is added as
                return storeLocation
                        + File.separator + NutsConstants.FOLDER_NAME_REPOSITORIES
                        + File.separator + getName()
                        + File.separator + getUuid() //added uuid discriminator
                        ;
            }
        }
        throw new NutsIllegalArgumentException("Unsupported strategy type " + strategy);
    }

    public String getUuid() {
        return config.getUuid();
    }

    public void setConfig(NutsRepositoryConfig newConfig) {
        this.config = newConfig;
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        configMirrors.clear();
        if (config.getMirrors() != null) {
            for (NutsRepositoryRef repo : config.getMirrors()) {
                configMirrors.put(repo.getName(), repo);
            }
        }
        fireConfigurationChanged();
    }

    @Override
    public void setIndexEnabled(boolean enabled) {
        if (enabled != config.isIndexEnabled()) {
            config.setIndexEnabled(enabled);
            fireConfigurationChanged();
        }
    }

    @Override
    public boolean isIndexEnabled() {
        return config.isIndexEnabled();
    }

    @Override
    public void setUser(NutsUserConfig user) {
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged();
    }

    @Override
    public void removeUser(String userId) {
        if (configUsers.containsKey(userId)) {
            configUsers.remove(userId);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsUserConfig getUser(String userId) {
        NutsUserConfig u = configUsers.get(userId);
        if (u == null) {
            if (NutsConstants.USER_ADMIN.equals(userId) || NutsConstants.USER_ANONYMOUS.equals(userId)) {
                u = new NutsUserConfig(userId, null, null, null, null);
                configUsers.put(userId, u);
                fireConfigurationChanged();
            }
        }
        return u;
    }

    @Override
    public NutsUserConfig[] getUsers() {
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    @Override
    public void removeMirror(String repositoryId) {
        if (configMirrors.remove(repositoryId) != null) {
            fireConfigurationChanged();
        }
    }

    @Override
    public void addMirror(NutsRepositoryRef c) {
        String mirrorName = c.getName();
        if (!CoreNutsUtils.isValidIdentifier(mirrorName)) {
            throw new NutsInvalidRepositoryException(mirrorName, "Invalid repository name : " + mirrorName);
        }
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, StringUtils.alignLeft(getName(), 20) + " add repo " + mirrorName);
        }
        if (configMirrors.containsKey(c.getName())) {
            throw new NutsIllegalArgumentException("Mirror with same name already exists : " + c.getName());
        }
        configMirrors.put(c.getName(), c);

        fireConfigurationChanged();
    }


    @Override
    public NutsRepositoryRef getMirror(String name) {
        return configMirrors.get(name);
    }

    @Override
    public void setMirrorEnabled(String repoName, boolean enabled) {
        NutsRepositoryRef e = getMirror(repoName);
        if (e != null && e.isEnabled() != enabled) {
            e.setEnabled(enabled);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsRepositoryRef[] getMirrors() {
        return configMirrors.values().toArray(new NutsRepositoryRef[0]);
    }


    @Override
    public boolean save(boolean force) {
        if (force || (!repository.getWorkspace().getConfigManager().isReadOnly() && isConfigurationChanged())) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public void save() {
        CoreNutsUtils.checkReadOnly(repository.getWorkspace());
        repository.getSecurityManager().checkAllowed(NutsConstants.RIGHT_SAVE_REPOSITORY);
        File file = CoreIOUtils.createFile(getStoreLocation(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean created = false;
        if (!file.exists()) {
            created = true;
        }
        new File(getStoreLocation()).mkdirs();
        if (config.getEnv() != null && config.getEnv().isEmpty()) {
            config.setEnv(null);
        }
        config.setMirrors(configMirrors.isEmpty() ? null : new ArrayList<>(configMirrors.values()));
        config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
        repository.getWorkspace().getIOManager().writeJson(config, file, true);
        configurationChanged = false;
        if (log.isLoggable(Level.CONFIG)) {
            if (created) {
                log.log(Level.CONFIG, StringUtils.alignLeft(repository.getName(), 20) + " Created repository " + repository.getName() + " at " + getStoreLocation());
            } else {
                log.log(Level.CONFIG, StringUtils.alignLeft(repository.getName(), 20) + " Updated repository " + repository.getName() + " at " + getStoreLocation());
            }
        }
    }

    @Override
    public Properties getEnv() {
        return getEnv(true);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        return getEnv(property, defaultValue, true);
    }

    private void fireConfigurationChanged() {
        setConfigurationChanged(true);
    }

    public NutsRepositoryConfigManager setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
        return this;
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

}
