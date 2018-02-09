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

    public static final Logger log = Logger.getLogger(DefaultNutsBootWorkspace.class.getName());
    private final String workspaceRoot;
    private final String workspace;
    private final String workspaceRuntimeVersion;
    private final String bootURL;
    private final String workspaceRuntimeId;
    private final NutsClassLoaderProvider contextClassLoaderProvider;

    public DefaultNutsBootWorkspace(String workspaceRoot, String runtimeId, String workspaceRuntimeVersion, String bootURL, NutsClassLoaderProvider provider) {
        this.workspaceRoot = StringUtils.isEmpty(workspaceRoot) ? NutsConstants.DEFAULT_WORKSPACE_ROOT : workspaceRoot;
        this.workspaceRuntimeVersion = StringUtils.isEmpty(workspaceRuntimeVersion) ? "LATEST" : workspaceRuntimeVersion;
        this.bootURL = bootURL;
        this.workspaceRuntimeId = StringUtils.isEmpty(runtimeId) ? "net.vpc.app.nuts:nuts-core" : runtimeId;
        this.workspace = NutsConstants.BOOTSTRAP_WORKSPACE_NAME;
        this.contextClassLoaderProvider = provider == null ? DefaultNutsClassLoaderProvider.INSTANCE : provider;
    }

    @Override
    public String getWorkspaceRuntimeVersion() {
        return IOUtils.loadProperties(Main.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"))
                .getProperty("project.version", "0.0.0");
    }

    public NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options) {
        workspace = resolveWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME);

        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        WorkspaceClassPath workspaceClassPath = loadWorkspaceClassPath(true);
        if (workspaceClassPath == null) {
            throw new NutsInvalidWorkspaceException(workspace, "Unable to load ClassPath");
        }

        File repoFolder = IOUtils.createFile(workspace, NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
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
            log.log(Level.INFO, "Loading Nuts ClassWorld from " + Arrays.asList(urls));
        }
        ClassLoader workspaceClassLoader = urls.length == 0 ? getContextClassLoader() : new URLClassLoader(urls, getContextClassLoader());
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
        if (nutsWorkspace == null) {
            //should never happen
            throw new NutsInvalidWorkspaceException(workspace, "Unable to load Workspace Component from ClassPath");
        }
        nutsWorkspaceImpl.initializeWorkspace(this, factoryInstance, NutsConstants.NUTS_COMPONENT_ID, workspaceClassPath.getId().toString(), workspace, workspaceClassLoader, options.copy().setIgnoreIfFound(true));
        return nutsWorkspace;
    }

    private URL[] resolveClassWorldURLs(Collection<File> list) {
        List<URL> urls = new ArrayList<>();
        for (File file : list) {
            if (file != null) {
                if (isLoadedClassPath(file)) {
                    log.log(Level.WARNING, "File will not be loaded (already in classloader) : " + file);
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

    private String resolveRepoURLs() {
        try {
            return createFile(workspaceRoot, workspace).toURI().toURL().toString() + ";"
                    + "https://github.com/thevpc/nuts/raw/master/nuts/nuts-bootstrap";
        } catch (MalformedURLException e) {
            throw new NutsParseException("Unable to parse repo urls", e);
        }
    }

    private String getWorkspaceRuntimeId() {
        return workspaceRuntimeId;
    }

    private String getValidBootVersion() {
        return workspaceRuntimeVersion;
    }

    private WorkspaceClassPath loadWorkspaceClassPath(boolean first) {
        String bootId = getWorkspaceRuntimeId();
        String bootVersion = getValidBootVersion();
        WorkspaceNutsId _bootId = WorkspaceNutsId.parse(bootId);
        File localCurrent = getBootFile(_bootId.groupId, _bootId.artifactId, "LATEST", "properties");
        List<WorkspaceClassPath> all = new ArrayList<>();
        if ("LATEST".equals(bootVersion)) {
            if (localCurrent != null && localCurrent.exists()) {
                WorkspaceClassPath c = null;
                try {
                    c = new WorkspaceClassPath(localCurrent);
                } catch (IOException e) {
                    //
                }
                if (c != null) {
                    all.add(c);
                    if (first) {
                        return c;
                    }
                }
            }
        }
        String urls = resolveRepoURLs();
        if (!StringUtils.isEmpty(bootURL)) {
            urls = bootURL + ";" + urls;
        }
        for (String u : urls.split(";")) {
            WorkspaceClassPath cp = null;
            try {
                String tu = u.trim();
                if (!tu.isEmpty()) {
                    cp = new WorkspaceClassPath(new URL(buildURL(tu, _bootId.groupId.replace('.', '/') + '/' + _bootId.artifactId + '/' + _bootId.artifactId + "-" + bootVersion + ".properties")));
                }
            } catch (Exception ex) {
                //ignore
            }
            if (cp != null) {
                all.add(cp);
                if (first) {
                    break;
                }
            }
        }
        if (all.isEmpty()) {
            String ver = "0.3.6.0";
            all.add(new WorkspaceClassPath(
                    "net.vpc.app.nuts:nuts-core#" + ver,
                    "javax.servlet:javax.servlet-api#3.1.0;"
                    + "net.vpc.app.nuts:nuts#" + ver + ";"
                    + "org.jline#jline#3.5.2;"
                    + "org.ow2.asm:asm#5.2;"
                    + "net.vpc.common:java-shell#0.4;"
                    + "org.glassfish:javax.json#1.0.4",
                    "~/.m2/repository;http://repo.maven.apache.org/maven2/;https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"
            ));
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() > 1) {

            Collections.sort(all, new Comparator<WorkspaceClassPath>() {
                @Override
                public int compare(WorkspaceClassPath o1, WorkspaceClassPath o2) {
                    return compareVersion(o1.getId().getVersion(), o2.getId().getVersion());
                }

                public int compareVersion(String o1, String o2) {
                    String[] split1 = splitByDigit(o1);
                    String[] split2 = splitByDigit(o2);
                    for (int i = 0; i < Math.max(split1.length, split2.length); i++) {
                        if (i >= split1.length) {
                            return -1;
                        }
                        if (i >= split2.length) {
                            return 1;
                        }
                        int x = compareVersionDigit(split1[i], split2[i]);
                        if (x != 0) {
                            return x;
                        }
                    }
                    return 0;
                }

                private int compareVersionDigit(String version1, String version2) {
                    if (version1.equals(version2)) {
                        return 0;
                    }
                    if (version1.isEmpty()) {
                        return -1;
                    }
                    if (version2.isEmpty()) {
                        return 1;
                    }
                    if (Character.isDigit(version1.charAt(0)) && Character.isDigit(version1.charAt(1))) {
                        return Integer.compare(Integer.parseInt(version1), Integer.parseInt(version2));
                    }
                    return version1.compareTo(version2);
                }

                private String[] splitByDigit(String version) {
                    List<String> all = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();
                    if (version == null) {
                        version = "";
                    } else {
                        version = version.trim();
                    }
                    if (version.isEmpty()) {
                        return new String[0];
                    }
                    int type = -1;
                    final int TYPE_D = 1;
                    final int TYPE_C = 2;
                    for (char cc : version.toCharArray()) {
                        int t = Character.isDigit(cc) ? TYPE_D : TYPE_C;
                        if (sb.length() == 0) {
                            type = t;
                        } else if (t == type) {
                            sb.append(cc);
                        } else {
                            all.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                    }
                    if (sb.length() > 0) {
                        all.add(sb.toString());
                    }
                    return all.toArray(new String[all.size()]);
                }
            });
        }
        WorkspaceClassPath cp = all.get(all.size() - 1);
        updateBootClassPath(cp);
        return cp;
    }

    public void updateBootClassPath(WorkspaceClassPath cp) {
        String bootId = getWorkspaceRuntimeId();

        WorkspaceNutsId _bootId = WorkspaceNutsId.parse(bootId);
        File localCurrent = getBootFile(_bootId.groupId, _bootId.artifactId, "LATEST", "properties");
        if (localCurrent != null) {
            localCurrent.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("project.id", cp.getId().toString());
            StringBuilder dsb = new StringBuilder();
            for (WorkspaceNutsId id : cp.getDependenciesArray()) {
                if (dsb.length() > 0) {
                    dsb.append(";");
                }
                dsb.append(id.toString());
            }
            p.setProperty("project.dependencies", dsb.toString());
            p.setProperty("project.repositories", cp.getRepositoriesString());
            Writer writer = null;
            try {
                try {
                    p.store(writer = new FileWriter(localCurrent), null);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getBootFile(WorkspaceNutsId vid, String ext, String[] repositories, File cacheFolder, boolean useCache) {
        for (String repository : repositories) {
            File file = getBootFile(vid.groupId, vid.artifactId, vid.version, ext, repository, cacheFolder, useCache);
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

    public File getBootFile(String groupId, String artifactId, String version, String ext) {
        return getBootFile(groupId, artifactId, version, ext, workspace, null, false);
    }

    private static File createFile(String parent, String child) {
        if (child.startsWith("~/")) {
            child = new File(System.getProperty("user.home"), child.substring(2)).getPath();
        }
        if ((child.startsWith("/") || child.startsWith("\\") || new File(child).isAbsolute())) {
            return new File(child);
        }
        if (parent != null) {
            if (parent.startsWith("~/")) {
                parent = new File(System.getProperty("user.home"), parent.substring(2)).getPath();
            }
        } else {
            parent = ".";
        }
        return new File(parent, child);
    }

    public File getBootFile(String groupId, String artifactId, String version, String ext, String repository, File cacheFolder, boolean useCache) {
        repository = repository.trim();
        String path = groupId.replace('.', '/') + '/' + artifactId + '/' + version + "/" + artifactId + "-" + version + "." + ext;
        if (useCache && cacheFolder != null) {
            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile()) {
                return f;
            }
        }
        if (repository.startsWith("~/")) {
            return getBootFile(groupId, artifactId, version, ext, new File(System.getProperty("user.home"), repository.substring(2)).getPath(), cacheFolder, useCache);
        } else if (cacheFolder != null && repository.startsWith("http://") || repository.startsWith("https://")) {
            File ok = null;
            try {
                File to = new File(new File(cacheFolder, path.replace('/', File.separatorChar)), artifactId + "-" + version + "." + ext);
                String urlPath = repository;
                if (!urlPath.endsWith("/")) {
                    urlPath += "/";
                }
                urlPath += path;
                InputStream from = new URL(urlPath).openStream();
                log.log(Level.CONFIG, "Loading " + (artifactId + "-" + version + "." + ext) + " from " + urlPath);
                IOUtils.copy(from, to, true, true);
                ok = to;
            } catch (IOException ex) {
                //not found
            }
            return ok;
        } else {
            File repoFolder = createFile(workspaceRoot, repository);
            if (!repoFolder.exists()) {
                if (!repoFolder.mkdirs()) {
                    log.log(Level.SEVERE, "Unable to create " + repoFolder);
                }
            }
            if (repoFolder.isDirectory()) {
                return new File(repoFolder, path.replace('/', File.separatorChar));
            } else {
                log.log(Level.SEVERE, "repository is not a valid folder : " + repoFolder);
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

    public String resolveWorkspacePath(String workspace, String defaultName) {
        if (StringUtils.isEmpty(workspace)) {
            File file = IOUtils.resolvePath(workspaceRoot + "/" + defaultName, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        } else {
            File file = IOUtils.resolvePath(workspace, null, workspaceRoot);
            workspace = file == null ? null : file.getPath();
        }
        return workspace;
    }

    @Override
    public String getRoot() {
        return workspaceRoot;
    }

    public NutsClassLoaderProvider getContextClassLoaderProvider() {
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
