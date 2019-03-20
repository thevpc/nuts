/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.io.File;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.strings.StringUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceRepositoryManager implements NutsWorkspaceRepositoryManagerExt {

    private static final Logger log = Logger.getLogger(DefaultNutsWorkspaceRepositoryManager.class.getName());
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

    public String getRepositoriesRoot() {
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

    @Override
    public NutsRepository wireRepository(NutsRepository repository) {
        if(repository==null){
            //mainly if the lcoation is inaccessible!
            return null;
        }
        CoreNutsUtils.validateRepositoryName(repository.getName(), repositories.keySet());
        repositories.put(repository.getName(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(ws, repository);
        }
        return repository;
    }

    @Override
    public void removeAllRepositories() {
        repositories.clear();
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryDefinition definition) {
        return addRepository(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addRepository(NutsCreateRepositoryOptions options) {
        if (options.isProxy()) {
            if (options.getConfig() == null) {
                NutsRepository proxy = addRepository(
                        new NutsCreateRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setLocation(options.getName())
                                .setEnabled(options.isEnabled())
                                .setCreate(options.isCreate())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.REPOSITORY_TYPE_NUTS)
                                                .setName(options.getName())
                                                .setLocation(null)
                                )
                );
                if(proxy==null){
                    //mainly becausse path is not accessible
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (!proxy.containsMirror(m2)) {
                    proxy.addMirror(new NutsCreateRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(options.getLocation())
                            .setCreate(options.isCreate())
                    );
                }
                return proxy;
            } else {
                NutsRepository proxy = addRepository(
                        new NutsCreateRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setEnabled(options.isEnabled())
                                .setLocation(options.getLocation())
                                .setCreate(options.isCreate())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.REPOSITORY_TYPE_NUTS)
                                                .setName(options.getConfig().getName())
                                                .setLocation(null)
                                )
                );
                if(proxy==null){
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (!proxy.containsMirror(m2)) {
                    proxy.addMirror(new NutsCreateRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(m2)
                            .setCreate(options.isCreate())
                            .setConfig(
                                    new NutsRepositoryConfig()
                                            .setName(m2)
                                            .setType(options.getConfig().getType())
                                            .setLocation(options.getConfig().getLocation())
                            ));
                }
                return proxy;
            }
        } else {
            if (!options.isTemporay()) {
                ws.getConfigManager().addRepository(CoreNutsUtils.optionsToRef(options));
            }
            return wireRepository(this.createRepository(options, getRepositoriesRoot(), null));
        }
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

    @Override
    public NutsRepository createRepository(NutsCreateRepositoryOptions options, String rootFolder, NutsRepository parentRepository) {
//        conf = CoreNutsUtils.loadNutsRepositoryConfig(new File(folder, NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME), ws);
        options = options.copy();
        try {
            NutsRepositoryConfig conf = options.getConfig();
            if (conf == null) {
                options.setLocation(CoreNutsUtils.resolveRepositoryPath(options, rootFolder, ws));
                conf = CoreNutsUtils.loadNutsRepositoryConfig(new File(options.getLocation(), NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME), ws);
                if (conf == null) {
                    throw new NutsInvalidRepositoryException(options.getLocation(), "Invalid location " + options.getLocation());
                }
                options.setConfig(conf);
            } else {
                options.setLocation(CoreNutsUtils.resolveRepositoryPath(options, rootFolder, ws));
            }
            NutsRepositoryFactoryComponent factory_ = ws.getExtensionManager().createSupported(NutsRepositoryFactoryComponent.class, conf);
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, ws, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            throw new NutsInvalidRepositoryException(options.getName(), "Invalid type " + conf.getType());
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }
}
