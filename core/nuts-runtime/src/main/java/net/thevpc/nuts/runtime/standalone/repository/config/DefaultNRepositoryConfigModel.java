package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryTagsListHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.util.NSpeedQualifiers;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NStoreLocationsMap;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class DefaultNRepositoryConfigModel extends AbstractNRepositoryConfigModel {

    private final NRepository repository;
    private final NSpeedQualifier speed;
    private final NPath storeLocation;
    private NRepositoryConfig config;
    private final Map<String, NUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;
    private int deployWeight;
    private boolean temporary;
    private String globalName;
    private boolean supportedMirroring;
    private final NRepositoryRegistryHelper repositoryRegistryHelper;
    private String repositoryName;
    private String repositoryType;
    private NWorkspace workspace;
    private NRepositoryRef repositoryRef;

    public DefaultNRepositoryConfigModel(NRepository repository, NAddRepositoryOptions options, NWorkspace workspace,
                                         NSpeedQualifier speed,
                                         boolean supportedMirroring, String repositoryType) {
        this.workspace = workspace;
        NAssert.requireNonNull(options, "repository options");
        NAssert.requireNonNull(options.getConfig(), "repository options config");
        this.repositoryRef = NRepositoryUtils.optionsToRef(options);
        String storeLocation = options.getLocation();
        NRepositoryConfig config = options.getConfig();
        String globalName = options.getConfig().getName();
        String repositoryName = options.getName();

        speed = speed == null ? NSpeedQualifier.NORMAL : speed;

        NAssert.requireNonBlank(repositoryType, "repository type");
        NAssert.requireNonBlank(repositoryName, "repository name");
        NAssert.requireNonBlank(globalName, "repository global name");
        NAssert.requireNonBlank(storeLocation, "repository store location");
        Path pfolder = Paths.get(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NInvalidRepositoryException(storeLocation, NMsg.ofC("unable to resolve root as a valid folder %s", storeLocation));
        }

        this.repositoryRegistryHelper = new NRepositoryRegistryHelper(repository.getWorkspace());
        this.repository = repository;
        this.repositoryName = repositoryName;
        this.globalName = globalName;
        this.storeLocation = NPath.of(storeLocation).toAbsolute(NWorkspaceExt.of().getConfigModel().getRepositoriesRoot());
        this.speed = speed;
        this.deployWeight = options.getDeployWeight();
        this.temporary = options.isTemporary();
        this.tags.addAll(new NRepositoryTagsListHelper().add(options.getConfig().getTags()).toSet());
        this.supportedMirroring = supportedMirroring;
        this.repositoryType = repositoryType;
        setConfig(config, false);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNRepositoryConfigModel.class);
    }

    public NRepository getRepository() {
        return repository;
    }

    public NWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

    public NRepositoryRef getRepositoryRef() {
        return new NRepositoryRef(repositoryRef);
    }

    public String getName() {
        return repositoryName;
    }

    public int getDeployWeight() {
        return deployWeight;
    }

//    public String getEnv(String key, String defaultValue, boolean inherit) {
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

