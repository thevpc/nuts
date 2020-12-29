package net.thevpc.nuts.runtime.standalone.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.main.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.main.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.main.repos.NutsRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.main.repos.NutsSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
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

public class DefaultNutsRepositoryManager implements NutsRepositoryManager {
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private NutsWorkspace workspace;
    public NutsLogger LOG;

    public DefaultNutsRepositoryManager(NutsWorkspace workspace) {
        this.workspace = workspace;
        LOG = workspace.log().of(DefaultNutsRepositoryManager.class);
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(workspace);
    }

    @Override
    public NutsRepositoryFilterManager filter() {
        return getWorkspace().filters().repository();
    }

    @Override
    public NutsRepositoryRef[] getRepositoryRefs(NutsSession session) {
        return repositoryRegistryHelper.getRepositoryRefs();
    }

    @Override
    public NutsRepository[] getRepositories(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsRepository findRepositoryById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirrorById(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(getWorkspace(), "ambiguous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository findRepositoryByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirrorByName(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(getWorkspace(), "ambiguous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository findRepository(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirror(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(getWorkspace(), "ambiguous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException {
        session=NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        if (DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NutsWorkspaceExt.of(getWorkspace()).getInstalledRepository();
        }
        NutsRepository r = findRepository(repositoryIdOrName, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(getWorkspace(), repositoryIdOrName);
    }

    @Override
    public NutsRepositoryManager removeRepository(String repositoryId, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, getWorkspace());
        getWorkspace().security().checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository", options.getSession());
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(getWorkspace().config());
            config.fireConfigurationChanged("config-main", options.getSession(), ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(getWorkspace()).events().fireOnRemoveRepository(new DefaultNutsWorkspaceEvent(options.getSession(), repository, "repository", repository, null));
        }
        return this;
    }

    @Override
    public void removeAllRepositories(NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, getWorkspace());
        for (NutsRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid(), options);
        }
    }

    protected void addRepository(NutsRepositoryRef ref, NutsRepository repo, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, getWorkspace());
        repositoryRegistryHelper.addRepository(ref, repo);
        if (repo != null) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(getWorkspace().config());
            config.fireConfigurationChanged("config-main", options.getSession(), ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(getWorkspace()).events().fireOnAddRepository(
                    new DefaultNutsWorkspaceEvent(options.getSession(), repo, "repository", null, repo)
            );
        }
    }


    @Override
    public NutsRepository addRepository(NutsRepositoryModel repository, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, getWorkspace());
        NutsRepositoryConfig config = new NutsRepositoryConfig();
        String name = repository.getName();
        String uuid = repository.getUuid();
        if (CoreStringUtils.isBlank(name)) {
            name = "custom";
        }
        if (CoreStringUtils.isBlank(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        config.setName(name);
        config.setType("custom");
        config.setUuid(uuid);
        config.setStoreLocationStrategy(repository.getStoreLocationStrategy());
        NutsAddRepositoryOptions options2 = new NutsAddRepositoryOptions();
        String rootFolder = NutsWorkspaceConfigManagerExt.of(getWorkspace().config()).getRepositoriesRoot();
        options2.setName(config.getName());
        options2.setConfig(config);
        options2.setDeployOrder(repository.getDeployOrder());
        options2.setSession(options.getSession());
        options2.setTemporary(true);
        options2.setLocation(CoreIOUtils.resolveRepositoryPath(options2, Paths.get(rootFolder), options.getSession()));
        NutsRepository r = new NutsSimpleRepositoryWrapper(options2, getWorkspace(), null, repository);
        addRepository(null, r, options);
        return r;
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryDefinition definition) {
        return addRepository(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryRef ref,NutsAddOptions options) {
        NutsAddRepositoryOptions o1 = CoreNutsUtils.refToOptions(ref);
        o1.setSession(options.getSession());
        NutsWorkspace ws = getWorkspace();
        NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(ws.config());
        NutsRepository r = this.createRepository(o1, Paths.get(cc.getRepositoriesRoot()), null);
        addRepository(ref, r, options);
        return r;
    }

    @Override
    public NutsRepository addRepository(NutsAddRepositoryOptions options) {
        //TODO excludedRepositoriesSet
//        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
//            return null;
//        }
        if (options.getSession() == null) {
            options.setSession(getWorkspace().createSession());
        }
        if (options.isProxy()) {
            if (options.getConfig() == null) {
                NutsRepository proxy = addRepository(
                        new NutsAddRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setLocation(options.getName())
                                .setEnabled(options.isEnabled())
                                .setCreate(options.isCreate())
                                .setDeployOrder(options.getDeployOrder())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.RepoTypes.NUTS)
                                                .setName(options.getName())
                                                .setLocation(null)
                                )
                );
                if (proxy == null) {
                    //mainly because path is not accessible
                    //or the repository is excluded
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (proxy.config().findMirror(m2, options.getSession().copy().setTransitive(false)) == null) {
                    proxy.config().addMirror(new NutsAddRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(options.getLocation())
                            .setDeployOrder(options.getDeployOrder())
                            .setCreate(options.isCreate())
                    );
                }
                return proxy;
            } else {
                NutsRepository proxy = addRepository(
                        new NutsAddRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setEnabled(options.isEnabled())
                                .setLocation(options.getLocation())
                                .setCreate(options.isCreate())
                                .setDeployOrder(options.getDeployOrder())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.RepoTypes.NUTS)
                                                .setName(options.getConfig().getName())
                                                .setLocation(null)
                                )
                );
                if (proxy == null) {
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (proxy.config().findMirror(m2, options.getSession().copy().setTransitive(false)) == null) {
                    proxy.config().addMirror(new NutsAddRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(m2)
                            .setCreate(options.isCreate())
                            .setDeployOrder(options.getDeployOrder())
                            .setConfig(
                                    new NutsRepositoryConfig()
                                            .setName(m2)
                                            .setType(CoreStringUtils.coalesce(options.getConfig().getType(), NutsConstants.RepoTypes.NUTS))
                                            .setLocation(options.getConfig().getLocation())
                            ));
                }
                return proxy;
            }
        } else {
            NutsRepositoryRef ref = options.isTemporary() ? null : CoreNutsUtils.optionsToRef(options);
            NutsRepository r = this.createRepository(options, Paths.get(NutsWorkspaceConfigManagerExt.of(getWorkspace().config()).getRepositoriesRoot()), null);
            addRepository(ref, r, new NutsAddOptions().setSession(options.getSession()));
            return r;
        }
    }

    public NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository) {
        options = options.copy();
        try {
            NutsRepositoryConfig conf = options.getConfig();
            if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, options.getSession()));
                conf = loadRepository(Paths.get(options.getLocation(), NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), getWorkspace(), options.getSession());
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NutsInvalidRepositoryException(getWorkspace(), options.getLocation(), "Invalid location " + options.getLocation());
                }
                options.setConfig(conf);
            } else {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, options.getSession()));
            }
            if (CoreStringUtils.isBlank(conf.getType())) {
                conf.setType(NutsConstants.RepoTypes.NUTS);
            }
            if (CoreStringUtils.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            NutsRepositoryFactoryComponent factory_ = getWorkspace().extensions().createSupported(NutsRepositoryFactoryComponent.class,conf, options.getSession());
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, getWorkspace(), parentRepository);
                if (r != null) {
                    return r;
                }
            }
            throw new NutsInvalidRepositoryException(getWorkspace(), options.getName(), "Invalid type " + conf.getType());
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }


    @Override
    public NutsRepository addRepository(String repositoryNamedUrl, NutsSession session) {
        return addRepository(CoreNutsUtils.defToOptions(CoreNutsUtils.repositoryStringToDefinition(repositoryNamedUrl).setSession(session)));
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
                Map<String, Object> a_config0 = ws.formats().element().setContentType(NutsContentType.JSON).parse(bytes, Map.class);
                String version = (String) a_config0.get("configVersion");
                if (version == null) {
                    version = ws.getApiVersion();
                }
                int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = ws.formats().element().setContentType(NutsContentType.JSON).parse(file, NutsRepositoryConfig.class);
            } catch (Exception ex) {
                onLoadRepositoryError(file, name, null, ex, session);
            }
        }
        return conf;
    }


    public NutsRepositorySPI toRepositorySPI(NutsRepository repo) {
        return (NutsRepositorySPI) repo;
    }

    private void onLoadRepositoryError(Path file, String name, String uuid, Throwable ex, NutsSession session) {
        NutsWorkspaceConfigManager wconfig = getWorkspace().config();
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("error loading repository " + file.toString(), new IOException(ex));
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, CoreStringUtils.exceptionToString(ex)});
        Path logError = Paths.get(getWorkspace().locations().getStoreLocation(getWorkspace().getApiId(), NutsStoreLocation.LOG))
                .resolve("invalid-config");
        try {
            Files.createDirectories(logError);
        } catch (IOException ex1) {
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
            o.println("workspace.options:" + wconfig.getOptions().format().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
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
