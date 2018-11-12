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
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-core.jar and its dependencies and for creating workspaces using
 * the method {@link #openWorkspace(NutsWorkspaceCreateOptions)}.
 * <p>
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsBootWorkspace implements NutsBootWorkspace {

    public static final Logger log = Logger.getLogger(DefaultNutsBootWorkspace.class.getName());
    private final String rootLocation;
    private final String runtimeSourceURL;
    private BootNutsId runtimeId;
    private final NutsClassLoaderProvider contextClassLoaderProvider;
    private NutsBootOptions bootOptions;

    public DefaultNutsBootWorkspace() {
        this(null);
    }

    public DefaultNutsBootWorkspace(NutsBootOptions bootOptions) {
        if (bootOptions == null) {
            bootOptions = new NutsBootOptions();
        }
        this.bootOptions=bootOptions;
        NutsLogUtils.prepare(bootOptions.getLogLevel(), bootOptions.getLogFolder(), bootOptions.getLogSize(), bootOptions.getLogCount());
        log.log(Level.CONFIG, "Create boot workspace with options {0}", new Object[]{bootOptions});
        this.rootLocation = NutsStringUtils.isEmpty(bootOptions.getHome()) ? NutsConstants.DEFAULT_NUTS_HOME : bootOptions.getHome();
        this.runtimeSourceURL = bootOptions.getRuntimeSourceURL();
        this.runtimeId = NutsStringUtils.isEmpty(bootOptions.getRuntimeId()) ? null : BootNutsId.parse(bootOptions.getRuntimeId());
        this.contextClassLoaderProvider = bootOptions.getClassLoaderProvider() == null ? DefaultNutsClassLoaderProvider.INSTANCE : bootOptions.getClassLoaderProvider();
    }

    @Override
    public NutsBootOptions getBootOptions() {
        return bootOptions.copy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNutsHomeLocation() {
        return rootLocation;
    }

    @Override
    public String getBootstrapLocation() {
        return getNutsHomeLocation() + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBootId() {
        return NutsConstants.NUTS_ID_BOOT + "#" + NutsIOUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"))
                .getProperty("project.version", "0.0.0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options) {
        try {

            long startTime = options.getCreationTime();
            if (options == null) {
                options = new NutsWorkspaceCreateOptions()
                        .setCreateIfNotFound(true)
                        .setSaveIfCreated(true);
            }
            if(startTime==0){
                options.setCreationTime(startTime=System.currentTimeMillis());
            }
            String workspaceLocation=options.getWorkspace();
            workspaceLocation = resolveWorkspacePath(workspaceLocation, NutsConstants.DEFAULT_WORKSPACE_NAME);
            log.log(Level.CONFIG, "Open Workspace {0} with options {1}", new Object[]{workspaceLocation, options});
            if (workspaceLocation.equals(NutsConstants.BOOTSTRAP_REPOSITORY_NAME) || new File(workspaceLocation).equals(new File(rootLocation, NutsConstants.DEFAULT_WORKSPACE_NAME))) {
                throw new NutsInvalidWorkspaceException(NutsConstants.BOOTSTRAP_REPOSITORY_NAME, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + " is not a valid workspace name");
            }
            LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
            NutsWorkspaceClassPath workspaceClassPath = loadWorkspaceClassPath(true);
            if (workspaceClassPath == null) {
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load ClassPath");
            }

            File repoFolder = createFile(rootLocation, NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
            File f = getBootFile(workspaceClassPath.getId(), getFileName(workspaceClassPath.getId(), "jar"), workspaceClassPath.getRepositoriesArray(), repoFolder, true);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + workspaceClassPath.getId());
            }

            allExtensionFiles.put(workspaceClassPath.getId().toString(), f);
            for (BootNutsId id : workspaceClassPath.getDependenciesArray()) {
                f = getBootFile(id, getFileName(id, "jar"), workspaceClassPath.getRepositoriesArray(), repoFolder, false);
                if (f == null) {
                    throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + id);
                }
                allExtensionFiles.put(id.toString(), f);
            }
            URL[] bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
            log.log(Level.CONFIG, "Loading Nuts ClassWorld from {0}", Arrays.asList(bootClassWorldURLs));
            ClassLoader workspaceClassLoader = bootClassWorldURLs.length == 0 ? getContextClassLoader() : new URLClassLoader(bootClassWorldURLs, getContextClassLoader());
            ServiceLoader<NutsWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsWorkspaceFactory.class, workspaceClassLoader);

            NutsWorkspace nutsWorkspace = null;
            NutsWorkspaceImpl nutsWorkspaceImpl = null;
            NutsWorkspaceFactory factoryInstance = null;
            for (NutsWorkspaceFactory a : serviceLoader) {
                factoryInstance = a;
                nutsWorkspace = a.createSupported(NutsWorkspace.class, this);
                nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
                break;
            }
            if (nutsWorkspace == null || nutsWorkspaceImpl == null) {
                //should never happen
                System.err.printf("Unable to load Workspace Component from ClassPath : \n");
                for (URL url : bootClassWorldURLs) {
                    System.err.printf("\t%s\n", url);
                }
                log.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(bootClassWorldURLs));
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load Workspace Component from ClassPath : " + Arrays.asList(bootClassWorldURLs));
            }
            if (nutsWorkspaceImpl.initializeWorkspace(this, factoryInstance, getBootId(), workspaceClassPath.getId().toString(), workspaceLocation,
                    bootClassWorldURLs,
                    workspaceClassLoader, options.copy().setIgnoreIfFound(true))) {
                log.log(Level.FINE, "Workspace created {0}", workspaceLocation);
            }

            if (options.getLogin() != null && options.getLogin().trim().length() > 0) {
                String password = options.getPassword();
                if (NutsStringUtils.isEmpty(password)) {
                    password = nutsWorkspace.createSession().getTerminal().readPassword("Password : ");
                }
                nutsWorkspace.getSecurityManager().login(options.getLogin(), password);
            }
            long endTime = System.currentTimeMillis();
            log.log(Level.FINE, "Nuts Workspace loaded in {0}ms", (endTime - startTime));
            if (bootOptions.isPerf()) {
                nutsWorkspace.createSession().getTerminal().getFormattedOut().printf("**Nuts** Workspace loaded in [[%s]]ms\n",
                        (endTime - startTime)
                );
            }
            return nutsWorkspace;
        }catch (Exception ex){
            System.err.printf("workspace-location   : %s\n", (options.getWorkspace() == null ? "" : options.getWorkspace()));
            System.err.printf("nuts-boot            : %s\n", getBootId());
            System.err.printf("nuts-runtime         : %s\n", getRuntimeId());
            System.err.printf("nuts-home            : %s\n", getNutsHomeLocation());
            System.err.printf("java-version         : %s\n", System.getProperty("java.version"));
            System.err.printf("java-executable      : %s\n", System.getProperty("java.home") + "/bin/java");
            System.err.printf("java-class-path      : %s\n", System.getProperty("java.class.path"));
            System.err.printf("java-library-path    : %s\n", System.getProperty("java.library.path"));
            System.err.printf("Unable to locate nuts-core components.\n");
            System.err.printf("You need internet connexion to initialize nuts configuration. Once components are downloaded, you may work offline...\n");
            System.err.printf("Exiting nuts, Bye!\n");
            if(ex instanceof NutsException){
                throw (NutsException) ex;
            }
            throw new NutsIllegalArgumentException("Unable to locate nuts-core components", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRuntimeId() {
        return runtimeId == null ? null : runtimeId.toString();
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
        return urls.toArray(new URL[urls.size()]);
    }

    private String[] resolveBootConfigRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
        initial.add(rootLocation + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE);
        return NutsStringUtils.splitAndRemoveDuplicates(initial);
    }

    private String[] resolveBootClassPathRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
        initial.add(rootLocation + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        initial.add(NutsConstants.URL_COMPONENTS_LOCAL);
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE);
        initial.add(NutsConstants.URL_COMPONENTS_REMOTE);
        return NutsStringUtils.splitAndRemoveDuplicates(initial);
    }

    private File getBootFileLocation(BootNutsId id, String name) {
        return new File(createFile(rootLocation, NutsConstants.BOOTSTRAP_REPOSITORY_NAME), getPathFile(id, name));
    }

    private NutsWorkspaceClassPath loadWorkspaceClassPath(boolean first) {
        BootNutsId wbootId = BootNutsId.parse(getBootId());
        File bootPropertiesFile = getBootFileLocation(wbootId, wbootId.getArtifactId()+".properties");
        String bootPropertiesPath = '/' + getPathFile(wbootId, wbootId.getArtifactId()+".properties");
        String[] resolvedBootRepositories = null;
        String repositories = null;
        BootNutsId _runtimeId = runtimeId;
        if (_runtimeId == null || repositories == null) {
            String runtimeId = null;
            boolean storeRuntimeFile = true;
            Properties bootProperties = null;
            if (bootPropertiesFile.exists()) {
                bootProperties = NutsIOUtils.loadFileProperties(bootPropertiesFile);
                runtimeId = bootProperties.getProperty("runtimeId");
                repositories = bootProperties.getProperty("repositories");
                if (!NutsStringUtils.isEmpty(runtimeId) && !NutsStringUtils.isEmpty(repositories)) {
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.CONFIG, "Loaded boot from " + bootPropertiesFile.getPath() + " : runtimeId=" + runtimeId + " ; repositories=" + repositories);
                    }
                    storeRuntimeFile = false;
                } else {
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.CONFIG, "Failed to load boot props file from " + bootPropertiesFile.getPath() + " . Corrupted file. runtimeId=" + runtimeId + " ; repositories=" + repositories);
                    }
                }
            } else {
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, "Failed to load boot props file from " + bootPropertiesFile.getPath() + " . File does not exist.");
                }
            }
            if (NutsStringUtils.isEmpty(runtimeId) || NutsStringUtils.isEmpty(repositories)) {
                resolvedBootRepositories = resolveBootConfigRepositories(repositories);
                for (String repo : resolvedBootRepositories) {
                    URL urlString = buildURL(repo, bootPropertiesPath);
                    if (urlString != null) {
                        Properties wruntimeProperties = NutsIOUtils.loadURLProperties(urlString);
                        if (!wruntimeProperties.isEmpty()) {
                            String wruntimeId = wruntimeProperties.getProperty("runtimeId");
                            String wrepositories = wruntimeProperties.getProperty("repositories");
                            if (!NutsStringUtils.isEmpty(wruntimeId) && !NutsStringUtils.isEmpty(wrepositories)) {
                                runtimeId = wruntimeId;
                                repositories = wrepositories;
                                if (log.isLoggable(Level.CONFIG)) {
                                    log.log(Level.CONFIG, "Loaded boot props from " + urlString + " : runtimeId=" + runtimeId + " ; repositories=" + repositories);
                                }
                                break;
                            }
                        } else {
                            if (log.isLoggable(Level.CONFIG)) {
                                log.log(Level.CONFIG, "Failed to load boot props file from " + urlString);
                            }
                        }
                    } else {
                        if (log.isLoggable(Level.CONFIG)) {
                            log.log(Level.CONFIG, "Failed to load boot props file from " + urlString);
                        }
                    }
                }
            }

            if (_runtimeId == null && NutsStringUtils.isEmpty(runtimeId)) {
//                storeRuntimeFile = false;
                runtimeId = NutsConstants.NUTS_ID_RUNTIME + "#" + wbootId.getVersion() + ".0";
                log.log(Level.CONFIG, "Failed to load boot props file from boot repositories. Considering defaults : {1}", new Object[]{bootPropertiesPath, runtimeId});
//                if (resolvedBootRepositories != null) {
//                    for (String repo : resolvedBootRepositories) {
//                        URL uu = buildURL(repo, bootPropertiesPath);
//                        log.log(Level.CONFIG, "Inaccessible repository boot path: {0}", uu == null ? (repo + "/" + bootPropertiesPath) : uu.toString());
//                    }
//                }
            }
            if (NutsStringUtils.isEmpty(repositories)) {
                repositories = "";
            }
            if (_runtimeId == null && storeRuntimeFile) {
                if (NutsStringUtils.isEmpty(repositories)) {
                    repositories = NutsConstants.URL_COMPONENTS_REMOTE;
                }
                bootProperties = new Properties();
                bootProperties.setProperty("runtimeId", runtimeId);
                bootProperties.setProperty("repositories", repositories);
                bootPropertiesFile.getParentFile().mkdirs();
                NutsIOUtils.storeProperties(bootProperties, bootPropertiesFile);
                log.log(Level.CONFIG, "Store boot file {0}", new Object[]{bootPropertiesFile});
            }
            if (_runtimeId == null) {
                _runtimeId = BootNutsId.parse(runtimeId);
            }
        }

        File localRuntimeConfigFile = getBootFile(_runtimeId, "nuts.properties");
        List<NutsWorkspaceClassPath> all = new ArrayList<>();
        if (localRuntimeConfigFile != null && localRuntimeConfigFile.exists()) {
            NutsWorkspaceClassPath c = null;
            try {
                c = new NutsWorkspaceClassPath(NutsIOUtils.loadFileProperties(localRuntimeConfigFile));
            } catch (Exception e) {
                //
            }
            if (c != null) {
                all.add(c);
                if (first) {
                    return c;
                }
            }
        }
        resolvedBootRepositories = resolveBootConfigRepositories(repositories);
        for (String u : resolvedBootRepositories) {
            NutsWorkspaceClassPath cp = null;
            URL urlString = buildURL(u, getPathFile(_runtimeId, "nuts.properties"));
            if (urlString != null) {
                try {
                    Properties p= NutsIOUtils.loadURLProperties(urlString);
                    if(p!=null && !p.isEmpty()) {
                        cp = new NutsWorkspaceClassPath(p);
                    }
                } catch (Exception ex) {
                    log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", new Object[]{urlString});
                    //ignore
                }
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", (u + "/" + getPathFile(_runtimeId, "nuts.properties")));
            }
            if (cp != null) {
                log.log(Level.CONFIG, "[SUCCESS] Loaded runtime id {0} from runtime props file {1}", new Object[]{cp.getId(), u});
                all.add(cp);
                if (first) {
                    break;
                }
            }
        }
        if (all.isEmpty()) {
            String runtimeVersion = BootNutsId.parse(getBootId()).version + ".0";
            all.add(new NutsWorkspaceClassPath(
                    NutsConstants.NUTS_ID_RUNTIME,
                    runtimeVersion,
                    getBootId() + ";"
                            + "net.vpc.common:vpc-common-utils#1.22;"
                            + "net.vpc.common:vpc-common-io#1.3.6;"
                            + "net.vpc.common:vpc-common-strings#1.2.14;"
                            + "org.ow2.asm:asm#5.2;"
                            + "com.google.code.gson:gson#2.8.5"
                    ,
                    expandPath0(NutsConstants.URL_COMPONENTS_LOCAL) + ";" + NutsConstants.URL_COMPONENTS_REMOTE
            ));
            log.log(Level.CONFIG, "Loading Default Runtime ClassPath {0}", runtimeVersion);
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() > 1) {

            Collections.sort(all, new NutsWorkspaceClassPathComparator());
        }
        NutsWorkspaceClassPath cp = all.get(all.size() - 1);
        String repositoriesToStore = NutsStringUtils.join(";",
                resolveBootClassPathRepositories(
                        NutsStringUtils.splitAndRemoveDuplicates(Arrays.asList(cp.getRepositoriesString()), Arrays.asList(resolvedBootRepositories))
                )
        );

        cp = new NutsWorkspaceClassPath(
                cp.getId().groupId + ":" + cp.getId().artifactId, cp.getId().version,
                cp.getDependenciesString(),
                repositoriesToStore);

        File runtimePropLocation = getBootFileLocation(cp.getId(), "nuts.properties");
        if (!runtimePropLocation.exists() || runtimePropLocation.length() <= 0) {
            runtimePropLocation.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("project.id", cp.getId().getGroupId() + ":" + cp.getId().getArtifactId());
            p.setProperty("project.version", cp.getId().getVersion());
            StringBuilder dsb = new StringBuilder();
            for (BootNutsId id : cp.getDependenciesArray()) {
                if (dsb.length() > 0) {
                    dsb.append(";");
                }
                dsb.append(id.toString());
            }
            p.setProperty("project.dependencies.compile", dsb.toString());
            p.setProperty("project.repositories", cp.getRepositoriesString());
            log.log(Level.CONFIG, "Store runtime file {0}", new Object[]{runtimePropLocation});
            NutsIOUtils.storeProperties(p, runtimePropLocation);
        }
        return cp;
    }

    protected String expandPath0(String path) {
        if (path.startsWith(NutsConstants.DEFAULT_NUTS_HOME + "/")) {
            path = rootLocation + "/" + path.substring(NutsConstants.DEFAULT_NUTS_HOME.length() + 1);
        }
        return path;
    }

    protected String expandPath(String path) {
        if (path.startsWith(NutsConstants.DEFAULT_NUTS_HOME + "/")) {
            path = rootLocation + "/" + path.substring(NutsConstants.DEFAULT_NUTS_HOME.length() + 1);
        }
        if (path.startsWith("~/")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    protected URL expandURL(String url) throws MalformedURLException {
        url = expandPath(url);
        if (NutsIOUtils.isRemoteURL(url)) {
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
            if (NutsIOUtils.isRemoteURL(base)) {
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

    private File getBootFile(BootNutsId vid, String fileName) {
        return getBootFile(vid, fileName, NutsConstants.BOOTSTRAP_REPOSITORY_NAME, null, false);
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
        String ff = id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + name;
        return ff;
    }

    private String getPath(BootNutsId id, String ext) {
        String ff = id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/nuts." + ext;
        System.out.println(ff);
        return ff;
    }
//    private String getPath(BootNutsId id, String ext) {
//        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + getFileName(id, ext);
//    }

    private File getBootFile(BootNutsId id, String fileName, String repository, File cacheFolder, boolean useCache) {
        String path = getPathFile(id, fileName);
        return getBootFile(path, repository, cacheFolder, useCache);
    }

    private File getBootFile(String path, String repository, File cacheFolder, boolean useCache) {
        repository = repository.trim();
        if (useCache && cacheFolder != null) {
            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile()) {
                return f;
            }
        }
        if (repository.startsWith("file:")) {
            File repoFolder = NutsIOUtils.urlToFile(repository);
            File ff=resolveFileForRepository(path,repoFolder,repository);
            if(ff!=null){
                return ff;
            }
        } else if (repository.startsWith("~/")) {
            File ff=resolveFileForRepository(path,new File(System.getProperty("user.home"), repository.substring(2)),repository);
            if(ff!=null){
                return ff;
            }
        } else if (cacheFolder != null && NutsIOUtils.isRemoteURL(repository)) {
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
                NutsIOUtils.copy(from, to, true, true);
                ok = to;
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Loading  {0}", new Object[]{urlPath});
                //not found
            }
            return ok;
        } else {
            File repoFolder = createFile(rootLocation, repository);
            File ff=resolveFileForRepository(path,repoFolder,repository);
            if(ff!=null){
                return ff;
            }
        }
        return null;
    }

    private File resolveFileForRepository(String path,File repoFolder,String repositoryString) {
        if(repoFolder==null){
            log.log(Level.CONFIG, "repository url is not a valid folder : {0} . Unable to locate path {1}",
                    new Object[]{repositoryString, path.replace('/', File.separatorChar)});
            return null;
        }
        File file = new File(repoFolder, path.replace('/', File.separatorChar));
        if (repoFolder.isDirectory()) {
            if (file.isFile()) {
                log.log(Level.CONFIG, "[SUCCESS] Locating {0}", new Object[]{file});
                return file;
            }else{
                log.log(Level.CONFIG, "[ERROR  ] Locating {0}", new Object[]{file});
            }
        } else {
            log.log(Level.CONFIG, "[ERROR  ] Locating {0} . Repository is not a valid folder : {1}", new Object[]{file,repoFolder});
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
                                if (isInfiniteLoopThread(DefaultNutsBootWorkspace.class.getName(), "isLoadedClassPath")) {
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
        if (NutsStringUtils.isEmpty(workspace)) {
            File file = NutsIOUtils.resolvePath(rootLocation + "/" + defaultName, null, rootLocation);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = NutsIOUtils.resolvePath(workspace, null, rootLocation);
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
