package net.thevpc.nuts;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public interface NBootOptionsBuilder extends NComponent {
    static NBootOptionsBuilder of(){
        return NExtensions.of(NBootOptionsBuilder.class);
    }

    NBootOptionsBuilder copy();

    NBootOptions build();

//    NBootOptionsBuilder setBootRepositories(String bootRepositories);

    NBootOptionsBuilder setRuntimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode);

    NBootOptionsBuilder setExtensionBootDescriptors(List<NBootDescriptor> extensionBootDescriptors);

    NBootOptionsBuilder setExtensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes);

    NBootOptionsBuilder setBootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory);

    NBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs);

    NBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader);

    NBootOptionsBuilder setUuid(String uuid);

    NBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet);

    NBootOptionsBuilder setRuntimeBootDescriptor(NBootDescriptor runtimeBootDescriptor);


    NBootOptionsBuilder setInitLaunchers(Boolean initLaunchers);

    NBootOptionsBuilder setInitScripts(Boolean initScripts);

    NBootOptionsBuilder setInitPlatforms(Boolean initPlatforms);

    NBootOptionsBuilder setInitJava(Boolean initJava);

    NBootOptionsBuilder setIsolationLevel(NIsolationLevel isolationLevel);

    NBootOptionsBuilder setDesktopLauncher(NSupportMode desktopLauncher);

    NBootOptionsBuilder setMenuLauncher(NSupportMode menuLauncher);

    NBootOptionsBuilder setUserLauncher(NSupportMode userLauncher);

    NBootOptionsBuilder setApiVersion(NVersion apiVersion);

    NBootOptionsBuilder setApplicationArguments(List<String> applicationArguments);


    NBootOptionsBuilder setArchetype(String archetype);

    NBootOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider);


    NBootOptionsBuilder setConfirm(NConfirmationMode confirm);

    NBootOptionsBuilder setDry(Boolean dry);

    NBootOptionsBuilder setShowStacktrace(Boolean showStacktrace);

    NBootOptionsBuilder setCreationTime(Instant creationTime);


    NBootOptionsBuilder setExcludedExtensions(List<String> excludedExtensions);

    NBootOptionsBuilder setExecutionType(NExecutionType executionType);

    NBootOptionsBuilder setSharedInstance(Boolean sharedInstance);

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder setRunAs(NRunAs runAs);

    NBootOptionsBuilder setExecutorOptions(List<String> executorOptions);

    /**
     * set home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NBootOptionsBuilder setHomeLocations(Map<NHomeLocation, String> homeLocations);

    NBootOptionsBuilder setJavaCommand(String javaCommand);


    NBootOptionsBuilder setJavaOptions(String javaOptions);


    NBootOptionsBuilder setLogConfig(NLogConfig logConfig);


    NBootOptionsBuilder setName(String workspaceName);

    NBootOptionsBuilder setOpenMode(NOpenMode openMode);


    NBootOptionsBuilder setOutputFormat(NContentType outputFormat);


    NBootOptionsBuilder setOutputFormatOptions(List<String> options);


    NBootOptionsBuilder setCredentials(char[] credentials);


    NBootOptionsBuilder setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy);


    NBootOptionsBuilder setRuntimeId(NId runtimeId);

    NBootOptionsBuilder setStoreLayout(NOsFamily storeLayout);

    NBootOptionsBuilder setStoreStrategy(NStoreStrategy storeStrategy);


    /**
     * set store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NBootOptionsBuilder setStoreLocations(Map<NStoreType, String> storeLocations);

    NBootOptionsBuilder setTerminalMode(NTerminalMode terminalMode);

    NBootOptionsBuilder setRepositories(List<String> transientRepositories);
    NBootOptionsBuilder setBootRepositories(List<String> transientRepositories);

    NBootOptionsBuilder setWorkspace(String workspace);

    NBootOptionsBuilder setDebug(String debug);

    /**
     * update 'global' option.
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @param global if true consider global/system repository shared between all users
     * @return if true consider global/system repository
     */
    NBootOptionsBuilder setSystem(Boolean global);

    NBootOptionsBuilder setGui(Boolean gui);

    NBootOptionsBuilder setInherited(Boolean inherited);


    NBootOptionsBuilder setReadOnly(Boolean readOnly);

    NBootOptionsBuilder setRecover(Boolean recover);

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     *
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NBootOptionsBuilder setReset(Boolean reset);

    /**
     * set resetHard flag.
     * Reset hard is used at boot time to reset ALL of nuts workspaces
     * @param resetHard resetHard flag
     * @return {@code this}
     */
    NBootOptionsBuilder setResetHard(Boolean resetHard);

    NBootOptionsBuilder setCommandVersion(Boolean version);

    NBootOptionsBuilder setCommandHelp(Boolean help);

    NBootOptionsBuilder setInstallCompanions(Boolean skipInstallCompanions);

    NBootOptionsBuilder setSkipWelcome(Boolean skipWelcome);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder setOutLinePrefix(String value);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder setErrLinePrefix(String value);

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @param skipBoot skipBoot
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */
    NBootOptionsBuilder setSkipBoot(Boolean skipBoot);

    NBootOptionsBuilder setTrace(Boolean trace);

    NBootOptionsBuilder setProgressOptions(String progressOptions);

    NBootOptionsBuilder setCached(Boolean cached);

    NBootOptionsBuilder setIndexed(Boolean indexed);

    NBootOptionsBuilder setTransitive(Boolean transitive);

    NBootOptionsBuilder setBot(Boolean bot);

    NBootOptionsBuilder setFetchStrategy(NFetchStrategy fetchStrategy);

    NBootOptionsBuilder setStdin(InputStream stdin);

    NBootOptionsBuilder setStdout(PrintStream stdout);

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NBootOptionsBuilder setStderr(PrintStream stderr);

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NBootOptionsBuilder setExecutorService(ExecutorService executorService);

    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder setExpireTime(Instant value);

    NBootOptionsBuilder setSkipErrors(Boolean value);

    NBootOptionsBuilder setSwitchWorkspace(Boolean value);

    NBootOptionsBuilder setErrors(List<NMsg> errors);

    NBootOptionsBuilder setCustomOptions(List<String> properties);

    /**
     * set locale
     *
     * @param locale value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder setLocale(String locale);

    /**
     * set theme
     *
     * @param theme value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder setTheme(String theme);

    NBootOptionsBuilder copyFrom(NWorkspaceOptions other);

    NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other);

    NBootOptionsBuilder copyFrom(NBootOptions other);
    NBootOptionsBuilder copyFrom(NBootOptionsBuilder other);

    NBootOptionsBuilder copyFromIfPresent(NBootOptions other);

    NBootOptionsBuilder setCmdLine(String cmdLine);

    NBootOptionsBuilder setCmdLine(String[] args);

    NBootOptionsBuilder setUserName(String username);

    NBootOptionsBuilder setStoreLocation(NStoreType location, String value);

    NBootOptionsBuilder setHomeLocation(NHomeLocation location, String value);

    NBootOptionsBuilder addOutputFormatOptions(String... options);

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NBootOptionsBuilder setDependencySolver(String dependencySolver);

    NBootOptionsBuilder unsetRuntimeOptions();

    NBootOptionsBuilder unsetCreationOptions();

    NBootOptionsBuilder unsetExportedOptions();


    /**
     * @since 0.8.5
     * @return this
     */
    NBootOptionsBuilder setPreviewRepo(Boolean bot);


