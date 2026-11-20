package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryWithChildren;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.*;
import java.net.URL;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryTagsListHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.security.DefaultNRepositoryAccess;
import net.thevpc.nuts.runtime.standalone.util.NSpeedQualifiers;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NStoreLocationsMap;
import net.thevpc.nuts.core.NRepositoryLocation;
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
    private boolean configurationChanged = false;
    private final int deployWeight;
    private boolean temporary;
    private String globalName;
    private final boolean supportedMirroring;
    private final NRepositoryRegistryHelper repositoryRegistryHelper;
    private final String repositoryName;
    private final String repositoryType;
    private final NWorkspace workspace;
    private final NRepositoryRef repositoryRef;

    public DefaultNRepositoryConfigModel(NRepository repository, NRepositorySpec options, NWorkspace workspace,
                                         NSpeedQualifier speed,
                                         boolean supportedMirroring, String repositoryType) {
        this.workspace = workspace;
        NAssert.requireNamedNonNull(options, "repository options");
        this.repositoryRef = NRepositoryUtils.optionsToRef(options);
        String storeLocation = options.location();
        NRepositoryConfig config = options.toConfig();
        String globalName = options.name();
        String repositoryName = options.name();

        speed = speed == null ? NSpeedQualifier.NORMAL : speed;

        NAssert.requireNamedNonBlank(repositoryType, "repository type");
        NAssert.requireNamedNonBlank(repositoryName, "repository name");
        NAssert.requireNamedNonBlank(globalName, "repository global name");
        NAssert.requireNamedNonBlank(storeLocation, "repository store location");
        Path pfolder = Paths.get(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NInvalidRepositoryException(storeLocation, NMsg.ofC("unable to resolve root as a valid folder %s", storeLocation));
        }

        this.repositoryRegistryHelper = new NRepositoryRegistryHelper();
        this.repository = repository;
        this.repositoryName = repositoryName;
        this.globalName = globalName;
        this.storeLocation = NPath.of(storeLocation).toAbsolute(NWorkspaceExt.of().getConfigModel().getRepositoriesRoot());
        this.speed = speed;
        this.deployWeight = options.deployWeight();
        this.temporary = options.isTemporary();
        this.tags.addAll(new NRepositoryTagsListHelper().add(options.tags()).toSet());
        this.supportedMirroring = supportedMirroring;
        this.repositoryType = repositoryType;
        setConfig(config, false);
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNRepositoryConfigModel.class);
    }

    private static NRepositoryAccess findRepositoryAccess(NSecurityManager sec, String user, String repoUuid, String repoName) {
        if (user == null) {
            return null;
        }
        NRepositoryAccess acc = null;
        if (repoUuid != null) {
            acc = sec.getRepositoryAccess(user, repoUuid).orNull();
        }
        if ((acc == null || acc.remoteCredential() == null) && repoName != null) {
            acc = sec.getRepositoryAccess(user, repoName).orNull();
        }
        return acc;
    }

    private static List<NRepositoryAccess> findRepositoryAccessList(NSecurityManager sec, String repoUuid, String repoName) {
        List<NRepositoryAccess> list = repoUuid == null ? null : sec.getRepositoryAccessListByRepository(repoUuid);
        if (list == null || list.isEmpty()) {
            list = repoName == null ? null : sec.getRepositoryAccessListByRepository(repoName);
        }
        return list == null ? Collections.<NRepositoryAccess>emptyList() : list;
    }

    public NRepository getRepository() {
        return repository;
    }

    public NWorkspace getWorkspace() {
        return repository.workspace();
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
            if (config.env() != null) {
                config.env().remove(property);
                fireConfigurationChanged("env");
            }
        } else {
            if (config.env() == null) {
                config.env(new LinkedHashMap<>());
            }
            if (!value.equals(config.env().get(property))) {
                config.env().put(property, value);
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
                NSpeedQualifier mspeed = mirror.config().speed();
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
        return config.groups();
    }

    @Override
    public NRepositoryLocation getLocation() {
        NRepositoryLocation loc = config.location();
        if (loc == null) {
            loc = NRepositoryLocation.of(null);
        }
        String name = config.name();
        return loc.name(name);
    }

    @Override
    public NPath getLocationPath() {
        String s = NStringUtils.stripToNull(config.location().path());
        if (s != null) {
            s = resolveLocationWithAuth(s);
            return NPath.of(s).toAbsolute(NWorkspace.of().workspaceLocation());
        }
        return null;
    }

    private String resolveLocationWithAuth(String s) {
        if (s == null) {
            return null;
        }
        if (s.startsWith("http://") || s.startsWith("https://")) {
            try {
                URL u = new URL(s);
                if (u.getUserInfo() != null && !u.getUserInfo().isEmpty()) {
                    return s;
                }
                String repoUuid = (repository != null) ? repository.uuid() : (config != null ? config.uuid() : null);
                String repoName = (repository != null) ? repository.name() : (config != null ? config.name() : null);
                if (repoUuid == null && config != null) {
                    repoUuid = config.uuid();
                }
                if (repoName == null && config != null) {
                    repoName = config.name();
                }
                if (repository != null || repoUuid != null || repoName != null) {
                    NSecurityManager sec = NSecurityManager.of();
                    String user = sec.currentUsername();
                    NRepositoryAccess acc = findRepositoryAccess(sec, user, repoUuid, repoName);
                    if (acc == null || acc.remoteCredential() == null) {
                        acc = findRepositoryAccess(sec, NConstants.Users.ANONYMOUS, repoUuid, repoName);
                    }
                    if (acc == null || acc.remoteCredential() == null) {
                        acc = findRepositoryAccess(sec, "user", repoUuid, repoName);
                    }
                    if (acc == null || acc.remoteCredential() == null) {
                        acc = findRepositoryAccess(sec, "admin", repoUuid, repoName);
                    }
                    if (acc == null || acc.remoteCredential() == null) {
                        List<NRepositoryAccess> list = findRepositoryAccessList(sec, repoUuid, repoName);
                        for (NRepositoryAccess a : list) {
                            if (a.remoteCredential() != null) {
                                acc = a;
                                break;
                            }
                        }
                    }
                    if (acc == null || acc.remoteCredential() == null) {
                        NRepositoryAccessConfig[] repoUsers = NWorkspaceExt.of().getConfigModel().getRepositoryUsers();
                        if (repoUsers != null) {
                            for (NRepositoryAccessConfig ru : repoUsers) {
                                if ((repoUuid != null && Objects.equals(ru.repository(), repoUuid))
                                        || (repoName != null && Objects.equals(ru.repository(), repoName))) {
                                    if (!NBlankable.isBlank(ru.remoteCredential())) {
                                        NSecureToken tok = NSecureToken.parse(ru.remoteCredential());
                                        acc = new DefaultNRepositoryAccess(
                                                ru.userName(),
                                                ru.repository(),
                                                repoName,
                                                ru.remoteUserName(),
                                                tok,
                                                ru.remoteAuthType(),
                                                ru.permissions()
                                        );
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (acc != null) {
                        String remoteUser = acc.remoteUserName();
                        if (NBlankable.isBlank(remoteUser)) {
                            remoteUser = acc.userName();
                        }
                        if (NBlankable.isBlank(remoteUser) && !NBlankable.isBlank(user) && !NConstants.Users.ANONYMOUS.equals(user)) {
                            remoteUser = user;
                        }
                        NSecureToken token = acc.remoteCredential();
                        if (token != null) {
                            NRef<String> passRef = NRef.of();
                            sec.runWithSecret(token, new NSecretRunner() {
                                @Override
                                public void run(NSecureToken id, NSecureString secretm, java.util.function.Function<String, String> env) {
                                    secretm.doWithContent(cc -> passRef.set(new String(cc)));
                                }
                            });
                            String pass = passRef.get();
                            if (pass != null && remoteUser != null) {
                                String encodedUser = CoreIOUtils.urlEncodeString(remoteUser);
                                String encodedPass = CoreIOUtils.urlEncodeString(pass);
                                String authPart = encodedUser + ":" + encodedPass + "@";
                                String hostPort = u.getHost() + (u.getPort() > 0 ? (":" + u.getPort()) : "");
                                String file = u.getFile();
                                String ref = u.getRef();
                                String full = u.getProtocol() + "://" + authPart + hostPort + (file != null ? file : "");
                                if (ref != null && !ref.isEmpty()) {
                                    full += "#" + ref;
                                }
                                return full;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                _LOG().log(Level.FINE, () -> NMsg.ofC("Failed to resolve repository location with auth: %s", ex));
            }
        }
        return s;
    }

    @Override
    public NPath getStoreLocation() {
        return storeLocation;
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        NStoreStrategy strategy = config.storeStrategy();
        if (strategy == null) {
            strategy = NStoreStrategy.values()[0];
        }
        return strategy;
    }

    @Override
    public NPath getStoreLocation(NStoreType storeType) {
        NStoreLocationsMap hlm = new NStoreLocationsMap(config.storeLocations());
        String n = hlm.get(storeType);
        if (temporary) {
            if (NBlankable.isBlank(n)) {
                n = storeType.toString().toLowerCase();
                n = n.trim();
            }
            return getStoreLocation().resolve(n);
        } else {
            switch (getStoreStrategy()) {
                case STANDALONE: {
                    if (NBlankable.isBlank(n)) {
                        n = storeType.toString().toLowerCase();
                    }
                    n = n.trim();
                    return getStoreLocation().resolve(n);
                }
                case EXPLODED: {
                    NPath storeLocation = NPath.of(NStoreKey.of(storeType));
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
        return config.uuid();
    }

    public void setConfig(NRepositoryConfig newConfig, boolean fireChange) {
        NAssert.requireNamedNonBlank(newConfig, "repository config");
        this.config = newConfig;
        if (this.config.uuid() == null) {
            fireChange = true;
            this.config.uuid(UUID.randomUUID().toString());
        }
        if (this.config.storeStrategy() == null) {
            fireChange = true;
            this.config.storeStrategy(NWorkspace.of().repositoryStoreStrategy());
        }
        if (config.location() != null && !NBlankable.isBlank(config.location().locationType())) {
            // do not waste time on constructor to connect to internet and check repo type....
            //if (!Objects.equals(NRepositoryUtils.getRepoType(config), repositoryType)) {
            if (!Objects.equals(config.location().locationType(), repositoryType)) {
                throw new NIllegalArgumentException(
                        NMsg.ofC("invalid Repository Type : expected %s, found %s", repositoryType, NRepositoryUtils.getRepoType(config))
                );
            }
        }
        tags.clear();
        if (this.config.tags() != null) {
            for (String tag : this.config.tags()) {
                if (!NBlankable.isBlank(tag)) {
                    tags.add(NStringUtils.strip(tag));
                }
            }
        }

        this.globalName = newConfig.name();
        removeAllMirrors();
        if (config.mirrors() != null) {
            for (NRepositoryRef ref : config.mirrors()) {
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
        NSession session = repository.workspace().currentSession();
        NRepositoryHelper.of(repository).events().fireOnAddRepository(
                new DefaultNRepositoryEvent(session, repository, repo, "mirror", null, repo)
        );
    }


    @Override
    public void setIndexEnabled(boolean enabled) {
        if (enabled != config.isIndexEnabled()) {
            config.indexEnabled(enabled);
            fireConfigurationChanged("index-enabled");
        }
    }

    @Override
    public boolean isIndexEnabled() {
        return config.isIndexEnabled();
    }


    public void setMirrorEnabled(String repoName, boolean enabled) {
        NRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            e.enabled(enabled);
            fireConfigurationChanged("mirror");
        }
    }

    /// /
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!NWorkspace.of().isReadOnly() && isConfigurationChanged())) {
            NWorkspaceUtils.of().checkReadOnly();
            NSecurityManager.of().checkRepositoryAllowed(repository.uuid(), NConstants.Permissions.SAVE, "save");

            config.configVersion(DefaultNWorkspace.VERSION_REPOSITORY_CONFIG);
            if (config.env() != null && config.env().isEmpty()) {
                config.env(null);
            }
            config.tags(tags.toArray(new String[0]));
            config.mirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            boolean created = ((NWorkspaceExt) workspace).store().saveRepoConfig(repository, config);

            configurationChanged = false;
            if (_LOG().isLoggable(Level.CONFIG)) {
                if (created) {
                    _LOG()
                            .log(NMsg.ofC(
                                    "%s created repository %s at %s",
                                    NStringUtils.formatAlign(repository.name(), 20, NPositionType.FIRST), repository.name(),
                                    getStoreLocation()
                            ).withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS));
                } else {
                    _LOG().log(NMsg.ofC(
                            "%s updated repository %s at %s",
                            NStringUtils.formatAlign(repository.name(), 20, NPositionType.FIRST), repository.name(),
                            getStoreLocation()
                    ).withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS));
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
        NSession session = repository.workspace().currentSession();
        DefaultNRepositoryEvent evt = new DefaultNRepositoryEvent(session, null, repository, "config." + configName, null, true);
        for (NRepositoryListener workspaceListener : repository.repositoryListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    @Override
    public void setEnabled(boolean enabled) {
        repositoryRef.enabled(enabled);
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
        return NRepositoryExt.of(repository).indexStore();
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
        NSession session = repository.workspace().currentSession();
        NSecurityManager.of().checkRepositoryAllowed(repository.uuid(), NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository r = repositoryRegistryHelper.removeRepository(repositoryId);
        if (r != null) {
            NRepositoryHelper.of(repository).events().fireOnRemoveRepository(new DefaultNRepositoryEvent(session, repository, r, "mirror", r, null));
        } else {
            throw new NNoSuchElementException(NMsg.ofC("repository %s not found", repositoryId));
        }
//        return this;
    }

    //
//    public NutsRepository getMirror(String repositoryIdOrName) {
//        return getMirror(repositoryIdOrName, false);
//    }
    public NOptional<NRepository> getMirror(String repositoryNameOrId) {
        NSession session = repository.workspace().currentSession();
        NRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return NOptional.of(y);
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().transitive(true).callWith(() -> mirror.config()
                        .getMirror(repositoryNameOrId)).orNull();
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.uuid(), m.uuid()
                                )
                        );
                    }
                }

            }
            if (repository instanceof NRepositoryWithChildren) {
                NOptional<NRepository> child = ((NRepositoryWithChildren) repository).getChild(repositoryNameOrId);
                if (child.isPresent()) {
                    return child;
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("repository %s", repositoryNameOrId));
    }

    public NRepository findMirrorById(String repositoryNameOrId) {
        NRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        NSession session = repository.workspace().currentSession();
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().transitive(true).callWith(() -> mirror.config()
                        .findMirrorById(repositoryNameOrId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.uuid(), m.uuid()
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
        NSession session = repository.workspace().currentSession();
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NRepository mirror : getMirrors()) {
                NRepository m = session.copy().transitive(true).callWith(() -> mirror.config()
                        .findMirrorByName(repositoryNameOrId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.uuid(), m.uuid()
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

    public NRepository addMirror(NRepositorySpec options) {
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
            removeMirror(repo.uuid());
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
            return o.orElseGetOptionalFrom(() -> NWorkspace.of().getConfigProperty(key));
        }
        return o;
    }


    public void set(String property, String value) {
        config_setEnv(property, value);
    }

    private NRepositoryConfigModel getConfig0() {
        return ((DefaultNRepoConfigManager) repository.config()).getModel();
    }


    /// /////////////////////////////////////////


    public NOptional<NLiteral> config_getEnv(String key, boolean inherit) {
        NRepositoryConfigModel model = ((DefaultNRepoConfigManager) repository.config()).getModel();
        NRepositoryConfig config = model.getConfig();
        String t = null;
        if (config.env() != null) {
            t = config.env().get(key);
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
            p.putAll(NWorkspace.of().configMap());
        }
        if (config.env() != null) {
            p.putAll(config.env());
        }
        return p;
    }


    private void config_setEnv(String property, String value) {
        NRepositoryConfig config = getConfig();
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NBlankable.isBlank(value)) {
            if (config.env() != null) {
                config.env().remove(property);
                fireConfigurationChanged("env");
            }
        } else {
            if (config.env() == null) {
                config.env(new LinkedHashMap<>());
            }
            if (!value.equals(config.env().get(property))) {
                config.env().put(property, value);
                fireConfigurationChanged("env");
            }
        }
    }
}
