package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsRepositoryEvent;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsRepositoryConfigManager implements NutsRepositoryConfigManager, NutsRepositoryConfigManagerExt {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositoryConfigManager.class.getName());

    private final NutsRepository repository;
    private final int speed;
    private final String storeLocation;
    private NutsRepositoryConfig config;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;
    private int deployOrder;
    private boolean temporary;
    private boolean enabled = true;
    private String globalName;
    private boolean supportedMirroring = false;
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private String repositoryName;
    private String repositoryType;

    public DefaultNutsRepositoryConfigManager(NutsRepository repository, NutsSession session, String storeLocation, NutsRepositoryConfig config, int speed, int deployPriority, boolean temporary, boolean enabled, String globalName, boolean supportedMirroring, String repositoryName, String repositoryType) {
        if (CoreStringUtils.isBlank(repositoryType)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Missing Repository Type");
        }
        if (CoreStringUtils.isBlank(repositoryName)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Missing Repository Name");
        }
        if (CoreStringUtils.isBlank(globalName)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Missing Repository Global Name");
        }
        if (CoreStringUtils.isBlank(storeLocation)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Missing folder");
        }
        Path pfolder = repository.getWorkspace().io().path(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NutsInvalidRepositoryException(repository.getWorkspace(), storeLocation, "Unable to resolve root as a valid folder " + storeLocation);
        }

        this.repositoryRegistryHelper = new NutsRepositoryRegistryHelper(repository.getWorkspace());
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
        setConfig(config, session, false);
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
        String n = CoreNutsUtils.getArrItem(config.getStoreLocations(), folderType.ordinal());
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
                throw new NutsIllegalArgumentException(repository.getWorkspace(), "Unsupported strategy type " + getStoreLocation());
            }
        }

    }

    @Override
    public String getUuid() {
        return config.getUuid();
    }

    public void setConfig(NutsRepositoryConfig newConfig, NutsSession session, boolean fireChange) {
        if (newConfig == null) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Missing Config");
        }
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            fireChange = true;
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreLocationStrategy() == null) {
            fireChange = true;
            this.config.setStoreLocationStrategy(repository.getWorkspace().config().getRepositoryStoreLocationStrategy());
        }
        if (CoreStringUtils.isBlank(config.getType())) {
            fireChange = true;
            config.setType(repositoryType);
        } else if (!config.getType().equals(repositoryType)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "Invalid Repository Type : expected " + repositoryType + ", found " + config.getType());
        }

        this.globalName = newConfig.getName();
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        removeAllMirrors(new NutsRemoveOptions().session(session));
        if (config.getMirrors() != null) {
            for (NutsRepositoryRef ref : config.getMirrors()) {
                NutsRepository r = repository.getWorkspace().config().createRepository(CoreNutsUtils.refToOptions(ref), getMirrorsRoot(), repository);
                addMirror(ref, r, session);
            }
        }
        if (fireChange) {
            fireConfigurationChanged();
        }
    }

    protected void addMirror(NutsRepositoryRef ref, NutsRepository repo, NutsSession session) {
        repositoryRegistryHelper.addRepository(ref, repo);
        if (repo != null) {
            NutsRepositoryExt.of(repository).fireOnAddRepository(
                    new DefaultNutsRepositoryEvent(session, repository, repo, "mirror", null, repo)
            );
        }
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
            if (NutsConstants.Users.ADMIN.equals(userId) || NutsConstants.Users.ANONYMOUS.equals(userId)) {
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
//    public NutsRepositoryConfigManager removeMirrorRef(String repositoryId) {
//        if (configMirrorRefs.remove(repositoryId) != null) {
//            fireConfigurationChanged();
//        }
//        return this;
//    }
//    @Override
//    public NutsRepositoryConfigManager addMirrorRef(NutsRepositoryRef c) {
//        repositoryRegistryHelper.addRepositoryRef(c);
//        if (LOG.isLoggable(Level.FINEST)) {
//            LOG.log(Level.FINEST, CoreStringUtils.alignLeft(getName(), 20) + " add repo " + c.getName());
//        }
//        fireConfigurationChanged();
//        return this;
//    }
//    @Override
//    public NutsRepositoryRef getMirrorRef(String name) {
//        return configMirrorRefs.get(name);
//    }
    @Override
    public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled) {
        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
            e.setEnabled(enabled);
            fireConfigurationChanged();
        }
        return this;
    }

