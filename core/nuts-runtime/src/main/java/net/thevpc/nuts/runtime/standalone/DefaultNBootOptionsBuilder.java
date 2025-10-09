/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NWorkspaceCmdLineParser;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.runtime.standalone.util.NDefaultClassLoaderNode;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class DefaultNBootOptionsBuilder implements NBootOptionsBuilder, Serializable {

    private static final long serialVersionUID = 1;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> outputFormatOptions;

    private List<String> customOptions;
    /**
     * nuts api version to boot option-type : exported (inherited in child
     * workspaces)
     */
    private NVersion apiVersion;

    /**
     * nuts runtime id (or version) to boot option-type : exported (inherited in
     * nuts runtime id (or version) to boot option-type : exported (inherited iton
     * child workspaces)
     */
    private NId runtimeId;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String javaCommand;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String javaOptions;

    /**
     * workspace folder location path option-type : exported (inherited in child
     * workspaces)
     */
    private String workspace;

    /**
     * out line prefix, option-type : exported (inherited in child workspaces)
     */
    private String outLinePrefix;

    /**
     * err line prefix, option-type : exported (inherited in child workspaces)
     */
    private String errLinePrefix;

    /**
     * user friendly workspace name option-type : exported (inherited in child
     * workspaces)
     */
    private String name;

    /**
     * if true, do not install nuts companion tools upon workspace creation
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean installCompanions;

    /**
     * if true, do not run welcome when no application arguments were resolved.
     * defaults to false option-type : exported (inherited in child workspaces)
     *
     * @since 0.5.5
     */
    private Boolean skipWelcome;

    /**
     * if true, do not bootstrap workspace after reset/recover. When
     * reset/recover is not active this option is not accepted and an error will
     * be thrown
     *
     * @since 0.6.0
     */
    private Boolean skipBoot;

    /**
     * if true consider system repository
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean system;

    /**
     * if true consider GUI/Swing mode
     * <br>
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean gui;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> excludedExtensions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private List<String> repositories;
    private List<String> bootRepositories;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String userName;

    /**
     * option-type : runtime
     */
    private Boolean sharedInstance;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private char[] credentials;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NTerminalMode terminalMode;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean readOnly;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean trace;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String progressOptions;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String dependencySolver;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NLogConfig logConfig;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NConfirmationMode confirm;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NContentType outputFormat;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> applicationArguments;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NOpenMode openMode;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Instant creationTime;

    /**
     * if true no real execution,
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean dry;

    /**
     * if true show exception stacktrace
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean showStacktrace;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Supplier<ClassLoader> classLoaderSupplier;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private List<String> executorOptions;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean recover;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean reset;

    /**
     * @since 0.8.5
     * reset ALL workspaces
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean resetHard;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean commandVersion;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean commandHelp;

    /**
     * option-type : runtime / exported (depending on the value)
     */
    private String debug;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Boolean inherited;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private NExecutionType executionType;
    /**
     * option-type : runtime (available only for the current workspace instance)
     *
     * @since 0.8.1
     */
    private NRunAs runAs;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private String archetype;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     *
     * @since 0.8.0
     */
    private Boolean switchWorkspace;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NStoreType, String> storeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private Map<NHomeLocation, String> homeLocations;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NOsFamily storeLayout;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NStoreStrategy storeStrategy;

    /**
     * option-type : create (used when creating new workspace. will not be
     * exported nor promoted to runtime)
     */
    private NStoreStrategy repositoryStoreStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private NFetchStrategy fetchStrategy;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean cached;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean indexed;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean transitive;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean bot;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private Boolean previewRepo;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private InputStream stdin;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stdout;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private PrintStream stderr;

    /**
     * not parsed option-type : runtime (available only for the current
     * workspace instance)
     */
    private ExecutorService executorService;

    /**
     * option-type : runtime (available only for the current workspace instance)
     */
    private Instant expireTime;
    private List<NMsg> errors;
    private Boolean skipErrors;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String locale;

    /**
     * option-type : exported (inherited in child workspaces)
     */
    private String theme;

    private Boolean initLaunchers;
    private Boolean initScripts;
    private Boolean initPlatforms;
    private Boolean initJava;
    private NIsolationLevel isolationLevel;
    private NSupportMode desktopLauncher;
    private NSupportMode menuLauncher;
    private NSupportMode userLauncher;


    /**
     * special
     */
    private NClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private List<NBootDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private List<NClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private NBootWorkspaceFactory bootWorkspaceFactory;

    /**
     * special
     */
    private List<URL> classWorldURLs;

    /**
     * special
     */
    private ClassLoader classWorldLoader;

    /**
     * special
     */
    private String uuid;

    /**
     * special
     */
    private Set<String> extensionsSet;

    /**
     * special
     */
    private NBootDescriptor runtimeBootDescriptor;

    public DefaultNBootOptionsBuilder() {
    }

    public DefaultNBootOptionsBuilder(NBootOptionsInfo other) {
        copyFrom(other);
    }

    @Override
    public NBootOptionsBuilder copy() {
        return new DefaultNBootOptionsBuilder().copyFrom(this);
    }

    @Override
    public NBootOptions build() {
        return new DefaultNBootOptions(
                getOutputFormatOptions().orNull(), getCustomOptions().orNull(), getApiVersion().orNull(), getRuntimeId().orNull(), getJavaCommand().orNull(),
                getJavaOptions().orNull(), getWorkspace().orNull(), getOutLinePrefix().orNull(), getErrLinePrefix().orNull(),
                getName().orNull(), getInstallCompanions().orNull(), getSkipWelcome().orNull(), getSkipBoot().orNull(),
                getSystem().orNull(), getGui().orNull(), getDry().orNull(), getShowStacktrace().orNull(), getRecover().orNull(), getReset().orNull(), getResetHard().orNull(), getCommandVersion().orNull(), getCommandHelp().orNull(), getCommandHelp().orNull(), getSwitchWorkspace().orNull(), getCached().orNull(), getCached().orNull(), getTransitive().orNull(), getBot().orNull(),
                getIsolationLevel().orNull(), getInitLaunchers().orNull(), getInitScripts().orNull(), getInitPlatforms().orNull(),
                getInitJava().orNull(), getExcludedExtensions().orNull(), getRepositories().orNull(), getUserName().orNull(),
                getCredentials().orNull(), getTerminalMode().orNull(), getReadOnly().orNull(), getTrace().orNull(), getProgressOptions().orNull(),
                getDependencySolver().orNull(), getLogConfig().orNull(), getConfirm().orNull(), getOutputFormat().orNull(),
                getApplicationArguments().orNull(), getOpenMode().orNull(), getCreationTime().orNull(),
                getClassLoaderSupplier().orNull(), getExecutorOptions().orNull(),
                getDebug().orNull(),
                getExecutionType().orNull(), getRunAs().orNull(), getArchetype().orNull(),
                getStoreLocations().orNull(), getHomeLocations().orNull(), getStoreLayout().orNull(), getStoreStrategy().orNull(),
                getRepositoryStoreStrategy().orNull(), getFetchStrategy().orNull(),
                getStdin().orNull(), getStdout().orNull(), getStderr().orNull(),
                getExecutorService().orNull(), getExpireTime().orNull(), getErrors().orNull(), getSkipErrors().orNull(), getLocale().orNull(),
                getTheme().orNull(), getUuid().orNull(), getBootRepositories().orNull(), getRuntimeBootDependencyNode().orNull(), getExtensionBootDescriptors().orNull(),
                getExtensionBootDependencyNodes().orNull(), getClassWorldURLs().orNull(), getExtensionsSet().orNull(), getBootWorkspaceFactory().orNull(), getRuntimeBootDescriptor().orNull(), getClassWorldLoader().orNull(),
                getDesktopLauncher().orNull(), getMenuLauncher().orNull(), getUserLauncher().orNull(), getPreviewRepo().orNull(), getSharedInstance().orNull());
    }


    public NBootOptionsBuilder copyFrom(NWorkspaceOptions other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setInstallCompanions(other.getInstallCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setSystem(other.getSystem().orNull());
        this.setGui(other.getGui().orNull());
        this.setUserName(other.getUserName().orNull());
        this.setCredentials(other.getCredentials().orNull());
        this.setTerminalMode(other.getTerminalMode().orNull());
        this.setReadOnly(other.getReadOnly().orNull());
        this.setTrace(other.getTrace().orNull());
        this.setProgressOptions(other.getProgressOptions().orNull());
        this.setLogConfig(other.getLogConfig().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setOutputFormat(other.getOutputFormat().orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        this.setOpenMode(other.getOpenMode().orNull());
        this.setCreationTime(other.getCreationTime().orNull());
        this.setDry(other.getDry().orNull());
        this.setShowStacktrace(other.getShowStacktrace().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setResetHard(other.getResetHard().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLayout(other.getStoreLayout().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
        this.setFetchStrategy(other.getFetchStrategy().orNull());
        this.setCached(other.getCached().orNull());
        this.setIndexed(other.getIndexed().orNull());
        this.setTransitive(other.getTransitive().orNull());
        this.setBot(other.getBot().orNull());
        this.setStdin(other.getStdin().orNull());
        this.setStdout(other.getStdout().orNull());
        this.setStderr(other.getStderr().orNull());
        this.setExecutorService(other.getExecutorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(other.getExcludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(other.getRepositories().orNull());
        this.setApplicationArguments(other.getApplicationArguments().orNull());
        this.setCustomOptions(other.getCustomOptions().orNull());
        this.setExpireTime(other.getExpireTime().orNull());
        this.setErrors(other.getErrors().orNull());
        this.setSkipErrors(other.getSkipErrors().orNull());
        this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        this.setLocale(other.getLocale().orNull());
        this.setTheme(other.getTheme().orNull());
        this.setDependencySolver(other.getDependencySolver().orNull());
        this.setIsolationLevel(other.getIsolationLevel().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitPlatforms(other.getInitPlatforms().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        this.setSharedInstance(other.getSharedInstance().orNull());
        this.setPreviewRepo(other.getPreviewRepo().orNull());
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFrom(NBootOptions other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setInstallCompanions(other.getInstallCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setSystem(other.getSystem().orNull());
        this.setGui(other.getGui().orNull());
        this.setUserName(other.getUserName().orNull());
        this.setCredentials(other.getCredentials().orNull());
        this.setTerminalMode(other.getTerminalMode().orNull());
        this.setReadOnly(other.getReadOnly().orNull());
        this.setTrace(other.getTrace().orNull());
        this.setProgressOptions(other.getProgressOptions().orNull());
        this.setLogConfig(other.getLogConfig().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setOutputFormat(other.getOutputFormat().orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        this.setOpenMode(other.getOpenMode().orNull());
        this.setCreationTime(other.getCreationTime().orNull());
        this.setDry(other.getDry().orNull());
        this.setShowStacktrace(other.getShowStacktrace().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setResetHard(other.getResetHard().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLayout(other.getStoreLayout().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
        this.setFetchStrategy(other.getFetchStrategy().orNull());
        this.setCached(other.getCached().orNull());
        this.setIndexed(other.getIndexed().orNull());
        this.setTransitive(other.getTransitive().orNull());
        this.setBot(other.getBot().orNull());
        this.setStdin(other.getStdin().orNull());
        this.setStdout(other.getStdout().orNull());
        this.setStderr(other.getStderr().orNull());
        this.setExecutorService(other.getExecutorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(other.getExcludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(other.getRepositories().orNull());
        this.setApplicationArguments(other.getApplicationArguments().orNull());
        this.setCustomOptions(other.getCustomOptions().orNull());
        this.setExpireTime(other.getExpireTime().orNull());
        this.setErrors(other.getErrors().orNull());
        this.setSkipErrors(other.getSkipErrors().orNull());
        this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        this.setLocale(other.getLocale().orNull());
        this.setTheme(other.getTheme().orNull());
        this.setDependencySolver(other.getDependencySolver().orNull());
        this.setIsolationLevel(other.getIsolationLevel().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitPlatforms(other.getInitPlatforms().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        this.setSharedInstance(other.getSharedInstance().orNull());
        this.setPreviewRepo(other.getPreviewRepo().orNull());
        this.setBootRepositories(other.getBootRepositories().orNull());
        this.setRuntimeBootDependencyNode(other.getRuntimeBootDependencyNode().orNull());
        this.setExtensionBootDescriptors(other.getExtensionBootDescriptors().orNull());
        this.setExtensionBootDependencyNodes(other.getExtensionBootDependencyNodes().orNull());
        this.setBootWorkspaceFactory(other.getBootWorkspaceFactory().orNull());
        this.setClassWorldURLs(other.getClassWorldURLs().orNull());
        this.setClassWorldLoader(other.getClassWorldLoader().orNull());
        this.setUuid(other.getUuid().orNull());
        this.setExtensionsSet(other.getExtensionsSet().orNull());
        this.setRuntimeBootDescriptor(other.getRuntimeBootDescriptor().orNull());
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFrom(NBootOptionsBuilder other) {
        if (other == null) {
            return this;
        }
        this.setApiVersion(other.getApiVersion().orNull());
        this.setRuntimeId(other.getRuntimeId().orNull());
        this.setJavaCommand(other.getJavaCommand().orNull());
        this.setJavaOptions(other.getJavaOptions().orNull());
        this.setWorkspace(other.getWorkspace().orNull());
        this.setName(other.getName().orNull());
        this.setInstallCompanions(other.getInstallCompanions().orNull());
        this.setSkipWelcome(other.getSkipWelcome().orNull());
        this.setSkipBoot(other.getSkipBoot().orNull());
        this.setSystem(other.getSystem().orNull());
        this.setGui(other.getGui().orNull());
        this.setUserName(other.getUserName().orNull());
        this.setCredentials(other.getCredentials().orNull());
        this.setTerminalMode(other.getTerminalMode().orNull());
        this.setReadOnly(other.getReadOnly().orNull());
        this.setTrace(other.getTrace().orNull());
        this.setProgressOptions(other.getProgressOptions().orNull());
        this.setLogConfig(other.getLogConfig().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setConfirm(other.getConfirm().orNull());
        this.setOutputFormat(other.getOutputFormat().orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        this.setOpenMode(other.getOpenMode().orNull());
        this.setCreationTime(other.getCreationTime().orNull());
        this.setDry(other.getDry().orNull());
        this.setShowStacktrace(other.getShowStacktrace().orNull());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        this.setExecutorOptions(other.getExecutorOptions().orNull());
        this.setRecover(other.getRecover().orNull());
        this.setReset(other.getReset().orNull());
        this.setResetHard(other.getResetHard().orNull());
        this.setCommandVersion(other.getCommandVersion().orNull());
        this.setCommandHelp(other.getCommandHelp().orNull());
        this.setDebug(other.getDebug().orNull());
        this.setInherited(other.getInherited().orNull());
        this.setExecutionType(other.getExecutionType().orNull());
        this.setRunAs(other.getRunAs().orNull());
        this.setArchetype(other.getArchetype().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setHomeLocations(other.getHomeLocations().orNull());
        this.setStoreLocations(other.getStoreLocations().orNull());
        this.setStoreLayout(other.getStoreLayout().orNull());
        this.setStoreStrategy(other.getStoreStrategy().orNull());
        this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
        this.setFetchStrategy(other.getFetchStrategy().orNull());
        this.setCached(other.getCached().orNull());
        this.setIndexed(other.getIndexed().orNull());
        this.setTransitive(other.getTransitive().orNull());
        this.setBot(other.getBot().orNull());
        this.setStdin(other.getStdin().orNull());
        this.setStdout(other.getStdout().orNull());
        this.setStderr(other.getStderr().orNull());
        this.setExecutorService(other.getExecutorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.setExcludedExtensions(other.getExcludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.setRepositories(other.getRepositories().orNull());
        this.setBootRepositories(other.getBootRepositories().orNull());
        this.setApplicationArguments(other.getApplicationArguments().orNull());
        this.setCustomOptions(other.getCustomOptions().orNull());
        this.setExpireTime(other.getExpireTime().orNull());
        this.setErrors(other.getErrors().orNull());
        this.setSkipErrors(other.getSkipErrors().orNull());
        this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        this.setLocale(other.getLocale().orNull());
        this.setTheme(other.getTheme().orNull());
        this.setDependencySolver(other.getDependencySolver().orNull());
        this.setIsolationLevel(other.getIsolationLevel().orNull());
        this.setInitLaunchers(other.getInitLaunchers().orNull());
        this.setInitJava(other.getInitJava().orNull());
        this.setInitScripts(other.getInitScripts().orNull());
        this.setInitPlatforms(other.getInitPlatforms().orNull());
        this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        this.setMenuLauncher(other.getMenuLauncher().orNull());
        this.setUserLauncher(other.getUserLauncher().orNull());
        this.setSharedInstance(other.getSharedInstance().orNull());
        this.setPreviewRepo(other.getPreviewRepo().orNull());
//        this.setBootRepositories(other.getBootRepositories().orNull());
        this.setRuntimeBootDependencyNode(other.getRuntimeBootDependencyNode().orNull());
        this.setExtensionBootDescriptors(other.getExtensionBootDescriptors().orNull());
        this.setExtensionBootDependencyNodes(other.getExtensionBootDependencyNodes().orNull());
        this.setBootWorkspaceFactory(other.getBootWorkspaceFactory().orNull());
        this.setClassWorldURLs(other.getClassWorldURLs().orNull());
        this.setClassWorldLoader(other.getClassWorldLoader().orNull());
        this.setUuid(other.getUuid().orNull());
        this.setExtensionsSet(other.getExtensionsSet().orNull());
        this.setRuntimeBootDescriptor(other.getRuntimeBootDescriptor().orNull());
        return this;
    }

//    public NOptional<String> getBootRepositories() {
//        return NOptional.of(bootRepositories);
//    }
//
//    @Override
//    public DefaultNBootOptionsBuilder setBootRepositories(String bootRepositories) {
//        this.bootRepositories = NStringUtils.trimToNull(bootRepositories);
//        return this;
//    }

    public NOptional<NClassLoaderNode> getRuntimeBootDependencyNode() {
        return NOptional.of(runtimeBootDependencyNode);
    }

    @Override
    public DefaultNBootOptionsBuilder setRuntimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public NOptional<List<NBootDescriptor>> getExtensionBootDescriptors() {
        return NOptional.of(extensionBootDescriptors);
    }

    @Override
    public DefaultNBootOptionsBuilder setExtensionBootDescriptors(List<NBootDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = NReservedLangUtils.nonNullList(extensionBootDescriptors);
        return this;
    }

    public NOptional<List<NClassLoaderNode>> getExtensionBootDependencyNodes() {
        return NOptional.of(extensionBootDependencyNodes);
    }

    @Override
    public DefaultNBootOptionsBuilder setExtensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = NReservedLangUtils.nonNullList(extensionBootDependencyNodes);
        return this;
    }

    public NOptional<NBootWorkspaceFactory> getBootWorkspaceFactory() {
        return NOptional.of(bootWorkspaceFactory);
    }

    @Override
    public DefaultNBootOptionsBuilder setBootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public NOptional<List<URL>> getClassWorldURLs() {
        return NOptional.of(classWorldURLs);
    }

    @Override
    public DefaultNBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = NReservedLangUtils.nonNullList(classWorldURLs);
        return this;
    }

    public NOptional<ClassLoader> getClassWorldLoader() {
        return NOptional.of(classWorldLoader);
    }

    @Override
    public DefaultNBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public NOptional<String> getUuid() {
        return NOptional.of(uuid);
    }

    @Override
    public DefaultNBootOptionsBuilder setUuid(String uuid) {
        this.uuid = NStringUtils.trimToNull(uuid);
        return this;
    }

    public NOptional<Set<String>> getExtensionsSet() {
        return NOptional.of(extensionsSet);
    }

    @Override
    public DefaultNBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = NReservedLangUtils.nonNullSet(extensionsSet);
        return this;
    }

    public NOptional<NBootDescriptor> getRuntimeBootDescriptor() {
        return NOptional.of(runtimeBootDescriptor);
    }

    @Override
    public DefaultNBootOptionsBuilder setRuntimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }


    @Override
    public NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other) {
        if (other.getApiVersion().isPresent()) {
            this.setApiVersion(other.getApiVersion().orNull());
        }
        if (other.getRuntimeId().isPresent()) {
            this.setRuntimeId(other.getRuntimeId().orNull());
        }
        if (other.getJavaCommand().isPresent()) {
            this.setJavaCommand(other.getJavaCommand().orNull());
        }
        if (other.getJavaOptions().isPresent()) {
            this.setJavaOptions(other.getJavaOptions().orNull());
        }
        if (other.getWorkspace().isPresent()) {
            this.setWorkspace(other.getWorkspace().orNull());
        }
        if (other.getName().isPresent()) {
            this.setName(other.getName().orNull());
        }
        if (other.getInstallCompanions().isPresent()) {
            this.setInstallCompanions(other.getInstallCompanions().orNull());
        }
        if (other.getSkipWelcome().isPresent()) {
            this.setSkipWelcome(other.getSkipWelcome().orNull());
        }
        if (other.getSkipBoot().isPresent()) {
            this.setSkipBoot(other.getSkipBoot().orNull());
        }
        if (other.getSystem().isPresent()) {
            this.setSystem(other.getSystem().orNull());
        }
        if (other.getGui().isPresent()) {
            this.setGui(other.getGui().orNull());
        }
        if (other.getUserName().isPresent()) {
            this.setUserName(other.getUserName().orNull());
        }
        if (other.getCredentials().isPresent()) {
            this.setCredentials(other.getCredentials().orNull());
        }
        if (other.getTerminalMode().isPresent()) {
            this.setTerminalMode(other.getTerminalMode().orNull());
        }
        if (other.getReadOnly().isPresent()) {
            this.setReadOnly(other.getReadOnly().orNull());
        }
        if (other.getTrace().isPresent()) {
            this.setTrace(other.getTrace().orNull());
        }
        if (other.getProgressOptions().isPresent()) {
            this.setProgressOptions(other.getProgressOptions().orNull());
        }
        if (other.getLogConfig().isPresent()) {
            this.setLogConfig(other.getLogConfig().orNull());
        }
        if (other.getConfirm().isPresent()) {
            this.setConfirm(other.getConfirm().orNull());
        }
        if (other.getConfirm().isPresent()) {
            this.setConfirm(other.getConfirm().orNull());
        }
        if (other.getOutputFormat().isPresent()) {
            this.setOutputFormat(other.getOutputFormat().orNull());
        }
        if (other.getOutputFormatOptions().isPresent()) {
            this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        }
        if (other.getOpenMode().isPresent()) {
            this.setOpenMode(other.getOpenMode().orNull());
        }
        if (other.getCreationTime().isPresent()) {
            this.setCreationTime(other.getCreationTime().orNull());
        }
        if (other.getDry().isPresent()) {
            this.setDry(other.getDry().orNull());
        }
        if (other.getShowStacktrace().isPresent()) {
            this.setShowStacktrace(other.getShowStacktrace().orNull());
        }
        if (other.getClassLoaderSupplier().isPresent()) {
            this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        }
        if (other.getExecutorOptions().isPresent()) {
            this.setExecutorOptions(other.getExecutorOptions().orNull());
        }
        if (other.getRecover().isPresent()) {
            this.setRecover(other.getRecover().orNull());
        }
        if (other.getReset().isPresent()) {
            this.setReset(other.getReset().orNull());
        }
        if (other.getResetHard().isPresent()) {
            this.setResetHard(other.getResetHard().orNull());
        }
        if (other.getCommandVersion().isPresent()) {
            this.setCommandVersion(other.getCommandVersion().orNull());
        }
        if (other.getCommandHelp().isPresent()) {
            this.setCommandHelp(other.getCommandHelp().orNull());
        }
        if (other.getDebug().isPresent()) {
            this.setDebug(other.getDebug().orNull());
        }
        if (other.getInherited().isPresent()) {
            this.setInherited(other.getInherited().orNull());
        }
        if (other.getExecutionType().isPresent()) {
            this.setExecutionType(other.getExecutionType().orNull());
        }
        if (other.getRunAs().isPresent()) {
            this.setRunAs(other.getRunAs().orNull());
        }
        if (other.getArchetype().isPresent()) {
            this.setArchetype(other.getArchetype().orNull());
        }
        if (other.getStoreStrategy().isPresent()) {
            this.setStoreStrategy(other.getStoreStrategy().orNull());
        }
        if (other.getHomeLocations().isPresent()) {
            this.setHomeLocations(other.getHomeLocations().orNull());
        }

        if (other.getStoreLocations().isPresent()) {
            this.setStoreLocations(other.getStoreLocations().orNull());
        }
        if (other.getStoreLayout().isPresent()) {
            this.setStoreLayout(other.getStoreLayout().orNull());
        }
        if (other.getStoreStrategy().isPresent()) {
            this.setStoreStrategy(other.getStoreStrategy().orNull());
        }
        if (other.getRepositoryStoreStrategy().isPresent()) {
            this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
        }
        if (other.getFetchStrategy().isPresent()) {
            this.setFetchStrategy(other.getFetchStrategy().orNull());
        }
        if (other.getCached().isPresent()) {
            this.setCached(other.getCached().orNull());
        }
        if (other.getIndexed().isPresent()) {
            this.setIndexed(other.getIndexed().orNull());
        }
        if (other.getTransitive().isPresent()) {
            this.setTransitive(other.getTransitive().orNull());
        }
        if (other.getBot().isPresent()) {
            this.setBot(other.getBot().orNull());
        }
        if (other.getStdin().isPresent()) {
            this.setStdin(other.getStdin().orNull());
        }
        if (other.getStdout().isPresent()) {
            this.setStdout(other.getStdout().orNull());
        }
        if (other.getStderr().isPresent()) {
            this.setStderr(other.getStderr().orNull());
        }
        if (other.getExecutorService().isPresent()) {
            this.setExecutorService(other.getExecutorService().orNull());
        }
        if (other.getExcludedExtensions().isPresent()) {
            this.setExcludedExtensions(other.getExcludedExtensions().orNull());
        }
        if (other.getRepositories().isPresent()) {
            this.setRepositories(other.getRepositories().orNull());
        }
        if (other.getApplicationArguments().isPresent()) {
            this.setApplicationArguments(other.getApplicationArguments().orNull());
        }
        if (other.getCustomOptions().isPresent()) {
            this.setCustomOptions(other.getCustomOptions().orNull());
        }
        if (other.getExpireTime().isPresent()) {
            this.setExpireTime(other.getExpireTime().orNull());
        }
        if (other.getErrors().isPresent()) {
            this.setErrors(other.getErrors().orNull());
        }
        if (other.getSkipErrors().isPresent()) {
            this.setSkipErrors(other.getSkipErrors().orNull());
        }
        if (other.getSwitchWorkspace().isPresent()) {
            this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        }
        if (other.getLocale().isPresent()) {
            this.setLocale(other.getLocale().orNull());
        }
        if (other.getTheme().isPresent()) {
            this.setTheme(other.getTheme().orNull());
        }
        if (other.getDependencySolver().isPresent()) {
            this.setDependencySolver(other.getDependencySolver().orNull());
        }
        if (other.getIsolationLevel().isPresent()) {
            this.setIsolationLevel(other.getIsolationLevel().orNull());
        }
        if (other.getInitLaunchers().isPresent()) {
            this.setInitLaunchers(other.getInitLaunchers().orNull());
        }
        if (other.getInitJava().isPresent()) {
            this.setInitJava(other.getInitJava().orNull());
        }
        if (other.getInitScripts().isPresent()) {
            this.setInitScripts(other.getInitScripts().orNull());
        }
        if (other.getInitLaunchers().isPresent()) {
            this.setInitLaunchers(other.getInitLaunchers().orNull());
        }
        if (other.getDesktopLauncher().isPresent()) {
            this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        }
        if (other.getMenuLauncher().isPresent()) {
            this.setMenuLauncher(other.getMenuLauncher().orNull());
        }
        if (other.getUserLauncher().isPresent()) {
            this.setUserLauncher(other.getUserLauncher().orNull());
        }
        if (other.getPreviewRepo().isPresent()) {
            this.setPreviewRepo(other.getPreviewRepo().orNull());
        }
        if (other.getSharedInstance().isPresent()) {
            this.setSharedInstance(other.getSharedInstance().orNull());
        }
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFromIfPresent(NBootOptions other) {
        if (other.getApiVersion().isPresent()) {
            this.setApiVersion(other.getApiVersion().orNull());
        }
        if (other.getRuntimeId().isPresent()) {
            this.setRuntimeId(other.getRuntimeId().orNull());
        }
        if (other.getJavaCommand().isPresent()) {
            this.setJavaCommand(other.getJavaCommand().orNull());
        }
        if (other.getJavaOptions().isPresent()) {
            this.setJavaOptions(other.getJavaOptions().orNull());
        }
        if (other.getWorkspace().isPresent()) {
            this.setWorkspace(other.getWorkspace().orNull());
        }
        if (other.getName().isPresent()) {
            this.setName(other.getName().orNull());
        }
        if (other.getInstallCompanions().isPresent()) {
            this.setInstallCompanions(other.getInstallCompanions().orNull());
        }
        if (other.getSkipWelcome().isPresent()) {
            this.setSkipWelcome(other.getSkipWelcome().orNull());
        }
        if (other.getSkipBoot().isPresent()) {
            this.setSkipBoot(other.getSkipBoot().orNull());
        }
        if (other.getSystem().isPresent()) {
            this.setSystem(other.getSystem().orNull());
        }
        if (other.getGui().isPresent()) {
            this.setGui(other.getGui().orNull());
        }
        if (other.getUserName().isPresent()) {
            this.setUserName(other.getUserName().orNull());
        }
        if (other.getCredentials().isPresent()) {
            this.setCredentials(other.getCredentials().orNull());
        }
        if (other.getTerminalMode().isPresent()) {
            this.setTerminalMode(other.getTerminalMode().orNull());
        }
        if (other.getReadOnly().isPresent()) {
            this.setReadOnly(other.getReadOnly().orNull());
        }
        if (other.getTrace().isPresent()) {
            this.setTrace(other.getTrace().orNull());
        }
        if (other.getProgressOptions().isPresent()) {
            this.setProgressOptions(other.getProgressOptions().orNull());
        }
        if (other.getLogConfig().isPresent()) {
            this.setLogConfig(other.getLogConfig().orNull());
        }
        if (other.getConfirm().isPresent()) {
            this.setConfirm(other.getConfirm().orNull());
        }
        if (other.getConfirm().isPresent()) {
            this.setConfirm(other.getConfirm().orNull());
        }
        if (other.getOutputFormat().isPresent()) {
            this.setOutputFormat(other.getOutputFormat().orNull());
        }
        if (other.getOutputFormatOptions().isPresent()) {
            this.setOutputFormatOptions(other.getOutputFormatOptions().orNull());
        }
        if (other.getOpenMode().isPresent()) {
            this.setOpenMode(other.getOpenMode().orNull());
        }
        if (other.getCreationTime().isPresent()) {
            this.setCreationTime(other.getCreationTime().orNull());
        }
        if (other.getDry().isPresent()) {
            this.setDry(other.getDry().orNull());
        }
        if (other.getShowStacktrace().isPresent()) {
            this.setShowStacktrace(other.getShowStacktrace().orNull());
        }
        if (other.getClassLoaderSupplier().isPresent()) {
            this.setClassLoaderSupplier(other.getClassLoaderSupplier().orNull());
        }
        if (other.getExecutorOptions().isPresent()) {
            this.setExecutorOptions(other.getExecutorOptions().orNull());
        }
        if (other.getRecover().isPresent()) {
            this.setRecover(other.getRecover().orNull());
        }
        if (other.getReset().isPresent()) {
            this.setReset(other.getReset().orNull());
        }
        if (other.getResetHard().isPresent()) {
            this.setResetHard(other.getResetHard().orNull());
        }
        if (other.getCommandVersion().isPresent()) {
            this.setCommandVersion(other.getCommandVersion().orNull());
        }
        if (other.getCommandHelp().isPresent()) {
            this.setCommandHelp(other.getCommandHelp().orNull());
        }
        if (other.getDebug().isPresent()) {
            this.setDebug(other.getDebug().orNull());
        }
        if (other.getInherited().isPresent()) {
            this.setInherited(other.getInherited().orNull());
        }
        if (other.getExecutionType().isPresent()) {
            this.setExecutionType(other.getExecutionType().orNull());
        }
        if (other.getRunAs().isPresent()) {
            this.setRunAs(other.getRunAs().orNull());
        }
        if (other.getArchetype().isPresent()) {
            this.setArchetype(other.getArchetype().orNull());
        }
        if (other.getStoreStrategy().isPresent()) {
            this.setStoreStrategy(other.getStoreStrategy().orNull());
        }
        if (other.getHomeLocations().isPresent()) {
            this.setHomeLocations(other.getHomeLocations().orNull());
        }

        if (other.getStoreLocations().isPresent()) {
            this.setStoreLocations(other.getStoreLocations().orNull());
        }
        if (other.getStoreLayout().isPresent()) {
            this.setStoreLayout(other.getStoreLayout().orNull());
        }
        if (other.getStoreStrategy().isPresent()) {
            this.setStoreStrategy(other.getStoreStrategy().orNull());
        }
        if (other.getRepositoryStoreStrategy().isPresent()) {
            this.setRepositoryStoreStrategy(other.getRepositoryStoreStrategy().orNull());
        }
        if (other.getFetchStrategy().isPresent()) {
            this.setFetchStrategy(other.getFetchStrategy().orNull());
        }
        if (other.getCached().isPresent()) {
            this.setCached(other.getCached().orNull());
        }
        if (other.getIndexed().isPresent()) {
            this.setIndexed(other.getIndexed().orNull());
        }
        if (other.getTransitive().isPresent()) {
            this.setTransitive(other.getTransitive().orNull());
        }
        if (other.getBot().isPresent()) {
            this.setBot(other.getBot().orNull());
        }
        if (other.getStdin().isPresent()) {
            this.setStdin(other.getStdin().orNull());
        }
        if (other.getStdout().isPresent()) {
            this.setStdout(other.getStdout().orNull());
        }
        if (other.getStderr().isPresent()) {
            this.setStderr(other.getStderr().orNull());
        }
        if (other.getExecutorService().isPresent()) {
            this.setExecutorService(other.getExecutorService().orNull());
        }
        if (other.getExcludedExtensions().isPresent()) {
            this.setExcludedExtensions(other.getExcludedExtensions().orNull());
        }
        if (other.getRepositories().isPresent()) {
            this.setRepositories(other.getRepositories().orNull());
        }
        if (other.getApplicationArguments().isPresent()) {
            this.setApplicationArguments(other.getApplicationArguments().orNull());
        }
        if (other.getCustomOptions().isPresent()) {
            this.setCustomOptions(other.getCustomOptions().orNull());
        }
        if (other.getExpireTime().isPresent()) {
            this.setExpireTime(other.getExpireTime().orNull());
        }
        if (other.getErrors().isPresent()) {
            this.setErrors(other.getErrors().orNull());
        }
        if (other.getSkipErrors().isPresent()) {
            this.setSkipErrors(other.getSkipErrors().orNull());
        }
        if (other.getSwitchWorkspace().isPresent()) {
            this.setSwitchWorkspace(other.getSwitchWorkspace().orNull());
        }
        if (other.getLocale().isPresent()) {
            this.setLocale(other.getLocale().orNull());
        }
        if (other.getTheme().isPresent()) {
            this.setTheme(other.getTheme().orNull());
        }
        if (other.getDependencySolver().isPresent()) {
            this.setDependencySolver(other.getDependencySolver().orNull());
        }
        if (other.getIsolationLevel().isPresent()) {
            this.setIsolationLevel(other.getIsolationLevel().orNull());
        }
        if (other.getInitLaunchers().isPresent()) {
            this.setInitLaunchers(other.getInitLaunchers().orNull());
        }
        if (other.getInitJava().isPresent()) {
            this.setInitJava(other.getInitJava().orNull());
        }
        if (other.getInitScripts().isPresent()) {
            this.setInitScripts(other.getInitScripts().orNull());
        }
        if (other.getInitLaunchers().isPresent()) {
            this.setInitLaunchers(other.getInitLaunchers().orNull());
        }
        if (other.getDesktopLauncher().isPresent()) {
            this.setDesktopLauncher(other.getDesktopLauncher().orNull());
        }
        if (other.getMenuLauncher().isPresent()) {
            this.setMenuLauncher(other.getMenuLauncher().orNull());
        }
        if (other.getUserLauncher().isPresent()) {
            this.setUserLauncher(other.getUserLauncher().orNull());
        }
        if (other.getPreviewRepo().isPresent()) {
            this.setPreviewRepo(other.getPreviewRepo().orNull());
        }
        if (other.getSharedInstance().isPresent()) {
            this.setSharedInstance(other.getSharedInstance().orNull());
        }
        if (other.getBootRepositories().isPresent()) {
            setBootRepositories(other.getBootRepositories().orNull());
        }
        if (other.getRuntimeBootDependencyNode().isPresent()) {
            setRuntimeBootDependencyNode(other.getRuntimeBootDependencyNode().orNull());
        }
        if (other.getExtensionBootDescriptors().isPresent()) {
            setExtensionBootDescriptors(other.getExtensionBootDescriptors().orNull());
        }
        if (other.getExtensionBootDependencyNodes().isPresent()) {
            setExtensionBootDependencyNodes(other.getExtensionBootDependencyNodes().orNull());
        }
        if (other.getBootWorkspaceFactory().isPresent()) {
            setBootWorkspaceFactory(other.getBootWorkspaceFactory().orNull());
        }
        if (other.getClassWorldURLs().isPresent()) {
            setClassWorldURLs(other.getClassWorldURLs().orNull());
        }
        if (other.getClassWorldLoader().isPresent()) {
            setClassWorldLoader(other.getClassWorldLoader().orNull());
        }
        if (other.getUuid().isPresent()) {
            setUuid(other.getUuid().orNull());
        }
        return this;
    }

    public NBootOptionsInfo toBootOptions() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.setApiVersion(this.getApiVersion().map(x -> x.toString()).orNull());
        r.setRuntimeId(this.getRuntimeId().map(x -> x.toString()).orNull());
        r.setJavaCommand(this.getJavaCommand().orNull());
        r.setJavaOptions(this.getJavaOptions().orNull());
        r.setWorkspace(this.getWorkspace().orNull());
        r.setName(this.getName().orNull());
        r.setInstallCompanions(this.getInstallCompanions().orNull());
        r.setSkipWelcome(this.getSkipWelcome().orNull());
        r.setSkipBoot(this.getSkipBoot().orNull());
        r.setSystem(this.getSystem().orNull());
        r.setGui(this.getGui().orNull());
        r.setUserName(this.getUserName().orNull());
        r.setCredentials(this.getCredentials().orNull());
        r.setTerminalMode(this.getTerminalMode().map(x -> x.id()).orNull());
        r.setReadOnly(this.getReadOnly().orNull());
        r.setTrace(this.getTrace().orNull());
        r.setProgressOptions(this.getProgressOptions().orNull());
        {
            NLogConfig c = this.getLogConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.setLogFileBase(c.getLogFileBase());
                v.setLogFileLevel(c.getLogFileLevel());
                v.setLogTermLevel(c.getLogTermLevel());
                v.setLogFileSize(c.getLogFileSize());
                v.setLogFileCount(c.getLogFileCount());
                v.setLogFileName(c.getLogFileName());
                v.setLogFileBase(c.getLogFileBase());
            }
            r.setLogConfig(v);
        }
        r.setConfirm(this.getConfirm().map(x -> x.id()).orNull());
        r.setConfirm(this.getConfirm().map(x -> x.id()).orNull());
        r.setOutputFormat(this.getOutputFormat().map(x -> x.id()).orNull());
        r.setOutputFormatOptions(this.getOutputFormatOptions().orNull());
        r.setOpenMode(this.getOpenMode().map(x -> x.id()).orNull());
        r.setCreationTime(this.getCreationTime().orNull());
        r.setDry(this.getDry().orNull());
        r.setShowStacktrace(this.getShowStacktrace().orNull());
        r.setClassLoaderSupplier(this.getClassLoaderSupplier().orNull());
        r.setExecutorOptions(this.getExecutorOptions().orNull());
        r.setRecover(this.getRecover().orNull());
        r.setReset(this.getReset().orNull());
        r.setResetHard(this.getResetHard().orNull());
        r.setCommandVersion(this.getCommandVersion().orNull());
        r.setCommandHelp(this.getCommandHelp().orNull());
        r.setDebug(this.getDebug().orNull());
        r.setInherited(this.getInherited().orNull());
        r.setExecutionType(this.getExecutionType().map(x -> x.id()).orNull());
        r.setRunAs(this.getRunAs().map(x -> x.toString()).orNull());
        r.setArchetype(this.getArchetype().orNull());
        r.setStoreStrategy(this.getStoreStrategy().map(x -> x.id()).orNull());
        {
            Map<NHomeLocation, String> c = this.getHomeLocations().orNull();
            Map<NBootHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NHomeLocation, String> e : c.entrySet()) {
                    v.put(NBootHomeLocation.of(
                            e.getKey().getOsFamily().id(),
                            e.getKey().getStoreLocation().id()
                    ), e.getValue());
                }
            }
            r.setHomeLocations(v);
        }
        {
            Map<NStoreType, String> c = this.getStoreLocations().orNull();
            Map<String, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NStoreType, String> e : c.entrySet()) {
                    v.put(e.getKey().id(), e.getValue());
                }
            }
            r.setStoreLocations(v);
        }
        r.setStoreLayout(this.getStoreLayout().map(x -> x.toString()).orNull());
        r.setStoreStrategy(this.getStoreStrategy().map(x -> x.toString()).orNull());
        r.setRepositoryStoreStrategy(this.getRepositoryStoreStrategy().map(x -> x.toString()).orNull());
        r.setFetchStrategy(this.getFetchStrategy().map(x -> x.toString()).orNull());
        r.setCached(this.getCached().orNull());
        r.setIndexed(this.getIndexed().orNull());
        r.setTransitive(this.getTransitive().orNull());
        r.setBot(this.getBot().orNull());
        r.setStdin(this.getStdin().orNull());
        r.setStdout(this.getStdout().orNull());
        r.setStderr(this.getStderr().orNull());
        r.setExecutorService(this.getExecutorService().orNull());

        r.setExcludedExtensions(this.getExcludedExtensions().orNull());
        r.setRepositories(this.getRepositories().orNull());
        r.setApplicationArguments(this.getApplicationArguments().orNull());
        r.setCustomOptions(this.getCustomOptions().orNull());
        r.setExpireTime(this.getExpireTime().orNull());
        r.setErrors(this.getErrors().isNotPresent() ? new ArrayList<>() : this.getErrors().get().stream().map(x -> x.toString()).collect(Collectors.toList()));
        r.setSkipErrors(this.getSkipErrors().orNull());
        r.setSwitchWorkspace(this.getSwitchWorkspace().orNull());
        r.setLocale(this.getLocale().orNull());
        r.setTheme(this.getTheme().orNull());
        r.setDependencySolver(this.getDependencySolver().orNull());
        r.setIsolationLevel(this.getIsolationLevel().map(x -> x.id()).orNull());
        r.setInitLaunchers(this.getInitLaunchers().orNull());
        r.setInitJava(this.getInitJava().orNull());
        r.setInitScripts(this.getInitScripts().orNull());
        r.setInitPlatforms(this.getInitPlatforms().orNull());
        r.setDesktopLauncher(this.getDesktopLauncher().map(x -> x.id()).orNull());
        r.setMenuLauncher(this.getMenuLauncher().map(x -> x.id()).orNull());
        r.setUserLauncher(this.getUserLauncher().map(x -> x.id()).orNull());
        r.setSharedInstance(this.getSharedInstance().orNull());
        r.setPreviewRepo(this.getPreviewRepo().orNull());
        return r;
    }

    /// ///////////////////////


    @Override
    public NOptional<NSupportMode> getDesktopLauncher() {
        return NOptional.ofNamed(desktopLauncher, "desktopLauncher");
    }

    @Override
    public NOptional<NSupportMode> getMenuLauncher() {
        return NOptional.ofNamed(menuLauncher, "menuLauncher");
    }

    @Override
    public NOptional<NSupportMode> getUserLauncher() {
        return NOptional.ofNamed(userLauncher, "userLauncher");
    }

    @Override
    public NBootOptionsBuilder setInitLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }

    @Override
    public NBootOptionsBuilder setInitScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }

    @Override
    public NBootOptionsBuilder setInitPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }

    @Override
    public NBootOptionsBuilder setInitJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }

    @Override
    public NBootOptionsBuilder setIsolationLevel(NIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }

    @Override
    public NBootOptionsBuilder setDesktopLauncher(NSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }

    @Override
    public NBootOptionsBuilder setMenuLauncher(NSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }

    @Override
    public NBootOptionsBuilder setUserLauncher(NSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }


    @Override
    public NOptional<NVersion> getApiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setApiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NOptional<List<String>> getApplicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setApplicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NOptional<String> getArchetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public NOptional<Supplier<ClassLoader>> getClassLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setClassLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NOptional<NConfirmationMode> getConfirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setConfirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NOptional<Boolean> getDry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> getShowStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setDry(Boolean dry) {
        this.dry = dry;
        return this;
    }

    /**
     * set dry
     *
     * @param showStacktrace showStacktrace
     * @return {@code this} instance
     * @since 0.8.4
     */
    @Override
    public NBootOptionsBuilder setShowStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    @Override
    public NOptional<Instant> getCreationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public NOptional<List<String>> getExcludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setExcludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public NOptional<NExecutionType> getExecutionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NOptional<NRunAs> getRunAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NBootOptionsBuilder setRunAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NOptional<List<String>> getExecutorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "homeLocations[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> getHomeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NBootOptionsBuilder setHomeLocations(Map<NHomeLocation, String> homeLocations) {
        if (homeLocations != null) {
            if (this.homeLocations == null) {
                this.homeLocations = new HashMap<>();
            }
            this.homeLocations.putAll(homeLocations);
        } else {
            this.homeLocations = null;
        }
        return this;
    }

    @Override
    public NOptional<String> getJavaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NBootOptionsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NOptional<String> getJavaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public NOptional<NLogConfig> getLogConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setLogConfig(NLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }

    @Override
    public NOptional<String> getName() {
        return NOptional.ofNamed(name, "name");
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setName(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public NOptional<NOpenMode> getOpenMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setOpenMode(NOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NOptional<NContentType> getOutputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setOutputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NOptional<List<String>> getOutputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setOutputFormatOptions(List<String> options) {
        if (options != null) {
            if (outputFormatOptions == null) {
                outputFormatOptions = new ArrayList<>();
            }
            this.outputFormatOptions.clear();
            return addOutputFormatOptions(NReservedLangUtils.nonNullList(options).toArray(new String[0]));
        } else {
            this.outputFormatOptions = null;
        }
        return this;
    }

    public NBootOptionsBuilder setOutputFormatOptions(String... options) {
        if (outputFormatOptions == null) {
            outputFormatOptions = new ArrayList<>();
        }
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NOptional<char[]> getCredentials() {
        return NOptional.ofNamed(credentials, "credentials");
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setCredentials(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> getRepositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setRepositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    @Override
    public NOptional<NId> getRuntimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setRuntimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> getStoreLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setStoreLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> getStoreStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    @Override
    public NOptional<Map<NStoreType, String>> getStoreLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NBootOptionsBuilder setStoreLocations(Map<NStoreType, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(NReservedLangUtils.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return this;
    }

    @Override
    public NOptional<NTerminalMode> getTerminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setTerminalMode(NTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NOptional<List<String>> getRepositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }
    @Override
    public NOptional<List<String>> getBootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setRepositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    @Override
    public NBootOptionsBuilder setBootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }



    @Override
    public NOptional<String> getUserName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> getWorkspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NOptional<String> getDebug() {
        return NOptional.ofNamed(debug, "debug");
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NOptional<Boolean> getSystem() {
        return NOptional.ofNamed(system, "system");
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setSystem(Boolean system) {
        this.system = system;
        return this;
    }

    @Override
    public NOptional<Boolean> getGui() {
        return NOptional.ofNamed(gui, "gui");
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setGui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public NOptional<Boolean> getInherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setInherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NOptional<Boolean> getReadOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public NOptional<Boolean> getRecover() {
        return NOptional.ofNamed(recover, "recover");
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setRecover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public NOptional<Boolean> getReset() {
        return NOptional.ofNamed(reset, "reset");
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setReset(Boolean reset) {
        this.reset = reset;
        return this;
    }

    @Override
    public NOptional<Boolean> getResetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }



    /**
     * set reset hard
     *
     * @param resetHard new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setResetHard(Boolean resetHard) {
        this.resetHard = resetHard;
        return this;
    }

    @Override
    public NOptional<Boolean> getCommandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NBootOptionsBuilder setCommandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NOptional<Boolean> getCommandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NBootOptionsBuilder setCommandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NOptional<Boolean> getInstallCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setInstallCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setSkipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NOptional<String> getOutLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NBootOptionsBuilder setOutLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<String> getErrLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NBootOptionsBuilder setErrLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setSkipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    @Override
    public NOptional<Boolean> getTrace() {
        return NOptional.ofNamed(trace, "trace");
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setTrace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public NOptional<String> getProgressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NBootOptionsBuilder setProgressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> getCached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NBootOptionsBuilder setCached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NOptional<Boolean> getIndexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NBootOptionsBuilder setIndexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NOptional<Boolean> getTransitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NBootOptionsBuilder setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NOptional<Boolean> getBot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NBootOptionsBuilder setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NOptional<Boolean> getPreviewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    @Override
    public NBootOptionsBuilder setPreviewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }

    @Override
    public NOptional<NFetchStrategy> getFetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NBootOptionsBuilder setFetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NOptional<InputStream> getStdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NBootOptionsBuilder setStdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NBootOptionsBuilder setStdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NOptional<PrintStream> getStderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NBootOptionsBuilder setStderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NOptional<ExecutorService> getExecutorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NBootOptionsBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NOptional<Instant> getExpireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NBootOptionsBuilder setExpireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NOptional<Boolean> getSkipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NBootOptionsBuilder setSkipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NOptional<Boolean> getSwitchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    public NBootOptionsBuilder setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NOptional<List<NMsg>> getErrors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NBootOptionsBuilder setErrors(List<NMsg> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NBootOptionsBuilder setCustomOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NOptional<String> getLocale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NBootOptionsBuilder setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NOptional<String> getTheme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NBootOptionsBuilder setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    public NBootOptionsBuilder copyFrom(NBootOptionsInfo other) {
        this.setApiVersion(other.getApiVersion() == null ? null : NVersion.get(other.getApiVersion()).orNull());
        this.setRuntimeId(other.getRuntimeId() == null ? null :
                other.getRuntimeId().contains("#") ? NId.get(other.getRuntimeId()).orNull() :
                        NId.getRuntime(other.getRuntimeId()).orNull()
        );
        this.setJavaCommand(other.getJavaCommand());
        this.setJavaOptions(other.getJavaOptions());
        this.setWorkspace(other.getWorkspace());
        this.setName(other.getName());
        this.setInstallCompanions(other.getInstallCompanions());
        this.setSkipWelcome(other.getSkipWelcome());
        this.setSkipBoot(other.getSkipBoot());
        this.setSystem(other.getSystem());
        this.setGui(other.getGui());
        this.setUserName(other.getUserName());
        this.setCredentials(other.getCredentials());
        this.setTerminalMode(NTerminalMode.parse(other.getTerminalMode()).orNull());
        this.setReadOnly(other.getReadOnly());
        this.setTrace(other.getTrace());
        this.setProgressOptions(other.getProgressOptions());
        {
            NBootLogConfig c = other.getLogConfig();
            NLogConfig v = null;
            if (c != null) {
                v = new NLogConfig();
                v.setLogFileBase(c.getLogFileBase());
                v.setLogFileLevel(c.getLogFileLevel());
                v.setLogTermLevel(c.getLogTermLevel());
                v.setLogFileSize(c.getLogFileSize());
                v.setLogFileCount(c.getLogFileCount());
                v.setLogFileName(c.getLogFileName());
                v.setLogFileBase(c.getLogFileBase());
            }
            this.setLogConfig(v);
        }
        this.setConfirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.setConfirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.setOutputFormat(NContentType.parse(other.getOutputFormat()).orNull());
        this.setOutputFormatOptions(other.getOutputFormatOptions());
        this.setOpenMode(NOpenMode.parse(other.getOpenMode()).orNull());
        this.setCreationTime(other.getCreationTime());
        this.setDry(other.getDry());
        this.setShowStacktrace(other.getShowStacktrace());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier());
        this.setExecutorOptions(other.getExecutorOptions());
        this.setRecover(other.getRecover());
        this.setReset(other.getReset());
        this.setResetHard(other.getResetHard());
        this.setCommandVersion(other.getCommandVersion());
        this.setCommandHelp(other.getCommandHelp());
        this.setDebug(other.getDebug());
        this.setInherited(other.getInherited());
        this.setExecutionType(NExecutionType.parse(other.getExecutionType()).orNull());
        this.setRunAs(NRunAs.parse(other.getRunAs()).orNull());
        this.setArchetype(other.getArchetype());
        this.setStoreStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        {
            Map<NBootHomeLocation, String> c = other.getHomeLocations();
            Map<NHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NBootHomeLocation, String> e : c.entrySet()) {
                    v.put(NHomeLocation.of(
                            NOsFamily.parse(e.getKey().getOsFamily()).orNull(),
                            NStoreType.parse(e.getKey().getStoreLocation()).get()
                    ), e.getValue());
                }
            }
            this.setHomeLocations(v);
        }
        {
            Map<String, String> c = other.getStoreLocations();
            Map<NStoreType, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<String, String> e : c.entrySet()) {
                    v.put(NStoreType.parse(e.getKey()).get(), e.getValue());
                }
            }
            this.setStoreLocations(v);
        }
        this.setStoreLayout(NOsFamily.parse(other.getStoreLayout()).orNull());
        this.setStoreStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        this.setRepositoryStoreStrategy(NStoreStrategy.parse(other.getRepositoryStoreStrategy()).orNull());
        this.setFetchStrategy(NFetchStrategy.parse(other.getFetchStrategy()).orNull());
        this.setCached(other.getCached());
        this.setIndexed(other.getIndexed());
        this.setTransitive(other.getTransitive());
        this.setBot(other.getBot());
        this.setStdin(other.getStdin());
        this.setStdout(other.getStdout());
        this.setStderr(other.getStderr());
        this.setExecutorService(other.getExecutorService());

        this.setExcludedExtensions(other.getExcludedExtensions());
        this.setRepositories(other.getRepositories());
        this.setBootRepositories(other.getBootRepositories());
        this.setApplicationArguments(other.getApplicationArguments());
        this.setCustomOptions(other.getCustomOptions());
        this.setExpireTime(other.getExpireTime());
        this.setErrors(other.getErrors() == null ? new ArrayList<>() : other.getErrors().stream().map(x -> NMsg.ofPlain(x)).collect(Collectors.toList()));
        this.setSkipErrors(other.getSkipErrors());
        this.setSwitchWorkspace(other.getSwitchWorkspace());
        this.setLocale(other.getLocale());
        this.setTheme(other.getTheme());
        this.setDependencySolver(other.getDependencySolver());
        this.setIsolationLevel(NIsolationLevel.parse(other.getIsolationLevel()).orNull());
        this.setInitLaunchers(other.getInitLaunchers());
        this.setInitJava(other.getInitJava());
        this.setInitScripts(other.getInitScripts());
        this.setInitPlatforms(other.getInitPlatforms());
        this.setDesktopLauncher(NSupportMode.parse(other.getDesktopLauncher()).orNull());
        this.setMenuLauncher(NSupportMode.parse(other.getMenuLauncher()).orNull());
        this.setUserLauncher(NSupportMode.parse(other.getUserLauncher()).orNull());
        this.setSharedInstance(other.getSharedInstance());
        this.setPreviewRepo(other.getPreviewRepo());
        this.setClassLoaderSupplier(other.getClassLoaderSupplier());
        this.setClassWorldLoader(other.getClassWorldLoader());

        this.setBootRepositories(other.getBootRepositories());
        this.setRuntimeBootDependencyNode(convertNode(other.getRuntimeBootDependencyNode()));
        this.setExtensionBootDescriptors(other.getExtensionBootDescriptors());
        this.setExtensionBootDependencyNodes(convertNodes(other.getExtensionBootDependencyNodes()));
        this.setBootWorkspaceFactory(other.getBootWorkspaceFactory());
        this.setClassWorldURLs(other.getClassWorldURLs());
        this.setClassWorldLoader(other.getClassWorldLoader());
        this.setUuid(other.getUuid());
        this.setExtensionsSet(other.getExtensionsSet());
        this.setRuntimeBootDescriptor(other.getRuntimeBootDescriptor());

        return this;
    }

    private List<NClassLoaderNode> convertNodes(List<NBootClassLoaderNode> dependencies) {
        return dependencies == null ? null : dependencies.stream().map(this::convertNode).collect(Collectors.toList());
    }

    private NClassLoaderNode convertNode(NBootClassLoaderNode n) {
        if (n == null) {
            return null;
        }
        List<NBootClassLoaderNode> dependencies = n.getDependencies();
        List<NClassLoaderNode> children = convertNodes(dependencies);
        return new NDefaultClassLoaderNode(
                NBlankable.isBlank(n.getId()) ? null : NId.get(n.getId()).get(),
                n.getURL(),
                n.isEnabled(),
                n.isIncludedInClasspath(),
                children == null ? null : children.toArray(new NClassLoaderNode[0])
        );
    }

    @Override
    public NBootOptionsBuilder setCmdLine(String cmdLine) {
        setCmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
        return this;
    }

    @Override
    public NBootOptionsBuilder setCmdLine(String[] args) {
        NWorkspaceOptionsBuilder b = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(args, b);
        copyFromIfPresent(b.build());
        return this;
    }

    public NOptional<Boolean> getSharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
    }

    @Override
    public NBootOptionsBuilder setSharedInstance(Boolean sharedInstance) {
        this.sharedInstance = sharedInstance;
        return this;
    }

    /**
     * set login
     *
     * @param username new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setUserName(String username) {
        this.userName = username;
        return this;
    }

    /**
     * set store location
     *
     * @param location location
     * @param value    new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setStoreLocation(NStoreType location, String value) {
        if (NBlankable.isBlank(value)) {
            if (storeLocations != null) {
                storeLocations.remove(location);
            }
        } else {
            if (storeLocations == null) {
                storeLocations = new HashMap<>();
            }
            storeLocations.put(location, value);
        }
        return this;
    }

    /**
     * set home location
     *
     * @param location location
     * @param value    new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder setHomeLocation(NHomeLocation location, String value) {
        if (NBlankable.isBlank(value)) {
            if (homeLocations != null) {
                homeLocations.remove(location);
            }
        } else {
            if (homeLocations == null) {
                homeLocations = new HashMap<>();
            }
            homeLocations.put(location, value);
        }
        return this;
    }

    /**
     * add output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder addOutputFormatOptions(String... options) {
        if (options != null) {
            for (String option : options) {
                if (option != null) {
                    option = NStringUtils.trim(option);
                    if (!option.isEmpty()) {
                        if (outputFormatOptions == null) {
                            outputFormatOptions = new ArrayList<>();
                        }
                        outputFormatOptions.add(option);
                    }
                }
            }
        }
        return this;
    }


    @Override
    public NOptional<String> getDependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public NBootOptionsBuilder setDependencySolver(String dependencySolver) {
        this.dependencySolver = dependencySolver;
        return this;
    }

    @Override
    public String toString() {
        return build().toCmdLine().toString();
    }

//    @Override
//    public NCmdLine toCmdLine() {
//        return build().toCmdLine();
//    }
//
//    @Override
//    public NCmdLine toCmdLine(NWorkspaceOptionsConfig config) {
//        return build().toCmdLine(config);
//    }

    @Override
    public NOptional<NIsolationLevel> getIsolationLevel() {
        return NOptional.ofNamed(isolationLevel, "isolationLevel");
    }

    @Override
    public NOptional<Boolean> getInitLaunchers() {
        return NOptional.ofNamed(initLaunchers, "initLaunchers");
    }

    @Override
    public NOptional<Boolean> getInitScripts() {
        return NOptional.ofNamed(initScripts, "initScripts");
    }

    @Override
    public NOptional<Boolean> getInitPlatforms() {
        return NOptional.ofNamed(initPlatforms, "initPlatforms");
    }

    @Override
    public NOptional<Boolean> getInitJava() {
        return NOptional.ofNamed(initJava, "initJava");
    }

    @Override
    public NBootOptionsBuilder unsetRuntimeOptions() {
        setCommandHelp(null);
        setCommandVersion(null);
        setOpenMode(null);
        setExecutionType(null);
        setRunAs(null);
        setReset(null);
        setRecover(null);
        setDry(null);
        setShowStacktrace(null);
        setExecutorOptions(null);
        setApplicationArguments(null);
        return this;
    }

    @Override
    public NBootOptionsBuilder unsetCreationOptions() {
        setName(null);
        setArchetype(null);
        setStoreLayout(null);
        setStoreStrategy(null);
        setRepositoryStoreStrategy(null);
        setStoreLocations(null);
        setHomeLocations(null);
        setSwitchWorkspace(null);
        return this;
    }

    @Override
    public NBootOptionsBuilder unsetExportedOptions() {
        setJavaCommand(null);
        setJavaOptions(null);
        setWorkspace(null);
        setUserName(null);
        setCredentials(null);
        setApiVersion(null);
        setRuntimeId(null);
        setTerminalMode(null);
        setLogConfig(null);
        setExcludedExtensions(null);
        setRepositories(null);
        setSystem(null);
        setGui(null);
        setReadOnly(null);
        setTrace(null);
        setProgressOptions(null);
        setDependencySolver(null);
        setDebug(null);
        setInstallCompanions(null);
        setSkipWelcome(null);
        setSkipBoot(null);
        setOutLinePrefix(null);
        setErrLinePrefix(null);
        setCached(null);
        setIndexed(null);
        setTransitive(null);
        setBot(null);
        setFetchStrategy(null);
        setConfirm(null);
        setOutputFormat(null);
        setOutputFormatOptions((List<String>) null);
        setExpireTime(null);
        setTheme(null);
        setLocale(null);
        setInitLaunchers(null);
        setInitPlatforms(null);
        setInitScripts(null);
        setInitJava(null);
        setDesktopLauncher(null);
        setMenuLauncher(null);
        setUserLauncher(null);
        return this;
    }

    @Override
    public NOptional<List<String>> getCustomOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NOptional<List<NArg>> getCustomOptionArgs() {
        return NOptional.ofNamed(customOptions == null ? null : customOptions.stream().map(x -> NArg.of(x)).collect(Collectors.toList()), "customOptions");
    }

    @Override
    public NOptional<NArg> getCustomOptionArg(String key) {
        return NOptional.ofNamedOptional(getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .findFirst(), key);
    }

    @Override
    public NOptional<String> getCustomOption(String key) {
        return NOptional.ofNamedOptional(getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .map(x->x.image())
                .findFirst(), key);
    }

    /// ///////////////////////
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
