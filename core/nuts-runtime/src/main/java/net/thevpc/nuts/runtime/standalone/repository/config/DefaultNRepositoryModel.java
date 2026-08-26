package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.core.*;


import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNRepositoryDB;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryWithChildren;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.NSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultNRepositoryModel {

    private final NRepositoryRegistryHelper repositoryRegistryHelper;
    private DefaultNRepositoryDB db = new DefaultNRepositoryDB();
    private final NWorkspace workspace;

    public DefaultNRepositoryModel(NWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NRepositoryRegistryHelper();
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
                final NRepository m = session.copy().transitive(true).callWith(() -> child.config()
                        .findMirrorById(repositoryId));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryId, finalY.uuid(), m.uuid()
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
                final NRepository m = session.copy().transitive(true).callWith(() -> child.config()
                        .findMirrorByName(repositoryName));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryName, finalY.uuid(), m.uuid()
                        ));
                    }
                }
            }
        }
        return NOptional.ofNamed(y, "repository with name : " + repositoryName);
    }



    public NOptional<NRepository> getRepository(String repositoryIdOrName)  {
        if (DefaultNInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NOptional.of(NWorkspaceExt.of().getInstalledRepository());
        }
        NRepository y = repositoryRegistryHelper.findRepository(repositoryIdOrName);
        if (y != null) {
            return NOptional.of(y);
        }
        NSession session = workspace.currentSession();
        if (session.isTransitive()) {
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                final NRepository m = session.copy().transitive(true).callWith(() -> child.config()
                        .getMirror(repositoryIdOrName).orNull()
                );
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        NRepository finalY = y;
                        return NOptional.ofError(() -> NMsg.ofC("ambiguous repository name %s found two Ids %s and %s",
                                repositoryIdOrName, finalY.uuid(), m.uuid()
                        ));
                    }
                }
            }
            for (NRepository child : repositoryRegistryHelper.getRepositories()) {
                if(child instanceof NRepositoryWithChildren){
                    NRepository c = ((NRepositoryWithChildren) child).getChild(repositoryIdOrName).orNull();
                    if(c!=null){
                        return NOptional.of(c);
                    }
                }
            }
        }
        return NOptional.ofNamed(y, "repository with name or id : " + repositoryIdOrName);
    }

    public void removeRepository(String repositoryId) {
        NSession session = NSession.of();
        NSecurityManager.of().checkAllowed(NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            NWorkspace.of().saveConfig();
            NWorkspaceExt.of(workspace).getConfigModel().fireConfigurationChanged("config-main", ConfigEventType.MAIN);
            NWorkspaceUtils.of().events().fireOnRemoveRepository(new DefaultNWorkspaceEvent(session, repository, "repository", repository, null));
            updateBootRepositories();
        }
    }

    public void removeAllRepositories() {
        for (NRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.uuid());
        }
    }

    private void updateBootRepositories() {
        NWorkspaceExt.of().getConfigModel().setBootRepositories(
                Arrays.stream(getRepositories())
                        .filter(x -> x.isEnabled() && ! x.isTemporary())
                        .map(x -> x.bootConnectionString())
                        .filter(x -> NBlankable.isNonBlank(x))
                        .collect(Collectors.toList())
        );
    }

    protected void addRepository(NRepository repo, boolean temp, boolean enabled) {
        repositoryRegistryHelper.addRepository(repo);
        repo.config().enabled(enabled);
        if (!temp) {
            NSession session = NSession.of();
            NWorkspaceExt.of().getConfigModel().fireConfigurationChanged("config-main", ConfigEventType.MAIN);
            // repo would be null if the repo is not accessible
            // like for system repo, if not already created
            NWorkspaceUtils.of().events().fireOnAddRepository(
                    new DefaultNWorkspaceEvent(session, repo, "repository", null, repo)
            );
            updateBootRepositories();
        }
    }

    public NRepository addRepository(NRepositorySpec options) {
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

    public NRepository createRepository(NRepositorySpec options, NRepository parentRepository) {
        return createRepository(options, null, parentRepository);
    }

    public NRepository createRepository(NRepositorySpec options, Path rootFolder, NRepository parentRepository, NRepositoryModel repoModel) {
        String name = repoModel.name();
        String uuid = repoModel.uuid();
        if (NBlankable.isBlank(name)) {
            name = "custom";
        }
        if (NBlankable.isBlank(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        NRepositorySpec options2 = new NRepositorySpec();
        options2.name(NStringUtils.firstNonBlankStripped(options.name(),name));
        options2.sourceLocation(NRepositoryLocation.of("custom@"));
        options2.uuid(uuid);
        options2.storeStrategy(repoModel.storeStrategy());
        options2.deployWeight(options.deployWeight());
        options2.temporary(true);
        options2.enabled(options.isEnabled());
        options2.location(CoreIOUtils.resolveRepositoryPath(options2, rootFolder));
        return new NSimpleRepositoryWrapper(options2, parentRepository, repoModel);
    }

    public NRepository createRepository(NRepositorySpec options, Path rootFolder, NRepository parentRepository) {
        NRepositoryModel repoModel = options.sourceModel();
        if (rootFolder == null) {
            if (parentRepository == null) {
                rootFolder = options.isTemporary() ?
                        NWorkspaceExt.of(workspace).getConfigModel().getTempRepositoriesRoot().toPath().get()
                        : NWorkspaceExt.of(workspace).getConfigModel().getRepositoriesRoot().toPath().get();
            } else {
                NRepositoryConfigManagerExt cc = NRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = (options.isTemporary() ? cc.getModel().getTempMirrorsRoot()
                        : cc.getModel().getMirrorsRoot()).toPath().get();
            }
        }
        if (repoModel != null) {
            return createRepository(options, rootFolder,parentRepository,repoModel);
        }

        options = options.copy();
        try {
            boolean temporary = options.isTemporary();
            if (temporary) {
//                options.setLocation(options.getName());
                options.location(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
                options.enabled(true);
            } else if (options.sourceLocation() == null) {
                options.location(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
                NRepositoryConfig conf2 = loadRepository(options);
                if (conf2 == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NInvalidRepositoryException(options.location(),
                            NMsg.ofC("invalid repository location ", options.location())
                    );
                }
                options.mergeConfig(conf2);
                if (options.isEnabled()) {
                    NRepositorySpec cp = options.copy();
                    cp.sourceLocation(options.sourceLocation().copy().name(options.name()));
                    options.enabled(
                            NWorkspace.of().bootOptions().repositories() == null
                                    || NRepositoryUtils.createRepositorySelectorList(
                                    NWorkspace.of().bootOptions().repositories().orNull()
                            ).get().acceptExisting(cp));
                }
            } else {
                if (options.isEnabled()) {
                    NRepositorySpec cp = options.copy();
                    cp.sourceLocation(options.sourceLocation().copy().name(options.name()));
                    options.enabled(
                            NWorkspace.of().bootOptions().repositories() == null
                                    || NRepositoryUtils.createRepositorySelectorList(
                                    NWorkspace.of().bootOptions().repositories().orNull()
                            ).get().acceptExisting(cp));
                }
                options.location(CoreIOUtils.resolveRepositoryPath(options, rootFolder));
            }
            if (NBlankable.isBlank(options.sourceLocation())
                    && !NBlankable.isBlank(options.location())
                //&& NPath.of(options.getLocation(), session).isFile()
            ) {
                options.sourceLocation(NRepositoryLocation.of(options.location()));
            }
            NRepositorySpec finalOptions = options;
            NRepositoryConfig config = options.toConfig();
            NRepositoryFactoryContext context = new NRepositoryFactoryContext() {
                @Override
                public NRepositorySpec spec() {
                    return finalOptions;
                }
                @Override
                public NRepositoryConfig config() {
                    return config;
                }

                @Override
                public String repositoryType() {
                    return NRepositoryUtils.getRepoType(spec());
                }

                @Override
                public NRepository parentRepository() {
                    return parentRepository;
                }

                @Override
                public NRepository createDefaultRepository(NRepositoryModel model) {
                    return new NSimpleRepositoryWrapper(spec(), parentRepository(), model);
                }
            };
            NRepositoryFactoryComponent factory_ = NExtensions.of()
                    .createSupported(NRepositoryFactoryComponent.class, context).orNull();
            if (factory_ != null) {

                NRepository r = factory_.createRepository(context);
                if (r != null) {
                    return r;
                }
            }
            String repoType = NRepositoryUtils.getRepoType(options);
            if (options.isTemporary()) {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(options.name(), NMsg.ofC("unable to detect valid type for temporary repository %s", options.sourceLocation()));
                } else {
                    throw new NInvalidRepositoryException(options.name(), NMsg.ofC("invalid repository type %s", repoType));
                }
            } else {
                if (NBlankable.isBlank(repoType)) {
                    throw new NInvalidRepositoryException(options.name(), NMsg.ofC("unable to detect valid type for repository %s", options.name()));
                } else {
                    throw new NInvalidRepositoryException(options.name(), NMsg.ofC("invalid repository type %s", repoType));
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
        NRepositoryLocation r = NRepositoryUtils.createRepositoryLocation(repositoryNamedUrl).get();
        NRepositorySpec options = NRepositorySelectorHelper.createRepositoryOptions(r, true);
        return addRepository(options);
    }

    public NRepositoryConfig loadRepository(NRepositorySpec options) {
        return ((NWorkspaceExt) workspace).store().loadRepoConfig(options.location(), options.name());
    }

    public NRepositorySPI toRepositorySPI(NRepository repo) {
        return (NRepositorySPI) repo;
    }


    public DefaultNRepositoryDB getDB() {
        return db;
    }
}
