package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

class DefaultNutsRepositoryConfigManager implements NutsRepositoryConfigManager {

    private AbstractNutsRepository abstractNutsRepository;
    private int speed;
    private String storeLocation;
    private NutsRepositoryConfig config;
    private static final Logger log = Logger.getLogger(DefaultNutsRepositoryConfigManager.class.getName());

    public DefaultNutsRepositoryConfigManager(AbstractNutsRepository abstractNutsRepository, String storeLocation, NutsRepositoryConfig config, int speed) {
        this.abstractNutsRepository = abstractNutsRepository;
        this.storeLocation = storeLocation;
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
    public String getName() {
        return getConfig().getName();
    }

    @Override
    public String getLocation() {
        return getConfig().getLocation();
    }

    //@Override
    public NutsRepositoryConfig getConfig() {
        return config;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    @Override
    public String getStoreLocation(NutsStoreFolder folderType) {
        String n = "";
        switch (folderType) {
            case PROGRAMS: {
                n = getConfig().getProgramsStoreLocation();
                break;
            }
            case TEMP: {
                n = getConfig().getTempStoreLocation();
                break;
            }
            case CACHE: {
                n = getConfig().getCacheStoreLocation();
                break;
            }
            case CONFIG: {
                n = getConfig().getConfigStoreLocation();
                break;
            }
            case LOGS: {
                n = getConfig().getLogsStoreLocation();
                break;
            }
            case VAR: {
                n = getConfig().getVarStoreLocation();
                break;
            }
            case LIB: {
                n = getConfig().getLibStoreLocation();
                break;
            }
        }
        NutsStoreLocationStrategy strategy = getConfig().getStoreLocationStrategy();
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
                String storeLocation = abstractNutsRepository.getWorkspace().getConfigManager().getStoreLocation(folderType);
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
        return getConfig().getUuid();
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
        File file = CoreIOUtils.createFile(getStoreLocation(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME);
        boolean created = false;
        if (!file.exists()) {
            created = true;
        }
        boolean saved = false;
        new File(getStoreLocation()).mkdirs();
        try {
            abstractNutsRepository.getWorkspace().getIOManager().writeJson(getConfig(), file, true);
            saved = true;
        } catch (NutsIOException ex) {
            //unable to store;
        }
        if (log.isLoggable(Level.CONFIG)) {
            if (created) {
                log.log(Level.CONFIG, StringUtils.alignLeft(abstractNutsRepository.getName(), 20) + " Created repository " + abstractNutsRepository.getName() + " at " + getStoreLocation());
            } else {
                log.log(Level.CONFIG, StringUtils.alignLeft(abstractNutsRepository.getName(), 20) + " Updated repository " + abstractNutsRepository.getName() + " at " + getStoreLocation());
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
        return getEnv(property, defaultValue, true);
    }
}