//    public Map<String, String> getEnv(boolean inherit) {
//        Map<String, String> p = new LinkedHashMap<>();
//        if (inherit) {
//            p.putAll(repository.getWorkspace().env().getEnvMap());
//        }
//        if (config.getEnv() != null) {
//            p.putAll(config.getEnv());
//        }
//        return p;
//    }

    public void setEnv(String property, String value) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NBlankable.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env");
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env");
            }
        }
    }

    @Override
    public NSpeedQualifier getSpeed() {
        List<NSpeedQualifier> all = new ArrayList<>();
        boolean unavailable = false;
        if (speed == NSpeedQualifier.UNAVAILABLE) {
            unavailable = true;
        } else {
            all.add(speed);
        }
        if (isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NSpeedQualifier mspeed = mirror.config().getSpeed();
                if (mspeed == NSpeedQualifier.UNAVAILABLE) {
                    unavailable = true;
                } else {
                    all.add(mspeed);
                }
            }
        }
        if (all.isEmpty()) {
            if (unavailable) {
                return NSpeedQualifier.UNAVAILABLE;
            }
            return NSpeedQualifier.NORMAL;
        }
        return NSpeedQualifiers.max(all.toArray(new NSpeedQualifier[0]));
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
    public NRepositoryLocation getLocation() {
        NRepositoryLocation loc = config.getLocation();
        if (loc == null) {
            loc = NRepositoryLocation.of(null);
        }
        String name = config.getName();
        return loc.setName(name);
    }

    @Override
    public NPath getLocationPath() {
        String s = NStringUtils.trimToNull(config.getLocation().getPath());
        if (s != null) {
            return NPath.of(s).toAbsolute(NWorkspace.of().getWorkspaceLocation());
        }
        return null;
    }

    @Override
    public NPath getStoreLocation() {
        return storeLocation;
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        NStoreStrategy strategy = config.getStoreStrategy();
        if (strategy == null) {
            strategy = NStoreStrategy.values()[0];
        }
        return strategy;
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType) {
        NStoreLocationsMap hlm = new NStoreLocationsMap(config.getStoreLocations());
        String n = hlm.get(folderType);
        if (temporary) {
            if (NBlankable.isBlank(n)) {
                n = folderType.toString().toLowerCase();
                n = n.trim();
            }
            return getStoreLocation().resolve(n);
        } else {
            switch (getStoreStrategy()) {
                case STANDALONE: {
                    if (NBlankable.isBlank(n)) {
                        n = folderType.toString().toLowerCase();
                    }
                    n = n.trim();
                    return getStoreLocation().resolve(n);
                }
                case EXPLODED: {
                    NPath storeLocation = NWorkspace.of().getStoreLocation(folderType);
                    //uuid is added as
                    return storeLocation.resolve(NConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());

                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofC("unsupported strategy type %s", getStoreLocation()));
                }
            }
        }
    }

    public String getUuid() {
        return config.getUuid();
    }

    public void setConfig(NRepositoryConfig newConfig, boolean fireChange) {
        NAssert.requireNonBlank(newConfig, "repository config");
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            fireChange = true;
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreStrategy() == null) {
            fireChange = true;
            this.config.setStoreStrategy(NWorkspace.of().getRepositoryStoreStrategy());
        }
        if(config.getLocation()!=null && !NBlankable.isBlank(config.getLocation().getLocationType())) {
            // do not waste time on constructor to connect to internet and check repo type....
            //if (!Objects.equals(NRepositoryUtils.getRepoType(config), repositoryType)) {
            if (!Objects.equals(config.getLocation().getLocationType(), repositoryType)) {
                throw new NIllegalArgumentException(
                        NMsg.ofC("invalid Repository Type : expected %s, found %s", repositoryType, NRepositoryUtils.getRepoType(config))
                );
            }
        }
        tags.clear();
        if (this.config.getTags() != null) {
            for (String tag : this.config.getTags()) {
                if (!NBlankable.isBlank(tag)) {
                    tags.add(NStringUtils.trim(tag));
                }
            }
        }

        this.globalName = newConfig.getName();
        this.configUsers.clear();
        if (config.getUsers() != null) {
            for (NUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        removeAllMirrors();
        if (config.getMirrors() != null) {
            for (NRepositoryRef ref : config.getMirrors()) {
                NRepository r = NWorkspaceExt.of()
                        .getRepositoryModel()
                        .createRepository(
                                NRepositoryUtils.refToOptions(ref),
                                repository
                        );
                addMirror(r);
            }
        }
        if (fireChange) {
            fireConfigurationChanged("*");
        }
    }

    @Override
    public void addMirror(NRepository repo) {
        repositoryRegistryHelper.addRepository(repo);
        NSession session = repository.getWorkspace().currentSession();
        NRepositoryHelper.of(repository).events().fireOnAddRepository(
                new DefaultNRepositoryEvent(session, repository, repo, "mirror", null, repo)
        );
    }


    @Override
    public void setIndexEnabled(boolean enabled) {
        if (enabled != config.isIndexEnabled()) {
            config.setIndexEnabled(enabled);
            fireConfigurationChanged("index-enabled");
        }
    }

    @Override
    public boolean isIndexEnabled() {
        return config.isIndexEnabled();
    }


    @Override
    public void setUser(NUserConfig user) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged("user");
//        return this;
    }

    @Override
    public void removeUser(String userId) {
        if (configUsers.containsKey(userId)) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            configUsers.remove(userId);
            fireConfigurationChanged("user");
        }
