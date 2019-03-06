/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;

/**
 * @author vpc
 */
class DefaultNutsWorkspaceRepositoryManager implements NutsWorkspaceRepositoryManager {

    private Map<String, NutsRepository> repositories = new LinkedHashMap<>();
    private final DefaultNutsWorkspace ws;
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    private NutsIndexStoreClientFactory indexStoreClientFactory;

    DefaultNutsWorkspaceRepositoryManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        try {
            indexStoreClientFactory = ws.getExtensionManager().createSupported(NutsIndexStoreClientFactory.class, ws);
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreClientFactory();
        }
    }

    @Override
    public NutsIndexStoreClientFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    @Override
    public void removeRepository(String repositoryId) {
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_REMOVE_REPOSITORY, "remove-repository");
        NutsRepository removed = repositories.remove(repositoryId);
        ws.getConfigManager().removeRepository(repositoryId);
        if (removed != null) {
            for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
                nutsWorkspaceListener.onRemoveRepository(ws, removed);
            }
        }
    }

    @Override
    public NutsRepository addProxiedRepository(NutsRepositoryLocation config, boolean autoCreate) {
        NutsRepository proxy = addRepository(
                new NutsRepositoryLocation()
                        .setType(NutsConstants.REPOSITORY_TYPE_NUTS)
                        .setName(config.getName())
                        .setLocation(null)
                , autoCreate);
        //Dont need to add mirror if repository is already loadable from config!
        if (!proxy.containsMirror(config.getName() + "-ref")) {
            return proxy.addMirror(new NutsRepositoryLocation()
                            .setName(config.getName() + "-ref")
                            .setLocation(config.getLocation())
                            .setType(config.getType())
                    , autoCreate);
        }
        return proxy;
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryLocation config, boolean autoCreate) {
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_ADD_REPOSITORY, "add-repository");
        config = config.copy();
        if (StringUtils.isEmpty(config.getName())) {
            if (StringUtils.isEmpty(config.getLocation())) {
                throw new IllegalArgumentException("You should consider specifying location and/or repositoryId");
            }
            File file = new File(this.resolveRepositoryPath(config.getLocation()));
            if (file.isDirectory()) {
                if (new File(file, NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME).exists()) {
                    NutsRepositoryConfig c = ws.getIOManager().readJson(new File(file, NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME), NutsRepositoryConfig.class);
                    if (c != null) {
                        config.setName(c.getName());
                        if (StringUtils.isEmpty(config.getType())) {
                            config.setType(c.getType());
                        } else if (!config.getType().equals(c.getType())) {
                            throw new IllegalArgumentException("Invalid repository type " + config.getType() + ". expected " + c.getType());
                        }
                    }
                } else {
                    config.setName(file.getName());
                }
            }
        } else if (StringUtils.isEmpty(config.getLocation())) {
            //no pbm!
        }

        if (StringUtils.isEmpty(config.getType())) {
            config.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        ws.checkSupportedRepositoryType(config.getType());
        NutsRepositoryLocation old = ws.getConfigManager().getRepository(config.getName());
        if (old != null) {
            throw new NutsRepositoryAlreadyRegisteredException(config.getName());
        }
        ws.getConfigManager().addRepository(config);
//        NutsRepository repo = openRepository(repositoryId, new File(getRepositoriesRoot(), repositoryId), location, type, autoCreate);
        return openRepository(config, getRepositoriesRoot(), autoCreate);
    }

    String getRepositoriesRoot() {
        return CoreIOUtils.createFile(ws.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath();
    }

    @Override
    public NutsRepository findRepository(String repositoryIdPath) {
        if (!StringUtils.isEmpty(repositoryIdPath)) {
            repositoryIdPath = CoreNutsUtils.trimSlashes(repositoryIdPath);
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
        return repositories.values().toArray(new NutsRepository[0]);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (StringUtils.isEmpty(repositoryType)) {
            repositoryType = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        return ws.getExtensionManager().createAllSupported(NutsRepositoryFactoryComponent.class, new NutsRepositoryLocation().setType(repositoryType)).size() > 0;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories() {
        List<NutsRepositoryDefinition> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : ws.getExtensionManager().createAll(NutsRepositoryFactoryComponent.class)) {
            all.addAll(Arrays.asList(provider.getDefaultRepositories(ws)));
        }
        Collections.sort(all, new Comparator<NutsRepositoryDefinition>() {
            @Override
            public int compare(NutsRepositoryDefinition o1, NutsRepositoryDefinition o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return all.toArray(new NutsRepositoryDefinition[0]);
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryName(repository.getName(), repositories.keySet());
        repositories.put(repository.getName(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(ws, repository);
        }
    }

    void removeAllRepositories() {
        repositories.clear();
    }

    @Override
    public NutsRepository openRepository(NutsRepositoryLocation location, String repositoriesRoot, boolean autoCreate) {
//        String repositoryName = location.getName();
//        String type = location.getType();
//        String locationPath = location.getLocation();
        if (StringUtils.isEmpty(location.getType())) {
            location.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        NutsRepositoryFactoryComponent factory_ = ws.getExtensionManager().createSupported(NutsRepositoryFactoryComponent.class, location);
        if (factory_ != null) {
            String root = ws.getIOManager().expandPath(location.getName(),
                    repositoriesRoot != null ? repositoriesRoot : CoreIOUtils.createFile(ws.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath()
            );

            NutsRepository r = factory_.create(location, ws, null, root);
            if (r != null) {
                r.open(autoCreate);
                wireRepository(r);
                return r;
            }
        }
        throw new NutsInvalidRepositoryException(location.getName(), "Invalid type " + location.getType());
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : ws.getExtensionManager().createAllSupported(NutsWorkspaceArchetypeComponent.class, ws)) {
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
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public String resolveRepositoryPath(String repositoryLocation) {
        String root = this.getRepositoriesRoot();
        NutsWorkspaceConfigManager configManager = this.ws.getConfigManager();
        return ws.getIOManager().expandPath(repositoryLocation,
                root != null ? root : CoreIOUtils.createFile(
                        configManager.getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath());
    }

    private static class DummyNutsIndexStoreClient implements NutsIndexStoreClient {
        @Override
        public List<NutsId> findVersions(NutsId id, NutsSession session) {
            return null;
        }

        @Override
        public Iterator<NutsId> find(NutsIdFilter filter, NutsSession session) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
        }

        @Override
        public void invalidate(NutsId id) {

        }

        @Override
        public void revalidate(NutsId id) {

        }

        @Override
        public boolean subscribe() {
            return false;
        }

        @Override
        public void unsubscribe() {

        }

        @Override
        public boolean isSubscribed(NutsRepository repository) {
            return false;
        }
    }

    private static class DummyNutsIndexStoreClientFactory implements NutsIndexStoreClientFactory {
        @Override
        public int getSupportLevel(NutsWorkspace criteria) {
            return 0;
        }

        @Override
        public NutsIndexStoreClient createNutsIndexStoreClient(NutsRepository repository) {
            return new DummyNutsIndexStoreClient();
        }
    }
}
