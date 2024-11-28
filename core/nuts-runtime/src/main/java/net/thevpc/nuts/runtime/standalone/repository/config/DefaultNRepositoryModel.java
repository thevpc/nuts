package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
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
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

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

    public DefaultNRepositoryModel(NWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NRepositoryRegistryHelper(workspace);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNRepositoryModel.class);
    }

    public NRepository[] getRepositories() {
        return repositoryRegistryHelper.getRepositories();
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public NOptional<NRepository> findRepositoryById(String repositoryId) {
        NRepository y = repositoryRegistryHelper.findRepositoryById(repositoryId);
        if (y != null) {
            return NOptional.of(y);
        }
        NSession session = workspace.currentSession();
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = session.copy().setTransitive(true).callWith(() -> child.config()
                        .findMirrorById(repositoryId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryId, finalY.getUuid(), m.getUuid()
                        ));
                    }
                }
            }
        }
        return NOptional.ofNamed(y, "repository with Id : " + repositoryId);
    }

    public NOptional<NRepository> findRepositoryByName(String repositoryName) {
        NRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryName);
        if (y != null) {
            return NOptional.of(y);
        }
        NSession session = workspace.currentSession();
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = session.copy().setTransitive(true).callWith(() -> child.config()
                        .findMirrorByName(repositoryName));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryName, finalY.getUuid(), m.getUuid()
                        ));
                    }
                }
            }
        }
        return NOptional.ofNamed(y, "repository with name : " + repositoryName);
    }

    public NOptional<NRepository> findRepository(String repositoryNameOrId) {
        NRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return NOptional.of(y);
        }
        NSession session = workspace.currentSession();
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = session.copy().setTransitive(true).callWith(() -> child.config()
                        .findMirror(repositoryNameOrId)
                );
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryNameOrId, finalY.getUuid(), m.getUuid()
                        ));
                    }
                }
            }
        }
        return NOptional.ofNamed(y, "repository with name or id : " + repositoryNameOrId);
    }

    public NRepository getRepository(String repositoryIdOrName) throws NRepositoryNotFoundException {
        if (DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NWorkspaceExt.of().getInstalledRepository();
        }
        return findRepository(repositoryIdOrName).get();
    }

    public void removeRepository(String repositoryId) {
        NSession session = workspace.currentSession();
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            NConfigs.of().save();
            NConfigsExt config = NConfigsExt.of(NConfigs.of());
            config.getModel().fireConfigurationChanged("config-main", ConfigEventType.MAIN);
            NWorkspaceUtils.of(workspace).events().fireOnRemoveRepository(new DefaultNWorkspaceEvent(session, repository, "repository", repository, null));
        }
    }

    public void removeAllRepositories() {
        for (NRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid());
        }
    }

    protected void addRepository(NRepository repo, boolean temp, boolean enabled) {
        repositoryRegistryHelper.addRepository(repo);
        repo.config().setEnabled(enabled);
//        NConfigs.of(session).save();
        if (!temp) {
            NSession session = workspace.currentSession();
            NConfigsExt config = NConfigsExt.of(NConfigs.of());
            config.getModel().fireConfigurationChanged("config-main", ConfigEventType.MAIN);
            if (repo != null) {
                // repo would be null if the repo is not accessible
                // like for system repo, if not already created
                NWorkspaceUtils.of(workspace).events().fireOnAddRepository(
                        new DefaultNWorkspaceEvent(session, repo, "repository", null, repo)
                );
            }
        }
    }

    public NRepository addRepository(NAddRepositoryOptions options) {
        //TODO excludedRepositoriesSet
//        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
//            return null;
//        }
        NRepository r = this.createRepository(options, null);
        if (r == null) {
            return null;/*fail safe and cannot load*/
        }
        addRepository(r, options.isTemporary(), options.isEnabled());
        return r;
    }

    public NRepository createRepository(NAddRepositoryOptions options, NRepository parentRepository) {
        return createRepository(options, null, parentRepository);
    }

    public NRepository createRepository(NAddRepositoryOptions options, Path rootFolder, NRepository parentRepository) {
        NSession session = workspace.currentSession();
        NRepositoryModel repoModel = options.getRepositoryModel();
        if (rootFolder == null) {
            if (parentRepository == null) {
                NConfigsExt cc = NConfigsExt.of(NConfigs.of());
                rootFolder = options.isTemporary() ?
                        cc.getModel().getTempRepositoriesRoot().toPath().get()
                        : cc.getModel().getRepositoriesRoot().toPath().get();
            } else {
                NRepositoryConfigManagerExt cc = NRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = (options.isTemporary() ? cc.getModel().getTempMirrorsRoot()
                        : cc.getModel().getMirrorsRoot()).toPath().get();
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
            config.setStoreStrategy(repoModel.getStoreStrategy());
            NAddRepositoryOptions options2 = new NAddRepositoryOptions();
            options2.setName(config.getName());
            options2.setConfig(config);
            options2.setDeployWeight(options.getDeployWeight());
            options2.setTemporary(true);
            options2.setEnabled(options.isEnabled());
            options2.setLocation(CoreIOUtils.resolveRepositoryPath(options2, rootFolder));
            return new NSimpleRepositoryWrapper(options2, workspace, null, repoModel);
        }

        options = options.copy();
        try {
            boolean temporary = options.isTemporary();
            NRepositoryConfig conf = options.getConfig();
            if (temporary) {
//                options.setLocation(options.getName());
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
                options.setEnabled(true);
            } else if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
                conf = loadRepository(NPath.of(options.getLocation()).resolve(NConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName());
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NInvalidRepositoryException(options.getLocation(),
                            NMsg.ofC("invalid repository location ", options.getLocation())
                    );
                }
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            NBootManager.of().getBootOptions().getRepositories() == null
                                    || NRepositorySelectorList.of(
                                    NBootManager.of().getBootOptions().getRepositories().orNull(),
                                    NRepositoryDB.of()
                            ).get().acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
            } else {
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            NBootManager.of().getBootOptions().getRepositories() == null
                                    || NRepositorySelectorList.of(
                                    NBootManager.of().getBootOptions().getRepositories().orNull(),
                                    NRepositoryDB.of()
                            ).get().acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
            }
            if (NBlankable.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            if (NBlankable.isBlank(conf.getLocation())
                    && !NBlankable.isBlank(options.getLocation())
                //&& NPath.of(options.getLocation(), session).isFile()
            ) {
                conf.setLocation(NRepositoryLocation.of(options.getLocation()));
            }

            NRepositoryFactoryComponent factory_ = NExtensions.of()
                    .createComponent(NRepositoryFactoryComponent.class, conf).orNull();
            if (factory_ != null) {
                NRepository r = factory_.create(options, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            String repoType = NRepositoryUtils.getRepoType(conf);
            if (options.isTemporary()) {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(options.getName(), NMsg.ofC("unable to detect valid type for temporary repository %s", conf.getLocation()));
                } else {
                    throw new NInvalidRepositoryException(options.getName(), NMsg.ofC("invalid repository type %s", repoType));
                }
            } else {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(options.getName(), NMsg.ofC("unable to detect valid type for repository %s", options.getName()));
                } else {
                    throw new NInvalidRepositoryException(options.getName(), NMsg.ofC("invalid repository type %s", repoType));
                }
            }
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    public NRepository addRepository(String repositoryNamedUrl) {
        NRepositoryLocation r = NRepositoryLocation.of(repositoryNamedUrl, NRepositoryDB.of()).get();
        NAddRepositoryOptions options = NRepositorySelectorHelper.createRepositoryOptions(r, true);
        return addRepository(options);
    }

    public NRepositoryConfig loadRepository(NPath file, String name) {
        NRepositoryConfig conf = null;
        if (file.isRegularFile() && file.getPermissions().contains(NPathPermission.CAN_READ)) {
            byte[] bytes = file.readBytes();
            try {
                NSession session = workspace.currentSession();
                NElements elem = NElements.of();
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
                NSession session = workspace.currentSession();
                if (NBootManager.of().getBootOptions().getRecover().orElse(false)) {
                    onLoadRepositoryError(file, name, null, ex);
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

    private void onLoadRepositoryError(NPath file, String name, String uuid, Throwable ex) {
        NConfigs wconfig = NConfigs.of();
        NBootManager wboot = NBootManager.of();
        NEnvs wenv = NEnvs.of();
        if (wconfig.isReadOnly()) {
            throw new NIOException(NMsg.ofC("error loading repository %s", file), ex);
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        _LOG().with().level(Level.SEVERE).verb(NLogVerb.FAIL).log(
                NMsg.ofJ("erroneous repository config file. Unable to load file {0} : {1}", file, ex));
        NPath logError = NLocations.of().getStoreLocation(getWorkspace().getApiId(), NStoreType.LOG)
                .resolve("invalid-config");
        try {
            logError.mkParentDirs();
        } catch (Exception ex1) {
            throw new NIOException(NMsg.ofC("unable to log repository error while loading config file %s : %s", file, ex1), ex);
        }
        NPath newfile = logError.resolve(fileName + ".json");
        _LOG().with().level(Level.SEVERE).verb(NLogVerb.FAIL)
                .log(NMsg.ofJ("erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile));
        try {
            Files.move(file.toPath().get(), newfile.toPath().get());
        } catch (IOException e) {
            throw new NIOException(NMsg.ofC("nable to load and re-create repository config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").getOutputStream())) {
            o.printf("workspace.path:%s%n", NLocations.of().getWorkspaceLocation());
            o.printf("repository.path:%s%n", file);
            o.printf("workspace.options:%s%n", wboot.getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(false)));
            for (NStoreType location : NStoreType.values()) {
                o.printf("location." + location.id() + ":%s%n", NLocations.of().getStoreLocation(location));
            }
            o.printf("java.class.path:%s%n", System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

}
