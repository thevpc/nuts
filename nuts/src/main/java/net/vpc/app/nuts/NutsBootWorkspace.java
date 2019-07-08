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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * NutsBootWorkspace is responsible of loading initial nuts-core.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at $root/default-workspace/boot where $root is
 * the nuts root folder (~/.nuts)
 * <p>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-core.jar and its dependencies and for creating workspaces using
 * the method {@link #openWorkspace()}.
 * <p>
 * @author vpc
 * @since 0.5.4
 */
public final class NutsBootWorkspace {

    private static final boolean NO_M2 = PrivateNutsUtils.getSysBoolNutsProperty("no-m2", false);
    public static final Logger LOG = Logger.getLogger(NutsBootWorkspace.class.getName());
    private final long creationTime = System.currentTimeMillis();
    private NutsDefaultWorkspaceOptions options;
    private String runtimeId;
    private String actualVersion;
    private NutsClassLoaderProvider contextClassLoaderProvider;
    private int newInstanceRequirements;
    private PrivateNutsBootConfig runningBootConfig;
    private String requiredJavaCommand;
    private String requiredBootVersion;
    private String requiredJavaOptions;
    private PrivateNutsId bootId;
    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return runningBootConfig.getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "home.apps":
                case "home.config":
                case "home.lib":
                case "home.temp":
                case "home.var":
                case "home.cache":
                case "home.run":
                case "home.log":
                    return getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()));
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = runningBootConfig.getStoreLocations();
                    if (s == null) {
                        return "${" + from + "}";
                    }
                    return s.get(from);
                }
            }
            return "${" + from + "}";
        }
    };

    public NutsBootWorkspace(String... args) {
        this(NutsArgumentsParser.parseNutsArguments(args));
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsDefaultWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            NutsDefaultWorkspaceOptions copy = options.copy();
            copy.setCreationTime(creationTime);
            options = copy;
        }
        actualVersion = Nuts.getVersion();
        this.options = (options instanceof NutsDefaultWorkspaceOptions) ? ((NutsDefaultWorkspaceOptions) options) : options.copy();
        this.bootId = PrivateNutsId.parse(NutsConstants.Ids.NUTS_API + "#" + actualVersion);
        newInstanceRequirements = 0;
        NutsLogUtils.bootstrap(options.getLogConfig());
        LOG.log(Level.CONFIG, "Start Nuts : {0}", Instant.ofEpochMilli(creationTime).toString());
        LOG.log(Level.CONFIG, "Open Nuts Workspace : {0}", options.format().getBootCommandLine());
        LOG.log(Level.CONFIG, "Open Nuts Workspace (compact) : {0}", options.format().compact().getBootCommandLine());
        runningBootConfig = createConfig(options);
        NutsLogUtils.prepare(options.getLogConfig(), PrivateNutsUtils.syspath(getRunningStoreLocation(NutsStoreLocation.LOG) + "/net/vpc/app/nuts/nuts/" + actualVersion));

        requiredBootVersion = options.getApiVersion();
        if (requiredBootVersion == null) {
            requiredBootVersion = runningBootConfig.getApiVersion();
        }
        requiredJavaCommand = options.getJavaCommand();
        if (requiredJavaCommand == null) {
            requiredJavaCommand = runningBootConfig.getJavaCommand();
        }
        requiredJavaOptions = options.getJavaOptions();
        if (requiredJavaOptions == null) {
            requiredJavaOptions = runningBootConfig.getJavaOptions();
        }
        newInstanceRequirements = checkRequirements(true);
        if (newInstanceRequirements == 0) {
            runningBootConfig.setApiVersion(actualVersion);
            runningBootConfig.setJavaCommand(null);
            runningBootConfig.setJavaOptions(null);
        }
        this.runtimeId = PrivateNutsUtils.isBlank(options.getRuntimeId()) ? null : PrivateNutsId.parse(options.getRuntimeId()).toString();
        this.contextClassLoaderProvider = options.getClassLoaderProvider() == null ? NutsDefaultClassLoaderProvider.INSTANCE : options.getClassLoaderProvider();
    }

    public String getRunningStoreLocation(NutsStoreLocation location) {
        Map<String, String> s = runningBootConfig.getStoreLocations();
        if (s != null) {
            return s.get(location.id());
        }
        return null;
    }

    public boolean hasUnsatisfiedRequirements() {
        return newInstanceRequirements != 0;
    }

    public int startNewProcess() {
        try {
            return createProcessBuilder().inheritIO().start().waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public ProcessBuilder createProcessBuilder() {
        return new ProcessBuilder(createProcessCommandLine());
    }

    public String[] createProcessCommandLine() {
        LOG.log(Level.FINE, "Running version {0}.  {1}", new Object[]{actualVersion, getRequirementsHelpString(true)});
        StringBuilder errors = new StringBuilder();
        if (NutsConstants.Versions.LATEST.equalsIgnoreCase(requiredBootVersion)
                || NutsConstants.Versions.RELEASE.equalsIgnoreCase(requiredBootVersion)) {
            String releaseVersion;
            try {
                String NUTS_ID_BOOT_API_PATH = "/" + NutsConstants.Ids.NUTS_API.replaceAll("[.:]", "/");
                releaseVersion = PrivateNutsUtils.resolveMavenReleaseVersion(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT, NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException(null, "Unable to load nuts version from " + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }

        String defaultWorkspaceLibFolder = getRunningStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        if (!NO_M2) {
            repos.add(System.getProperty("user.home") + PrivateNutsUtils.syspath("/.m2/repository"));
        }
        repos.addAll(Arrays.asList(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
        ));
        File file = PrivateNutsUtils.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion,
                repos.toArray(new String[0]),
                getRunningStoreLocation(NutsStoreLocation.CACHE) + File.separator + NutsConstants.Folders.BOOT
        );
        if (file == null) {
            errors.append("Unable to load ").append(bootId).append("\n");
            showError(runningBootConfig,
                    options.getWorkspace(), null,
                    errors.toString()
            );
            throw new NutsIllegalArgumentException(null, "Unable to load " + NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion);
        }

        List<String> cmd = new ArrayList<>();
        String jc = requiredJavaCommand;
        if (jc == null || jc.trim().isEmpty()) {
            jc = PrivateNutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : PrivateNutsCommandLine.parseCommandLineArray(options.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(Arrays.asList(options.format().compact().getBootCommand()));
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            System.out.println("[EXEC] " + sb);
        }
        return cmd.toArray(new String[0]);
    }

    public NutsDefaultWorkspaceOptions getOptions() {
        return options;
    }

    private static class DepsAndRepos {

        LinkedHashSet<String> deps = new LinkedHashSet<>();
        LinkedHashSet<String> repos = new LinkedHashSet<>();
    }

    private DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(String urlPath) {
        DepsAndRepos depsAndRepos = null;
        if (!NO_M2) {
            File mavenNutsCorePom = new File(System.getProperty("user.home"), (".m2/repository/" + urlPath).replace("/", File.separator));
            if (mavenNutsCorePom.isFile()) {
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(mavenNutsCorePom.getPath());
            }
        }
        if (depsAndRepos == null || depsAndRepos.deps.isEmpty()) {
            for (String baseUrl : new String[]{
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
            }) {
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(baseUrl + "/" + urlPath);
                if (!depsAndRepos.deps.isEmpty()) {
                    break;
                }
            }
        }
        return depsAndRepos;
    }

    private DepsAndRepos loadDependenciesAndRepositoriesFromPomUrl(String url) {
        DepsAndRepos depsAndRepos = new DepsAndRepos();
//        String repositories = null;
//        String dependencies = null;
        InputStream xml = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                xml = new URL(url).openStream();
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            }
//            List<String> dependenciesList = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            Element c = doc.getDocumentElement();
            for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("dependencies")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("dependency")) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            String groupId = null;
                            String artifactId = null;
                            String version = null;
                            String scope = null;
                            for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                if (c3.getChildNodes().item(k) instanceof Element) {
                                    Element c4 = (Element) c3.getChildNodes().item(k);
                                    switch (c4.getNodeName()) {
                                        case "groupId": {
                                            groupId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "artifactId": {
                                            artifactId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "version": {
                                            version = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "scope": {
                                            scope = c4.getTextContent().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (PrivateNutsUtils.isBlank(groupId)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty groupId");
                            } else if (groupId.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in groupId=" + groupId);
                            }
                            if (PrivateNutsUtils.isBlank(artifactId)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty artifactId");
                            } else if (artifactId.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + artifactId);
                            }
                            if (PrivateNutsUtils.isBlank(version)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty artifactId");
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + version);
                            }
                            //this is maven dependency, using "compile"
                            if (PrivateNutsUtils.isBlank(scope) || scope.equals("compile")) {
                                depsAndRepos.deps.add(groupId + ":" + artifactId + "#" + version);
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + version);
                            }
                        }
                    }
                } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            switch (c3.getNodeName()) {
                                case "nuts-runtime-repositories": {
                                    String t = c3.getTextContent().trim();
                                    if (t.length() > 0) {
                                        depsAndRepos.deps.addAll(PrivateNutsUtils.split(t, ";", true));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            //ignore
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }

        return depsAndRepos;
    }

    private void openWorkspaceAttempt(OpenWorkspaceData info, boolean recover) {
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        PrivateNutsBootConfig recipient = runningBootConfig.copy();
        info.cfg = recipient;
        if (recover) {
            if (!PrivateNutsUtils.isBlank(info.cfg.getRuntimeId())
                    && !info.cfg.getRuntimeDependencies().isEmpty()) {
                LOG.log(Level.CONFIG, "[RECOV. ] Invalidating old  runtime.");
            }
            info.cfg.setRuntimeId(null);
            info.cfg.setRuntimeDependencies(null);
        }

        if (!PrivateNutsUtils.isBlank(recipient.getApiVersion()) && !actualVersion.equals(recipient.getApiVersion())) {
            LOG.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{recipient.getApiVersion(), actualVersion});
            recipient.setApiVersion(actualVersion);
            recipient.setRuntimeId(null);
            recipient.setRuntimeDependencies(null);
        } else {
            recipient.setApiVersion(actualVersion);
        }
        PrivateNutsId apiId = PrivateNutsUtils.isBlank(recipient.getApiVersion()) ? bootId : new PrivateNutsId(bootId.getGroupId(), bootId.getArtifactId(), recipient.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");

        String runtimeId = recipient.getRuntimeId();
        if (PrivateNutsUtils.isBlank(runtimeId)) {
            runtimeId = resolveLatestRuntimeId(apiId.getVersion());
        }
        if (PrivateNutsUtils.isBlank(runtimeId)) {
            runtimeId = NutsConstants.Ids.NUTS_RUNTIME + "#" + apiId.getVersion() + ".0";
            LOG.log(Level.CONFIG, "[ERROR  ] Failed to load latest runtime id. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        PrivateNutsId _runtimeId = PrivateNutsId.parse(runtimeId);
        recipient.setRuntimeId(runtimeId);
        String runtimeVersion = _runtimeId.getVersion();

        //resolve runtime dependencies plus boot repositories
        if (recipient.getRuntimeDependencies().isEmpty()) {
            try {
                String urlPath = "net/vpc/app/nuts/nuts-core/" + runtimeVersion + "/nuts-core-" + runtimeVersion + ".pom";
                DepsAndRepos depsAndRepos = loadDependenciesAndRepositoriesFromPomPath(urlPath);
                recipient.setRuntimeDependencies(depsAndRepos.deps);
                recipient.setBootRepositories(depsAndRepos.repos.stream().collect(Collectors.joining(";")) + ";" + recipient.getBootRepositories());
            } catch (Exception ex) {
                //
            }
        }
        LinkedHashSet<String> bootRepositories = new LinkedHashSet<>();
        if (recipient.getBootRepositories() != null) {
            for (String v : recipient.getBootRepositories().split(";")) {
                v = v.trim();
                if (v.length() > 0) {
                    bootRepositories.add(v);
                }
            }
        }
        bootRepositories.add(NutsConstants.BootstrapURLs.LOCAL_MAVEN_CENTRAL);
        bootRepositories.add(NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT);
        bootRepositories.add(NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL);
        recipient.setBootRepositories(
                bootRepositories.stream().collect(Collectors.joining(";"))
        );
        LinkedHashSet<String> excludedDeps = new LinkedHashSet<>();
        if (options.getExcludedExtensions() != null) {
            for (String excludedExtension : options.getExcludedExtensions()) {
                excludedDeps.add(PrivateNutsId.parse(excludedExtension).getShortName());
            }
        }
        Set<String> visitedDeps = new HashSet<>();
        for (String r : recipient.getRuntimeDependencies()) {
            visitedDeps.add(PrivateNutsId.parse(r).getShortName());
        }
        LinkedHashMap<String, PrivateNutsId> deps = new LinkedHashMap<>();
        for (String extension : recipient.getExtensions()) {
            PrivateNutsId id = PrivateNutsId.parse(extension);
            if (!excludedDeps.contains(id.getShortName())) {
                String urlPath = id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + "/" + id.getArtifactId() + "-" + runtimeVersion + ".pom";
                DepsAndRepos depsAndRepos = loadDependenciesAndRepositoriesFromPomPath(urlPath);
                for (String dep : depsAndRepos.deps) {
                    PrivateNutsId did = PrivateNutsId.parse(dep);
                    String shortName = did.getShortName();
                    if (!visitedDeps.contains(shortName)) {
                        PrivateNutsId old = deps.get(shortName);
                        if (old == null || old.getVersion().equals(did.getVersion())) {
                            //ignore
                        } else {
                            //always override!!
                            deps.put(shortName, did);
                        }
                    }
                }
            }
        }
        recipient.setExtensionDependencies(new LinkedHashSet<>(deps.values().stream().map(PrivateNutsId::getLongName).collect(Collectors.toList())));

        String workspaceBootLibFolder = getRunningStoreLocation(NutsStoreLocation.CACHE) + File.separator + NutsConstants.Folders.BOOT;
        PrivateNutsId bootRuntime;
        if (PrivateNutsUtils.isBlank(info.cfg.getRuntimeId())) {
            bootRuntime = PrivateNutsId.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.cfg.getRuntimeId());
        } else if (info.cfg.getRuntimeId().contains("#")) {
            bootRuntime = PrivateNutsId.parse(info.cfg.getRuntimeId());
        } else {
            bootRuntime = PrivateNutsId.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.cfg.getRuntimeId());
        }
        String[] repositories = PrivateNutsUtils.splitUrlStrings(info.cfg.getBootRepositories()).toArray(new String[0]);
        File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootLibFolder, !recover);
        if (f == null || !f.isFile()) {
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + bootRuntime + ". Unable to resolve file "
                    + (f == null ? getFileName(bootRuntime, "jar") : f.getPath())
            );
        }

        allExtensionFiles.put(info.cfg.getRuntimeId(), f);
        for (String idStr : info.cfg.getRuntimeDependencies()) {
            PrivateNutsId id = PrivateNutsId.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, !recover);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        for (String idStr : info.cfg.getExtensionDependencies()) {
            PrivateNutsId id = PrivateNutsId.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, !recover);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load Extension " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        info.bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
        LOG.log(Level.CONFIG, "Loading Nuts ClassWorld from {0} jars : ", new Object[]{info.bootClassWorldURLs.length});
        if (LOG.isLoggable(Level.CONFIG)) {
            for (URL bootClassWorldURL : info.bootClassWorldURLs) {
                LOG.log(Level.CONFIG, "\t {0}", new Object[]{PrivateNutsUtils.formatURL(bootClassWorldURL)});
            }
        }
        info.workspaceClassLoader = info.bootClassWorldURLs.length == 0 ? getContextClassLoader() : new NutsBootClassLoader(info.bootClassWorldURLs, getContextClassLoader());
        ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, info.workspaceClassLoader);

        List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
        for (NutsBootWorkspaceFactory a : serviceLoader) {
            factories.add(a);
        }
        factories.sort(new NutsBootWorkspaceFactoryComparator(options));
        NutsBootWorkspaceFactory factoryInstance = null;
        for (NutsBootWorkspaceFactory a : factories) {
            factoryInstance = a;
            try {
                info.nutsWorkspace = a.createWorkspace(options);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Unable to create workspace using factory " + a, ex);
            }
            if (info.nutsWorkspace != null) {
                break;
            }
        }
        if (info.nutsWorkspace == null) {
            //should never happen
            System.err.print("Unable to load Workspace Component from ClassPath : \n");
            for (URL url : info.bootClassWorldURLs) {
                System.err.printf("\t %s%n", PrivateNutsUtils.formatURL(url));
            }
            LOG.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(info.bootClassWorldURLs));
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load Workspace Component from ClassPath : " + Arrays.asList(info.bootClassWorldURLs));
        }
        LOG.log(Level.FINE, "Initialize Workspace");
        ((NutsWorkspaceSPI) info.nutsWorkspace).initializeWorkspace(info.cfg.getWorkspace(),
                info.cfg.getApiVersion(),
                info.cfg.getRuntimeId(),
                info.cfg.getRuntimeDependencies().stream().collect(Collectors.joining(";")),
                info.cfg.getExtensionDependencies().stream().collect(Collectors.joining(";")),
                info.cfg.getBootRepositories(),
                options, factoryInstance,
                info.bootClassWorldURLs,
                info.workspaceClassLoader);
        if (recover) {
            if (!info.nutsWorkspace.config().isReadOnly()) {
                LOG.log(Level.CONFIG, "Save Workspace");
                info.nutsWorkspace.config().save();
            }
        }
        LOG.log(Level.FINE, "End Initialize Workspace");
    }

    public void run() {
        if (hasUnsatisfiedRequirements()) {
            startNewProcess();
            return;
        }

        NutsWorkspace workspace = null;
        try {
            workspace = this.openWorkspace();
        } catch (Throwable ex) {
            if (ex instanceof NutsExecutionException) {
                throw ex;
            }
            int x = 204;
            try {
                String extra = "";
                if (getOptions().isReset()) {
                    //
                } else if (getOptions().isRecover()) {
                    extra = ". You may need to use --reset (ATTENTION: this will delete all your nuts configuration. Use it at your own risk)";
                } else {
                    extra = ". Try --recover to run in fail safe mode";
                }
                runWorkspaceCommand(null, "Cannot open workspace" + extra + " :" + ex.toString());
            } catch (Exception ex2) {
                LOG.log(Level.SEVERE, "runWorkspaceCommand failed : " + ex2.toString(), ex2);
            }
            if (ex instanceof RuntimeException) {
                throw ex;
            }
            if (ex instanceof Error) {
                throw ex;
            }
            throw new NutsExecutionException(null, ex.getMessage(), ex, x);
        }
        runWorkspaceCommand(workspace, "Workspace started successfully");
    }

    public NutsWorkspace openWorkspace() {
        if (hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(null, "Unable to open a distinct version : " + getRequirementsHelpString(true));
        }
        if (options.isReset()) {
            deleteStoreLocations(null, true, true, NutsStoreLocation.values());
        } else if (options.isRecover()) {
            deleteStoreLocations(null, true, false, NutsStoreLocation.CACHE, NutsStoreLocation.TEMP);
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, wil exit before creating workspace
        if (options.getApplicationArguments().length == 0 && options.isSkipWelcome()
                && (options.isRecover() || options.isReset())) {
            throw new NutsExecutionException(null, 0);
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Open Workspace with command line  : {0}", options.format().getBootCommandLine());
            LOG.log(Level.CONFIG, "Open Workspace with config        : ");
            LOG.log(Level.CONFIG, "\t nuts-uuid                      : {0}", PrivateNutsUtils.desc(runningBootConfig.getUuid()));
            LOG.log(Level.CONFIG, "\t nuts-name                      : {0}", PrivateNutsUtils.desc(runningBootConfig.getName()));
            LOG.log(Level.CONFIG, "\t nuts-api-version               : {0}", actualVersion);
            LOG.log(Level.CONFIG, "\t nuts-boot-repositories         : {0}", PrivateNutsUtils.desc(runningBootConfig.getBootRepositories()));
            LOG.log(Level.CONFIG, "\t nuts-runtime-dependencies      : {0}", PrivateNutsUtils.desc(runningBootConfig.getRuntimeDependencies()));
            LOG.log(Level.CONFIG, "\t nuts-extension-dependencies    : {0}", PrivateNutsUtils.desc(runningBootConfig.getExtensionDependencies()));
            LOG.log(Level.CONFIG, "\t nuts-workspace                 : {0}", PrivateNutsUtils.formatLogValue(options.getWorkspace(), runningBootConfig.getWorkspace()));
            LOG.log(Level.CONFIG, "\t nuts-store-apps                : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.APPS), getRunningStoreLocation(NutsStoreLocation.APPS)));
            LOG.log(Level.CONFIG, "\t nuts-store-config              : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.CONFIG), getRunningStoreLocation(NutsStoreLocation.CONFIG)));
            LOG.log(Level.CONFIG, "\t nuts-store-var                 : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.VAR), getRunningStoreLocation(NutsStoreLocation.VAR)));
            LOG.log(Level.CONFIG, "\t nuts-store-log                 : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.LOG), getRunningStoreLocation(NutsStoreLocation.LOG)));
            LOG.log(Level.CONFIG, "\t nuts-store-temp                : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.TEMP), getRunningStoreLocation(NutsStoreLocation.TEMP)));
            LOG.log(Level.CONFIG, "\t nuts-store-cache               : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.CACHE), getRunningStoreLocation(NutsStoreLocation.CACHE)));
            LOG.log(Level.CONFIG, "\t nuts-store-run                 : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.RUN), getRunningStoreLocation(NutsStoreLocation.RUN)));
            LOG.log(Level.CONFIG, "\t nuts-store-lib                 : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.LIB), getRunningStoreLocation(NutsStoreLocation.LIB)));
            LOG.log(Level.CONFIG, "\t nuts-store-strategy            : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocationStrategy(), runningBootConfig.getStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-repos-store-strategy      : {0}", PrivateNutsUtils.formatLogValue(options.getRepositoryStoreLocationStrategy(), runningBootConfig.getRepositoryStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-store-layout              : {0}", PrivateNutsUtils.formatLogValue(options.getStoreLocationLayout(), runningBootConfig.getStoreLocationLayout() == null ? "system" : runningBootConfig.getStoreLocationLayout().id()));
            LOG.log(Level.CONFIG, "\t option-read-only               : {0}", options.isReadOnly());
            LOG.log(Level.CONFIG, "\t option-trace                   : {0}", options.isTrace());
            LOG.log(Level.CONFIG, "\t inherited                      : {0}", options.isInherited());
            LOG.log(Level.CONFIG, "\t inherited-nuts-boot-args       : {0}", PrivateNutsUtils.desc(System.getProperty("nuts.boot.args")));
            LOG.log(Level.CONFIG, "\t inherited-nuts-args            : {0}", PrivateNutsUtils.desc(System.getProperty("nuts.args")));
            LOG.log(Level.CONFIG, "\t option-open-mode               : {0}", PrivateNutsUtils.formatLogValue(options.getOpenMode(), options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
            LOG.log(Level.CONFIG, "\t java-home                      : {0}", System.getProperty("java.home"));
            LOG.log(Level.CONFIG, "\t java-classpath                 : {0}", System.getProperty("java.class.path"));
            LOG.log(Level.CONFIG, "\t java-library-path              : {0}", System.getProperty("java.library.path"));
            LOG.log(Level.CONFIG, "\t os-name                        : {0}", System.getProperty("os.name"));
            LOG.log(Level.CONFIG, "\t os-arch                        : {0}", System.getProperty("os.arch"));
            LOG.log(Level.CONFIG, "\t os-version                     : {0}", System.getProperty("os.version"));
            LOG.log(Level.CONFIG, "\t user-name                      : {0}", System.getProperty("user.name"));
            LOG.log(Level.CONFIG, "\t user-dir                       : {0}", System.getProperty("user.dir"));
            LOG.log(Level.CONFIG, "\t user-home                      : {0}", System.getProperty("user.home"));
        }
        OpenWorkspaceData info = new OpenWorkspaceData();
        try {

            if (options.getOpenMode() == NutsWorkspaceOpenMode.OPEN_EXISTING) {
                //add fail fast test!!
                if (!new File(runningBootConfig.getWorkspace(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(null, runningBootConfig.getWorkspace());
                }
            }
            try {
                openWorkspaceAttempt(info, options.isRecover() && !options.isReset());
            } catch (NutsException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (options.isReset() || options.isRecover()) {
                    throw ex;
                }
                info = new OpenWorkspaceData();
                try {
                    openWorkspaceAttempt(info, true);
                } catch (Throwable ex2) {
                    LOG.log(Level.SEVERE, "Unable to open workspace");
                    LOG.log(Level.SEVERE, "First  Attempt result in error : " + ex.toString(), ex);
                    LOG.log(Level.SEVERE, "Second Attempt result in error : " + ex2.toString(), ex2);
                    throw ex;
                }
            }
            return info.nutsWorkspace;
        } catch (NutsReadOnlyException | NutsUserCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            if (info.cfg == null) {
                info.cfg = new PrivateNutsBootConfig()
                        .setApiVersion(this.actualVersion)
                        .setRuntimeId(runtimeId);
            }
            showError(info.cfg,
                    options.getWorkspace(),
                    info.bootClassWorldURLs,
                    ex.toString()
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            throw new NutsIllegalArgumentException(null, "Unable to locate valid nuts-core components", ex);
        }
    }

    private URL[] resolveClassWorldURLs(Collection<File> list) {
        List<URL> urls = new ArrayList<>();
        for (File file : list) {
            if (file != null) {
                if (isLoadedClassPath(file)) {
                    LOG.log(Level.WARNING, "File will not be loaded (already in classloader) : {0}", file);
                } else {
                    try {
                        urls.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        LOG.log(Level.WARNING, "Failed to create url for  {0}", file);
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * find latest runtime version for the API version provided
     *
     * @param apiId api version
     * @return latest runtime version
     */
    private static String resolveLatestRuntimeId(String apiId) {
        String bestVersion = null;
        if (!NO_M2) {
            File mavenNutsCoreFolder = new File(System.getProperty("user.home"), ".m2/repository/net/vpc/app/nuts/nuts-core/".replace("/", File.separator));
            if (mavenNutsCoreFolder.isDirectory()) {
                File[] chidren = mavenNutsCoreFolder.listFiles();
                if (chidren != null) {
                    for (File file : chidren) {
                        if (file.isDirectory()) {
                            String[] goodChildren = file.list(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".pom");
                                }
                            });
                            if (goodChildren != null && goodChildren.length > 0) {
                                String p = file.getName();
                                if (p.startsWith(apiId + ".")) {
                                    if (bestVersion == null || PrivateNutsUtils.compareRuntimeVersion(bestVersion, p) < 0) {
                                        bestVersion = p;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            URL runtimeMetadata = new URL("https://raw.githubusercontent.com/thevpc/vpc-public-maven/master/net/vpc/app/nuts/nuts-core/maven-metadata.xml");
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(runtimeMetadata.openStream());
            Element c = doc.getDocumentElement();

            for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("versioning")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("versions")) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                if (c3.getChildNodes().item(k) instanceof Element && c3.getChildNodes().item(k).getNodeName().equals("version")) {
                                    Element c4 = (Element) c3.getChildNodes().item(k);
                                    String p = c4.getTextContent();
                                    if (p.startsWith(apiId + ".")) {
                                        if (bestVersion == null || PrivateNutsUtils.compareRuntimeVersion(bestVersion, p) < 0) {
                                            bestVersion = p;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
            }
        } catch (Exception ex) {
            // ignore any error
        }
        if (bestVersion == null) {
            return null;
        }
        return NutsConstants.Ids.NUTS_RUNTIME + "#" + bestVersion;
    }

    protected String getHome(NutsStoreLocation storeFolder) {
        return NutsPlatformUtils.getPlatformHomeFolder(
                runningBootConfig.getStoreLocationLayout(),
                storeFolder,
                runningBootConfig.getHomeLocations(),
                runningBootConfig.isGlobal(),
                runningBootConfig.getName()
        );
    }

    protected String expandPath(String path, String base) {
        path = PrivateNutsUtils.replaceDollarString(path, pathExpansionConverter);
        if (PrivateNutsUtils.isURL(path)) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (base == null) {
            base = System.getProperty("user.dir");
        }
        if (new File(path).isAbsolute()) {
            return path;
        }
        return base + File.separator + path;
    }

    protected URL expandURL(String url) throws MalformedURLException {
        url = expandPath(url, runningBootConfig.getWorkspace());
        if (PrivateNutsUtils.isURL(url)) {
            return new URL(url);
        }
        return new File(url).toURI().toURL();
    }

    private File getBootFile(PrivateNutsId vid, String fileName, String[] repositories, String cacheFolder, boolean useCache) {
        String path = getPathFile(vid, fileName);
        for (String repository : repositories) {
            File file = getBootFile(path, repository, cacheFolder, useCache);
            if (file != null) {
                return file;
            }
        }
        return null;
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

    private String getFileName(PrivateNutsId id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    private String getPathFile(PrivateNutsId id, String name) {
        return id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + "/" + name;
    }

    private File getBootFile(String path, String repository, String cacheFolder, boolean useCache) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        repository = repository.trim();
        repository = expandPath(repository, runningBootConfig.getWorkspace());
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (f.isFile()) {
                return f;
            }
            if (cacheFolder.equals(repository)) {
                return null;
            }
        }
        File localFile = null;
        if (PrivateNutsUtils.isURL(repository)) {
            try {
                localFile = PrivateNutsUtils.toFile(new URL(repository));
            } catch (Exception ex) {
                //ignore
            }
        } else {
            localFile = new File(repository);
        }
        if (localFile == null) {
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
                PrivateNutsUtils.copy(new URL(urlPath), to);
                LOG.log(Level.CONFIG, "[SUCCESS] Loading  {0}", new Object[]{urlPath});
                ok = to;
            } catch (IOException ex) {
                LOG.log(Level.CONFIG, "[ERROR  ] Loading  {0}", new Object[]{urlPath});
                //not found
            }
            return ok;
        } else {
            repository = localFile.getPath();
        }
        File repoFolder = createFile(getHome(NutsStoreLocation.CONFIG), repository);
        File ff = null;

        if (repoFolder.isDirectory()) {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            if (file.isFile()) {
                ff = file;
            } else {
                LOG.log(Level.CONFIG, "[ERROR  ] Locating {0}", new Object[]{file});
            }
        } else {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            LOG.log(Level.CONFIG, "[ERROR  ] Locating {0} . Repository is not a valid folder : {1}", new Object[]{file, repoFolder});
        }

        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
                File to = new File(cacheFolder, path);
                String toc = PrivateNutsUtils.getAbsolutePath(to.getPath());
                String ffc = PrivateNutsUtils.getAbsolutePath(ff.getPath());
                if (ffc.equals(toc)) {
                    return ff;
                }
                try {
                    if (to.getParentFile() != null) {
                        to.getParentFile().mkdirs();
                    }
                    String ext = "config";
                    if (ff.getName().endsWith(".jar")) {
                        ext = "jar";
                    }
                    if (to.isFile()) {
                        PrivateNutsUtils.copy(ff, to);
                        LOG.log(Level.CONFIG, "[RECOV. ] Cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    } else {
                        PrivateNutsUtils.copy(ff, to);
                        LOG.log(Level.CONFIG, "[CACHED ] Cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    }
                    return to;
                } catch (IOException ex) {
                    LOG.log(Level.CONFIG, "[ERROR  ] Caching file {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
                    //not found
                }
                return ff;

            }
            return ff;
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
                                LOG.log(Level.FINEST, "Class {0} Loaded successfully from {1}", new Object[]{aClass, file});
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

    private String getWorkspaceRunModeString() {
        if (this.getOptions().isReset()) {
            return "reset";
        } else if (this.getOptions().isRecover()) {
            return "recover";
        } else {
            return "exec";
        }
    }

    private void runWorkspaceCommand(NutsWorkspace workspace, String message) {
        NutsWorkspaceOptions o = this.getOptions();
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Running workspace in {0} mode", getWorkspaceRunModeString());
        }
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
                case "version": {
                    System.out.println("nuts-version :" + actualVersion);
                    return;
                }
                case "help": {
                    System.out.println("Nuts is a package manager mainly for java applications.");
                    System.out.println("Unluckily it was unable to locate nuts-core component which is esessential for its execution.\n");
                    System.out.println("nuts-version :" + actualVersion);
                    System.out.println("Try to reinstall nuts (with internet access available) and type 'nuts help' to get a list of global options and commands");
                    return;
                }
            }
        }
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsExecutionException(null, "Workspace not available to run : " + new PrivateNutsCommandLine(o.getApplicationArguments()).toString(), 1);
        }
        if (o.getApplicationArguments().length == 0) {
            if (o.isSkipWelcome()) {
                return;
            }
            workspace.exec().command("welcome")
                    .executorOptions(o.getExecutorOptions())
                    .executionType(o.getExecutionType())
                    .failFast().run();
        } else {
            workspace.exec().command(o.getApplicationArguments())
                    .executorOptions(o.getExecutorOptions())
                    .executionType(o.getExecutionType())
                    .failFast()
                    .run();
        }
    }

    private void deleteStoreLocations(NutsWorkspace workspace, boolean includeBoot, boolean includeRoot, NutsStoreLocation... locations) {
        NutsWorkspaceOptions o = getOptions();
        NutsConfirmationMode confirm = o.getConfirm() == null ? NutsConfirmationMode.ASK : o.getConfirm();
        if (confirm == NutsConfirmationMode.ASK
                && this.getOptions().getOutputFormat() != null
                && this.getOptions().getOutputFormat() != NutsOutputFormat.PLAIN) {
            throw new NutsExecutionException(workspace, "Unable to switch to interactive mode for non plain text output format. "
                    + "You need to provide default response (-y|-n) for resetting/recovering workspace", 243);
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Deleting Workspace locations : {0}", runningBootConfig.getWorkspace());
        }
        boolean force = false;
        switch (confirm) {
            case ASK: {
                break;
            }
            case YES: {
                force = true;
                break;
            }
            case NO:
            case ERROR: {
                if (workspace == null) {
                    System.err.println("reset cancelled (applied '--no' argument)");
                } else {
                    workspace.io().getTerminal().out().println(" cancelled (applied '--no' argument)");
                }
                throw new NutsUserCancelException(workspace);
            }
        }
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.config();
        List<File> folders = new ArrayList<>();
        if (conf != null) {
            if (includeRoot) {
                folders.add(conf.getWorkspaceLocation().toFile());
            }
            if (includeBoot) {
                folders.add(conf.getWorkspaceLocation().resolve("boot").toFile());
            }
            for (NutsStoreLocation value : locations) {
                folders.add(conf.getStoreLocation(value).toFile());
            }
        } else {
            if (includeRoot) {
                folders.add(new File(runningBootConfig.getWorkspace()));
            }
            if (includeBoot) {
                folders.add(new File(runningBootConfig.getWorkspace(), "boot"));
            }
            Map<String, String> storeLocations = runningBootConfig.getStoreLocations();
            if (storeLocations != null) {
                for (NutsStoreLocation value : locations) {
                    String p = storeLocations.get(value.id());
                    if (p != null) {
                        folders.add(new File(p));
                    }
                }
            }
        }
        String header = DELETE_FOLDERS_HEADER;
        PrivateNutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, header,
                workspace != null ? workspace.io().getTerminal() : null,
                workspace != null ? workspace.createSession() : null
        );
    }

    private void fallbackInstallActionUnavailable(String message) {
        System.out.println(message);
        if (LOG.isLoggable(Level.SEVERE)) {
            LOG.log(Level.SEVERE, message);
        }
    }

    private void showError(PrivateNutsBootConfig actualBootConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage) {
        Map<String, String> rbc_locations = actualBootConfig.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = Collections.emptyMap();
        }
        System.err.printf("Unable to bootstrap Nuts. : %s%n", extraMessage);
        System.err.printf("Here after current environment info:%n");
        System.err.printf("  nuts-boot-api-version            : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getApiVersion(), "<?> Not Found!"));
        System.err.printf("  nuts-boot-runtime                : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getRuntimeId(), "<?> Not Found!"));
        System.err.printf("  nuts-boot-repositories           : %s%n", PrivateNutsUtils.nvl(actualBootConfig.getBootRepositories(), "<?> Not Found!"));
        System.err.printf("  workspace-location               : %s%n", PrivateNutsUtils.nvl(workspace, "<default-location>"));
        System.err.printf("  nuts-store-apps                  : %s%n", rbc_locations.get(NutsStoreLocation.APPS.id()));
        System.err.printf("  nuts-store-config                : %s%n", rbc_locations.get(NutsStoreLocation.CONFIG.id()));
        System.err.printf("  nuts-store-var                   : %s%n", rbc_locations.get(NutsStoreLocation.VAR.id()));
        System.err.printf("  nuts-store-log                   : %s%n", rbc_locations.get(NutsStoreLocation.LOG.id()));
        System.err.printf("  nuts-store-temp                  : %s%n", rbc_locations.get(NutsStoreLocation.TEMP.id()));
        System.err.printf("  nuts-store-cache                 : %s%n", rbc_locations.get(NutsStoreLocation.CACHE.id()));
        System.err.printf("  nuts-store-run                   : %s%n", rbc_locations.get(NutsStoreLocation.RUN.id()));
        System.err.printf("  nuts-store-lib                   : %s%n", rbc_locations.get(NutsStoreLocation.LIB.id()));
        System.err.printf("  nuts-store-strategy              : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationStrategy()));
        System.err.printf("  nuts-store-layout                : %s%n", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationLayout()));
        System.err.printf("  nuts-boot-args                   : %s%n", options.format().getBootCommandLine());
        System.err.printf("  nuts-app-args                    : %s%n", Arrays.toString(options.getApplicationArguments()));
        System.err.printf("  option-read-only                 : %s%n", options.isReadOnly());
        System.err.printf("  option-trace                     : %s%n", options.isTrace());
        System.err.printf("  option-open-mode                 : %s%n", PrivateNutsUtils.desc(options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            System.err.printf("  nuts-runtime-classpath           : %s%n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    System.err.printf("  nuts-runtime-classpath           : %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                } else {
                    System.err.printf("                                     %s%n", PrivateNutsUtils.formatURL(bootClassWorldURL));
                }
            }
        }
        System.err.printf("  java-version                     : %s%n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s%n", PrivateNutsUtils.resolveJavaCommand(null));
        System.err.printf("  java-class-path                  : %s%n", System.getProperty("java.class.path"));
        System.err.printf("  java-library-path                : %s%n", System.getProperty("java.library.path"));
        System.err.printf("  os-name                          : %s%n", System.getProperty("os.name"));
        System.err.printf("  os-arch                          : %s%n", System.getProperty("os.arch"));
        System.err.printf("  os-version                       : %s%n", System.getProperty("os.version"));
        System.err.printf("  user-name                        : %s%n", System.getProperty("user.name"));
        System.err.printf("  user-home                        : %s%n", System.getProperty("user.home"));
        System.err.printf("  user-dir                         : %s%n", System.getProperty("user.dir"));
        System.err.printf("");
        System.err.printf("If the problem persists you may want to get more debug info by adding '--debug' argument.%n");
        if (!options.isReset() && !options.isRecover()) {
            System.err.printf("You may also enable recover mode to ignore existing cache info with '--recover' argument.%n");
            System.err.printf("Here is the proper command : %n");
            System.err.printf("  java -jar nuts.jar --debug --recover [...]%n");
        } else if (!options.isReset() && options.isRecover()) {
            System.err.printf("You may also enable full reset mode to ignore existing confguration with '--reset' argument.%n");
            System.err.printf("ATTENTION: this will delete all your nuts configuration. Use it at your own risk.%n");
            System.err.printf("Here is the proper command : %n");
            System.err.printf("  java -jar nuts.jar --debug --reset [...]%n");
        }
        System.err.printf("Now exiting Nuts, Bye!%n");
    }

    /**
     * createConfig from Options
     *
     * @param config boot config. Should contain home,workspace, and all
     * StoreLocation information
     * @return resolved config
     */
    private PrivateNutsBootConfig createConfig(NutsWorkspaceOptions options) {
        PrivateNutsBootConfig config = new PrivateNutsBootConfig(options);
        String ws = options.getWorkspace();
        int maxDepth = 36;
        PrivateNutsBootConfig lastConfigLoaded = null;
        String lastConfigPath = null;
        String workspace0 = config.getWorkspace();
        for (int i = 0; i < maxDepth; i++) {
            lastConfigPath
                    = PrivateNutsUtils.isValidWorkspaceName(ws)
                    ? NutsPlatformUtils.getPlatformHomeFolder(
                            null, null, null,
                            config.isGlobal(),
                            PrivateNutsUtils.resolveValidWorkspaceName(ws)
                    ) : PrivateNutsUtils.getAbsolutePath(ws);

            PrivateNutsBootConfig configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastConfigPath);
            if (configLoaded == null) {
                //not loaded
                break;
            }
            if (PrivateNutsUtils.isBlank(configLoaded.getWorkspace())) {
                lastConfigLoaded = configLoaded;
                break;
            }
            ws = configLoaded.getWorkspace();
            if (i >= maxDepth - 1) {
                throw new NutsIllegalArgumentException(null, "Cyclic Workspace resolution");
            }
        }
        boolean namedWorkspace = PrivateNutsUtils.isValidWorkspaceName(workspace0);
        config.setWorkspace(lastConfigPath);
        if (lastConfigLoaded != null) {
            config.setWorkspace(lastConfigPath);
            config.setName(lastConfigLoaded.getName());
            config.setUuid(lastConfigLoaded.getUuid());
            config.setApiVersion(lastConfigLoaded.getApiVersion());
            config.setRuntimeId(lastConfigLoaded.getRuntimeId());
            config.setRuntimeDependencies(lastConfigLoaded.getRuntimeDependencies());
            config.setExtensionDependencies(lastConfigLoaded.getExtensionDependencies());
            config.setBootRepositories(lastConfigLoaded.getBootRepositories());
            config.setJavaCommand(lastConfigLoaded.getJavaCommand());
            config.setJavaOptions(lastConfigLoaded.getJavaOptions());
            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
            config.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
            config.setStoreLocations(lastConfigLoaded.getStoreLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getStoreLocations()));
            config.setHomeLocations(lastConfigLoaded.getHomeLocations() == null ? null : new LinkedHashMap<>(lastConfigLoaded.getHomeLocations()));
        }
        if (PrivateNutsUtils.isBlank(config.getName())) {
            config.setName(PrivateNutsUtils.resolveValidWorkspaceName(workspace0));
        }
        Map<String, String> homeLocations = config.getHomeLocations();
        if (config.getStoreLocationStrategy() == null) {
            config.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
        }
        if (config.getRepositoryStoreLocationStrategy() == null) {
            config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
        }

        String workspace = config.getWorkspace();
        String[] homes = new String[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            homes[type.ordinal()] = NutsPlatformUtils.getPlatformHomeFolder(config.getStoreLocationLayout(), type, homeLocations,
                    config.isGlobal(), config.getName());
            if (PrivateNutsUtils.isBlank(homes[type.ordinal()])) {
                throw new NutsIllegalArgumentException(null, "Missing Home for " + type.name().toLowerCase());
            }
        }
        NutsStoreLocationStrategy storeLocationStrategy = config.getStoreLocationStrategy();
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Map<String, String> storeLocations = config.getStoreLocations() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(config.getStoreLocations());
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            switch (location) {
                default: {
                    if (PrivateNutsUtils.isBlank(storeLocations.get(typeId))) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, (workspace + File.separator + location.name().toLowerCase()));
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()]);
                                break;
                            }
                        }
                    } else if (!PrivateNutsUtils.isAbsolutePath(storeLocations.get(typeId))) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, (workspace + File.separator + location.name().toLowerCase()));
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()] + PrivateNutsUtils.syspath("/" + storeLocations.get(typeId)));
                                break;
                            }
                        }
                    }

                }
            }
        }
        config.setStoreLocations(storeLocations);
        return config;
    }

    public String getRequiredBootVersion() {
        return requiredBootVersion;
    }

    public String getRequiredJavaOptions() {
        return requiredJavaOptions;
    }

    public String getRequiredJavaCommand() {
        return requiredJavaCommand;
    }

    private int checkRequirements(boolean insatistfiedOnly) {
        int req = 0;
        if (!PrivateNutsUtils.isBlank(requiredBootVersion)) {
            if ((insatistfiedOnly && !requiredBootVersion.equals(actualVersion)) || !insatistfiedOnly) {
                req += 1;
            }
        }
        if (!PrivateNutsUtils.isBlank(requiredJavaCommand)) {
            if ((insatistfiedOnly && PrivateNutsUtils.isActualJavaCommand(requiredJavaCommand)) || !insatistfiedOnly) {
                req += 2;
            }
        }
        if (!PrivateNutsUtils.isBlank(requiredJavaOptions)) {
            if ((insatistfiedOnly && PrivateNutsUtils.isActualJavaOptions(requiredJavaOptions)) || !insatistfiedOnly) {
                req += 4;
            }
        }
        return req;
    }

    public String getRequirementsHelpString(boolean insatistfiedOnly) {
        int req = insatistfiedOnly ? newInstanceRequirements : checkRequirements(false);
        StringBuilder sb = new StringBuilder();
        if ((req & 1) != 0) {
            sb.append("Nuts Version ").append(requiredBootVersion);
        }
        if ((req & 2) != 0) {
            sb.append("Java Command ").append(requiredJavaCommand);
        }
        if ((req & 4) != 0) {
            sb.append("Java Options ").append(requiredJavaCommand);
        }
        if (sb.length() > 0) {
            sb.insert(0, "Required ");
            return sb.toString();
        }
        return null;
    }

    static class NutsBootClassLoader extends URLClassLoader {

        NutsBootClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }

    private static class OpenWorkspaceData {

        PrivateNutsBootConfig cfg = null;
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
    }

    private static class NutsBootWorkspaceFactoryComparator implements Comparator<NutsBootWorkspaceFactory> {

        private final NutsWorkspaceOptions options;

        public NutsBootWorkspaceFactoryComparator(NutsWorkspaceOptions options) {
            this.options = options;
        }

        @Override
        public int compare(NutsBootWorkspaceFactory o1, NutsBootWorkspaceFactory o2) {
            //sort by reverse order!
            return Integer.compare(o2.getBootSupportLevel(options), o1.getBootSupportLevel(options));
        }
    }
}
