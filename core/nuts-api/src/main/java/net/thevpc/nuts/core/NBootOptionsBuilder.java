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

/**
 * NBootOptionsBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBootOptionsBuilder extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NBootOptionsBuilder of() {
        return NExtensions.of(NBootOptionsBuilder.class);
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    NBootOptionsBuilder copy();

    /**
     * Build.
     *
     * @return build result
     */
    NBootOptions build();

//    NBootOptionsBuilder setBootRepositories(String bootRepositories);

    /**
     * Runtime boot dependency node.
     *
     * @param runtimeBootDependencyNode runtime boot dependency node
     * @return runtime boot dependency node result
     */
    NBootOptionsBuilder runtimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode);

    /**
     * Boot workspace factory.
     *
     * @param bootWorkspaceFactory boot workspace factory
     * @return boot workspace factory result
     */
    NBootOptionsBuilder bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory);

    /**
     * Class world ur ls.
     *
     * @param classWorldURLs class world ur ls
     * @return class world ur ls result
     */
    NBootOptionsBuilder classWorldURLs(List<URL> classWorldURLs);

    /**
     * Class world loader.
     *
     * @param classWorldLoader class world loader
     * @return class world loader result
     */
    NBootOptionsBuilder classWorldLoader(ClassLoader classWorldLoader);

    /**
     * Uuid.
     *
     * @param uuid uuid
     * @return uuid result
     */
    NBootOptionsBuilder uuid(String uuid);

    /**
     * Runtime boot descriptor.
     *
     * @param runtimeBootDescriptor runtime boot descriptor
     * @return runtime boot descriptor result
     */
    NBootOptionsBuilder runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor);


    /**
     * Init launchers.
     *
     * @param initLaunchers init launchers
     * @return init launchers result
     */
    NBootOptionsBuilder initLaunchers(Boolean initLaunchers);

    /**
     * Init scripts.
     *
     * @param initScripts init scripts
     * @return init scripts result
     */
    NBootOptionsBuilder initScripts(Boolean initScripts);

    /**
     * Init platforms.
     *
     * @param initPlatforms init platforms
     * @return init platforms result
     */
    NBootOptionsBuilder initPlatforms(Boolean initPlatforms);

    /**
     * Init java.
     *
     * @param initJava init java
     * @return init java result
     */
    NBootOptionsBuilder initJava(Boolean initJava);

    /**
     * Checks if isolation level.
     *
     * @param isolationLevel isolation level
     * @return isolation level result
     */
    NBootOptionsBuilder isolationLevel(NIsolationLevel isolationLevel);

    /**
     * Desktop launcher.
     *
     * @param desktopLauncher desktop launcher
     * @return desktop launcher result
     */
    NBootOptionsBuilder desktopLauncher(NSupportMode desktopLauncher);

    /**
     * Menu launcher.
     *
     * @param menuLauncher menu launcher
     * @return menu launcher result
     */
    NBootOptionsBuilder menuLauncher(NSupportMode menuLauncher);

    /**
     * User launcher.
     *
     * @param userLauncher user launcher
     * @return user launcher result
     */
    NBootOptionsBuilder userLauncher(NSupportMode userLauncher);

    /**
     * Api version.
     *
     * @param apiVersion api version
     * @return api version result
     */
    NBootOptionsBuilder apiVersion(NVersion apiVersion);

    /**
     * Application arguments.
     *
     * @param applicationArguments application arguments
     * @return application arguments result
     */
    NBootOptionsBuilder applicationArguments(List<String> applicationArguments);


    /**
     * Archetype.
     *
     * @param archetype archetype
     * @return archetype result
     */
    NBootOptionsBuilder archetype(String archetype);

    /**
     * Class loader supplier.
     *
     * @param provider provider
     * @return class loader supplier result
     */
    NBootOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider);


    /**
     * Confirm.
     *
     * @param confirm confirm
     * @return confirm result
     */
    NBootOptionsBuilder confirm(NConfirmationMode confirm);

    /**
     * Cry.
     *
     * @param dry dry
     * @return cry result
     */
    NBootOptionsBuilder cry(Boolean dry);

    /**
     * Show stacktrace.
     *
     * @param showStacktrace show stacktrace
     * @return show stacktrace result
     */
    NBootOptionsBuilder showStacktrace(Boolean showStacktrace);

    /**
     * Creation time.
     *
     * @param creationTime creation time
     * @return creation time result
     */
    NBootOptionsBuilder creationTime(Instant creationTime);


    /**
     * Excluded extensions.
     *
     * @param excludedExtensions excluded extensions
     * @return excluded extensions result
     */
    NBootOptionsBuilder excludedExtensions(List<String> excludedExtensions);

    /**
     * Execution type.
     *
     * @param executionType execution type
     * @return execution type result
     */
    NBootOptionsBuilder executionType(NExecutionType executionType);

    /**
     * Shared instance.
     *
     * @param sharedInstance shared instance
     * @return shared instance result
     */
    NBootOptionsBuilder sharedInstance(Boolean sharedInstance);

    /**
     * set runAs mode
     *
     * @param runAs runAs
     * @return {@code this} instance
     * @since 0.8.1
     */
    NBootOptionsBuilder runAs(NRunAs runAs);

    /**
     * Executor options.
     *
     * @param executorOptions executor options
     * @return executor options result
     */
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

    /**
     * Java command.
     *
     * @param javaCommand java command
     * @return java command result
     */
    NBootOptionsBuilder javaCommand(String javaCommand);


    /**
     * Java options.
     *
     * @param javaOptions java options
     * @return java options result
     */
    NBootOptionsBuilder javaOptions(String javaOptions);


    /**
     * Log config.
     *
     * @param logConfig log config
     * @return log config result
     */
    NBootOptionsBuilder logConfig(NLogConfig logConfig);


    /**
     * Name.
     *
     * @param workspaceName workspace name
     * @return name result
     */
    NBootOptionsBuilder name(String workspaceName);

    /**
     * Open mode.
     *
     * @param openMode open mode
     * @return open mode result
     */
    NBootOptionsBuilder openMode(NOpenMode openMode);


    /**
     * Output format.
     *
     * @param outputFormat output format
     * @return output format result
     */
    NBootOptionsBuilder outputFormat(NContentType outputFormat);


    /**
     * Output format options.
     *
     * @param options options
     * @return output format options result
     */
    NBootOptionsBuilder outputFormatOptions(List<String> options);


    /**
     * Credential.
     *
     * @param credentials credentials
     * @return credential result
     */
    NBootOptionsBuilder credential(char[] credentials);


    /**
     * Repository store strategy.
     *
     * @param repositoryStoreStrategy repository store strategy
     * @return repository store strategy result
     */
    NBootOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy);


    /**
     * Runtime id.
     *
     * @param runtimeId runtime id
     * @return runtime id result
     */
    NBootOptionsBuilder runtimeId(NId runtimeId);

    /**
     * Store layout.
     *
     * @param storeLayout store layout
     * @return store layout result
     */
    NBootOptionsBuilder storeLayout(NOsFamily storeLayout);

    /**
     * Store strategy.
     *
     * @param storeStrategy store strategy
     * @return store strategy result
     */
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

    /**
     * Terminal mode.
     *
     * @param terminalMode terminal mode
     * @return terminal mode result
     */
    NBootOptionsBuilder terminalMode(NTerminalMode terminalMode);

    /**
     * Repositories.
     *
     * @param transientRepositories transient repositories
     * @return repositories result
     */
    NBootOptionsBuilder repositories(List<String> transientRepositories);

    /**
     * Boot repositories.
     *
     * @param transientRepositories transient repositories
     * @return boot repositories result
     */
    NBootOptionsBuilder bootRepositories(List<String> transientRepositories);

    /**
     * Workspace.
     *
     * @param workspace workspace
     * @return workspace result
     */
    NBootOptionsBuilder workspace(String workspace);

    /**
     * Debug.
     *
     * @param debug debug
     * @return debug result
     */
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

    /**
     * Gui.
     *
     * @param gui gui
     * @return gui result
     */
    NBootOptionsBuilder gui(Boolean gui);

    /**
     * Inherited.
     *
     * @param inherited inherited
     * @return inherited result
     */
    NBootOptionsBuilder inherited(Boolean inherited);


    /**
     * Read only.
     *
     * @param readOnly read only
     * @return read only result
     */
    NBootOptionsBuilder readOnly(Boolean readOnly);

    /**
     * Recover.
     *
     * @param recover recover
     * @return recover result
     */
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

    /**
     * Command version.
     *
     * @param version version
     * @return command version result
     */
    NBootOptionsBuilder commandVersion(Boolean version);

    /**
     * Command help.
     *
     * @param help help
     * @return command help result
     */
    NBootOptionsBuilder commandHelp(Boolean help);

    /**
     * Install companions.
     *
     * @param skipInstallCompanions skip install companions
     * @return install companions result
     */
    NBootOptionsBuilder installCompanions(Boolean skipInstallCompanions);

    /**
     * Skip welcome.
     *
     * @param skipWelcome skip welcome
     * @return skip welcome result
     */
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

    /**
     * Trace.
     *
     * @param trace trace
     * @return trace result
     */
    NBootOptionsBuilder trace(Boolean trace);

    /**
     * Progress options.
     *
     * @param progressOptions progress options
     * @return progress options result
     */
    NBootOptionsBuilder progressOptions(String progressOptions);

    /**
     * Cached.
     *
     * @param cached cached
     * @return cached result
     */
    NBootOptionsBuilder cached(Boolean cached);

    /**
     * Indexed.
     *
     * @param indexed indexed
     * @return indexed result
     */
    NBootOptionsBuilder indexed(Boolean indexed);

    /**
     * Transitive.
     *
     * @param transitive transitive
     * @return transitive result
     */
    NBootOptionsBuilder transitive(Boolean transitive);

    /**
     * Bot.
     *
     * @param bot bot
     * @return bot result
     */
    NBootOptionsBuilder bot(Boolean bot);

    /**
     * Fetch strategy.
     *
     * @param fetchStrategy fetch strategy
     * @return fetch strategy result
     */
    NBootOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy);

    /**
     * Stdin.
     *
     * @param stdin stdin
     * @return stdin result
     */
    NBootOptionsBuilder stdin(InputStream stdin);

    /**
     * Stdout.
     *
     * @param stdout stdout
     * @return stdout result
     */
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

    /**
     * Skip errors.
     *
     * @param value value
     * @return skip errors result
     */
    NBootOptionsBuilder skipErrors(Boolean value);

    /**
     * Switch workspace.
     *
     * @param value value
     * @return switch workspace result
     */
    NBootOptionsBuilder switchWorkspace(Boolean value);

    /**
     * Errors.
     *
     * @param errors errors
     * @return errors result
     */
    NBootOptionsBuilder errors(List<NMsg> errors);

    /**
     * Custom options.
     *
     * @param properties properties
     * @return custom options result
     */
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

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NBootOptionsBuilder copyFrom(NWorkspaceOptions other);

    /**
     * Copy from if present.
     *
     * @param other other
     * @return copy from if present result
     */
    NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NBootOptionsBuilder copyFrom(NBootOptions other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NBootOptionsBuilder copyFrom(NBootOptionsBuilder other);

    /**
     * Copy from if present.
     *
     * @param other other
     * @return copy from if present result
     */
    NBootOptionsBuilder copyFromIfPresent(NBootOptions other);

    /**
     * Cmd line.
     *
     * @param cmdLine cmd line
     * @return cmd line result
     */
    NBootOptionsBuilder cmdLine(String cmdLine);

    /**
     * Cmd line.
     *
     * @param args args
     * @return cmd line result
     */
    NBootOptionsBuilder cmdLine(String[] args);

    /**
     * User name.
     *
     * @param username username
     * @return user name result
     */
    NBootOptionsBuilder userName(String username);

    /**
     * Store location.
     *
     * @param location location
     * @param value value
     * @return store location result
     */
    NBootOptionsBuilder storeLocation(NStoreType location, String value);

    /**
     * Home location.
     *
     * @param location location
     * @param value value
     * @return home location result
     */
    NBootOptionsBuilder homeLocation(NHomeLocation location, String value);

    /**
     * Adds the specified output format options.
     *
     * @param options options
     * @return add output format options result
     */
    NBootOptionsBuilder addOutputFormatOptions(String... options);

    /**
     * update dependency solver Name
     *
     * @param dependencySolver dependency solver name
     * @return {@code this} instance
     * @since 0.8.3
     */
    NBootOptionsBuilder dependencySolver(String dependencySolver);

    /**
     * Unset runtime options.
     *
     * @return unset runtime options result
     */
    NBootOptionsBuilder unsetRuntimeOptions();

    /**
     * Unset creation options.
     *
     * @return unset creation options result
     */
    NBootOptionsBuilder unsetCreationOptions();

    /**
     * Unset exported options.
     *
     * @return unset exported options result
     */
    NBootOptionsBuilder unsetExportedOptions();


    /**
     * @return this
     * @since 0.8.5
     */
    NBootOptionsBuilder previewRepo(Boolean bot);


    /**
     * Runtime boot dependency node.
     *
     * @return runtime boot dependency node result
     */
    NOptional<NClassLoaderNode> runtimeBootDependencyNode();

    /**
     * Boot workspace factory.
     *
     * @return boot workspace factory result
     */
    NOptional<NBootWorkspaceFactory> bootWorkspaceFactory();

    /**
     * Class world ur ls.
     *
     * @return class world ur ls result
     */
    NOptional<List<URL>> classWorldURLs();

    /**
     * Class world loader.
     *
     * @return class world loader result
     */
    NOptional<ClassLoader> classWorldLoader();

    /**
     * Uuid.
     *
     * @return uuid result
     */
    NOptional<String> uuid();

    /**
     * Runtime boot descriptor.
     *
     * @return runtime boot descriptor result
     */
    NOptional<NBootDescriptor> runtimeBootDescriptor();

    /**
     * Desktop launcher.
     *
     * @return desktop launcher result
     */
    NOptional<NSupportMode> desktopLauncher();

    /**
     * Menu launcher.
     *
     * @return menu launcher result
     */
    NOptional<NSupportMode> menuLauncher();

    /**
     * User launcher.
     *
     * @return user launcher result
     */
    NOptional<NSupportMode> userLauncher();

    /**
     * Checks if isolation level.
     *
     * @return isolation level result
     */
    NOptional<NIsolationLevel> isolationLevel();

    /**
     * Converts to boot options.
     *
     * @return to boot options result
     */
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

    /**
     * Run as.
     *
     * @return run as result
     */
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

    /**
     * Boot repositories.
     *
     * @return boot repositories result
     */
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

    /**
     * Debug.
     *
     * @return debug result
     */
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

    /**
     * Reset hard.
     *
     * @return reset hard result
     */
    NOptional<Boolean> resetHard();


    /**
     * Command version.
     *
     * @return command version result
     */
    NOptional<Boolean> commandVersion();


    /**
     * Command help.
     *
     * @return command help result
     */
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

    /**
     * Skip errors.
     *
     * @return skip errors result
     */
    NOptional<Boolean> skipErrors();

    /**
     * Switch workspace.
     *
     * @return switch workspace result
     */
    NOptional<Boolean> switchWorkspace();

    /**
     * Errors.
     *
     * @return errors result
     */
    NOptional<List<NMsg>> errors();

    /**
     * Custom options.
     *
     * @return custom options result
     */
    NOptional<List<String>> customOptions();

    /**
     * Custom option arg.
     *
     * @param key key
     * @return custom option arg result
     */
    NOptional<NArg> customOptionArg(String key);

    /**
     * Custom option.
     *
     * @param key key
     * @return custom option result
     */
    NOptional<String> customOption(String key);

    /**
     * Custom option args.
     *
     * @return custom option args result
     */
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
