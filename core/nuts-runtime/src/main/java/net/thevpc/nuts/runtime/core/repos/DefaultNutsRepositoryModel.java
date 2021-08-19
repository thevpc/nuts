package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repos.NutsRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repos.NutsSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DefaultNutsRepositoryModel {

    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    public NutsLogger LOG;
    private NutsWorkspace workspace;

    public DefaultNutsRepositoryModel(NutsWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(workspace);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.workspace.log().setSession(session).of(DefaultNutsRepositoryModel.class);
        }
        return LOG;
    }

    public NutsRepositoryFilterManager filter() {
        return getWorkspace().filters().repository();
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
        getWorkspace().security().setSession(session).checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(getWorkspace().config());
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
        if (!temp) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(getWorkspace().config());
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
                NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(getWorkspace().config());
                rootFolder = options.isTemporary() ? Paths.get(cc.getModel().getTempRepositoriesRoot(session))
                        : Paths.get(cc.getModel().getRepositoriesRoot(session));
            } else {
                NutsRepositoryConfigManagerExt cc = NutsRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = options.isTemporary() ? cc.getModel().getTempMirrorsRoot(session)
                        : cc.getModel().getMirrorsRoot(session);
            }
        }
        if (repoModel != null) {
            NutsRepositoryConfig config = new NutsRepositoryConfig();
            String name = repoModel.getName();
            String uuid = repoModel.getUuid();
            if (CoreStringUtils.isBlank(name)) {
                name = "custom";
            }
            if (CoreStringUtils.isBlank(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
            config.setName(name);
            config.setType("custom");
            config.setUuid(uuid);
            config.setStoreLocationStrategy(repoModel.getStoreLocationStrategy());
            NutsAddRepositoryOptions options2 = new NutsAddRepositoryOptions();
            options2.setName(config.getName());
            options2.setConfig(config);
            options2.setDeployOrder(options.getDeployOrder());
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
                conf = loadRepository(Paths.get(options.getLocation(), NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), getWorkspace(), session);
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
                            session.getWorkspace().env().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelector.parse(session.getWorkspace().env().getBootOptions().getRepositories()).acceptExisting(
                                    options.getName(),
                                    conf.getLocation()
                            ));
                }
            } else {
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            session.getWorkspace().env().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelector.parse(session.getWorkspace().env().getBootOptions().getRepositories()).acceptExisting(
                                    options.getName(),
                                    conf.getLocation()
                            ));
                }
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
            }
            if (CoreStringUtils.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            NutsRepositoryFactoryComponent factory_ = getWorkspace().extensions()
                    .setSession(session)
                    .createSupported(NutsRepositoryFactoryComponent.class, conf);
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, session, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            if (options.isTemporary()) {
                if (CoreStringUtils.isBlank(conf.getType())) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("unable to detect valid type for temporary repository"));
                } else {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("invalid repository type %s", conf.getType()));
                }
            } else {
                if (CoreStringUtils.isBlank(conf.getType())) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.cstyle("unable to detect valid type for repository"));
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
        NutsRepositorySelector.Selection r = null;
        try {
            r = NutsRepositorySelector.parseSelection(repositoryNamedUrl);
        } catch (Exception ex) {
            throw new NutsInvalidRepositoryException(session, repositoryNamedUrl, NutsMessage.cstyle("invalid repository definition"));
        }
        NutsAddRepositoryOptions options = NutsRepositorySelector.createRepositoryOptions(r, true, session);
        return addRepository(options, session);
    }

    public NutsRepositoryConfig loadRepository(Path file, String name, NutsWorkspace ws, NutsSession session) {
        NutsRepositoryConfig conf = null;
        if (Files.isRegularFile(file) && Files.isReadable(file)) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(file);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            try {
                Map<String, Object> a_config0 = ws.elem().setSession(session).setContentType(NutsContentType.JSON).parse(bytes, Map.class);
                String version = (String) a_config0.get("configVersion");
                if (version == null) {
                    version = ws.getApiVersion().toString();
                }
                int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = ws.elem().setSession(session).setContentType(NutsContentType.JSON).parse(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                if (session.getWorkspace().env().getBootOptions().isRecover()) {
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

    private void onLoadRepositoryError(Path file, String name, String uuid, Throwable ex, NutsSession session) {
        NutsWorkspaceConfigManager wconfig = getWorkspace().config().setSession(session);
        NutsWorkspaceEnvManager wenv = getWorkspace().env().setSession(session);
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("error loading repository " + file.toString(), new IOException(ex));
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("erroneous repository config file. Unable to load file {0} : {1}", new Object[]{file, ex});
        Path logError = Paths.get(getWorkspace().locations().getStoreLocation(getWorkspace().getApiId(), NutsStoreLocation.LOG))
                .resolve("invalid-config");
        try {
            CoreIOUtils.mkdirs(logError);
        } catch (Exception ex1) {
            throw new UncheckedIOException("unable to log repository error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile.toString());
        try {
            Files.move(file, newfile);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load and re-create repository config file " + file.toString() + " : " + e.toString(), new IOException(ex));
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").toFile())) {
            o.println("workspace.path:" + workspace.locations().getWorkspaceLocation());
            o.println("repository.path:" + file);
            o.println("workspace.options:" + wenv.getBootOptions().formatter().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":" + workspace.locations().getStoreLocation(location));
            }
            o.println("java.class.path:" + System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

}
