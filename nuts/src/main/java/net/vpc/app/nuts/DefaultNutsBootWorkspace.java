/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
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
 * the method {@link #openWorkspace(String, NutsWorkspaceCreateOptions)}.
 *
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsBootWorkspace implements NutsBootWorkspace {

    public static final Logger log = Logger.getLogger(DefaultNutsBootWorkspace.class.getName());
    private final String rootLocation;
    private final String runtimeSourceURL;
    private BootNutsId runtimeId;
    private final NutsClassLoaderProvider contextClassLoaderProvider;

    public DefaultNutsBootWorkspace() {
        this(null);
    }

    public DefaultNutsBootWorkspace(NutsBootOptions bootOptions) {
        if (bootOptions == null) {
            bootOptions = new NutsBootOptions();
        }
        log.log(Level.CONFIG, "Create boot workspace with options {0}", new Object[]{bootOptions});
        this.rootLocation = StringUtils.isEmpty(bootOptions.getRoot()) ? NutsConstants.DEFAULT_WORKSPACE_ROOT : bootOptions.getRoot();
        this.runtimeSourceURL = bootOptions.getRuntimeSourceURL();
        this.runtimeId = StringUtils.isEmpty(bootOptions.getRuntimeId()) ? null : BootNutsId.parse(bootOptions.getRuntimeId());
        this.contextClassLoaderProvider = bootOptions.getClassLoaderProvider() == null ? DefaultNutsClassLoaderProvider.INSTANCE : bootOptions.getClassLoaderProvider();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRootLocation() {
        return rootLocation;
    }

    @Override
    public String getBootstrapLocation() {
        return getRootLocation() + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBootId() {
        return NutsConstants.NUTS_ID_BOOT + "#" + IOUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app/nuts/nuts.properties"))
                .getProperty("project.version", "0.0.0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NutsWorkspace openWorkspace(String workspaceLocation, NutsWorkspaceCreateOptions options) {
        if (options == null) {
            options = new NutsWorkspaceCreateOptions()
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(true);
        }
        log.log(Level.CONFIG, "Open Workspace {0} with options {1}", new Object[]{workspaceLocation, options});
        workspaceLocation = resolveWorkspacePath(workspaceLocation, NutsConstants.DEFAULT_WORKSPACE_NAME);
        if (workspaceLocation.equals(NutsConstants.BOOTSTRAP_REPOSITORY_NAME) || new File(workspaceLocation).equals(new File(rootLocation, NutsConstants.DEFAULT_WORKSPACE_NAME))) {
            throw new NutsInvalidWorkspaceException(NutsConstants.BOOTSTRAP_REPOSITORY_NAME, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + " is not a valid workspace name");
        }
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        NutsWorkspaceClassPath workspaceClassPath = loadWorkspaceClassPath(true);
        if (workspaceClassPath == null) {
            throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load ClassPath");
        }

        File repoFolder = createFile(rootLocation, NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        File f = getBootFile(workspaceClassPath.getId(), "jar", workspaceClassPath.getRepositoriesArray(), repoFolder, true);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + workspaceClassPath.getId());
        }

        allExtensionFiles.put(workspaceClassPath.getId().toString(), f);
        for (BootNutsId id : workspaceClassPath.getDependenciesArray()) {
            f = getBootFile(id, "jar", workspaceClassPath.getRepositoriesArray(), repoFolder, false);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        URL[] urls = resolveClassWorldURLs(allExtensionFiles.values());
        if (log != null) {
            log.log(Level.INFO, "Loading Nuts ClassWorld from {0}", Arrays.asList(urls));
        }
        ClassLoader workspaceClassLoader = urls.length == 0 ? getContextClassLoader() : new URLClassLoader(urls, getContextClassLoader());
        ServiceLoader<NutsWorkspaceObjectFactory> serviceLoader = ServiceLoader.load(NutsWorkspaceObjectFactory.class, workspaceClassLoader);

        NutsWorkspace nutsWorkspace = null;
        NutsWorkspaceImpl nutsWorkspaceImpl = null;
        NutsWorkspaceObjectFactory factoryInstance = null;
        for (NutsWorkspaceObjectFactory a : serviceLoader) {
            factoryInstance = a;
            nutsWorkspace = a.createSupported(NutsWorkspace.class, this);
            nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
            break;
        }
        if (nutsWorkspace == null || nutsWorkspaceImpl == null) {
            //should never happen
            System.err.printf("Unable to load Workspace Component from ClassPath : \n");
            for (URL url : urls) {
                System.err.printf("\t%s\n", url);
            }
            log.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(urls));
            throw new NutsInvalidWorkspaceException(workspaceLocation, "Unable to load Workspace Component from ClassPath : " + Arrays.asList(urls));
        }
        if (nutsWorkspaceImpl.initializeWorkspace(this, factoryInstance, getBootId(), workspaceClassPath.getId().toString(), workspaceLocation, workspaceClassLoader, options.copy().setIgnoreIfFound(true))) {
            log.log(Level.FINE, "Workspace created {0}", workspaceLocation);
        }
        return nutsWorkspace;
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
        return StringUtils.splitAndRemoveDuplicates(initial);
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
        return StringUtils.splitAndRemoveDuplicates(initial);
    }

    private File getBootFileLocation(BootNutsId id, String ext) {
        return new File(createFile(rootLocation, NutsConstants.BOOTSTRAP_REPOSITORY_NAME), getPath(id, ext));
    }

    private NutsWorkspaceClassPath loadWorkspaceClassPath(boolean first) {
        BootNutsId wbootId = BootNutsId.parse(getBootId());
        File bootPropertiesFile = getBootFileLocation(wbootId, "properties");
        String bootPropertiesPath = '/' + getPath(wbootId, "properties");
        String[] resolvedBootRepositories = null;
        String repositories = null;
        BootNutsId _runtimeId = runtimeId;
        if (_runtimeId == null || repositories == null) {
            String runtimeId = null;
            boolean storeRuntimeFile = true;
            Properties bootProperties = null;
            if (bootPropertiesFile.exists()) {
                bootProperties = IOUtils.loadFileProperties(bootPropertiesFile);
                runtimeId = bootProperties.getProperty("runtimeId");
                repositories = bootProperties.getProperty("repositories");
                if (!StringUtils.isEmpty(runtimeId) && !StringUtils.isEmpty(repositories)) {
                    log.log(Level.CONFIG, "Loaded boot from " + bootPropertiesFile.getPath() + " : runtimeId=" + runtimeId + " ; repositories=" + repositories);
                    storeRuntimeFile = false;
                } else {
                    log.log(Level.CONFIG, "Failed to load boot props file from " + bootPropertiesFile.getPath() + " . Corrupted file. runtimeId=" + runtimeId + " ; repositories=" + repositories);
                }
            } else {
                log.log(Level.CONFIG, "Failed to load boot props file from " + bootPropertiesFile.getPath() + " . File does not exist.");
            }
            if (StringUtils.isEmpty(runtimeId) || StringUtils.isEmpty(repositories)) {
                resolvedBootRepositories = resolveBootConfigRepositories(repositories);
                for (String repo : resolvedBootRepositories) {
                    URL urlString = buildURL(repo, bootPropertiesPath);
                    if (urlString != null) {
                        Properties wruntimeProperties = IOUtils.loadURLProperties(urlString);
                        if (!wruntimeProperties.isEmpty()) {
                            String wruntimeId = wruntimeProperties.getProperty("runtimeId");
                            String wrepositories = wruntimeProperties.getProperty("repositories");
                            if (!StringUtils.isEmpty(wruntimeId) && !StringUtils.isEmpty(wrepositories)) {
                                runtimeId = wruntimeId;
                                repositories = wrepositories;
                                log.log(Level.CONFIG, "Loaded boot props from " + urlString + " : runtimeId=" + runtimeId + " ; repositories=" + repositories);
                                break;
                            }
                        } else {
                            log.log(Level.CONFIG, "Failed to load boot props file from " + urlString);
                        }
                    } else {
                        log.log(Level.CONFIG, "Failed to load boot props file from " + urlString);
                    }
                }
            }

            if (_runtimeId == null && StringUtils.isEmpty(runtimeId)) {
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
            if (StringUtils.isEmpty(repositories)) {
                repositories = "";
            }
            if (_runtimeId == null && storeRuntimeFile) {
                if (StringUtils.isEmpty(repositories)) {
                    repositories = NutsConstants.URL_COMPONENTS_REMOTE;
                }
                bootProperties = new Properties();
                bootProperties.setProperty("runtimeId", runtimeId);
                bootProperties.setProperty("repositories", repositories);
                bootPropertiesFile.getParentFile().mkdirs();
                IOUtils.storeProperties(bootProperties, bootPropertiesFile);
                log.log(Level.CONFIG, "Store boot file {1}", new Object[]{bootPropertiesFile});
            }
            if (_runtimeId == null) {
                _runtimeId = BootNutsId.parse(runtimeId);
            }
        }

        File localRuntimeConfigFile = getBootFile(_runtimeId, "properties");
        List<NutsWorkspaceClassPath> all = new ArrayList<>();
        if (localRuntimeConfigFile != null && localRuntimeConfigFile.exists()) {
            NutsWorkspaceClassPath c = null;
            try {
                c = new NutsWorkspaceClassPath(IOUtils.loadFileProperties(localRuntimeConfigFile));
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
            URL urlString = buildURL(u, getPath(_runtimeId, "properties"));
            if (urlString != null) {
                try {
                    cp = new NutsWorkspaceClassPath(IOUtils.loadURLProperties(urlString));
                } catch (Exception ex) {
                    log.log(Level.CONFIG, "Failed to load runtime props file from  {0}", urlString);
                    //ignore
                }
            } else {
                log.log(Level.CONFIG, "Failed to load runtime props file from  {0}", (u + "/" + getPath(_runtimeId, "properties")));
            }
            if (cp != null) {
                log.log(Level.CONFIG, "Loaded runtime id {0} from runtime props file {1}", new Object[]{cp.getId(), u});
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
                    + "net.vpc.common:java-shell#0.5;"
                    + "net.vpc.common:vpc-common-utils#1.21;"
                    + "net.vpc.common:vpc-common-commandline#1.0;"
                    + "javax.servlet:javax.servlet-api#3.1.0;"
                    + "org.jline#jline#3.5.2;"
                    + "org.ow2.asm:asm#5.2;"
                    + "org.glassfish:javax.json#1.0.4",
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
        String repositoriesToStore = StringUtils.join(";",
                resolveBootClassPathRepositories(
                        StringUtils.splitAndRemoveDuplicates(Arrays.asList(cp.getRepositoriesString()), Arrays.asList(resolvedBootRepositories))
                )
        );

        cp = new NutsWorkspaceClassPath(
                cp.getId().groupId + ":" + cp.getId().artifactId, cp.getId().version,
                cp.getDependenciesString(),
                repositoriesToStore);

        File runtimePropLocation = getBootFileLocation(cp.getId(), "properties");
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
            IOUtils.storeProperties(p, runtimePropLocation);
        }
        return cp;
    }

    protected String expandPath0(String path) {
        if (path.startsWith("~/.nuts/")) {
            path = rootLocation + "/" + path.substring("~/.nuts/".length());
        }
        return path;
    }

    protected String expandPath(String path) {
        if (path.startsWith("~/.nuts/")) {
            path = rootLocation + "/" + path.substring("~/.nuts/".length());
        }
        if (path.startsWith("~/")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    protected URL expandURL(String url) throws MalformedURLException {
        url = expandPath(url);
        if (IOUtils.isRemoteURL(url)) {
            return new URL(url);
        }
        return new File(url).toURI().toURL();
    }

    private File getBootFile(BootNutsId vid, String ext, String[] repositories, File cacheFolder, boolean useCache) {
        for (String repository : repositories) {
            File file = getBootFile(vid, ext, repository, cacheFolder, useCache);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private URL buildURL(String base, String path) {
        try {
            if (IOUtils.isRemoteURL(base)) {
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

    private File getBootFile(BootNutsId vid, String ext) {
        return getBootFile(vid, ext, NutsConstants.BOOTSTRAP_REPOSITORY_NAME, null, false);
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

    private String getPath(BootNutsId id, String ext) {
        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + getFileName(id, ext);
    }

    private File getBootFile(BootNutsId id, String ext, String repository, File cacheFolder, boolean useCache) {
        repository = repository.trim();
        String path = getPath(id, ext);
        if (useCache && cacheFolder != null) {
            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile()) {
                return f;
            }
        }
        if (repository.startsWith("file:")) {
            File repoFolder = IOUtils.urlToFile(repository);
            if (repoFolder != null) {
                if (repoFolder.isDirectory()) {
                    File file = new File(repoFolder, path.replace('/', File.separatorChar));
                    if (file.isFile()) {
                        return file;
                    }
                } else {
                    log.log(Level.CONFIG, "repository is not a valid folder : {0} . Unable to locate path {1}",
                            new Object[]{repoFolder, path.replace('/', File.separatorChar)});
                }
            } else {
                log.log(Level.CONFIG, "repository url is not a valid folder : {0} . Unable to locate path {1}",
                        new Object[]{repoFolder, path.replace('/', File.separatorChar)});
            }
        } else if (repository.startsWith("~/")) {
            return getBootFile(id, ext, new File(System.getProperty("user.home"), repository.substring(2)).getPath(), cacheFolder, useCache);
        } else if (cacheFolder != null && IOUtils.isRemoteURL(repository)) {
            File ok = null;
            try {
                File to = new File(cacheFolder, path);
                String urlPath = repository;
                if (!urlPath.endsWith("/")) {
                    urlPath += "/";
                }
                urlPath += path;
                InputStream from = new URL(urlPath).openStream();
                log.log(Level.CONFIG, "Loading {0}.{1} from {2}", new Object[]{id.toString(), ext, urlPath});
                IOUtils.copy(from, to, true, true);
                ok = to;
            } catch (IOException ex) {
                //not found
            }
            return ok;
        } else {
            File repoFolder = createFile(rootLocation, repository);
            if (repoFolder.isDirectory()) {
                File file = new File(repoFolder, path.replace('/', File.separatorChar));
                if (file.isFile()) {
                    return file;
                }
            } else {
                log.log(Level.CONFIG, "repository is not a valid folder : {0} . Unable to locate path {1}", new Object[]{repoFolder, path.replace('/', File.separatorChar)});
            }
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
                                log.log(Level.FINEST, "Class {0} Loaded successufully from {1}", new Object[]{aClass, file});
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
        if (StringUtils.isEmpty(workspace)) {
            File file = IOUtils.resolvePath(rootLocation + "/" + defaultName, null, rootLocation);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = IOUtils.resolvePath(workspace, null, rootLocation);
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
