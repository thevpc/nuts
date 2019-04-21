package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsRepositoryConfigManager implements NutsRepositoryConfigManager, NutsRepositoryConfigManagerExt {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositoryConfigManager.class.getName());

    private final AbstractNutsRepository repository;
    private final int speed;
    private final String storeLocation;
    private NutsRepositoryConfig config;
    private final Map<String, NutsRepositoryRef> configMirrorRefs = new LinkedHashMap<>();
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;
    private int deployOrder;
    private boolean temporary;
    private boolean enabled = true;
    private String globalName;
    private boolean supportedMirroring = false;
    private final Map<String, NutsRepository> mirrors = new HashMap<>();
    private String repositoryName;
    private String repositoryType;

    public DefaultNutsRepositoryConfigManager(AbstractNutsRepository repository, String storeLocation, NutsRepositoryConfig config, int speed, int deployPriority, boolean temporary, boolean enabled, String globalName, boolean supportedMirroring, String repositoryName, String repositoryType) {
        if (CoreStringUtils.isBlank(repositoryType)) {
            throw new NutsIllegalArgumentException("Missing Repository Type");
        }
        if (CoreStringUtils.isBlank(repositoryName)) {
            throw new NutsIllegalArgumentException("Missing Repository Name");
        }
        if (CoreStringUtils.isBlank(globalName)) {
            throw new NutsIllegalArgumentException("Missing Repository Global Name");
        }
        if (CoreStringUtils.isBlank(storeLocation)) {
            throw new NutsIllegalArgumentException("Missing folder");
        }
        Path pfolder = repository.getWorkspace().io().path(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NutsInvalidRepositoryException(storeLocation, "Unable to resolve root as a valid folder " + storeLocation);
        }

        this.repository = repository;
        this.repositoryName = repositoryName;
        this.globalName = globalName;
        this.storeLocation = storeLocation;
        this.speed = speed;
        this.deployOrder = deployPriority;
        this.temporary = temporary;
        this.enabled = enabled;
        this.supportedMirroring = supportedMirroring;
        this.repositoryType = repositoryType;
        setConfig(config);
    }

    @Override
    public String getName() {
        return repositoryName;
    }

    @Override
    public int getDeployOrder() {
        return deployOrder;
    }

    @Override
    public String getEnv(String key, String defaultValue, boolean inherit) {
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().getProperty(defaultValue);
        }
        if (!CoreStringUtils.isBlank(t)) {
            return t;
        }
        t = repository.getWorkspace().config().getEnv(key, null);
        if (!CoreStringUtils.isBlank(t)) {
            return t;
        }
        return defaultValue;
    }

    @Override
    public Properties getEnv(boolean inherit) {
        Properties p = new Properties();
        if (inherit) {
            p.putAll(repository.getWorkspace().config().getEnv());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }

    @Override
    public void setEnv(String property, String value) {
        if (CoreStringUtils.isBlank(value)) {
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
    public int getSpeed(boolean transitive) {
        int s = speed;
        if (isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors()) {
                s += mirror.config().getSpeed(transitive);
            }
        }
        return s;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public String getType() {
        return repositoryType;
    }

    @Override
    public String getGroups() {
        return config.getGroups();
    }

    @Override
    public String getLocation(boolean expand) {
        String s = config.getLocation();
        if (s != null && expand) {
            s = repository.getWorkspace().io().expandPath(s);
        }
        return s;
    }

    @Override
    public Path getStoreLocation() {
        return repository.getWorkspace().io().path(storeLocation);
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        NutsStoreLocationStrategy strategy = config.getStoreLocationStrategy();
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.values()[0];
        }
        return strategy;
    }

    @Override
    public Path getStoreLocation(NutsStoreLocation folderType) {
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
        switch (getStoreLocationStrategy()) {
            case STANDALONE: {
                if (CoreStringUtils.isBlank(n)) {
                    n = folderType.toString().toLowerCase();
                }
                n = n.trim();
                return getStoreLocation().resolve(n);
            }
            case EXPLODED: {
                Path storeLocation = repository.getWorkspace().config().getStoreLocation(folderType);
                //uuid is added as
                return storeLocation.resolve(NutsConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());

            }
            default: {
                throw new NutsIllegalArgumentException("Unsupported strategy type " + getStoreLocation());
            }
        }

    }

    @Override
    public String getUuid() {
        return config.getUuid();
    }

    public void setConfig(NutsRepositoryConfig newConfig) {
        if (newConfig == null) {
            throw new NutsIllegalArgumentException("Missing Config");
        }
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreLocationStrategy() == null) {
            this.config.setStoreLocationStrategy(repository.getWorkspace().config().getRepositoryStoreLocationStrategy());
        }
        if (CoreStringUtils.isBlank(config.getType())) {
            config.setType(repositoryType);
        } else if (!config.getType().equals(repositoryType)) {
            throw new NutsIllegalArgumentException("Invalid Repository Type : expected " + repositoryType + ", found " + config.getType());
        }

        this.globalName = newConfig.getName();
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        configMirrorRefs.clear();
        if (config.getMirrors() != null) {
            for (NutsRepositoryRef repo : config.getMirrors()) {
                configMirrorRefs.put(repo.getName(), repo);
            }
        }
        for (NutsRepositoryRef ref : getMirrorRefs()) {
            wireRepository(repository.getWorkspace().config().createRepository(
                    CoreNutsUtils.refToOptions(ref).setCreate(true),
                    getMirrorsRoot(), repository));
        }

        fireConfigurationChanged();
    }

    @Override
    public String uuid() {
        return getUuid();
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public NutsRepositoryConfigManager setIndexEnabled(boolean enabled) {
        if (enabled != config.isIndexEnabled()) {
            config.setIndexEnabled(enabled);
            fireConfigurationChanged();
        }
        return this;
    }

    @Override
    public boolean isIndexEnabled() {
        return config.isIndexEnabled();
    }

    @Override
    public NutsRepositoryConfigManager setUser(NutsUserConfig user) {
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged();
        return this;
    }

    @Override
    public NutsRepositoryConfigManager removeUser(String userId) {
        if (configUsers.containsKey(userId)) {
            configUsers.remove(userId);
            fireConfigurationChanged();
        }
        return this;
    }

    @Override
    public NutsUserConfig getUser(String userId) {
        NutsUserConfig u = configUsers.get(userId);
        if (u == null) {
            if (NutsConstants.Names.USER_ADMIN.equals(userId) || NutsConstants.Names.USER_ANONYMOUS.equals(userId)) {
                u = new NutsUserConfig(userId, null, null, null);
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

//    @Override
    public NutsRepositoryConfigManager removeMirrorRef(String repositoryId) {
        if (configMirrorRefs.remove(repositoryId) != null) {
            fireConfigurationChanged();
        }
        return this;
    }

//    @Override
    public NutsRepositoryConfigManager addMirrorRef(NutsRepositoryRef c) {
        String mirrorName = c.getName();
        if (!CoreNutsUtils.isValidIdentifier(mirrorName)) {
            throw new NutsInvalidRepositoryException(mirrorName, "Invalid repository name : " + mirrorName);
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, CoreStringUtils.alignLeft(getName(), 20) + " add repo " + mirrorName);
        }
        if (configMirrorRefs.containsKey(c.getName())) {
            throw new NutsIllegalArgumentException("Mirror with same name already exists : " + c.getName());
        }
        configMirrorRefs.put(c.getName(), c);

        fireConfigurationChanged();
        return this;
    }

//    @Override
    public NutsRepositoryRef getMirrorRef(String name) {
        return configMirrorRefs.get(name);
    }

//    @Override
    public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
        NutsRepositoryRef e = getMirrorRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
            e.setEnabled(enabled);
            fireConfigurationChanged();
        }
        return this;
    }

//    @Override
    public NutsRepositoryRef[] getMirrorRefs() {
        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
    }

    @Override
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!repository.getWorkspace().config().isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.checkReadOnly(repository.getWorkspace());
            repository.security().checkAllowed(NutsConstants.Rights.SAVE_REPOSITORY,"save");
            Path file = getStoreLocation().resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!Files.exists(file)) {
                created = true;
            }
            CoreIOUtils.mkdirs(getStoreLocation());
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(configMirrorRefs.isEmpty() ? null : new ArrayList<>(configMirrorRefs.values()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            repository.getWorkspace().io().writeJson(config, file, true);
            configurationChanged = false;
            if (LOG.isLoggable(Level.CONFIG)) {
                if (created) {
                    LOG.log(Level.CONFIG, CoreStringUtils.alignLeft(repository.config().getName(), 20) + " Created repository " + repository.config().getName() + " at " + getStoreLocation());
                } else {
                    LOG.log(Level.CONFIG, CoreStringUtils.alignLeft(repository.config().getName(), 20) + " Updated repository " + repository.config().getName() + " at " + getStoreLocation());
                }
            }
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repository : mirrors.values()) {
            try {
                ok |= repository.config().save(force);
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    @Override
    public void save() {
        save(true);
    }

    @Override
    public Properties getEnv() {
        return getEnv(true);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        return getEnv(property, defaultValue, true);
    }

    public void fireConfigurationChanged() {
        setConfigurationChanged(true);
    }

    public NutsRepositoryConfigManager setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
        return this;
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    @Override
    public NutsRepositoryConfigManager setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    public NutsRepositoryConfigManager setTemporary(boolean transientRepository) {
        this.temporary = transientRepository;
        return this;
    }

    @Override
    public boolean isIndexSubscribed() {
        return repository.nutsIndexStoreClient.isSubscribed(repository);
    }

    @Override
    public boolean subscribeIndex() {
        return repository.nutsIndexStoreClient.subscribe();
    }

    @Override
    public NutsRepositoryConfigManager unsubscribeIndex() {
        repository.nutsIndexStoreClient.unsubscribe();
        return this;
    }

    @Override
    public String getGlobalName() {
        return globalName;
    }

    @Override
    public boolean isSupportedMirroring() {
        return supportedMirroring;
    }

    @Override
    public NutsRepositoryConfigManager removeMirror(String repositoryId) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
        boolean updated = false;
        NutsRepository repo = null;
        try {
            repo = getMirror(repositoryId);
        } catch (NutsRepositoryNotFoundException ex) {
            //ignore
        }
        if (repo != null) {
            updated = true;
        }
        if (getMirrorRef(repositoryId) != null) {
            updated = true;
        }
        if (!updated) {
            throw new NutsRepositoryNotFoundException(repositoryId);
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{0} remove repo {1}", new Object[]{CoreStringUtils.alignLeft(getName(), 20), repositoryId});
        }
        removeMirrorRef(repositoryId);
        if (repo != null) {
            mirrors.remove(repositoryId);
            ((AbstractNutsRepository) repository).fireOnRemoveRepository(repo);
        }
        return this;
    }

    @Override
    public boolean containsMirror(String repositoryIdPath) {
        return mirrors.containsKey(repositoryIdPath);
    }

    @Override
    public NutsRepository getMirror(String repositoryIdPath) {
        NutsRepository r = mirrors.get(repositoryIdPath);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(repositoryIdPath);
    }

    @Override
    public NutsRepository findMirror(String repositoryIdPath) {
        NutsRepository r = mirrors.get(repositoryIdPath);
        if (r != null) {
            return r;
        }
        return null;
    }

    @Override
    public NutsRepository[] getMirrors() {
        return mirrors.values().toArray(new NutsRepository[0]);
    }

    @Override
    public NutsRepository addMirror(NutsRepositoryDefinition definition) {
        return addMirror(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addMirror(NutsCreateRepositoryOptions options) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException();
        }
        String mirrorName = options.getName();
        NutsRepositoryRef repoConf = getMirrorRef(mirrorName);
        if (repoConf != null) {
            throw new NutsRepositoryAlreadyRegisteredException(mirrorName);
        }
        if (!options.isTemporay()) {
            addMirrorRef(CoreNutsUtils.optionsToRef(options));
        }
        return wireRepository(repository.getWorkspace().config().createRepository(options, getMirrorsRoot(), repository));
    }

    public Path getMirrorsRoot() {
        return getStoreLocation().resolve(NutsConstants.Folders.REPOSITORIES);
    }

    protected NutsRepository wireRepository(NutsRepository repository) {
        if (repository == null) {
            return null;
        }
        //System.out.println(getName()+" -> "+repository.config().getName());
        CoreNutsUtils.validateRepositoryName(repository.config().getName(), mirrors.keySet());
        mirrors.put(repository.config().getName(), repository);
        ((AbstractNutsRepository) repository).fireOnAddRepository(repository);
        return repository;
    }

    @Override
    public int getFindSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive) {
        int namespaceSupport = ((AbstractNutsRepository) repository).getFindSupportLevelCurrent(supportedAction, id, mode);
        if (transitive) {
            for (NutsRepository remote : mirrors.values()) {
                int r = remote.config().getFindSupportLevel(supportedAction, id, mode, transitive);
                if (r > 0 && r > namespaceSupport) {
                    namespaceSupport = r;
                }
            }
        }
        return namespaceSupport;
    }

    public NutsRepositoryConfig getStoredConfig() {
        return config;
    }

}
