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
@NScore(fixed = NScorable.DEFAULT_SCORE)
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
                outputFormatOptions().orNull(), customOptions().orNull(), apiVersion().orNull(), runtimeId().orNull(), javaCommand().orNull(),
                javaOptions().orNull(), workspace().orNull(), outLinePrefix().orNull(), errLinePrefix().orNull(),
                name().orNull(), installCompanions().orNull(), skipWelcome().orNull(), skipBoot().orNull(),
                system().orNull(), gui().orNull(), dry().orNull(), showStacktrace().orNull(), recover().orNull(), reset().orNull(), resetHard().orNull(), commandVersion().orNull(), commandHelp().orNull(), commandHelp().orNull(), switchWorkspace().orNull(), cached().orNull(), cached().orNull(), transitive().orNull(), bot().orNull(),
                isolationLevel().orNull(), initLaunchers().orNull(), initScripts().orNull(), initPlatforms().orNull(),
                initJava().orNull(), excludedExtensions().orNull(), repositories().orNull(), userName().orNull(),
                credential().orNull(), terminalMode().orNull(), readOnly().orNull(), trace().orNull(), progressOptions().orNull(),
                dependencySolver().orNull(), logConfig().orNull(), confirm().orNull(), outputFormat().orNull(),
                applicationArguments().orNull(), openMode().orNull(), creationTime().orNull(),
                classLoaderSupplier().orNull(), executorOptions().orNull(),
                debug().orNull(),
                executionType().orNull(), runAs().orNull(), archetype().orNull(),
                storeLocations().orNull(), homeLocations().orNull(), storeLayout().orNull(), storeStrategy().orNull(),
                repositoryStoreStrategy().orNull(), fetchStrategy().orNull(),
                stdin().orNull(), stdout().orNull(), stderr().orNull(),
                executorService().orNull(), expireTime().orNull(), errors().orNull(), skipErrors().orNull(), locale().orNull(),
                theme().orNull(), uuid().orNull(), bootRepositories().orNull(), runtimeBootDependencyNode().orNull(), extensionBootDescriptors().orNull(),
                extensionBootDependencyNodes().orNull(), classWorldURLs().orNull(), extensionsSet().orNull(), bootWorkspaceFactory().orNull(), runtimeBootDescriptor().orNull(), classWorldLoader().orNull(),
                desktopLauncher().orNull(), menuLauncher().orNull(), userLauncher().orNull(), previewRepo().orNull(), sharedInstance().orNull());
    }


    public NBootOptionsBuilder copyFrom(NWorkspaceOptions other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion().orNull());
        this.runtimeId(other.runtimeId().orNull());
        this.javaCommand(other.javaCommand().orNull());
        this.javaOptions(other.javaOptions().orNull());
        this.workspace(other.workspace().orNull());
        this.name(other.name().orNull());
        this.installCompanions(other.installCompanions().orNull());
        this.skipWelcome(other.skipWelcome().orNull());
        this.skipBoot(other.skipBoot().orNull());
        this.system(other.system().orNull());
        this.gui(other.gui().orNull());
        this.userName(other.userName().orNull());
        this.credential(other.credential().orNull());
        this.terminalMode(other.terminalMode().orNull());
        this.readOnly(other.readOnly().orNull());
        this.trace(other.trace().orNull());
        this.progressOptions(other.progressOptions().orNull());
        this.logConfig(other.logConfig().orNull());
        this.confirm(other.confirm().orNull());
        this.confirm(other.confirm().orNull());
        this.outputFormat(other.outputFormat().orNull());
        this.outputFormatOptions(other.outputFormatOptions().orNull());
        this.openMode(other.openMode().orNull());
        this.creationTime(other.creationTime().orNull());
        this.cry(other.dry().orNull());
        this.showStacktrace(other.showStacktrace().orNull());
        this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        this.executorOptions(other.executorOptions().orNull());
        this.recover(other.recover().orNull());
        this.reset(other.reset().orNull());
        this.resetHard(other.resetHard().orNull());
        this.commandVersion(other.commandVersion().orNull());
        this.commandHelp(other.commandHelp().orNull());
        this.debug(other.debug().orNull());
        this.inherited(other.inherited().orNull());
        this.executionType(other.executionType().orNull());
        this.runAs(other.runAs().orNull());
        this.archetype(other.archetype().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.homeLocations(other.homeLocations().orNull());
        this.storeLocations(other.storeLocations().orNull());
        this.storeLayout(other.storeLayout().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        this.fetchStrategy(other.fetchStrategy().orNull());
        this.cached(other.cached().orNull());
        this.indexed(other.indexed().orNull());
        this.transitive(other.transitive().orNull());
        this.bot(other.bot().orNull());
        this.stdin(other.stdin().orNull());
        this.stdout(other.stdout().orNull());
        this.stderr(other.stderr().orNull());
        this.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.repositories().orNull());
        this.applicationArguments(other.applicationArguments().orNull());
        this.customOptions(other.customOptions().orNull());
        this.expireTime(other.expireTime().orNull());
        this.errors(other.errors().orNull());
        this.skipErrors(other.skipErrors().orNull());
        this.switchWorkspace(other.switchWorkspace().orNull());
        this.locale(other.locale().orNull());
        this.theme(other.theme().orNull());
        this.dependencySolver(other.dependencySolver().orNull());
        this.isolationLevel(other.isolationLevel().orNull());
        this.initLaunchers(other.initLaunchers().orNull());
        this.initJava(other.initJava().orNull());
        this.initScripts(other.initScripts().orNull());
        this.initPlatforms(other.initPlatforms().orNull());
        this.desktopLauncher(other.desktopLauncher().orNull());
        this.menuLauncher(other.menuLauncher().orNull());
        this.userLauncher(other.userLauncher().orNull());
        this.sharedInstance(other.sharedInstance().orNull());
        this.previewRepo(other.previewRepo().orNull());
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFrom(NBootOptions other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion().orNull());
        this.runtimeId(other.runtimeId().orNull());
        this.javaCommand(other.javaCommand().orNull());
        this.javaOptions(other.javaOptions().orNull());
        this.workspace(other.workspace().orNull());
        this.name(other.name().orNull());
        this.installCompanions(other.installCompanions().orNull());
        this.skipWelcome(other.skipWelcome().orNull());
        this.skipBoot(other.skipBoot().orNull());
        this.system(other.system().orNull());
        this.gui(other.gui().orNull());
        this.userName(other.userName().orNull());
        this.credential(other.credential().orNull());
        this.terminalMode(other.terminalMode().orNull());
        this.readOnly(other.readOnly().orNull());
        this.trace(other.trace().orNull());
        this.progressOptions(other.progressOptions().orNull());
        this.logConfig(other.logConfig().orNull());
        this.confirm(other.confirm().orNull());
        this.confirm(other.confirm().orNull());
        this.outputFormat(other.outputFormat().orNull());
        this.outputFormatOptions(other.outputFormatOptions().orNull());
        this.openMode(other.openMode().orNull());
        this.creationTime(other.creationTime().orNull());
        this.cry(other.dry().orNull());
        this.showStacktrace(other.showStacktrace().orNull());
        this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        this.executorOptions(other.executorOptions().orNull());
        this.recover(other.recover().orNull());
        this.reset(other.reset().orNull());
        this.resetHard(other.resetHard().orNull());
        this.commandVersion(other.commandVersion().orNull());
        this.commandHelp(other.commandHelp().orNull());
        this.debug(other.debug().orNull());
        this.inherited(other.inherited().orNull());
        this.executionType(other.executionType().orNull());
        this.runAs(other.runAs().orNull());
        this.archetype(other.archetype().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.homeLocations(other.homeLocations().orNull());
        this.storeLocations(other.storeLocations().orNull());
        this.storeLayout(other.storeLayout().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        this.fetchStrategy(other.fetchStrategy().orNull());
        this.cached(other.cached().orNull());
        this.indexed(other.indexed().orNull());
        this.transitive(other.transitive().orNull());
        this.bot(other.bot().orNull());
        this.stdin(other.stdin().orNull());
        this.stdout(other.stdout().orNull());
        this.stderr(other.stderr().orNull());
        this.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.repositories().orNull());
        this.applicationArguments(other.applicationArguments().orNull());
        this.customOptions(other.customOptions().orNull());
        this.expireTime(other.expireTime().orNull());
        this.errors(other.errors().orNull());
        this.skipErrors(other.skipErrors().orNull());
        this.switchWorkspace(other.switchWorkspace().orNull());
        this.locale(other.locale().orNull());
        this.theme(other.theme().orNull());
        this.dependencySolver(other.dependencySolver().orNull());
        this.isolationLevel(other.isolationLevel().orNull());
        this.initLaunchers(other.initLaunchers().orNull());
        this.initJava(other.initJava().orNull());
        this.initScripts(other.initScripts().orNull());
        this.initPlatforms(other.initPlatforms().orNull());
        this.desktopLauncher(other.desktopLauncher().orNull());
        this.menuLauncher(other.menuLauncher().orNull());
        this.userLauncher(other.userLauncher().orNull());
        this.sharedInstance(other.sharedInstance().orNull());
        this.previewRepo(other.previewRepo().orNull());
        this.bootRepositories(other.bootRepositories().orNull());
        this.runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        this.extensionBootDescriptors(other.extensionBootDescriptors().orNull());
        this.extensionBootDependencyNodes(other.extensionBootDependencyNodes().orNull());
        this.bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        this.classWorldURLs(other.classWorldURLs().orNull());
        this.classWorldLoader(other.classWorldLoader().orNull());
        this.uuid(other.uuid().orNull());
        this.extensionsSet(other.extensionsSet().orNull());
        this.runtimeBootDescriptor(other.runtimeBootDescriptor().orNull());
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFrom(NBootOptionsBuilder other) {
        if (other == null) {
            return this;
        }
        this.apiVersion(other.apiVersion().orNull());
        this.runtimeId(other.runtimeId().orNull());
        this.javaCommand(other.javaCommand().orNull());
        this.javaOptions(other.javaOptions().orNull());
        this.workspace(other.workspace().orNull());
        this.name(other.name().orNull());
        this.installCompanions(other.installCompanions().orNull());
        this.skipWelcome(other.skipWelcome().orNull());
        this.skipBoot(other.skipBoot().orNull());
        this.system(other.system().orNull());
        this.gui(other.gui().orNull());
        this.userName(other.userName().orNull());
        this.credential(other.credential().orNull());
        this.terminalMode(other.terminalMode().orNull());
        this.readOnly(other.readOnly().orNull());
        this.trace(other.trace().orNull());
        this.progressOptions(other.progressOptions().orNull());
        this.logConfig(other.logConfig().orNull());
        this.confirm(other.confirm().orNull());
        this.confirm(other.confirm().orNull());
        this.outputFormat(other.outputFormat().orNull());
        this.outputFormatOptions(other.outputFormatOptions().orNull());
        this.openMode(other.openMode().orNull());
        this.creationTime(other.creationTime().orNull());
        this.cry(other.dry().orNull());
        this.showStacktrace(other.showStacktrace().orNull());
        this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        this.executorOptions(other.executorOptions().orNull());
        this.recover(other.recover().orNull());
        this.reset(other.reset().orNull());
        this.resetHard(other.resetHard().orNull());
        this.commandVersion(other.commandVersion().orNull());
        this.commandHelp(other.commandHelp().orNull());
        this.debug(other.debug().orNull());
        this.inherited(other.inherited().orNull());
        this.executionType(other.executionType().orNull());
        this.runAs(other.runAs().orNull());
        this.archetype(other.archetype().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.homeLocations(other.homeLocations().orNull());
        this.storeLocations(other.storeLocations().orNull());
        this.storeLayout(other.storeLayout().orNull());
        this.storeStrategy(other.storeStrategy().orNull());
        this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        this.fetchStrategy(other.fetchStrategy().orNull());
        this.cached(other.cached().orNull());
        this.indexed(other.indexed().orNull());
        this.transitive(other.transitive().orNull());
        this.bot(other.bot().orNull());
        this.stdin(other.stdin().orNull());
        this.stdout(other.stdout().orNull());
        this.stderr(other.stderr().orNull());
        this.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        this.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        this.repositories(other.repositories().orNull());
        this.bootRepositories(other.bootRepositories().orNull());
        this.applicationArguments(other.applicationArguments().orNull());
        this.customOptions(other.customOptions().orNull());
        this.expireTime(other.expireTime().orNull());
        this.errors(other.errors().orNull());
        this.skipErrors(other.skipErrors().orNull());
        this.switchWorkspace(other.switchWorkspace().orNull());
        this.locale(other.locale().orNull());
        this.theme(other.theme().orNull());
        this.dependencySolver(other.dependencySolver().orNull());
        this.isolationLevel(other.isolationLevel().orNull());
        this.initLaunchers(other.initLaunchers().orNull());
        this.initJava(other.initJava().orNull());
        this.initScripts(other.initScripts().orNull());
        this.initPlatforms(other.initPlatforms().orNull());
        this.desktopLauncher(other.desktopLauncher().orNull());
        this.menuLauncher(other.menuLauncher().orNull());
        this.userLauncher(other.userLauncher().orNull());
        this.sharedInstance(other.sharedInstance().orNull());
        this.previewRepo(other.previewRepo().orNull());
//        this.setBootRepositories(other.getBootRepositories().orNull());
        this.runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        this.extensionBootDescriptors(other.extensionBootDescriptors().orNull());
        this.extensionBootDependencyNodes(other.extensionBootDependencyNodes().orNull());
        this.bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        this.classWorldURLs(other.classWorldURLs().orNull());
        this.classWorldLoader(other.classWorldLoader().orNull());
        this.uuid(other.uuid().orNull());
        this.extensionsSet(other.extensionsSet().orNull());
        this.runtimeBootDescriptor(other.runtimeBootDescriptor().orNull());
        return this;
    }

//    public NOptional<String> getBootRepositories() {
//        return NOptional.of(bootRepositories);
//    }
//
//    @Override
//    public DefaultNBootOptionsBuilder setBootRepositories(String bootRepositories) {
//        this.bootRepositories = NStringUtils.stripToNull(bootRepositories);
//        return this;
//    }

    public NOptional<NClassLoaderNode> runtimeBootDependencyNode() {
        return NOptional.of(runtimeBootDependencyNode);
    }

    @Override
    public DefaultNBootOptionsBuilder runtimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public NOptional<List<NBootDescriptor>> extensionBootDescriptors() {
        return NOptional.of(extensionBootDescriptors);
    }

    @Override
    public DefaultNBootOptionsBuilder extensionBootDescriptors(List<NBootDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = NReservedLangUtils.nonNullList(extensionBootDescriptors);
        return this;
    }

    public NOptional<List<NClassLoaderNode>> extensionBootDependencyNodes() {
        return NOptional.of(extensionBootDependencyNodes);
    }

    @Override
    public DefaultNBootOptionsBuilder extensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = NReservedLangUtils.nonNullList(extensionBootDependencyNodes);
        return this;
    }

    public NOptional<NBootWorkspaceFactory> bootWorkspaceFactory() {
        return NOptional.of(bootWorkspaceFactory);
    }

    @Override
    public DefaultNBootOptionsBuilder bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public NOptional<List<URL>> classWorldURLs() {
        return NOptional.of(classWorldURLs);
    }

    @Override
    public DefaultNBootOptionsBuilder classWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = NReservedLangUtils.nonNullList(classWorldURLs);
        return this;
    }

    public NOptional<ClassLoader> classWorldLoader() {
        return NOptional.of(classWorldLoader);
    }

    @Override
    public DefaultNBootOptionsBuilder classWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public NOptional<String> uuid() {
        return NOptional.of(uuid);
    }

    @Override
    public DefaultNBootOptionsBuilder uuid(String uuid) {
        this.uuid = NStringUtils.stripToNull(uuid);
        return this;
    }

    public NOptional<Set<String>> extensionsSet() {
        return NOptional.of(extensionsSet);
    }

    @Override
    public DefaultNBootOptionsBuilder extensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = NReservedLangUtils.nonNullSet(extensionsSet);
        return this;
    }

    public NOptional<NBootDescriptor> runtimeBootDescriptor() {
        return NOptional.of(runtimeBootDescriptor);
    }

    @Override
    public DefaultNBootOptionsBuilder runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }


    @Override
    public NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other) {
        if (other.apiVersion().isPresent()) {
            this.apiVersion(other.apiVersion().orNull());
        }
        if (other.runtimeId().isPresent()) {
            this.runtimeId(other.runtimeId().orNull());
        }
        if (other.javaCommand().isPresent()) {
            this.javaCommand(other.javaCommand().orNull());
        }
        if (other.javaOptions().isPresent()) {
            this.javaOptions(other.javaOptions().orNull());
        }
        if (other.workspace().isPresent()) {
            this.workspace(other.workspace().orNull());
        }
        if (other.name().isPresent()) {
            this.name(other.name().orNull());
        }
        if (other.installCompanions().isPresent()) {
            this.installCompanions(other.installCompanions().orNull());
        }
        if (other.skipWelcome().isPresent()) {
            this.skipWelcome(other.skipWelcome().orNull());
        }
        if (other.skipBoot().isPresent()) {
            this.skipBoot(other.skipBoot().orNull());
        }
        if (other.system().isPresent()) {
            this.system(other.system().orNull());
        }
        if (other.gui().isPresent()) {
            this.gui(other.gui().orNull());
        }
        if (other.userName().isPresent()) {
            this.userName(other.userName().orNull());
        }
        if (other.credential().isPresent()) {
            this.credential(other.credential().orNull());
        }
        if (other.terminalMode().isPresent()) {
            this.terminalMode(other.terminalMode().orNull());
        }
        if (other.readOnly().isPresent()) {
            this.readOnly(other.readOnly().orNull());
        }
        if (other.trace().isPresent()) {
            this.trace(other.trace().orNull());
        }
        if (other.progressOptions().isPresent()) {
            this.progressOptions(other.progressOptions().orNull());
        }
        if (other.logConfig().isPresent()) {
            this.logConfig(other.logConfig().orNull());
        }
        if (other.confirm().isPresent()) {
            this.confirm(other.confirm().orNull());
        }
        if (other.confirm().isPresent()) {
            this.confirm(other.confirm().orNull());
        }
        if (other.outputFormat().isPresent()) {
            this.outputFormat(other.outputFormat().orNull());
        }
        if (other.outputFormatOptions().isPresent()) {
            this.outputFormatOptions(other.outputFormatOptions().orNull());
        }
        if (other.openMode().isPresent()) {
            this.openMode(other.openMode().orNull());
        }
        if (other.creationTime().isPresent()) {
            this.creationTime(other.creationTime().orNull());
        }
        if (other.dry().isPresent()) {
            this.cry(other.dry().orNull());
        }
        if (other.showStacktrace().isPresent()) {
            this.showStacktrace(other.showStacktrace().orNull());
        }
        if (other.classLoaderSupplier().isPresent()) {
            this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        }
        if (other.executorOptions().isPresent()) {
            this.executorOptions(other.executorOptions().orNull());
        }
        if (other.recover().isPresent()) {
            this.recover(other.recover().orNull());
        }
        if (other.reset().isPresent()) {
            this.reset(other.reset().orNull());
        }
        if (other.resetHard().isPresent()) {
            this.resetHard(other.resetHard().orNull());
        }
        if (other.commandVersion().isPresent()) {
            this.commandVersion(other.commandVersion().orNull());
        }
        if (other.commandHelp().isPresent()) {
            this.commandHelp(other.commandHelp().orNull());
        }
        if (other.debug().isPresent()) {
            this.debug(other.debug().orNull());
        }
        if (other.inherited().isPresent()) {
            this.inherited(other.inherited().orNull());
        }
        if (other.executionType().isPresent()) {
            this.executionType(other.executionType().orNull());
        }
        if (other.runAs().isPresent()) {
            this.runAs(other.runAs().orNull());
        }
        if (other.archetype().isPresent()) {
            this.archetype(other.archetype().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            this.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.homeLocations().isPresent()) {
            this.homeLocations(other.homeLocations().orNull());
        }

        if (other.storeLocations().isPresent()) {
            this.storeLocations(other.storeLocations().orNull());
        }
        if (other.storeLayout().isPresent()) {
            this.storeLayout(other.storeLayout().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            this.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.repositoryStoreStrategy().isPresent()) {
            this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        }
        if (other.fetchStrategy().isPresent()) {
            this.fetchStrategy(other.fetchStrategy().orNull());
        }
        if (other.cached().isPresent()) {
            this.cached(other.cached().orNull());
        }
        if (other.indexed().isPresent()) {
            this.indexed(other.indexed().orNull());
        }
        if (other.transitive().isPresent()) {
            this.transitive(other.transitive().orNull());
        }
        if (other.bot().isPresent()) {
            this.bot(other.bot().orNull());
        }
        if (other.stdin().isPresent()) {
            this.stdin(other.stdin().orNull());
        }
        if (other.stdout().isPresent()) {
            this.stdout(other.stdout().orNull());
        }
        if (other.stderr().isPresent()) {
            this.stderr(other.stderr().orNull());
        }
        if (other.executorService().isPresent()) {
            this.executorService(other.executorService().orNull());
        }
        if (other.excludedExtensions().isPresent()) {
            this.excludedExtensions(other.excludedExtensions().orNull());
        }
        if (other.repositories().isPresent()) {
            this.repositories(other.repositories().orNull());
        }
        if (other.applicationArguments().isPresent()) {
            this.applicationArguments(other.applicationArguments().orNull());
        }
        if (other.customOptions().isPresent()) {
            this.customOptions(other.customOptions().orNull());
        }
        if (other.expireTime().isPresent()) {
            this.expireTime(other.expireTime().orNull());
        }
        if (other.errors().isPresent()) {
            this.errors(other.errors().orNull());
        }
        if (other.skipErrors().isPresent()) {
            this.skipErrors(other.skipErrors().orNull());
        }
        if (other.switchWorkspace().isPresent()) {
            this.switchWorkspace(other.switchWorkspace().orNull());
        }
        if (other.locale().isPresent()) {
            this.locale(other.locale().orNull());
        }
        if (other.theme().isPresent()) {
            this.theme(other.theme().orNull());
        }
        if (other.dependencySolver().isPresent()) {
            this.dependencySolver(other.dependencySolver().orNull());
        }
        if (other.isolationLevel().isPresent()) {
            this.isolationLevel(other.isolationLevel().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            this.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.initJava().isPresent()) {
            this.initJava(other.initJava().orNull());
        }
        if (other.initScripts().isPresent()) {
            this.initScripts(other.initScripts().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            this.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.desktopLauncher().isPresent()) {
            this.desktopLauncher(other.desktopLauncher().orNull());
        }
        if (other.menuLauncher().isPresent()) {
            this.menuLauncher(other.menuLauncher().orNull());
        }
        if (other.userLauncher().isPresent()) {
            this.userLauncher(other.userLauncher().orNull());
        }
        if (other.previewRepo().isPresent()) {
            this.previewRepo(other.previewRepo().orNull());
        }
        if (other.sharedInstance().isPresent()) {
            this.sharedInstance(other.sharedInstance().orNull());
        }
        return this;
    }

    @Override
    public NBootOptionsBuilder copyFromIfPresent(NBootOptions other) {
        if (other.apiVersion().isPresent()) {
            this.apiVersion(other.apiVersion().orNull());
        }
        if (other.runtimeId().isPresent()) {
            this.runtimeId(other.runtimeId().orNull());
        }
        if (other.javaCommand().isPresent()) {
            this.javaCommand(other.javaCommand().orNull());
        }
        if (other.javaOptions().isPresent()) {
            this.javaOptions(other.javaOptions().orNull());
        }
        if (other.workspace().isPresent()) {
            this.workspace(other.workspace().orNull());
        }
        if (other.name().isPresent()) {
            this.name(other.name().orNull());
        }
        if (other.installCompanions().isPresent()) {
            this.installCompanions(other.installCompanions().orNull());
        }
        if (other.skipWelcome().isPresent()) {
            this.skipWelcome(other.skipWelcome().orNull());
        }
        if (other.skipBoot().isPresent()) {
            this.skipBoot(other.skipBoot().orNull());
        }
        if (other.system().isPresent()) {
            this.system(other.system().orNull());
        }
        if (other.gui().isPresent()) {
            this.gui(other.gui().orNull());
        }
        if (other.userName().isPresent()) {
            this.userName(other.userName().orNull());
        }
        if (other.credential().isPresent()) {
            this.credential(other.credential().orNull());
        }
        if (other.terminalMode().isPresent()) {
            this.terminalMode(other.terminalMode().orNull());
        }
        if (other.readOnly().isPresent()) {
            this.readOnly(other.readOnly().orNull());
        }
        if (other.trace().isPresent()) {
            this.trace(other.trace().orNull());
        }
        if (other.progressOptions().isPresent()) {
            this.progressOptions(other.progressOptions().orNull());
        }
        if (other.logConfig().isPresent()) {
            this.logConfig(other.logConfig().orNull());
        }
        if (other.confirm().isPresent()) {
            this.confirm(other.confirm().orNull());
        }
        if (other.confirm().isPresent()) {
            this.confirm(other.confirm().orNull());
        }
        if (other.outputFormat().isPresent()) {
            this.outputFormat(other.outputFormat().orNull());
        }
        if (other.outputFormatOptions().isPresent()) {
            this.outputFormatOptions(other.outputFormatOptions().orNull());
        }
        if (other.openMode().isPresent()) {
            this.openMode(other.openMode().orNull());
        }
        if (other.creationTime().isPresent()) {
            this.creationTime(other.creationTime().orNull());
        }
        if (other.dry().isPresent()) {
            this.cry(other.dry().orNull());
        }
        if (other.showStacktrace().isPresent()) {
            this.showStacktrace(other.showStacktrace().orNull());
        }
        if (other.classLoaderSupplier().isPresent()) {
            this.classLoaderSupplier(other.classLoaderSupplier().orNull());
        }
        if (other.executorOptions().isPresent()) {
            this.executorOptions(other.executorOptions().orNull());
        }
        if (other.recover().isPresent()) {
            this.recover(other.recover().orNull());
        }
        if (other.reset().isPresent()) {
            this.reset(other.reset().orNull());
        }
        if (other.resetHard().isPresent()) {
            this.resetHard(other.resetHard().orNull());
        }
        if (other.commandVersion().isPresent()) {
            this.commandVersion(other.commandVersion().orNull());
        }
        if (other.commandHelp().isPresent()) {
            this.commandHelp(other.commandHelp().orNull());
        }
        if (other.debug().isPresent()) {
            this.debug(other.debug().orNull());
        }
        if (other.inherited().isPresent()) {
            this.inherited(other.inherited().orNull());
        }
        if (other.executionType().isPresent()) {
            this.executionType(other.executionType().orNull());
        }
        if (other.runAs().isPresent()) {
            this.runAs(other.runAs().orNull());
        }
        if (other.archetype().isPresent()) {
            this.archetype(other.archetype().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            this.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.homeLocations().isPresent()) {
            this.homeLocations(other.homeLocations().orNull());
        }

        if (other.storeLocations().isPresent()) {
            this.storeLocations(other.storeLocations().orNull());
        }
        if (other.storeLayout().isPresent()) {
            this.storeLayout(other.storeLayout().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            this.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.repositoryStoreStrategy().isPresent()) {
            this.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        }
        if (other.fetchStrategy().isPresent()) {
            this.fetchStrategy(other.fetchStrategy().orNull());
        }
        if (other.cached().isPresent()) {
            this.cached(other.cached().orNull());
        }
        if (other.indexed().isPresent()) {
            this.indexed(other.indexed().orNull());
        }
        if (other.transitive().isPresent()) {
            this.transitive(other.transitive().orNull());
        }
        if (other.bot().isPresent()) {
            this.bot(other.bot().orNull());
        }
        if (other.stdin().isPresent()) {
            this.stdin(other.stdin().orNull());
        }
        if (other.stdout().isPresent()) {
            this.stdout(other.stdout().orNull());
        }
        if (other.stderr().isPresent()) {
            this.stderr(other.stderr().orNull());
        }
        if (other.executorService().isPresent()) {
            this.executorService(other.executorService().orNull());
        }
        if (other.excludedExtensions().isPresent()) {
            this.excludedExtensions(other.excludedExtensions().orNull());
        }
        if (other.repositories().isPresent()) {
            this.repositories(other.repositories().orNull());
        }
        if (other.applicationArguments().isPresent()) {
            this.applicationArguments(other.applicationArguments().orNull());
        }
        if (other.customOptions().isPresent()) {
            this.customOptions(other.customOptions().orNull());
        }
        if (other.expireTime().isPresent()) {
            this.expireTime(other.expireTime().orNull());
        }
        if (other.errors().isPresent()) {
            this.errors(other.errors().orNull());
        }
        if (other.skipErrors().isPresent()) {
            this.skipErrors(other.skipErrors().orNull());
        }
        if (other.switchWorkspace().isPresent()) {
            this.switchWorkspace(other.switchWorkspace().orNull());
        }
        if (other.locale().isPresent()) {
            this.locale(other.locale().orNull());
        }
        if (other.theme().isPresent()) {
            this.theme(other.theme().orNull());
        }
        if (other.dependencySolver().isPresent()) {
            this.dependencySolver(other.dependencySolver().orNull());
        }
        if (other.isolationLevel().isPresent()) {
            this.isolationLevel(other.isolationLevel().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            this.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.initJava().isPresent()) {
            this.initJava(other.initJava().orNull());
        }
        if (other.initScripts().isPresent()) {
            this.initScripts(other.initScripts().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            this.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.desktopLauncher().isPresent()) {
            this.desktopLauncher(other.desktopLauncher().orNull());
        }
        if (other.menuLauncher().isPresent()) {
            this.menuLauncher(other.menuLauncher().orNull());
        }
        if (other.userLauncher().isPresent()) {
            this.userLauncher(other.userLauncher().orNull());
        }
        if (other.previewRepo().isPresent()) {
            this.previewRepo(other.previewRepo().orNull());
        }
        if (other.sharedInstance().isPresent()) {
            this.sharedInstance(other.sharedInstance().orNull());
        }
        if (other.bootRepositories().isPresent()) {
            bootRepositories(other.bootRepositories().orNull());
        }
        if (other.runtimeBootDependencyNode().isPresent()) {
            runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        }
        if (other.extensionBootDescriptors().isPresent()) {
            extensionBootDescriptors(other.extensionBootDescriptors().orNull());
        }
        if (other.extensionBootDependencyNodes().isPresent()) {
            extensionBootDependencyNodes(other.extensionBootDependencyNodes().orNull());
        }
        if (other.bootWorkspaceFactory().isPresent()) {
            bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        }
        if (other.classWorldURLs().isPresent()) {
            classWorldURLs(other.classWorldURLs().orNull());
        }
        if (other.classWorldLoader().isPresent()) {
            classWorldLoader(other.classWorldLoader().orNull());
        }
        if (other.uuid().isPresent()) {
            uuid(other.uuid().orNull());
        }
        return this;
    }

    public NBootOptionsInfo toBootOptions() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.setApiVersion(this.apiVersion().map(x -> x.toString()).orNull());
        r.setRuntimeId(this.runtimeId().map(x -> x.toString()).orNull());
        r.setJavaCommand(this.javaCommand().orNull());
        r.setJavaOptions(this.javaOptions().orNull());
        r.setWorkspace(this.workspace().orNull());
        r.setName(this.name().orNull());
        r.setInstallCompanions(this.installCompanions().orNull());
        r.setSkipWelcome(this.skipWelcome().orNull());
        r.setSkipBoot(this.skipBoot().orNull());
        r.setSystem(this.system().orNull());
        r.setGui(this.gui().orNull());
        r.setUserName(this.userName().orNull());
        r.setCredential(this.credential().orNull());
        r.setTerminalMode(this.terminalMode().map(x -> x.id()).orNull());
        r.setReadOnly(this.readOnly().orNull());
        r.setTrace(this.trace().orNull());
        r.setProgressOptions(this.progressOptions().orNull());
        {
            NLogConfig c = this.logConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.setLogFileBase(c.logFileBase());
                v.setLogFileLevel(c.logFileLevel());
                v.setLogTermLevel(c.logTermLevel());
                v.setLogFileSize(c.logFileSize());
                v.setLogFileCount(c.logFileCount());
                v.setLogFileName(c.logFileName());
                v.setLogFileBase(c.logFileBase());
            }
            r.setLogConfig(v);
        }
        r.setConfirm(this.confirm().map(x -> x.id()).orNull());
        r.setConfirm(this.confirm().map(x -> x.id()).orNull());
        r.setOutputFormat(this.outputFormat().map(x -> x.id()).orNull());
        r.setOutputFormatOptions(this.outputFormatOptions().orNull());
        r.setOpenMode(this.openMode().map(x -> x.id()).orNull());
        r.setCreationTime(this.creationTime().orNull());
        r.setDry(this.dry().orNull());
        r.setShowStacktrace(this.showStacktrace().orNull());
        r.setClassLoaderSupplier(this.classLoaderSupplier().orNull());
        r.setExecutorOptions(this.executorOptions().orNull());
        r.setRecover(this.recover().orNull());
        r.setReset(this.reset().orNull());
        r.setResetHard(this.resetHard().orNull());
        r.setCommandVersion(this.commandVersion().orNull());
        r.setCommandHelp(this.commandHelp().orNull());
        r.setDebug(this.debug().orNull());
        r.setInherited(this.inherited().orNull());
        r.setExecutionType(this.executionType().map(x -> x.id()).orNull());
        r.setRunAs(this.runAs().map(x -> x.toString()).orNull());
        r.setArchetype(this.archetype().orNull());
        r.setStoreStrategy(this.storeStrategy().map(x -> x.id()).orNull());
        {
            Map<NHomeLocation, String> c = this.homeLocations().orNull();
            Map<NBootHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NHomeLocation, String> e : c.entrySet()) {
                    v.put(NBootHomeLocation.of(
                            e.getKey().osFamily().id(),
                            e.getKey().storeType().id()
                    ), e.getValue());
                }
            }
            r.setHomeLocations(v);
        }
        {
            Map<NStoreType, String> c = this.storeLocations().orNull();
            Map<String, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NStoreType, String> e : c.entrySet()) {
                    v.put(e.getKey().id(), e.getValue());
                }
            }
            r.setStoreLocations(v);
        }
        r.setStoreLayout(this.storeLayout().map(x -> x.toString()).orNull());
        r.setStoreStrategy(this.storeStrategy().map(x -> x.toString()).orNull());
        r.setRepositoryStoreStrategy(this.repositoryStoreStrategy().map(x -> x.toString()).orNull());
        r.setFetchStrategy(this.fetchStrategy().map(x -> x.toString()).orNull());
        r.setCached(this.cached().orNull());
        r.setIndexed(this.indexed().orNull());
        r.setTransitive(this.transitive().orNull());
        r.setBot(this.bot().orNull());
        r.setStdin(this.stdin().orNull());
        r.setStdout(this.stdout().orNull());
        r.setStderr(this.stderr().orNull());
        r.setExecutorService(this.executorService().orNull());

        r.setExcludedExtensions(this.excludedExtensions().orNull());
        r.setRepositories(this.repositories().orNull());
        r.setApplicationArguments(this.applicationArguments().orNull());
        r.setCustomOptions(this.customOptions().orNull());
        r.setExpireTime(this.expireTime().orNull());
        r.setErrors(this.errors().isNotPresent() ? new ArrayList<>() : this.errors().get().stream().map(x -> x.toString()).collect(Collectors.toList()));
        r.setSkipErrors(this.skipErrors().orNull());
        r.setSwitchWorkspace(this.switchWorkspace().orNull());
        r.setLocale(this.locale().orNull());
        r.setTheme(this.theme().orNull());
        r.setDependencySolver(this.dependencySolver().orNull());
        r.setIsolationLevel(this.isolationLevel().map(x -> x.id()).orNull());
        r.setInitLaunchers(this.initLaunchers().orNull());
        r.setInitJava(this.initJava().orNull());
        r.setInitScripts(this.initScripts().orNull());
        r.setInitPlatforms(this.initPlatforms().orNull());
        r.setDesktopLauncher(this.desktopLauncher().map(x -> x.id()).orNull());
        r.setMenuLauncher(this.menuLauncher().map(x -> x.id()).orNull());
        r.setUserLauncher(this.userLauncher().map(x -> x.id()).orNull());
        r.setSharedInstance(this.sharedInstance().orNull());
        r.setPreviewRepo(this.previewRepo().orNull());
        return r;
    }

    /// ///////////////////////


    @Override
    public NOptional<NSupportMode> desktopLauncher() {
        return NOptional.ofNamed(desktopLauncher, "desktopLauncher");
    }

    @Override
    public NOptional<NSupportMode> menuLauncher() {
        return NOptional.ofNamed(menuLauncher, "menuLauncher");
    }

    @Override
    public NOptional<NSupportMode> userLauncher() {
        return NOptional.ofNamed(userLauncher, "userLauncher");
    }

    @Override
    public NBootOptionsBuilder initLaunchers(Boolean initLaunchers) {
        this.initLaunchers = initLaunchers;
        return this;
    }

    @Override
    public NBootOptionsBuilder initScripts(Boolean initScripts) {
        this.initScripts = initScripts;
        return this;
    }

    @Override
    public NBootOptionsBuilder initPlatforms(Boolean initPlatforms) {
        this.initPlatforms = initPlatforms;
        return this;
    }

    @Override
    public NBootOptionsBuilder initJava(Boolean initJava) {
        this.initJava = initJava;
        return this;
    }

    @Override
    public NBootOptionsBuilder isolationLevel(NIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
        return this;
    }

    @Override
    public NBootOptionsBuilder desktopLauncher(NSupportMode desktopLauncher) {
        this.desktopLauncher = desktopLauncher;
        return this;
    }

    @Override
    public NBootOptionsBuilder menuLauncher(NSupportMode menuLauncher) {
        this.menuLauncher = menuLauncher;
        return this;
    }

    @Override
    public NBootOptionsBuilder userLauncher(NSupportMode userLauncher) {
        this.userLauncher = userLauncher;
        return this;
    }


    @Override
    public NOptional<NVersion> apiVersion() {
        return NOptional.ofNamed(apiVersion, "apiVersion");
    }

    /**
     * set apiVersion
     *
     * @param apiVersion new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder apiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Override
    public NOptional<List<String>> applicationArguments() {
        return NOptional.ofNamed(applicationArguments, "applicationArguments");
    }

    /**
     * set applicationArguments
     *
     * @param applicationArguments new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder applicationArguments(List<String> applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    @Override
    public NOptional<String> archetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    /**
     * set archetype
     *
     * @param archetype new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder archetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return NOptional.ofNamed(classLoaderSupplier, "classLoaderSupplier");
    }

    /**
     * set provider
     *
     * @param provider new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider) {
        this.classLoaderSupplier = provider;
        return this;
    }

    @Override
    public NOptional<NConfirmationMode> confirm() {
        return NOptional.ofNamed(confirm, "confirm");
    }

    /**
     * set confirm
     *
     * @param confirm new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder confirm(NConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NOptional<Boolean> dry() {
        return NOptional.ofNamed(dry, "dry");
    }

    @Override
    public NOptional<Boolean> showStacktrace() {
        return NOptional.ofNamed(showStacktrace, "showStacktrace");
    }

    /**
     * set dry
     *
     * @param dry new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder cry(Boolean dry) {
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
    public NBootOptionsBuilder showStacktrace(Boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    @Override
    public NOptional<Instant> creationTime() {
        return NOptional.ofNamed(creationTime, "creationTime");
    }

    /**
     * set creationTime
     *
     * @param creationTime new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder creationTime(Instant creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public NOptional<List<String>> excludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    /**
     * set excludedExtensions
     *
     * @param excludedExtensions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder excludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    @Override
    public NOptional<NExecutionType> executionType() {
        return NOptional.ofNamed(executionType, "executionType");
    }

    /**
     * set executionType
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder executionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NOptional<NRunAs> runAs() {
        return NOptional.ofNamed(runAs, "runAs");
    }

    /**
     * set runAsUser
     *
     * @param runAs new value
     * @return {@code this} instance
     */
    public NBootOptionsBuilder runAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }

    @Override
    public NOptional<List<String>> executorOptions() {
        return NOptional.ofNamed(executorOptions, "executorOptions");
    }

    /**
     * set executorOptions
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder executorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    @Override
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(homeLocations == null ? null : homeLocations.get(location), "homeLocations[" + location + "]");
    }

    @Override
    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    @Override
    public NBootOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations) {
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
    public NOptional<String> javaCommand() {
        return NOptional.ofNamed(javaCommand, "javaCommand");
    }

    @Override
    public NBootOptionsBuilder javaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    @Override
    public NOptional<String> javaOptions() {
        return NOptional.ofNamed(javaOptions, "javaOptions");
    }

    /**
     * set javaOptions
     *
     * @param javaOptions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder javaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    @Override
    public NOptional<NLogConfig> logConfig() {
        return NOptional.ofNamed(logConfig, "logConfig");
    }

    /**
     * set logConfig
     *
     * @param logConfig new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder logConfig(NLogConfig logConfig) {
        this.logConfig = logConfig == null ? null : logConfig.copy();
        return this;
    }

    @Override
    public NOptional<String> name() {
        return NOptional.ofNamed(name, "name");
    }

    /**
     * set workspace name
     *
     * @param workspaceName new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder name(String workspaceName) {
        this.name = workspaceName;
        return this;
    }

    @Override
    public NOptional<NOpenMode> openMode() {
        return NOptional.ofNamed(openMode, "openMode");
    }

    /**
     * set openMode
     *
     * @param openMode new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder openMode(NOpenMode openMode) {
        this.openMode = openMode;
        return this;
    }

    @Override
    public NOptional<NContentType> outputFormat() {
        return NOptional.ofNamed(outputFormat, "outputFormat");
    }

    /**
     * set outputFormat
     *
     * @param outputFormat new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder outputFormat(NContentType outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public NOptional<List<String>> outputFormatOptions() {
        return NOptional.ofNamed(outputFormatOptions, "outputFormatOptions");
    }

    /**
     * set output format options
     *
     * @param options new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder outputFormatOptions(List<String> options) {
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
    public NOptional<char[]> credential() {
        return NOptional.ofNamed(credentials, "credentials");
    }

    /**
     * set password
     *
     * @param credentials new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder credential(char[] credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    /**
     * set repositoryStoreStrategy
     *
     * @param repositoryStoreStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return this;
    }

    @Override
    public NOptional<NId> runtimeId() {
        return NOptional.ofNamed(runtimeId, "runtimeId");
    }

    /**
     * set runtimeId
     *
     * @param runtimeId new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder runtimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    @Override
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(storeLocations == null ? null : storeLocations.get(folder), "storeLocations[" + folder + "]");
    }

    @Override
    public NOptional<NOsFamily> storeLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    /**
     * set storeLayout
     *
     * @param storeLayout new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder storeLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return this;
    }

    @Override
    public NOptional<NStoreStrategy> storeStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    /**
     * set storeStrategy
     *
     * @param storeStrategy new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder storeStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    @Override
    public NOptional<Map<NStoreType, String>> storeLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    @Override
    public NBootOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations) {
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
    public NOptional<NTerminalMode> terminalMode() {
        return NOptional.ofNamed(terminalMode, "terminalMode");
    }

    /**
     * set terminalMode
     *
     * @param terminalMode new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder terminalMode(NTerminalMode terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    @Override
    public NOptional<List<String>> repositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }
    @Override
    public NOptional<List<String>> bootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    /**
     * set repositories
     *
     * @param repositories new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder repositories(List<String> repositories) {
        this.repositories = repositories;
        return this;
    }

    @Override
    public NBootOptionsBuilder bootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }



    @Override
    public NOptional<String> userName() {
        return NOptional.ofNamed(userName, "userName");
    }

    @Override
    public NOptional<String> workspace() {
        return NOptional.ofNamed(workspace, "workspace");
    }

    /**
     * set workspace
     *
     * @param workspace workspace
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NOptional<String> debug() {
        return NOptional.ofNamed(debug, "debug");
    }

    /**
     * set debug
     *
     * @param debug new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder debug(String debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public NOptional<Boolean> system() {
        return NOptional.ofNamed(system, "system");
    }

    /**
     * set system
     *
     * @param system new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder system(Boolean system) {
        this.system = system;
        return this;
    }

    @Override
    public NOptional<Boolean> gui() {
        return NOptional.ofNamed(gui, "gui");
    }

    /**
     * set gui
     *
     * @param gui new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder gui(Boolean gui) {
        this.gui = gui;
        return this;
    }

    @Override
    public NOptional<Boolean> inherited() {
        return NOptional.ofNamed(inherited, "inherited");
    }

    /**
     * set inherited
     *
     * @param inherited new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder inherited(Boolean inherited) {
        this.inherited = inherited;
        return this;
    }

    @Override
    public NOptional<Boolean> readOnly() {
        return NOptional.ofNamed(readOnly, "readOnly");
    }

    /**
     * set readOnly
     *
     * @param readOnly new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public NOptional<Boolean> recover() {
        return NOptional.ofNamed(recover, "recover");
    }

    /**
     * set recover
     *
     * @param recover new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder recover(Boolean recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public NOptional<Boolean> reset() {
        return NOptional.ofNamed(reset, "reset");
    }

    /**
     * set reset
     *
     * @param reset new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder reset(Boolean reset) {
        this.reset = reset;
        return this;
    }

    @Override
    public NOptional<Boolean> resetHard() {
        return NOptional.ofNamed(resetHard, "resetHard");
    }



    /**
     * set reset hard
     *
     * @param resetHard new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder resetHard(Boolean resetHard) {
        this.resetHard = resetHard;
        return this;
    }

    @Override
    public NOptional<Boolean> commandVersion() {
        return NOptional.ofNamed(commandVersion, "commandVersion");
    }

    @Override
    public NBootOptionsBuilder commandVersion(Boolean version) {
        this.commandVersion = version;
        return this;
    }

    @Override
    public NOptional<Boolean> commandHelp() {
        return NOptional.ofNamed(commandHelp, "commandHelp");
    }

    @Override
    public NBootOptionsBuilder commandHelp(Boolean help) {
        this.commandHelp = help;
        return this;
    }

    @Override
    public NOptional<Boolean> installCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    /**
     * set skipInstallCompanions
     *
     * @param skipInstallCompanions new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder installCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return this;
    }

    @Override
    public NOptional<Boolean> skipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    /**
     * set skipWelcome
     *
     * @param skipWelcome new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder skipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return this;
    }

    @Override
    public NOptional<String> outLinePrefix() {
        return NOptional.ofNamed(outLinePrefix, "outLinePrefix");
    }

    @Override
    public NBootOptionsBuilder outLinePrefix(String outLinePrefix) {
        this.outLinePrefix = outLinePrefix;
        return this;
    }

    @Override
    public NOptional<String> errLinePrefix() {
        return NOptional.ofNamed(errLinePrefix, "errLinePrefix");
    }

    @Override
    public NBootOptionsBuilder errLinePrefix(String errLinePrefix) {
        this.errLinePrefix = errLinePrefix;
        return this;
    }

    @Override
    public NOptional<Boolean> skipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    /**
     * set skipWelcome
     *
     * @param skipBoot new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder skipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return this;
    }

    @Override
    public NOptional<Boolean> trace() {
        return NOptional.ofNamed(trace, "trace");
    }

    /**
     * set trace
     *
     * @param trace new value
     * @return {@code this} instance
     */
    @Override
    public NBootOptionsBuilder trace(Boolean trace) {
        this.trace = trace;
        return this;
    }

    public NOptional<String> progressOptions() {
        return NOptional.ofNamed(progressOptions, "progressOptions");
    }

    @Override
    public NBootOptionsBuilder progressOptions(String progressOptions) {
        this.progressOptions = progressOptions;
        return this;
    }

    @Override
    public NOptional<Boolean> cached() {
        return NOptional.ofNamed(cached, "cached");
    }

    @Override
    public NBootOptionsBuilder cached(Boolean cached) {
        this.cached = cached;
        return this;
    }

    @Override
    public NOptional<Boolean> indexed() {
        return NOptional.ofNamed(indexed, "indexed");
    }

    @Override
    public NBootOptionsBuilder indexed(Boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    @Override
    public NOptional<Boolean> transitive() {
        return NOptional.ofNamed(transitive, "transitive");
    }

    @Override
    public NBootOptionsBuilder transitive(Boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NOptional<Boolean> bot() {
        return NOptional.ofNamed(bot, "bot");
    }

    @Override
    public NBootOptionsBuilder bot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    @Override
    public NOptional<Boolean> previewRepo() {
        return NOptional.ofNamed(previewRepo, "previewRepo");
    }

    @Override
    public NBootOptionsBuilder previewRepo(Boolean bot) {
        this.previewRepo = bot;
        return this;
    }

    @Override
    public NOptional<NFetchStrategy> fetchStrategy() {
        return NOptional.ofNamed(fetchStrategy, "fetchStrategy");
    }

    @Override
    public NBootOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return this;
    }

    @Override
    public NOptional<InputStream> stdin() {
        return NOptional.ofNamed(stdin, "stdin");
    }

    @Override
    public NBootOptionsBuilder stdin(InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    @Override
    public NOptional<PrintStream> stdout() {
        return NOptional.ofNamed(stdout, "stdout");
    }

    @Override
    public NBootOptionsBuilder stdout(PrintStream stdout) {
        this.stdout = stdout;
        return this;
    }

    @Override
    public NOptional<PrintStream> stderr() {
        return NOptional.ofNamed(stderr, "stderr");
    }

    @Override
    public NBootOptionsBuilder stderr(PrintStream stderr) {
        this.stderr = stderr;
        return this;
    }

    @Override
    public NOptional<ExecutorService> executorService() {
        return NOptional.ofNamed(executorService, "executorService");
    }

    @Override
    public NBootOptionsBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Override
    public NOptional<Instant> expireTime() {
        return NOptional.ofNamed(expireTime, "expireTime");
    }

    @Override
    public NBootOptionsBuilder expireTime(Instant expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    @Override
    public NOptional<Boolean> skipErrors() {
        return NOptional.ofNamed(skipErrors, "skipErrors");
    }

    @Override
    public NBootOptionsBuilder skipErrors(Boolean value) {
        this.skipErrors = value;
        return this;
    }

    @Override
    public NOptional<Boolean> switchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    public NBootOptionsBuilder switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    @Override
    public NOptional<List<NMsg>> errors() {
        return NOptional.ofNamed(errors, "errors");
    }

    @Override
    public NBootOptionsBuilder errors(List<NMsg> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public NBootOptionsBuilder customOptions(List<String> properties) {
        this.customOptions = properties;
        return this;
    }

    @Override
    public NOptional<String> locale() {
        return NOptional.ofNamed(locale, "locale");
    }

    @Override
    public NBootOptionsBuilder locale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public NOptional<String> theme() {
        return NOptional.ofNamed(theme, "theme");
    }

    @Override
    public NBootOptionsBuilder theme(String theme) {
        this.theme = theme;
        return this;
    }

    public NBootOptionsBuilder copyFrom(NBootOptionsInfo other) {
        this.apiVersion(other.getApiVersion() == null ? null : NVersion.get(other.getApiVersion()).orNull());
        this.runtimeId(other.getRuntimeId() == null ? null :
                other.getRuntimeId().contains("#") ? NId.get(other.getRuntimeId()).orNull() :
                        NId.getRuntime(other.getRuntimeId()).orNull()
        );
        this.javaCommand(other.getJavaCommand());
        this.javaOptions(other.getJavaOptions());
        this.workspace(other.getWorkspace());
        this.name(other.getName());
        this.installCompanions(other.getInstallCompanions());
        this.skipWelcome(other.getSkipWelcome());
        this.skipBoot(other.getSkipBoot());
        this.system(other.getSystem());
        this.gui(other.getGui());
        this.userName(other.getUserName());
        this.credential(other.getCredential());
        this.terminalMode(NTerminalMode.parse(other.getTerminalMode()).orNull());
        this.readOnly(other.getReadOnly());
        this.trace(other.getTrace());
        this.progressOptions(other.getProgressOptions());
        {
            NBootLogConfig c = other.getLogConfig();
            NLogConfig v = null;
            if (c != null) {
                v = new NLogConfig();
                v.logFileBase(c.getLogFileBase());
                v.logFileLevel(c.getLogFileLevel());
                v.logTermLevel(c.getLogTermLevel());
                v.logFileSize(c.getLogFileSize());
                v.logFileCount(c.getLogFileCount());
                v.logFileName(c.getLogFileName());
                v.logFileBase(c.getLogFileBase());
            }
            this.logConfig(v);
        }
        this.confirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.confirm(NConfirmationMode.parse(other.getConfirm()).orNull());
        this.outputFormat(NContentType.parse(other.getOutputFormat()).orNull());
        this.outputFormatOptions(other.getOutputFormatOptions());
        this.openMode(NOpenMode.parse(other.getOpenMode()).orNull());
        this.creationTime(other.getCreationTime());
        this.cry(other.getDry());
        this.showStacktrace(other.getShowStacktrace());
        this.classLoaderSupplier(other.getClassLoaderSupplier());
        this.executorOptions(other.getExecutorOptions());
        this.recover(other.getRecover());
        this.reset(other.getReset());
        this.resetHard(other.getResetHard());
        this.commandVersion(other.getCommandVersion());
        this.commandHelp(other.getCommandHelp());
        this.debug(other.getDebug());
        this.inherited(other.getInherited());
        this.executionType(NExecutionType.parse(other.getExecutionType()).orNull());
        this.runAs(NRunAs.parse(other.getRunAs()).orNull());
        this.archetype(other.getArchetype());
        this.storeStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
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
            this.homeLocations(v);
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
            this.storeLocations(v);
        }
        this.storeLayout(NOsFamily.parse(other.getStoreLayout()).orNull());
        this.storeStrategy(NStoreStrategy.parse(other.getStoreStrategy()).orNull());
        this.repositoryStoreStrategy(NStoreStrategy.parse(other.getRepositoryStoreStrategy()).orNull());
        this.fetchStrategy(NFetchStrategy.parse(other.getFetchStrategy()).orNull());
        this.cached(other.getCached());
        this.indexed(other.getIndexed());
        this.transitive(other.getTransitive());
        this.bot(other.getBot());
        this.stdin(other.getStdin());
        this.stdout(other.getStdout());
        this.stderr(other.getStderr());
        this.executorService(other.getExecutorService());

        this.excludedExtensions(other.getExcludedExtensions());
        this.repositories(other.getRepositories());
        this.bootRepositories(other.getBootRepositories());
        this.applicationArguments(other.getApplicationArguments());
        this.customOptions(other.getCustomOptions());
        this.expireTime(other.getExpireTime());
        this.errors(other.getErrors() == null ? new ArrayList<>() : other.getErrors().stream().map(x -> NMsg.ofPlain(x)).collect(Collectors.toList()));
        this.skipErrors(other.getSkipErrors());
        this.switchWorkspace(other.getSwitchWorkspace());
        this.locale(other.getLocale());
        this.theme(other.getTheme());
        this.dependencySolver(other.getDependencySolver());
        this.isolationLevel(NIsolationLevel.parse(other.getIsolationLevel()).orNull());
        this.initLaunchers(other.getInitLaunchers());
        this.initJava(other.getInitJava());
        this.initScripts(other.getInitScripts());
        this.initPlatforms(other.getInitPlatforms());
        this.desktopLauncher(NSupportMode.parse(other.getDesktopLauncher()).orNull());
        this.menuLauncher(NSupportMode.parse(other.getMenuLauncher()).orNull());
        this.userLauncher(NSupportMode.parse(other.getUserLauncher()).orNull());
        this.sharedInstance(other.getSharedInstance());
        this.previewRepo(other.getPreviewRepo());
        this.classLoaderSupplier(other.getClassLoaderSupplier());
        this.classWorldLoader(other.getClassWorldLoader());

        this.bootRepositories(other.getBootRepositories());
        this.runtimeBootDependencyNode(convertNode(other.getRuntimeBootDependencyNode()));
        this.extensionBootDescriptors(other.getExtensionBootDescriptors());
        this.extensionBootDependencyNodes(convertNodes(other.getExtensionBootDependencyNodes()));
        this.bootWorkspaceFactory(other.getBootWorkspaceFactory());
        this.classWorldURLs(other.getClassWorldURLs());
        this.classWorldLoader(other.getClassWorldLoader());
        this.uuid(other.getUuid());
        this.extensionsSet(other.getExtensionsSet());
        this.runtimeBootDescriptor(other.getRuntimeBootDescriptor());

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
    public NBootOptionsBuilder cmdLine(String cmdLine) {
        cmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
        return this;
    }

    @Override
    public NBootOptionsBuilder cmdLine(String[] args) {
        NWorkspaceOptionsBuilder b = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(args, b);
        copyFromIfPresent(b.build());
        return this;
    }

    public NOptional<Boolean> sharedInstance() {
        return NOptional.ofNamed(sharedInstance, "sharedInstance");
    }

    @Override
    public NBootOptionsBuilder sharedInstance(Boolean sharedInstance) {
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
    public NBootOptionsBuilder userName(String username) {
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
    public NBootOptionsBuilder storeLocation(NStoreType location, String value) {
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
    public NBootOptionsBuilder homeLocation(NHomeLocation location, String value) {
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
                    option = NStringUtils.strip(option);
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
    public NOptional<String> dependencySolver() {
        return NOptional.ofNamed(dependencySolver, "dependencySolver");
    }

    @Override
    public NBootOptionsBuilder dependencySolver(String dependencySolver) {
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
    public NOptional<NIsolationLevel> isolationLevel() {
        return NOptional.ofNamed(isolationLevel, "isolationLevel");
    }

    @Override
    public NOptional<Boolean> initLaunchers() {
        return NOptional.ofNamed(initLaunchers, "initLaunchers");
    }

    @Override
    public NOptional<Boolean> initScripts() {
        return NOptional.ofNamed(initScripts, "initScripts");
    }

    @Override
    public NOptional<Boolean> initPlatforms() {
        return NOptional.ofNamed(initPlatforms, "initPlatforms");
    }

    @Override
    public NOptional<Boolean> initJava() {
        return NOptional.ofNamed(initJava, "initJava");
    }

    @Override
    public NBootOptionsBuilder unsetRuntimeOptions() {
        commandHelp(null);
        commandVersion(null);
        openMode(null);
        executionType(null);
        runAs(null);
        reset(null);
        recover(null);
        cry(null);
        showStacktrace(null);
        executorOptions(null);
        applicationArguments(null);
        return this;
    }

    @Override
    public NBootOptionsBuilder unsetCreationOptions() {
        name(null);
        archetype(null);
        storeLayout(null);
        storeStrategy(null);
        repositoryStoreStrategy(null);
        storeLocations(null);
        homeLocations(null);
        switchWorkspace(null);
        return this;
    }

    @Override
    public NBootOptionsBuilder unsetExportedOptions() {
        javaCommand(null);
        javaOptions(null);
        workspace(null);
        userName(null);
        credential(null);
        apiVersion(null);
        runtimeId(null);
        terminalMode(null);
        logConfig(null);
        excludedExtensions(null);
        repositories(null);
        system(null);
        gui(null);
        readOnly(null);
        trace(null);
        progressOptions(null);
        dependencySolver(null);
        debug(null);
        installCompanions(null);
        skipWelcome(null);
        skipBoot(null);
        outLinePrefix(null);
        errLinePrefix(null);
        cached(null);
        indexed(null);
        transitive(null);
        bot(null);
        fetchStrategy(null);
        confirm(null);
        outputFormat(null);
        outputFormatOptions((List<String>) null);
        expireTime(null);
        theme(null);
        locale(null);
        initLaunchers(null);
        initPlatforms(null);
        initScripts(null);
        initJava(null);
        desktopLauncher(null);
        menuLauncher(null);
        userLauncher(null);
        return this;
    }

    @Override
    public NOptional<List<String>> customOptions() {
        return NOptional.ofNamed(customOptions, "customOptions");
    }

    @Override
    public NOptional<List<NArg>> customOptionArgs() {
        return NOptional.ofNamed(customOptions == null ? null : customOptions.stream().map(x -> NArg.of(x)).collect(Collectors.toList()), "customOptions");
    }

    @Override
    public NOptional<NArg> customOptionArg(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .findFirst(), key);
    }

    @Override
    public NOptional<String> customOption(String key) {
        return NOptional.ofNamedOptional(customOptions().orElse(new ArrayList<>()).stream().map(x -> NArg.of(x))
                .filter(x -> Objects.equals(x.getStringKey().orNull(), key))
                .map(x->x.image())
                .findFirst(), key);
    }

}
