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
package net.vpc.app.nuts.boot;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.repos.MavenFolderRepository;
import net.vpc.app.nuts.boot.repos.MavenRemoteRepository;
import net.vpc.app.nuts.util.*;

import javax.security.auth.callback.CallbackHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vpc on 1/6/17.
 */
public class BootNutsWorkspace implements NutsWorkspace {
    public static final Logger log = Logger.getLogger(BootNutsWorkspace.class.getName());
    private static NutsWorkspaceFactory emptyFactory = new NutsWorkspaceFactory() {
        @Override
        public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
            return null;
        }

        @Override
        public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
            return null;
        }

        @Override
        public <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public <T> List<T> createAll(Class<T> type) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Set<Class> getExtensionPoints() {
            return Collections.emptySet();
        }

        @Override
        public Set<Class> getExtensionTypes(Class extensionPoint) {
            return Collections.emptySet();
        }

        @Override
        public List<Object> getExtensionObjects(Class extensionPoint) {
            return Collections.emptyList();
        }
    };
    private List<NutsFile> nutsComponentIdDependencies;
    private NutsFile nutsComponentId;
    private String workspaceRoot;
    private ObservableMap<String, Object> sharedObjects = new ObservableMap<>();
    private NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private Map<String, NutsRepository> repositories = new HashMap<String, NutsRepository>();
    private List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    //    private Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<Class>(
