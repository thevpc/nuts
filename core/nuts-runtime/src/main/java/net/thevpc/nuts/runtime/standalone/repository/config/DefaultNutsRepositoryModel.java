package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNutsRepositoryDB;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.spi.NutsRepositorySelectorList;
import net.thevpc.nuts.spi.NutsRepositoryURL;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DefaultNutsRepositoryModel {

    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private final NutsWorkspace workspace;
    public NutsLogger LOG;

    public DefaultNutsRepositoryModel(NutsWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(workspace);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsRepositoryModel.class, session);
        }
        return LOG;
    }

    public NutsRepository[] getRepositories(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsRepository findRepositoryById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorById(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository findRepositoryByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorByName(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository findRepository(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirror(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.cstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )

                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException {
        NutsWorkspaceUtils.checkSession(getWorkspace(), session);
        if (DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NutsWorkspaceExt.of(getWorkspace()).getInstalledRepository();
        }
        NutsRepository r = findRepository(repositoryIdOrName, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(session, repositoryIdOrName);
    }

    public void removeRepository(String repositoryId, NutsSession session) {
        session.security().setSession(session).checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            session.config().save();
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(session.config());
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(session).events().fireOnRemoveRepository(new DefaultNutsWorkspaceEvent(session, repository, "repository", repository, null));
        }
    }

    public void removeAllRepositories(NutsSession session) {
        for (NutsRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid(), session);
        }
    }

    protected void addRepository(NutsRepository repo, NutsSession session, boolean temp) {
        repositoryRegistryHelper.addRepository(repo, session);
        session.config().save();
        if (!temp) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(session.config());
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            if (repo != null) {
                // repo would be null if the repo is not accessible
                // like for system repo, if not already created
                NutsWorkspaceUtils.of(session).events().fireOnAddRepository(
                        new DefaultNutsWorkspaceEvent(session, repo, "repository", null, repo)
                );
            }
        }
    }

    public NutsRepository addRepository(NutsAddRepositoryOptions options, NutsSession session) {
        //TODO excludedRepositoriesSet
//        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
//            return null;
//        }
        NutsRepository r = this.createRepository(options, null, session);
        addRepository(r, session, options.isTemporary());
        return r;
    }

    public NutsRepository createRepository(NutsAddRepositoryOptions options, NutsRepository parentRepository, NutsSession session) {
        return createRepository(options, null, parentRepository, session);
    }

    public NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository, NutsSession session) {
        NutsRepositoryModel repoModel = options.getRepositoryModel();
        if (rootFolder == null) {
            if (parentRepository == null) {
                NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(session.config());
                rootFolder = options.isTemporary() ?
                        cc.getModel().getTempRepositoriesRoot(session).toFile()
                        : cc.getModel().getRepositoriesRoot(session).toFile();
            } else {
                NutsRepositoryConfigManagerExt cc = NutsRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = (options.isTemporary() ? cc.getModel().getTempMirrorsRoot(session)
                        : cc.getModel().getMirrorsRoot(session)).toFile();
            }
        }
        if (repoModel != null) {
            NutsRepositoryConfig config = new NutsRepositoryConfig();
            String name = repoModel.getName();
            String uuid = repoModel.getUuid();
            if (NutsBlankable.isBlank(name)) {
                name = "custom";
            }
            if (NutsBlankable.isBlank(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
            config.setName(name);
            config.setType("custom");
            config.setUuid(uuid);
            config.setStoreLocationStrategy(repoModel.getStoreLocationStrategy());
            NutsAddRepositoryOptions options2 = new NutsAddRepositoryOptions();
            options2.setName(config.getName());
            options2.setConfig(config);
            options2.setDeployWeight(options.getDeployWeight());
            options2.setTemporary(true);
            options2.setEnabled(options.isEnabled());
            options2.setLocation(CoreIOUtils.resolveRepositoryPath(options2, rootFolder, session));
            return new NutsSimpleRepositoryWrapper(options2, session, null, repoModel);
        }

        options = options.copy();
        try {
            boolean temporary = options.isTemporary();
            NutsRepositoryConfig conf = options.getConfig();
            if (temporary) {
//                options.setLocation(options.getName());
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                options.setEnabled(true);
            } else if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                conf = loadRepository(NutsPath.of(options.getLocation(),session).resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), session);
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NutsInvalidRepositoryException(session, options.getLocation(),
                            NutsMessage.cstyle("invalid repository location ", options.getLocation())
                    );
                }
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            session.boot().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelectorList.ofAll(
                                            session.boot().getBootOptions().getRepositories(),
                                    DefaultNutsRepositoryDB.INSTANCE,session
                            ).acceptExisting(
                                    NutsRepositoryURL.of(options.getName(),
                                            conf.getLocation())
                            ));
                }
            } else {
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            session.boot().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelectorList.ofAll(
                                            session.boot().getBootOptions().getRepositories(),
                                    DefaultNutsRepositoryDB.INSTANCE,session
                            ).acceptExisting(
                                    NutsRepositoryURL.of(options.getName(),
                                            conf.getLocation())
                            ));
                }
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
            }
            if (NutsBlankable.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            if (NutsBlankable.isBlank(conf.getType())
                    && NutsBlankable.isBlank(conf.getLocation())
                    && !NutsBlankable.isBlank(options.getLocation())
                    && NutsPath.of(options.getLocation(), session).isFile()
            ) {
                conf.setType("nuts");
                conf.setLocation(options.getLocation());
            }
            String repositoryType = conf.getType();
            String location = conf.getLocation();
            if (NutsBlankable.isBlank(repositoryType)) {
                if (!NutsBlankable.isBlank(location)) {
                    NutsRepositoryURL nru = NutsRepositoryURL.of(location);
                    conf.setType(nru.getType());
                    conf.setLocation(nru.getLocation());
                }
            }


            NutsRepositoryFactoryComponent factory_ = session.extensions()
                    .setSession(session)
                    .createSupported(NutsRepositoryFactoryComponent.class, false, conf);
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, session, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            if (options.isTemporary()) {
                if (NutsBlankable.isBlank(conf.getType())) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("unable to detect valid type for temporary repository"));
                } else {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("invalid repository type %s", conf.getType()));
                }
            } else {
                if (NutsBlankable.isBlank(conf.getType())) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("unable to detect valid type for repository %s",options.getName()));
                } else {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("invalid repository type %s", conf.getType()));
                }
            }
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    public NutsRepository addRepository(String repositoryNamedUrl, NutsSession session) {
        NutsWorkspaceUtils.checkSession(getWorkspace(), session);
        NutsRepositoryURL r = null;
        try {
            r = NutsRepositoryURL.of(repositoryNamedUrl,DefaultNutsRepositoryDB.INSTANCE,session);
        } catch (Exception ex) {
            throw new NutsInvalidRepositoryException(session, repositoryNamedUrl, NutsMessage.cstyle("invalid repository definition"));
        }
        NutsAddRepositoryOptions options = NutsRepositorySelectorHelper.createRepositoryOptions(r, true, session);
        return addRepository(options, session);
    }

    public NutsRepositoryConfig loadRepository(NutsPath file, String name, NutsSession session) {
        NutsRepositoryConfig conf = null;
        if (file.isRegularFile() && file.getPermissions().contains(NutsPathPermission.CAN_READ)) {
            byte[] bytes= file.readAllBytes();
            try {
                NutsElements elem = NutsElements.of(session);
                Map<String, Object> a_config0 = elem.json().parse(bytes, Map.class);
                String version = (String) a_config0.get("configVersion");
                if (version == null) {
                    version = session.getWorkspace().getApiVersion().toString();
                }
                int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = elem.json().parse(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                if (session.boot().getBootOptions().isRecover()) {
                    onLoadRepositoryError(file, name, null, ex, session);
                } else {
                    throw ex;
                }
            }
        }
        return conf;
    }

    public NutsRepositorySPI toRepositorySPI(NutsRepository repo) {
        return (NutsRepositorySPI) repo;
    }

    private void onLoadRepositoryError(NutsPath file, String name, String uuid, Throwable ex, NutsSession session) {
        NutsWorkspaceConfigManager wconfig = session.config().setSession(session);
        NutsBootManager wboot = session.boot().setSession(session);
        NutsWorkspaceEnvManager wenv = session.env().setSession(session);
        if (wconfig.isReadOnly()) {
            throw new NutsIOException(session, NutsMessage.cstyle("error loading repository %s", file), ex);
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log(
                NutsMessage.jstyle("erroneous repository config file. Unable to load file {0} : {1}", file, ex));
        NutsPath logError = session.locations().getStoreLocation(getWorkspace().getApiId(), NutsStoreLocation.LOG)
                .resolve("invalid-config");
        try {
            logError.mkParentDirs();
        } catch (Exception ex1) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to log repository error while loading config file %s : %s", file, ex1), ex);
        }
        NutsPath newfile = logError.resolve(fileName + ".json");
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile));
        try {
            Files.move(file.toFile(), newfile.toFile());
        } catch (IOException e) {
            throw new NutsIOException(session, NutsMessage.cstyle("nable to load and re-create repository config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").getOutputStream())) {
            o.printf("workspace.path:%s%n", session.locations().getWorkspaceLocation());
            o.printf("repository.path:%s%n", file);
            o.printf("workspace.options:%s%n", wboot.getBootOptions().formatter().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.printf("location." + location.id() + ":%s%n", session.locations().getStoreLocation(location));
            }
            o.printf("java.class.path:%s%n", System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

}
