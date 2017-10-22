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
package net.vpc.app.nuts.extensions.workspaces;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.NutsIdPatternFilter;
import net.vpc.app.nuts.extensions.archetypes.DefaultNutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.extensions.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.app.nuts.util.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Principal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    private List<NutsFile> nutsComponentIdDependencies;
    private NutsFile nutsComponentId;
    private ObservableMap<String, Object> sharedObjects = new ObservableMap<>();
    private NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private Map<String, NutsRepository> repositories = new HashMap<String, NutsRepository>();
    private List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    private Map<String, NutsServer> servers = new HashMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<NutsId, NutsWorkspaceExtension>();
    private Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<Class>(
            Arrays.asList(
                    //order is important!!because autowiring should follow this very order
                    NutsPrintStream.class,
                    NutsTerminal.class,
                    NutsCommand.class,
                    NutsCommandLineConsoleComponent.class,
                    NutsDescriptorContentParserComponent.class,
                    NutsExecutorComponent.class,
                    NutsInstallerComponent.class,
                    NutsRepositoryFactoryComponent.class,
                    NutsServerComponent.class,
                    NutsTransportComponent.class,
                    NutsWorkspace.class,
                    NutsWorkspaceArchetypeComponent.class,
                    NutsCommandAutoCompleteComponent.class
            )
    );
    private DefaultNutsWorkspaceFactory factory = new DefaultNutsWorkspaceFactory();
    private String workspace;
    private boolean initializing;
    private ThreadLocal<LoginContext> currentSubject = new ThreadLocal<>();
    private ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private NutsWorkspace bootstrapWorkspace;
    private ClassLoader workspaceClassLoader;
    private File cwd=new File(System.getProperty("user.dir"));

    public DefaultNutsWorkspace() {

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
            face.put("arch", CorePlatformUtils.getArch());
            face.put("os", CorePlatformUtils.getOs());
            face.put("osdist", CorePlatformUtils.getOsdist());
            return id.setQuery(face);
        }
        return id;
    }

    @Override
    public String getCurrentLogin() {
        if (initializing) {
            return NutsConstants.USER_ADMIN;
        }
        String name = null;
        Subject currentSubject = getCurrentSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!StringUtils.isEmpty(name)) {
                    if (!StringUtils.isEmpty(name)) {
                        return name;
                    }
                }
            }
        }
        return NutsConstants.USER_ANONYMOUS;
    }

    public Subject getCurrentSubject() {
        LoginContext loginContext = currentSubject.get();
        return loginContext == null ? null : loginContext.getSubject();
    }

    @Override
    public void login(String login, String password) throws LoginException {
        login(new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) callbacks[i];
                        nameCallback.setName(login);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];
                        passwordCallback.setPassword(password == null ? null : password.toCharArray());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i], "The submitted Callback is unsupported");
                    }
                }
            }
        });
    }

    @Override
    public boolean switchUnsecureMode(String adminPassword) throws LoginException, IOException {
        if (adminPassword == null){
            adminPassword="";
        }
        NutsSecurityEntityConfig adminSecurity = getConfig().getSecurity(NutsConstants.USER_ADMIN);
        if (adminSecurity == null || StringUtils.isEmpty(adminSecurity.getCredentials())) {
            log.log(Level.SEVERE, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
            setUserCredentials(NutsConstants.USER_ADMIN, "admin");
        }
        String credentials = SecurityUtils.evalSHA1(adminPassword);
        if(Objects.equals(credentials,adminPassword)){
            throw new SecurityException("Invalid credentials");
        }
        boolean activated=false;
        if (getConfig().isSecure()) {
            getConfig().setSecure(false);
            activated=true;
        } else {
            activated=false;
        }
        return activated;
    }

    public boolean isAdmin() {
        return NutsConstants.USER_ADMIN.equals(getCurrentLogin());
    }

    @Override
    public boolean switchSecureMode(String adminPassword) throws LoginException, IOException {
        if (adminPassword == null){
            adminPassword="";
        }
        boolean deactivated=false;
        String credentials = SecurityUtils.evalSHA1(adminPassword);
        if(Objects.equals(credentials,adminPassword)){
            throw new SecurityException("Invalid credentials");
        }
        if (!getConfig().isSecure()) {
            getConfig().setSecure(true);
            deactivated=true;
        } else {
            deactivated=false;
        }
        return deactivated;
    }

    public String login(CallbackHandler handler) throws LoginException {
        NutsWorkspaceLoginModule.install();//initialize it
        NutsEnvironmentContext.WORKSPACE.set(this);
        if (!NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
            throw new LoginException("Already logged in");
        }
        LoginContext login = new LoginContext("nuts", handler);
        login.login();
        currentSubject.set(login);
        return getCurrentLogin();
    }

    public void logout() throws LoginException {
        if (NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
            throw new LoginException("Not logged in");
        }
        LoginContext loginContext = currentSubject.get();
        loginContext.logout();
        currentSubject.set(null);
    }

    @Override
    public void setUserCredentials(String login, String password, String oldPassword) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_SET_PASSWORD)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_SET_PASSWORD);
        }
        if (StringUtils.isEmpty(login)) {
            if (!NutsConstants.USER_ANONYMOUS.equals(getCurrentLogin())) {
                login = getCurrentLogin();
            } else {
                throw new IllegalArgumentException("Not logged in");
            }
        }
        NutsSecurityEntityConfig u = getConfig().getSecurity(login);
        if (u == null) {
            throw new IllegalArgumentException("No such user " + login);
        }

        if (!getCurrentLogin().equals(login)) {
            if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
                throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_ADMIN);
            }
        }
        if (!isAllowed(NutsConstants.RIGHT_ADMIN)) {
            if (StringUtils.isEmpty(password)) {
                throw new SecurityException("Missing old password");
            }
            //check old password
            if (StringUtils.isEmpty(u.getCredentials())
                    || u.getCredentials().equals(SecurityUtils.evalSHA1(password))) {
                throw new SecurityException("Invalid password");
            }
        }
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Missing password");
        }
        getConfig().setSecurity(u);
        setUserCredentials(u.getUser(), password);
    }

    @Override
    public String getWorkspaceVersion() {
        for (NutsWorkspaceExtension extension : getExtensions()) {
            if (extension.getWiredId().isSameFullName(NutsId.parseOrError(NutsConstants.NUTS_COMPONENT_CORE_ID))) {
                return extension.getWiredId().getVersion().toString();
            }
        }
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

    @Override
    public NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException {
        NutsWorkspace nutsWorkspace = getFactory().createSupported(NutsWorkspace.class, this);
        nutsWorkspace.initializeWorkspace(workspace, this, workspaceClassLoader, options.copy().setIgnoreIfFound(true), session);
        return nutsWorkspace;
    }

    /**
     * @param workspace            workspace location path
     * @param workspaceClassLoader
     * @param options              creation options
     * @param session              session   @return return true if created
     * @throws IOException
     */
    @Override
    public boolean initializeWorkspace(String workspace, NutsWorkspace bootstrapWorkspace, ClassLoader workspaceClassLoader, NutsWorkspaceCreateOptions options, NutsSession session) throws IOException {
        this.bootstrapWorkspace = bootstrapWorkspace;
        if (bootstrapWorkspace == null) {
            throw new IllegalArgumentException("Null Bootstrap Workspace");
        }
        this.workspaceClassLoader = workspaceClassLoader;
        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        boolean exists = isWorkspaceFolder(workspace);
        if (!options.isCreateIfNotFound() && !exists) {
            throw new NutsWorkspaceNotFoundException(workspace);
        }
        if (!options.isIgnoreIfFound() && exists) {
            throw new NutsWorkspaceAlreadyExistsException(workspace);
        }

        this.workspace = resolveWorkspacePath(workspace);

        //now will iterate over Extension classes to wire them ...
        ServiceLoader<NutsComponent> serviceLoader = ServiceLoader.load(NutsComponent.class, workspaceClassLoader);
        for (NutsComponent extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl.getClass();
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    defaultWiredComponents.add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }

        if (session.getTerminal() == null) {
            session.setTerminal(createTerminal(null));
        }

        //versionProperties = IOUtils.loadProperties(DefaultNutsWorkspace.class.getResource("/META-INF/nuts-core-version.properties"));


        initializing = true;
        try {
            if (!reloadWorkspace(options.isSaveIfCreated(), session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                if (!options.isCreateIfNotFound()) {
                    throw new NutsWorkspaceNotFoundException(workspace);
                }
                exists = false;
                config = new NutsWorkspaceConfig();
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    save();
                }
            } else if (config.getRepositories().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    save();
                }
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    private NutsTerminal createTerminal(Class ignoredClass) throws IOException {
        NutsTerminal term = getFactory().createSupported(NutsTerminal.class, this);
        if (term == null) {
            throw new RuntimeException("Should never happen ! Terminal could not be resolved.");
        } else {
            if (ignoredClass != null && ignoredClass.equals(term.getClass())) {
                return null;
            }
            term.install(this, null, null, null);
        }
        return term;
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

            //extensions already wired... this is needless!
            for (String extensionId : config.getExtensions()) {
                if (excludedExtensions != null && NutsUtils.finNutsIdByFullNameInStrings(NutsId.parseOrError(extensionId), excludedExtensions) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE);
                wireExtension(NutsId.parseOrError(extensionId), sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }


            NutsSecurityEntityConfig adminSecurity = this.config.getSecurity(NutsConstants.USER_ADMIN);
            if (adminSecurity == null || StringUtils.isEmpty(adminSecurity.getCredentials())) {
                log.log(Level.SEVERE, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
                setUserCredentials(NutsConstants.USER_ADMIN, "admin");
                if (save) {
                    save();
                }
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
        for (NutsWorkspaceArchetypeComponent extension : factory.createAllSupported(NutsWorkspaceArchetypeComponent.class, this)) {
            set.add(extension.getName());
        }
        return set;
    }

    protected void initializeWorkspace(String archetype, NutsSession session) throws IOException {
        if (StringUtils.isEmpty(archetype)) {
            archetype = "default";
        }
        //should be here but the problem is that no repository is already
        //registered so where would we install extension from ?
//        try {
//            addExtension(NutsConstants.NUTS_CORE_ID, session);
//        }catch(Exception ex){
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }

        NutsWorkspaceArchetypeComponent instance = factory.createSupported(NutsWorkspaceArchetypeComponent.class, this);
        if (instance == null) {
            //get the default implementation
            instance = new DefaultNutsWorkspaceArchetypeComponent();
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        setUserCredentials(NutsConstants.USER_ADMIN, "admin");

        instance.initialize(this, session);

//        //isn't it too late for adding extensions?
//        try {
//            addExtension(NutsConstants.NUTS_COMPONENT_CORE_ID, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
    }

    @Override
    public void addUser(String user) {
        if (StringUtils.isEmpty(user)) {
            throw new IllegalArgumentException("Invalid user");
        }
        config.setSecurity(new NutsSecurityEntityConfig(user, null, null, null));

    }

    @Override
    public void setUserCredentials(String user, String credentials) throws IOException {
        NutsSecurityEntityConfig security = getConfig().getSecurity(user);
        if (security == null) {
            throw new IllegalArgumentException("User not found " + user);
        }
        if (StringUtils.isEmpty(credentials)) {
            credentials = null;
        } else {
            credentials = SecurityUtils.evalSHA1(credentials);
        }

        security.setCredentials(credentials);
    }

    @Override
    public NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException {
        NutsRepository proxy = addRepository(repositoryId, repositoryId, NutsConstants.DEFAULT_REPOSITORY_TYPE, autoCreate);
        return proxy.addMirror(repositoryId + "-ref", location, type, autoCreate);
    }

    @Override
    public NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_ADD_REPOSITORY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_ADD_REPOSITORY);
        }

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
    public NutsRepository findRepository(String repositoryIdPath) throws IOException {
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

//    private NutsWorkspaceConfig loadWorkspaceConfig(String workspace) throws IOException {
//        workspace=resolveWorkspacePath(workspace);
//        File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
//        NutsWorkspaceConfig config = JsonUtils.loadJson(file, NutsWorkspaceConfig.class);
//        if(config==null){
//            config=new NutsWorkspaceConfig();
//        }
//        return config;
//    }

    @Override
    public void removeRepository(String repositoryId) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_REMOVE_REPOSITORY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_REMOVE_REPOSITORY);
        }
        NutsRepository removed = repositories.remove(repositoryId);
        getConfig().removeRepository(repositoryId);
        if (removed != null) {
            for (NutsWorkspaceListener nutsWorkspaceListener : getWorkspaceListeners()) {
                nutsWorkspaceListener.onRemoveRepository(this, removed);
            }
        }
    }

    private String resolveWorkspacePath(String workspace) throws IOException {
        if (StringUtils.isEmpty(workspace)) {
            workspace = IOUtils.resolvePath(NutsConstants.DEFAULT_WORKSPACE_ROOT + "/" + NutsConstants.DEFAULT_WORKSPACE_NAME, null).getPath();
        } else {
            workspace = IOUtils.resolvePath(workspace, null).getPath();
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

    private boolean isWorkspaceFolder(String workspace) throws IOException {
        workspace = resolveWorkspacePath(workspace);
        File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public NutsRepository openRepository(String repositoryId, File repositoryRoot, String location, String type, boolean autoCreate) throws IOException {
        if (StringUtils.isEmpty(type)) {
            type = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        NutsEnvironmentContext.WORKSPACE.set(this);
        NutsRepositoryFactoryComponent factory_ = factory.createSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(type, location));
        if (factory_ != null) {
            NutsRepository r = factory_.create(repositoryId, location, type, repositoryRoot);
            if (r != null) {
                r.open(autoCreate);
                return r;
            }
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

    private void postInstall(NutsFile nutToInstall, NutsSession session) throws IOException {
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installer = descriptor.getInstaller();
            NutsInstallerComponent nutsInstallerComponent = getInstaller(nutToInstall, session);
            if (nutsInstallerComponent == null) {
                return;
            }
            String[] args = null;
            Properties props = null;
            if (installer != null) {
                args = installer.getArgs();
                props = installer.getProperties();
            }
            NutsExecutionContext executionContext = new NutsExecutionContext(nutToInstall, new String[0], args, props, session, this);
            if (!nutsInstallerComponent.isInstalled(executionContext)) {
                nutsInstallerComponent.install(executionContext);
            }
        }
    }

    @Override
    public NutsFile install(String id, NutsSession session) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetch(id, true, session);
        if (nutToInstall != null && nutToInstall.getFile() != null && !nutToInstall.isInstalled()) {
            postInstall(nutToInstall, session);
        }
        return nutToInstall;
    }

    @Override
    public NutsId commit(File folder, NutsSession session) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_DEPLOY)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_DEPLOY);
        }
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException("Not a directory " + folder);
        }

        File file = new File(folder, NutsConstants.NUTS_DESC_FILE);
        NutsDescriptor d = NutsDescriptor.parse(file);
        String oldVersion = StringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = VersionUtils.incVersion(oldVersion);
            NutsFile newVersionFound = null;
            try {
                newVersionFound = fetch(d.getId().setVersion(newVersion).toString(), session);
            } catch (NutsNotFoundException ex) {

            }
            if (newVersionFound == null) {
                d = d.setId(d.getId().setVersion(newVersion));
            } else {
                d = d.setId(d.getId().setVersion(oldVersion + ".1"));
            }
            NutsId newId = deploy(folder, null, d, null, session);
            d.write(file);
            CoreIOUtils.delete(folder);
            return newId;
        } else {
            throw new IllegalArgumentException("commit not supported");
        }
    }

    @Override
    public NutsFile checkout(String id, File folder, NutsSession session) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetch(id, true, session);
        if ("zip".equals(nutToInstall.getDescriptor().getExt())) {
            CoreIOUtils.unzip(nutToInstall.getFile(), folder,getCwd());
            File file = new File(folder, NutsConstants.NUTS_DESC_FILE);
            NutsDescriptor d = NutsDescriptor.parse(file);
            NutsVersion oldVersion = d.getId().getVersion();
            NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
            d = d.setId(newId);
            d.write(file, true);
            return new NutsFile(
                    newId,
                    d,
                    folder,
                    false,
                    false
            );
        } else {
            throw new IllegalArgumentException("Checkout not supported");
        }
    }

    public NutsUpdate checkUpdates(String id, NutsSession session) throws IOException {
        NutsId baseId = NutsId.parseOrError(id);
        NutsVersion version = baseId.getVersion();
        NutsId oldId = null;
        NutsId newId = null;
        if (version.isSingleValue()) {
            try {
                oldId = bootstrapWorkspace.resolveId(id, session.setFetchMode(FetchMode.OFFLINE));
            } catch (Exception ex) {
                //ignore
            }
        } else {
            try {
                oldId = bootstrapWorkspace.resolveId(id, session.setFetchMode(FetchMode.OFFLINE));
            } catch (Exception ex) {
                //ignore
            }
        }
        try {
            newId = bootstrapWorkspace.resolveId(id, session.setFetchMode(FetchMode.ONLINE));
        } catch (Exception ex) {
            //ignore
        }
        if (newId != null && (oldId == null || !newId.equals(oldId))) {
            return new NutsUpdate(
                    baseId, oldId, newId
            );
        }
        return null;
    }

    private NutsFile bootstrapUpdate(String id, NutsSession session) throws IOException {
        NutsVersion version = NutsId.parseOrError(id).getVersion();
        if (version.isSingleValue()) {
            throw new IOException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsFile nutToInstall = bootstrapWorkspace.fetch(id, true, session);
        if (nutToInstall != null && nutToInstall.getFile() != null && !NutsConstants.DEFAULT_REPOSITORY_NAME.equals(nutToInstall.getId().getNamespace())) {
            bootstrapWorkspace.findRepository(NutsConstants.DEFAULT_REPOSITORY_NAME).deploy(
                    nutToInstall.getId(),
                    nutToInstall.getDescriptor(),
                    nutToInstall.getFile(),
                    session
            );
        }
        return nutToInstall;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        List<NutsUpdate> found = new ArrayList<>();
        NutsUpdate r = checkUpdates(NutsConstants.NUTS_COMPONENT_ID, session);
        if (r != null) {
            found.add(r);
        }
        if (requiresCoreExtension()) {
            r = checkUpdates(NutsConstants.NUTS_COMPONENT_CORE_ID, session);
            if (r != null) {
                found.add(r);
            }
        }
        for (String ext : getConfig().getExtensions()) {
            NutsId nutsId = NutsId.parseOrError(ext);
            r = checkUpdates(nutsId.toString(), session);
            if (r != null) {
                found.add(r);
            }
        }
        return found.toArray(new NutsUpdate[found.size()]);
    }

    /**
     * true when core extension is required for running this workspace.
     * A default implementation should be as follow, but developers may implements
     * this with other logic :
     * core extension is required when there are no extensions or when the <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code>
     * is forced to false
     *
     * @return true when core extension is required for running this workspace
     */
    public boolean requiresCoreExtension() {
        boolean exclude = false;
        if (getConfig().getExtensions().length > 0) {
            exclude = Boolean.parseBoolean(getConfig().getEnv(NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION, "false"));
        }
        if (!exclude) {
            boolean coreFound = false;
            for (String ext : getConfig().getExtensions()) {
                if (NutsId.parseOrError(ext).isSameFullName(NutsId.parse(NutsConstants.NUTS_COMPONENT_CORE_ID))) {
                    coreFound = true;
                    break;
                }
            }
            if (!coreFound) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludeCoreExtension() {
        if (getConfig().getExtensions().length == 0) {
            return false;
        }
        //should check some env?
        return false;
    }

    @Override
    public NutsFile updateWorkspace(NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsFile r = bootstrapUpdate(NutsConstants.NUTS_COMPONENT_ID, session);
        if (requiresCoreExtension()) {
            bootstrapUpdate(NutsConstants.NUTS_COMPONENT_CORE_ID, session);
        }
        List<NutsFile> updatedExtensions = new ArrayList<>();
        for (String ext : getConfig().getExtensions()) {
            NutsVersion version = NutsId.parseOrError(ext).getVersion();
            if (!version.isSingleValue()) {
                //will update bootstrap workspace so that next time
                //it will be loaded
                NutsFile newVersion = bootstrapUpdate(ext, session);
                if (!newVersion.getId().getVersion().equals(version)) {
                    updatedExtensions.add(newVersion);
                }
            }
        }
        if (updatedExtensions.size() > 0) {
            log.severe("Some extensions were updated. Nuts should be restarted for extensions to take effect.");
        }
        return r;
    }

    @Override
    public List<NutsFile> update(Set<String> toUpdateIds, Set<String> toRetainDependencies, NutsSession session) throws IOException {
        Map<String, NutsFile> all = new HashMap<>();
        for (String id : toUpdateIds) {
            NutsFile updated = update(id, session);
            all.put(updated.getId().getFullName(), updated);
        }
        if (toRetainDependencies != null) {
            for (String d : toRetainDependencies) {
                NutsDependency dd = NutsDependency.parse(d);
                if (all.containsKey(dd.getFullName())) {
                    NutsFile updated = all.get(dd.getFullName());
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new IllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public NutsFile update(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (!isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsVersion version = NutsId.parseOrError(id).getVersion();
        if (version.isSingleValue()) {
            throw new IOException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsFile nutToInstall = fetch(id, true, session);
        if (nutToInstall != null && nutToInstall.getFile() != null && !nutToInstall.isInstalled() && !nutToInstall.isCached()) {
            postInstall(nutToInstall, session);
        }
        return nutToInstall;
    }

    @Override
    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsFile nutToInstall = null;
        try {
            nutToInstall = fetch(id, checkDependencies, session.copy().setFetchMode(FetchMode.OFFLINE).setTransitive(false));
        } catch (Exception e) {
            return false;
        }
        return isInstalled(nutToInstall, session);
    }

    protected NutsInstallerComponent getInstaller(NutsFile nutToInstall, NutsSession session) throws IOException {
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsFile runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getArgs() != null && installerDescriptor.getArgs().length > 0) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch(installerDescriptor.getId(), session.copy().setTransitive(false), true);
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = getFactory().createSupported(NutsInstallerComponent.class, runnerFile);
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    protected boolean isInstalled(NutsFile nutToInstall, NutsSession session) throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return true;
        }
        return ii.isInstalled(nutToInstall, this, session);
    }

    public boolean uninstall(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (!isAllowed(NutsConstants.RIGHT_UNINSTALL)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_UNINSTALL);
        }
        NutsFile nutToInstall = fetch(id, true, session.copy().setTransitive(false));
        if (!isInstalled(nutToInstall, session)) {
            throw new IOException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return false;
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        NutsExecutionContext executionContext = new NutsExecutionContext(nutToInstall, new String[0], installer == null ? null : installer.getArgs(), installer == null ? null : installer.getProperties(), session, this);
        ii.uninstall(executionContext);
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

    public void exec(String[] cmd, NutsSession session) throws IOException{
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (cmd == null || cmd.length == 0) {
            throw new IllegalArgumentException("Missing command");
        }
        String[] args2 = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args2, 0, args2.length);
        exec(
                cmd[0],
                args2,
                session
        );
    }

    public NutsWorkspaceConfig getConfig() {
        return config;
    }

    public void exec(String id, String[] args, NutsSession session) throws IOException{
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (!isAllowed(NutsConstants.RIGHT_EXEC)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_EXEC + " : " + id);
        }
        if (id.contains("/") || id.contains("\\")) {
            try (CharacterizedFile c=characterize(new File(id), null,session)){
                if (c.descriptor == null) {
                    //this is a native file?
                    c.descriptor= new DefaultNutsDescriptor(
                            NutsId.parse("temp:exe#1.0"),
                            null,
                            null,
                            "exe",
                            true, "exe", new NutsExecutorDescriptor(NutsId.parse("exec"), new String[0], null), null, null, null, null, null, null, null, null, null
                    );
                }
                NutsFile nutToRun=new NutsFile(
                        c.descriptor.getId(),
                        c.descriptor,
                        c.contentFile,
                        false,
                        c.tempFile!=null
                );
                exec(nutToRun,args,session);
            }
        } else {
            NutsId nid = NutsId.parseOrError(id);
            exec(nid, args, session);
        }
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) throws IOException {
        for (NutsExecutorComponent nutsExecutorComponent : getFactory().createAll(NutsExecutorComponent.class)) {
            if(nutsExecutorComponent.getId().isSameFullName(nutsId)){
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsFile nutsFile) throws IOException {
        NutsExecutorComponent executorComponent = getFactory().createSupported(NutsExecutorComponent.class, nutsFile);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException("Nuts Executor not found for " + nutsFile);
    }

    protected void exec(NutsId nutsId, String[] args, NutsSession session) throws IOException{
        NutsFile nutToRun = fetch(nutsId, session, true);
        exec(nutToRun, args, session);
    }

    protected void exec(NutsFile nutToRun, String[] appArgs, NutsSession session) throws IOException{
        if (nutToRun != null && nutToRun.getFile() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsExecutorDescriptor executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            String[] executrorArgs = null;
            Properties execProps = null;
            if (executor == null) {
                execComponent = resolveNutsExecutorComponent(nutToRun);
            } else {
                if (executor.getId() == null) {
                    execComponent = resolveNutsExecutorComponent(nutToRun);
                } else {
                    execComponent = resolveNutsExecutorComponent(executor.getId());
//                    NutsFile runnerFile = fetch(executor.getId(), session, true);
//                    execComponent = resolveNutsExecutorComponent(runnerFile);
                }
                executrorArgs = executor.getArgs();
                execProps = executor.getProperties();
            }
            boolean nowait = false;
            if (appArgs.length > 0 && "&".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                nowait = true;
            }
            if (appArgs.length > 0 && ">null".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                session = session.copy();
                NutsPrintStream nostream = createEnhancedPrintStream(CoreIOUtils.NULL_PRINT_STREAM);
                NutsTerminal t = createTerminal(null, nostream, nostream);
                session.setTerminal(t);
            }
            final NutsExecutionContext executionContext = new NutsExecutionContext(nutToRun, appArgs, executrorArgs, execProps, session, this,nutToRun.getDescriptor().getExecutor());
            if (nowait) {
                NutsExecutorComponent finalExecComponent = execComponent;
                Thread thread = new Thread("Exec-" + nutToRun.getId().toString()) {
                    @Override
                    public void run() {
                        try {
                            finalExecComponent.exec(executionContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.setDaemon(true);
                thread.start();
            } else {
                execComponent.exec(executionContext);
            }
            return;
        }
        throw new NutsNotFoundException("Nuts not found " + nutToRun);
    }

    public boolean isFetched(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        return isFetched(NutsId.parseOrError(id), session);
    }

    public boolean isFetched(NutsId id, NutsSession session) throws IOException {
        NutsSession offlineSession = session.copy().setFetchMode(FetchMode.OFFLINE);
//        NutsId nutsId = null;
//        try {
//            nutsId = resolveId(id, offlineSession);
//        } catch (Exception e) {
//            return false;
//        }
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
                    String[] groups = getConfig().getImports();
                    for (String group : groups) {
                        try {
                            NutsId f = resolveId(id.setGroup(group), session);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException ex) {
                            //not found
                        }
                    }
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

    @Override
    public List<NutsId> find(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) throws IOException {
        return CorePlatformUtils.toList(findIterator(repositoryFilter, filter, session));
    }

    public Iterator<NutsId> findIterator(NutsRepositoryFilter repositoryFilter, NutsDescriptorFilter filter, NutsSession session) throws IOException {
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
    public NutsDescriptor fetchDescriptor(String idString, boolean effective, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId id = NutsId.parseOrError(idString);
        id = configureFetchEnv(id);
        Set<String> errors = new LinkedHashSet<>();
        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfig().getImports();
                    for (String group : groups) {
                        try {
                            NutsDescriptor f = fetchDescriptor(id.setGroup(group).toString(), effective, session);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException exc) {
                            errors.add(StringUtils.exceptionToString(exc));
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }

                for (NutsRepository repo : getEnabledRepositories(id, session)) {
                    try {
                        NutsDescriptor child = repo.fetchDescriptor(id, session);
                        if (child != null) {
//                            if (StringUtils.isEmpty(child.getId().getNamespace())) {
//                                child = child.setId(child.getId().setNamespace(repo.getRepositoryId()));
//                            }
                            if (effective) {
                                try {
                                    return fetchEffectiveDescriptor(child, session);
                                } catch (NutsNotFoundException ex) {
                                    //ignore
                                }
                            } else {
                                return child;
                            }
                        }
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }


//        NutsId nutsId = resolveId(id, session);
//        Set<String> errors = new LinkedHashSet<>();
//        NutsSession transitiveSession = session.copy().setTransitive(true);
//        for (FetchMode mode : resolveFetchModes(session.getFetchMode())) {
//            NutsSession session2 = session.copy().setFetchMode(mode);
//            for (NutsRepository repo : getEnabledRepositories(nutsId, transitiveSession)) {
//                NutsDescriptor nutsDescriptor = null;
//                try {
//                    nutsDescriptor = repo.fetchDescriptor(nutsId, session2);
//                } catch (Exception exc) {
//                    errors.add(StringUtils.exceptionToString(exc));
//                }
//                if (nutsDescriptor != null) {
//                    if (effective) {
//                        try {
//                            return fetchEffectiveDescriptor(nutsDescriptor, session2);
//                        } catch (NutsNotFoundException ex) {
//                            //ignore
//                        }
//                    } else {
//                        return nutsDescriptor;
//                    }
//                }
//            }
//        }
        throw new NutsNotFoundException(idString, StringUtils.join("\n", errors), null);
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

    public void push(NutsId id, String repositoryId, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (StringUtils.trim(id.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new IOException("Invalid Version " + id.getVersion());
        }
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsFile file = fetch(id, nonTransitiveSession, false);
        if (file == null) {
            throw new IOException("Nothing to push");
        }
        if (StringUtils.isEmpty(repositoryId)) {
            Set<String> errors = new LinkedHashSet<>();
            for (NutsRepository repo : getEnabledRepositories(file.getId(), session)) {
                NutsFile id2 = null;
                try {
                    id2 = repo.fetch(file.getId(), session);
                } catch (Exception e) {
                    errors.add(StringUtils.exceptionToString(e));
                    //
                }
                if (id2 != null && repo.isSupportedMirroring()) {
                    try {
                        repo.push(id, repositoryId, session);
                        return;
                    } catch (Exception e) {
                        errors.add(StringUtils.exceptionToString(e));
                        //
                    }
                }
            }
            throw new RepositoryNotFoundException(repositoryId + " : " + StringUtils.join("\n", errors));
        } else {
            NutsRepository repository = findRepository(repositoryId);
            checkEnabled(repository.getRepositoryId());
            repository.deploy(file.getId(), file.getDescriptor(), file.getFile(), session);
        }
    }

    private NutsDescriptor resolveNutsDescriptorFromFileContent(File localPath,NutsSession session) {
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = factory.createAllSupported(NutsDescriptorContentParserComponent.class, this);
            if (allParsers.size() > 0) {
                NutsEnvironmentContext.WORKSPACE.set(this);
                String fileExtension = IOUtils.getFileExtension(localPath);
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(this,session,localPath, fileExtension, null, null);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    public NutsFile createBundle(File contentFolder, File destFile, NutsSession session) throws IOException {
        if (contentFolder.isDirectory()) {
            NutsDescriptor descriptor = null;
            File ext = new File(contentFolder, NutsConstants.NUTS_DESC_FILE);
            if (ext.exists()) {
                descriptor = NutsDescriptor.parse(ext);
            } else {
                descriptor = resolveNutsDescriptorFromFileContent(contentFolder,session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getExt())) {
                    if (destFile == null) {
                        destFile = CoreIOUtils.createFileByCwd(contentFolder.getParent() + "/" + descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip",getCwd());
//                        destFile=new File(contentFolder.getPath() + ".zip");
                    }
                    CoreIOUtils.zip(contentFolder, destFile);
                    return new NutsFile(
                            descriptor.getId(),
                            descriptor,
                            destFile,
                            true,
                            false
                    );
                } else {
                    throw new IOException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
            throw new IOException("Invalid Nut Folder source. unable to detect descriptor");
        } else {
            throw new IOException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
        }
    }

    private CharacterizedFile characterize(File contentFile, File descFile, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        CharacterizedFile c = new CharacterizedFile();
        c.contentFile=contentFile;
        if (!c.contentFile.exists()) {
            throw new IOException("File does not exists " + c.contentFile);
        }
        if (c.contentFile.isDirectory()) {
            if (descFile == null) {
                File ext = new File(c.contentFile, NutsConstants.NUTS_DESC_FILE);
                if (ext.exists()) {
                    c.descriptor = NutsDescriptor.parse(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile,session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getExt())) {
                        File zipFilePath = CoreIOUtils.createFileByCwd(c.contentFile.getPath() + ".zip",getCwd());
                        CoreIOUtils.zip(c.contentFile, zipFilePath);
                        c.contentFile = zipFilePath;
                        c.tempFile = c.contentFile;
                    } else {
                        throw new IOException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else {
                c.descriptor = NutsDescriptor.parse(descFile);
            }
        } else if (c.contentFile.isFile()) {
            if (descFile == null) {
                File ext = CoreIOUtils.createFileByCwd(c.contentFile.getPath() + "." + NutsConstants.NUTS_DESC_FILE,cwd);
                if (ext.exists()) {
                    c.descriptor = NutsDescriptor.parse(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile,session);
                }
            } else {
                c.descriptor = NutsDescriptor.parse(descFile);
            }
        } else {
            throw new IOException("Path does not denote a valid file or folder " + c.contentFile);
        }
        return c;
    }

    public NutsId deploy(File contentFile, String contentFileSHA1, File descFile, String descSHA1, String repositoryId, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (!contentFile.exists()) {
            throw new IOException("File does not exists " + contentFile);
        }
        if (descFile != null) {
            if (descSHA1 != null && SecurityUtils.evalSHA1(descFile).equals(descSHA1)) {
                throw new IOException("Invalid Content Hash");
            }
        }
        try (CharacterizedFile c=characterize(contentFile, descFile,session)){
            if (c.descriptor == null) {
                throw new IOException("Missing descriptor");
            }
            return deploy(c.contentFile, contentFileSHA1, c.descriptor, repositoryId, session);
        }
    }

    public NutsId deploy(String contentURL, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException {
        File tempFile = null;
        File contentFile = null;
        try {
            if (contentURL.contains("://")) {
                tempFile = CoreIOUtils.createTempFile(new URL(contentURL));
                contentFile = tempFile;
            } else {
                contentFile = CoreIOUtils.createFileByCwd(contentURL,getCwd());
            }
            return deploy(contentFile, sha1, descriptor, repositoryId, session);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    public NutsId deploy(String contentURL, String sha1, String descriptorURL, String descSHA1, String repositoryId, NutsSession session) throws IOException {
        if (StringUtils.isEmpty(descriptorURL)) {
            descriptorURL = null;
        }
        File tempFile = null;
        File tempDescFile = null;
        File contentFile = null;
        File descFile = null;
        try {
            if (contentURL.contains("://")) {
                tempFile = CoreIOUtils.createTempFile(new URL(contentURL));
                contentFile = tempFile;
            } else {
                contentFile = CoreIOUtils.createFileByCwd(contentURL,getCwd());
            }
            if (descriptorURL != null) {
                if (descriptorURL.contains("://")) {
                    tempDescFile = CoreIOUtils.createTempFile(new URL(descriptorURL));
                    descFile = tempDescFile;
                } else {
                    descFile = CoreIOUtils.createFileByCwd(descriptorURL,getCwd());
                }
            }
            return deploy(contentFile, sha1, descFile, descSHA1, repositoryId, session);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
            if (tempDescFile != null) {
                tempDescFile.delete();
            }
        }
    }

    public NutsId deploy(File contentFile, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        File tempFile = null;
        try {
            if (contentFile.isDirectory()) {
                File descFile = new File(contentFile, NutsConstants.NUTS_DESC_FILE);
                NutsDescriptor descriptor2;
                if (descFile.exists()) {
                    descriptor2 = NutsDescriptor.parse(descFile);
                } else {
                    descriptor2 = resolveNutsDescriptorFromFileContent(contentFile,session);
                }
                if (descriptor == null) {
                    descriptor = descriptor2;
                } else {
                    if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                        descriptor.write(descFile);
                    }
                }
                if (descriptor != null) {
                    if ("zip".equals(descriptor.getExt())) {
                        File zipFilePath = CoreIOUtils.createFileByCwd(contentFile.getPath() + ".zip",getCwd());
                        CoreIOUtils.zip(contentFile, zipFilePath);
                        contentFile = zipFilePath;
                        tempFile = contentFile;
                    } else {
                        throw new IOException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else {
                if (sha1 != null && SecurityUtils.evalSHA1(contentFile).equals(sha1)) {
                    throw new IOException("Invalid Content Hash");
                }
                if (descriptor == null) {
                    descriptor = resolveNutsDescriptorFromFileContent(contentFile,session);
                }
            }
            if (descriptor == null) {
                throw new NutsNotFoundException(" at " + contentFile);
            }
            if (StringUtils.isEmpty(descriptor.getExt())) {
                int r = contentFile.getName().lastIndexOf(".");
                if (r >= 0) {
                    descriptor = descriptor.setExt(contentFile.getName().substring(r + 1));
                }
            }
            //remove workspace
            descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
            if (StringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
                throw new IOException("Invalid Version " + descriptor.getId().getVersion());
            }

            NutsSession transitiveSession = session.copy().setTransitive(true);

            NutsId effId = fetchEffectiveId(descriptor, transitiveSession);
            for (String os : descriptor.getOs()) {
                CorePlatformUtils.checkSupportedOs(NutsId.parseOrError(os).getFullName());
            }
            for (String arch : descriptor.getArch()) {
                CorePlatformUtils.checkSupportedArch(NutsId.parseOrError(arch).getFullName());
            }
            if (StringUtils.isEmpty(repositoryId)) {
                class NutsRepositoryInfo implements Comparable<NutsRepositoryInfo> {

                    NutsRepository repo;
                    int supportLevel;
                    int deployOrder;

                    @Override
                    public int compareTo(NutsRepositoryInfo o) {
                        int x = Integer.compare(o.deployOrder, this.deployOrder);
                        if (x != 0) {
                            return x;
                        }
                        x = Integer.compare(o.supportLevel, this.supportLevel);
                        if (x != 0) {
                            return x;
                        }
                        return 0;
                    }
                }
                List<NutsRepositoryInfo> possible = new ArrayList<>();
                for (NutsRepository repo : getEnabledRepositories(effId, session)) {
                    int t = 0;
                    try {
                        t = repo.getSupportLevel(effId, session);
                    } catch (Exception e) {
                        //ignore...
                    }
                    if (t > 0) {
                        NutsRepositoryInfo e = new NutsRepositoryInfo();
                        e.repo = repo;
                        e.supportLevel = t;
                        e.deployOrder = CoreStringUtils.parseInt(repo.getConfig().getEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "0"), 0);
                        possible.add(e);
                    }
                }
                if (possible.size() > 0) {
                    Collections.sort(possible);
                    return possible.get(0).repo.deploy(effId, descriptor, contentFile, session);
                }
            } else {
                NutsRepository goodRepo = getEnabledRepositoryOrError(repositoryId);
                if (goodRepo == null) {
                    throw new IOException("Repository Not found " + repositoryId);
                }
                return goodRepo.deploy(effId, descriptor, contentFile, session);
            }
            throw new IOException("Repository Not found " + repositoryId);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
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
            for (int i = 0; i < parents.length; i++) {
                NutsId p = fetchEffectiveId(fetchDescriptor(parents[i].toString(), false, session), session);
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
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        File tempFile = IOUtils.createTempFile(descriptor);
        try {
            IOUtils.copy(contentInputStream, tempFile, true, true);
            return deploy(tempFile, sha1, descriptor, repositoryId, session);
        } finally {
            tempFile.delete();
        }
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
                    File fetch = repo.fetch(id, session2, localPath);
                    if (fetch != null) {
                        return fetch;
                    } else {
                        errors.add(StringUtils.exceptionToString(new NutsNotFoundException(id.toString())));
                    }
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
    }

    public void checkSupportedRepositoryType(String type) throws IOException {
        if (!isSupportedRepositoryType(type)) {
            throw new IllegalArgumentException("Unsupported repository type " + type);
        }
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) throws IOException {
        if (StringUtils.isEmpty(repositoryType)) {
            repositoryType = NutsConstants.DEFAULT_REPOSITORY_TYPE;
        }
        NutsEnvironmentContext.WORKSPACE.set(this);
        return factory.createAllSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(repositoryType, null)).size() > 0;
    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    @Override
    public NutsWorkspaceExtension addExtension(String id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsId oldId = CoreNutsUtils.finNutsIdByFullName(NutsId.parseOrError(id), extensions.keySet());
        NutsWorkspaceExtension old = null;
        if (oldId == null) {
            NutsId nutsId = resolveId(id, session);
            NutsId eid = NutsId.parseOrError(id);
            if (StringUtils.isEmpty(eid.getGroup())) {
                eid = eid.setGroup(nutsId.getGroup());
            }
            getConfig().addExtension(eid);
            return wireExtension(eid, session);
        } else {
            old = extensions.get(oldId);
            getConfig().addExtension(NutsId.parseOrError(id));
            return old;
        }
    }

    protected NutsWorkspaceExtension wireExtension(NutsId id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        if (id == null) {
            throw new IllegalArgumentException("Extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.finNutsIdByFullName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsWorkspaceExtensionAlreadyRegisteredException(id.toString(), wired.toString());
        }
        log.log(Level.FINE, "Installing extension {0}", id);
        List<NutsFile> nutsFiles = fetchWithDependencies(id, true, NutsUtils.EXEC_DEPENDENCIES_FILTER, session);
        NutsId toWire = null;
        for (NutsFile nutsFile : nutsFiles) {
            if (nutsFile.getId().isSameFullName(id)) {
                if (toWire == null || toWire.getVersion().compareTo(nutsFile.getId().getVersion()) < 0) {
                    toWire = nutsFile.getId();
                }
            }
        }
        if (toWire == null) {
            toWire = id;
        }
        List<URL> urls = nutsFiles.stream()
                .filter(x ->
                        !isLoadedClassPath(x, session)
                ).map(x -> {
                    try {
                        return (x == null || x.getFile() == null) ? null : x.getFile().toURI().toURL();
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).filter(x -> x != null).collect(Collectors.toList());
        URL[] urlArr = urls.toArray(new URL[urls.size()]);
        ClassLoader cls = workspaceClassLoader == null ? new URLClassLoader(urlArr) : new URLClassLoader(urlArr, workspaceClassLoader);
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, toWire, cls);

        //now will iterate over Extension classes to wire them ...
        ServiceLoader<NutsComponent> serviceLoader = ServiceLoader.load(NutsComponent.class, workspaceExtension.getClassLoader());
        for (NutsComponent extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl.getClass();
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }
        extensions.put(id, workspaceExtension);
        log.log(Level.FINE, "Extension {0} installed successfully", id);
        NutsTerminal newTerminal = createTerminal(session.getTerminal() == null ? null : session.getTerminal().getClass());
        if (newTerminal != null) {
            log.log(Level.FINE, "Extension {0} changed Terminal configuration. Reloading Session Terminal", id);
            session.setTerminal(newTerminal);
        }

        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NutsFile file, NutsSession session) {
        if (file.getId().isSameFullName(NutsId.parseOrError(NutsConstants.NUTS_COMPONENT_ID))) {
            return true;
        }
        try {
//            NutsFile file = fetch(id.toString(), session);
            if (file.getFile() != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getFile());
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (zname.endsWith(".class")) {
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
                            return false;
                        }
                    }
                }

            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        if (!factory.isRegisteredType(extensionPointType, extensionImpl.getClass().getName())
                && !factory.isRegisteredInstance(extensionPointType, extensionImpl)) {
            factory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    private boolean registerType(Class extensionPointType, Class extensionType) {
        if (!factory.isRegisteredType(extensionPointType, extensionType.getName())
                && !factory.isRegisteredType(extensionPointType, extensionType)) {
            factory.registerType(extensionPointType, extensionType);
            return true;
        }
        log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
        return false;
    }

    private List<Class> resolveComponentTypes(Object o) {
        List<Class> a = new ArrayList<>();
        if (o != null) {
            for (Class extensionPointType : SUPPORTED_EXTENSION_TYPES) {
                if (extensionPointType.isInstance(o)) {
                    a.add(extensionPointType);
                }
            }
        }
        return a;
    }

    @Override
    public boolean installExtensionComponent(Class extensionPointType, Object extensionImpl) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    public boolean installExtensionComponentType(Class extensionPointType, Class extensionImplType) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isAssignableFrom(extensionImplType)) {
                return registerType(extensionPointType, extensionImplType);
            }
            throw new ClassCastException(extensionImplType.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    @Override
    public NutsWorkspaceExtension[] getExtensions() {
        return extensions.values().toArray(new NutsWorkspaceExtension[extensions.size()]);
    }

    @Override
    public void save() throws IOException {
        if (!isAllowed(NutsConstants.RIGHT_SAVE_WORKSPACE)) {
            throw new SecurityException("Not Allowed " + NutsConstants.RIGHT_SAVE_WORKSPACE);
        }
        File file = IOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_FILE);
        JsonUtils.storeJson(config, file, JsonUtils.PRETTY_IGNORE_EMPTY_OPTIONS);
        for (NutsRepository repo : getEnabledRepositories()) {
            repo.save();
        }
    }

    @Override
    public NutsWorkspaceFactory getFactory() {
        return factory;
    }

    @Override
    public Map<String, Object> getSharedObjects() {
        return sharedObjects;
    }

    @Override
    public boolean isAllowed(String right) {
        NutsWorkspaceConfig c = getConfig();
        if(!c.isSecure()){
            return true;
        }
        String name = getCurrentLogin();
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        if (NutsConstants.USER_ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsSecurityEntityConfig s = c.getSecurity(n);
            if (s != null) {
                if (s.containsRight(right)) {
                    return true;
                }
                for (String g : s.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public NutsServer startServer(ServerConfig serverConfig) throws IOException {
        if (serverConfig == null) {
            serverConfig = new HttpServerConfig();
        }
        NutsServerComponent server = factory.createSupported(NutsServerComponent.class, serverConfig);
        if (server == null) {
            throw new IllegalArgumentException("Not server extensions are registered.");
        }
        NutsServer s = server.start(this, serverConfig);
        if (servers.get(s.getServerId()) != null) {
            servers.get(s.getServerId()).stop();
        }
        servers.put(s.getServerId(), s);
        return s;
    }

    @Override
    public NutsServer getServer(String serverId) {
        NutsServer nutsServer = servers.get(serverId);
        if (nutsServer == null) {
            throw new IllegalArgumentException("Server not found " + serverId);
        }
        return nutsServer;
    }

    @Override
    public void stopServer(String serverId) throws IOException {
        getServer(serverId).stop();
    }

    @Override
    public boolean isRunningServer(String serverId) {
        NutsServer nutsServer = servers.get(serverId);
        if (nutsServer == null) {
            return false;
        }
        return nutsServer.isRunning();
    }

    @Override
    public List<NutsServer> getServers() {
        return new ArrayList<>(servers.values());
    }

    @Override
    public NutsCommandLineConsoleComponent createCommandLineConsole(NutsSession session) throws IOException {
        NutsCommandLineConsoleComponent cmd = getFactory().createSupported(NutsCommandLineConsoleComponent.class, this);
        if (cmd == null) {
            throw new NutsExtensionMissingException(NutsCommandLineConsoleComponent.class, "Console");
        }
        cmd.init(this, session);
        return cmd;
    }

    @Override
    public NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException {
        NutsTerminal term = getFactory().createSupported(NutsTerminal.class, null);
        if (term == null) {
            throw new NutsExtensionMissingException(NutsCommandLineConsoleComponent.class, "Terminal");
        }
        term.install(this, in, out, err);
        return term;
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = repositories.get(repoId);
        if (r != null) {
            if (!isEnabledRepository(repoId)) {
                throw new IllegalArgumentException("Repository " + repoId + " is disabled.");
            }
        }
        return r;
    }

    protected boolean isEnabledRepository(String repoId) {
        NutsWorkspaceConfig.NutsRepositoryLocation repository = config.getRepository(repoId);
        return repository != null && repository.isEnabled();
    }

    protected void checkEnabled(String repoId) {
        if (!isEnabledRepository(repoId)) {
            throw new IllegalArgumentException("Repository " + repoId + " is disabled");
        }
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
        return CORE_SUPPORT;
    }

    @Override
    public NutsPrintStream createEnhancedPrintStream(OutputStream out) throws IOException {
        if (out == null) {
            return null;
        }
        if(out instanceof NutsPrintStream){
            return (NutsPrintStream) out;
        }
        return getFactory().createSupported(NutsPrintStream.class, this, new Class[]{OutputStream.class}, new Object[]{out});
    }

    public File getCwd() {
        return cwd;
    }

    public void setCwd(File cwd) {
        if(cwd==null){
            throw new IllegalArgumentException("Invalid cwd");
        }
        if(!cwd.isDirectory()){
            throw new IllegalArgumentException("Invalid cwd "+cwd);
        }
        if(!cwd.isAbsolute()){
            throw new IllegalArgumentException("Invalid cwd "+cwd);
        }
        this.cwd = cwd;
    }

    private class CharacterizedFile implements AutoCloseable{
        File contentFile;
        File tempFile;
        NutsDescriptor descriptor;

        @Override
        public void close() {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}
