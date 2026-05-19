package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NMsg;
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
    static NBootOptionsBuilder of() {
        return NExtensions.of(NBootOptionsBuilder.class);
    }

    NBootOptionsBuilder copy();

    NBootOptions build();

//    NBootOptionsBuilder setBootRepositories(String bootRepositories);

    NBootOptionsBuilder runtimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode);

    NBootOptionsBuilder extensionBootDescriptors(List<NBootDescriptor> extensionBootDescriptors);

    NBootOptionsBuilder extensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes);

    NBootOptionsBuilder bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory);

    NBootOptionsBuilder classWorldURLs(List<URL> classWorldURLs);

    NBootOptionsBuilder classWorldLoader(ClassLoader classWorldLoader);

    NBootOptionsBuilder uuid(String uuid);

    NBootOptionsBuilder extensionsSet(Set<String> extensionsSet);

    NBootOptionsBuilder runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor);


    NBootOptionsBuilder initLaunchers(Boolean initLaunchers);

    NBootOptionsBuilder initScripts(Boolean initScripts);

    NBootOptionsBuilder initPlatforms(Boolean initPlatforms);

    NBootOptionsBuilder initJava(Boolean initJava);

    NBootOptionsBuilder isolationLevel(NIsolationLevel isolationLevel);

    NBootOptionsBuilder desktopLauncher(NSupportMode desktopLauncher);

    NBootOptionsBuilder menuLauncher(NSupportMode menuLauncher);

    NBootOptionsBuilder userLauncher(NSupportMode userLauncher);

    NBootOptionsBuilder apiVersion(NVersion apiVersion);

    NBootOptionsBuilder applicationArguments(List<String> applicationArguments);


    NBootOptionsBuilder archetype(String archetype);

    NBootOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider);


    NBootOptionsBuilder confirm(NConfirmationMode confirm);

    NBootOptionsBuilder cry(Boolean dry);

    NBootOptionsBuilder showStacktrace(Boolean showStacktrace);

    NBootOptionsBuilder creationTime(Instant creationTime);


    NBootOptionsBuilder excludedExtensions(List<String> excludedExtensions);

    NBootOptionsBuilder executionType(NExecutionType executionType);

    NBootOptionsBuilder sharedInstance(Boolean sharedInstance);

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder runAs(NRunAs runAs);

    NBootOptionsBuilder executorOptions(List<String> executorOptions);

    /**
     * set home locations.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime).
     *
     * @param homeLocations home locations map
     * @return {@code this} instance
     */
    NBootOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations);

    NBootOptionsBuilder javaCommand(String javaCommand);


    NBootOptionsBuilder javaOptions(String javaOptions);


    NBootOptionsBuilder logConfig(NLogConfig logConfig);


    NBootOptionsBuilder name(String workspaceName);

    NBootOptionsBuilder openMode(NOpenMode openMode);


    NBootOptionsBuilder outputFormat(NContentType outputFormat);


    NBootOptionsBuilder outputFormatOptions(List<String> options);


    NBootOptionsBuilder credential(char[] credentials);


    NBootOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy);


    NBootOptionsBuilder runtimeId(NId runtimeId);

    NBootOptionsBuilder storeLayout(NOsFamily storeLayout);

    NBootOptionsBuilder storeStrategy(NStoreStrategy storeStrategy);


    /**
     * set store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @param storeLocations store locations map
     * @return {@code this} instance
     */
    NBootOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations);

    NBootOptionsBuilder terminalMode(NTerminalMode terminalMode);

    NBootOptionsBuilder repositories(List<String> transientRepositories);

    NBootOptionsBuilder bootRepositories(List<String> transientRepositories);

    NBootOptionsBuilder workspace(String workspace);

    NBootOptionsBuilder debug(String debug);

    /**
     * update 'global' option.
     * if true consider global/system repository shared between all users
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @param global if true consider global/system repository shared between all users
     * @return if true consider global/system repository
     */
    NBootOptionsBuilder system(Boolean global);

    NBootOptionsBuilder gui(Boolean gui);

    NBootOptionsBuilder inherited(Boolean inherited);


    NBootOptionsBuilder readOnly(Boolean readOnly);

    NBootOptionsBuilder recover(Boolean recover);

    /**
     * set the 'reset' flag to delete the workspace folders and files.
     * This is equivalent to the following nuts command line options :
     * "-Z" and "--reset" options
     *
     * @param reset reset flag, when null inherit default ('false')
     * @return {@code this} instance
     */
    NBootOptionsBuilder reset(Boolean reset);

    /**
     * set resetHard flag.
     * Reset hard is used at boot time to reset ALL of nuts workspaces
     *
     * @param resetHard resetHard flag
     * @return {@code this}
     */
    NBootOptionsBuilder resetHard(Boolean resetHard);

    NBootOptionsBuilder commandVersion(Boolean version);

    NBootOptionsBuilder commandHelp(Boolean help);

    NBootOptionsBuilder installCompanions(Boolean skipInstallCompanions);

    NBootOptionsBuilder skipWelcome(Boolean skipWelcome);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder outLinePrefix(String value);

    /**
     * set output line prefix
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder errLinePrefix(String value);

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
    NBootOptionsBuilder skipBoot(Boolean skipBoot);

    NBootOptionsBuilder trace(Boolean trace);

    NBootOptionsBuilder progressOptions(String progressOptions);

    NBootOptionsBuilder cached(Boolean cached);

    NBootOptionsBuilder indexed(Boolean indexed);

    NBootOptionsBuilder transitive(Boolean transitive);

    NBootOptionsBuilder bot(Boolean bot);

    NBootOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy);

    NBootOptionsBuilder stdin(InputStream stdin);

    NBootOptionsBuilder stdout(PrintStream stdout);

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NBootOptionsBuilder stderr(PrintStream stderr);

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NBootOptionsBuilder executorService(ExecutorService executorService);

    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NBootOptionsBuilder expireTime(Instant value);

    NBootOptionsBuilder skipErrors(Boolean value);

    NBootOptionsBuilder switchWorkspace(Boolean value);

    NBootOptionsBuilder errors(List<NMsg> errors);

    NBootOptionsBuilder customOptions(List<String> properties);

    /**
     * set locale
     *
     * @param locale value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder locale(String locale);

    /**
     * set theme
     *
     * @param theme value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder theme(String theme);

    NBootOptionsBuilder copyFrom(NWorkspaceOptions other);

    NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other);

    NBootOptionsBuilder copyFrom(NBootOptions other);

    NBootOptionsBuilder copyFrom(NBootOptionsBuilder other);

    NBootOptionsBuilder copyFromIfPresent(NBootOptions other);

    NBootOptionsBuilder cmdLine(String cmdLine);

    NBootOptionsBuilder cmdLine(String[] args);

    NBootOptionsBuilder userName(String username);

    NBootOptionsBuilder storeLocation(NStoreType location, String value);

    NBootOptionsBuilder homeLocation(NHomeLocation location, String value);

    NBootOptionsBuilder addOutputFormatOptions(String... options);

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NBootOptionsBuilder dependencySolver(String dependencySolver);

    NBootOptionsBuilder unsetRuntimeOptions();

    NBootOptionsBuilder unsetCreationOptions();

    NBootOptionsBuilder unsetExportedOptions();


    /**
     * @return this
     * @since 0.8.5
     */
    NBootOptionsBuilder previewRepo(Boolean bot);


    NOptional<NClassLoaderNode> runtimeBootDependencyNode();

    NOptional<List<NBootDescriptor>> extensionBootDescriptors();

    NOptional<List<NClassLoaderNode>> extensionBootDependencyNodes();

    NOptional<NBootWorkspaceFactory> bootWorkspaceFactory();

    NOptional<List<URL>> classWorldURLs();

    NOptional<ClassLoader> classWorldLoader();

    NOptional<String> uuid();

    NOptional<Set<String>> extensionsSet();

    NOptional<NBootDescriptor> runtimeBootDescriptor();

    NOptional<NSupportMode> desktopLauncher();

    NOptional<NSupportMode> menuLauncher();

    NOptional<NSupportMode> userLauncher();

    NOptional<NIsolationLevel> isolationLevel();

    NBootOptionsInfo toBootOptions();

    /**
     * init launcher
     *
     * @return init launcher
     * @since 0.8.4
     */
    NOptional<Boolean> initLaunchers();

    /**
     * init scripts
     *
     * @return init scripts
     * @since 0.8.4
     */
    NOptional<Boolean> initScripts();

    /**
     * init platforms
     *
     * @return init platforms
     * @since 0.8.4
     */
    NOptional<Boolean> initPlatforms();

    /**
     * init java
     *
     * @return init java
     * @since 0.8.4
     */
    NOptional<Boolean> initJava();

    /**
     * nuts api version to boot.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return nuts api version to boot.
     */
    NOptional<NVersion> apiVersion();

    /**
     * application arguments.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return application arguments.
     */
    NOptional<List<String>> applicationArguments();

    /**
     * workspace archetype to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return workspace archetype to consider when creating a new workspace.
     */
    NOptional<String> archetype();

    /**
     * class loader supplier.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return class loader supplier.
     */
    NOptional<Supplier<ClassLoader>> classLoaderSupplier();

    /**
     * confirm mode.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return confirm mode.
     */
    NOptional<NConfirmationMode> confirm();

    /**
     * if true no real execution, with dry exec (execute without side effect).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true no real execution, with dry exec (execute without side effect).
     */

    NOptional<Boolean> dry();

    /**
     * if true, show exception stacktrace when error.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, show stacktrace when error.
     */

    NOptional<Boolean> showStacktrace();

    /**
     * workspace creation evaluated time.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return workspace creation evaluated time.
     */
    NOptional<Instant> creationTime();

    /**
     * extensions to be excluded when opening the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return extensions to be excluded when opening the workspace.
     */
    NOptional<List<String>> excludedExtensions();

    /**
     * execution type.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return execution type.
     */
    NOptional<NExecutionType> executionType();

    NOptional<NRunAs> runAs();

    /**
     * extra executor options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return extra executor options.
     */
    NOptional<List<String>> executorOptions();

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
    NOptional<Map<NHomeLocation, String>> homeLocations();

    /**
     * java command (or java home) used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java command (or java home) used to run workspace.
     */
    NOptional<String> javaCommand();

    /**
     * java options used to run workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return java options used to run workspace.
     */
    NOptional<String> javaOptions();

    /**
     * workspace log configuration.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return workspace log configuration.
     */
    NOptional<NLogConfig> logConfig();

    /**
     * user friendly workspace name.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return user friendly workspace name.
     */
    NOptional<String> name();

    /**
     * mode used to open workspace.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return mode used to open workspace.
     */
    NOptional<NOpenMode> openMode();

    /**
     * default output format type.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output format type.
     */
    NOptional<NContentType> outputFormat();

    /**
     * default output formation options.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return default output formation options.
     */
    NOptional<List<String>> outputFormatOptions();

    /**
     * credential needed to log into workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return credential needed to log into workspace.
     */
    NOptional<char[]> credential();

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
    NOptional<NStoreStrategy> repositoryStoreStrategy();

    /**
     * nuts runtime id (or version) to boot.
     *
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return nuts runtime id (or version) to boot.
     */
    NOptional<NId> runtimeId();

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
    NOptional<NOsFamily> storeLayout();

    /**
     * store location strategy for creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store location strategy for creating a new workspace.
     */
    NOptional<NStoreStrategy> storeStrategy();

    /**
     * store locations map to consider when creating a new workspace.
     * <br>
     * <strong>option-type :</strong> create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @return store locations map to consider when creating a new workspace.
     */
    NOptional<Map<NStoreType, String>> storeLocations();

    /**
     * terminal mode (inherited, formatted, filtered) to use.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return terminal mode (inherited, formatted, filtered) to use.
     */
    NOptional<NTerminalMode> terminalMode();

    /**
     * repositories to register temporarily when running the workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return repositories to register temporarily when running the workspace.
     */
    NOptional<List<String>> repositories();

    NOptional<List<String>> bootRepositories();

    /**
     * username to log into when running workspace.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return username to log into when running workspace.
     */
    NOptional<String> userName();

    /**
     * workspace folder location path.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child
     * workspaces)
     *
     * @return workspace folder location path.
     */
    NOptional<String> workspace();

    NOptional<String> debug();

    /**
     * if true consider system repository
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider system repository
     */

    NOptional<Boolean> system();

    /**
     * if true consider GUI/Swing mode
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true consider GUI/Swing mode
     */

    NOptional<Boolean> gui();

    /**
     * if true, workspace were invoked from parent process and hence inherits its options.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace were invoked from parent process and hence inherits its options.
     */

    NOptional<Boolean> inherited();

    /**
     * if true, workspace configuration are non modifiable.
     * However cache stills modifiable so that It's possible to load external libraries.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, workspace configuration are non modifiable.
     */

    NOptional<Boolean> readOnly();

    /**
     * if true, boot, cache and temp folder are deleted.
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, boot, cache and temp folder are deleted.
     */

    NOptional<Boolean> recover();

    /**
     * if true, workspace will be reset (all configuration and runtime files deleted).
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return if true, workspace will be reset (all configuration and runtime files deleted).
     */

    NOptional<Boolean> reset();

    NOptional<Boolean> resetHard();


    NOptional<Boolean> commandVersion();


    NOptional<Boolean> commandHelp();

    /**
     * if true, do not install nuts companion tools upon workspace creation.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return if true, do not install nuts companion tools upon workspace creation.
     */

    NOptional<Boolean> installCompanions();

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

    NOptional<Boolean> skipWelcome();


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
    NOptional<String> outLinePrefix();

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
    NOptional<String> errLinePrefix();

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

    NOptional<Boolean> skipBoot();

    /**
     * when true, extra trace user-friendly information is written to standard output.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */

    NOptional<Boolean> trace();

    /**
     * return progress options string.
     * progress options configures how progress monitors are processed.
     * 'no' value means that progress is disabled.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return when true, extra trace user-friendly information is written to standard output.
     */
    NOptional<String> progressOptions();

    /**
     * return dependency solver Name
     *
     * @return dependency solver Name
     * @since 0.8.3
     */
    NOptional<String> dependencySolver();

    /**
     * when true, use cache
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use cache when true
     */

    NOptional<Boolean> cached();

    /**
     * when true, use index
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use index when true
     */
    NOptional<Boolean> indexed();

    /**
     * when true, use transitive repositories
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */

    NOptional<Boolean> transitive();

    /**
     * when true, application is running in bot (robot) mode. No interaction or trace is allowed.
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return application is running in bot (robot) mode. No interaction or trace is allowed.
     */

    NOptional<Boolean> bot();

    /**
     * @return application is running in preview mode (using preview repositories)
     * @since 0.8.5
     */
    NOptional<Boolean> previewRepo();

    /**
     * @return workspace is running as shared Workspace instance (Singleton)
     * @since 0.8.5
     */
    NOptional<Boolean> sharedInstance();

    /**
     * default fetch strategy
     * <br>
     * <strong>option-type :</strong> exported (inherited in child workspaces)
     *
     * @return use transitive repositories when true
     */
    NOptional<NFetchStrategy> fetchStrategy();


    /**
     * default standard input. when null, use {@code System.in}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard input or null
     */
    NOptional<InputStream> stdin();

    /**
     * default standard output. when null, use {@code System.out}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard output or null
     */
    NOptional<PrintStream> stdout();

    /**
     * default standard error. when null, use {@code System.err}
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return default standard error or null
     */
    NOptional<PrintStream> stderr();

    /**
     * executor service used to create worker threads. when null, use default.
     * this option cannot be defined via arguments.
     *
     * <br>
     * <strong>option-type :</strong> runtime (available only for the current workspace instance)
     *
     * @return executor service used to create worker threads. when null, use default.
     */
    NOptional<ExecutorService> executorService();

    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    NOptional<Instant> expireTime();

    NOptional<Boolean> skipErrors();

    NOptional<Boolean> switchWorkspace();

    NOptional<List<NMsg>> errors();

    NOptional<List<String>> customOptions();

    NOptional<NArg> customOptionArg(String key);

    NOptional<String> customOption(String key);

    NOptional<List<NArg>> customOptionArgs();

    /**
     * locale
     *
     * @return session locale
     * @since 0.8.1
     */
    NOptional<String> locale();

    /**
     * theme
     *
     * @return session locale
     * @since 0.8.1
     */
    NOptional<String> theme();


}
