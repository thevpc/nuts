package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.repos.DefaultNutsRepositoryManager;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryUtils;
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
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryConfigModel;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;

public class DefaultNutsRepositoryConfigModel implements NutsRepositoryConfigModel{

    private NutsLogger LOG;

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

    public DefaultNutsRepositoryConfigModel(NutsRepository repository, NutsAddRepositoryOptions options, NutsSession session,
            int speed,
            boolean supportedMirroring, String repositoryType) {
        if (options == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository options"));
        }
        if (options.getConfig() == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository options config"));
        }
        this.repositoryRef = CoreNutsUtils.optionsToRef(options);
//        NutsSession session = options.getSession();
        String storeLocation = options.getLocation();
        NutsRepositoryConfig config = options.getConfig();
        String globalName = options.getConfig().getName();
        String repositoryName = options.getName();

        speed = Math.max(0, speed);

        if (NutsBlankable.isBlank(repositoryType)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository type"));
        }
        if (NutsBlankable.isBlank(repositoryName)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository name"));
        }
        if (NutsBlankable.isBlank(globalName)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository global name"));
        }
        if (NutsBlankable.isBlank(storeLocation)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing folder"));
        }
        Path pfolder = Paths.get(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NutsInvalidRepositoryException(session, storeLocation, NutsMessage.cstyle("unable to resolve root as a valid folder %s",storeLocation));
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
        setConfig(config, session, false);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(DefaultNutsRepositoryConfigModel.class);
        }
        return LOG;
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public NutsWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

    public NutsRepositoryRef getRepositoryRef(NutsSession session) {
        return new NutsRepositoryRef(repositoryRef);
    }

    public String getName() {
        return repositoryName;
    }

    public int getDeployOrder(NutsSession session) {
        return deployOrder;
    }

//    public String getEnv(String key, String defaultValue, boolean inherit,NutsSession session) {
//        String t = null;
//        if (config.getEnv() != null) {
//            t = config.getEnv().get(defaultValue);
//        }
//        if (!NutsBlankable.isBlank(t)) {
//            return t;
//        }
//        if (inherit) {
//            t = repository.getWorkspace().env().getEnv(key, null);
//            if (!NutsBlankable.isBlank(t)) {
//                return t;
//            }
//        }
//        return defaultValue;
//    }

//    public Map<String, String> getEnv(boolean inherit,NutsSession session) {
//        Map<String, String> p = new LinkedHashMap<>();
//        if (inherit) {
//            p.putAll(repository.getWorkspace().env().getEnvMap());
//        }
//        if (config.getEnv() != null) {
//            p.putAll(config.getEnv());
//        }
//        return p;
//    }

    public void setEnv(String property, String value, NutsSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NutsBlankable.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env", session);
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env", session);
            }
        }
    }

    @Override
    public int getSpeed(NutsSession session) {
        int s = speed;
        if (isSupportedMirroring(session)) {
            for (NutsRepository mirror : getMirrors(session)) {
                s += mirror.config().setSession(session).getSpeed();
            }
        }
        return s;
    }

    @Override
    public String getType(NutsSession session) {
        return repositoryType;
    }

    @Override
    public String getGroups(NutsSession session) {
        return config.getGroups();
    }

    @Override
    public String getLocation(boolean expand,NutsSession session) {
        String s = config.getLocation();
        if (s != null && expand) {
            s = repository.getWorkspace().io().path(s).builder().withWorkspaceBaseDir().build().toString();
        }
        return s;
    }

    @Override
    public String getStoreLocation() {
        return storeLocation;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy(NutsSession session) {
        NutsStoreLocationStrategy strategy = config.getStoreLocationStrategy();
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.values()[0];
        }
        return strategy;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType,NutsSession session) {
        NutsStoreLocationsMap hlm = new NutsStoreLocationsMap(config.getStoreLocations());

//        String n = CoreNutsUtils.getArrItem(config.getStoreLocations(), folderType.ordinal());
        String n = hlm.get(folderType);
        if (temporary) {
            if (NutsBlankable.isBlank(n)) {
                n = folderType.toString().toLowerCase();
                n = n.trim();
            }
            return Paths.get(getStoreLocation()).resolve(n).toString();
        } else {
            switch (getStoreLocationStrategy(session)) {
                case STANDALONE: {
                    if (NutsBlankable.isBlank(n)) {
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
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported strategy type %s", getStoreLocation()));
                }
            }
        }
    }

    public String getUuid() {
        return config.getUuid();
    }

    public String getLocation() {
        return config.getLocation();
    }

    public void setConfig(NutsRepositoryConfig newConfig, NutsSession session, boolean fireChange) {
        if (newConfig == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing config"));
        }
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            fireChange = true;
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreLocationStrategy() == null) {
            fireChange = true;
            this.config.setStoreLocationStrategy(repository.getWorkspace().locations().getRepositoryStoreLocationStrategy());
        }
        if (NutsBlankable.isBlank(config.getType())) {
            fireChange = true;
            config.setType(repositoryType);
        } else if (!config.getType().equals(repositoryType)) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid Repository Type : expected %s, found %s" ,repositoryType, config.getType())
                    );
        }

        this.globalName = newConfig.getName();
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        removeAllMirrors(session);
        if (config.getMirrors() != null) {
            for (NutsRepositoryRef ref : config.getMirrors()) {
                NutsRepository r = ((DefaultNutsRepositoryManager) repository.getWorkspace().repos())
                        .getModel()
                        .createRepository(
                                CoreNutsUtils.refToOptions(ref),
                                repository, session
                        );
                addMirror(r, session);
            }
        }
        if (fireChange) {
            fireConfigurationChanged("*", session);
        }
    }

    @Override
    public void addMirror(NutsRepository repo, NutsSession session) {
        repositoryRegistryHelper.addRepository(repo, session);
        NutsRepositoryUtils.of(repository).events().fireOnAddRepository(
                new DefaultNutsRepositoryEvent(session, repository, repo, "mirror", null, repo)
        );
    }

    @Override
    public void setIndexEnabled(boolean enabled, NutsSession session) {
        if (enabled != config.isIndexEnabled()) {
            config.setIndexEnabled(enabled);
            fireConfigurationChanged("index-enabled", session);
        }
    }

    @Override
    public boolean isIndexEnabled(NutsSession session) {
        return config.isIndexEnabled();
    }

    @Override
    public void setUser(NutsUserConfig user, NutsSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged("user", session);
//        return this;
    }

    @Override
    public void removeUser(String userId, NutsSession session) {
        if (configUsers.containsKey(userId)) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            configUsers.remove(userId);
            fireConfigurationChanged("user", session);
        }