////    @Override
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!repository.getWorkspace().config().isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.checkReadOnly(repository.getWorkspace());
            repository.security().checkAllowed(NutsConstants.Rights.SAVE_REPOSITORY, "save");
            Path file = getStoreLocation().resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!Files.exists(file)) {
                created = true;
            }
            CoreIOUtils.mkdirs(getStoreLocation());
            config.setConfigVersion(repository.getWorkspace().config().current().getApiId().getVersion().getValue());
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            if (CoreStringUtils.isBlank(config.getConfigVersion())) {
                config.setConfigVersion(repository.getWorkspace().config().current().getApiId().getVersion().getValue());
            }
            repository.getWorkspace().json().value(config).print(file);
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
        for (NutsRepository repo : getMirrors()) {
            try {
                ok |= repo.config().save(force);
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

    @Override
    public NutsRepositoryConfigManager setTemporary(boolean transientRepository) {
        this.temporary = transientRepository;
        return this;
    }

    @Override
    public boolean isIndexSubscribed() {
        return NutsRepositoryExt.of(repository).getIndexStoreClient().isSubscribed(repository);
    }

    @Override
    public boolean subscribeIndex() {
        return NutsRepositoryExt.of(repository).getIndexStoreClient().subscribe();
    }

    @Override
    public NutsRepositoryConfigManager unsubscribeIndex() {
        NutsRepositoryExt.of(repository).getIndexStoreClient().unsubscribe();
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
    public NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException(repository.getWorkspace());
        }
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        if (options.getSession() == null) {
            options.setSession(repository.getWorkspace().createSession());
        }
        repository.security().checkAllowed(NutsConstants.Rights.REMOVE_REPOSITORY, "remove-repository");
        final NutsRepository r = repositoryRegistryHelper.removeRepository(repositoryId);
        if (r != null) {
            NutsRepositoryExt.of(repository).fireOnRemoveRepository(new DefaultNutsRepositoryEvent(options.getSession(), repository, r, "mirror", r, null));
        } else {
            throw new NutsRepositoryNotFoundException(repository.getWorkspace(), repositoryId);
        }
        return this;
    }

    @Override
    public NutsRepository getMirror(String repositoryIdOrName) {
        return getMirror(repositoryIdOrName, false);
    }

    @Override
    public NutsRepository getMirror(String repositoryIdPath, boolean transitive) {
        NutsRepository r = findMirror(repositoryIdPath, transitive);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(repository.getWorkspace(), repositoryIdPath);
    }

    @Override
    public NutsRepository findMirror(String repositoryNameOrId, boolean transitive) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (transitive && isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors()) {
                NutsRepository m = mirror.config().findMirror(repositoryNameOrId, true);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(repository.getWorkspace(), "Ambigous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                }

            }
        }
        return y;
    }

    @Override
    public NutsRepository[] getMirrors() {
        return repositoryRegistryHelper.getRepositories();
    }

    @Override
    public NutsRepository addMirror(NutsRepositoryDefinition definition) {
        return addMirror(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addMirror(NutsCreateRepositoryOptions options) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException(repository.getWorkspace());
        }
        if (options.isTemporary()) {
            return null;
        }
        if (options.getSession() == null) {
            options.setSession(repository.getWorkspace().createSession());
        }
        NutsRepositoryRef ref = CoreNutsUtils.optionsToRef(options);
        NutsRepository repo = repository.getWorkspace().config().createRepository(options, getMirrorsRoot(), repository);
        addMirror(ref, repo, options.getSession());
        return repo;
    }

    public Path getMirrorsRoot() {
        return getStoreLocation().resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive) {
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        double result = 0;
        if (xrepo.acceptNutsId(id)) {
            int r = repository.config().getSpeed();
            if (r > 0) {
                result += 1.0 / r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repositoryRegistryHelper.getRepositories()) {
                int r = remote.config().getSupportLevel(supportedAction, id, mode, transitive);
                if (r > 0) {
                    result += 1.0 / r;
                }
            }
        }
        int intResult = 0;
        if (result != 0) {
            intResult = (int) (1.0 / result);
            if (intResult < 0) {
                intResult = Integer.MAX_VALUE;
            }
        }
        return intResult;
    }

    public NutsRepositoryConfig getStoredConfig() {
        return config;
    }

//    @Override
    public void removeAllMirrors(NutsRemoveOptions options) {
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        if (options.getSession() == null) {
            options.setSession(repository.getWorkspace().createSession());
        }
        for (NutsRepository repo : repositoryRegistryHelper.getRepositories()) {
            removeMirror(repo.getUuid(), options);
        }
    }
}
