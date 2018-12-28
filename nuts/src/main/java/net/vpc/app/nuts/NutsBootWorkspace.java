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
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * NutsBootWorkspace is responsible of loading initial nuts-core.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace(NutsWorkspaceOptions)} . NutsBootWorkspace
 * is also responsible of managing local jar cache folder located at
 * $root/bootstrap where $root is the nuts root folder (~/.nuts) defined by
 * {@link #getHomeLocation()}.
 * <pre>
 *   ~/.nuts/bootstrap ({@link #getBootstrapLocation})
 *       └── net
 *           └── vpc
 *               └── app
 *                   └── nuts
 *                       ├── nuts
 *                       │   └── 0.3.8
 *                       │   │   └── nuts.properties
 *                       │   └── LATEST
 *                       │       └── nuts.properties
 *                       └── nuts-core
 *                           └── 0.3.8.0
 *                               └── nuts-core.properties
 * </pre> Created by vpc on 1/6/17.
 *
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-core.jar and its dependencies and for creating workspaces using
 * the method {@link #openWorkspace()}.
 * <p>
 * Created by vpc on 1/6/17.
 */
public class NutsBootWorkspace {

    public static final Logger log = Logger.getLogger(NutsBootWorkspace.class.getName());
    private NutsWorkspaceOptions options;
    private String home;
    private String runtimeSourceURL;
    private String runtimeId;
    private NutsClassLoaderProvider contextClassLoaderProvider;

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions()
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(true);
        }
        this.options = options;
        this.home = NutsUtils.isEmpty(options.getHome()) ? NutsConstants.DEFAULT_NUTS_HOME : options.getHome();
        this.runtimeSourceURL = options.getBootRuntimeSourceURL();
        this.runtimeId = NutsUtils.isEmpty(options.getBootRuntime()) ? null : BootNutsId.parse(options.getBootRuntime()).toString();
        this.contextClassLoaderProvider = options.getClassLoaderProvider() == null ? DefaultNutsClassLoaderProvider.INSTANCE : options.getClassLoaderProvider();
    }

    public NutsWorkspace openWorkspace() {

        String bootIdActualVersion = Nuts.getActualVersion();
        BootNutsId wbootId = BootNutsId.parse(NutsConstants.NUTS_ID_BOOT_API + "#" + bootIdActualVersion);
        NutsBootConfig bootConfig0 = null;
        NutsBootConfig actualBootConfig = null;
        try {

            long startTime = options.getCreationTime();
            if (startTime == 0) {
                options.setCreationTime(startTime = System.currentTimeMillis());
            }
            String workspaceLocation = options.getWorkspace();
            workspaceLocation = resolveWorkspacePath(workspaceLocation, NutsConstants.DEFAULT_WORKSPACE_NAME);
            NutsLogUtils.prepare(options.getLogLevel(), options.getLogFolder(), options.getLogName(), options.getLogSize(), options.getLogCount(), this.home, workspaceLocation);
            log.log(Level.CONFIG, "Open Workspace {0} with options {1}", new Object[]{workspaceLocation, options});
//            if (new File(workspaceLocation).equals(new File(home, NutsConstants.DEFAULT_WORKSPACE_NAME))) {
//                throw new NutsInvalidWorkspaceException(NutsConstants.DEFAULT_WORKSPACE_NAME, NutsConstants.DEFAULT_WORKSPACE_NAME + " is not a valid workspace name");
//            }
            LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
            bootConfig0 = NutsUtils.loadNutsBootConfig(home, workspaceLocation);
            if (bootConfig0.getBootAPIVersion() != null && bootConfig0.getBootRuntime() != null && bootConfig0.getBootRuntimeDependencies() != null) {
                //Ok
            } else {
                bootConfig0 = loadNutsBootConfig(true, workspaceLocation, wbootId);

            }
            if (!bootIdActualVersion.equals(bootConfig0.getBootAPIVersion())) {
                log.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{bootConfig0.getBootAPIVersion(), bootIdActualVersion});
                actualBootConfig = loadNutsBootConfig(true, workspaceLocation, wbootId);
            } else {
                actualBootConfig = bootConfig0;
            }

            if (actualBootConfig == null) {
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load ClassPath");
            }
//            if (actualBootConfig.getBootAPIVersion() == null) {
//                actualBootConfig.setBootAPIVersion(wbootId.getVersion());
//            }

//            File repoFolder = createFile(home, NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
            File workspaceBootCacheFolder = getCacheFolder(workspaceLocation);
            BootNutsId bootRuntime = BootNutsId.parse(actualBootConfig.getBootRuntime());
            String[] repositories = NutsUtils.splitUrlStrings(actualBootConfig.getBootRepositories()).toArray(new String[0]);
            File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootCacheFolder, true);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + bootRuntime);
            }

            allExtensionFiles.put(actualBootConfig.getBootRuntime(), f);
            for (String idStr : NutsUtils.split(actualBootConfig.getBootRuntimeDependencies(), "\n\t ;,")) {
                BootNutsId id = BootNutsId.parse(idStr);
                f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootCacheFolder, false);
                if (f == null) {
                    f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootCacheFolder, false);
                    throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + id);
                }
                allExtensionFiles.put(id.toString(), f);
            }
            URL[] bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
            log.log(Level.CONFIG, "Loading Nuts ClassWorld from {0} jars : {1}", new Object[]{bootClassWorldURLs.length, Arrays.asList(bootClassWorldURLs)});
            ClassLoader workspaceClassLoader = bootClassWorldURLs.length == 0 ? getContextClassLoader() : new URLClassLoader(bootClassWorldURLs, getContextClassLoader());
            ServiceLoader<NutsWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsWorkspaceFactory.class, workspaceClassLoader);

            NutsWorkspace nutsWorkspace = null;
            NutsWorkspaceFactory factoryInstance = null;
            for (NutsWorkspaceFactory a : serviceLoader) {
                factoryInstance = a;
                nutsWorkspace = a.createSupported(NutsWorkspace.class, this);
                break;
            }
            if (nutsWorkspace == null) {
                //should never happen
                System.err.printf("Unable to load Workspace Component from ClassPath : \n");
                for (URL url : bootClassWorldURLs) {
                    System.err.printf("\t%s\n", url);
                }
                log.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs));
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load Workspace Component from ClassPath : " + Arrays.asList(bootClassWorldURLs));
            }
            if (options.isNoColors()) {
                nutsWorkspace.getUserProperties().put("no-colors", "true");
            }
            if (((NutsWorkspaceImpl) nutsWorkspace).initializeWorkspace(factoryInstance, actualBootConfig, bootConfig0, workspaceLocation,
                    bootClassWorldURLs,
                    workspaceClassLoader, options.copy().setIgnoreIfFound(true))) {
                log.log(Level.FINE, "Workspace created {0}", workspaceLocation);
            }

            if (options.getLogin() != null && options.getLogin().trim().length() > 0) {
                String password = options.getPassword();
                if (NutsUtils.isEmpty(password)) {
                    password = nutsWorkspace.getTerminal().readPassword("Password : ");
                }
                nutsWorkspace.getSecurityManager().login(options.getLogin(), password);
            }
            long endTime = System.currentTimeMillis();
            log.log(Level.FINE, "Nuts Workspace loaded in {0}", NutsUtils.formatPeriodMilli(endTime - startTime));
            if (options.isPerf()) {
                nutsWorkspace.getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]\n",
                        NutsUtils.formatPeriodMilli(endTime - startTime)
                );
            }
            return nutsWorkspace;
        } catch (Exception ex) {
            if (bootConfig0 == null) {
                bootConfig0 = new NutsBootConfig()
                        .setBootAPIVersion(wbootId.getVersion())
                        .setBootRuntime(runtimeId);
            }
            if (actualBootConfig == null) {
                actualBootConfig = new NutsBootConfig()
                        .setBootAPIVersion(NutsConstants.NUTS_ID_BOOT_API + "#" + bootIdActualVersion)
                        .setBootRuntime(runtimeId);
            }
            NutsUtils.showError(
                    actualBootConfig
                    , bootConfig0
                    , home
                    , options.getWorkspace(),
                    ex.toString()
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            throw new NutsIllegalArgumentException("Unable to locate valid nuts-core components", ex);
        }
    }

    private File getCacheFolder(String workspaceLocation) {
        return createFile(workspaceLocation, "cache");
    }


    private URL[] resolveClassWorldURLs(Collection<File> list) {
        List<URL> urls = new ArrayList<>();
        for (File file : list) {
            if (file != null) {
                if (isLoadedClassPath(file)) {
                    log.log(Level.WARNING, "File will not be loaded (already in classloader) : {0}", file);
                } else {
                    try {
                        urls.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    private String[] resolveBootConfigRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
//        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private String[] resolveBootClassPathRepositories(String workspaceLocation, String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
//        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        initial.add(NutsConstants.URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL);
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add(workspaceLocation + "/repositories/local/components");
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private NutsBootConfig loadNutsBootConfig(boolean first, String workspaceLocation, BootNutsId wbootId) {
//        NutsBootConfig json = NutsUtils.loadNutsBootConfig(getHomeLocation(), workspaceLocation);
//        if (json.getBootRuntime() != null && json.getBootRuntimeDependencies() != null) {
//            return json;
//        }
        File cacheFolder = getCacheFolder(workspaceLocation);

        String bootAPIPropertiesPath = '/' + getPathFile(wbootId, wbootId.getArtifactId() + ".properties");
        String runtimeId = null;
        String repositories = null;
        List<String> resolvedBootRepositories = new ArrayList<>();
        resolvedBootRepositories.add(cacheFolder.getPath());
        resolvedBootRepositories.addAll(Arrays.asList(NutsUtils.splitAndRemoveDuplicates(this.runtimeSourceURL, NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT)));
        for (String repo : resolvedBootRepositories) {
            URL urlString = buildURL(repo, bootAPIPropertiesPath);
            if (urlString != null) {
                Properties wruntimeProperties = NutsUtils.loadURLProperties(urlString, new File(cacheFolder, bootAPIPropertiesPath.replace('/', File.separatorChar)));
                if (!wruntimeProperties.isEmpty()) {
                    String wruntimeId = wruntimeProperties.getProperty("runtimeId");
                    String wrepositories = wruntimeProperties.getProperty("repositories");
                    if (!NutsUtils.isEmpty(wruntimeId) && !NutsUtils.isEmpty(wrepositories)) {
                        runtimeId = wruntimeId;
                        repositories = wrepositories;
                        if (log.isLoggable(Level.CONFIG)) {
                            log.log(Level.CONFIG, "[SUCCESS] Loaded  boot props from  " + urlString + " : runtimeId=" + runtimeId + " ; repositories=" + repositories);
                        }
                        break;
                        //no need to log, already done in NutsUtils. loadURLProperties
                    }
                }
            } else {
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, "[ERROR  ] Loading props file from " + urlString);
                }
            }
        }


        if (NutsUtils.isEmpty(runtimeId)) {
            runtimeId = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + wbootId.getVersion() + ".0";
            log.log(Level.CONFIG, "[ERROR  ] Failed to load boot props file from boot repositories. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        if (NutsUtils.isEmpty(repositories)) {
            repositories = "";
        }
        BootNutsId _runtimeId = BootNutsId.parse(runtimeId);
        List<NutsBootConfig> all = new ArrayList<>();

        resolvedBootRepositories.clear();
        resolvedBootRepositories.add(cacheFolder.getPath());
        resolvedBootRepositories.addAll(Arrays.asList(resolveBootConfigRepositories(/*repositories*/)));

        String bootRuntimePropertiesPath = getPathFile(_runtimeId, "nuts.properties");
        for (String u : resolvedBootRepositories) {
            NutsBootConfig cp = null;
            URL urlString = buildURL(u, bootRuntimePropertiesPath);
            if (urlString != null) {
                try {
                    Properties p = NutsUtils.loadURLProperties(urlString, new File(cacheFolder, bootAPIPropertiesPath.replace('/', File.separatorChar)));
                    if (p != null && !p.isEmpty()) {
                        cp = NutsUtils.createNutsBootConfig(p);
                        cp.setBootAPIVersion(wbootId.getVersion());
                        //NutsUtils.storeProperties(p,new File(cacheFolder,bootPropertiesPath.replace('/',File.separatorChar)));
                    }
                } catch (Exception ex) {
                    log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", new Object[]{urlString});
                }
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", (u + "/" + bootRuntimePropertiesPath));
            }
            if (cp != null) {
                log.log(Level.CONFIG, "[SUCCESS] Loaded runtime id {0} from runtime props file {1}", new Object[]{cp.getBootRuntime(), u});
                all.add(cp);
                if (first) {
                    break;
                }
            }
        }
        if (all.isEmpty()) {
            String runtimeVersion = null;
            String runtimeId0 = null;
            if (this.runtimeId != null) {
                runtimeId0 = this.runtimeId;
                runtimeVersion = BootNutsId.parse(this.runtimeId).version;
            } else {
                String bootAPI = wbootId.toString();
                if (bootAPI == null) {
                    bootAPI = NutsConstants.NUTS_ID_BOOT_API + "#" + Nuts.getActualVersion();
                }
                runtimeVersion = BootNutsId.parse(bootAPI).version + ".0";
                runtimeId0 = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + runtimeVersion;
            }
            log.log(Level.CONFIG, "Loading Default Runtime ClassPath {0}", runtimeVersion);
            String[] jarRepositories = {
                    cacheFolder.getPath(),
                    NutsConstants.URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT
            };
            for (String repoURL : jarRepositories) {
                BootNutsId bootRuntimeNutsId = BootNutsId.parse(runtimeId0);
                String pomPath = getPathFile(bootRuntimeNutsId, getFileName(bootRuntimeNutsId, "pom"));
                URL urlString = buildURL(repoURL, pomPath);
                if (urlString != null) {
                    String[] bootNutsIds = NutsUtils.parseDependenciesFromMaven(urlString, new File(cacheFolder, pomPath.replace('/', File.separatorChar)));
                    if (bootNutsIds != null) {
                        NutsBootConfig bcc = new NutsBootConfig()
                                .setBootAPIVersion(wbootId.getVersion())
                                .setBootRuntime(runtimeId0)
                                .setBootRuntimeDependencies(NutsUtils.join(";", bootNutsIds))
                                .setBootRepositories(NutsUtils.join(";", jarRepositories));
                        all.add(bcc);

                        //cache boot-api     properties
                        Properties p = new Properties();
                        p.setProperty("project.id", wbootId.getGroupId() + ":" + wbootId.getArtifactId());
                        p.setProperty("project.version", wbootId.getVersion());
                        p.setProperty("project.name", wbootId.getGroupId() + ":" + wbootId.getArtifactId());
                        p.setProperty("runtimeId", bootRuntimeNutsId.toString());
                        p.setProperty("repositories", bcc.getBootRepositories());
                        File cacheFile = new File(cacheFolder, bootAPIPropertiesPath.replace('/', File.separatorChar));
                        NutsUtils.storeProperties(p, cacheFile);
                        log.log(Level.CONFIG, "[CACHED ] Caching properties file {0}", new Object[]{cacheFile.getPath()});

                        //cache boot-runtime properties
                        p = new Properties();
                        p.setProperty("project.id", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
                        p.setProperty("project.version", bootRuntimeNutsId.getVersion());
                        p.setProperty("project.name", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
//                        p.setProperty("runtimeId",bootRuntimeNutsId.toString());
//                        p.setProperty("repositories",bcc.getBootRepositories());
                        p.setProperty("project.dependencies.compile", bcc.getBootRuntimeDependencies());
                        cacheFile = new File(cacheFolder, bootRuntimePropertiesPath.replace('/', File.separatorChar));
                        NutsUtils.storeProperties(p, cacheFile);
                        log.log(Level.CONFIG, "[CACHED ] Caching properties file {0}", new Object[]{cacheFile.getPath()});
                        break;
                    }
                } else {
                    log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime jar dependencies from  {0}", urlString);
                }
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() > 1) {

            Collections.sort(all, new NutsWorkspaceClassPathComparator());
        }
        NutsBootConfig cp = all.get(all.size() - 1);
        List<String> repos2 = new ArrayList<>();
        repos2.add(cp.getBootRepositories());
        repos2.addAll(resolvedBootRepositories);
        String repositoriesToStore = NutsUtils.join(";",
                resolveBootClassPathRepositories(
                        workspaceLocation,
                        repos2.toArray(new String[0])
                )
        );

        cp.setBootRepositories(repositoriesToStore);
        cp.setBootJavaCommand(options.getBootJavaCommand());
        cp.setBootJavaOptions(options.getBootJavaOptions());
        return cp;
    }

//    protected String expandPath0(String path) {
//        if (path.startsWith(NutsConstants.DEFAULT_NUTS_HOME + "/")) {
//            path = home + "/" + path.substring(NutsConstants.DEFAULT_NUTS_HOME.length() + 1);
//        }
//        return path;
//    }

    protected String expandPath(String path) {
        if (path.startsWith(NutsConstants.DEFAULT_NUTS_HOME + "/")) {
            path = this.home + "/" + path.substring(NutsConstants.DEFAULT_NUTS_HOME.length() + 1);
        }
        if (path.startsWith("~/")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    protected URL expandURL(String url) throws MalformedURLException {
        url = expandPath(url);
        if (NutsUtils.isRemoteURL(url)) {
            return new URL(url);
        }
        return new File(url).toURI().toURL();
    }

    private File getBootFile(BootNutsId vid, String fileName, String[] repositories, File cacheFolder, boolean useCache) {
        String path = getPathFile(vid, fileName);
        for (String repository : repositories) {
            File file = getBootFile(path, repository, cacheFolder, useCache);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private URL buildURL(String base, String path) {
        try {
            if (NutsUtils.isRemoteURL(base)) {
                if (!base.endsWith("/") && !path.endsWith("/")) {
                    base += "/";
                }
                return expandURL(base + path);
            } else {
                path = path.replace('/', File.separatorChar);
                if (!base.endsWith(File.separator) && !path.endsWith(File.separator)) {
                    base += File.separator;
                }
                return expandURL(base + path);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static File createFile(String parent, String child) {
        String userHome = System.getProperty("user.home");
        if (child.startsWith("~/")) {
            child = new File(userHome, child.substring(2)).getPath();
        }
        if ((child.startsWith("/") || child.startsWith("\\") || new File(child).isAbsolute())) {
            return new File(child);
        }
        if (parent != null) {
            if (parent.startsWith("~/")) {
                parent = new File(userHome, parent.substring(2)).getPath();
            }
        } else {
            parent = ".";
        }
        return new File(parent, child);
    }

    private String getFileName(BootNutsId id, String ext) {
        return id.artifactId + "-" + id.version + "." + ext;
    }

    private String getPathFile(BootNutsId id, String name) {
        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + name;
    }

//    private String getPath(BootNutsId id, String ext) {
//        String ff = id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/nuts." + ext;
//        System.out.println(ff);
//        return ff;
//    }
//    private String getPath(BootNutsId id, String ext) {
//        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + getFileName(id, ext);
//    }

//    private File getBootFile(BootNutsId id, String fileName, String repository, File cacheFolder, boolean useCache) {
//        String path = getPathFile(id, fileName);
//        return getBootFile(path, repository, cacheFolder, useCache);
//    }

    private File getBootFile(String path, String repository, File cacheFolder, boolean useCache) {
        repository = repository.trim();
        repository = expandPath(repository);
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile()) {
                return f;
            }
            if (cacheFolder.getPath().equals(repository)) {
                return null;
            }
        }
        if (NutsUtils.isRemoteURL(repository)) {
            if (cacheFolder == null) {
                return null;
            }
            File ok = null;
            File to = new File(cacheFolder, path);
            String urlPath = repository;
            if (!urlPath.endsWith("/")) {
                urlPath += "/";
            }
            urlPath += path;
            try {
                InputStream from = new URL(urlPath).openStream();
                log.log(Level.CONFIG, "[SUCCESS] Loading  {0}", new Object[]{urlPath});
                NutsUtils.copy(from, to, true, true);
                ok = to;
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Loading  {0}", new Object[]{urlPath});
                //not found
            }
            return ok;
        } else if (repository.startsWith("file:")) {
            repository = NutsUtils.urlToFile(repository).getPath();
        }
        File repoFolder = createFile(home, repository);
        File ff = resolveFileForRepository(path, repoFolder, repository);
        if (ff != null) {
            if (cacheFolder != null && Boolean.getBoolean("nuts.cache.cache-local-files")) {
                File to = new File(cacheFolder, path);
                String toc = null;
                try {
                    toc = to.getCanonicalPath();
                } catch (IOException e) {
                    toc = to.getAbsolutePath();
                }
                String ffc = null;
                try {
                    ffc = ff.getCanonicalPath();
                } catch (IOException e) {
                    ffc = ff.getAbsolutePath();
                }
                if (ffc.equals(toc)) {
                    return ff;
                }
                try {
                    log.log(Level.CONFIG, "[SUCCESS] Loading  {0}", new Object[]{ff});
                    NutsUtils.copy(ff, to, true);
                    return to;
                } catch (IOException ex) {
                    log.log(Level.CONFIG, "[ERROR  ] Loading  {0}", new Object[]{ff});
                    //not found
                }
                return ff;

            }
            return ff;
        }
        return null;
    }

    private File resolveFileForRepository(String path, File repoFolder, String repositoryString) {
        if (repoFolder == null) {
            log.log(Level.CONFIG, "repository url is not a valid folder : {0} . Unable to locate path {1}",
                    new Object[]{repositoryString, path.replace('/', File.separatorChar)});
            return null;
        }
        File file = new File(repoFolder, path.replace('/', File.separatorChar));
        if (repoFolder.isDirectory()) {
            if (file.isFile()) {
                log.log(Level.CONFIG, "[SUCCESS] Locating {0}", new Object[]{file});
                return file;
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Locating {0}", new Object[]{file});
            }
        } else {
            log.log(Level.CONFIG, "[ERROR  ] Locating {0} . Repository is not a valid folder : {1}", new Object[]{file, repoFolder});
        }
        return null;
    }

    private boolean isLoadedClassPath(File file) {
        try {
            if (file != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                if (isInfiniteLoopThread(NutsBootWorkspace.class.getName(), "isLoadedClassPath")) {
                                    return false;
                                }
                                ClassLoader contextClassLoader = getContextClassLoader();
                                if (contextClassLoader == null) {
                                    return false;
                                }
                                Class<?> aClass = contextClassLoader.loadClass(clz);
                                log.log(Level.FINEST, "Class {0} Loaded successfully from {1}", new Object[]{aClass, file});
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

    private boolean isInfiniteLoopThread(String className, String methodName) {
        Thread thread = Thread.currentThread();
        StackTraceElement[] elements = thread.getStackTrace();

        if (elements == null || elements.length == 0) {
            return false;
        }

        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[elements.length - (i + 1)];
            if (className.equals(element.getClassName())) {
                if (methodName.equals(element.getMethodName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String resolveWorkspacePath(String workspace, String defaultName) {
        if (NutsUtils.isEmpty(workspace)) {
            File file = NutsUtils.resolvePath(home + "/" + defaultName, new File(home), home);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = NutsUtils.resolvePath(workspace, new File(home), home);
            workspace = file == null ? null : file.getPath();
        }
        return workspace;
    }

    private NutsClassLoaderProvider getContextClassLoaderProvider() {
        return contextClassLoaderProvider;
    }

    protected ClassLoader getContextClassLoader() {
        NutsClassLoaderProvider currentContextClassLoaderProvider = getContextClassLoaderProvider();
        if (currentContextClassLoaderProvider == null) {
            return null;
        }
        return currentContextClassLoaderProvider.getContextClassLoader();
    }

}