//        return this;
    }

    @Override
    public NutsUserConfig getUser(String userId, NutsSession session) {
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
    public NutsUserConfig[] getUsers(NutsSession session) {
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

//    
//    public NutsRepositoryConfigManager removeMirrorRef(String repositoryId) {
//        if (configMirrorRefs.remove(repositoryId) != null) {
//            fireConfigurationChanged();
//        }
//        return this;
//    }
//    
//    public NutsRepositoryConfigManager addMirrorRef(NutsRepositoryRef c) {
//        repositoryRegistryHelper.addRepositoryRef(c);
//        if (LOG.isLoggable(Level.FINEST)) {
//            LOG.log(Level.FINEST, CoreStringUtils.alignLeft(getName(), 20) + " add repo " + c.getName());
//        }
//        fireConfigurationChanged();
//        return this;
//    }
//    
//    public NutsRepositoryRef getMirrorRef(String name) {
//        return configMirrorRefs.get(name);
//    }
    public void setMirrorEnabled(String repoName, boolean enabled, NutsSession session) {
        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            e.setEnabled(enabled);
            fireConfigurationChanged("mirror", session);
        }
    }

////    
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force, NutsSession session) {
        NutsWorkspaceUtils.checkSession(repository.getWorkspace(), session);
        boolean ok = false;
        if (force || (!repository.getWorkspace().config().isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.of(session).checkReadOnly();
            repository.security().setSession(session).checkAllowed(NutsConstants.Permissions.SAVE, "save");
            Path file = Paths.get(getStoreLocation()).resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!Files.exists(file)) {
                created = true;
            }
            CoreIOUtils.mkdirs(Paths.get(getStoreLocation()),session);
            config.setConfigVersion(DefaultNutsWorkspace.VERSION_REPOSITORY_CONFIG);
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
//            if (NutsBlankable.isBlank(config.getConfigVersion())) {
//                config.setConfigVersion(repository.getWorkspace().getApiVersion());
//            }
            repository.getWorkspace().elem().setSession(session).setContentType(NutsContentType.JSON).setValue(config).print(file);
            configurationChanged = false;
            if (_LOG(session).isLoggable(Level.CONFIG)) {
                if (created) {
                    _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle(
                                    "{0} created repository {1} at {2}",
                                    CoreStringUtils.alignLeft(repository.getName(), 20) , repository.getName() ,
                                    session.text().ofStyled(getStoreLocation(),NutsTextStyle.path())
                                    ));
                } else {
                    _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log(NutsMessage.jstyle(
                            "{0} updated repository {1} at {2}",
                            CoreStringUtils.alignLeft(repository.getName(), 20) , repository.getName() ,
                            session.text().ofStyled(getStoreLocation(),NutsTextStyle.path())
                    ));
                }
            }
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : getMirrors(session)) {
            try {
                NutsRepositoryConfigManager config = repo.config();
                if (config instanceof NutsRepositoryConfigManagerExt) {
                    ok |= ((NutsRepositoryConfigManagerExt) config)
                            .getModel()
                            .save(force, session);
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

    public void save(NutsSession session) {
        save(true, session);
    }

    @Override
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
    public void setEnabled(boolean enabled, NutsSession session) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled(NutsSession session) {
        return enabled;
    }

    @Override
    public boolean isTemporary(NutsSession session) {
        return temporary;
    }

    @Override
    public void setTemporary(boolean transientRepository, NutsSession session) {
        this.temporary = transientRepository;
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
    public void subscribeIndex(NutsSession session) {
        NutsIndexStore s = getIndexStore();
        if (s != null) {
            s.subscribe(session);
        }
    }

    @Override
    public void unsubscribeIndex(NutsSession session) {
        NutsIndexStore s = getIndexStore();
        if (s != null) {
            s.unsubscribe(session);
        }
    }

    @Override
    public String getGlobalName(NutsSession session) {
        return globalName;
    }

    @Override
    public boolean isSupportedMirroring(NutsSession session) {
        return supportedMirroring;
    }

    @Override
    public void removeMirror(String repositoryId, NutsSession session) {
        if (!isSupportedMirroring(session)) {
            throw new NutsUnsupportedOperationException(session,NutsMessage.cstyle("unsupported operation '%s'","removeMirror"));
        }
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        repository.security().setSession(session).checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NutsRepository r = repositoryRegistryHelper.removeRepository(repositoryId);
        if (r != null) {
            NutsRepositoryUtils.of(repository).events().fireOnRemoveRepository(new DefaultNutsRepositoryEvent(session, repository, r, "mirror", r, null));
        } else {
            throw new NutsRepositoryNotFoundException(session, repositoryId);
        }
//        return this;
    }

//    
//    public NutsRepository getMirror(String repositoryIdOrName) {
//        return getMirror(repositoryIdOrName, false);
//    }
    public NutsRepository getMirror(String repositoryIdPath, NutsSession session) {
        NutsRepository r = findMirror(repositoryIdPath, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(session, repositoryIdPath);
    }

    public NutsRepository findMirror(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirror(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId ,y.getUuid() ,m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NutsRepository findMirrorById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorById(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId ,y.getUuid() ,m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NutsRepository findMirrorByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorByName(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId ,y.getUuid() ,m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NutsRepository[] getMirrors(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    public NutsRepository addMirror(NutsAddRepositoryOptions options, NutsSession session) {
        if (!isSupportedMirroring(session)) {
            throw new NutsUnsupportedOperationException(session,NutsMessage.cstyle("unsupported operation '%s'","addMirror"));
        }
        if (options.isTemporary()) {
            return null;
        }
        NutsRepository repo = ((DefaultNutsRepositoryManager) repository.getWorkspace().repos())
                .getModel()
                .createRepository(
                        options,
                        repository, session
                );
        addMirror(repo, session);
        return repo;
    }

    @Override
    public Path getTempMirrorsRoot(NutsSession session) {
        return Paths.get(getStoreLocation()).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public Path getMirrorsRoot(NutsSession session) {
        return Paths.get(getStoreLocation()).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public NutsRepositoryConfig getStoredConfig(NutsSession session) {
        return config;
    }

//    
    public void removeAllMirrors(NutsSession options) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        for (NutsRepository repo : repositoryRegistryHelper.getRepositories()) {
            removeMirror(repo.getUuid(), options);
        }
    }

    public NutsRepositoryConfig getConfig(NutsSession session) {
        return config;
    }
    
}
