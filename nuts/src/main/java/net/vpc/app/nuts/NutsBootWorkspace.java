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
 * local jar cache folder located at $root/default-workspace/cache/bootstrap
 * where $root is the nuts root folder (~/.nuts)
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
    private NutsBootConfig loadedBootConfig;
    private NutsBootConfig runningBootConfig;
    private String requiredBootVersion;
    private String bootJavaCommand;
    private String requiredJavaOptions;
    //    String workspaceLocation;
    private NutsBootId bootId;
    private final NutsObjectConverter<String, String> pathExpansionConverter = new NutsObjectConverter<String, String>() {
        @Override
        public String convert(String from) {
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

    public NutsBootWorkspace(String[] args) {
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
        this.bootId = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_API + "#" + actualVersion);
        newInstance = true;
        if (options.getBootCommand() != null) {
            switch (options.getBootCommand()) {
                case UPDATE:
                case CHECK_UPDATES:
                case CLEANUP:
                case RESET: {
                    newInstance = false;
                }
            }
        }
        runningBootConfig = new NutsBootConfig(options);
        NutsLogUtils.bootstrap(options.getLogConfig());
        log.log(Level.CONFIG, "Open Nuts Workspace : {0}", options.getBootArgumentsString(true, true, true));
        loadedBootConfig = expandAllPaths(runningBootConfig);
        NutsLogUtils.prepare(options.getLogConfig(), NutsUtils.syspath(runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS) + "/bootstrap/net/vpc/app/nuts/nuts/" + actualVersion));

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
        log.log(Level.FINE, "Running version {0}. Requested version {1}", new Object[]{actualVersion, requiredBootVersion});
        StringBuilder errors = new StringBuilder();
        if ("LATEST".equalsIgnoreCase(requiredBootVersion) || "RELEASE".equalsIgnoreCase(requiredBootVersion)) {
            String releaseVersion = null;
            try {
                String NUTS_ID_BOOT_API_PATH = "/" + NutsConstants.NUTS_ID_BOOT_API.replaceAll("[.:]", "/");
                releaseVersion = NutsUtils.resolveMavenReleaseVersion(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT, NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException("Unable to load nuts version from " + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }

        String defaultWorkspaceLibFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        if (!NutsUtils.getSystemBoolean("nuts.export.no-m2", false)) {
            repos.add(System.getProperty("user.home") + NutsUtils.syspath("/.m2/repository"));
        }
        repos.addAll(Arrays.asList(
                NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT,
                NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL
        ));
        File file = NutsUtils.resolveOrDownloadJar(NutsConstants.NUTS_ID_BOOT_API + "#" + requiredBootVersion,
                repos.toArray(new String[0]),
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
        cmd.addAll(Arrays.asList(options.getExportedBootArguments()));
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

    private void openWorkspaceAttempt(OpenWorkspaceData info, boolean recover) {
        LinkedHashMap<String, File> allExtensionFiles = new LinkedHashMap<>();
        info.bootConfig0 = runningBootConfig.copy();
        if (recover) {
            if (!NutsUtils.isEmpty(info.bootConfig0.getRuntimeId())
                    && !NutsUtils.isEmpty(info.bootConfig0.getRuntimeDependencies())) {
                log.log(Level.CONFIG, "[RECOV. ] Invalidating old  runtime.");
            }
            info.bootConfig0.setRuntimeId(null);
            info.bootConfig0.setRuntimeDependencies(null);
        }
        if (!NutsUtils.isEmpty(info.bootConfig0.getApiVersion()) && !NutsUtils.isEmpty(info.bootConfig0.getRuntimeId()) && !NutsUtils.isEmpty(info.bootConfig0.getRuntimeDependencies())) {
            //Ok
        } else {
            info.bootConfig0 = buildNutsBootConfig(info.bootConfig0, recover);
        }
        if (info.bootConfig0 != null && !actualVersion.equals(info.bootConfig0.getApiVersion())) {
            log.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{info.bootConfig0.getApiVersion(), actualVersion});
            info.actualBootConfig = buildNutsBootConfig(info.bootConfig0, recover);
        } else {
            info.actualBootConfig = info.bootConfig0;
        }

        if (info.actualBootConfig == null) {
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load ClassPath");
        }

        String workspaceBootLibFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE) + "/bootstrap";
        NutsBootId bootRuntime = null;
        if (NutsUtils.isEmpty(info.actualBootConfig.getRuntimeId())) {
            bootRuntime = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        } else if (info.actualBootConfig.getRuntimeId().contains("#")) {
            bootRuntime = NutsBootId.parse(info.actualBootConfig.getRuntimeId());
        } else {
            bootRuntime = NutsBootId.parse(NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        }
        String[] repositories = NutsUtils.splitUrlStrings(info.actualBootConfig.getRepositories()).toArray(new String[0]);
        File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootLibFolder, !recover);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load " + bootRuntime);
        }

        allExtensionFiles.put(info.actualBootConfig.getRuntimeId(), f);
        for (String idStr : NutsUtils.split(info.actualBootConfig.getRuntimeDependencies(), "\n\t ;,")) {
            NutsBootId id = NutsBootId.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, !recover);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        info.bootClassWorldURLs = resolveClassWorldURLs(allExtensionFiles.values());
        log.log(Level.CONFIG, "Loading Nuts ClassWorld from {0} jars : {1}", new Object[]{info.bootClassWorldURLs.length, Arrays.asList(info.bootClassWorldURLs)});
        if (log.isLoggable(Level.CONFIG)) {
            for (URL bootClassWorldURL : info.bootClassWorldURLs) {
                log.log(Level.CONFIG, "\t {0}", new Object[]{NutsUtils.formatURL(bootClassWorldURL)});
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
                System.err.printf("\t %s\n", NutsUtils.formatURL(url));
            }
            log.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(info.bootClassWorldURLs));
            throw new NutsInvalidWorkspaceException(this.runningBootConfig.getWorkspace(), "Unable to load Workspace Component from ClassPath : " + Arrays.asList(info.bootClassWorldURLs));
        }
        ((NutsWorkspaceImpl) info.nutsWorkspace).initializeWorkspace(factoryInstance, info.actualBootConfig, info.bootConfig0,
                info.bootClassWorldURLs,
                info.workspaceClassLoader, options.copy());
        if (recover) {
//            info.nutsWorkspace.getConfigManager().setBootConfig(new NutsBootConfig());
            if (!info.nutsWorkspace.config().isReadOnly()) {
                info.nutsWorkspace.save();
            }
        }
    }

    public int run() {
        switch (this.getOptions().getBootCommand()) {
            case RESET: {
                return actionReset(null, true);
            }
            case CLEANUP: {
                return actionCleanup(null, true);
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
            log.log(Level.SEVERE, "Open Workspace Failed", ex);
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
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        if (this.getOptions().getInitMode() != null) {
            switch (this.getOptions().getInitMode()) {
                case CLEANUP: {
                    actionCleanup(null, false);
                    break;
                }
                case RESET: {
                    actionReset(null, false);
                    break;
                }
            }
        }

//        System.out.println("openWorkspace() ===> " + options.getBootArgumentsString(true, true, true));
//        System.out.println("openWorkspace() ===> ws=" + NutsUtils.formatLogValue(options.getWorkspace(), runningBootConfig.getWorkspace()));
//        System.out.println("openWorkspace() ===> var=" + NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.VAR), runningBootConfig.getStoreLocation(NutsStoreLocation.VAR)));
//        System.out.println("openWorkspace() ===> programs=" + NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.PROGRAMS), runningBootConfig.getStoreLocation(NutsStoreLocation.PROGRAMS)));
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Open Workspace with command line  : {0}", options.getBootArgumentsString(true, true, true));
            log.log(Level.CONFIG, "Open Workspace with config        :");
            log.log(Level.CONFIG, "\t nuts-boot-api                  : {0}", actualVersion);
            log.log(Level.CONFIG, "\t nuts-workspace                 : {0}", NutsUtils.formatLogValue(options.getWorkspace(), runningBootConfig.getWorkspace()));
            log.log(Level.CONFIG, "\t nuts-store-strategy            : {0}", NutsUtils.formatLogValue(options.getStoreLocationStrategy(), runningBootConfig.getStoreLocationStrategy()));
            log.log(Level.CONFIG, "\t nuts-repos-store-strategy      : {0}", NutsUtils.formatLogValue(options.getRepositoryStoreLocationStrategy(), runningBootConfig.getRepositoryStoreLocationStrategy()));
            log.log(Level.CONFIG, "\t nuts-store-layout              : {0}", NutsUtils.formatLogValue(options.getStoreLocationLayout(), runningBootConfig.getStoreLocationLayout()));
            log.log(Level.CONFIG, "\t nuts-store-programs            : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.PROGRAMS), runningBootConfig.getStoreLocation(NutsStoreLocation.PROGRAMS)));
            log.log(Level.CONFIG, "\t nuts-store-config              : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.CONFIG), runningBootConfig.getStoreLocation(NutsStoreLocation.CONFIG)));
            log.log(Level.CONFIG, "\t nuts-store-var                 : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.VAR), runningBootConfig.getStoreLocation(NutsStoreLocation.VAR)));
            log.log(Level.CONFIG, "\t nuts-store-logs                : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.LOGS), runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS)));
            log.log(Level.CONFIG, "\t nuts-store-temp                : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.TEMP), runningBootConfig.getStoreLocation(NutsStoreLocation.TEMP)));
            log.log(Level.CONFIG, "\t nuts-store-cache               : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.CACHE), runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE)));
            log.log(Level.CONFIG, "\t nuts-store-lib                 : {0}", NutsUtils.formatLogValue(options.getStoreLocation(NutsStoreLocation.LIB), runningBootConfig.getStoreLocation(NutsStoreLocation.LIB)));
            log.log(Level.CONFIG, "\t option-recover                 : {0}", (options.getInitMode() == null ? "" : options.getInitMode().name().toLowerCase()));
            log.log(Level.CONFIG, "\t option-read-only               : {0}", options.isReadOnly());
            log.log(Level.CONFIG, "\t option-open-mode               : {0}", options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode());
            log.log(Level.CONFIG, "\t java-home                      : {0}", System.getProperty("java.home"));
            log.log(Level.CONFIG, "\t java-classpath                 : {0}", System.getProperty("java.class.path"));
            log.log(Level.CONFIG, "\t java-library-path              : {0}", System.getProperty("java.library.path"));
            log.log(Level.CONFIG, "\t os-name                        : {0}", System.getProperty("os.name"));
            log.log(Level.CONFIG, "\t os-arch                        : {0}", System.getProperty("os.arch"));
            log.log(Level.CONFIG, "\t os-version                     : {0}", System.getProperty("os.version"));
            log.log(Level.CONFIG, "\t user-name                      : {0}", System.getProperty("user.name"));
            log.log(Level.CONFIG, "\t user-dir                       : {0}", System.getProperty("user.dir"));
            log.log(Level.CONFIG, "\t user-home                      : {0}", System.getProperty("user.home"));
        }
        OpenWorkspaceData info = new OpenWorkspaceData();
        try {

            if (options.getOpenMode() == NutsWorkspaceOpenMode.OPEN_EXISTING) {
                //add fail fast test!!
                if (!new File(runningBootConfig.getWorkspace(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
                }
            }
            try {
                openWorkspaceAttempt(info, options.getInitMode() == NutsBootInitMode.RECOVER);
            } catch (NutsException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (options.getInitMode() == NutsBootInitMode.RECOVER) {
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

//    private String[] resolveBootConfigRepositories(String... possibilities) {
//        List<String> initial = new ArrayList<>();
//        initial.add(runtimeSourceURL);
////        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
//        if (possibilities != null) {
//            initial.addAll(Arrays.asList(possibilities));
//        }
//        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
//        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
//    }
    private String[] resolveBootClassPathRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
//        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (!NutsUtils.getSystemBoolean("nuts.export.no-m2", false)) {
            initial.add(NutsConstants.URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL);
        }
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add("${workspace}/" + NutsConstants.FOLDER_NAME_REPOSITORIES + "/" + NutsConstants.DEFAULT_REPOSITORY_NAME + "/" + NutsConstants.FOLDER_NAME_LIB);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL);
        initial.add(NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT);
        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private NutsBootConfig buildNutsBootConfig(NutsBootConfig bootConfig0, boolean recover) {
        String cacheFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE) + "/bootstrap";
        NutsBootId apiId = NutsUtils.isEmpty(bootConfig0.getApiVersion()) ? bootId : new NutsBootId(bootId.getGroupId(), bootId.getArtifactId(), bootConfig0.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");
        String runtimeId = bootConfig0.getRuntimeId();
        String repositories = bootConfig0.getRepositories();
        boolean cacheRemoteOnly = true;
        if (bootConfig0.getStoreLocationStrategy() == NutsStoreLocationStrategy.STANDALONE) {
            cacheRemoteOnly = false;
        }
        List<String> resolvedBootRepositories = new ArrayList<>();
        if (NutsUtils.isEmpty(runtimeId) || NutsUtils.isEmpty(repositories)) {
            resolvedBootRepositories.addAll(Arrays.asList(NutsUtils.splitAndRemoveDuplicates(this.runtimeSourceURL, NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT)));
            for (String repo : resolvedBootRepositories) {
                URL urlString = buildURL(repo, bootAPIPropertiesPath);
                if (urlString != null) {
                    Properties wruntimeProperties = NutsUtils.loadURLProperties(urlString, new File(cacheFolder, bootAPIPropertiesPath.replace('/', File.separatorChar)), !recover);
                    if (!wruntimeProperties.isEmpty()) {
                        String wruntimeId = wruntimeProperties.getProperty("bootRuntimeId");
                        String wrepositories = wruntimeProperties.getProperty("repositories");
                        if (!NutsUtils.isEmpty(wruntimeId) && !NutsUtils.isEmpty(wrepositories)) {
                            runtimeId = wruntimeId;
                            repositories = wrepositories;
                            if (log.isLoggable(Level.CONFIG)) {
                                log.log(Level.CONFIG, "Boot {0} with repositories {1}", new Object[]{runtimeId, repositories});
                            }
                            break;
                            //no need to log, already done in NutsUtils. loadURLProperties
                        }
                    }
                } else {
                    if (log.isLoggable(Level.CONFIG)) {
                        log.log(Level.CONFIG, "[ERROR  ] Loading props file from {0}", urlString);
                    }
                }
            }
        }

        if (NutsUtils.isEmpty(runtimeId)) {
            runtimeId = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + apiId.getVersion() + ".0";
            log.log(Level.CONFIG, "[ERROR  ] Failed to load boot props file from boot repositories. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        NutsBootId _runtimeId = NutsBootId.parse(runtimeId);

        resolvedBootRepositories.clear();

        //will load only from local cached file!!
        String bootRuntimePropertiesPath = getPathFile(_runtimeId, "nuts.properties");
        NutsBootConfig goodCp = null;
        if (!recover) {
            File cacheFile = new File(cacheFolder, NutsUtils.syspath(bootRuntimePropertiesPath));
            try {
                Properties p = NutsUtils.loadURLProperties(cacheFile.toURI().toURL(), null, !recover);
                if (p != null && !p.isEmpty()) {
                    String id = p.getProperty("project.id");
                    String version = p.getProperty("project.version");
                    String dependencies = p.getProperty("project.dependencies.compile");
                    if (!NutsUtils.isEmpty(id) && !NutsUtils.isEmpty(version)) {
                        String repositories0 = p.getProperty("project.repositories");
                        if (repositories0 == null) {
                            repositories0 = "";
                        }
                        goodCp = new NutsBootConfig()
                                .setRuntimeId(id + "#" + version)
                                .setRuntimeDependencies(dependencies)
                                .setRepositories(repositories0 + ";" + repositories);
                        log.log(Level.CONFIG, "[SUCCESS] Loaded config from  {0} as {1}", new Object[]{cacheFile.getPath(), goodCp});
                        goodCp = bootConfig0.copy()
                                .setApiVersion(apiId.getVersion())
                                .setRuntimeId(goodCp.getRuntimeId())
                                .setRuntimeDependencies(goodCp.getRuntimeDependencies())
                                .setRepositories(goodCp.getRepositories());
                    } else {
                        log.log(Level.CONFIG, "[ERROR  ] Loaded config from  {0}. missing id or version", new Object[]{cacheFile.getPath()});
                    }
                }
            } catch (Exception ex) {
                log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime props file from  {0}", new Object[]{cacheFile.getPath()});
            }
        }

        if (goodCp == null) {
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
            LinkedHashSet<String> jarRepositories = new LinkedHashSet();
            if (!NutsUtils.getSystemBoolean("nuts.boot.no-m2", false)) {
                jarRepositories.add(NutsConstants.URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL);
            }
            jarRepositories.addAll(Arrays.asList(
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_GIT,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL,
                    NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT
            ));
            if (!NutsUtils.isEmpty(repositories)) {
                for (String r : repositories.split(";")) {
                    r = r.trim();
                    if (r.length() > 0) {
                        jarRepositories.add(r);
                    }
                }
            }

            for (String repoURL : jarRepositories) {
                NutsBootId bootRuntimeNutsId = NutsBootId.parse(runtimeId0);
                String pomPath = getPathFile(bootRuntimeNutsId, getFileName(bootRuntimeNutsId, "pom"));
                URL urlString2 = buildURL(repoURL, pomPath);
                if (urlString2 != null) {
                    String[] bootNutsIds = NutsUtils.parseDependenciesFromMaven(urlString2, new File(cacheFolder, NutsUtils.syspath(pomPath)), !recover, cacheRemoteOnly);
                    if (bootNutsIds != null) {
                        goodCp = bootConfig0.copy()
                                .setApiVersion(apiId.getVersion())
                                .setRuntimeId(runtimeId0)
                                .setRuntimeDependencies(NutsUtils.join(";", bootNutsIds))
                                .setRepositories(NutsUtils.join(";", jarRepositories));

                        //cache boot-api     properties
                        File cacheFile = new File(cacheFolder, NutsUtils.syspath(bootAPIPropertiesPath));
                        boolean cacheExists = cacheFile.isFile();
                        if (recover || !cacheExists) {
                            Properties p = new Properties();
                            p.setProperty("project.id", apiId.getGroupId() + ":" + apiId.getArtifactId());
                            p.setProperty("project.version", apiId.getVersion());
                            p.setProperty("project.name", apiId.getGroupId() + ":" + apiId.getArtifactId());
                            p.setProperty("bootRuntimeId", bootRuntimeNutsId.toString());
                            p.setProperty("repositories", goodCp.getRepositories());
                            NutsUtils.storeProperties(p, cacheFile);
                            if (!cacheExists) {
                                log.log(Level.CONFIG, "[CACHED ] Cached prp file {0}", new Object[]{cacheFile.getPath()});
                            } else if (recover) {
                                log.log(Level.CONFIG, "[RECOV. ] Cached prp file {0}", new Object[]{cacheFile.getPath()});
                            }
                        }
                        //cache boot-runtime properties
                        cacheFile = new File(cacheFolder, NutsUtils.syspath(bootRuntimePropertiesPath));
                        cacheExists = cacheFile.isFile();
                        if (recover || !cacheExists) {
                            Properties p = new Properties();
                            p.setProperty("project.id", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
                            p.setProperty("project.version", bootRuntimeNutsId.getVersion());
                            p.setProperty("project.name", bootRuntimeNutsId.getGroupId() + ":" + bootRuntimeNutsId.getArtifactId());
//                        p.setProperty("runtimeId",bootRuntimeNutsId.toString());
//                        p.setProperty("repositories",bcc.getRepositories());
                            p.setProperty("project.dependencies.compile", goodCp.getRuntimeDependencies() == null ? "" : goodCp.getRuntimeDependencies());
                            NutsUtils.storeProperties(p, cacheFile);
                            if (!cacheExists) {
                                log.log(Level.CONFIG, "[CACHED ] Cached prp file {0}", new Object[]{cacheFile.getPath()});
                            } else {
                                log.log(Level.CONFIG, "[RECOV. ] Cached prp file {0}", new Object[]{cacheFile.getPath()});
                            }
                        }
                        break;
                    }
                } else {
                    log.log(Level.CONFIG, "[ERROR  ] Failed to load runtime jar dependencies from  {0}", urlString2);
                }
            }
        }
        if (goodCp == null) {
            return null;
        }
        List<String> repos2 = new ArrayList<>();
        repos2.add(goodCp.getRepositories());
        repos2.addAll(resolvedBootRepositories);
        String repositoriesToStore = NutsUtils.join(";",
                resolveBootClassPathRepositories(repos2.toArray(new String[0]))
        );

        goodCp.setRepositories(repositoriesToStore);
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
        path = NutsUtils.replaceDollarString(path, pathExpansionConverter);
        if (NutsUtils.isURL(path)) {
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
        if (NutsUtils.isURL(url)) {
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
            if (NutsUtils.isURL(base)) {
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
        if (NutsUtils.isURL(repository)) {
            try {
                localFile = NutsUtils.toFile(new URL(repository));
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
                NutsUtils.copy(new URL(urlPath), to);
                log.log(Level.CONFIG, "[SUCCESS] Loading  {0}", new Object[]{urlPath});
                ok = to;
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Loading  {0}", new Object[]{urlPath});
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
                log.log(Level.CONFIG, "[ERROR  ] Locating {0}", new Object[]{file});
            }
        } else {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            log.log(Level.CONFIG, "[ERROR  ] Locating {0} . Repository is not a valid folder : {1}", new Object[]{file, repoFolder});
        }

        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
                File to = new File(cacheFolder, path);
                String toc = NutsUtils.getAbsolutePath(to.getPath());
                String ffc = NutsUtils.getAbsolutePath(ff.getPath());
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
                        NutsUtils.copy(ff, to);
                        log.log(Level.CONFIG, "[RECOV. ] Cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    } else {
                        NutsUtils.copy(ff, to);
                        log.log(Level.CONFIG, "[CACHED ] Cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    }
                    return to;
                } catch (IOException ex) {
                    log.log(Level.CONFIG, "[ERROR  ] Caching file {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
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

    private String expandWorkspacePath(String workspace) {
        if (NutsUtils.isEmpty(workspace)) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        String uws = workspace.replace('\\', '/');
        if (workspace.equals("~")) {
            throw new NutsIllegalArgumentException("Workspace can not span over hole user home");
        } else if (workspace.equals("~~")) {
            throw new NutsIllegalArgumentException("Workspace can not span over hole nuts home");
        } else if (uws.indexOf('/') < 0) {
            String home = getHome(NutsStoreLocation.CONFIG);
            if (home == null) {
                throw new NutsIllegalArgumentException("Null Home");
            }
            return home + File.separator + workspace;
        } else if (uws.startsWith("~/")) {
            return System.getProperty("user.home") + File.separator + workspace.substring(2);
        } else if (uws.startsWith("~~/")) {
            String home = getHome(NutsStoreLocation.CONFIG);
            if (home == null) {
                throw new NutsIllegalArgumentException("Null Home");
            }
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

    private int runWorkspaceCommand(NutsWorkspace workspace, String message) {
        NutsWorkspaceOptions o = this.getOptions();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
        switch (o.getBootCommand()) {
            case VERSION: {
                if (workspace == null) {
                    System.out.println("nuts-api :" + actualVersion);
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                PrintStream out = workspace.getTerminal().getFormattedOut();

                workspace.formatter().createWorkspaceVersionFormat()
                        .addOptions(o.getApplicationArguments())
                        .format(out);
                out.println();
                return 0;
            }
            case INFO: {
                if (workspace == null) {
                    System.out.println("nuts-boot-api          :" + actualVersion);
                    System.out.println("nuts-workspace         :" + runningBootConfig.getWorkspace() + "  ::  " + (NutsUtils.isEmpty(o.getWorkspace()) ? "<EMPTY>" : o.getWorkspace()));
                    System.out.println("nuts-store-strategy    :" + runningBootConfig.getStoreLocationStrategy());
                    System.out.println("nuts-store-layout      :" + runningBootConfig.getStoreLocationLayout());
                    System.out.println("nuts-store-programs    :" + runningBootConfig.getStoreLocation(NutsStoreLocation.PROGRAMS));
                    System.out.println("nuts-store-config      :" + runningBootConfig.getStoreLocation(NutsStoreLocation.CONFIG));
                    System.out.println("nuts-store-var         :" + runningBootConfig.getStoreLocation(NutsStoreLocation.VAR));
                    System.out.println("nuts-store-logs        :" + runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS));
                    System.out.println("nuts-store-temp        :" + runningBootConfig.getStoreLocation(NutsStoreLocation.TEMP));
                    System.out.println("nuts-store-cache       :" + runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE));
                    System.out.println("nuts-store-lib         :" + runningBootConfig.getStoreLocation(NutsStoreLocation.LIB));
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
                workspace.formatter().createWorkspaceInfoFormat()
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
                Map<String, List<String>> ids = new LinkedHashMap<>();
                NutsInstallOptions options = new NutsInstallOptions().setTrace(true);
                String[] applicationArguments = o.getApplicationArguments();
                for (int i = 0; i < applicationArguments.length; i++) {
                    String c = applicationArguments[i];
                    switch (c) {
                        case "-f":
                        case "--force":
                            options.setForce(true);
                            break;
                        case "-i":
                        case "--ignore":
                            options.setForce(false);
                            break;
                        default: {
                            ArrayList<String> args = new ArrayList<>();
                            ids.put(c, args);
                            if (i + 1 < applicationArguments.length && "--".equals(applicationArguments[i + 1])) {
                                i += 2;
                                while (i < applicationArguments.length) {
                                    args.add(applicationArguments[i]);
                                    i++;
                                }
                            }
                            break;
                        }
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to install", 1);
                }
                for (Map.Entry<String, List<String>> id : ids.entrySet()) {
                    workspace.install(id.getKey(), id.getValue().toArray(new String[0]), options, null);
                }
                return 0;
            }
            case UNINSTALL: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                Map<String, List<String>> ids = new LinkedHashMap<>();
                NutsUninstallOptions options = new NutsUninstallOptions().setTrace(true);
                String[] applicationArguments = o.getApplicationArguments();
                for (int i = 0; i < applicationArguments.length; i++) {
                    String c = applicationArguments[i];
                    switch (c) {
                        case "-r":
                        case "--erase":
                            options.setErase(true);
                            break;
                        default:
                            ArrayList<String> args = new ArrayList<>();
                            ids.put(c, args);
                            if (i + 1 < applicationArguments.length && "--".equals(applicationArguments[i + 1])) {
                                i += 2;
                                while (i < applicationArguments.length) {
                                    args.add(applicationArguments[i]);
                                    i++;
                                }
                            }
                            break;
                    }
                }
                if (ids.isEmpty()) {
                    throw new NutsExecutionException("Missing nuts to uninstall", 1);
                }
                for (Map.Entry<String, List<String>> id : ids.entrySet()) {
                    workspace.uninstall(id.getKey(), id.getValue().toArray(new String[0]), options, null);
                }
                return 0;
            }
            case INSTALL_COMPANION_TOOLS: {
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
                workspace.installCompanionTools(new NutsInstallCompanionOptions().setAsk(!force).setForce(force).setTrace(!silent), null);
                return 0;
            }
            case UPDATE: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                if (o.getApplicationArguments().length == 0) {
                    if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                            .setApplyUpdates(true)
                            .setEnableMajorUpdates(true),
                            null).length > 0) {
                        return 0;
                    }
                } else {
                    final NutsSession session = workspace.createSession();
                    NutsUpdateResult[] defs = workspace.update(o.getApplicationArguments(), null, new NutsUpdateOptions()
                            .setApplyUpdates(false)
                            .setTrace(true),
                            session);
                    boolean someUpdatable = false;
                    for (NutsUpdateResult d : defs) {
                        if (d.isUpdateAvailable()) {
                            someUpdatable = true;
                        }
                    }
                    if (someUpdatable) {
                        return 0;
                    }
                }
                return 1;
            }
            case CHECK_UPDATES: {
                if (workspace == null) {
                    fallbackInstallActionUnavailable(message);
                    return 1;
                }
                if (o.getApplicationArguments().length == 0) {
                    if (workspace.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                            .setApplyUpdates(false)
                            .setEnableMajorUpdates(true)
                            .setEnableMajorUpdates(true),
                            null).length > 0) {
                        return 0;
                    }
                } else {
                    final NutsSession session = workspace.createSession();
                    NutsUpdateResult[] defs = workspace.update(o.getApplicationArguments(), null, new NutsUpdateOptions()
                            .setApplyUpdates(false)
                            .setTrace(true),
                            session);
                    boolean someUpdatable = false;
                    for (NutsUpdateResult d : defs) {
                        if (d.isUpdateAvailable()) {
                            someUpdatable = true;
                        }
                    }
                    if (someUpdatable) {
                        return 0;
                    }
                }
                return 1;
            }
            case CLEANUP: {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                actionCleanup(workspace, true);
                return 0;
            }
            case RESET: {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                actionReset(workspace, true);
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
                .setExecutionType(o.getExecutionType())
                .exec()
                .getResult();
    }

    private int actionReset(NutsWorkspace workspace, boolean readArguments) {
//        if (!new File(runningBootConfig.getWorkspace()).isDirectory()) {
//            return 0;
//        }
        NutsWorkspaceOptions o = getOptions();
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.config();
        boolean force = false;
        if (readArguments) {
            for (String argument : o.getApplicationArguments()) {
                if ("-f".equals(argument) || "--force".equals(argument)) {
                    force = true;
                }
            }
        }
        boolean yes = Boolean.TRUE.equals(o.getDefaultResponse());
        boolean no = Boolean.FALSE.equals(o.getDefaultResponse());
        if (!force) {
            if (no) {
                if (workspace == null) {
                    System.err.println("reset cancelled (applied '--no' argument)");
                } else {
                    workspace.getTerminal().getOut().println(" cancelled (applied '--no' argument)");
                }
                throw new NutsUserCancelException();
            }
        }
        if (yes) {
            force = true;
        }
        List<File> folders = new ArrayList<>();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Deleting all folders for Workspace : {0}", runningBootConfig.getWorkspace());
        }
        if (conf != null) {
            folders.add(conf.getWorkspaceLocation().toFile());
            for (NutsStoreLocation value : NutsStoreLocation.values()) {
                folders.add(conf.getStoreLocation(value).toFile());
            }
        } else {
            folders.add(new File(runningBootConfig.getWorkspace()));
            for (NutsStoreLocation value : NutsStoreLocation.values()) {
                folders.add(new File(runningBootConfig.getStoreLocation(value)));
            }
        }
        String header = "**************\n"
                + "** ATTENTION *\n"
                + "**************\n"
                + "You are about to delete all workspace configuration files.\n"
                + "Are you sure this is what you want ?";
        NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, header, workspace != null ? workspace.getTerminal() : null);
        return 0;
    }

    private int actionCleanup(NutsWorkspace workspace, boolean readArguments) {
        if (!new File(runningBootConfig.getWorkspace()).isDirectory()) {
            return 0;
        }
        NutsWorkspaceOptions o = this.getOptions();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace pre-command : cleanup");
        }
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.config();
        boolean force = false;
        boolean yes = Boolean.TRUE.equals(o.getDefaultResponse());
        boolean no = Boolean.FALSE.equals(o.getDefaultResponse());
        if (readArguments) {
            for (String argument : o.getApplicationArguments()) {
                if ("-f".equals(argument) || "--force".equals(argument)) {
                    force = true;
                }
            }
        }
        if (!force) {
            if (no) {
                if (workspace == null) {
                    System.err.println("clean cancelled (applied '--no' argument)");
                } else {
                    workspace.getTerminal().getOut().println("clean cancelled (applied '--no' argument)");
                }
                throw new NutsUserCancelException();
            }
        }
        if (yes) {
            force = true;
        }
        List<File> folders = new ArrayList<>();
        if (conf != null) {
//            folders.add(new File(conf.getStoreLocation(NutsStoreLocation.LIB)));
            folders.add(conf.getStoreLocation(NutsStoreLocation.CACHE).toFile());
            folders.add(conf.getStoreLocation(NutsStoreLocation.LOGS).toFile());
            folders.add(conf.getStoreLocation(NutsStoreLocation.TEMP).toFile());
        } else {
//            folders.add(new File(runningBootConfig.getStoreLocation(NutsStoreLocation.LIB)));
            folders.add(new File(runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE)));
            folders.add(new File(runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS)));
            folders.add(new File(runningBootConfig.getStoreLocation(NutsStoreLocation.TEMP)));
        }
        File[] children = new File(this.runningBootConfig.getWorkspace(), NutsConstants.FOLDER_NAME_REPOSITORIES).listFiles();
        if (children != null) {
            for (File child : children) {
                folders.add(new File(child, NutsConstants.FOLDER_NAME_LIB));
            }
        }
        NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, null, workspace != null ? workspace.getTerminal() : null);
        return 0;
    }

    private void fallbackInstallActionUnavailable(String message) {
        System.out.println(message);
        if (log.isLoggable(Level.SEVERE)) {
            log.log(Level.SEVERE, message);
        }
    }

    public void showError(NutsBootConfig actualBootConfig, NutsBootConfig workspaceConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage) {
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
        System.err.printf("  nuts-store-programs              : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.PROGRAMS));
        System.err.printf("  nuts-store-config                : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.CONFIG));
        System.err.printf("  nuts-store-var                   : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.VAR));
        System.err.printf("  nuts-store-logs                  : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.LOGS));
        System.err.printf("  nuts-store-temp                  : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.TEMP));
        System.err.printf("  nuts-store-config                : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.CACHE));
        System.err.printf("  nuts-store-lib                   : %s\n", workspaceConfig.getStoreLocation(NutsStoreLocation.LIB));
        System.err.printf("  workspace-location               : %s\n", (workspace == null ? "<default-location>" : workspace));
        System.err.printf("  nuts-boot-args                   : %s\n", options.getBootArgumentsString(true, true, true));
        System.err.printf("  nuts-app-args                    : %s\n", Arrays.toString(options.getApplicationArguments()));
        System.err.printf("  option-recover                   : %s\n", (options.getInitMode() == null ? "" : options.getInitMode().name().toLowerCase()));
        System.err.printf("  option-read-only                 : %s\n", options.isReadOnly());
        System.err.printf("  option-open-mode                 : %s\n", options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode());
        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            System.err.printf("  nuts-runtime-classpath           : %s\n", "<none>");
        } else {
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    System.err.printf("  nuts-runtime-classpath           : %s\n", NutsUtils.formatURL(bootClassWorldURL));
                } else {
                    System.err.printf("                                     %s\n", NutsUtils.formatURL(bootClassWorldURL));
                }
            }
        }
        System.err.printf("  java-version                     : %s\n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s\n", NutsUtils.resolveJavaCommand(null));
        System.err.printf("  java-class-path                  : %s\n", System.getProperty("java.class.path"));
        System.err.printf("  java-library-path                : %s\n", System.getProperty("java.library.path"));
        System.err.printf("  os-name                          : %s\n", System.getProperty("os.name"));
        System.err.printf("  os-arch                          : %s\n", System.getProperty("os.arch"));
        System.err.printf("  os-version                       : %s\n", System.getProperty("os.version"));
        System.err.printf("  user-name                        : %s\n", System.getProperty("user.name"));
        System.err.printf("  user-home                        : %s\n", System.getProperty("user.home"));
        System.err.printf("  user-dir                         : %s\n", System.getProperty("user.dir"));
        System.err.print("Reported Error is :\n");
        System.err.print(extraMessage + "\n");
        System.err.print("If the problem persists you may want to get more debug info by adding '--verbose' argument.\n");
        System.err.print("You may also enable recover mode to ignore existing cache info with '--recover' argument.\n");
        System.err.print("Here is the proper command : \n");
        System.err.print("  java -jar nuts.jar --verbose --recover [...]\n");
        System.err.print("Now exiting Nuts, Bye!\n");
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
            NutsBootConfig configLoaded = NutsUtils.loadNutsBootConfig(ws);
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
                throw new NutsIllegalArgumentException("Cyclic Workspace resolution");
            }
        }
        config.setWorkspace(lastConfigPath);
        if (lastConfigLoaded != null) {
            config.setWorkspace(lastConfigPath);
            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                config.setStoreLocation(type, lastConfigLoaded.getStoreLocation(type));
            }
            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
                for (NutsStoreLocation loc : NutsStoreLocation.values()) {
                    String llid = layout.name().toLowerCase() + "." + loc.name().toLowerCase();
                    String homeLocation = lastConfigLoaded.getHomeLocation(layout, loc);
                    if (!NutsUtils.isEmpty(homeLocation)) {
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
                if (NutsUtils.isEmpty(homes[type.ordinal()])) {
                    throw new NutsIllegalArgumentException("Missing Home for " + type.name().toLowerCase());
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
//            if (NutsUtils.isEmpty(config.getConfigStoreLocation())) {
//                config.setConfigStoreLocation(workspace + File.separator + NutsStoreFolder.CONFIG.name().toLowerCase());
//            } else if (!NutsUtils.isAbsolutePath(config.getConfigStoreLocation())) {
//                config.setConfigStoreLocation(workspace + File.separator + NutsUtils.syspath(config.getConfigStoreLocation()));
//            }
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                switch (type) {
                    default: {
                        if (NutsUtils.isEmpty(config.getStoreLocation(type))) {
                            switch (storeLocationStrategy) {
                                case STANDALONE: {
                                    config.setStoreLocation(type, (workspace + File.separator + type.name().toLowerCase()));
                                    break;
                                }
                                case EXPLODED: {
                                    config.setStoreLocation(type, homes[type.ordinal()] + NutsUtils.syspath("/" + workspaceName + "/" + type.name().toLowerCase()));
                                    break;
                                }
                            }
                        } else if (!NutsUtils.isAbsolutePath(config.getStoreLocation(type))) {
                            switch (storeLocationStrategy) {
                                case STANDALONE: {
                                    config.setStoreLocation(type, (workspace + File.separator + type.name().toLowerCase()));
                                    break;
                                }
                                case EXPLODED: {
                                    config.setStoreLocation(type, homes[type.ordinal()] + NutsUtils.syspath("/" + workspaceName + "/" + config.getStoreLocation(type)));
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

}
