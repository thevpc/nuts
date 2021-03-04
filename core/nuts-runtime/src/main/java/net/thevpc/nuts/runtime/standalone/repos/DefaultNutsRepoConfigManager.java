package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.repos.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.standalone.util.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.NutsStoreLocationsMap;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsRepoConfigManager implements NutsRepositoryConfigManager, NutsRepositoryConfigManagerExt {

    private final NutsLogger LOG;

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
    private boolean supportedMirroring;
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private String repositoryName;
    private String repositoryType;
    private NutsRepositoryRef repositoryRef;

    public DefaultNutsRepoConfigManager(NutsRepository repository, NutsAddRepositoryOptions options,
            int speed,
            boolean supportedMirroring, String repositoryType) {
        LOG = repository.getWorkspace().log().of(DefaultNutsRepoConfigManager.class);
        if (options == null) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing repository options");
        }
        if (options.getConfig() == null) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing repository options config");
        }
        this.repositoryRef = CoreNutsUtils.optionsToRef(options);
        NutsSession session = options.getSession();
        String storeLocation = options.getLocation();
        NutsRepositoryConfig config = options.getConfig();
        String globalName = options.getConfig().getName();
        String repositoryName = options.getName();

        speed = Math.max(0, speed);

        if (CoreStringUtils.isBlank(repositoryType)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing repository type");
        }
        if (CoreStringUtils.isBlank(repositoryName)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing repository name");
        }
        if (CoreStringUtils.isBlank(globalName)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing repository global name");
        }
        if (CoreStringUtils.isBlank(storeLocation)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing folder");
        }
        Path pfolder = Paths.get(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NutsInvalidRepositoryException(repository.getWorkspace(), storeLocation, "unable to resolve root as a valid folder " + storeLocation);
        }

        this.repositoryRegistryHelper = new NutsRepositoryRegistryHelper(repository.getWorkspace());
        this.repository = repository;
        this.repositoryName = repositoryName;
        this.globalName = globalName;
        this.storeLocation = storeLocation;
        this.speed = speed;
        this.deployOrder = options.getDeployOrder();
        this.temporary = options.isTemporary();
        this.enabled = options.isEnabled();
        this.supportedMirroring = supportedMirroring;
        this.repositoryType = repositoryType;
        setConfig(config, session, false, new NutsUpdateOptions().setSession(session));
    }

    public NutsRepositoryRef getRepositoryRef() {
        return new NutsRepositoryRef(repositoryRef);
    }

    @Override
    public String getName() {
        return repositoryName;
    }

    @Override
    public int getDeployOrder() {
        return deployOrder;
    }

    public String getEnv(String key, String defaultValue, boolean inherit) {
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().get(defaultValue);
        }
        if (!CoreStringUtils.isBlank(t)) {
            return t;
        }
        if (inherit) {
            t = repository.getWorkspace().env().getEnv(key, null);
            if (!CoreStringUtils.isBlank(t)) {
                return t;
            }
        }
        return defaultValue;
    }

    public Map<String, String> getEnv(boolean inherit) {
        Map<String, String> p = new LinkedHashMap<>();
        if (inherit) {
            p.putAll(repository.getWorkspace().env().getEnvMap());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }

    public void setEnv(String property, String value, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (CoreStringUtils.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env", options.getSession());
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env", options.getSession());
            }
        }
    }

    @Override
    public int getSpeed(NutsSession session) {
        int s = speed;
        if (isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors(session)) {
                s += mirror.config().getSpeed(session);
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
    public String getStoreLocation() {
        return storeLocation;
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
    public String getStoreLocation(NutsStoreLocation folderType) {
        NutsStoreLocationsMap hlm = new NutsStoreLocationsMap(config.getStoreLocations());

//        String n = CoreNutsUtils.getArrItem(config.getStoreLocations(), folderType.ordinal());
        String n = hlm.get(folderType);
        if (temporary) {
            if (CoreStringUtils.isBlank(n)) {
                n = folderType.toString().toLowerCase();
                n = n.trim();
            }
            return Paths.get(getStoreLocation()).resolve(n).toString();
        } else {
            switch (getStoreLocationStrategy()) {
                case STANDALONE: {
                    if (CoreStringUtils.isBlank(n)) {
                        n = folderType.toString().toLowerCase();
                    }
                    n = n.trim();
                    return Paths.get(getStoreLocation()).resolve(n).toString();
                }
                case EXPLODED: {
                    Path storeLocation = Paths.get(repository.getWorkspace().locations().getStoreLocation(folderType));
                    //uuid is added as
                    return storeLocation.resolve(NutsConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid()).toString();

                }
                default: {
                    throw new NutsIllegalArgumentException(repository.getWorkspace(), "unsupported strategy type " + getStoreLocation());
                }
            }
        }
    }

    @Override
    public String getUuid() {
        return config.getUuid();
    }

    public void setConfig(NutsRepositoryConfig newConfig, NutsSession session, boolean fireChange, NutsUpdateOptions options) {
        if (newConfig == null) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "missing config");
        }
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            fireChange = true;
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreLocationStrategy() == null) {
            fireChange = true;
            this.config.setStoreLocationStrategy(repository.getWorkspace().locations().getRepositoryStoreLocationStrategy());
        }
        if (CoreStringUtils.isBlank(config.getType())) {
            fireChange = true;
            config.setType(repositoryType);
        } else if (!config.getType().equals(repositoryType)) {
            throw new NutsIllegalArgumentException(repository.getWorkspace(), "invalid Repository Type : expected " + repositoryType + ", found " + config.getType());
        }

        this.globalName = newConfig.getName();
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        removeAllMirrors(CoreNutsUtils.toRemoveOptions(options));
        if (config.getMirrors() != null) {
            for (NutsRepositoryRef ref : config.getMirrors()) {
                NutsRepository r = ((DefaultNutsRepositoryManager) repository.getWorkspace().repos()).createRepository(
                        CoreNutsUtils.refToOptions(ref),
                        repository
                );
                addMirror(r, CoreNutsUtils.toAddOptions(options));
            }
        }
        if (fireChange) {
            fireConfigurationChanged("*", options.getSession());
        }
    }

    protected void addMirror(NutsRepository repo, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        repositoryRegistryHelper.addRepository(repo);
        NutsRepositoryUtils.of(repository).events().fireOnAddRepository(
                new DefaultNutsRepositoryEvent(options.getSession(), repository, repo, "mirror", null, repo)
        );
    }

    @Override
    public NutsRepositoryConfigManager setIndexEnabled(boolean enabled, NutsUpdateOptions options) {
        if (enabled != config.isIndexEnabled()) {
            options = CoreNutsUtils.validate(options, repository.getWorkspace());
            config.setIndexEnabled(enabled);
            fireConfigurationChanged("index-enabled", options.getSession());
        }
        return this;
    }

    @Override
    public boolean isIndexEnabled() {
        return config.isIndexEnabled();
    }

    @Override
    public NutsRepositoryConfigManager setUser(NutsUserConfig user, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged("user", options.getSession());
        return this;
    }

    @Override
    public NutsRepositoryConfigManager removeUser(String userId, NutsRemoveOptions options) {
        if (configUsers.containsKey(userId)) {
            options = CoreNutsUtils.validate(options, repository.getWorkspace());
            configUsers.remove(userId);
            fireConfigurationChanged("user", options.getSession());
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
                fireConfigurationChanged("user", repository.getWorkspace().createSession());
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
    public NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled, NutsUpdateOptions options) {
        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
            options = CoreNutsUtils.validate(options, repository.getWorkspace());
            e.setEnabled(enabled);
            fireConfigurationChanged("mirror", options.getSession());
        }
        return this;
    }

