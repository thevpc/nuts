/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsInvalidRepositoryException;
import net.vpc.app.nuts.NutsRepoInfo;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryAlreadyRegisteredException;
import net.vpc.app.nuts.NutsRepositoryDefinition;
import net.vpc.app.nuts.NutsRepositoryFactoryComponent;
import net.vpc.app.nuts.NutsRepositoryListener;
import net.vpc.app.nuts.NutsRepositoryLocation;
import net.vpc.app.nuts.NutsRepositoryNotFoundException;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.NutsWorkspaceListener;
import net.vpc.app.nuts.NutsWorkspaceRepositoryManager;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceRepositoryManager implements NutsWorkspaceRepositoryManager {

    private Map<String, NutsRepository> repositories = new HashMap<String, NutsRepository>();
    private final DefaultNutsWorkspace ws;
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    DefaultNutsWorkspaceRepositoryManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public void removeRepository(String repositoryId) {
        if (!ws.getSecurityManager().isAllowed(NutsConstants.RIGHT_REMOVE_REPOSITORY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_REMOVE_REPOSITORY);
        }
        NutsRepository removed = repositories.remove(repositoryId);
        ws.getConfigManager().getConfig().removeRepository(repositoryId);
        if (removed != null) {
            for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
                nutsWorkspaceListener.onRemoveRepository(ws.self(), removed);
            }
        }
    }

    @Override
    public NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate) {
        NutsRepository proxy = addRepository(repositoryId, repositoryId, NutsConstants.DEFAULT_REPOSITORY_TYPE, autoCreate);
        return proxy.addMirror(repositoryId + "-ref", location, type, autoCreate);
    }

    @Override
    public NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate) {
        if (!ws.getSecurityManager().isAllowed(NutsConstants.RIGHT_ADD_REPOSITORY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_ADD_REPOSITORY);
        }
        if (CoreStringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        ws.checkSupportedRepositoryType(type);
        NutsRepositoryLocation old = ws.getConfigManager().getConfig().getRepository(repositoryId);
        if (old != null) {
            throw new NutsRepositoryAlreadyRegisteredException(repositoryId);
        }
        ws.getConfigManager().getConfig().addRepository(new NutsRepositoryLocationImpl(repositoryId, location, type));
//        NutsRepository repo = openRepository(repositoryId, new File(getRepositoriesRoot(), repositoryId), location, type, autoCreate);
        NutsRepository repo = openRepository(repositoryId, location, type, getRepositoriesRoot(), autoCreate);
        return repo;
    }

    File getRepositoriesRoot() {
        return CoreIOUtils.createFile(ws.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES);
    }

    @Override
    public NutsRepository findRepository(String repositoryIdPath) {
        if (!CoreStringUtils.isEmpty(repositoryIdPath)) {
            while (repositoryIdPath.startsWith("/")) {
                repositoryIdPath = repositoryIdPath.substring(1);
            }
            while (repositoryIdPath.endsWith("/")) {
                repositoryIdPath = repositoryIdPath.substring(0, repositoryIdPath.length() - 1);
            }
            if (repositoryIdPath.contains("/")) {
                int s = repositoryIdPath.indexOf("/");
                NutsRepository r = repositories.get(repositoryIdPath.substring(0, s));
                if (r != null) {
                    return r.getMirror(repositoryIdPath.substring(s + 1));
                }
            } else {
                NutsRepository r = repositories.get(repositoryIdPath);
                if (r != null) {
                    return r;
                }
            }
        }
        throw new NutsRepositoryNotFoundException(repositoryIdPath);
    }

    @Override
    public NutsRepository[] getRepositories() {
        return repositories.values().toArray(new NutsRepository[repositories.size()]);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (CoreStringUtils.isEmpty(repositoryType)) {
            repositoryType = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        return ws.getExtensionManager().getFactory().createAllSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(repositoryType, null)).size() > 0;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories() {
        List<NutsRepositoryDefinition> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : ws.getExtensionManager().getFactory().createAll(NutsRepositoryFactoryComponent.class)) {
            all.addAll(Arrays.asList(provider.getDefaultRepositories()));
        }
        return all.toArray(new NutsRepositoryDefinition[all.size()]);
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryId(repository.getRepositoryId());
        if (repositories.containsKey(repository.getRepositoryId())) {
            throw new NutsRepositoryAlreadyRegisteredException(repository.getRepositoryId());
        }
        repositories.put(repository.getRepositoryId(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(ws.self(), repository);
        }
    }

    void removeAllRepositories() {
        repositories.clear();
    }

    @Override
    public NutsRepository openRepository(String repositoryId, String location, String type, File repositoryRoot, boolean autoCreate) {
        if (CoreStringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        NutsRepositoryFactoryComponent factory_ = ws.getExtensionManager().getFactory().createSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(type, location));
        if (factory_ != null) {
            NutsRepository r = factory_.create(repositoryId, location, type, ws, null, repositoryRoot);
            if (r != null) {
                r.open(autoCreate);
                wireRepository(r);
                return r;
            }
        }
        throw new NutsInvalidRepositoryException(repositoryId, "Invalid type " + type);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : ws.getExtensionManager().getFactory().createAllSupported(NutsWorkspaceArchetypeComponent.class, ws)) {
            set.add(extension.getName());
        }
        return set;
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[repositoryListeners.size()]);
    }

}