//            Arrays.asList(
//                    //order is important!!because autowiring should follow this very order
//                    NutsPrintStream.class,
//                    NutsTerminal.class,
//                    NutsCommand.class,
//                    NutsCommandLineConsoleComponent.class,
//                    NutsDescriptorContentParserComponent.class,
//                    NutsExecutorComponent.class,
//                    NutsInstallerComponent.class,
//                    NutsRepositoryFactoryComponent.class,
//                    NutsServerComponent.class,
//                    NutsTransportComponent.class,
//                    NutsWorkspace.class,
//                    NutsWorkspaceArchetypeComponent.class,
//                    NutsCommandAutoCompleteComponent.class
//            )
//    );
//    private DefaultNutsWorkspaceFactory factory = new DefaultNutsWorkspaceFactory();
    private String workspace;

    public BootNutsWorkspace() {

    }

    private static List<NutsRepository> filterRepositories(List<NutsRepository> repos, NutsId id, NutsSession session) {
        return filterRepositories(repos, id, session, false, null);
    }

    private static List<NutsRepository> filterRepositories(List<NutsRepository> repos, NutsId id, NutsSession session, boolean sortByLevelDesc, Comparator<NutsRepository> postComp) {
        class RepoAndLevel {

            NutsRepository r;
            int level;

            public RepoAndLevel(NutsRepository r, int level) {
                this.r = r;
                this.level = level;
            }
        }
        List<RepoAndLevel> repos2 = new ArrayList<>();
//        List<Integer> reposLevels = new ArrayList<>();
        for (NutsRepository repository : repos) {
            int t = 0;
            try {
                t = repository.getSupportLevel(id, session);
            } catch (Exception e) {
                //ignore...
            }
            if (t > 0) {
                repos2.add(new RepoAndLevel(repository, t));
//                    reposLevels.add(t);
            }
        }
        if (sortByLevelDesc) {
            Collections.sort(repos2, (o1, o2) -> {
                int x = Integer.compare(o2.level, o1.level);
                if (x != 0) {
                    return x;
                }
                if (postComp != null) {
                    return postComp.compare(o1.r, o2.r);
                }
                return 0;
            });
        }
        return repos2.stream().map(x -> x.r).collect(Collectors.toList());
    }

    public static NutsId configureFetchEnv(NutsId id) {

        Map<String, String> face = id.getQueryMap();
        if (face.get(NutsConstants.QUERY_FACE) == null && face.get("arch") == null && face.get("os") == null && face.get("osdist") == null && face.get("platform") == null) {
            //face.put("arch", PlatformUtils.getArch());
            //face.put("os", PlatformUtils.getOs());
            //face.put("osdist", PlatformUtils.getOsdist());
            return id.setQuery(face);
        }
        return id;
    }



    @Override
    public String getCurrentLogin() {
        return NutsConstants.USER_ANONYMOUS;
    }

    public String login(CallbackHandler handler) {
        return getCurrentLogin();
    }

    @Override
    public String getWorkspaceVersion() {
        return Main.getBootVersion();
    }

    public void addSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.addListener(listener);
    }

    public void removeSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.removeListener(listener);
    }

    public MapListener<String, Object>[] getSharedObjectsListeners() {
        return sharedObjects.getListeners();
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[workspaceListeners.size()]);
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

    private Object[] privateCreateWorkspaceInstance(Collection<NutsFile> list, NutsSession session) {
        List<URL> urls = list.stream()
                .filter(x ->
                        !isLoadedClassPath(x)
                ).map(x -> {
                    try {
                        return (x == null || x.getFile() == null) ? null : x.getFile().toURI().toURL();
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

        ClassLoader workspaceClassLoader = urls.isEmpty() ? null : new URLClassLoader(urls.toArray(new URL[urls.size()]));
        ServiceLoader<NutsComponent> serviceLoader = workspaceClassLoader == null ? ServiceLoader.load(NutsComponent.class) : ServiceLoader.load(NutsComponent.class, workspaceClassLoader);

        int bestSupportLevel = Integer.MIN_VALUE;
        NutsWorkspace nutsWorkspace = null;
        for (NutsComponent extensionImpl : serviceLoader) {
            if (extensionImpl instanceof NutsWorkspace) {
                int supportLevel = extensionImpl.getSupportLevel(this);
                if (supportLevel > 0) {
                    if (nutsWorkspace == null || supportLevel > bestSupportLevel) {
                        bestSupportLevel = supportLevel;
                        nutsWorkspace = (NutsWorkspace) extensionImpl;
                    }
                }
            }
        }
        if (nutsWorkspace == null) {
            //should never happen
            return null;
        }
        return new Object[]{
                nutsWorkspace,
                workspaceClassLoader
        };

    }

    @Override
    public NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException {
        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        workspace = resolveWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME);
        File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
        NutsWorkspaceConfig config = JsonUtils.loadJson(file, NutsWorkspaceConfig.class);
        if (config == null) {
            config = new NutsWorkspaceConfig();
        }
        Set<String> excludedExtensions = null;
        LinkedHashMap<String, NutsFile> allExtensionFiles = new LinkedHashMap<>();
        NutsSession sessionCopy = session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE);

        if (requiresCoreExtension(config)) {
            privateFillExtensionFiles(allExtensionFiles, sessionCopy, NutsConstants.NUTS_COMPONENT_CORE_ID);
        }
        for (String extensionIdString : config.getExtensions()) {
            if (excludedExtensions != null && NutsUtils.finNutsIdByFullNameInStrings(NutsId.parseOrError(extensionIdString), excludedExtensions) != null) {
                continue;
            }
            privateFillExtensionFiles(allExtensionFiles, sessionCopy, extensionIdString);
        }
        Object[] a = privateCreateWorkspaceInstance(allExtensionFiles.values(), sessionCopy);
        if (a == null) {
            throw new NutsWorkspaceInvalidException(workspace);
        }
        NutsWorkspace nutsWorkspace = (NutsWorkspace) a[0];
        ClassLoader workspaceClassLoader = (ClassLoader) a[1];
        nutsWorkspace.initializeWorkspace(workspaceRoot, workspace, this, workspaceClassLoader, options.copy().setIgnoreIfFound(true), session);
        return nutsWorkspace;
    }

    public boolean requiresCoreExtension(NutsWorkspaceConfig config) {
        boolean exclude = false;
        if (config.getExtensions().length > 0) {
            exclude = Boolean.parseBoolean(config.getEnv(NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION, "false"));
        }
        if (!exclude) {
            boolean coreFound = false;
            for (String ext : config.getExtensions()) {
                if (NutsId.parseOrError(ext).isSameFullName(NutsId.parse(NutsConstants.NUTS_COMPONENT_CORE_ID))) {
                    coreFound = true;
                    break;
                }
            }
            return !coreFound;
        }
        return false;
    }

    private void privateFillExtensionFiles(LinkedHashMap<String, NutsFile> allExtensionFiles, NutsSession sessionCopy, String extensionIdString) throws IOException {
        NutsId extensionId = NutsId.parseOrError(extensionIdString);
        log.log(Level.FINE, "Fetching extension {0}", extensionId);
        List<NutsFile> nutsFiles = fetchWithDependencies(extensionId, true, NutsUtils.EXEC_DEPENDENCIES_FILTER, sessionCopy);
        for (NutsFile nutsFile : nutsFiles) {
            String fullName = nutsFile.getId().getFullName();
            if (allExtensionFiles.containsKey(fullName)) {
                NutsFile other = allExtensionFiles.get(fullName);
                if (other.getId().getVersion().compareTo(nutsFile.getId().getVersion()) < 0) {
                    allExtensionFiles.put(fullName, nutsFile);
                }
            } else {
                allExtensionFiles.put(fullName, nutsFile);
            }
        }
    }

    /**
     * @param workspace            workspace location path
     * @param workspaceClassLoader
     * @param options              creation options
     * @param session              session   @return return true if created
     * @throws IOException
     */
    @Override
    public boolean initializeWorkspace(String workspaceRoot, String workspace, NutsWorkspace bootstrapWorkspace, ClassLoader workspaceClassLoader, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException {
        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }

        this.workspaceRoot = StringUtils.isEmpty(workspaceRoot) ? NutsConstants.DEFAULT_WORKSPACE_ROOT : workspaceRoot;
        //now will iterate over Extension classes to wire them ...
        if (session.getTerminal() == null) {
            DefaultNutsTerminal terminal = new DefaultNutsTerminal();
            terminal.install(this, null, null, null);
            session.setTerminal(terminal);
        }

        this.workspace = resolveWorkspacePath(workspace, NutsConstants.BOOTSTRAP_WORKSPACE_NAME);
        config = new NutsWorkspaceConfig();
        Set<String> excludedRepositories = options.getExcludedRepositories();
        if (excludedRepositories == null) {
            excludedRepositories = Collections.EMPTY_SET;
        }
        NutsRepository defaultRepo = this.addRepository(NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_NAME, NutsConstants.DEFAULT_REPOSITORY_TYPE, true);
        defaultRepo.getConfig().setEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "10");
        if (!excludedRepositories.contains("maven-local")) {
            this.addRepository("maven-local", System.getProperty("maven-local", "~/.m2/repository"), "maven", true);
        }
        if (!excludedRepositories.contains("maven-central")) {
            this.addProxiedRepository("maven-central", "http://repo.maven.apache.org/maven2/", "maven", true);
        }
        if (!excludedRepositories.contains("maven-vpc-public")) {
            this.addProxiedRepository("maven-vpc-public", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "maven", true);
        }

        return false;
    }

    public NutsFile[] fetchNutsIdWithDependencies(NutsSession session) {
        if (nutsComponentIdDependencies == null) {
            try {
                nutsComponentIdDependencies = fetchWithDependencies(NutsId.parseOrError(NutsConstants.NUTS_COMPONENT_ID), true, NutsUtils.EXEC_DEPENDENCIES_FILTER, session);
            } catch (IOException e) {
                nutsComponentIdDependencies = new ArrayList<>();
                log.log(Level.SEVERE, "Unable to load dependencies for " + NutsConstants.NUTS_COMPONENT_ID);
            }
        }
        return nutsComponentIdDependencies.toArray(new NutsFile[nutsComponentIdDependencies.size()]);
    }

    public NutsFile fetchNutsId(NutsSession session) throws IOException {
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsId.parseOrError(NutsConstants.NUTS_COMPONENT_ID), session, false);
        }
        return nutsComponentId;
    }

    public boolean reloadWorkspace(boolean save, NutsSession session, Set<String> excludedExtensions, Set<String> excludedRepositories) throws IOException {
        File file = IOUtils.createFile(this.workspace, NutsConstants.NUTS_WORKSPACE_FILE);
        NutsWorkspaceConfig config = JsonUtils.loadJson(file, NutsWorkspaceConfig.class);
        if (config != null) {
            repositories.clear();
            this.config = config;
            for (NutsWorkspaceConfig.NutsRepositoryLocation repositoryConfig : config.getRepositories()) {
                if (excludedRepositories != null && excludedRepositories.contains(repositoryConfig.getId())) {
                    continue;
                }
                wireRepository(openRepository(repositoryConfig.getId(), IOUtils.createFile(getRepositoriesRoot(), repositoryConfig.getId()), repositoryConfig.getLocation(), repositoryConfig.getType(), true));
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(this);
            }
            return true;
        }
        return false;
    }

    private File getRepositoriesRoot() {
        return IOUtils.createFile(workspace, NutsConstants.DEFAULT_REPOSITORIES_ROOT);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        return set;
    }

    @Override
    public NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException {
        NutsRepository proxy = addRepository(repositoryId, repositoryId, NutsConstants.DEFAULT_REPOSITORY_TYPE, autoCreate);
        return proxy.addMirror(repositoryId + "-ref", location, type, autoCreate);
    }

    @Override
    public NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException {
        if (StringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        checkSupportedRepositoryType(type);
        NutsWorkspaceConfig.NutsRepositoryLocation old = getConfig().getRepository(repositoryId);
        if (old != null) {
            throw new NutsRepositoryAlreadyRegisteredException(repositoryId);
        }
        getConfig().addRepository(new NutsWorkspaceConfig.NutsRepositoryLocation(repositoryId, location, type));
        NutsRepository repo = openRepository(repositoryId, new File(getRepositoriesRoot(), repositoryId), location, type, autoCreate);
        wireRepository(repo);
        return repo;
    }

    @Override
    public NutsRepository findRepository(String repositoryIdPath) {
        if (!StringUtils.isEmpty(repositoryIdPath)) {
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
        throw new RepositoryNotFoundException(repositoryIdPath);
    }

    @Override
    public void removeRepository(String repositoryId) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    @Override
    public NutsRepository openRepository(String repositoryId, File repositoryRoot, String location, String type, boolean autoCreate) throws IOException {
        if (StringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        NutsRepository r = null;
        if (NutsConstants.DEFAULT_REPOSITORY_TYPE.equals(type)) {
            if (!location.contains("://")) {
                return new NutsFolderRepository(repositoryId, location, this, repositoryRoot);
            }
        } else if ("maven".equals(type)) {
            NutsWorkspace workspace = NutsEnvironmentContext.WORKSPACE.get();
            if (location.startsWith("http://") || location.startsWith("https://")) {
                r = (new MavenRemoteRepository(repositoryId, location, this, repositoryRoot));
            } else if (!location.contains("://")) {
                r = new MavenFolderRepository(repositoryId, location, this, repositoryRoot);
            }
        }
        if (r != null) {
            r.open(autoCreate);
            return r;
        }
        throw new NutsRepositoryUnsupportedException(type);
    }

    @Override
    public NutsRepository[] getRepositories() {
        return repositories.values().toArray(new NutsRepository[repositories.size()]);
    }

    protected void wireRepository(NutsRepository repository) {
        NutsUtils.validateRepositoryId(repository.getRepositoryId());
        if (repositories.containsKey(repository.getRepositoryId())) {
            throw new NutsRepositoryAlreadyRegisteredException(repository.getRepositoryId());
        }
        repositories.put(repository.getRepositoryId(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(this, repository);
        }
    }

    @Override
    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        try {
            fetch(id, checkDependencies, session.copy().setFetchMode(FetchMode.OFFLINE).setTransitive(false));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected NutsInstallerComponent getInstaller(NutsFile nutToInstall, NutsSession session) {
        return null;
    }

    public boolean uninstall(String id, NutsSession session) {
        return true;
    }

    public NutsFile fetch(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        try {
            NutsFile fetched = fetch(id, true, session);
            if (fetched.isCached()) {
                return fetched;
            }
        } catch (NutsNotFoundException ex) {
            //not found will try to install it
            if (!session.isTransitive()) {
                throw ex;
            }
        }

        return fetch(id, true, session);
    }

    public NutsWorkspaceConfig getConfig() {
        return config;
    }

    public boolean isFetched(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        return isFetched(NutsId.parseOrError(id), session);
    }

    public boolean isFetched(NutsId id, NutsSession session) {
        NutsSession offlineSession = session.copy().setFetchMode(FetchMode.OFFLINE);
        try {
            NutsFile found = fetch(id, offlineSession, false);
            return found != null;
        } catch (Exception e) {
            return false;
        }
    }

    public NutsFile fetch(String id, boolean dependencies, NutsSession session) throws IOException {
        return fetch(NutsId.parseOrError(id), session, dependencies);
    }

    public List<NutsFile> fetchWithDependencies(String id, boolean includeMain, NutsDependencyFilter dependencyFilter, NutsSession session) throws IOException {
        return fetchWithDependencies(NutsId.parseOrError(id), includeMain, dependencyFilter, session);
    }

    public List<NutsFile> fetchWithDependencies(NutsId id, boolean includeMain, NutsDependencyFilter dependencyFilter, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        List<NutsFile> all = new ArrayList<>();
        NutsFile main = fetch(id, session, false);
        HashSet<String> visited = new HashSet<>();
        Stack<NutsFile> stack = new Stack<>();
        stack.push(main);
        while (!stack.isEmpty()) {
            NutsFile curr = stack.pop();
            if (!visited.contains(curr.getId().toString())) {
                visited.add(curr.getId().toString());
                if (curr.getId().toString().equals(id.toString())) {
                    if (includeMain) {
                        all.add(curr);
                    }
                } else {
                    all.add(curr);
                }
                if (curr.getDescriptor() != null) {
                    for (NutsDependency dept : fetchEffectiveDescriptor(curr.getDescriptor(), session).getDependencies()) {
                        if (dependencyFilter == null || dependencyFilter.accept(dept)) {
                            NutsId item = dept.toId();
                            if (!visited.contains(item.toString())) {
                                try {
                                    NutsFile itemFile = fetch(item, session, false);
                                    if (!visited.contains(itemFile.getId().toString())) {
                                        stack.push(itemFile);
                                    }
                                } catch (NutsResolutionPendingException ex) {
                                    //ignore...
                                } catch (NutsNotFoundException ex) {
                                    if (!visited.contains(item.toString())) {
                                        stack.push(new NutsFile(item, null, null, false, false));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return all;
    }

    @Override
    public NutsId resolveId(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId nutsId = NutsId.parseOrError(id);
        return resolveId(nutsId, session);
    }

    private FetchMode[] resolveFetchModes(FetchMode fetchMode) {
        return fetchMode == FetchMode.ONLINE ? new FetchMode[]{FetchMode.OFFLINE, FetchMode.REMOTE} : new FetchMode[]{fetchMode};
    }

    public NutsId resolveId(NutsId id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }

        //add env parameters to fetch adequate nuts
        id = configureFetchEnv(id);

        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    throw new NutsNotFoundException(id);
                }

                for (NutsRepository repo : getEnabledRepositories(id, session)) {
                    try {
                        NutsId child = repo.resolveId(id, session);
                        if (child != null) {
                            if (StringUtils.isEmpty(child.getNamespace())) {
                                child = child.setNamespace(repo.getRepositoryId());
                            }
                            return child;
                        }
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(id);
    }

    public List<NutsId> find(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) throws IOException {
        ArrayList<NutsId> all = new ArrayList<>();
        Iterator<NutsId> it = findIterator(repositoryFilter, filter, session);
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public Iterator<NutsId> findIterator(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (filter instanceof NutsIdPatternFilter) {
            String[] ids = ((NutsIdPatternFilter) filter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = NutsId.parse(id);
                    if (nid != null) {

                        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
                            NutsSession session2 = session.copy().setFetchMode(mode);
                            IteratorList<NutsId> all = new IteratorList<NutsId>();
                            for (NutsRepository repo : getEnabledRepositories()) {
                                try {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        Iterator<NutsId> child = repo.findVersions(nid, filter, session2);
                                        all.add(child);
                                    }
                                } catch (Exception exc) {
                                    //
                                }
                            }
                            PushBackIterator<NutsId> b = new PushBackIterator<>(all);
                            if (b.hasNext()) {
                                b.pushBack();
                                return b;
                            }
                        }
                        return Collections.emptyIterator();
                    }
                }
            }
        }


        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            IteratorList<NutsId> all = new IteratorList<NutsId>();
            for (NutsRepository repo : getEnabledRepositories()) {
                try {
                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                        Iterator<NutsId> child = repo.find(filter, session2);
                        all.add(child);
                    }
                } catch (Exception exc) {
                    //
                }
            }
            PushBackIterator<NutsId> b = new PushBackIterator<>(all);
            if (b.hasNext()) {
                b.pushBack();
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<NutsId> findVersions(String id, NutsVersionFilter versionFilter, NutsRepositoryFilter repositoryFilter, NutsSession session) throws IOException {
        NutsId nutsId = NutsId.parseOrError(id);
        return findVersions(nutsId, versionFilter, session, repositoryFilter);
    }

    public Iterator<NutsId> findVersions(NutsId id, NutsVersionFilter versionFilter, NutsSession session, NutsRepositoryFilter repositoryFilter) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            IteratorList<NutsId> all = new IteratorList<NutsId>();
            for (NutsRepository repo : getEnabledRepositories(id, session2)) {
                if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                    try {
                        Iterator<NutsId> child = repo.findVersions(id, versionFilter, session2);
                        all.add(child);
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            }
            PushBackIterator<NutsId> b = new PushBackIterator<>(all);
            if (b.hasNext()) {
                b.pushBack();
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    @Override
    public NutsDescriptor fetchDescriptor(String id, boolean effective, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId nutsId = resolveId(id, session);
        Set<String> errors = new LinkedHashSet<>();
        NutsSession transitiveSession = session.copy().setTransitive(true);
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, transitiveSession)) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = repo.fetchDescriptor(nutsId, session2);
                } catch (Exception exc) {
                    errors.add(StringUtils.exceptionToString(exc));
                }
                if (nutsDescriptor != null) {
                    if (effective) {
                        try {
                            return fetchEffectiveDescriptor(nutsDescriptor, session2);
                        } catch (NutsNotFoundException ex) {
                            //ignore
                        }
                    } else {
                        return nutsDescriptor;
                    }
                }
            }
        }
        throw new NutsNotFoundException(id, StringUtils.join("\n", errors), null);
    }

    @Override
    public String fetchHash(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId nutsId = NutsId.parseOrError(id);
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, session2)) {
                try {
                    String hash = repo.fetchHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    @Override
    public String fetchDescriptorHash(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId nutsId = NutsId.parseOrError(id);
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, session2)) {
                try {
                    String hash = repo.fetchDescriptorHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    public void push(String id, String repositoryId, NutsSession session) throws IOException {
        push(NutsId.parseOrError(id), repositoryId, session);
    }

    @Override
    public void addUser(String user) {
        throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);

    }

    public NutsId fetchEffectiveId(NutsId id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (id == null) {
            throw new NutsNotFoundException("<null>");
        }
        if (NutsUtils.isEffectiveId(id)) {
            return id;
        }
        return fetchDescriptor(id.toString(), true, session).getId();
    }

    public NutsId fetchEffectiveId(NutsDescriptor descriptor, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (descriptor == null) {
            throw new NutsNotFoundException("<null>");
        }
        NutsId thisId = descriptor.getId();
        if (NutsUtils.isEffectiveId(thisId)) {
            return thisId.setFace(descriptor.getFace());
        }
        String g = thisId.getGroup();
        String v = thisId.getVersion().getValue();
        if ((StringUtils.isEmpty(g)) || (StringUtils.isEmpty(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetchEffectiveId(fetchDescriptor(parent.toString(), false, session), session);
                if (StringUtils.isEmpty(g)) {
                    g = p.getGroup();
                }
                if (StringUtils.isEmpty(v)) {
                    v = p.getVersion().getValue();
                }
                if (!StringUtils.isEmpty(g) && !StringUtils.isEmpty(v)) {
                    break;
                }
            }
            NutsId bestId = new NutsId(null, g, thisId.getName(), v, "");
            String bestResult = bestId.toString();
            if (StringUtils.isEmpty(g) || StringUtils.isEmpty(v)) {
                throw new NutsNotFoundException(bestResult, "unable to fetchEffective for " + thisId + ". Best Result is " + bestResult, null);
            }
            return bestId.setFace(descriptor.getFace());
        } else {
            return thisId.setFace(descriptor.getFace());
        }
    }

    public NutsDescriptor fetchEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = fetchEffectiveDescriptor(
                    fetchDescriptor(parents[i].toString(), false, session),
                    session
            );
        }
        NutsDescriptor nutsDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
        if (nutsDescriptor.getPackaging().isEmpty()) {
            descriptor.applyParents(parentDescriptors).applyProperties();
        }
        return nutsDescriptor;
    }

    public NutsId deploy(InputStream contentInputStream, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException {
        throw new IOException("Invalid Nut Folder source. unable to detect descriptor");
    }

    public File fetch(String id, File localPath, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        return fetch(NutsId.parseOrError(id), session, localPath);
    }

    public File fetch(NutsId id, NutsSession session, File localPath) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        id = resolveId(id, session);
//        id = configureFetchEnv(id);
        Set<String> errors = new LinkedHashSet<>();
        NutsSession transitiveSession = session.copy().setTransitive(true);
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(id, transitiveSession)) {
                try {
                    return repo.fetch(id, session2, localPath);
                } catch (Exception ex) {
                    errors.add(StringUtils.exceptionToString(ex));
                }
            }
        }
        throw new NutsNotFoundException(id.toString(), StringUtils.join("\n", errors), null);
    }

    public NutsFile fetch(NutsId id, NutsSession session, boolean fetchDependencies) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        Set<NutsId> fetching = (Set<NutsId>) session.getProperties().get("private.runtime.fetch");
        if (fetching == null) {
            fetching = new HashSet<>();
            session.getProperties().put("private.runtime.fetch", fetching);
        }
        if (fetching.contains(id)) {
            throw new NutsResolutionPendingException(id);
        }
        try {
            fetching.add(id);
            log.log(Level.FINE, "fetch {0}", id);
            id = resolveId(id, session);
//        id = configureFetchEnv(id);
            NutsFile found = null;
            LinkedHashSet<String> errors = new LinkedHashSet<>();
            for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
                if (found != null) {
                    break;
                }
                NutsSession session2 = session.copy().setFetchMode(mode);
                try {
                    NutsSession transitiveSession = session2.copy().setTransitive(true);
                    String ns = id.getNamespace();
                    List<NutsRepository> enabledRepositories = new ArrayList<>();
                    if (!StringUtils.isEmpty(ns)) {
                        try {
                            NutsRepository repository = findRepository(ns);
                            if (repository != null) {
                                enabledRepositories.add(repository);
                            }
                        } catch (RepositoryNotFoundException ex) {
                            //
                        }
                    } else {
                        enabledRepositories = getEnabledRepositories(id, transitiveSession);
                    }
                    for (NutsRepository repo : enabledRepositories) {
                        NutsFile fetch = null;
                        try {
                            fetch = repo.fetch(id, session2);
                        } catch (Exception ex) {
                            errors.add(StringUtils.exceptionToString(ex));
                        }
                        if (fetch != null) {
                            if (StringUtils.isEmpty(fetch.getId().getNamespace())) {
                                fetch.setId(fetch.getId().setNamespace(repo.getRepositoryId()));
                            }
                            found = fetch;
                            break;
                        }
                    }
                } catch (NutsNotFoundException ex) {
                    //
                }
            }
            if (found == null) {
                throw new NutsNotFoundException(id.toString(), StringUtils.join("\n", errors), null);
            }
            if (fetchDependencies) {
                NutsDescriptor nutsDescriptor = fetchEffectiveDescriptor(found.getDescriptor(), session);
                for (NutsDependency dependency : nutsDescriptor.getDependencies()) {
                    try {
//                    NutsId resolvedId = resolveId(dependency.toId(), session);
//                    fetch(resolvedId, session, true);
                        fetch(dependency.toId(), session, true);
                    } catch (NutsResolutionPendingException ex) {
                        //ignore
                    } catch (NutsNotFoundException ex) {
                        throw new NutsNotFoundException(id.toString(), "Unable to resolve " + id.toString() + " : Missing dependency " + dependency.toId() + "\n" + StringUtils.exceptionToString(ex), ex);
                    } catch (Exception ex) {
                        throw new NutsNotFoundException(id.toString(), "Unable to resolve " + id.toString() + " : Missing dependency " + dependency.toId(), ex);
                    }
                }
            }
            NutsInstallerComponent installer = getInstaller(found, session);
            if (installer != null) {
                found.setInstalled(installer.isInstalled(found, this, session));
                if (found.isInstalled()) {
                    found.setInstallFolder(installer.getInstallFolder(new NutsExecutionContext(found, session, this)));
                }
            } else {
                found.setInstalled(true);
            }
            return found;
        } finally {
            fetching.remove(id);
        }
    }

    public void checkSupportedRepositoryType(String type) throws IOException {
        if (!isSupportedRepositoryType(type)) {
            throw new IllegalArgumentException("Unsupported repository type " + type);
        }
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (StringUtils.isEmpty(repositoryType)) {
            repositoryType = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }

        if (NutsConstants.DEFAULT_REPOSITORY_TYPE.equals(repositoryType)) {
            return true;
        }
        return "maven".equals(repositoryType);

    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    private boolean isLoadedClassPath(NutsFile file) {
        if (file.getId().isSameFullName(NutsId.parseOrError(NutsConstants.NUTS_COMPONENT_ID))) {
            return true;
        }
        try {
            if (file.getFile() != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getFile());
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(clz);
                                return true;
                            } catch (ClassNotFoundException e) {
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            //ignorereturn false;
                        }
                    }
                }

            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    @Override
    public NutsWorkspaceExtension[] getExtensions() {
        return new NutsWorkspaceExtension[0];
    }

    @Override
    public NutsWorkspaceFactory getFactory() {
        return emptyFactory;
    }

    @Override
    public void save() throws IOException {
        File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
        JsonUtils.storeJson(config, file, JsonUtils.PRETTY_IGNORE_EMPTY_OPTIONS);
        for (NutsRepository repo : getEnabledRepositories()) {
            repo.save();
        }
    }

    @Override
    public Map<String, Object> getSharedObjects() {
        return sharedObjects;
    }

    @Override
    public boolean isAllowed(String right) {
        return true;
    }

    @Override
    public boolean isRunningServer(String serverId) {
        return false;
    }

    @Override
    public List<NutsServer> getServers() {
        return Collections.emptyList();
    }

    @Override
    public void setUserCredentials(String user, String credentials) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    public void exec(String[] cmd, Properties env, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    public void exec(String id, String[] args, Properties env, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    public void push(NutsId id, String repositoryId, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    public NutsFile createBundle(File contentFolder, File destFile, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    public NutsId deploy(File contentFile, String contentFileSHA1, File descFile, String descSHA1, String repositoryId, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    public NutsId deploy(String contentURL, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    public NutsId deploy(String contentURL, String sha1, String descriptorURL, String descSHA1, String repositoryId, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    public NutsId deploy(File contentFile, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsWorkspaceExtension addExtension(String id, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public boolean installExtensionComponent(Class extensionPointType, Object extensionImpl) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return false;
    }

    @Override
    public NutsServer startServer(ServerConfig serverConfig) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsServer getServer(String serverId) {
        throw new IllegalArgumentException("Server not found " + serverId);
    }

    @Override
    public void stopServer(String serverId) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    @Override
    public NutsCommandLineConsoleComponent createCommandLineConsole(NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsTerminal createTerminal() throws IOException {
        return createTerminal(null, null, null);
    }

    @Override
    public NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException {
        NutsTerminal nutsTerminal = new DefaultNutsTerminal();
        nutsTerminal.install(this, in, out, err);
        return nutsTerminal;
    }

    protected boolean isEnabledRepository(String repoId) {
        NutsWorkspaceConfig.NutsRepositoryLocation repository = config.getRepository(repoId);
        return repository != null && repository.isEnabled();
    }

    public List<NutsRepository> getEnabledRepositories(NutsId nutsId, NutsSession session) {
        return filterRepositories(getEnabledRepositories(), nutsId, session);
    }

    public List<NutsRepository> getEnabledRepositories() {
        List<NutsRepository> repos = new ArrayList<>();
        for (NutsRepository repository : repositories.values()) {
            if (isEnabledRepository(repository.getRepositoryId())) {
                repos.add(repository);
            }
        }
        Collections.sort(repos, new Comparator<NutsRepository>() {
            @Override
            public int compare(NutsRepository o1, NutsRepository o2) {
                return Integer.compare(o1.getSpeed(), o2.getSpeed());
            }
        });
        return repos;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return BOOT_SUPPORT;
    }

    @Override
    public NutsPrintStream createEnhancedPrintStream(OutputStream out) {
        if (out == null) {
            return null;
        }
        return new NutsPrintStream(out);
    }

    private void throwSecurityException(String id) {
        throw new SecurityException("Not Allowed " + id);
    }

    public String resolveWorkspacePath(String workspace, String defaultName) throws IOException {
        if (StringUtils.isEmpty(workspace)) {
            File file = IOUtils.resolvePath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
            workspace = file==null?null:file.getPath();
        } else {
            File file = IOUtils.resolvePath(workspace, null, workspaceRoot);
            workspace = file==null?null:file.getPath();
        }

        Set<String> visited = new HashSet<String>();
        while (true) {
            File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
            NutsWorkspaceConfig nutsWorkspaceConfig = JsonUtils.loadJson(file, NutsWorkspaceConfig.class);
            if (nutsWorkspaceConfig != null) {
                String nextWorkspace = nutsWorkspaceConfig.getWorkspace();
                if (nextWorkspace != null && nextWorkspace.trim().length() > 0) {
                    if (visited.contains(nextWorkspace)) {
                        throw new IOException("Circular Workspace Dependency : " + nextWorkspace);
                    }
                    visited.add(nextWorkspace);
                    workspace = nextWorkspace;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return workspace;
    }

    @Override
    public NutsFile install(String id, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsId commit(File folder, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsFile checkout(String id, File folder, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsFile updateWorkspace(NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public NutsFile update(String id, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public List<NutsFile> update(Set<String> toUpdateIds, Set<String> toRetainDependencies, NutsSession session) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
        return null;
    }

    @Override
    public void login(String login, String password) {
    }

    public void logout() {
    }

    @Override
    public void setUserCredentials(String login, String password, String oldPassword) {
        throwSecurityException(NutsConstants.RIGHT_ADMIN);
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(NutsSession session, boolean applyUpdates, String[] args) {
        return new NutsUpdate[0];
    }

    @Override
    public NutsUpdate checkUpdates(String id, NutsSession session) {
        return null;
    }

    @Override
    public File getCwd() {
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public void setCwd(File file) {
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public boolean switchUnsecureMode(String adminPassword) {
        return false;
    }

    @Override
    public boolean switchSecureMode(String adminPassword) {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    @Override
    public String getWorkspaceRootLocation() {
        return workspaceRoot;
    }

    @Override
    public Map<String, String> getRuntimeProperties(NutsSession session) {
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public File resolveNutsJarFile() {
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public int execExternalNuts(NutsSession session, File nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor) {
        throw new IllegalArgumentException("Unsupported");
    }
}