//        return this;
    }

    @Override
    public NOptional<NUserConfig> findUser(String userId) {
        NUserConfig u = configUsers.get(userId);
        if (u == null) {
            if (NConstants.Users.ADMIN.equals(userId) || NConstants.Users.ANONYMOUS.equals(userId)) {
                u = new NUserConfig(userId, null, null, null);
                configUsers.put(userId, u);
                fireConfigurationChanged("user");
            }
        }
        return NOptional.ofNamed(u, "user " + userId);
    }

    @Override
    public NUserConfig[] findUsers() {
        return configUsers.values().toArray(new NUserConfig[0]);
    }

    public void setMirrorEnabled(String repoName, boolean enabled) {
        NRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            e.setEnabled(enabled);
            fireConfigurationChanged("mirror");
        }
    }

    ////
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!NWorkspace.of().isReadOnly() && isConfigurationChanged())) {
            NWorkspaceUtils.of(getWorkspace()).checkReadOnly();
            repository.security().checkAllowed(NConstants.Permissions.SAVE, "save");

            config.setConfigVersion(DefaultNWorkspace.VERSION_REPOSITORY_CONFIG);
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setTags(tags.toArray(new String[0]));
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
//            if (NutsBlankable.isBlank(config.getConfigVersion())) {
//                config.setConfigVersion(repository.getWorkspace().getApiVersion());
//            }
            boolean created=((NWorkspaceExt)workspace).store().saveRepoConfig(repository,config);

            configurationChanged = false;
            if (_LOG().isLoggable(Level.CONFIG)) {
                if (created) {
                    _LOGOP().level(Level.CONFIG).verb(NLogVerb.SUCCESS)
                            .log(NMsg.ofJ(
                                    "{0} created repository {1} at {2}",
                                    NStringUtils.formatAlign(repository.getName(), 20, NPositionType.FIRST), repository.getName(),
                                    getStoreLocation()
                            ));
                } else {
                    _LOGOP().level(Level.CONFIG).verb(NLogVerb.SUCCESS).log(NMsg.ofJ(
                            "{0} updated repository {1} at {2}",
                            NStringUtils.formatAlign(repository.getName(), 20, NPositionType.FIRST), repository.getName(),
                            getStoreLocation()
                    ));
                }
            }
            ok = true;
        }
        NException error = null;
        for (NRepository repo : getMirrors()) {
            try {
                NRepositoryConfigManager config = repo.config();
                if (config instanceof NRepositoryConfigManagerExt) {
                    ok |= ((NRepositoryConfigManagerExt) config)
                            .getModel()
                            .save(force);
                }
            } catch (NException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    public void save() {
        save(true);
    }

    @Override
    public void fireConfigurationChanged(String configName) {
        this.configurationChanged = true;
        NSession session = repository.getWorkspace().currentSession();
        DefaultNRepositoryEvent evt = new DefaultNRepositoryEvent(session, null, repository, "config." + configName, null, true);
        for (NRepositoryListener workspaceListener : repository.getRepositoryListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    @Override
    public void setEnabled(boolean enabled) {
        repositoryRef.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return repositoryRef.isEnabled();
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public void setTemporary(boolean transientRepository) {
        this.temporary = transientRepository;
    }

    @Override
    public boolean isIndexSubscribed() {
        NIndexStore s = getIndexStore();
        return s != null && s.isSubscribed();
    }

    private NIndexStore getIndexStore() {
        return NRepositoryExt.of(repository).getIndexStore();
    }

    @Override
    public void subscribeIndex() {
        NIndexStore s = getIndexStore();
        if (s != null) {
            s.subscribe();
        }
    }

    @Override
    public void unsubscribeIndex() {
        NIndexStore s = getIndexStore();
        if (s != null) {
            s.unsubscribe();
        }
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
    public void removeMirror(String repositoryId) {
        if (!isSupportedMirroring()) {
            throw new NUnsupportedOperationException(NMsg.ofC("unsupported operation '%s'", "removeMirror"));
        }
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        NSession session = repository.getWorkspace().currentSession();
        repository.security().checkAllowed(NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository r = repositoryRegistryHelper.removeRepository(repositoryId);
        if (r != null) {
            NRepositoryHelper.of(repository).events().fireOnRemoveRepository(new DefaultNRepositoryEvent(session, repository, r, "mirror", r, null));
        } else {
            throw new NRepositoryNotFoundException(repositoryId);
        }
//        return this;
    }

    //
//    public NutsRepository getMirror(String repositoryIdOrName) {
//        return getMirror(repositoryIdOrName, false);
//    }
    public NRepository getMirror(String repositoryIdPath) {
        NRepository r = findMirror(repositoryIdPath);
        if (r != null) {
            return r;
        }
        throw new NRepositoryNotFoundException(repositoryIdPath);
    }

    public NRepository findMirror(String repositoryNameOrId) {
        NSession session = repository.getWorkspace().currentSession();
        NRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().setTransitive(true).callWith(() -> mirror.config()
                        .findMirror(repositoryNameOrId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NRepository findMirrorById(String repositoryNameOrId) {
        NRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        NSession session = repository.getWorkspace().currentSession();
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().setTransitive(true).callWith(() -> mirror.config()
                        .findMirrorById(repositoryNameOrId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NRepository findMirrorByName(String repositoryNameOrId) {
        NRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        NSession session = repository.getWorkspace().currentSession();
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().setTransitive(true).callWith(() -> mirror.config()
                        .findMirrorByName(repositoryNameOrId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public List<NRepository> getMirrors() {
        return Arrays.asList(repositoryRegistryHelper.getRepositories());
    }

    public NRepository addMirror(NAddRepositoryOptions options) {
        if (!isSupportedMirroring()) {
            throw new NUnsupportedOperationException(NMsg.ofC("unsupported operation '%s'", "addMirror"));
        }
        if (options.isTemporary()) {
            return null;
        }
        NRepository repo = NWorkspaceExt.of()
                .getRepositoryModel()
                .createRepository(
                        options,
                        repository
                );
        addMirror(repo);
        return repo;
    }

    @Override
    public NPath getTempMirrorsRoot() {
        return getStoreLocation().resolve(NConstants.Folders.REPOSITORIES);
    }

    @Override
    public NPath getMirrorsRoot() {
        return getStoreLocation().resolve(NConstants.Folders.REPOSITORIES);
    }

    public NRepositoryConfig getStoredConfig() {
        return config;
    }

    //
    public void removeAllMirrors() {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        for (NRepository repo : repositoryRegistryHelper.getRepositories()) {
            removeMirror(repo.getUuid());
        }
    }

    public NRepositoryConfig getConfig() {
        return config;
    }

    public Map<String, String> toMap(boolean inherit) {
        return config_getEnv(inherit);
    }

    public Map<String, String> toMap() {
        return config_getEnv(true);
    }


    @Override
    public NOptional<NLiteral> get(String key, boolean inherit) {
        NOptional<NLiteral> o = config_getEnv(key, inherit);
        if (o.isBlank() && inherit) {
            return o.orElseUse(() -> NWorkspace.of().getConfigProperty(key));
        }
        return o;
    }


    public void set(String property, String value) {
        config_setEnv(property, value);
    }

    private NRepositoryConfigModel getConfig0() {
        return ((DefaultNRepoConfigManager) repository.config()).getModel();
    }


    ////////////////////////////////////////////


    public NOptional<NLiteral> config_getEnv(String key, boolean inherit) {
        NRepositoryConfigModel model = ((DefaultNRepoConfigManager) repository.config()).getModel();
        NRepositoryConfig config = model.getConfig();
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().get(key);
        }
        if (!NBlankable.isBlank(t)) {
            return NOptional.of(NLiteral.of(t));
        }
        if (inherit) {
            return NWorkspace.of().getConfigProperty(key);
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("repository property not found : %s", key));
    }

    private Map<String, String> config_getEnv(boolean inherit) {
        NRepositoryConfigModel model = ((DefaultNRepoConfigManager) repository.config()).getModel();
        NRepositoryConfig config = model.getConfig();
        Map<String, String> p = new LinkedHashMap<>();
        if (inherit) {
            p.putAll(NWorkspace.of().getConfigMap());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }


    private void config_setEnv(String property, String value) {
        NRepositoryConfig config = getConfig();
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NBlankable.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env");
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env");
            }
        }
    }
}
