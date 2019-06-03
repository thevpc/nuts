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
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class NutsBootWorkspace {

    public static final Logger LOG = Logger.getLogger(NutsBootWorkspace.class.getName());
    private final long creationTime = System.currentTimeMillis();
    private NutsWorkspaceOptions options;
    //    private String home;
    private String runtimeSourceURL;
    private String runtimeId;
    private String actualVersion;
    private NutsClassLoaderProvider contextClassLoaderProvider;
    private int newInstanceRequirements;
    private NutsBootConfig loadedBootConfig;
    private NutsBootConfig runningBootConfig;
    private String requiredJavaCommand;
    private String requiredBootVersion;
    private String requiredJavaOptions;
    private NutsIdLimited bootId;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return runningBootConfig.getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "home.programs":
                case "home.config":
                case "home.lib":
                case "home.temp":
                case "home.var":
                case "home.cache":
                case "home.logs":
                    return getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()));
                case "programs":
                case "config":
                case "lib":
                case "cache":
                case "temp":
                case "logs":
                case "var":
                    return runningBootConfig.getStoreLocation(NutsStoreLocation.valueOf(from.toUpperCase()));
            }
            return "${" + from + "}";
        }
    };
    private static final String DELETE_FOLDERS_HEADER = " ATTENTION ! You are about to delete workspace files.";

    public NutsBootWorkspace(String... args) {
        this(NutsArgumentsParser.parseNutsArguments(args));
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(creationTime);
        }
        actualVersion = Nuts.getVersion();
        this.options = options;
        this.bootId = NutsIdLimited.parse(NutsConstants.Ids.NUTS_API + "#" + actualVersion);
        newInstanceRequirements = 0;
        runningBootConfig = new NutsBootConfig(options);
        NutsLogUtils.bootstrap(options.getLogConfig());
        LOG.log(Level.CONFIG, "Open Nuts Workspace : {0}", options.format().getBootCommandLine());
        LOG.log(Level.CONFIG, "Open Nuts Workspace (compact) : {0}", options.format().compact().getBootCommandLine());
        loadedBootConfig = expandAllPaths(runningBootConfig);
        NutsLogUtils.prepare(options.getLogConfig(), NutsUtilsLimited.syspath(runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS) + "/net/vpc/app/nuts/nuts/" + actualVersion));

        requiredBootVersion = options.getRequiredBootVersion();
        if (requiredBootVersion == null) {
            requiredBootVersion = loadedBootConfig.getApiVersion();
        }
        requiredJavaCommand = options.getBootJavaCommand();
        if (requiredJavaCommand == null) {
            requiredJavaCommand = loadedBootConfig.getJavaCommand();
        }
        requiredJavaOptions = options.getBootJavaOptions();
        if (requiredJavaOptions == null) {
            requiredJavaOptions = loadedBootConfig.getJavaOptions();
        }
        newInstanceRequirements = checkRequirements(true);
        if (newInstanceRequirements == 0) {
            runningBootConfig.setApiVersion(actualVersion);
            runningBootConfig.setJavaCommand(null);
            runningBootConfig.setJavaOptions(null);
        }
        this.runtimeSourceURL = options.getBootRuntimeSourceURL();
        this.runtimeId = NutsUtilsLimited.isBlank(options.getBootRuntime()) ? null : NutsIdLimited.parse(options.getBootRuntime()).toString();
        this.contextClassLoaderProvider = options.getClassLoaderProvider() == null ? NutsDefaultClassLoaderProvider.INSTANCE : options.getClassLoaderProvider();
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
                releaseVersion = NutsUtilsLimited.resolveMavenReleaseVersion(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT, NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException(null, "Unable to load nuts version from " + NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }

        String defaultWorkspaceLibFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        if (!NutsUtilsLimited.getSystemBoolean("nuts.export.no-m2", false)) {
            repos.add(System.getProperty("user.home") + NutsUtilsLimited.syspath("/.m2/repository"));
        }
        repos.addAll(Arrays.asList(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
        ));
        File file = NutsUtilsLimited.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion,
                repos.toArray(new String[0]),
                loadedBootConfig.getBootsrap()//defaultWorkspaceLibFolder
        );
        if (file == null) {
            errors.append("Unable to load ").append(bootId).append("\n");
            showError(
                    runningBootConfig,
                    new NutsBootConfig()
                            .setApiVersion(requiredBootVersion)
                            .setRuntimeId(null)
                            .setJavaCommand(requiredJavaCommand)
                            .setJavaOptions(requiredJavaCommand),
                    options.getWorkspace(), null,
                    errors.toString()
            );
            throw new NutsIllegalArgumentException(null, "Unable to load " + NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion);
        }

        List<String> cmd = new ArrayList<>();
        String jc = requiredJavaCommand;
        if (jc == null || jc.trim().isEmpty()) {
            jc = NutsUtilsLimited.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NutsUtilsLimited.parseCommandLine(options.getBootJavaOptions())) {
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

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private void openWorkspaceAttempt(OpenWorkspaceData info, boolean recover) {
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        info.bootConfig0 = runningBootConfig.copy();
        if (recover) {
            if (!NutsUtilsLimited.isBlank(info.bootConfig0.getRuntimeId())
                    && !NutsUtilsLimited.isBlank(info.bootConfig0.getRuntimeDependencies())) {
                LOG.log(Level.CONFIG, "[RECOV. ] Invalidating old  runtime.");
            }
            info.bootConfig0.setRuntimeId(null);
            info.bootConfig0.setRuntimeDependencies(null);
        }
        if (!NutsUtilsLimited.isBlank(info.bootConfig0.getApiVersion()) && !NutsUtilsLimited.isBlank(info.bootConfig0.getRuntimeId()) && !NutsUtilsLimited.isBlank(info.bootConfig0.getRuntimeDependencies())) {
            //Ok
        } else {
            info.bootConfig0 = buildNutsBootConfig(info.bootConfig0, recover);
        }
        if (info.bootConfig0 != null && !actualVersion.equals(info.bootConfig0.getApiVersion())) {
            LOG.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{info.bootConfig0.getApiVersion(), actualVersion});
            info.actualBootConfig = buildNutsBootConfig(info.bootConfig0, recover);
        } else {
            info.actualBootConfig = info.bootConfig0;
        }

        if (info.actualBootConfig == null) {
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load ClassPath");
        }

        String workspaceBootLibFolder = runningBootConfig.getBootsrap();
        NutsIdLimited bootRuntime;
        if (NutsUtilsLimited.isBlank(info.actualBootConfig.getRuntimeId())) {
            bootRuntime = NutsIdLimited.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        } else if (info.actualBootConfig.getRuntimeId().contains("#")) {
            bootRuntime = NutsIdLimited.parse(info.actualBootConfig.getRuntimeId());
        } else {
            bootRuntime = NutsIdLimited.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        }
        String[] repositories = NutsUtilsLimited.splitUrlStrings(info.actualBootConfig.getRepositories()).toArray(new String[0]);
        File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootLibFolder, !recover);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + bootRuntime + ". Unable to resolve file " + getFileName(bootRuntime, "jar"));
        }

        allExtensionFiles.put(info.actualBootConfig.getRuntimeId(), f);
        for (String idStr : NutsUtilsLimited.split(info.actualBootConfig.getRuntimeDependencies(), "\n\t ;,")) {
            NutsIdLimited id = NutsIdLimited.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, !recover);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        info.bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
        LOG.log(Level.CONFIG, "Loading Nuts ClassWorld from {0} jars : {1}", new Object[]{info.bootClassWorldURLs.length, Arrays.asList(info.bootClassWorldURLs)});
        if (LOG.isLoggable(Level.CONFIG)) {
            for (URL bootClassWorldURL : info.bootClassWorldURLs) {
                LOG.log(Level.CONFIG, "\t {0}", new Object[]{NutsUtilsLimited.formatURL(bootClassWorldURL)});
            }
        }
        info.workspaceClassLoader = info.bootClassWorldURLs.length == 0 ? getContextClassLoader() : new URLClassLoader(info.bootClassWorldURLs, getContextClassLoader());

        ServiceLoader<NutsWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsWorkspaceFactory.class, info.workspaceClassLoader);

        NutsWorkspaceFactory factoryInstance = null;
        for (NutsWorkspaceFactory a : serviceLoader) {
            factoryInstance = a;
            info.nutsWorkspace = a.createSupported(NutsWorkspace.class, this);
            break;
        }
        if (info.nutsWorkspace == null) {
            //should never happen
            System.err.print("Unable to load Workspace Component from ClassPath : \n");
            for (URL url : info.bootClassWorldURLs) {
                System.err.printf("\t %s%n", NutsUtilsLimited.formatURL(url));
            }
            LOG.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(info.bootClassWorldURLs));
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load Workspace Component from ClassPath : " + Arrays.asList(info.bootClassWorldURLs));
        }
        ((NutsWorkspaceSPI) info.nutsWorkspace).initializeWorkspace(factoryInstance, info.actualBootConfig, info.bootConfig0,
                info.bootClassWorldURLs,
                info.workspaceClassLoader, options.copy());
        if (recover) {
//            info.nutsWorkspace.getConfigManager().setBootConfig(new NutsBootConfig());
            if (!info.nutsWorkspace.config().isReadOnly()) {
                info.nutsWorkspace.config().save();
            }
        }
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
                runWorkspaceCommand(null, "Cannot start workspace to run command " + getOptions().getBootCommand() + ". Try --clean or --reset to help recovering :" + ex.toString());
            } catch (Exception e) {
                System.err.println(e.toString());
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

    private static class OpenWorkspaceData {

        NutsBootConfig bootConfig0 = null;
        NutsBootConfig actualBootConfig = null;
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
    }

    public NutsWorkspace openWorkspace() {
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        switch (this.getOptions().getBootCommand()) {
            case RECOVER: {
                deleteStoreLocations(null, true, false, NutsStoreLocation.CACHE, NutsStoreLocation.TEMP);
                break;
            }
            case RESET: {
                deleteStoreLocations(null, true, true, NutsStoreLocation.values());
                break;
            }
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Open Workspace with command line  : {0}", options.format().getBootCommandLine());
            LOG.log(Level.CONFIG, "Open Workspace with config        : ");
            LOG.log(Level.CONFIG, "\t nuts-api-version               : {0}", actualVersion);
            LOG.log(Level.CONFIG, "\t nuts-workspace                 : {0}", NutsUtilsLimited.formatLogValue(options.getWorkspace(), runningBootConfig.getWorkspace()));
            LOG.log(Level.CONFIG, "\t nuts-store-strategy            : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocationStrategy(), runningBootConfig.getStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-repos-store-strategy      : {0}", NutsUtilsLimited.formatLogValue(options.getRepositoryStoreLocationStrategy(), runningBootConfig.getRepositoryStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-store-layout              : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocationLayout(), runningBootConfig.getStoreLocationLayout()));
            LOG.log(Level.CONFIG, "\t nuts-store-programs            : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.PROGRAMS), runningBootConfig.getStoreLocation(NutsStoreLocation.PROGRAMS)));
            LOG.log(Level.CONFIG, "\t nuts-store-config              : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.CONFIG), runningBootConfig.getStoreLocation(NutsStoreLocation.CONFIG)));
            LOG.log(Level.CONFIG, "\t nuts-store-var                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.VAR), runningBootConfig.getStoreLocation(NutsStoreLocation.VAR)));
            LOG.log(Level.CONFIG, "\t nuts-store-logs                : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.LOGS), runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS)));
            LOG.log(Level.CONFIG, "\t nuts-store-temp                : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.TEMP), runningBootConfig.getStoreLocation(NutsStoreLocation.TEMP)));
            LOG.log(Level.CONFIG, "\t nuts-store-cache               : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.CACHE), runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE)));
            LOG.log(Level.CONFIG, "\t nuts-store-lib                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.LIB), runningBootConfig.getStoreLocation(NutsStoreLocation.LIB)));
            LOG.log(Level.CONFIG, "\t option-read-only               : {0}", options.isReadOnly());
            LOG.log(Level.CONFIG, "\t option-open-mode               : {0}", options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode());
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
                openWorkspaceAttempt(info, options.getBootCommand() == NutsBootCommand.RECOVER);
            } catch (NutsException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (options.getBootCommand() == NutsBootCommand.RECOVER) {
                    throw ex;
                }
                info = new OpenWorkspaceData();
                try {
                    openWorkspaceAttempt(info, true);
                } catch (Throwable ex2) {
                    throw ex;
                }
            }
            return info.nutsWorkspace;
        } catch (NutsReadOnlyException | NutsUserCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            if (info.bootConfig0 == null) {
                info.bootConfig0 = new NutsBootConfig()
                        .setApiVersion(this.bootId.getVersion())
                        .setRuntimeId(runtimeId);
            }
            if (info.actualBootConfig == null) {
                info.actualBootConfig = new NutsBootConfig()
                        .setApiVersion(this.actualVersion)
                        .setRuntimeId(runtimeId);
            }
            showError(
                    info.actualBootConfig,
                    info.bootConfig0,
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
                        //
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    private String[] resolveBootClassPathRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
//        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (!NutsUtilsLimited.getSystemBoolean("nuts.export.no-m2", false)) {
            initial.add(NutsConstants.BootstrapURLs.LOCAL_MAVEN_CENTRAL);
        }
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add("${workspace}/" + NutsConstants.Folders.REPOSITORIES + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME + "/" + NutsConstants.Folders.LIB);
        initial.add(NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT);
        initial.add(NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL);
        initial.add(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT);
        return NutsUtilsLimited.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private static void resolveRuntimeDependencies(String runtimeVersion, NutsBootConfig recipient) {
        List<String> dependencies = new ArrayList<>();
        try {
            InputStream xml = null;
            String bestVersion = null;
            String urlPath = "net/vpc/app/nuts/nuts-core/" + runtimeVersion + "/nuts-core-" + runtimeVersion + ".pom";
            if (!NutsUtilsLimited.getSystemBoolean("nuts.export.no-m2", false)) {
                File mavenNutsCorePom = new File(System.getProperty("user.home"), (".m2/repository/" + urlPath).replace("/", File.separator));
                if (mavenNutsCorePom.isFile()) {
                    xml = Files.newInputStream(mavenNutsCorePom.toPath());
                }
            }
            if (xml == null) {
                xml = new URL("https://raw.githubusercontent.com/thevpc/vpc-public-maven/master/" + urlPath).openStream();
            }
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
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
                            if (NutsUtilsLimited.isBlank(groupId)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty groupId");
                            } else if (groupId.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in groupId=" + groupId);
                            }
                            if (NutsUtilsLimited.isBlank(artifactId)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty artifactId");
                            } else if (artifactId.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + artifactId);
                            }
                            if (NutsUtilsLimited.isBlank(version)) {
                                throw new NutsIllegalArgumentException(null, "Unexpected empty artifactId");
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + version);
                            }
                            if (NutsUtilsLimited.isBlank(scope) || scope.equals("compile")) {
                                dependencies.add(groupId + ":" + artifactId + "#" + version);
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(null, "Unexpected maven variable in artifactId=" + version);
                            }
                        }
                    }
                    recipient.setRuntimeDependencies(NutsUtilsLimited.join(";", dependencies.toArray(new String[0])));
                } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            switch (c3.getNodeName()) {
                                case "nuts-runtime-repositories": {
                                    String t = c3.getTextContent().trim();
                                    String e = recipient.getRepositories();
                                    if (t.length() > 0) {
                                        if (NutsUtilsLimited.isBlank(e)) {
                                            e = t;
                                        } else {
                                            e = t + ";" + e;
                                        }
                                        recipient.setRepositories(e);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
            }
        } catch (Exception ex) {
            //
        }
    }

    /**
     * find latest runtime version for the API version provided
     *
     * @param apiId api version
     * @return latest runtime version
     */
    private static String resolveLatestRuntimeId(String apiId) {
        String bestVersion = null;
        if (!NutsUtilsLimited.getSystemBoolean("nuts.export.no-m2", false)) {
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
                                    if (bestVersion == null || NutsUtilsLimited.compareRuntimeVersion(bestVersion, p) < 0) {
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
                                        if (bestVersion == null || NutsUtilsLimited.compareRuntimeVersion(bestVersion, p) < 0) {
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
            //
        }
        if (bestVersion == null) {
            return null;
        }
        return NutsConstants.Ids.NUTS_RUNTIME + "#" + bestVersion;
    }

    private NutsBootConfig buildNutsBootConfig(NutsBootConfig bootConfig0, boolean recover) {
        NutsIdLimited apiId = NutsUtilsLimited.isBlank(bootConfig0.getApiVersion()) ? bootId : new NutsIdLimited(bootId.getGroupId(), bootId.getArtifactId(), bootConfig0.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");
        String runtimeId = bootConfig0.getRuntimeId();
        if (NutsUtilsLimited.isBlank(runtimeId)) {
            runtimeId = resolveLatestRuntimeId(apiId.getVersion());
        }
        if (NutsUtilsLimited.isBlank(runtimeId)) {
            runtimeId = NutsConstants.Ids.NUTS_RUNTIME + "#" + apiId.getVersion() + ".0";
            LOG.log(Level.CONFIG, "[ERROR  ] Failed to load latest runtime id. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        NutsIdLimited _runtimeId = NutsIdLimited.parse(runtimeId);
        NutsBootConfig goodCp = bootConfig0.copy();
        goodCp.setApiVersion(apiId.getVersion());
        goodCp.setRuntimeId(runtimeId);
        resolveRuntimeDependencies(_runtimeId.getVersion(), goodCp);
        goodCp.setRepositories(
                NutsUtilsLimited.join(";",
                        NutsUtilsLimited.splitAndRemoveDuplicates(goodCp.getRepositories(), NutsUtilsLimited.join(";",
                                new String[]{
                                    NutsConstants.BootstrapURLs.LOCAL_MAVEN_CENTRAL,
                                    NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                                    NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL

                                }
                        )))
        );
        return goodCp;
    }

    protected String getHome(NutsStoreLocation storeFolder) {
        return NutsPlatformUtils.resolveHomeFolder(runningBootConfig.getStoreLocationLayout(), storeFolder, runningBootConfig.getHomeLocations(), runningBootConfig.isGlobal());
    }

    protected String expandPath(String path, String base) {
//        String home = this.runningBootConfig.getHome();
//        if (path.startsWith(home + File.separator)) {
//            path = home + File.separator + path.substring(home.length() + 1);
//        }
        path = NutsUtilsLimited.replaceDollarString(path, pathExpansionConverter);
        if (NutsUtilsLimited.isURL(path)) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (path.startsWith("~~/") || path.startsWith("~~\\")) {
            path = getHome(NutsStoreLocation.CONFIG) + path.substring(1);
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
        if (NutsUtilsLimited.isURL(url)) {
            return new URL(url);
        }
        return new File(url).toURI().toURL();
    }

    private File getBootFile(NutsIdLimited vid, String fileName, String[] repositories, String cacheFolder, boolean useCache) {
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
        base = expandPath(base, runningBootConfig.getWorkspace());
        try {
            if (NutsUtilsLimited.isURL(base)) {
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

    private String getFileName(NutsIdLimited id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    private String getPathFile(NutsIdLimited id, String name) {
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
        if (NutsUtilsLimited.isURL(repository)) {
            try {
                localFile = NutsUtilsLimited.toFile(new URL(repository));
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
                NutsUtilsLimited.copy(new URL(urlPath), to);
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
                String toc = NutsUtilsLimited.getAbsolutePath(to.getPath());
                String ffc = NutsUtilsLimited.getAbsolutePath(ff.getPath());
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
                        NutsUtilsLimited.copy(ff, to);
                        LOG.log(Level.CONFIG, "[RECOV. ] Cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    } else {
                        NutsUtilsLimited.copy(ff, to);
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

    private String expandWorkspacePath(String workspace) {
        if (NutsUtilsLimited.isBlank(workspace)) {
            workspace = NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String uws = workspace.replace('\\', '/');
        if (workspace.equals("~")) {
            throw new NutsIllegalArgumentException(null, "Workspace can not span over hole user home");
        } else if (workspace.equals("~~")) {
            throw new NutsIllegalArgumentException(null, "Workspace can not span over hole nuts home");
        } else if (uws.startsWith("~/")) {
            return System.getProperty("user.home") + File.separator + workspace.substring(2);
        } else if (!uws.equals(".") && !uws.equals("..") && uws.indexOf('/') < 0) {
            String home = getHome(NutsStoreLocation.CONFIG);
            if (home == null) {
                throw new NutsIllegalArgumentException(null, "Null Home");
            }
            return home + File.separator + workspace;
        } else if (uws.startsWith("~~/")) {
            String home = getHome(NutsStoreLocation.CONFIG);
            if (home == null) {
                throw new NutsIllegalArgumentException(null, "Null Home");
            }
            return home + File.separator + workspace.substring(3);
        } else {
            return NutsUtilsLimited.getAbsolutePath(workspace);
        }
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

    private void runWorkspaceCommand(NutsWorkspace workspace, String message) {
        NutsWorkspaceOptions o = this.getOptions();
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
//        switch (o.getBootCommand()) {
//            case RESET: {
//                if (LOG.isLoggable(Level.SEVERE)) {
//                    LOG.log(Level.SEVERE, message);
//                }
//                deleteStoreLocations(workspace, true, true, NutsStoreLocation.values());
//                if (getOptions().getApplicationArguments().length == 0) {
//                    return;
//                }
//                //o.setBootCommand(NutsBootCommand.EXEC);
//            }
//            case RECOVER: {
//                if (LOG.isLoggable(Level.SEVERE)) {
//                    LOG.log(Level.SEVERE, message);
//                }
//                deleteStoreLocations(workspace, true, false, NutsStoreLocation.CACHE, NutsStoreLocation.TEMP);
//                if (getOptions().getApplicationArguments().length == 0) {
//                    return;
//                }
////                o.setBootCommand(NutsBootCommand.EXEC);
//            }
//        }
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
                case "version": {
                    System.out.println("nuts-version :" + actualVersion);
                    return;
                }
                case "help": {

                    System.out.println("Nuts is a package manager mainly for java applications.");
                    System.err.println("Unluckily it was unable to locate nuts-core component which esessential for its execution.\n");
                    System.out.println("nuts-version :" + actualVersion);
                    System.out.println("Try to reinstall nuts (with internet access available) and type 'nuts help' to get a list of global options and commands");

                    return;
                }
            }
        }
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsExecutionException(null, "Workspace boot command not available : " + o.getBootCommand(), 1);
        }
        if (o.getApplicationArguments().length == 0) {
            workspace.exec().command("welcome").failFast().run();
            return;
        }
        workspace.exec()
                .command(o.getApplicationArguments())
                .executorOptions(o.getExecutorOptions())
                .executionType(o.getExecutionType())
                .failFast()
                .run();
    }
//
//    private void actionReset(NutsWorkspace workspace, String[] readArguments) {
//        NutsWorkspaceOptions o = getOptions();
//        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.config();
//        boolean force = false;
//        Set<NutsStoreLocation> toDelete = new HashSet();
//
//        for (String argument : readArguments) {
//            if ("-f".equals(argument) || "--force".equals(argument)) {
//                force = true;
//            } else {
//                if (!argument.startsWith("-")) {
//                    NutsStoreLocation z = null;
//                    try {
//                        z = NutsStoreLocation.valueOf(argument.trim().toUpperCase());
//                    } catch (Exception ex) {
//                        //ignore
//                    }
//                    if (z != null) {
//                        toDelete.add(z);
//                    } else {
//                        switch (argument) {
//                            case "soft":
//                            case "cleanup": {
//                                toDelete.add(NutsStoreLocation.CACHE);
//                                toDelete.add(NutsStoreLocation.TEMP);
//                                toDelete.add(NutsStoreLocation.LOGS);
//                                break;
//                            }
//                            case "all": {
//                                toDelete.addAll(Arrays.asList(NutsStoreLocation.values()));
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (toDelete.isEmpty()) {
//            toDelete.add(NutsStoreLocation.CACHE);
//            toDelete.add(NutsStoreLocation.TEMP);
//            toDelete.add(NutsStoreLocation.LOGS);
//        }
//        deleteStoreLocations(workspace, force, true, true, toDelete.toArray(new NutsStoreLocation[0]));
//    }
//

    private void deleteStoreLocations(NutsWorkspace workspace, boolean includeBoot, boolean includeRoot, NutsStoreLocation... locations) {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Deleting Workspace locations : {0}", runningBootConfig.getWorkspace());
        }
        boolean force = false;
        NutsWorkspaceOptions o = getOptions();
        NutsConfirmationMode confirm = o.getConfirm() == null ? NutsConfirmationMode.ASK : o.getConfirm();
        switch (confirm) {
            case ASK: {
                break;
            }
            case YES: {
                force = true;
                break;
            }
            case NO:
            case CANCEL: {
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
            for (NutsStoreLocation value : locations) {
                folders.add(new File(runningBootConfig.getStoreLocation(value)));
            }
        }
        String header = DELETE_FOLDERS_HEADER;
        NutsUtilsLimited.deleteAndConfirmAll(folders.toArray(new File[0]), force, header,
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

    public void showError(NutsBootConfig actualBootConfig, NutsBootConfig workspaceConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage) {
        System.err.printf("Unable to bootstrap Nuts. : %s%n", extraMessage);
        System.err.printf("Here after current environment info:%n");
        System.err.printf("  nuts-boot-api-version            : %s%n", actualBootConfig.getApiVersion() == null ? "<?> Not Found!" : actualBootConfig.getApiVersion());
        System.err.printf("  nuts-boot-runtime                : %s%n", actualBootConfig.getRuntimeId() == null ? "<?> Not Found!" : actualBootConfig.getRuntimeId());
        System.err.printf("  nuts-workspace-api-version       : %s%n", workspaceConfig.getApiVersion() == null ? "<?> Not Found!" : workspaceConfig.getApiVersion());
        System.err.printf("  nuts-workspace-runtime           : %s%n", workspaceConfig.getRuntimeId() == null ? "<?> Not Found!" : workspaceConfig.getRuntimeId());
        System.err.printf("  nuts-store-strategy              : %s%n", workspaceConfig.getStoreLocationStrategy());
        System.err.printf("  nuts-store-layout                : %s%n", workspaceConfig.getStoreLocationLayout());
        System.err.printf("  nuts-store-programs              : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.PROGRAMS));
        System.err.printf("  nuts-store-config                : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.CONFIG));
        System.err.printf("  nuts-store-var                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.VAR));
        System.err.printf("  nuts-store-logs                  : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.LOGS));
        System.err.printf("  nuts-store-temp                  : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.TEMP));
        System.err.printf("  nuts-store-config                : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.CACHE));
        System.err.printf("  nuts-store-lib                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.LIB));
        System.err.printf("  workspace-location               : %s%n", (workspace == null ? "<default-location>" : workspace));
        System.err.printf("  nuts-boot-args                   : %s%n", options.format().getBootCommandLine());
        System.err.printf("  nuts-app-args                    : %s%n", Arrays.toString(options.getApplicationArguments()));
        System.err.printf("  option-read-only                 : %s%n", options.isReadOnly());
        System.err.printf("  option-open-mode                 : %s%n", options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode());
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            System.err.printf("  nuts-runtime-classpath           : %s%n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    System.err.printf("  nuts-runtime-classpath           : %s%n", NutsUtilsLimited.formatURL(bootClassWorldURL));
                } else {
                    System.err.printf("                                     %s%n", NutsUtilsLimited.formatURL(bootClassWorldURL));
                }
            }
        }
        System.err.printf("  java-version                     : %s%n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s%n", NutsUtilsLimited.resolveJavaCommand(null));
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
        System.err.printf("You may also enable recover mode to ignore existing cache info with '--recover' argument.%n");
        System.err.printf("Here is the proper command : %n");
        System.err.printf("  java -jar nuts.jar --debug --recover [...]%n");
        System.err.printf("Now exiting Nuts, Bye!%n");
    }

    /**
     * resolves and expands paths of a given workspace
     *
     * @param config boot config. Should contain home,workspace, and all
     * StoreLocation information
     * @return resolved config
     */
    private NutsBootConfig expandAllPaths(NutsBootConfig config) {
        String ws = options.getWorkspace();
        int maxDepth = 36;
        NutsBootConfig lastConfigLoaded = null;
        String lastConfigPath = null;
        for (int i = 0; i < maxDepth; i++) {
            ws = expandWorkspacePath(ws);
            lastConfigPath = ws;
            NutsBootConfig configLoaded = NutsUtilsLimited.loadNutsBootConfig(ws);
            if (configLoaded == null) {
                //not loaded
                break;
            }
            if ((configLoaded.getWorkspace() == null || configLoaded.getWorkspace().isEmpty())) {
                lastConfigLoaded = configLoaded;
                break;
            }
            ws = configLoaded.getWorkspace();
            if (i == maxDepth - 1) {
                throw new NutsIllegalArgumentException(null, "Cyclic Workspace resolution");
            }
        }
        config.setWorkspace(lastConfigPath);
        if (lastConfigLoaded != null) {
            config.setWorkspace(lastConfigPath);
            config.setApiVersion(lastConfigLoaded.getApiVersion());
            config.setRuntimeId(lastConfigLoaded.getRuntimeId());
            config.setRuntimeDependencies(lastConfigLoaded.getRuntimeDependencies());
            config.setRepositories(lastConfigLoaded.getRepositories());
            config.setJavaCommand(lastConfigLoaded.getJavaCommand());
            config.setJavaOptions(lastConfigLoaded.getJavaOptions());
            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                config.setStoreLocation(type, lastConfigLoaded.getStoreLocation(type));
            }
            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
                for (NutsStoreLocation loc : NutsStoreLocation.values()) {
                    String homeLocation = lastConfigLoaded.getHomeLocation(layout, loc);
                    if (!NutsUtilsLimited.isBlank(homeLocation)) {
                        config.setHomeLocation(layout, loc, homeLocation);
                    } else if (options.getHomeLocation(layout, loc) != null) {
//                        System.err.println("runtime option "+llid+"="+options.getHomeLocation(layout, loc)+" is ignored");
                    }
                }
            }
        }
        String[] homeLocations = config.getHomeLocations();
        final NutsStoreLocationLayout storeLocationLayout = config.getStoreLocationLayout();
        if (storeLocationLayout == null) {
            config.setStoreLocationLayout(NutsStoreLocationLayout.values()[0]);
        }
        if (config.getStoreLocationStrategy() == null) {
            config.setStoreLocationStrategy(NutsStoreLocationStrategy.values()[0]);
        }
        {
            String workspace = config.getWorkspace();
            String[] homes = new String[NutsStoreLocation.values().length];
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                homes[type.ordinal()] = NutsPlatformUtils.resolveHomeFolder(storeLocationLayout, type, homeLocations, config.isGlobal());
                if (NutsUtilsLimited.isBlank(homes[type.ordinal()])) {
                    throw new NutsIllegalArgumentException(null, "Missing Home for " + type.name().toLowerCase());
                }
            }
            NutsStoreLocationStrategy storeLocationStrategy = config.getStoreLocationStrategy();
            if (storeLocationStrategy == null) {
                storeLocationStrategy = NutsStoreLocationStrategy.values()[0];
            }
            if (workspace.startsWith(homes[NutsStoreLocation.CONFIG.ordinal()])) {
                String w = workspace.substring(homes[NutsStoreLocation.CONFIG.ordinal()].length());
                while (w.startsWith("/") || w.startsWith("\\")) {
                    w = w.substring(1);
                }
                workspace = w;
            }
            String workspaceName = new File(workspace).getName();
            if (!workspace.contains("/") && !workspace.contains("\\")) {
                workspace = homes[NutsStoreLocation.CONFIG.ordinal()] + File.separator + workspace;
            }
            config.setWorkspace(workspace);
//            if (NutsUtilsLimited.isEmpty(config.getConfigStoreLocation())) {
//                config.setConfigStoreLocation(workspace + File.separator + NutsStoreFolder.CONFIG.name().toLowerCase());
//            } else if (!NutsUtilsLimited.isAbsolutePath(config.getConfigStoreLocation())) {
//                config.setConfigStoreLocation(workspace + File.separator + NutsUtilsLimited.syspath(config.getConfigStoreLocation()));
//            }
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                switch (type) {
                    default: {
                        if (NutsUtilsLimited.isBlank(config.getStoreLocation(type))) {
                            switch (storeLocationStrategy) {
                                case STANDALONE: {
                                    config.setStoreLocation(type, (workspace + File.separator + type.name().toLowerCase()));
                                    break;
                                }
                                case EXPLODED: {
                                    config.setStoreLocation(type, homes[type.ordinal()] + NutsUtilsLimited.syspath("/" + workspaceName + "/" + type.name().toLowerCase()));
                                    break;
                                }
                            }
                        } else if (!NutsUtilsLimited.isAbsolutePath(config.getStoreLocation(type))) {
                            switch (storeLocationStrategy) {
                                case STANDALONE: {
                                    config.setStoreLocation(type, (workspace + File.separator + type.name().toLowerCase()));
                                    break;
                                }
                                case EXPLODED: {
                                    config.setStoreLocation(type, homes[type.ordinal()] + NutsUtilsLimited.syspath("/" + workspaceName + "/" + config.getStoreLocation(type)));
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
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
        if (!NutsUtilsLimited.isBlank(requiredBootVersion)) {
            if ((insatistfiedOnly && !requiredBootVersion.equals(actualVersion)) || !insatistfiedOnly) {
                req += 1;
            }
        }
        if (!NutsUtilsLimited.isBlank(requiredJavaCommand)) {
            if ((insatistfiedOnly && NutsUtilsLimited.isActualJavaCommand(requiredJavaCommand)) || !insatistfiedOnly) {
                req += 2;
            }
        }
        if (!NutsUtilsLimited.isBlank(requiredJavaOptions)) {
            if ((insatistfiedOnly && NutsUtilsLimited.isActualJavaOptions(requiredJavaOptions)) || !insatistfiedOnly) {
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

}
