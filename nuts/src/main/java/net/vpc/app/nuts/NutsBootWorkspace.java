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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
 * {@link #openWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at $root/default-workspace/cache where $root
 * is the nuts root folder (~/.nuts) defined by
 * <pre>
 *   ~/.nuts/default-workspace/cache
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
 * <p>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-core.jar and its dependencies and for creating workspaces using
 * the method {@link #openWorkspace()}.
 * <p>
 * Created by vpc on 1/6/17.
 */
public class NutsBootWorkspace {

    public static final Logger log = Logger.getLogger(NutsBootWorkspace.class.getName());
    private final long creationTime = System.currentTimeMillis();
    private NutsWorkspaceOptions options;
    //    private String home;
    private String runtimeSourceURL;
    private String runtimeId;
    private String actualVersion;
    private NutsClassLoaderProvider contextClassLoaderProvider;
    private boolean newInstance;
    NutsBootConfig loadedBootConfig;
    NutsBootConfig runningBootConfig;
    String requiredBootVersion;
    String bootJavaCommand;
    String requiredJavaOptions;
    //    String workspaceLocation;
    NutsBootId bootId;

    public NutsBootWorkspace(String[] args) {
        this(NutsArgumentsParser.parseNutsArguments(args));
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions()
                    .setCreateIfNotFound(true)
                    .setSaveIfCreated(true);
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(creationTime);
        }
        actualVersion = getActualVersion();
        this.options = options;
        this.bootId = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_API + "#" + actualVersion);
        newInstance = true;
        if (options.getBootCommand() != null) {
            switch (options.getBootCommand()) {
                case UPDATE:
                case CHECK_UPDATES:
                case CLEAN:
                case RESET: {
                    newInstance = false;
                }
            }
        }
        runningBootConfig = new NutsBootConfig(options);
        NutsLogUtils.bootstrap(options.getLogConfig());
        log.log(Level.CONFIG, "Open Nus Workspace... : app=" + Arrays.toString(options.getApplicationArguments()) + " ; boot=" + Arrays.toString(options.getBootArguments()));
        loadedBootConfig = compile(runningBootConfig);
        NutsLogUtils.prepare(options.getLogConfig(), NutsUtils.syspath(runningBootConfig.getLogsStoreLocation() + "/log/net/vpc/app/nuts/nuts/LATEST"));

        requiredBootVersion = options.getRequiredBootVersion();
        if (requiredBootVersion == null) {
            requiredBootVersion = loadedBootConfig.getApiVersion();
        }
        bootJavaCommand = options.getBootJavaCommand();
        if (bootJavaCommand == null) {
            bootJavaCommand = loadedBootConfig.getJavaCommand();
        }
        requiredJavaOptions = options.getBootJavaOptions();
        if (requiredJavaOptions == null) {
            requiredJavaOptions = loadedBootConfig.getJavaOptions();
        }
        if ((requiredBootVersion == null || requiredBootVersion.trim().isEmpty() || requiredBootVersion.equals(actualVersion))
                && NutsUtils.isActualJavaCommand(bootJavaCommand)) {
            newInstance = false;
        }
        if (!newInstance) {
            runningBootConfig.setApiVersion(actualVersion);
            runningBootConfig.setJavaCommand(null);
            runningBootConfig.setJavaOptions(null);
        }
        this.runtimeSourceURL = options.getBootRuntimeSourceURL();
        this.runtimeId = NutsUtils.isEmpty(options.getBootRuntime()) ? null : NutsBootId.parse(options.getBootRuntime()).toString();
        this.contextClassLoaderProvider = options.getClassLoaderProvider() == null ? NutsDefaultClassLoaderProvider.INSTANCE : options.getClassLoaderProvider();
    }

    public boolean isRequiredNewProcess() {
        return newInstance;
    }

    public int startNewProcess() {
        log.fine("Running version " + actualVersion + ". Requested version " + requiredBootVersion);
        StringBuilder errors = new StringBuilder();
        if ("LATEST".equalsIgnoreCase(requiredBootVersion) || "RELEASE".equalsIgnoreCase(requiredBootVersion)) {
            String releaseVersion = null;
            try {
                releaseVersion = NutsUtils.resolveMavenReleaseVersion(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT, NutsConstants.NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }

        String defaultWorkspaceLibFolder = runningBootConfig.getLibStoreLocation();
        File file = NutsUtils.resolveOrDownloadJar(NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion,
                new String[]{
                        defaultWorkspaceLibFolder,
                        System.getProperty("user.home") + NutsUtils.syspath("/.m2/repository"),
                        NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                        NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL
                },
                defaultWorkspaceLibFolder
        );
        if (file == null) {
            errors.append("Unable to load ").append(bootId).append("\n");
            showError(
                    runningBootConfig,
                    new NutsBootConfig()
                            .setApiVersion(requiredBootVersion)
                            .setRuntimeId(null)
                            .setJavaCommand(bootJavaCommand)
                            .setJavaOptions(bootJavaCommand),
                    options.getHome(),
                    options.getWorkspace(), null,
                    errors.toString()
            );
            throw new NutsIllegalArgumentException("Unable to load " + NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion);
        }

        List<String> cmd = new ArrayList<>();
        String jc = bootJavaCommand;
        if (jc == null || jc.trim().isEmpty()) {
            jc = NutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NutsMinimalCommandLine.parseCommandLine(options.getBootJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        Collections.addAll(cmd, NutsMinimalCommandLine.parseCommandLine(options.getBootJavaOptions()));
//        cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        cmd.add("-jar");
        cmd.add(file.getPath());
        //cmd.add("--verbose");
        cmd.addAll(Arrays.asList(options.getBootArguments()));
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
        try {
            new ProcessBuilder(cmd).inheritIO().start().waitFor();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to start nuts", ex);
        }
        return 0;
    }

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private void openWorkspaceAttempt(OpenWorkspaceData info, boolean reset) {
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        info.bootConfig0 = runningBootConfig.copy();
        if (reset) {
            info.bootConfig0.setRuntimeId(null);
            info.bootConfig0.setRuntimeDependencies(null);
        }
        if (!NutsUtils.isEmpty(info.bootConfig0.getApiVersion()) && !NutsUtils.isEmpty(info.bootConfig0.getRuntimeId()) && !NutsUtils.isEmpty(info.bootConfig0.getRuntimeDependencies())) {
            //Ok
        } else {
            info.bootConfig0 = buildNutsBootConfig(info.bootConfig0, true);
        }
        if (info.bootConfig0 != null && !actualVersion.equals(info.bootConfig0.getApiVersion())) {
            log.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{info.bootConfig0.getApiVersion(), actualVersion});
            info.actualBootConfig = buildNutsBootConfig(info.bootConfig0, true);
        } else {
            info.actualBootConfig = info.bootConfig0;
        }

        if (info.actualBootConfig == null) {
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load ClassPath");
        }

        String workspaceBootLibFolder = runningBootConfig.getLibStoreLocation();
        NutsBootId bootRuntime = null;
        if (NutsUtils.isEmpty(info.actualBootConfig.getRuntimeId())) {
            bootRuntime = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        } else if (info.actualBootConfig.getRuntimeId().contains("#")) {
            bootRuntime = NutsBootId.parse(info.actualBootConfig.getRuntimeId());
        } else {
            bootRuntime = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        }
        String[] repositories = NutsUtils.splitUrlStrings(info.actualBootConfig.getRepositories()).toArray(new String[0]);
        File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootLibFolder, !reset);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load " + bootRuntime);
        }

        allExtensionFiles.put(info.actualBootConfig.getRuntimeId(), f);
        for (String idStr : NutsUtils.split(info.actualBootConfig.getRuntimeDependencies(), "\n\t ;,")) {
            NutsBootId id = NutsBootId.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, false);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        info.bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
        log.log(Level.CONFIG, "Loading Nuts ClassWorld from {0} jars : {1}", new Object[]{info.bootClassWorldURLs.length, Arrays.asList(info.bootClassWorldURLs)});
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
                System.err.printf("\t%s\n", url);
            }
            log.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(info.bootClassWorldURLs));
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load Workspace Component from ClassPath : " + Arrays.asList(info.bootClassWorldURLs));
        }
        ((NutsWorkspaceImpl) info.nutsWorkspace).initializeWorkspace(factoryInstance, info.actualBootConfig, info.bootConfig0,
                info.bootClassWorldURLs,
                info.workspaceClassLoader, options.copy().setIgnoreIfFound(true));
        if (reset) {
            info.nutsWorkspace.getConfigManager().setBootConfig(new NutsBootConfig());
            if (!info.nutsWorkspace.getConfigManager().isReadOnly()) {
                info.nutsWorkspace.getConfigManager().save();
            }
        }
    }

    public int run() throws IOException {
        switch (this.getOptions().getBootCommand()) {
            case CLEAN: {
                if (!new File(runningBootConfig.getWorkspace()).isDirectory()) {
                    return 0;
                }
                if (!Boolean.getBoolean("nut.workspace-clean")) {
                    return actionReset(null);
                }
                return actionClean(null);
            }
            case RESET: {
                if (!new File(runningBootConfig.getWorkspace()).isDirectory()) {
                    return 0;
                }
                if (!Boolean.getBoolean("nut.workspace-reset")) {
                    return actionReset(null);
                }
                break;
            }
        }

        if (isRequiredNewProcess()) {
            startNewProcess();
            return 0;
        }
//o.setCreationTime(startTime);
        NutsWorkspace workspace = null;
        try {
            workspace = this.openWorkspace();
        } catch (NutsException ex) {
            switch (this.getOptions().getBootCommand()) {
                case VERSION:
                case INFO:
                case HELP:
                case LICENSE: {
                    try {
                        runWorkspaceCommand(null, "Cannot start workspace to run command " + getOptions().getBootCommand() + ". " + ex.getMessage());
                        return 0;
                    } catch (NutsUserCancelException e) {
                        System.err.println(e.getMessage());
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    }
                }
            }
            throw ex;
        } catch (Throwable ex) {
            int x = 204;
            try {
                x = runWorkspaceCommand(null, "Cannot start workspace to run command " + getOptions().getBootCommand() + ". Try --clean or --reset to help recovering :" + ex.toString());
            } catch (Exception e) {
                System.err.println(e.toString());
            }
            if (ex instanceof RuntimeException) {
                throw ex;
            }
            if (ex instanceof Error) {
                throw ex;
            }
            throw new NutsExecutionException(ex.getMessage(), ex, x);
        }
        return runWorkspaceCommand(workspace, "Workspace started successfully");
    }

    private static class OpenWorkspaceData {

        NutsBootConfig bootConfig0 = null;
        NutsBootConfig actualBootConfig = null;
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
    }

    public NutsWorkspace openWorkspace() {
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Open Workspace with config :");
            log.log(Level.CONFIG, "\t nuts-boot-api                  : {0}", actualVersion);

            log.log(Level.CONFIG, "\t nuts-home                      : {0}", NutsUtils.desc(options.getHome()));
            log.log(Level.CONFIG, "\t nuts-home           (resolved) : {0}", runningBootConfig.getHome());

            log.log(Level.CONFIG, "\t nuts-workspace                 : {0}", NutsUtils.desc(options.getWorkspace()));
            log.log(Level.CONFIG, "\t nuts-workspace      (resolved) : {0}", runningBootConfig.getWorkspace());

            log.log(Level.CONFIG, "\t nuts-store-strategy            : {0}", NutsUtils.desc(options.getStoreLocationStrategy()));
            log.log(Level.CONFIG, "\t nuts-store-strategy (resolved) : {0}", runningBootConfig.getStoreLocationStrategy());

            log.log(Level.CONFIG, "\t nuts-store-layout              : {0}", NutsUtils.desc(options.getStoreLocationLayout()));
            log.log(Level.CONFIG, "\t nuts-store-layout   (resolved) : {0}", runningBootConfig.getStoreLocationLayout());

            log.log(Level.CONFIG, "\t nuts-store-programs            : {0}", NutsUtils.desc(options.getProgramsStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-programs (resolved) : {0}", runningBootConfig.getProgramsStoreLocation());

            log.log(Level.CONFIG, "\t nuts-store-config              : {0}", NutsUtils.desc(options.getConfigStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-config   (resolved) : {0}", runningBootConfig.getConfigStoreLocation());

            log.log(Level.CONFIG, "\t nuts-store-var                 : {0}", NutsUtils.desc(options.getVarStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-var      (resolved) : {0}", runningBootConfig.getVarStoreLocation());

            log.log(Level.CONFIG, "\t nuts-store-logs                : {0}", NutsUtils.desc(options.getLogsStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-logs     (resolved) : {0}", runningBootConfig.getLogsStoreLocation());

            log.log(Level.CONFIG, "\t nuts-store-temp                : {0}", NutsUtils.desc(options.getTempStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-temp     (resolved) : {0}", runningBootConfig.getTempStoreLocation());

            log.log(Level.CONFIG, "\t nuts-store-cache               : {0}", NutsUtils.desc(options.getCacheStoreLocation()));
            log.log(Level.CONFIG, "\t nuts-store-cache    (resolved) : {0}", runningBootConfig.getCacheStoreLocation());

            log.log(Level.CONFIG, "\t java-home                      : {0}", System.getProperty("java.home"));
            log.log(Level.CONFIG, "\t java-classpath                 : {0}", System.getProperty("java.class.path"));
            log.log(Level.CONFIG, "\t java-library-path              : {0}", System.getProperty("java.library.path"));
            log.log(Level.CONFIG, "\t os-name                        : {0}", System.getProperty("os.name"));
            log.log(Level.CONFIG, "\t os-arch                        : {0}", System.getProperty("os.arch"));
            log.log(Level.CONFIG, "\t os-version                     : {0}", System.getProperty("os.version"));
            log.log(Level.CONFIG, "\t user-dir                       : {0}", System.getProperty("user.dir"));
            log.log(Level.CONFIG, "\t user-home                      : {0}", System.getProperty("user.home"));
        }
        OpenWorkspaceData info = new OpenWorkspaceData();
        try {

            long startTime = options.getCreationTime();
            if (startTime == 0) {
                options.setCreationTime(startTime = System.currentTimeMillis());
            }
            if (!options.isCreateIfNotFound()) {
                //add fail fast test!!
                if (!new File(runningBootConfig.getWorkspace(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
                }
            }
            try {
                openWorkspaceAttempt(info, false);
            } catch (NutsException ex) {
                throw ex;
            } catch (Throwable ex) {
                info = new OpenWorkspaceData();
                try {
                    openWorkspaceAttempt(info, true);
                } catch (Throwable ex2) {
                    throw ex;
                }
            }
            return info.nutsWorkspace;
        } catch (NutsReadOnlyException ex) {
            throw ex;
        } catch (NutsUserCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            if (info.bootConfig0 == null) {
                info.bootConfig0 = new NutsBootConfig()
                        .setApiVersion(this.bootId.getVersion())
                        .setRuntimeId(runtimeId);
            }
            if (info.actualBootConfig == null) {
                info.actualBootConfig = new NutsBootConfig()
                        .setApiVersion(NutsConstants.NUTS_ID_BOOT_API + "#" + this.actualVersion)
                        .setRuntimeId(runtimeId);
            }
            showError(
                    info.actualBootConfig,
                    info.bootConfig0,
                    this.runningBootConfig.getHome(),
                    options.getWorkspace(),
                    info.bootClassWorldURLs,
                    ex.toString()
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            throw new NutsIllegalArgumentException("Unable to locate valid nuts-core components", ex);
        }
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
        initial.add("${workspace}/" + NutsConstants.FOLDER_NAME_REPOSITORIES + "/" + NutsConstants.DEFAULT_REPOSITORY_NAME + "/" + NutsConstants.FOLDER_NAME_LIB);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private NutsBootConfig buildNutsBootConfig(NutsBootConfig bootConfig0, boolean first) {
        String libFolder = runningBootConfig.getLibStoreLocation();
        NutsBootId apiId = NutsUtils.isEmpty(bootConfig0.getApiVersion()) ? bootId : new NutsBootId(bootId.getGroupId(), bootId.getArtifactId(), bootConfig0.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");
        String runtimeId = bootConfig0.getRuntimeId();
        String repositories = bootConfig0.getRepositories();
        List<String> resolvedBootRepositories = new ArrayList<>();
        if (NutsUtils.isEmpty(runtimeId) || NutsUtils.isEmpty(repositories)) {
            resolvedBootRepositories.add("${lib}");
            resolvedBootRepositories.addAll(Arrays.asList(NutsUtils.splitAndRemoveDuplicates(this.runtimeSourceURL, NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT)));
            for (String repo : resolvedBootRepositories) {
                URL urlString = buildURL(repo, bootAPIPropertiesPath);
                if (urlString != null) {
                    Properties wruntimeProperties = NutsUtils.loadURLProperties(urlString, new File(libFolder, bootAPIPropertiesPath.replace('/', File.separatorChar)));
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
        }

        if (NutsUtils.isEmpty(runtimeId)) {
            runtimeId = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + apiId.getVersion() + ".0";
            log.log(Level.CONFIG, "[ERROR  ] Failed to load boot props file from boot repositories. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        if (NutsUtils.isEmpty(repositories)) {
            repositories = "";
        }
        NutsBootId _runtimeId = NutsBootId.parse(runtimeId);
        List<NutsBootConfig> all = new ArrayList<>();

        resolvedBootRepositories.clear();
        resolvedBootRepositories.add("${lib}");
        resolvedBootRepositories.addAll(Arrays.asList(resolveBootConfigRepositories(/*repositories*/)));

        String bootRuntimePropertiesPath = getPathFile(_runtimeId, "nuts.properties");
        for (String u : resolvedBootRepositories) {
            NutsBootConfig cp = null;
            URL urlString = buildURL(u, bootRuntimePropertiesPath);
            if (urlString != null) {
                try {
                    Properties p = NutsUtils.loadURLProperties(urlString, new File(libFolder, NutsUtils.syspath(bootAPIPropertiesPath)));
                    if (p != null && !p.isEmpty()) {
                        cp = NutsUtils.createNutsBootConfig(p);
                        cp = bootConfig0.copy()
                                .setApiVersion(apiId.getVersion())
                                .setRuntimeId(cp.getRuntimeId())
                                .setRuntimeDependencies(cp.getRuntimeDependencies())
                                .setRepositories(cp.getRepositories());
                        //NutsUtils.storeProperties(p,new File(cacheFolder,bootPropertiesPath.replace('/',File.separatorChar)));
                    }
                } catch (Exception ex) {
                    log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", new Object[]{urlString});
                }
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", (u + "/" + bootRuntimePropertiesPath));
            }
            if (cp != null) {
                log.log(Level.CONFIG, "[SUCCESS] Loaded runtime id {0} from runtime props file {1}", new Object[]{cp.getRuntimeId(), u});
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
                runtimeVersion = NutsBootId.parse(this.runtimeId).version;
            } else {
                String bootAPI = apiId.toString();
                runtimeVersion = NutsBootId.parse(bootAPI).version + ".0";
                runtimeId0 = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + runtimeVersion;
            }
            log.log(Level.CONFIG, "Loading Default Runtime ClassPath {0}", runtimeVersion);
            String[] jarRepositories = {
                    libFolder,
                    NutsConstants.URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT
            };
            for (String repoURL : jarRepositories) {
                NutsBootId bootRuntimeNutsId = NutsBootId.parse(runtimeId0);
                String pomPath = getPathFile(bootRuntimeNutsId, getFileName(bootRuntimeNutsId, "pom"));
                URL urlString = buildURL(repoURL, pomPath);
                if (urlString != null) {
                    String[] bootNutsIds = NutsUtils.parseDependenciesFromMaven(urlString, new File(libFolder, NutsUtils.syspath(pomPath)));
                    if (bootNutsIds != null) {
                        NutsBootConfig bcc = bootConfig0.copy()
                                .setApiVersion(apiId.getVersion())
                                .setRuntimeId(runtimeId0)
                                .setRuntimeDependencies(NutsUtils.join(";", bootNutsIds))
                                .setRepositories(NutsUtils.join(";", jarRepositories));
                        all.add(bcc);

                        //cache boot-api     properties
                        Properties p = new Properties();
                        p.setProperty("project.id", apiId.getGroupId() + ":" + apiId.getArtifactId());
                        p.setProperty("project.version", apiId.getVersion());
                        p.setProperty("project.name", apiId.getGroupId() + ":" + apiId.getArtifactId());
                        p.setProperty("runtimeId", bootRuntimeNutsId.toString());
                        p.setProperty("repositories", bcc.getRepositories());
                        File cacheFile = new File(libFolder, NutsUtils.syspath(bootAPIPropertiesPath));
                        NutsUtils.storeProperties(p, cacheFile);
                        log.log(Level.CONFIG, "[CACHED ] Caching properties file {0}", new Object[]{cacheFile.getPath()});

                        //cache boot-runtime properties
                        p = new Properties();
                        p.setProperty("project.id", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
                        p.setProperty("project.version", bootRuntimeNutsId.getVersion());
                        p.setProperty("project.name", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
//                        p.setProperty("runtimeId",bootRuntimeNutsId.toString());
//                        p.setProperty("repositories",bcc.getRepositories());
                        p.setProperty("project.dependencies.compile", bcc.getRuntimeDependencies() == null ? "" : bcc.getRuntimeDependencies());
                        cacheFile = new File(libFolder, NutsUtils.syspath(bootRuntimePropertiesPath));
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
        repos2.add(cp.getRepositories());
        repos2.addAll(resolvedBootRepositories);
        String repositoriesToStore = NutsUtils.join(";",
                resolveBootClassPathRepositories(
                        this.runningBootConfig.getWorkspace(),
                        repos2.toArray(new String[0])
                )
        );

        cp.setRepositories(repositoriesToStore);
        return cp;
    }

    protected String expandPath(String path, String base) {
//        String home = this.runningBootConfig.getHome();
//        if (path.startsWith(home + File.separator)) {
//            path = home + File.separator + path.substring(home.length() + 1);
//        }
        path = NutsUtils.replaceDollarString(path, new NutsObjectConverter<String, String>() {
            @Override
            public String convert(String from) {
                switch (from) {
                    case "home":
                        return runningBootConfig.getHome();
                    case "workspace":
                        return runningBootConfig.getWorkspace();
                    case "user.home":
                        return System.getProperty("user.home");
                    case "config":
                        return runningBootConfig.getConfigStoreLocation();
                    case "lib":
                        return runningBootConfig.getLibStoreLocation();
                    case "programs":
                        return runningBootConfig.getProgramsStoreLocation();
                    case "cache":
                        return runningBootConfig.getCacheStoreLocation();
                    case "temp":
                        return runningBootConfig.getTempStoreLocation();
                    case "logs":
                        return runningBootConfig.getLogsStoreLocation();
                    case "var":
                        return runningBootConfig.getVarStoreLocation();
                }
                return "${" + from + "}";
            }
        });
        if (NutsUtils.isRemoteURL(path) || path.startsWith("file:")) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (path.startsWith("~~/") || path.startsWith("~~\\")) {
            path = runningBootConfig.getHome() + path.substring(1);
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
        if (NutsUtils.isRemoteURL(url) || url.startsWith("file:")) {
            return new URL(url);
        }
        return new File(url).toURI().toURL();
    }

    private File getBootFile(NutsBootId vid, String fileName, String[] repositories, String cacheFolder, boolean useCache) {
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

    private String getFileName(NutsBootId id, String ext) {
        return id.artifactId + "-" + id.version + "." + ext;
    }

    private String getPathFile(NutsBootId id, String name) {
        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + name;
    }

    //    private String getPath(NutsBootId id, String ext) {
//        String ff = id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/nuts." + ext;
//        System.out.println(ff);
//        return ff;
//    }
//    private String getPath(NutsBootId id, String ext) {
//        return id.groupId.replace('.', '/') + '/' + id.artifactId + '/' + id.version + "/" + getFileName(id, ext);
//    }
//    private File getBootFile(NutsBootId id, String fileName, String repository, File cacheFolder, boolean useCache) {
//        String path = getPathFile(id, fileName);
//        return getBootFile(path, repository, cacheFolder, useCache);
//    }
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
        File repoFolder = createFile(this.runningBootConfig.getHome(), repository);
        File ff = resolveFileForRepository(path, repoFolder, repository);
        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
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

    private String resolveWorkspacePath(String workspace) {
        String home = runningBootConfig.getHome();
        if (home == null) {
            throw new NutsIllegalArgumentException("Null Home");
        }
        if (NutsUtils.isEmpty(workspace)) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        String uws = workspace.replace('\\', '/');
        if (workspace.equals("~")) {
            throw new NutsIllegalArgumentException("Workspace can not span over hole user home");
        } else if (workspace.equals("~~")) {
            throw new NutsIllegalArgumentException("Workspace can not span over hole nuts home");
        } else if (uws.indexOf('/') < 0) {
            return home + File.separator + workspace;
        } else if (uws.startsWith("~/")) {
            return System.getProperty("user.home") + File.separator + workspace.substring(2);
        } else if (uws.startsWith("~~/")) {
            return home + File.separator + workspace.substring(3);
        } else {
            return NutsUtils.getAbsolutePath(workspace);
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

    private int runWorkspaceCommand(NutsWorkspace workspace, String message) throws IOException {
        NutsWorkspaceOptions o = this.getOptions();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
        NutsWorkspaceConfigManager conf = null;
        if (workspace != null) {
            conf = workspace.getConfigManager();
        }
        switch (o.getBootCommand()) {
            case VERSION: {
                if (workspace == null) {
                    System.out.println("nuts-api :" + actualVersion);
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                PrintStream out = workspace.getTerminal().getFormattedOut();

                workspace.getFormatManager().createWorkspaceVersionFormat()
                        .addOptions(o.getApplicationArguments())
                        .format(out);
                out.println();
                return 0;
            }
            case INFO: {
                if (workspace == null) {
                    System.out.println("nuts-boot-api          :" + actualVersion);
                    System.out.println("nuts-home              :" + (NutsUtils.isEmpty(o.getHome()) ? "<EMPTY>" : o.getHome()));
                    System.out.println("nuts-workspace         :" + runningBootConfig.getWorkspace() + "  ::  " + (NutsUtils.isEmpty(o.getWorkspace()) ? "<EMPTY>" : o.getWorkspace()));
                    System.out.println("nuts-store-strategy    :" + runningBootConfig.getStoreLocationStrategy());
                    System.out.println("nuts-store-layout      :" + runningBootConfig.getStoreLocationLayout());
                    System.out.println("nuts-store-programs    :" + runningBootConfig.getProgramsStoreLocation());
                    System.out.println("nuts-store-config      :" + runningBootConfig.getConfigStoreLocation());
                    System.out.println("nuts-store-var         :" + runningBootConfig.getVarStoreLocation());
                    System.out.println("nuts-store-logs        :" + runningBootConfig.getLogsStoreLocation());
                    System.out.println("nuts-store-temp        :" + runningBootConfig.getTempStoreLocation());
                    System.out.println("nuts-store-cache       :" + runningBootConfig.getCacheStoreLocation());
                    System.out.println("java-home              :" + System.getProperty("java.home"));
                    System.out.println("java-classpath         :" + System.getProperty("java.class.path"));
                    System.out.println("java-library-path      :" + System.getProperty("java.library.path"));
                    System.out.println("os-name                :" + System.getProperty("os.name"));
                    System.out.println("os-arch                :" + System.getProperty("os.arch"));
                    System.out.println("os-version             :" + System.getProperty("os.version"));
                    System.out.println("user-dir               :" + System.getProperty("user.dir"));
                    System.out.println("user-home              :" + System.getProperty("user.home"));
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                PrintStream out = workspace.getTerminal().getFormattedOut();
                workspace.getFormatManager().createWorkspaceInfoFormat()
                        .addOptions(o.getApplicationArguments())
                        .format(out);
                out.println();
                return 0;
            }
            case HELP: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                workspace.getTerminal().getFormattedOut().println(workspace.getHelpText());
                return 0;
            }
            case LICENSE: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                workspace.getTerminal().getFormattedOut().println(workspace.getLicenseText());
                return 0;
            }
            case INSTALL: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                List<String> ids = new ArrayList<>();
                NutsConfirmAction confirm = NutsConfirmAction.ERROR;
                for (String c : o.getApplicationArguments()) {
                    switch (c) {
                        case "-f":
                        case "--force":
                            confirm = NutsConfirmAction.FORCE;
                            break;
                        case "-i":
                        case "--ignore":
                            confirm = NutsConfirmAction.IGNORE;
                            break;
                        case "-e":
                        case "--error":
                            confirm = NutsConfirmAction.ERROR;
                            break;
                        default:
                            ids.add(c);
                            break;
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to install", 1);
                }
                for (String id : ids) {
                    workspace.install(id, o.getApplicationArguments(), confirm, null);
                }
                return 0;
            }
            case UNINSTALL: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                List<String> ids = new ArrayList<>();
                NutsConfirmAction confirm = NutsConfirmAction.ERROR;
                boolean deleteData = false;
                for (String c : o.getApplicationArguments()) {
                    if (c.equals("-f") || c.equals("--force")) {
                        confirm = NutsConfirmAction.FORCE;
                    } else if (c.equals("-i") || c.equals("--ignore")) {
                        confirm = NutsConfirmAction.IGNORE;
                    } else if (c.equals("-e") || c.equals("--error")) {
                        confirm = NutsConfirmAction.ERROR;
                    } else if (c.equals("-r") || c.equals("--erase")) {
                        deleteData = true;
                    } else {
                        ids.add(c);
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to uninstall", 1);
                }
                for (String id : ids) {
                    workspace.uninstall(id, o.getApplicationArguments(), confirm, deleteData, null);
                }
                return 0;
            }
            case INSTALL_COMPANIONS: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                boolean force = false;
                boolean silent = false;
                for (String argument : o.getApplicationArguments()) {
                    if ("-f".equals(argument) || "--force".equals(argument)) {
                        force = true;
                    } else if ("-s".equals(argument) || "--silent".equals(argument)) {
                        silent = true;
                    }
                }
                workspace.installCompanionTools(!force, force, silent, null);
                return 0;
            }
            case UPDATE: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(true)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true),
                        null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CHECK_UPDATES: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(false)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true),
                        null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CLEAN: {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                return actionClean(workspace);
            }
            case RESET: {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                actionReset(workspace);
                return 0;
            }
        }
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            return 1;
        }
        if (o.getApplicationArguments().length == 0) {
            workspace.getTerminal().getFormattedOut().println(workspace.getWelcomeText());
            return 0;
        }
        return workspace.createExecBuilder()
                .setCommand(o.getApplicationArguments())
                .setExecutorOptions(o.getExecutorOptions())
                .exec()
                .getResult();
    }

    private int actionReset(NutsWorkspace workspace) throws IOException {
        NutsWorkspaceOptions o = getOptions();
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.getConfigManager();
        boolean force = false;
        for (String argument : o.getApplicationArguments()) {
            if ("-f".equals(argument) || "--force".equals(argument)) {
                force = true;
            }
        }
        if (!force) {
            System.out.println("**************");
            System.out.println("** ATTENTION *");
            System.out.println("**************");
            System.out.println("You are about to delete all workspace configuration files.");
            System.out.println("Are you sure this is what you want ?");
        }
        List<File> folders = new ArrayList<>();
        if (conf != null) {
            folders.add(new File(conf.getWorkspaceLocation()));
            for (NutsStoreFolder value : NutsStoreFolder.values()) {
                folders.add(new File(conf.getStoreLocation(value)));
            }
        } else {
            folders.add(new File(runningBootConfig.getWorkspace()));
            folders.add(new File(runningBootConfig.getProgramsStoreLocation()));
            folders.add(new File(runningBootConfig.getLogsStoreLocation()));
            folders.add(new File(runningBootConfig.getCacheStoreLocation()));
            folders.add(new File(runningBootConfig.getVarStoreLocation()));
            folders.add(new File(runningBootConfig.getLibStoreLocation()));
            folders.add(new File(runningBootConfig.getTempStoreLocation()));
            folders.add(new File(runningBootConfig.getConfigStoreLocation()));
        }
        NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force);
        return 0;
    }

    private int actionClean(NutsWorkspace workspace) throws IOException {
        NutsWorkspaceOptions o = this.getOptions();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.getConfigManager();
        if (workspace != null) {
            conf = workspace.getConfigManager();
        }

//        String message = ;
//        if (log.isLoggable(Level.SEVERE)) {
//            log.log(Level.SEVERE, message);
//        }
        boolean force = false;
        for (String argument : o.getApplicationArguments()) {
            if ("-f".equals(argument) || "--force".equals(argument)) {
                force = true;
            }
        }
        List<File> folders = new ArrayList<>();
        if (workspace != null) {
            folders.add(new File(conf.getStoreLocation(NutsStoreFolder.LIB)));
            folders.add(new File(conf.getStoreLocation(NutsStoreFolder.CACHE)));
            folders.add(new File(conf.getStoreLocation(NutsStoreFolder.LOGS)));
        } else {
            folders.add(new File(runningBootConfig.getLibStoreLocation()));
            folders.add(new File(runningBootConfig.getCacheStoreLocation()));
            folders.add(new File(runningBootConfig.getLogsStoreLocation()));
        }
        File[] children = new File(this.runningBootConfig.getWorkspace(), NutsConstants.FOLDER_NAME_REPOSITORIES).listFiles();
        if (children != null) {
            for (File child : children) {
                folders.add(new File(child, NutsConstants.FOLDER_NAME_LIB));
            }
        }
        NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force);
        return 0;
    }

    private void fallbackInstallActionUnavailable(String message) {
        System.out.println(message);
        if (log.isLoggable(Level.SEVERE)) {
            log.log(Level.SEVERE, message);
        }
    }

    public static String getActualVersion() {
        return NutsUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"), null).getProperty("project.version", "0.0.0");
    }

    public void showError(NutsBootConfig actualBootConfig, NutsBootConfig workspaceConfig, String home, String workspace, URL[] bootClassWorldURLs, String extraMessage) {
        System.err.printf("Unable to locate nuts-core component. It is essential for Nuts to work.\n");
        System.err.printf("This component needs Internet connexion to initialize Nuts configuration.\n");
        System.err.printf("Don't panic, once components are downloaded, you will be able to work offline...\n");
        System.err.printf("Here after current environment info :\n");
        System.err.printf("  nuts-boot-api-version            : %s\n", actualBootConfig.getApiVersion() == null ? "<?> Not Found!" : actualBootConfig.getApiVersion());
        System.err.printf("  nuts-boot-runtime                : %s\n", actualBootConfig.getRuntimeId() == null ? "<?> Not Found!" : actualBootConfig.getRuntimeId());
        System.err.printf("  nuts-workspace-api-version       : %s\n", workspaceConfig.getApiVersion() == null ? "<?> Not Found!" : workspaceConfig.getApiVersion());
        System.err.printf("  nuts-workspace-runtime           : %s\n", workspaceConfig.getRuntimeId() == null ? "<?> Not Found!" : workspaceConfig.getRuntimeId());
        System.err.printf("  nuts-store-strategy              : %s\n", workspaceConfig.getStoreLocationStrategy());
        System.err.printf("  nuts-store-layout                : %s\n", workspaceConfig.getStoreLocationLayout());
        System.err.printf("  nuts-store-programs              : %s\n", workspaceConfig.getProgramsStoreLocation());
        System.err.printf("  nuts-store-config                : %s\n", workspaceConfig.getConfigStoreLocation());
        System.err.printf("  nuts-store-var                   : %s\n", workspaceConfig.getVarStoreLocation());
        System.err.printf("  nuts-store-temp                  : %s\n", workspaceConfig.getTempStoreLocation());
        System.err.printf("  nuts-store-config                : %s\n", workspaceConfig.getCacheStoreLocation());
        System.err.printf("  nuts-store-logs                  : %s\n", workspaceConfig.getLogsStoreLocation());
        System.err.printf("  nuts-home                        : %s\n", home);
        System.err.printf("  workspace-location               : %s\n", (workspace == null ? "<default-location>" : workspace));
        System.err.printf("  boot-args                        : %s\n", Arrays.toString(options.getBootArguments()));
        System.err.printf("  app-args                         : %s\n", Arrays.toString(options.getApplicationArguments()));
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            System.err.printf("  nuts-runtime-classpath           : %s\n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    System.err.printf("  nuts-runtime-classpath           : %s\n", String.valueOf(bootClassWorldURL));
                } else {
                    System.err.printf("                                     %s\n", String.valueOf(bootClassWorldURL));
                }
            }
        }
        System.err.printf("  java-version                     : %s\n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s\n", System.getProperty("java.home") + "/bin/java");
        System.err.printf("  java-class-path                  : %s\n", System.getProperty("java.class.path"));
        System.err.printf("  java-library-path                : %s\n", System.getProperty("java.library.path"));
        System.err.printf("  os-name                          : %s\n", System.getProperty("os.name"));
        System.err.printf("  os-arch                          : %s\n", System.getProperty("os.arch"));
        System.err.printf("  os-version                       : %s\n", System.getProperty("os.version"));
        System.err.printf("  user-name                        : %s\n", System.getProperty("user.name"));
        System.err.printf("  user-home                        : %s\n", System.getProperty("user.home"));
        System.err.printf("  user-dir                         : %s\n", System.getProperty("user.dir"));
        System.err.printf("Reported Error is :\n");
        System.err.printf(extraMessage + "\n");
        System.err.printf("If the problem persists you may want to get more debug info by adding '--verbose' argument :\n");
        System.err.printf("  java -jar nuts.jar --verbose [...]\n");
        System.err.printf("Now exiting Nuts, Bye!\n");
    }

    /**
     * compile and return loaded
     *
     * @param config
     * @return
     */
    private NutsBootConfig compile(NutsBootConfig config) {
        NutsBootConfig configLoaded = null;
        config.setHome(Nuts.resolveHomeFolder(null, config.getHome(), config.getStoreLocationLayout()));
        String ws = options.getWorkspace();
        for (int i = 0; i < 36; i++) {
            ws = resolveWorkspacePath(ws);
            configLoaded = NutsUtils.loadNutsBootConfig(ws);
            if (configLoaded.getWorkspace() == null || configLoaded.getWorkspace().isEmpty()) {
                config.setWorkspace(ws);
                config.setStoreLocationStrategy(configLoaded.getStoreLocationStrategy());
                config.setStoreLocationLayout(configLoaded.getStoreLocationLayout());
                config.setProgramsStoreLocation(configLoaded.getProgramsStoreLocation());
                config.setLogsStoreLocation(configLoaded.getLogsStoreLocation());
                config.setVarStoreLocation(configLoaded.getVarStoreLocation());
                config.setCacheStoreLocation(configLoaded.getCacheStoreLocation());
                config.setTempStoreLocation(configLoaded.getTempStoreLocation());
                config.setConfigStoreLocation(configLoaded.getConfigStoreLocation());
                break;
            }
            ws = configLoaded.getWorkspace();
            if (i == 35) {
                throw new NutsIllegalArgumentException("Cyclic Workspace resolution");
            }
        }

        if (config.getStoreLocationLayout() == null) {
            config.setStoreLocationLayout(NutsStoreLocationLayout.SYSTEM);
        }
        if (config.getStoreLocationStrategy() == null) {
            config.setStoreLocationStrategy(NutsStoreLocationStrategy.SYSTEM);
        }
        config.setProgramsStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.PROGRAMS, config));
        config.setLogsStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.LOGS, config));
        config.setVarStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.VAR, config));
        config.setCacheStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.CACHE, config));
        config.setLibStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.LIB, config));
        config.setTempStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.TEMP, config));
        config.setConfigStoreLocation(Nuts.resolveWorkspaceFolder(NutsStoreFolder.CONFIG, config));
        return configLoaded;
    }

}