//    NOptional<String> getBootRepositories();

    NOptional<NClassLoaderNode> getRuntimeBootDependencyNode();

    NOptional<List<NBootDescriptor>> getExtensionBootDescriptors();

    NOptional<List<NClassLoaderNode>> getExtensionBootDependencyNodes();

    NOptional<NBootWorkspaceFactory> getBootWorkspaceFactory();

    NOptional<List<URL>> getClassWorldURLs();

    NOptional<ClassLoader> getClassWorldLoader();

    NOptional<String> getUuid();

    NOptional<Set<String>> getExtensionsSet();

    NOptional<NBootDescriptor> getRuntimeBootDescriptor();

    NOptional<NSupportMode> getDesktopLauncher();

    NOptional<NSupportMode> getMenuLauncher();

    NOptional<NSupportMode> getUserLauncher();

    NOptional<NIsolationLevel> getIsolationLevel();

    NBootOptionsInfo toBootOptions();

    /**
     * init launcher
     *
     * @return init launcher
     * @since 0.8.4
     */
    NOptional<Boolean> getInitLaunchers();

    /**
     * init scripts
     *
     * @return init scripts
     * @since 0.8.4
     */
    NOptional<Boolean> getInitScripts();

    /**
     * init platforms
     *
     * @return init platforms
     * @since 0.8.4
     */
    NOptional<Boolean> getInitPlatforms();

    /**
     * init java
     *
     * @return init java
     * @since 0.8.4
     */
    NOptional<Boolean> getInitJava();

    /**
     * nuts api version to boot.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return nuts api version to boot.
     */
    NOptional<NVersion> getApiVersion();

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return application arguments.
     */
    NOptional<List<String>> getApplicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return workspace archetype to consider when creating a new workspace.
     */
    NOptional<String> getArchetype();

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return class loader supplier.
     */
    NOptional<Supplier<ClassLoader>> getClassLoaderSupplier();

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return confirm mode.
     */
    NOptional<NConfirmationMode> getConfirm();

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true no real execution, with dry exec (execute without side effect).
     */

    NOptional<Boolean> getDry();

    /**
     * if true, show exception stacktrace when error.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, show stacktrace when error.
     */

    NOptional<Boolean> getShowStacktrace();

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return workspace creation evaluated time.
     */
    NOptional<Instant> getCreationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return extensions to be excluded when opening the workspace.
     */
    NOptional<List<String>> getExcludedExtensions();

    /**
     * execution type.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return execution type.
     */
    NOptional<NExecutionType> getExecutionType();

    NOptional<NRunAs> getRunAs();

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return extra executor options.
     */
    NOptional<List<String>> getExecutorOptions();

    /**
     * return home location.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param location location
     * @return home location.
     */
    NOptional<String> getHomeLocation(NHomeLocation location);

    /**
     * return home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @return home locations
     */
    NOptional<Map<NHomeLocation, String>> getHomeLocations();

    /**
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java command (or java home) used to run workspace.
     */
    NOptional<String> getJavaCommand();

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java options used to run workspace.
     */
    NOptional<String> getJavaOptions();

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return workspace log configuration.
     */
    NOptional<NLogConfig> getLogConfig();

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return user friendly workspace name.
     */
    NOptional<String> getName();

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return mode used to open workspace.
     */
    NOptional<NOpenMode> getOpenMode();

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output format type.
     */
    NOptional<NContentType> getOutputFormat();

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output formation options.
     */
    NOptional<List<String>> getOutputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return credential needed to log into workspace.
     */
    NOptional<char[]> getCredentials();

    /**
     * repository store location strategy to consider when creating new repositories
     * for a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return repository store location strategy to consider when creating new repositories
     * for a new workspace.
     */
    NOptional<NStoreStrategy> getRepositoryStoreStrategy();

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return nuts runtime id (or version) to boot.
     */
    NOptional<NId> getRuntimeId();

    /**
     * store location for the given folder.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param folder folder type
     * @return store location for the given folder.
     */
    NOptional<String> getStoreType(NStoreType folder);


    /**
     * store location layout to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location layout to consider when creating a new workspace.
     */
    NOptional<NOsFamily> getStoreLayout();

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location strategy for creating a new workspace.
     */
    NOptional<NStoreStrategy> getStoreStrategy();

    /**
     * store locations map to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store locations map to consider when creating a new workspace.
     */
    NOptional<Map<NStoreType, String>> getStoreLocations();

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NOptional<NTerminalMode> getTerminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return repositories to register temporarily when running the workspace.
     */
    NOptional<List<String>> getRepositories();
    NOptional<List<String>> getBootRepositories();

    /**
     * username to log into when running workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return username to log into when running workspace.
     */
    NOptional<String> getUserName();

    /**
     * workspace folder location path.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return workspace folder location path.
     */
    NOptional<String> getWorkspace();

    NOptional<String> getDebug();

    /**
     * if true consider system repository
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider system repository
     */

    NOptional<Boolean> getSystem();

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider GUI/Swing mode
     */

    NOptional<Boolean> getGui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace were invoked from parent process and hence inherits its options.
     */

    NOptional<Boolean> getInherited();

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that It's possible to load external libraries.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, workspace configuration are non modifiable.
     */

    NOptional<Boolean> getReadOnly();

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, boot, cache and temp folder are deleted.
     */

    NOptional<Boolean> getRecover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */

    NOptional<Boolean> getReset();

    NOptional<Boolean> getResetHard();


    NOptional<Boolean> getCommandVersion();


    NOptional<Boolean> getCommandHelp();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, do not install nuts companion tools upon workspace creation.
     */

    NOptional<Boolean> getInstallCompanions();

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.5.5
     */

    NOptional<Boolean> getSkipWelcome();


    /**
     * if not null ant not empty, this prefix will be prefixed to output stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return out line prefix
     * @since 0.8.0
     */
    NOptional<String> getOutLinePrefix();

    /**
     * if not null ant not empty, this prefix will be prefixed to error stream
     * <br>
     * defaults to null.
     * <br>
     * <strong>option-type :</strong>  exported (inherited in child workspaces)
     *
     * @return err line prefix
     * @since 0.8.0
     */
    NOptional<String> getErrLinePrefix();

    /**
     * if true, do not bootstrap workspace after reset/recover.
     * When reset/recover is not active this option is not accepted and an error will be thrown
     * <br>
     * defaults to false.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, do not run welcome when no application arguments were resolved
     * @since 0.6.0
     */

    NOptional<Boolean> getSkipBoot();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */

    NOptional<Boolean> getTrace();

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    NOptional<String> getProgressOptions();

    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    NOptional<String> getDependencySolver();

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use cache when true
     */

    NOptional<Boolean> getCached();

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use index when true
     */
    NOptional<Boolean> getIndexed();

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */

    NOptional<Boolean> getTransitive();

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */

    NOptional<Boolean> getBot();

    /**
     * @return application is running in preview mode (using preview repositories)
     * @since 0.8.5
     */
    NOptional<Boolean> getPreviewRepo();

    /**
     * @return workspace is running as shared Workspace instance (Singleton)
     * @since 0.8.5
     */
    NOptional<Boolean> getSharedInstance();

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    NOptional<NFetchStrategy> getFetchStrategy();


    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard input or null
     */
    NOptional<InputStream> getStdin();

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard output or null
     */
    NOptional<PrintStream> getStdout();

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NOptional<PrintStream> getStderr();

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NOptional<ExecutorService> getExecutorService();

    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    NOptional<Instant> getExpireTime();

    NOptional<Boolean> getSkipErrors();

    NOptional<Boolean> getSwitchWorkspace();

    NOptional<List<NMsg>> getErrors();

    NOptional<List<String>> getCustomOptions();

    NOptional<NArg> getCustomOptionArg(String key);

    NOptional<String> getCustomOption(String key);

    NOptional<List<NArg>> getCustomOptionArgs();

    /**
     * locale
     *
     * @return session locale
     * @since 0.8.1
     */
    NOptional<String> getLocale();

    /**
     * theme
     *
     * @return session locale
     * @since 0.8.1
     */
    NOptional<String> getTheme();


}
