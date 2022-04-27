/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.boot.PrivateWorkspaceOptionsConfigHelper;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Workspace creation/opening options class.
 * <p>
 * %category Config
 *
 * @since 0.5.4
 */
public class DefaultNutsWorkspaceOptions implements Serializable, NutsWorkspaceOptions {
    public static NutsWorkspaceOptions BLANK = new DefaultNutsWorkspaceOptions(
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null
    );

    private static final long serialVersionUID = 1;
    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> outputFormatOptions;

    private final List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private final String apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * child workspaces)
     */
    private final String runtimeId;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String javaCommand;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String javaOptions;

    /**
     * workspace folder location path option-type : exported (inherited in child
     * workspaces)
     */
    private final String workspace;

    /**
     * out line prefix, option-type : exported (inherited in child workspaces)
     */
    private final String outLinePrefix;

    /**
     * err line prefix, option-type : exported (inherited in child workspaces)
     */
    private final String errLinePrefix;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private final String name;

    /**
     * if true, do not install nuts companion tools upon workspace creation
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean skipCompanions;

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * defaults to false option-type : exported (inherited in child workspaces)
     *
     * @since 0.5.5
     */
    private final Boolean skipWelcome;

    /**
     * if true, do not bootstrap workspace after reset/recover. When
     * reset/recover is not active this option is not accepted and an error will
     * be thrown
     *
     * @since 0.6.0
     */
    private final Boolean skipBoot;

    /**
     * if true consider global/system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean global;

    /**
     * if true consider GUI/Swing mode
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean gui;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> excludedExtensions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final List<String> repositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String userName;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final char[] credentials;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsTerminalMode terminalMode;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean readOnly;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean trace;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String progressOptions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String dependencySolver;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final NutsOpenMode openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Instant creationTime;

    /**
     * if true no real execution, wil dry exec option-type : runtime (available
     * only for the current workspace instance)
     */
    private final Boolean dry;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Supplier<ClassLoader> classLoaderSupplier;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final List<String> executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean recover;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean reset;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean commandVersion;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean commandHelp;

    /**
     * option-type : runtime / exported (depending on the value)
     */
    private final String debug;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final Boolean inherited;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private final NutsExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private final NutsRunAs runAs;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final String archetype;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @since 0.8.0
     */
    private final Boolean switchWorkspace;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final Map<NutsStoreLocation, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final Map<NutsHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsOsFamily storeLocationLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsStoreLocationStrategy storeLocationStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private final NutsStoreLocationStrategy repositoryStoreLocationStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final NutsFetchStrategy fetchStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean cached;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean indexed;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean transitive;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final Boolean bot;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final InputStream stdin;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final PrintStream stdout;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final PrintStream stderr;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private final ExecutorService executorService;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
