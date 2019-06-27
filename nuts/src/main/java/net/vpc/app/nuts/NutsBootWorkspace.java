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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static net.vpc.app.nuts.NutsUtilsLimited.readStringFromFile;
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

    private static final boolean NO_M2 = NutsUtilsLimited.getSysBoolNutsProperty("no-m2", false);
    public static final Logger LOG = Logger.getLogger(NutsBootWorkspace.class.getName());
    private final long creationTime = System.currentTimeMillis();
    private NutsWorkspaceOptions options;
    private String runtimeId;
    private String actualVersion;
    private NutsClassLoaderProvider contextClassLoaderProvider;
    private int newInstanceRequirements;
    private NutsBootConfig runningBootConfig;
    private String requiredJavaCommand;
    private String requiredBootVersion;
    private String requiredJavaOptions;
    private NutsIdLimited bootId;
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
        this.bootId = NutsIdLimited.parse(NutsConstants.Ids.NUTS_API + "#" + actualVersion);
        newInstanceRequirements = 0;
        NutsLogUtils.bootstrap(options.getLogConfig());
        LOG.log(Level.CONFIG, "Start Nuts : {0}", Instant.ofEpochMilli(creationTime).toString());
        LOG.log(Level.CONFIG, "Open Nuts Workspace : {0}", options.format().getBootCommandLine());
        LOG.log(Level.CONFIG, "Open Nuts Workspace (compact) : {0}", options.format().compact().getBootCommandLine());
        runningBootConfig = new NutsBootConfig(options);
        rebuildConfig(runningBootConfig);
        NutsLogUtils.prepare(options.getLogConfig(), NutsUtilsLimited.syspath(runningBootConfig.getStoreLocation(NutsStoreLocation.LOG) + "/net/vpc/app/nuts/nuts/" + actualVersion));

        requiredBootVersion = options.getRequiredBootVersion();
        if (requiredBootVersion == null) {
            requiredBootVersion = runningBootConfig.getApiVersion();
        }
        requiredJavaCommand = options.getBootJavaCommand();
        if (requiredJavaCommand == null) {
            requiredJavaCommand = runningBootConfig.getJavaCommand();
        }
        requiredJavaOptions = options.getBootJavaOptions();
        if (requiredJavaOptions == null) {
            requiredJavaOptions = runningBootConfig.getJavaOptions();
        }
        newInstanceRequirements = checkRequirements(true);
        if (newInstanceRequirements == 0) {
            runningBootConfig.setApiVersion(actualVersion);
            runningBootConfig.setJavaCommand(null);
            runningBootConfig.setJavaOptions(null);
        }
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
        if (!NO_M2) {
            repos.add(System.getProperty("user.home") + NutsUtilsLimited.syspath("/.m2/repository"));
        }
        repos.addAll(Arrays.asList(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
        ));
        File file = NutsUtilsLimited.resolveOrDownloadJar(NutsConstants.Ids.NUTS_API + "#" + requiredBootVersion,
                repos.toArray(new String[0]),
                runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE) + File.separator + NutsConstants.Folders.BOOT
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
        info.userBootConfig = runningBootConfig.copy();
        if (recover) {
            if (!NutsUtilsLimited.isBlank(info.userBootConfig.getRuntimeId())
                    && !NutsUtilsLimited.isBlank(info.userBootConfig.getRuntimeDependencies())) {
                LOG.log(Level.CONFIG, "[RECOV. ] Invalidating old  runtime.");
            }
            info.userBootConfig.setRuntimeId(null);
            info.userBootConfig.setRuntimeDependencies(null);
        }
        if (!NutsUtilsLimited.isBlank(info.userBootConfig.getApiVersion()) && !NutsUtilsLimited.isBlank(info.userBootConfig.getRuntimeId()) && !NutsUtilsLimited.isBlank(info.userBootConfig.getRuntimeDependencies())) {
            //Ok
        } else {
            info.userBootConfig = buildNutsBootConfig(info.userBootConfig, recover);
        }
        if (info.userBootConfig != null && !actualVersion.equals(info.userBootConfig.getApiVersion())) {
            LOG.log(Level.CONFIG, "Nuts Workspace version {0} does not match runtime version {1}. Resolving best dependencies.", new Object[]{info.userBootConfig.getApiVersion(), actualVersion});
            info.runningBootConfig = buildNutsBootConfig(info.userBootConfig, recover);
        } else {
            info.runningBootConfig = info.userBootConfig;
        }

        if (info.runningBootConfig == null) {
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load ClassPath");
        }

        String workspaceBootLibFolder = runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE) + File.separator + NutsConstants.Folders.BOOT;
        NutsIdLimited bootRuntime;
        if (NutsUtilsLimited.isBlank(info.runningBootConfig.getRuntimeId())) {
            bootRuntime = NutsIdLimited.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.runningBootConfig.getRuntimeId());
        } else if (info.runningBootConfig.getRuntimeId().contains("#")) {
            bootRuntime = NutsIdLimited.parse(info.runningBootConfig.getRuntimeId());
        } else {
            bootRuntime = NutsIdLimited.parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + info.runningBootConfig.getRuntimeId());
        }
        String[] repositories = NutsUtilsLimited.splitUrlStrings(info.runningBootConfig.getRepositories()).toArray(new String[0]);
        File f = getBootFile(bootRuntime, getFileName(bootRuntime, "jar"), repositories, workspaceBootLibFolder, !recover);
        if (f == null || !f.isFile()) {
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + bootRuntime + ". Unable to resolve file "
                    + (f == null ? getFileName(bootRuntime, "jar") : f.getPath())
            );
        }

        allExtensionFiles.put(info.runningBootConfig.getRuntimeId(), f);
        for (String idStr : NutsUtilsLimited.split(info.runningBootConfig.getRuntimeDependencies(), "\n\t ;,")) {
            NutsIdLimited id = NutsIdLimited.parse(idStr);
            f = getBootFile(id, getFileName(id, "jar"), repositories, workspaceBootLibFolder, !recover);
            if (f == null) {
                throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load " + id);
            }
            allExtensionFiles.put(id.toString(), f);
        }
        for (String idStr : NutsUtilsLimited.split(info.runningBootConfig.getExtensionDependencies(), "\n\t ;,")) {
            NutsIdLimited id = NutsIdLimited.parse(idStr);
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
                LOG.log(Level.CONFIG, "\t {0}", new Object[]{NutsUtilsLimited.formatURL(bootClassWorldURL)});
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
                System.err.printf("\t %s%n", NutsUtilsLimited.formatURL(url));
            }
            LOG.log(Level.SEVERE, "Unable to load Workspace Component from ClassPath : {0}", Arrays.asList(info.bootClassWorldURLs));
            throw new NutsInvalidWorkspaceException(null, this.runningBootConfig.getWorkspace(), "Unable to load Workspace Component from ClassPath : " + Arrays.asList(info.bootClassWorldURLs));
        }
        LOG.log(Level.FINE, "Initialize Workspace");
        ((NutsWorkspaceSPI) info.nutsWorkspace).initializeWorkspace(factoryInstance, info.runningBootConfig, info.userBootConfig,
                info.bootClassWorldURLs,
                info.workspaceClassLoader, options.copy());
        if (recover) {
//            info.nutsWorkspace.getConfigManager().setBootConfig(new NutsBootConfig());
            if (!info.nutsWorkspace.config().isReadOnly()) {
                LOG.log(Level.SEVERE, "Save Workspace");
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
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, wil exit before creating workspace
        if (options.getApplicationArguments().length == 0 && options.isSkipWelcome()
                && (options.getBootCommand() == NutsBootCommand.RECOVER || options.getBootCommand() == NutsBootCommand.RESET)) {
            throw new NutsExecutionException(null, 0);
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Open Workspace with command line  : {0}", options.format().getBootCommandLine());
            LOG.log(Level.CONFIG, "Open Workspace with config        : ");
            LOG.log(Level.CONFIG, "\t nuts-uuid                      : {0}", NutsUtilsLimited.desc(runningBootConfig.getUuid()));
            LOG.log(Level.CONFIG, "\t nuts-name                      : {0}", NutsUtilsLimited.desc(runningBootConfig.getName()));
            LOG.log(Level.CONFIG, "\t nuts-api-version               : {0}", actualVersion);
            LOG.log(Level.CONFIG, "\t nuts-workspace                 : {0}", NutsUtilsLimited.formatLogValue(options.getWorkspace(), runningBootConfig.getWorkspace()));
            LOG.log(Level.CONFIG, "\t nuts-store-apps                : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.APPS), runningBootConfig.getStoreLocation(NutsStoreLocation.APPS)));
            LOG.log(Level.CONFIG, "\t nuts-store-config              : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.CONFIG), runningBootConfig.getStoreLocation(NutsStoreLocation.CONFIG)));
            LOG.log(Level.CONFIG, "\t nuts-store-var                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.VAR), runningBootConfig.getStoreLocation(NutsStoreLocation.VAR)));
            LOG.log(Level.CONFIG, "\t nuts-store-log                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.LOG), runningBootConfig.getStoreLocation(NutsStoreLocation.LOG)));
            LOG.log(Level.CONFIG, "\t nuts-store-temp                : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.TEMP), runningBootConfig.getStoreLocation(NutsStoreLocation.TEMP)));
            LOG.log(Level.CONFIG, "\t nuts-store-cache               : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.CACHE), runningBootConfig.getStoreLocation(NutsStoreLocation.CACHE)));
            LOG.log(Level.CONFIG, "\t nuts-store-run                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.RUN), runningBootConfig.getStoreLocation(NutsStoreLocation.RUN)));
            LOG.log(Level.CONFIG, "\t nuts-store-lib                 : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocation(NutsStoreLocation.LIB), runningBootConfig.getStoreLocation(NutsStoreLocation.LIB)));
            LOG.log(Level.CONFIG, "\t nuts-store-strategy            : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocationStrategy(), runningBootConfig.getStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-repos-store-strategy      : {0}", NutsUtilsLimited.formatLogValue(options.getRepositoryStoreLocationStrategy(), runningBootConfig.getRepositoryStoreLocationStrategy()));
            LOG.log(Level.CONFIG, "\t nuts-store-layout              : {0}", NutsUtilsLimited.formatLogValue(options.getStoreLocationLayout(), runningBootConfig.getStoreLocationLayout() == null ? "system" : runningBootConfig.getStoreLocationLayout().id()));
            LOG.log(Level.CONFIG, "\t option-read-only               : {0}", options.isReadOnly());
            LOG.log(Level.CONFIG, "\t option-trace                   : {0}", options.isTrace());
            LOG.log(Level.CONFIG, "\t inherited                      : {0}", options.isInherited());
            LOG.log(Level.CONFIG, "\t inherited-nuts-boot-args       : {0}", NutsUtilsLimited.desc(System.getProperty("nuts.boot.args")));
            LOG.log(Level.CONFIG, "\t inherited-nuts-args            : {0}", NutsUtilsLimited.desc(System.getProperty("nuts.args")));
            LOG.log(Level.CONFIG, "\t option-open-mode               : {0}", NutsUtilsLimited.formatLogValue(options.getOpenMode(), options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
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
            if (info.userBootConfig == null) {
                info.userBootConfig = new NutsBootConfig()
                        .setApiVersion(this.bootId.getVersion())
                        .setRuntimeId(runtimeId);
            }
            if (info.runningBootConfig == null) {
                info.runningBootConfig = new NutsBootConfig()
                        .setApiVersion(this.actualVersion)
                        .setRuntimeId(runtimeId);
            }
            showError(
                    info.runningBootConfig,
                    info.userBootConfig,
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
            // ignore any error
        }
        if (bestVersion == null) {
            return null;
        }
        return NutsConstants.Ids.NUTS_RUNTIME + "#" + bestVersion;
    }

    private NutsBootConfig buildNutsBootConfig(NutsBootConfig bootConfig0, boolean recover) {
        NutsIdLimited apiId = NutsUtilsLimited.isBlank(bootConfig0.getApiVersion()) ? bootId : new NutsIdLimited(bootId.getGroupId(), bootId.getArtifactId(), bootConfig0.getApiVersion());
        String bootAPIPropertiesPath = '/' + getPathFile(apiId, apiId.getArtifactId() + ".properties");
        NutsBootConfig recipient = bootConfig0.copy();
        recipient.setApiVersion(apiId.getVersion());
        String runtimeId = bootConfig0.getRuntimeId();
        if (NutsUtilsLimited.isBlank(runtimeId)) {
            runtimeId = resolveLatestRuntimeId(apiId.getVersion());
        }
        if (NutsUtilsLimited.isBlank(runtimeId)) {
            runtimeId = NutsConstants.Ids.NUTS_RUNTIME + "#" + apiId.getVersion() + ".0";
            LOG.log(Level.CONFIG, "[ERROR  ] Failed to load latest runtime id. Considering defaults : {1}", new Object[]{bootAPIPropertiesPath, runtimeId});
        }
        NutsIdLimited _runtimeId = NutsIdLimited.parse(runtimeId);
        recipient.setRuntimeId(runtimeId);
        String runtimeVersion = _runtimeId.getVersion();
        List<String> dependencies = new ArrayList<>();
        try {
            InputStream xml = null;
            String urlPath = "net/vpc/app/nuts/nuts-core/" + runtimeVersion + "/nuts-core-" + runtimeVersion + ".pom";
            if (!NO_M2) {
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
                            //this is maven dependency, using "compile"
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

        recipient.setRepositories(
                NutsUtilsLimited.join(";",
                        NutsUtilsLimited.splitAndRemoveDuplicates(recipient.getRepositories(), NutsUtilsLimited.join(";",
                                new String[]{
                                    NutsConstants.BootstrapURLs.LOCAL_MAVEN_CENTRAL,
                                    NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                                    NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL

                                }
                        )))
        );
        return recipient;
    }

    protected String getHome(NutsStoreLocation storeFolder) {
        return NutsPlatformUtils.resolveHomeFolder(
                runningBootConfig.getStoreLocationLayout(),
                storeFolder,
                runningBootConfig.getHomeLocations(),
                runningBootConfig.getDefaultHomeLocations(),
                runningBootConfig.isGlobal(),
                runningBootConfig.getName()
        );
    }

    protected String expandPath(String path, String base) {
        path = NutsUtilsLimited.replaceDollarString(path, pathExpansionConverter);
        if (NutsUtilsLimited.isURL(path)) {
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
            throw new NutsExecutionException(null, "Workspace boot command not available : " + o.getBootCommand(), 1);
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
        System.err.printf("  nuts-boot-api-version            : %s%n", NutsUtilsLimited.nvl(actualBootConfig.getApiVersion(), "<?> Not Found!"));
        System.err.printf("  nuts-boot-runtime                : %s%n", NutsUtilsLimited.nvl(actualBootConfig.getRuntimeId(), "<?> Not Found!"));
        System.err.printf("  nuts-workspace-api-version       : %s%n", NutsUtilsLimited.nvl(workspaceConfig.getApiVersion(), "<?> Not Found!"));
        System.err.printf("  nuts-workspace-runtime           : %s%n", NutsUtilsLimited.nvl(workspaceConfig.getRuntimeId(), "<?> Not Found!"));
        System.err.printf("  workspace-location               : %s%n", NutsUtilsLimited.nvl(workspace, "<default-location>"));
        System.err.printf("  nuts-store-apps                  : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.APPS));
        System.err.printf("  nuts-store-config                : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.CONFIG));
        System.err.printf("  nuts-store-var                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.VAR));
        System.err.printf("  nuts-store-log                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.LOG));
        System.err.printf("  nuts-store-temp                  : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.TEMP));
        System.err.printf("  nuts-store-cache                 : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.CACHE));
        System.err.printf("  nuts-store-run                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.RUN));
        System.err.printf("  nuts-store-lib                   : %s%n", workspaceConfig.getStoreLocation(NutsStoreLocation.LIB));
        System.err.printf("  nuts-store-strategy              : %s%n", NutsUtilsLimited.desc(workspaceConfig.getStoreLocationStrategy()));
        System.err.printf("  nuts-store-layout                : %s%n", NutsUtilsLimited.desc(workspaceConfig.getStoreLocationLayout()));
        System.err.printf("  nuts-boot-args                   : %s%n", options.format().getBootCommandLine());
        System.err.printf("  nuts-app-args                    : %s%n", Arrays.toString(options.getApplicationArguments()));
        System.err.printf("  option-read-only                 : %s%n", options.isReadOnly());
        System.err.printf("  option-trace                     : %s%n", options.isTrace());
        System.err.printf("  option-open-mode                 : %s%n", NutsUtilsLimited.desc(options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
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
     * resolves and expands paths and valid default values
     *
     * @param config boot config. Should contain home,workspace, and all
     * StoreLocation information
     * @return resolved config
     */
    private void rebuildConfig(NutsBootConfig config) {
        String ws = options.getWorkspace();
        int maxDepth = 36;
        NutsBootConfig lastConfigLoaded = null;
        String lastConfigPath = null;
        String workspace0 = config.getWorkspace();
        for (int i = 0; i < maxDepth; i++) {
            lastConfigPath
                    = NutsUtilsLimited.isValidWorkspaceName(ws)
                    ? NutsPlatformUtils.resolveHomeFolder(
                            null, null, null, null,
                            config.isGlobal(),
                            NutsUtilsLimited.resolveValidWorkspaceName(ws)
                    ) : NutsUtilsLimited.getAbsolutePath(ws);

            NutsBootConfig configLoaded = loadBootConfig(lastConfigPath);
            if (configLoaded == null) {
                //not loaded
                break;
            }
            if (NutsUtilsLimited.isBlank(configLoaded.getWorkspace())) {
                lastConfigLoaded = configLoaded;
                break;
            }
            ws = configLoaded.getWorkspace();
            if (i >= maxDepth - 1) {
                throw new NutsIllegalArgumentException(null, "Cyclic Workspace resolution");
            }
        }
        boolean namedWorkspace = NutsUtilsLimited.isValidWorkspaceName(workspace0);
        config.setWorkspace(lastConfigPath);
        if (lastConfigLoaded != null) {
            config.setWorkspace(lastConfigPath);
            config.setName(lastConfigLoaded.getName());
            config.setUuid(lastConfigLoaded.getUuid());
            config.setApiVersion(lastConfigLoaded.getApiVersion());
            config.setRuntimeId(lastConfigLoaded.getRuntimeId());
            config.setRuntimeDependencies(lastConfigLoaded.getRuntimeDependencies());
            config.setExtensionDependencies(lastConfigLoaded.getExtensionDependencies());
            config.setRepositories(lastConfigLoaded.getRepositories());
            config.setJavaCommand(lastConfigLoaded.getJavaCommand());
            config.setJavaOptions(lastConfigLoaded.getJavaOptions());
            config.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
            config.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
            config.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
            for (NutsStoreLocation folder : NutsStoreLocation.values()) {
                config.setStoreLocation(folder, lastConfigLoaded.getStoreLocation(folder));
                config.setHomeLocation(null, folder, lastConfigLoaded.getHomeLocation(null, folder));
            }
            for (NutsOsFamily layout : NutsOsFamily.values()) {
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
        if (NutsUtilsLimited.isBlank(config.getName())) {
            config.setName(NutsUtilsLimited.resolveValidWorkspaceName(workspace0));
        }

        String[] homeLocations = config.getHomeLocations();
        String[] defaultHomeLocations = config.getDefaultHomeLocations();
        final NutsOsFamily storeLocationLayout = config.getStoreLocationLayout();
        if (storeLocationLayout == null) {
            config.setStoreLocationLayout(null);
        }
        if (config.getStoreLocationStrategy() == null) {
            config.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
        }
        if (config.getRepositoryStoreLocationStrategy() == null) {
            config.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
        }
        {
            String workspace = config.getWorkspace();
            String[] homes = new String[NutsStoreLocation.values().length];
            for (NutsStoreLocation type : NutsStoreLocation.values()) {
                homes[type.ordinal()] = NutsPlatformUtils.resolveHomeFolder(storeLocationLayout, type, homeLocations, defaultHomeLocations,
                        config.isGlobal(), config.getName());
                if (NutsUtilsLimited.isBlank(homes[type.ordinal()])) {
                    throw new NutsIllegalArgumentException(null, "Missing Home for " + type.name().toLowerCase());
                }
            }
            NutsStoreLocationStrategy storeLocationStrategy = config.getStoreLocationStrategy();
            if (storeLocationStrategy == null) {
                storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
            }
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
                                    config.setStoreLocation(type, homes[type.ordinal()]);
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
                                    config.setStoreLocation(type, homes[type.ordinal()] + NutsUtilsLimited.syspath("/" + config.getStoreLocation(type)));
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
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

    private static NutsBootConfig loadBootConfig(String workspaceLocation) {
        File versionFile = new File(workspaceLocation, NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (versionFile.isFile()) {
                String json = readStringFromFile(versionFile).trim();
                if (json.length() > 0) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.FINEST, "Loading Workspace Config {0}", versionFile.getPath());
                    }
                    return NutsBootConfigLoaderLimited.loadBootConfigJSON(json);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "Previous Workspace Config not found at {0}", versionFile.getPath());
            }
        } catch (Exception ex) {
            LOG.log(Level.CONFIG, "Unable to load nuts version file " + versionFile + ".\n", ex);
        }
        return null;
    }

    static class NutsBootClassLoader extends URLClassLoader {

        NutsBootClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }

    private static class OpenWorkspaceData {

        NutsBootConfig userBootConfig = null;
        NutsBootConfig runningBootConfig = null;
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
    }

    private static class BootSupportLevelContext implements NutsSupportLevelContext<NutsWorkspaceOptions> {

        private NutsWorkspaceOptions options;

        public BootSupportLevelContext(NutsWorkspaceOptions options) {
            this.options = options;
        }

        @Override
        public NutsWorkspace getWorkspace() {
            return null;
        }

        @Override
        public NutsWorkspaceOptions getConstraints() {
            return options;
        }
    }

    private static class NutsBootWorkspaceFactoryComparator implements Comparator<NutsBootWorkspaceFactory> {

        private NutsWorkspaceOptions options;

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
