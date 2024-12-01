package net.thevpc.nuts;

import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NMsg;
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

public interface NBootOptionsBuilder extends NBootOptions, NComponent {
    static NBootOptionsBuilder of(){
        return NExtensions.of(NBootOptionsBuilder.class);
    }

    NBootOptionsBuilder copy();

    NBootOptions build();

    NBootOptionsBuilder setBootRepositories(String bootRepositories);

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

    NBootOptionsBuilder setAll(NWorkspaceOptions other);

    NBootOptionsBuilder setAllPresent(NWorkspaceOptions other);

    NBootOptionsBuilder setAll(NBootOptions other);

    NBootOptionsBuilder setAllPresent(NBootOptions other);

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


}