//    private String bootRepositories = null;
    private final Instant expireTime;
    private final List<NutsMessage> errors;
    private final Boolean skipErrors;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String locale;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private final String theme;

    public DefaultNutsWorkspaceOptions(List<String> outputFormatOptions, List<String> customOptions, String apiVersion, String runtimeId, String javaCommand, String javaOptions, String workspace, String outLinePrefix, String errLinePrefix, String name, Boolean skipCompanions, Boolean skipWelcome, Boolean skipBoot, Boolean global, Boolean gui, List<String> excludedExtensions, List<String> repositories, String userName, char[] credentials, NutsTerminalMode terminalMode, Boolean readOnly, Boolean trace, String progressOptions, String dependencySolver, NutsLogConfig logConfig, NutsConfirmationMode confirm, NutsContentType outputFormat, List<String> applicationArguments, NutsOpenMode openMode, Instant creationTime, Boolean dry, Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions, Boolean recover, Boolean reset, Boolean commandVersion, Boolean commandHelp, String debug, Boolean inherited, NutsExecutionType executionType, NutsRunAs runAs, String archetype, Boolean switchWorkspace, Map<NutsStoreLocation, String> storeLocations, Map<NutsHomeLocation, String> homeLocations, NutsOsFamily storeLocationLayout, NutsStoreLocationStrategy storeLocationStrategy, NutsStoreLocationStrategy repositoryStoreLocationStrategy, NutsFetchStrategy fetchStrategy, Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, InputStream stdin, PrintStream stdout, PrintStream stderr, ExecutorService executorService, Instant expireTime, List<NutsMessage> errors, Boolean skipErrors, String locale, String theme) {
        this.outputFormatOptions = PrivateNutsUtilCollections.unmodifiableList(outputFormatOptions);
        this.customOptions = PrivateNutsUtilCollections.unmodifiableList(customOptions);
        this.apiVersion = NutsUtilStrings.trimToNull(apiVersion);
        this.runtimeId = NutsUtilStrings.trimToNull(runtimeId);
        this.javaCommand = NutsUtilStrings.trimToNull(javaCommand);
        this.javaOptions = NutsUtilStrings.trimToNull(javaOptions);
        this.workspace = NutsUtilStrings.trimToNull(workspace);
        this.outLinePrefix = NutsUtilStrings.trimToNull(outLinePrefix);
        this.errLinePrefix = NutsUtilStrings.trimToNull(errLinePrefix);
        this.name = NutsUtilStrings.trimToNull(name);
        this.skipCompanions = skipCompanions;
        this.skipWelcome = skipWelcome;
        this.skipBoot = skipBoot;
        this.global = global;
        this.gui = gui;
        this.excludedExtensions = PrivateNutsUtilCollections.unmodifiableList(excludedExtensions);
        this.repositories = PrivateNutsUtilCollections.unmodifiableList(repositories);
        this.userName = NutsUtilStrings.trimToNull(userName);
        this.credentials = credentials == null ? null : Arrays.copyOf(credentials, credentials.length);
        this.terminalMode = terminalMode;
        this.readOnly = readOnly;
        this.trace = trace;
        this.progressOptions = NutsUtilStrings.trimToNull(progressOptions);
        this.dependencySolver = NutsUtilStrings.trimToNull(dependencySolver);
        this.logConfig = logConfig == null ? null : logConfig.copy();
        this.confirm = confirm;
        this.outputFormat = outputFormat;
        this.applicationArguments = PrivateNutsUtilCollections.unmodifiableList(applicationArguments);
        this.openMode = openMode == null ? NutsOpenMode.OPEN_OR_CREATE : openMode;
        this.creationTime = creationTime;
        this.dry = dry;
        this.classLoaderSupplier = classLoaderSupplier;
        this.executorOptions = PrivateNutsUtilCollections.unmodifiableList(executorOptions);
        this.recover = recover;
        this.reset = reset;
        this.commandVersion = commandVersion;
        this.commandHelp = commandHelp;
        this.debug = NutsUtilStrings.trimToNull(debug);
        this.inherited = inherited;
        this.executionType = executionType;
        this.runAs = runAs == null ? NutsRunAs.CURRENT_USER : runAs;
        this.archetype = NutsUtilStrings.trimToNull(archetype);
        this.switchWorkspace = switchWorkspace;
        this.storeLocations = PrivateNutsUtilCollections.unmodifiableMap(storeLocations);
        this.homeLocations = PrivateNutsUtilCollections.unmodifiableMap(homeLocations);
        this.storeLocationLayout = storeLocationLayout;
        this.storeLocationStrategy = storeLocationStrategy;
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        this.fetchStrategy = fetchStrategy == null ? NutsFetchStrategy.ONLINE : fetchStrategy;
        this.cached = cached;
        this.indexed = indexed;
        this.transitive = transitive;
        this.bot = bot;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executorService = executorService;
        this.expireTime = expireTime;
        this.errors = PrivateNutsUtilCollections.unmodifiableList(errors);
        this.skipErrors = skipErrors;
        this.locale = NutsUtilStrings.trimToNull(locale);
        this.theme = NutsUtilStrings.trimToNull(theme);
    }


    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public List<String> getApplicationArguments() {
        return PrivateNutsUtilCollections.unmodifiableList(applicationArguments);
    }


    @Override
    public String getArchetype() {
        return archetype;
    }


    @Override
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return classLoaderSupplier;
    }


    @Override
    public NutsConfirmationMode getConfirm() {
        return confirm;
    }


    @Override
    public boolean isDry() {
        return dry != null && dry;
    }

    @Override
    public Boolean getDry() {
        return dry;
    }


    @Override
    public Instant getCreationTime() {
        return creationTime;
    }


    @Override
    public List<String> getExcludedExtensions() {
        return PrivateNutsUtilCollections.unmodifiableList(excludedExtensions);
    }


    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }


    @Override
    public NutsRunAs getRunAs() {
        return runAs;
    }


    @Override
    public List<String> getExecutorOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(executorOptions);
    }


    @Override
    public String getHomeLocation(NutsHomeLocation location) {
        return homeLocations.get(location);
    }

    @Override
    public Map<NutsHomeLocation, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }


    @Override
    public String getJavaCommand() {
        return javaCommand;
    }


    @Override
    public String getJavaOptions() {
        return javaOptions;
    }


    @Override
    public NutsLogConfig getLogConfig() {
        return logConfig;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public NutsOpenMode getOpenMode() {
        return openMode;
    }


    @Override
    public NutsContentType getOutputFormat() {
        return outputFormat;
    }


    @Override
    public List<String> getOutputFormatOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(outputFormatOptions);
    }


    @Override
    public char[] getCredentials() {
        return credentials == null ? null : Arrays.copyOf(credentials, credentials.length);
    }


    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }


    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return storeLocations.get(folder);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }


    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }


    @Override
    public Map<NutsStoreLocation, String> getStoreLocations() {
        return new LinkedHashMap<>(storeLocations);
    }


    @Override
    public NutsTerminalMode getTerminalMode() {
        return terminalMode;
    }


    @Override
    public List<String> getRepositories() {
        return PrivateNutsUtilCollections.unmodifiableList(repositories);
    }


    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public String getDebug() {
        return debug;
    }


    @Override
    public boolean isGlobal() {
        return global != null && global;
    }

    @Override
    public Boolean getGlobal() {
        return global;
    }


    @Override
    public boolean isGui() {
        return gui != null && gui;
    }

    @Override
    public Boolean getGui() {
        return gui;
    }


    @Override
    public boolean isInherited() {
        return inherited != null && inherited;
    }

    @Override
    public Boolean getInherited() {
        return inherited;
    }


    @Override
    public boolean isReadOnly() {
        return readOnly != null && readOnly;
    }

    @Override
    public Boolean getReadOnly() {
        return readOnly;
    }


    @Override
    public boolean isRecover() {
        return recover != null && recover;
    }

    @Override
    public Boolean getRecover() {
        return recover;
    }


    @Override
    public boolean isReset() {
        return reset != null && reset;
    }

    @Override
    public Boolean getReset() {
        return reset;
    }


    @Override
    public boolean isCommandVersion() {
        return commandVersion != null && commandVersion;
    }

    @Override
    public Boolean getCommandVersion() {
        return commandVersion;
    }

    @Override
    public boolean isCommandHelp() {
        return commandHelp != null && commandHelp;
    }

    @Override
    public Boolean getCommandHelp() {
        return commandHelp;
    }

    @Override
    public boolean isSkipCompanions() {
        return skipCompanions != null && skipCompanions;
    }

    @Override
    public Boolean getSkipCompanions() {
        return skipCompanions;
    }


    @Override
    public boolean isSkipWelcome() {
        return skipWelcome != null && skipWelcome;
    }

    @Override
    public Boolean getSkipWelcome() {
        return skipWelcome;
    }

    @Override
    public String getOutLinePrefix() {
        return outLinePrefix;
    }


    @Override
    public String getErrLinePrefix() {
        return errLinePrefix;
    }

    @Override
    public boolean isSkipBoot() {
        return skipBoot != null && skipBoot;
    }

    @Override
    public Boolean getSkipBoot() {
        return skipBoot;
    }


    @Override
    public boolean isTrace() {
        return trace == null || trace;
    }

    @Override
    public Boolean getTrace() {
        return trace;
    }

    public String getProgressOptions() {
        return progressOptions;
    }

    public boolean isCached() {
        return cached == null || cached;
    }

    @Override
    public Boolean getCached() {
        return cached;
    }

    public boolean isIndexed() {
        return indexed == null || indexed;
    }

    @Override
    public Boolean getIndexed() {
        return indexed;
    }

    @Override
    public boolean isTransitive() {
        return transitive == null || transitive;
    }

    @Override
    public Boolean getTransitive() {
        return transitive;
    }

    @Override
    public boolean isBot() {
        return bot != null && bot;
    }

    @Override
    public Boolean getBot() {
        return bot;
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    @Override
    public InputStream getStdin() {
        return stdin;
    }

    @Override
    public PrintStream getStdout() {
        return stdout;
    }

    @Override
    public PrintStream getStderr() {
        return stderr;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public Instant getExpireTime() {
        return expireTime;
    }

    @Override
    public boolean isSkipErrors() {
        return skipErrors != null && skipErrors;
    }

    @Override
    public Boolean getSkipErrors() {
        return skipErrors;
    }

    @Override
    public boolean isSwitchWorkspace() {
        return switchWorkspace != null && switchWorkspace;
    }

    @Override
    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    @Override
    public List<NutsMessage> getErrors() {
        return PrivateNutsUtilCollections.unmodifiableList(errors);
    }

    @Override
    public List<String> getCustomOptions() {
        return PrivateNutsUtilCollections.unmodifiableList(customOptions);
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public String getTheme() {
        return theme;
    }


    @Override
    public String getDependencySolver() {
        return dependencySolver;
    }

    @Override
    public String toString() {
        return toCommandLine().toString();
    }

    @Override
    public NutsWorkspaceOptions readOnly() {
        return this;
    }

    @Override
    public NutsCommandLine toCommandLine() {
        return toCommandLine(new NutsWorkspaceOptionsConfig());
    }

    @Override
    public NutsCommandLine toCommandLine(NutsWorkspaceOptionsConfig config) {
        return new PrivateWorkspaceOptionsConfigHelper(config, this).toCommandLine();
    }

    @Override
    public NutsWorkspaceOptionsBuilder builder() {
        return new DefaultNutsWorkspaceOptionsBuilder().setAll(this);
    }

}
