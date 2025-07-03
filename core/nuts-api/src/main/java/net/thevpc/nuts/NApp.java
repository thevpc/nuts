package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NOptional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents application configuration bound to the current {@code NSession}
 * This interface provides methods for application lifecycle management, configuration,
 * properties, and utility functions to support application execution and interaction.
 */
public interface NApp extends NComponent {

    static NAppBuilder builder() {
        return new NAppBuilder();
    }

    static NAppBuilder builder(String[] args) {
        return new NAppBuilder().args(args);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Info{
        String id() default "";
    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Main{

    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Installer {

    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Uninstaller {

    }
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Updater {

    }

    /**
     * Returns the instance of {@code NApp} that is bound to the current {@code NSession}.
     *
     * @return the instance of {@code NApp} that is bound to the current {@code NSession}.
     */
    static NApp of() {
        return NExtensions.of(NApp.class);
    }

    static NOptional<NApp> get() {
        return NExtensions.get().flatMap(x->x.createComponent(NApp.class));
    }

    /**
     * Prepares the application with the provided initialization information for the current {@code session}
     *
     * @param appInitInfo the initialization information required to prepare the application for the current {@code session}
     */
    void prepare(NAppInitInfo appInitInfo);

    /**
     * Creates and returns a copy of this {@code NApp} instance.
     *
     * @return a new {@code NApp} instance that is a copy of this instance
     */
    NApp copy();

    /**
     * Copies the properties and state from the specified {@code other} instance of {@code NApp}
     * into this instance.
     *
     * @param other the {@code NApp} instance from which properties and state should be copied
     * @return the current {@code NApp} instance with updated properties and state
     */
    NApp copyFrom(NApp other);

    /**
     * Retrieves the identifier associated with this instance of {@code NApp}.
     *
     * @return an {@code NOptional} containing the {@code NId} associated with this application,
     *         or an empty {@code NOptional} if no identifier is set.
     */
    NOptional<NId> getId();

    /**
     * Retrieves the current execution mode of the application.
     *
     * @return the {@code NApplicationMode} representing the current mode
     *         in which the application is running
     */
    NApplicationMode getMode();


    /**
     * detected bundle name
     * @return detected bundle name
     */
    String getBundleName();

    /**
     * Retrieves the list of arguments associated with the current execution mode
     * of the application.
     *
     * @return a {@code List<String>} containing the arguments relevant to the
     *         current application mode, or an empty list if no mode arguments
     *         are set.
     */
    List<String> getModeArguments();

    /**
     * Retrieves the {@code NCmdLineAutoComplete} instance associated with the application.
     * This utility can be used to collect command line argument candidates, manage
     * autocomplete suggestions, and retrieve information about the current command line context.
     *
     * @return an instance of {@code NCmdLineAutoComplete} providing command line auto-completion features
     */
    NCmdLineAutoComplete getAutoComplete();

    /**
     * Retrieves the detailed help text associated with this application, providing
     * guidance or instructions on its usage.
     *
     * @return an {@code NOptional<NText>} containing the help text if available,
     *         or an empty {@code NOptional} if no help text is defined.
     */
    NOptional<NText> getHelpText();

    /**
     * Displays detailed help information about the application.
     * This method is used to output guidance, instructions, or
     * any relevant assistance to the user regarding the application's usage.
     */
    void printHelp();

    /**
     * Retrieves the main application class associated with this {@code NApp} instance.
     *
     * @return a {@code Class<?>} representing the main application class, or {@code null} if It's not defined.
     */
    Class<?> getAppClass();

    /**
     * Retrieves the path to the binary folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the binary folder.
     */
    NPath getBinFolder();

    /**
     * Retrieves the path to the configuration folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the configuration folder.
     */
    NPath getConfFolder();

    /**
     * Retrieves the path to the log folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the log folder.
     */
    NPath getLogFolder();

    /**
     * Retrieves the path to the temporary folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the temporary folder.
     */
    NPath getTempFolder();

    /**
     * Retrieves the path to the variable folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the variable folder.
     */
    NPath getVarFolder();

    /**
     * Retrieves the path to the library folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the library folder.
     */
    NPath getLibFolder();

    /**
     * Retrieves the path to the run folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the run folder.
     */
    NPath getRunFolder();

    /**
     * Retrieves the path to the cache folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the cache folder.
     */
    NPath getCacheFolder();

    /**
     * Retrieves the path to a specific version folder associated with the application.
     *
     * @param storeType the type of store (e.g., configuration, cache, runtime, etc.)
     *                  for which the versioned folder path is to be retrieved
     * @param version   the specific version identifier for which the folder path is required
     * @return an {@code NPath} representing the location of the version-specific folder,
     *         or {@code null} if the path cannot be determined
     */
    NPath getVersionFolder(NStoreType storeType, String version);

    /**
     * Retrieves the path to the shared applications folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared applications folder.
     */
    NPath getSharedAppsFolder();

    /**
     * Retrieves the path to the shared configuration folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared configuration folder.
     */
    NPath getSharedConfFolder();

    /**
     * Retrieves the path to the shared log folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared log folder.
     */
    NPath getSharedLogFolder();

    /**
     * Retrieves the path to the shared temporary folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared temporary folder.
     */
    NPath getSharedTempFolder();

    /**
     * Retrieves the path to the shared variable folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared variable folder.
     */
    NPath getSharedVarFolder();

    /**
     * Retrieves the path to the shared library folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared library folder.
     */
    NPath getSharedLibFolder();

    /**
     * Retrieves the path to the shared run folder associated with this application.
     *
     * @return an {@code NPath} representing the location of the shared run folder.
     */
    NPath getSharedRunFolder();

    /**
     * Retrieves the path to the shared folder associated with the specified {@code location}.
     * The shared folder depends on the provided {@code location} parameter, which represents
     * the type of store (e.g., configuration, cache, runtime, etc.).
     *
     * @param location the {@code NStoreType} specifying the type of store whose shared folder
     *                 path is to be retrieved
     * @return an {@code NPath} representing the location of the shared folder for the given
     *         {@code location}
     */
    NPath getSharedFolder(NStoreType location);

    /**
     * Retrieves the version information wrapped in an NOptional.
     *
     * @return an NOptional containing the NVersion instance if available, or an empty NOptional if no version is found.
     */
    NOptional<NVersion> getVersion();

    /**
     * Retrieves a list of arguments as strings.
     *
     * @return a List of String objects representing the arguments
     */
    List<String> getArguments();

    /**
     * Retrieves the start time of the clock.
     *
     * @return an NClock object representing the start time.
     */
    NClock getStartTime();

    /**
     * Retrieves the previous version of the current entity or state, if available.
     *
     * @return an {@code NOptional} containing the previous {@code NVersion} if it exists,
     *         or an empty {@code NOptional} if no previous version is available.
     */
    NOptional<NVersion> getPreviousVersion();

    /**
     * Retrieves the command line object associated with the current instance.
     *
     * @return an NCmdLine object representing the command line arguments and options.
     */
    NCmdLine getCmdLine();

    /**
     * Processes the command line arguments using the provided command line runner.
     *
     * @param commandLineRunner an instance of NCmdLineRunner responsible for handling the command line input
     */
    void runCmdLine(NCmdLineRunner commandLineRunner);

    /**
     * Retrieves the folder path associated with the specified storage type location.
     *
     * @param location the specific type of storage location for which the folder path is needed
     * @return the folder path corresponding to the given storage type location
     */
    NPath getFolder(NStoreType location);

    /**
     * Determines if the current mode of operation is execution mode.
     *
     * @return true if the system is in execution mode, otherwise false
     */
    boolean isExecMode();

    /**
     * Retrieves an instance of NAppStoreLocationResolver, which is responsible
     * for resolving the location of the application store.
     *
     * @return an instance of NAppStoreLocationResolver that handles store location resolution.
     */
    NAppStoreLocationResolver getStoreLocationResolver();

    /**
     * Sets the folder for the given location type.
     *
     * @param location the type of the store (e.g., file system, database, etc.)
     * @param folder the path of the folder to be set
     * @return the updated NApp instance
     */
    NApp setFolder(NStoreType location, NPath folder);

    /**
     * Sets the shared folder for the application to the specified location and path.
     *
     * @param location the type of storage location where the shared folder is to be set
     * @param folder the path to the folder to be set as the shared folder
     * @return the current instance of NApp with the updated shared folder configuration
     */
    NApp setSharedFolder(NStoreType location, NPath folder);

    /**
     * Sets the ID of the application.
     *
     * @param appId the ID to be set for the application
     * @return the updated instance of NApp
     */
    NApp setId(NId appId);

    /**
     * Sets the list of arguments for the application.
     *
     * @param args the list of arguments to be set
     * @return the instance of the NApp with the updated arguments
     */
    NApp setArguments(List<String> args);

    /**
     * Sets the arguments for the application.
     *
     * @param args the array of arguments to be passed to the application
     * @return the current instance of NApp with the updated arguments
     */
    NApp setArguments(String[] args);

    /**
     * Sets the start time for the application.
     *
     * @param startTime an instance of NClock representing the desired start time
     * @return the current NApp instance with the updated start time
     */
    NApp setStartTime(NClock startTime);

    /**
     * Retrieves the property associated with the specified name and scope,
     * or computes and stores it using the provided supplier if the property
     * does not already exist.
     *
     * @param <T> the type of the property being retrieved or computed
     * @param name the name of the property to retrieve or compute
     * @param scope the scope within which the property is stored
     * @param supplier the supplier function used to compute the property if it
     *                 does not already exist
     * @return the existing or newly computed property of type T
     */
    <T> T getOrComputeProperty(String name, NScopeType scope, Supplier<T> supplier);

    /**
     * Sets a property with the specified name, scope, and value.
     *
     * @param <T>   The type of the value to be set.
     * @param name  The name of the property to set. Must not be null.
     * @param scope The scope in which the property is being set. Must not be null.
     * @param value The value to assign to the property.
     * @return The value that was set for the property.
     */
    <T> T setProperty(String name, NScopeType scope, T value);

    /**
     * Retrieves a property with the specified name and scope type.
     *
     * @param name the name of the property to retrieve
     * @param scope the scope type which defines the context of the property
     * @param <T> the type of the property value
     * @return an NOptional containing the property value if found, or an empty NOptional if the property does not exist
     */
    <T> NOptional<T> getProperty(String name, NScopeType scope);



}
