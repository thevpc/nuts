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
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsBootWorkspace implements NutsBootWorkspace {

    private static final String DEFAULT_REMOTE_BOOTSTRAP_REPOSITORY_URL = "https://raw.githubusercontent.com/thevpc/nuts/master/nuts-bootstrap";

    public static final Logger log = Logger.getLogger(DefaultNutsBootWorkspace.class.getName());
    private final String root;
    private final String runtimeSourceURL;
    private WorkspaceNutsId runtimeId;
    private final NutsClassLoaderProvider contextClassLoaderProvider;

    public DefaultNutsBootWorkspace() {
        this(null);
    }

    public DefaultNutsBootWorkspace(NutsBootOptions bootOptions) {
        if (bootOptions == null) {
            bootOptions = new NutsBootOptions();
        }
        this.root = StringUtils.isEmpty(bootOptions.getRoot()) ? NutsConstants.DEFAULT_WORKSPACE_ROOT : bootOptions.getRoot();
        this.runtimeSourceURL = bootOptions.getRuntimeSourceURL();
        this.runtimeId = StringUtils.isEmpty(bootOptions.getRuntimeId()) ? null : WorkspaceNutsId.parse(bootOptions.getRuntimeId());
        this.contextClassLoaderProvider = bootOptions.getClassLoaderProvider() == null ? DefaultNutsClassLoaderProvider.INSTANCE : bootOptions.getClassLoaderProvider();
    }

    @Override
    public String getBootId() {
        return "net.vpc.app.nuts:nuts#" + IOUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"))
                .getProperty("project.version", "0.0.0");
    }

    @Override
    public NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options) {
        workspace = resolveWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME);
        if (workspace.equals(NutsConstants.BOOTSTRAP_REPOSITORY_NAME) || new File(workspace).equals(new File(root, NutsConstants.DEFAULT_WORKSPACE_NAME))) {
            throw new NutsInvalidWorkspaceException(NutsConstants.BOOTSTRAP_REPOSITORY_NAME, NutsConstants.BOOTSTRAP_REPOSITORY_NAME + " is not a valid workspace name");
        }
        if (options == null) {
            options = new NutsWorkspaceCreateOptions()
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(true);
        }
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        NutsWorkspaceClassPath workspaceClassPath = loadWorkspaceClassPath(true);
        if (workspaceClassPath == null) {
            throw new NutsInvalidWorkspaceException(workspace, "Unable to load ClassPath");
        }

        File repoFolder = createFile(root, NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        File f = getBootFile(workspaceClassPath.getId(), "jar", workspaceClassPath.getRepositoriesArray(), repoFolder, true);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(workspace, "Unable to load " + workspaceClassPath.getId());
        }

        allExtensionFiles.put(workspaceClassPath.getId().toString(), f);
        for (WorkspaceNutsId id : workspaceClassPath.getDependenciesArray()) {
            f = getBootFile(id, "jar", workspaceClassPath.getRepositoriesArray(), repoFolder, false);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(workspace, "Unable to load " + id);
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
        if (nutsWorkspace == null) {
            //should never happen
            System.err.printf("Unable to load Workspace Component from ClassPath : \n");
            for (URL url : urls) {
                System.err.printf("\t%s\n", url);
            }
            throw new NutsInvalidWorkspaceException(workspace, "Unable to load Workspace Component from ClassPath : " + Arrays.asList(urls));
        }
        if (nutsWorkspaceImpl.initializeWorkspace(this, factoryInstance, getBootId(), workspaceClassPath.getId().toString(), workspace, workspaceClassLoader, options.copy().setIgnoreIfFound(true))) {
            log.fine("Worlspace created " + workspace);
        }
        return nutsWorkspace;
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

    @Override
    public String getRuntimeId() {
        return runtimeId == null ? null : runtimeId.toString();
    }

    private String[] resolveBootConfigRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
        initial.add(root + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
//        initial.add("~/.m2/repository");
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add(DEFAULT_REMOTE_BOOTSTRAP_REPOSITORY_URL);
//        initial.add("https://github.com/thevpc/nuts/raw/master/nuts/nuts-bootstrap");

        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (String v : initial) {
            if (!StringUtils.isEmpty(v)) {
                v = v.trim();
                for (String v0 : v.split(";")) {
                    v0 = v0.trim();
                    if (!allValid.contains(v0)) {
                        allValid.add(v0);
                    }
                }
            }
        }
        return allValid.toArray(new String[allValid.size()]);
    }

    private String[] resolveBootClassPathRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
        initial.add(root + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add("~/.m2/repository");
        initial.add(DEFAULT_REMOTE_BOOTSTRAP_REPOSITORY_URL);
        initial.add("http://repo.maven.apache.org/maven2/");
//        initial.add("https://github.com/thevpc/nuts/raw/master/nuts/nuts-bootstrap");

        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (String v : initial) {
            if (!StringUtils.isEmpty(v)) {
                v = v.trim();
                for (String v0 : v.split(";")) {
                    v0 = v0.trim();
                    if (!allValid.contains(v0)) {
                        allValid.add(v0);
                    }
                }
            }
        }
        return allValid.toArray(new String[allValid.size()]);
    }

    private File getBootFileLocation(WorkspaceNutsId id, String ext) {
        return new File(createFile(root, NutsConstants.BOOTSTRAP_REPOSITORY_NAME), getPath(id, ext));
    }

    private NutsWorkspaceClassPath loadWorkspaceClassPath(boolean first) {
        WorkspaceNutsId wbootId = WorkspaceNutsId.parse(getBootId());
        File runtimeFile = getBootFileLocation(wbootId, "boot");
        String bootPath = '/' + wbootId.groupId.replace('.', '/') + '/' + wbootId.groupId + '/' + wbootId.version + '/' + wbootId.artifactId + "-" + wbootId.getVersion() + ".boot";
        String[] resolvedBootRepositories = null;
        String repositories = null;
        WorkspaceNutsId _runtimeId = runtimeId;
        if (_runtimeId == null || repositories == null) {
            String runtimeId = null;
            boolean storeRuntimeFile = true;
            Properties bootProperties = null;
            if (runtimeFile.exists()) {
                bootProperties = IOUtils.loadFileProperties(runtimeFile);
                runtimeId = bootProperties.getProperty("runtimeId");
                repositories = bootProperties.getProperty("repositories");
                if (!StringUtils.isEmpty(runtimeId) && !StringUtils.isEmpty(repositories)) {
                    storeRuntimeFile = false;
                }
            }
            if (StringUtils.isEmpty(runtimeId) || StringUtils.isEmpty(repositories)) {
                resolvedBootRepositories = resolveBootConfigRepositories(repositories);
                for (String repo : resolvedBootRepositories) {
                    String urlString = buildURL(repo, bootPath);
                    Properties wruntimeProperties = IOUtils.loadURLProperties(urlString);
                    if (!wruntimeProperties.isEmpty()) {
                        String wruntimeId = wruntimeProperties.getProperty("runtimeId");
                        String wrepositories = wruntimeProperties.getProperty("repositories");
                        if (!StringUtils.isEmpty(wruntimeId) && !StringUtils.isEmpty(wrepositories)) {
                            runtimeId = wruntimeId;
                            repositories = wrepositories;
                            break;
                        }
                    }
                }
            }

            if (_runtimeId == null && StringUtils.isEmpty(runtimeId)) {
//                storeRuntimeFile = false;
                runtimeId = "net.vpc.app.nuts:nuts-core#" + wbootId.getVersion() + ".0";
                log.log(Level.CONFIG, "Failed to resolve boot file (" + bootPath + ") from repositories. considering defaults : " + runtimeId);
                for (String resolvedRepository : resolvedBootRepositories) {
                    log.log(Level.CONFIG, "\tInaccessible boot repository : " + resolvedRepository);
                }
            }
            if (StringUtils.isEmpty(repositories)) {
                repositories = "";
            }
            if (_runtimeId == null && storeRuntimeFile) {
                bootProperties = new Properties();
                bootProperties.setProperty("runtimeId", runtimeId);
                bootProperties.setProperty("repositories", repositories);
                runtimeFile.getParentFile().mkdirs();
                IOUtils.storeProperties(bootProperties, runtimeFile);
            }
            if (_runtimeId == null) {
                _runtimeId = WorkspaceNutsId.parse(runtimeId);
            }
        }

        File localRuntimeConfigFile = getBootFile(_runtimeId, "properties");
        List<NutsWorkspaceClassPath> all = new ArrayList<>();
        if (localRuntimeConfigFile != null && localRuntimeConfigFile.exists()) {
            NutsWorkspaceClassPath c = null;
            try {
                c = new NutsWorkspaceClassPath(localRuntimeConfigFile);
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
            try {
                String urlString = buildURL(u, getPath(_runtimeId, "properties"));
                cp = new NutsWorkspaceClassPath(new URL(urlString));
            } catch (Exception ex) {
                log.log(Level.CONFIG, "Runtime inaccessible from : {0}", u);
                //ignore
            }
            if (cp != null) {
                log.log(Level.CONFIG, "Loaded Runtime {0} from : {1}", new Object[]{cp.getId(), u});
                all.add(cp);
                if (first) {
                    break;
                }
            }
        }
        if (all.isEmpty()) {
            String runtimeVersion = WorkspaceNutsId.parse(getBootId()).version + ".0";
            all.add(new NutsWorkspaceClassPath(
                    "net.vpc.app.nuts:nuts-core",
                    runtimeVersion,
                    getBootId() + ";"
                    + "javax.servlet:javax.servlet-api#3.1.0;"
                    + "org.jline#jline#3.5.2;"
                    + "org.ow2.asm:asm#5.2;"
                    + "net.vpc.common:java-shell#0.5;"
                    + "org.glassfish:javax.json#1.0.4",
                    "~/.m2/repository;http://repo.maven.apache.org/maven2/;https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"
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
        List<String> resolvedRepositories2 = new ArrayList<>();
        resolvedRepositories2.add(cp.getRepositoriesString());
        resolvedRepositories2.addAll(Arrays.asList(resolvedBootRepositories));

        cp = new NutsWorkspaceClassPath(cp.getId().groupId + ":" + cp.getId().artifactId, cp.getId().version, cp.getDependenciesString(),
                StringUtils.join(";", Arrays.asList(resolveBootClassPathRepositories(resolvedRepositories2.toArray(new String[resolvedRepositories2.size()])))));

        File runtimePropLocation = getBootFileLocation(cp.getId(), "properties");
        if (!runtimePropLocation.exists() || runtimePropLocation.length() <= 0) {
            runtimePropLocation.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("project.id", cp.getId().getGroupId() + ":" + cp.getId().getArtifactId());
            p.setProperty("project.version", cp.getId().getVersion());
            StringBuilder dsb = new StringBuilder();
            for (WorkspaceNutsId id : cp.getDependenciesArray()) {
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

    private File getBootFile(WorkspaceNutsId vid, String ext, String[] repositories, File cacheFolder, boolean useCache) {
        for (String repository : repositories) {
            File file = getBootFile(vid, ext, repository, cacheFolder, useCache);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private String buildURL(String base, String path) {
        if (base.startsWith("http://") || base.startsWith("https://")) {
            if (!base.endsWith("/") && !path.endsWith("/")) {
                base += "/";
            }
            return base + path;
        } else {
            path = path.replace('/', File.separatorChar);
            if (!base.endsWith(File.separator) && !path.endsWith(File.separator)) {
                base += File.separator;
            }
            return base + path;
        }
    }

    private File getBootFile(WorkspaceNutsId vid, String ext) {
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

    private String getFileName(WorkspaceNutsId id, String ext) {
        return id.artifactId + "-" + id.version + "." + ext;
    }

    private String getPath(WorkspaceNutsId id, String ext) {
        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + getFileName(id, ext);
    }

    private File getBootFile(WorkspaceNutsId id, String ext, String repository, File cacheFolder, boolean useCache) {
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
        } else if (cacheFolder != null && repository.startsWith("http://") || repository.startsWith("https://")) {
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
            File repoFolder = createFile(root, repository);
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
//                                System.out.println("Loaded " + aClass + " from " + file);
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
            File file = IOUtils.resolvePath(root + "/" + defaultName, null, root);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = IOUtils.resolvePath(workspace, null, root);
            workspace = file == null ? null : file.getPath();
        }
        return workspace;
    }

    @Override
    public String getRoot() {
        return root;
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