////    @Override
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force, NutsSession session) {
        session = NutsWorkspaceUtils.of(repository.getWorkspace()).validateSession(session);
        boolean ok = false;
        if (force || (!repository.getWorkspace().config().isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.of(repository.getWorkspace()).checkReadOnly();
            repository.security().checkAllowed(NutsConstants.Permissions.SAVE, "save", session);
            Path file = Paths.get(getStoreLocation()).resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!Files.exists(file)) {
                created = true;
            }
            CoreIOUtils.mkdirs(Paths.get(getStoreLocation()));
            config.setConfigVersion(repository.getWorkspace().getApiVersion());
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            if (CoreStringUtils.isBlank(config.getConfigVersion())) {
                config.setConfigVersion(repository.getWorkspace().getApiVersion());
            }
            repository.getWorkspace().formats().element().setSession(session).setContentType(NutsContentType.JSON).setValue(config).print(file);
            configurationChanged = false;
            if (LOG.isLoggable(Level.CONFIG)) {
                if (created) {
                    LOG.with().session(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log(CoreStringUtils.alignLeft(repository.getName(), 20) + " created repository " + repository.getName() + " at " + getStoreLocation());
                } else {
                    LOG.with().session(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log(CoreStringUtils.alignLeft(repository.getName(), 20) + " updated repository " + repository.getName() + " at " + getStoreLocation());
                }
            }
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : getMirrors(session)) {
            try {
                NutsRepositoryConfigManager config = repo.config();
                if (config instanceof NutsRepositoryConfigManagerExt) {
                    ok |= ((NutsRepositoryConfigManagerExt) config).save(force, session);
                }
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
    public void save(NutsSession session) {
        save(true, session);
    }

    public void fireConfigurationChanged(String configName, NutsSession session) {
        this.configurationChanged = true;
        DefaultNutsRepositoryEvent evt = new DefaultNutsRepositoryEvent(session, null, repository, "config." + configName, null, true);
        for (NutsRepositoryListener workspaceListener : repository.getRepositoryListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    @Override
    public NutsRepositoryConfigManager setEnabled(boolean enabled, NutsUpdateOptions options) {
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
    public NutsRepositoryConfigManager setTemporary(boolean transientRepository, NutsUpdateOptions options) {
        this.temporary = transientRepository;
        return this;
    }

    @Override
    public boolean isIndexSubscribed(NutsSession session) {
        NutsIndexStore s = getIndexStore();
        return s != null && s.isSubscribed(session);
    }

    private NutsIndexStore getIndexStore() {
        return NutsRepositoryExt.of(repository).getIndexStore();
    }

    @Override
    public NutsRepositoryConfigManager subscribeIndex(NutsSession session) {
        NutsIndexStore s = getIndexStore();
        if (s != null) {
            s.subscribe(session);
        }
        return this;
    }

    @Override
    public NutsRepositoryConfigManager unsubscribeIndex(NutsSession session) {
        NutsIndexStore s = getIndexStore();
        if (s != null) {
            s.unsubscribe(session);
        }
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
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        repository.security().checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository", options.getSession());
        final NutsRepository r = repositoryRegistryHelper.removeRepository(repositoryId);
        if (r != null) {
            NutsRepositoryUtils.of(repository).events().fireOnRemoveRepository(new DefaultNutsRepositoryEvent(options.getSession(), repository, r, "mirror", r, null));
        } else {
            throw new NutsRepositoryNotFoundException(repository.getWorkspace(), repositoryId);
        }
        return this;
    }

//    @Override
//    public NutsRepository getMirror(String repositoryIdOrName) {
//        return getMirror(repositoryIdOrName, false);
//    }
    @Override
    public NutsRepository getMirror(String repositoryIdPath, NutsSession session) {
        NutsRepository r = findMirror(repositoryIdPath, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(repository.getWorkspace(), repositoryIdPath);
    }

    @Override
    public NutsRepository findMirror(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config().findMirror(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(repository.getWorkspace(), "ambiguous repository name " + repositoryNameOrId + " ; found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                }

            }
        }
        return y;
    }

    @Override
    public NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config().findMirrorById(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(repository.getWorkspace(), "ambiguous repository name " + repositoryNameOrId + " ; found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                }

            }
        }
        return y;
    }

    @Override
    public NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config().findMirrorByName(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(repository.getWorkspace(), "ambiguous repository name " + repositoryNameOrId + "; found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                }

            }
        }
        return y;
    }

    @Override
    public NutsRepository[] getMirrors(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    @Override
    public NutsRepository addMirror(NutsAddRepositoryOptions options) {
        if (!isSupportedMirroring()) {
            throw new NutsUnsupportedOperationException(repository.getWorkspace());
        }
        if (options.isTemporary()) {
            return null;
        }
        if (options.getSession() == null) {
            options.setSession(repository.getWorkspace().createSession());
        }
        NutsRepository repo = ((DefaultNutsRepositoryManager) repository.getWorkspace().repos()).createRepository(
                options,
                repository
        );
        addMirror(repo, new NutsAddOptions().setSession(options.getSession()));
        return repo;
    }

    @Override
    public Path getTempMirrorsRoot() {
        return Paths.get(getStoreLocation()).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public Path getMirrorsRoot() {
        return Paths.get(getStoreLocation()).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public NutsRepositoryConfig getStoredConfig() {
        return config;
    }

//    @Override
    public void removeAllMirrors(NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        for (NutsRepository repo : repositoryRegistryHelper.getRepositories()) {
            removeMirror(repo.getUuid(), options);
        }
    }
}
