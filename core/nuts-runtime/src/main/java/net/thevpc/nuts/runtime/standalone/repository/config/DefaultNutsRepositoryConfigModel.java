package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsStoreLocationsMap;
import net.thevpc.nuts.NutsLogVerb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.util.NutsSpeedQualifiers;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNutsWorkspace;
import net.thevpc.nuts.spi.NutsRepositoryLocation;

public class DefaultNutsRepositoryConfigModel implements NutsRepositoryConfigModel {

    private NutsLogger LOG;

    private final NutsRepository repository;
    private final NutsSpeedQualifier speed;
    private final NutsPath storeLocation;
    private NutsRepositoryConfig config;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;
    private int deployWeight;
    private boolean temporary;
    private boolean enabled = true;
    private String globalName;
    private boolean supportedMirroring;
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private String repositoryName;
    private String repositoryType;
    private NutsRepositoryRef repositoryRef;

    public DefaultNutsRepositoryConfigModel(NutsRepository repository, NutsAddRepositoryOptions options, NutsSession session,
                                            NutsSpeedQualifier speed,
            boolean supportedMirroring, String repositoryType) {
        if (options == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository options"));
        }
        if (options.getConfig() == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository options config"));
        }
        this.repositoryRef = net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils.optionsToRef(options);
//        NutsSession session = options.getSession();
        String storeLocation = options.getLocation();
        NutsRepositoryConfig config = options.getConfig();
        String globalName = options.getConfig().getName();
        String repositoryName = options.getName();

        speed = speed==null?NutsSpeedQualifier.NORMAL : speed;

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
        this.storeLocation = NutsPath.of(storeLocation,session);
        this.speed = speed;
        this.deployWeight = options.getDeployWeight();
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
            LOG = NutsLogger.of(DefaultNutsRepositoryConfigModel.class,session);
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

    public int getDeployWeight(NutsSession session) {
        return deployWeight;
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
    public NutsSpeedQualifier getSpeed(NutsSession session) {
        List<NutsSpeedQualifier> all=new ArrayList<>();
        boolean unavailable=false;
        if(speed==NutsSpeedQualifier.UNAVAILABLE){
            unavailable=true;
        }else{
            all.add(speed);
        }
        if (isSupportedMirroring(session)) {
            for (NutsRepository mirror : getMirrors(session)) {
                NutsSpeedQualifier mspeed = mirror.config().setSession(session).getSpeed();
                if(mspeed==NutsSpeedQualifier.UNAVAILABLE){
                    unavailable=true;
                }else{
                    all.add(mspeed);
                }
            }
        }
        if(all.isEmpty()){
            if(unavailable){
                return NutsSpeedQualifier.UNAVAILABLE;
            }
            return NutsSpeedQualifier.NORMAL;
        }
        return NutsSpeedQualifiers.max(all.toArray(new NutsSpeedQualifier[0]));
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
    public NutsRepositoryLocation getLocation(NutsSession session) {
        NutsRepositoryLocation loc = config.getLocation();
        if(loc==null){
            loc=NutsRepositoryLocation.of(null);
        }
        String name = config.getName();
        return loc.setName(name);
    }

    @Override
    public NutsPath getLocationPath(NutsSession session) {
        String s=NutsUtilStrings.trimToNull(config.getLocation().getPath());
        if (s != null) {
            return NutsPath.of(s,session).toAbsolute(session.locations().getWorkspaceLocation());
        }
        return null;
    }

    @Override
    public NutsPath getStoreLocation() {
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
    public NutsPath getStoreLocation(NutsStoreLocation folderType,NutsSession session) {
        NutsStoreLocationsMap hlm = new NutsStoreLocationsMap(config.getStoreLocations());

//        String n = CoreNutsUtils.getArrItem(config.getStoreLocations(), folderType.ordinal());
        String n = hlm.get(folderType);
        if (temporary) {
            if (NutsBlankable.isBlank(n)) {
                n = folderType.toString().toLowerCase();
                n = n.trim();
            }
            return getStoreLocation().resolve(n);
        } else {
            switch (getStoreLocationStrategy(session)) {
                case STANDALONE: {
                    if (NutsBlankable.isBlank(n)) {
                        n = folderType.toString().toLowerCase();
                    }
                    n = n.trim();
                    return getStoreLocation().resolve(n);
                }
                case EXPLODED: {
                    NutsPath storeLocation = session.locations().getStoreLocation(folderType);
                    //uuid is added as
                    return storeLocation.resolve(NutsConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());

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

    public NutsRepositoryLocation getLocation() {
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
            this.config.setStoreLocationStrategy(session.locations().getRepositoryStoreLocationStrategy());
        }
        if (!Objects.equals(NutsRepositoryUtils.getRepoType(config),repositoryType)) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid Repository Type : expected %s, found %s" ,repositoryType, NutsRepositoryUtils.getRepoType(config))
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
                NutsRepository r = ((DefaultNutsRepositoryManager) session.repos())
                        .getModel()
                        .createRepository(
                                net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils.refToOptions(ref),
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
        NutsRepositoryHelper.of(repository).events().fireOnAddRepository(
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
                fireConfigurationChanged("user", session);
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
        NutsSessionUtils.checkSession(repository.getWorkspace(), session);
        boolean ok = false;
        if (force || (!session.config().isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.of(session).checkReadOnly();
            repository.security().setSession(session).checkAllowed(NutsConstants.Permissions.SAVE, "save");
            NutsPath file = getStoreLocation().resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!file.exists()) {
                created = true;
            }
            getStoreLocation().mkdirs();
            config.setConfigVersion(DefaultNutsWorkspace.VERSION_REPOSITORY_CONFIG);
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
//            if (NutsBlankable.isBlank(config.getConfigVersion())) {
//                config.setConfigVersion(repository.getWorkspace().getApiVersion());
//            }
            NutsElements.of(session).setSession(session).json().setValue(config)
                    .setNtf(false)
                    .print(file);
            configurationChanged = false;
            if (_LOG(session).isLoggable(Level.CONFIG)) {
                if (created) {
                    _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle(
                                    "{0} created repository {1} at {2}",
                                    CoreStringUtils.alignLeft(repository.getName(), 20) , repository.getName() ,
                                    getStoreLocation()
                                    ));
                } else {
                    _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log(NutsMessage.jstyle(
                            "{0} updated repository {1} at {2}",
                            CoreStringUtils.alignLeft(repository.getName(), 20) , repository.getName() ,
                            getStoreLocation()
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
            NutsRepositoryHelper.of(repository).events().fireOnRemoveRepository(new DefaultNutsRepositoryEvent(session, repository, r, "mirror", r, null));
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

    public List<NutsRepository> getMirrors(NutsSession session) {
        return Arrays.asList(repositoryRegistryHelper.getRepositories());
    }

    public NutsRepository addMirror(NutsAddRepositoryOptions options, NutsSession session) {
        if (!isSupportedMirroring(session)) {
            throw new NutsUnsupportedOperationException(session,NutsMessage.cstyle("unsupported operation '%s'","addMirror"));
        }
        if (options.isTemporary()) {
            return null;
        }
        NutsRepository repo = ((DefaultNutsRepositoryManager) session.repos())
                .getModel()
                .createRepository(
                        options,
                        repository, session
                );
        addMirror(repo, session);
        return repo;
    }

    @Override
    public NutsPath getTempMirrorsRoot(NutsSession session) {
        return getStoreLocation().resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public NutsPath getMirrorsRoot(NutsSession session) {
        return getStoreLocation().resolve(NutsConstants.Folders.REPOSITORIES);
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
            t = config.getEnv().get(key);
        }
        if (!NutsBlankable.isBlank(t)) {
            return t;
        }
        if (inherit) {
            t = session.config().getConfigProperty(key).getString();
            if (!NutsBlankable.isBlank(t)) {
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
            p.putAll(session.config().getConfigMap());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }


    private void config_setEnv(String property, String value, NutsSession session) {
        NutsRepositoryConfig config = getConfig(session);
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
}
