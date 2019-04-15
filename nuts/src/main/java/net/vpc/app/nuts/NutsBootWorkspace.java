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
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public static final Logger log = Logger.getLogger(NutsBootWorkspace.class.getName());
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
    //    String workspaceLocation;
    private NutsBootId bootId;
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
        this.bootId = NutsBootId.parse(NutsConstants.Ids.NUTS_API + "#" + actualVersion);
        newInstanceRequirements = 0;
//        if (options.getBootCommand() != null) {
//            switch (options.getBootCommand()) {
//                case UPDATE:
//                case CHECK_UPDATES:
//                case CLEANUP:
//                case RESET: {
//                    newInstanceRequirements = false;
//                }
//            }
//        }
        runningBootConfig = new NutsBootConfig(options);
        NutsLogUtils.bootstrap(options.getLogConfig());
        log.log(Level.CONFIG, "Open Nuts Workspace : {0}", options.getBootArgumentsString(true, true, true));
        loadedBootConfig = expandAllPaths(runningBootConfig);
        NutsLogUtils.prepare(options.getLogConfig(), NutsUtils.syspath(runningBootConfig.getStoreLocation(NutsStoreLocation.LOGS) + "/net/vpc/app/nuts/nuts/" + actualVersion));

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
        this.runtimeId = NutsUtils.isBlank(options.getBootRuntime()) ? null : NutsBootId.parse(options.getBootRuntime()).toString();
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
        log.log(Level.FINE, "Running version {0}.  {1}", new Object[]{actualVersion, getRequirementsHelpString(true)});
        StringBuilder errors = new StringBuilder();
        if (NutsConstants.Versions.LATEST.equalsIgnoreCase(requiredBootVersion) || NutsConstants.Versions.RELEASE.equalsIgnoreCase(requiredBootVersion)) {
            String releaseVersion = null;
            try {
                String NUTS_ID_BOOT_API_PATH = "/" + NutsConstants.Ids.NUTS_API.replaceAll("[.:]", "/");
                releaseVersion = NutsUtils.resolveMavenReleaseVersion(NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT, NUTS_ID_BOOT_API_PATH);
                requiredBootVersion = releaseVersion;
            } catch (Exception ex) {
                errors.append("Unable to load nuts version from " + NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT + ".\n");
                throw new NutsIllegalArgumentException("Unable to load nuts version from " + NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT);
            }
            System.out.println("detected version " + requiredBootVersion);
        }

        String defaultWorkspaceLibFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        if (!NutsUtils.getSystemBoolean("nuts.export.no-m2", false)) {
            repos.add(System.getProperty("user.home") + NutsUtils.syspath("/.m2/repository"));
        }
        repos.addAll(Arrays.asList(NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT,
                NutsConstants.BootsrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootsrapURLs.REMOTE_MAVEN_CENTRAL
        ));
        File file = NutsUtils.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion,
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
            throw new NutsIllegalArgumentException("Unable to load " + NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion);
        }

        List<String> cmd = new ArrayList<>();
        String jc = requiredJavaCommand;
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
//        Collections.addAll(cmd, NutsMinimalCommandLine.parseCommandLine(options.getBootJavaOptions()));
//        cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        cmd.add("-jar");
        cmd.add(file.getPath());
        //cmd.add("--verbose");
        cmd.addAll(Arrays.asList(options.getBootArguments(true, true, true)));
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
            if (!NutsUtils.isBlank(info.bootConfig0.getRuntimeId())
                    && !NutsUtils.isBlank(info.bootConfig0.getRuntimeDependencies())) {
                log.log(Level.CONFIG, "[RECOV. ] Invalidating old  runtime.");
            }
            info.bootConfig0.setRuntimeId(null);
            info.bootConfig0.setRuntimeDependencies(null);
        }
        if (!NutsUtils.isBlank(info.bootConfig0.getApiVersion()) && !NutsUtils.isBlank(info.bootConfig0.getRuntimeId()) && !NutsUtils.isBlank(info.bootConfig0.getRuntimeDependencies())) {
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

        String workspaceBootLibFolder = runningBootConfig.getBootsrap();
        NutsBootId bootRuntime = null;
        if (NutsUtils.isBlank(info.actualBootConfig.getRuntimeId())) {
            bootRuntime = NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
        } else if (info.actualBootConfig.getRuntimeId().contains("#")) {
            bootRuntime = NutsBootId.parse(info.actualBootConfig.getRuntimeId());
        } else {
            bootRuntime = NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.actualBootConfig.getRuntimeId());
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
                info.nutsWorkspace.config().save();
            }
        }
    }

    public void run() {
        switch (this.getOptions().getBootCommand()) {
            case RESET: {
                actionReset(null, getOptions().getApplicationArguments());
                return;
            }
        }
        if (hasUnsatisfiedRequirements()) {
            startNewProcess();
            return;
        }
        NutsWorkspace workspace = null;
        try {
            workspace = this.openWorkspace();
        } catch (NutsException ex) {
            log.log(Level.SEVERE, "Open Workspace Failed", ex);
            switch (this.getOptions().getBootCommand()) {
                case VERSION:
                case HELP: {
                    try {
                        runWorkspaceCommand(null, "Cannot start workspace to run command " + getOptions().getBootCommand() + ". " + ex.getMessage());
                        return;
                    } catch (NutsUserCancelException e) {
                        System.err.println(e.getMessage());
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    }
                }
            }
            throw ex;
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
            throw new NutsExecutionException(ex.getMessage(), ex, x);
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
        if (this.getOptions().getInitMode() != null) {
            switch (this.getOptions().getInitMode()) {
                case CLEANUP: {
                    actionReset(null, new String[]{"cleanup"});
                    break;
                }
                case RESET: {
                    actionReset(null, new String[]{"all"});
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
                if (!new File(runningBootConfig.getWorkspace(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).isFile()) {
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

    private String[] resolveBootClassPathRepositories(String... possibilities) {
        List<String> initial = new ArrayList<>();
        initial.add(runtimeSourceURL);
//        initial.add(home + "/" + NutsConstants.BOOTSTRAP_REPOSITORY_NAME);
        if (!NutsUtils.getSystemBoolean("nuts.export.no-m2", false)) {
            initial.add(NutsConstants.BootsrapURLs.LOCAL_MAVEN_CENTRAL);
        }
        if (possibilities != null) {
            initial.addAll(Arrays.asList(possibilities));
        }
        initial.add("${workspace}/" + NutsConstants.Folders.REPOSITORIES + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME + "/" + NutsConstants.Folders.LIB);
        initial.add(NutsConstants.BootsrapURLs.REMOTE_MAVEN_GIT);
        initial.add(NutsConstants.BootsrapURLs.REMOTE_MAVEN_CENTRAL);
        initial.add(NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT);
        return NutsUtils.splitAndRemoveDuplicates(initial.toArray(new String[0]));
    }

    private NutsBootConfig buildNutsBootConfig(NutsBootConfig bootConfig0, boolean recover) {
        String cacheFolder = runningBootConfig.getBootsrap();
        NutsBootId apiId = NutsUtils.isBlank(bootConfig0.getApiVersion()) ? bootId : new NutsBootId(bootId.getGroupId(), bootId.getArtifactId(), bootConfig0.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");
        String runtimeId = bootConfig0.getRuntimeId();
        String repositories = bootConfig0.getRepositories();
        boolean cacheRemoteOnly = true;
        if (bootConfig0.getStoreLocationStrategy() == NutsStoreLocationStrategy.STANDALONE) {
            cacheRemoteOnly = false;
        }
        List<String> resolvedBootRepositories = new ArrayList<>();
        if (NutsUtils.isBlank(runtimeId) || NutsUtils.isBlank(repositories)) {
            resolvedBootRepositories.addAll(Arrays.asList(NutsUtils.splitAndRemoveDuplicates(this.runtimeSourceURL, NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT)));
            for (String repo : resolvedBootRepositories) {
                URL urlString = buildURL(repo, bootAPIPropertiesPath);
                if (urlString != null) {
                    Properties wruntimeProperties = NutsUtils.loadURLProperties(urlString, new File(cacheFolder, bootAPIPropertiesPath.replace('/', File.separatorChar)), !recover);
                    if (!wruntimeProperties.isEmpty()) {
                        String wruntimeId = wruntimeProperties.getProperty("bootRuntimeId");
                        String wrepositories = wruntimeProperties.getProperty("repositories");
                        if (!NutsUtils.isBlank(wruntimeId) && !NutsUtils.isBlank(wrepositories)) {
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

        if (NutsUtils.isBlank(runtimeId)) {
            runtimeId = NutsConstants.Ids.NUTS_RUNTIME + "#" + apiId.getVersion() + ".0";
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
                    if (!NutsUtils.isBlank(id) && !NutsUtils.isBlank(version)) {
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
                runtimeId0 = NutsConstants.Ids.NUTS_RUNTIME + "#" + runtimeVersion;
            }
            log.log(Level.CONFIG, "Loading Default Runtime ClassPath {0}", runtimeVersion);
            LinkedHashSet<String> jarRepositories = new LinkedHashSet();
            if (!NutsUtils.getSystemBoolean("nuts.boot.no-m2", false)) {
                jarRepositories.add(NutsConstants.BootsrapURLs.LOCAL_MAVEN_CENTRAL);
            }
            jarRepositories.addAll(Arrays.asList(NutsConstants.BootsrapURLs.REMOTE_MAVEN_GIT,
                    NutsConstants.BootsrapURLs.REMOTE_MAVEN_CENTRAL,
                    NutsConstants.BootsrapURLs.REMOTE_NUTS_GIT
            ));
            if (!NutsUtils.isBlank(repositories)) {
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
        if (NutsUtils.isBlank(workspace)) {
            workspace = NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
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

    private void runWorkspaceCommand(NutsWorkspace workspace, String message) {
        NutsWorkspaceOptions o = this.getOptions();
        if (log.isLoggable(Level.CONFIG)) {
            log.log(Level.CONFIG, "Running workspace command : {0}", o.getBootCommand());
        }
        switch (o.getBootCommand()) {
            case VERSION: {
                if (workspace == null) {
                    System.out.println("nuts-version :" + actualVersion);
                    return;
                }
                workspace.exec().command("version").command(o.getApplicationArguments()).failFast().exec();
                return;
            }

            case HELP: {
                if (workspace == null) {
                    System.out.println("Nuts is a package manager mainly for java applications.");
                    System.err.println("Unluckily it was unable to locate nuts-core component which esessential for its execution.\n");
                    System.out.println("nuts-version :" + actualVersion);
                    System.out.println("Try to reinstall nuts (with internet access available) and type 'nuts help' to get a list of global options and commands");
                    return;
                }
                workspace.exec().command("help").command(o.getApplicationArguments()).failFast().exec();
                return;
            }
            case RESET: {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, message);
                }
                actionReset(workspace, getOptions().getApplicationArguments());
                return;
            }
        }
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsExecutionException("Workspace boot command not available : " + o.getBootCommand(), 1);
        }
        if (o.getApplicationArguments().length == 0) {
            workspace.exec().command("version").failFast().exec();
//            workspace.getTerminal().getFormattedOut().println(workspace.getWelcomeText());
            return;
        }
        workspace.exec()
                .command(o.getApplicationArguments())
                .executorOptions(o.getExecutorOptions())
                .executionType(o.getExecutionType())
                .failFast()
                .exec();
    }

    private void actionReset(NutsWorkspace workspace, String[] readArguments) {
        NutsWorkspaceOptions o = getOptions();
        NutsWorkspaceConfigManager conf = workspace == null ? null : workspace.config();
        boolean force = false;
        Set<NutsStoreLocation> toDelete = new HashSet();

        for (String argument : readArguments) {
            if ("-f".equals(argument) || "--force".equals(argument)) {
                force = true;
            } else {
                if (!argument.startsWith("-")) {
                    NutsStoreLocation z = null;
                    try {
                        z = NutsStoreLocation.valueOf(argument.trim().toUpperCase());
                    } catch (Exception ex) {
                        //ignore
                    }
                    if (z != null) {
                        toDelete.add(z);
                    } else {
                        switch (argument) {
                            case "soft":
                            case "cleanup": {
                                toDelete.add(NutsStoreLocation.CACHE);
                                toDelete.add(NutsStoreLocation.TEMP);
                                toDelete.add(NutsStoreLocation.LOGS);
                                break;
                            }
                            case "all": {
                                for (NutsStoreLocation v : NutsStoreLocation.values()) {
                                    toDelete.add(v);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        boolean yes = o.isYes();
        boolean no = o.isNo();
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
                + "You are about to delete workspace files.\n"
                + "Are you sure this is what you want ?";
        NutsUtils.deleteAndConfirmAll(folders.toArray(new File[0]), force, header, workspace != null ? workspace.getTerminal() : null);
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
            config.setApiVersion(lastConfigLoaded.getApiVersion());
            config.setRuntimeId(lastConfigLoaded.getRuntimeId());
            config.setJavaCommand(lastConfigLoaded.getJavaCommand());
            config.setJavaOptions(lastConfigLoaded.getJavaOptions());
            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                config.setStoreLocation(type, lastConfigLoaded.getStoreLocation(type));
            }
            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
                for (NutsStoreLocation loc : NutsStoreLocation.values()) {
                    String llid = layout.name().toLowerCase() + "." + loc.name().toLowerCase();
                    String homeLocation = lastConfigLoaded.getHomeLocation(layout, loc);
                    if (!NutsUtils.isBlank(homeLocation)) {
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
                if (NutsUtils.isBlank(homes[type.ordinal()])) {
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
                        if (NutsUtils.isBlank(config.getStoreLocation(type))) {
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
        if (!NutsUtils.isBlank(requiredBootVersion)) {
            if ((insatistfiedOnly && !requiredBootVersion.equals(actualVersion)) || !insatistfiedOnly) {
                req += 1;
            }
        }
        if (!NutsUtils.isBlank(requiredJavaCommand)) {
            if ((insatistfiedOnly && NutsUtils.isActualJavaCommand(requiredJavaCommand)) || !insatistfiedOnly) {
                req += 2;
            }
        }
        if (!NutsUtils.isBlank(requiredJavaOptions)) {
            if ((insatistfiedOnly && NutsUtils.isActualJavaOptions(requiredJavaOptions)) || !insatistfiedOnly) {
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
