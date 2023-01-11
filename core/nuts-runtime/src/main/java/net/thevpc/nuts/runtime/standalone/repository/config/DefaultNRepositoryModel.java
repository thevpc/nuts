package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.NSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DefaultNRepositoryModel {

    private final NRepositoryRegistryHelper repositoryRegistryHelper;
    private final NWorkspace workspace;
    public NLogger LOG;

    public DefaultNRepositoryModel(NWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NRepositoryRegistryHelper(workspace);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNRepositoryModel.class, session);
        }
        return LOG;
    }

    public NRepository[] getRepositories(NSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public NRepository findRepositoryById(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorById(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NRepository findRepositoryByName(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorByName(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NRepository findRepository(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirror(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )

                        );
                    }
                }
            }
        }
        return y;
    }

    public NRepository getRepository(String repositoryIdOrName, NSession session) throws NRepositoryNotFoundException {
        NSessionUtils.checkSession(getWorkspace(), session);
        if (DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NWorkspaceExt.of(getWorkspace()).getInstalledRepository();
        }
        NRepository r = findRepository(repositoryIdOrName, session);
        if (r != null) {
            return r;
        }
        throw new NRepositoryNotFoundException(session, repositoryIdOrName);
    }

    public void removeRepository(String repositoryId, NSession session) {
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository repository = repositoryRegistryHelper.removeRepository(repositoryId, session);
        if (repository != null) {
            NConfigs.of(session).save();
            NConfigsExt config = NConfigsExt.of(NConfigs.of(session));
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            NWorkspaceUtils.of(session).events().fireOnRemoveRepository(new DefaultNWorkspaceEvent(session, repository, "repository", repository, null));
        }
    }

    public void removeAllRepositories(NSession session) {
        for (NRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid(), session);
        }
    }

    protected void addRepository(NRepository repo, NSession session, boolean temp, boolean enabled) {
        repositoryRegistryHelper.addRepository(repo, session);
        repo.setEnabled(enabled, session);
        NConfigs.of(session).save();
        if (!temp) {
            NConfigsExt config = NConfigsExt.of(NConfigs.of(session));
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            if (repo != null) {
                // repo would be null if the repo is not accessible
                // like for system repo, if not already created
                NWorkspaceUtils.of(session).events().fireOnAddRepository(
                        new DefaultNWorkspaceEvent(session, repo, "repository", null, repo)
                );
            }
        }
    }

    public NRepository addRepository(NAddRepositoryOptions options, NSession session) {
        //TODO excludedRepositoriesSet
//        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
//            return null;
//        }
        NRepository r = this.createRepository(options, null, session);
        addRepository(r, session, options.isTemporary(),options.isEnabled());
        return r;
    }

    public NRepository createRepository(NAddRepositoryOptions options, NRepository parentRepository, NSession session) {
        return createRepository(options, null, parentRepository, session);
    }

    public NRepository createRepository(NAddRepositoryOptions options, Path rootFolder, NRepository parentRepository, NSession session) {
        NRepositoryModel repoModel = options.getRepositoryModel();
        if (rootFolder == null) {
            if (parentRepository == null) {
                NConfigsExt cc = NConfigsExt.of(NConfigs.of(session));
                rootFolder = options.isTemporary() ?
                        cc.getModel().getTempRepositoriesRoot(session).toFile()
                        : cc.getModel().getRepositoriesRoot(session).toFile();
            } else {
                NRepositoryConfigManagerExt cc = NRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = (options.isTemporary() ? cc.getModel().getTempMirrorsRoot(session)
                        : cc.getModel().getMirrorsRoot(session)).toFile();
            }
        }
        if (repoModel != null) {
            NRepositoryConfig config = new NRepositoryConfig();
            String name = repoModel.getName();
            String uuid = repoModel.getUuid();
            if (NBlankable.isBlank(name)) {
                name = "custom";
            }
            if (NBlankable.isBlank(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
            config.setName(name);
            config.setLocation(NRepositoryLocation.of("custom@"));
            config.setUuid(uuid);
            config.setStoreLocationStrategy(repoModel.getStoreLocationStrategy());
            NAddRepositoryOptions options2 = new NAddRepositoryOptions();
            options2.setName(config.getName());
            options2.setConfig(config);
            options2.setDeployWeight(options.getDeployWeight());
            options2.setTemporary(true);
            options2.setEnabled(options.isEnabled());
            options2.setLocation(CoreIOUtils.resolveRepositoryPath(options2, rootFolder, session));
            return new NSimpleRepositoryWrapper(options2, session, null, repoModel);
        }

        options = options.copy();
        try {
            boolean temporary = options.isTemporary();
            NRepositoryConfig conf = options.getConfig();
            if (temporary) {
//                options.setLocation(options.getName());
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                options.setEnabled(true);
            } else if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                conf = loadRepository(NPath.of(options.getLocation(),session).resolve(NConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), session);
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NInvalidRepositoryException(session, options.getLocation(),
                            NMsg.ofC("invalid repository location ", options.getLocation())
                    );
                }
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            NBootManager.of(session).getBootOptions().getRepositories() == null
                                    || NRepositorySelectorList.ofAll(
                                            NBootManager.of(session).getBootOptions().getRepositories().orNull(),
                                    NRepositoryDB.of(session),session
                            ).acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
            } else {
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            NBootManager.of(session).getBootOptions().getRepositories() == null
                                    || NRepositorySelectorList.ofAll(
                                            NBootManager.of(session).getBootOptions().getRepositories().orNull(),
                                    NRepositoryDB.of(session),session
                            ).acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
            }
            if (NBlankable.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            if (NBlankable.isBlank(conf.getLocation())
                    && !NBlankable.isBlank(options.getLocation())
                    && NPath.of(options.getLocation(), session).isFile()
            ) {
                conf.setLocation(NRepositoryLocation.of(options.getLocation()));
            }

            NRepositoryFactoryComponent factory_ = session.extensions()
                    .setSession(session)
                    .createSupported(NRepositoryFactoryComponent.class, false, conf);
            if (factory_ != null) {
                NRepository r = factory_.create(options, session, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            String repoType = NRepositoryUtils.getRepoType(conf);
            if (options.isTemporary()) {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(session, options.getName(), NMsg.ofPlain("unable to detect valid type for temporary repository"));
                } else {
                    throw new NInvalidRepositoryException(session, options.getName(), NMsg.ofC("invalid repository type %s", repoType));
                }
            } else {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(session, options.getName(), NMsg.ofC("unable to detect valid type for repository %s",options.getName()));
                } else {
                    throw new NInvalidRepositoryException(session, options.getName(), NMsg.ofC("invalid repository type %s", repoType));
                }
            }
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    public NRepository addRepository(String repositoryNamedUrl, NSession session) {
        NSessionUtils.checkSession(getWorkspace(), session);
        NRepositoryLocation r = null;
        try {
            r = NRepositoryLocation.of(repositoryNamedUrl, NRepositoryDB.of(session),session);
        } catch (Exception ex) {
            throw new NInvalidRepositoryException(session, repositoryNamedUrl, NMsg.ofPlain("invalid repository definition"));
        }
        NAddRepositoryOptions options = NRepositorySelectorHelper.createRepositoryOptions(r, true, session);
        return addRepository(options, session);
    }

    public NRepositoryConfig loadRepository(NPath file, String name, NSession session) {
        NRepositoryConfig conf = null;
        if (file.isRegularFile() && file.getPermissions().contains(NPathPermission.CAN_READ)) {
            byte[] bytes= file.readBytes();
            try {
                NElements elem = NElements.of(session);
                Map<String, Object> a_config0 = elem.json().parse(bytes, Map.class);
                NVersion version = NVersion.of((String) a_config0.get("configVersion")).orNull();
                if (version == null || version.isBlank()) {
                    version = session.getWorkspace().getApiVersion();
                }
                int buildNumber = CoreNUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = elem.json().parse(file, NRepositoryConfig.class);
            } catch (RuntimeException ex) {
                if (NBootManager.of(session).getBootOptions().getRecover().orElse(false)) {
                    onLoadRepositoryError(file, name, null, ex, session);
                } else {
                    throw ex;
                }
            }
        }
        return conf;
    }

    public NRepositorySPI toRepositorySPI(NRepository repo) {
        return (NRepositorySPI) repo;
    }

    private void onLoadRepositoryError(NPath file, String name, String uuid, Throwable ex, NSession session) {
        NConfigs wconfig = NConfigs.of(session).setSession(session);
        NBootManager wboot = NBootManager.of(session);
        NEnvs wenv = NEnvs.of(session);
        if (wconfig.isReadOnly()) {
            throw new NIOException(session, NMsg.ofC("error loading repository %s", file), ex);
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().session(session).level(Level.SEVERE).verb(NLoggerVerb.FAIL).log(
                NMsg.ofJ("erroneous repository config file. Unable to load file {0} : {1}", file, ex));
        NPath logError = NLocations.of(session).getStoreLocation(getWorkspace().getApiId(), NStoreLocation.LOG)
                .resolve("invalid-config");
        try {
            logError.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(session, NMsg.ofC("unable to log repository error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        LOG.with().session(session).level(Level.SEVERE).verb(NLoggerVerb.FAIL)
                .log(NMsg.ofJ("erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile));
        try {
            Files.move(file.toFile(), newfile.toFile());
        } catch (IOException e) {
            throw new NIOException(session, NMsg.ofC("nable to load and re-create repository config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").getOutputStream())) {
            o.printf("workspace.path:%s%n", NLocations.of(session).getWorkspaceLocation());
            o.printf("repository.path:%s%n", file);
            o.printf("workspace.options:%s%n", wboot.getBootOptions().toCommandLine(new NWorkspaceOptionsConfig().setCompact(false)));
            for (NStoreLocation location : NStoreLocation.values()) {
                o.printf("location." + location.id() + ":%s%n", NLocations.of(session).getStoreLocation(location));
            }
            o.printf("java.class.path:%s%n", System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

}
