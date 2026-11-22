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
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootHomeLocation;
import net.thevpc.nuts.boot.NBootLogConfig;
import net.thevpc.nuts.core.NWorkspaceCmdLineParser;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Workspace creation/opening options class.
 * <p>
 * %category Config
 *
 * @since 0.5.4
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNWorkspaceOptionsBuilder implements NWorkspaceOptionsBuilder {

    private final DefaultNWorkspaceOptionsBuilderIdentityState identityState;
    private final DefaultNWorkspaceOptionsBuilderExecutionState executionState;
    private final DefaultNWorkspaceOptionsBuilderWorkspaceState workspaceState;
    private final DefaultNWorkspaceOptionsBuilderClassWorldState classworldState;

    public DefaultNWorkspaceOptionsBuilder() {
        this.identityState = new DefaultNWorkspaceOptionsBuilderIdentityState(this);
        this.executionState = new DefaultNWorkspaceOptionsBuilderExecutionState(this);
        this.workspaceState = new DefaultNWorkspaceOptionsBuilderWorkspaceState(this);
        this.classworldState = new DefaultNWorkspaceOptionsBuilderClassWorldState(this);

    }
    public NWorkspaceOptionsBuilder resetOptions() {
        identityState.outputFormatOptions = null;
        identityState.customOptions = null;
        identityState.apiVersion = null;
        identityState.runtimeId = null;
        identityState.javaCommand = null;
        identityState.javaOptions = null;
        identityState.workspace = null;
        identityState.outLinePrefix = null;
        identityState.errLinePrefix = null;
        identityState.name = null;
        workspaceState.installCompanions = null;
        workspaceState.skipWelcome = null;
        workspaceState.skipBoot = null;
        workspaceState.system = null;
        workspaceState.gui = null;
        workspaceState.excludedExtensions = null;
        workspaceState.repositories = null;
        identityState.userName = null;
        identityState.sharedInstance = null;
        identityState.credentials = null;
        executionState.terminalMode = null;
        executionState.readOnly = null;
        executionState.trace = null;
        identityState.progressOptions = null;
        identityState.dependencySolver = null;
        executionState.logConfig = null;
        executionState.confirm = null;
        executionState.outputFormat = null;
        executionState.applicationArguments = null;
        executionState.openMode = null;
        identityState.creationTime = null;
        executionState.dry = null;
        executionState.showStacktrace = null;
        executionState.classLoaderSupplier = null;
        executionState.executorOptions = null;
        executionState.recover = null;
        executionState.reset = null;
        executionState.resetHard = null;
        executionState.commandHelp = null;
        identityState.debug = null;
        identityState.inherited = null;
        executionState.executionType = null;
        executionState.runAs = null;
        workspaceState.archetype = null;
        workspaceState.switchWorkspace = null;
        workspaceState.storeLocations = null;
        workspaceState.homeLocations = null;
        workspaceState.storeLayout = null;
        workspaceState.storeStrategy = null;
        workspaceState.repositoryStoreStrategy = null;
        workspaceState.bootRepositories = null;
        executionState.fetchStrategy = null;
        executionState.cached = null;
        executionState.indexed = null;
        executionState.transitive = null;
        executionState.bot = null;
        executionState.previewRepo = null;
        executionState.stdin = null;
        executionState.stdout = null;
        executionState.stderr = null;
        executionState.executorService = null;
        executionState.expireTime = null;
        //executionState.errors are not rest
        //executionState.errors = null;
        executionState.skipErrors = null;
        identityState.locale = null;
        identityState.theme = null;
        executionState.initLaunchers = null;
        executionState.initScripts = null;
        executionState.initPlatforms = null;
        executionState.initJava = null;
        executionState.localMaven = null;
        executionState.isolationLevel = null;
        executionState.desktopLauncher = null;
        executionState.menuLauncher = null;
        executionState.userLauncher = null;
        return this;
    }
    public NOptional<NSupportMode> desktopLauncher() {
        return executionState.desktopLauncher();
    }
    public NOptional<NSupportMode> menuLauncher() {
        return executionState.menuLauncher();
    }
    public NOptional<NSupportMode> userLauncher() {
        return executionState.userLauncher();
    }
    public NWorkspaceOptionsBuilder initLaunchers(Boolean initLaunchers) {
        return executionState.initLaunchers(initLaunchers);
    }
    public NWorkspaceOptionsBuilder initScripts(Boolean initScripts) {
        return executionState.initScripts(initScripts);
    }
    public NWorkspaceOptionsBuilder initPlatforms(Boolean initPlatforms) {
        return executionState.initPlatforms(initPlatforms);
    }
    public NWorkspaceOptionsBuilder localMaven(Boolean localMaven) {
        return executionState.localMaven(localMaven);
    }
    public NWorkspaceOptionsBuilder initJava(Boolean initJava) {
        return executionState.initJava(initJava);
    }
    public NWorkspaceOptionsBuilder isolationLevel(NIsolationLevel isolationLevel) {
        return executionState.isolationLevel(isolationLevel);
    }
    public NWorkspaceOptionsBuilder desktopLauncher(NSupportMode desktopLauncher) {
        return executionState.desktopLauncher(desktopLauncher);
    }
    public NWorkspaceOptionsBuilder menuLauncher(NSupportMode menuLauncher) {
        return executionState.menuLauncher(menuLauncher);
    }
    public NWorkspaceOptionsBuilder userLauncher(NSupportMode userLauncher) {
        return executionState.userLauncher(userLauncher);
    }
    public NWorkspaceOptionsBuilder copy() {
        return new DefaultNWorkspaceOptionsBuilder().copyFrom(this);
    }
    public NOptional<NVersion> apiVersion() {
        return identityState.apiVersion();
    }
    public NWorkspaceOptionsBuilder apiVersion(NVersion apiVersion) {
        return identityState.apiVersion(apiVersion);
    }
    public NOptional<List<String>> applicationArguments() {
        return executionState.applicationArguments();
    }
    public NWorkspaceOptionsBuilder applicationArguments(List<String> applicationArguments) {
        return executionState.applicationArguments(applicationArguments);
    }
    public NOptional<String> archetype() {
        return workspaceState.archetype();
    }
    public NWorkspaceOptionsBuilder archetype(String archetype) {
        return workspaceState.archetype(archetype);
    }
    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return executionState.classLoaderSupplier();
    }
    public NWorkspaceOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider) {
        return executionState.classLoaderSupplier(provider);
    }
    public NOptional<NConfirmationMode> confirm() {
        return executionState.confirm();
    }
    public NWorkspaceOptionsBuilder confirm(NConfirmationMode confirm) {
        return executionState.confirm(confirm);
    }
    public NOptional<Boolean> dry() {
        return executionState.dry();
    }
    public NOptional<Boolean> force() {
        return executionState.force();
    }
    public NOptional<Boolean> showStacktrace() {
        return executionState.showStacktrace();
    }
    public NWorkspaceOptionsBuilder dry(Boolean dry) {
        return executionState.dry(dry);
    }
    public NWorkspaceOptionsBuilder force(Boolean force) {
        return executionState.force(force);
    }
    public NWorkspaceOptionsBuilder showStacktrace(Boolean showStacktrace) {
        return executionState.showStacktrace(showStacktrace);
    }
    public NOptional<Instant> creationTime() {
        return identityState.creationTime();
    }
    public NWorkspaceOptionsBuilder creationTime(Instant creationTime) {
        return identityState.creationTime(creationTime);
    }
    public NOptional<List<String>> excludedExtensions() {
        return workspaceState.excludedExtensions();
    }
    public NWorkspaceOptionsBuilder excludedExtensions(List<String> excludedExtensions) {
        return workspaceState.excludedExtensions(excludedExtensions);
    }
    public NOptional<NExecutionType> executionType() {
        return executionState.executionType();
    }
    public NWorkspaceOptionsBuilder executionType(NExecutionType executionType) {
        return executionState.executionType(executionType);
    }
    public NOptional<NRunAs> runAs() {
        return executionState.runAs();
    }
    public NWorkspaceOptionsBuilder runAs(NRunAs runAs) {
        return executionState.runAs(runAs);
    }
    public NOptional<List<String>> executorOptions() {
        return executionState.executorOptions();
    }
    public NWorkspaceOptionsBuilder executorOptions(List<String> executorOptions) {
        return executionState.executorOptions(executorOptions);
    }
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(workspaceState.homeLocations == null ? null : workspaceState.homeLocations.get(location), "workspaceState.homeLocations[" + location + "]");
    }
    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return workspaceState.homeLocations();
    }
    public NWorkspaceOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations) {
        return workspaceState.homeLocations(homeLocations);
    }
    public NOptional<String> javaCommand() {
        return identityState.javaCommand();
    }
    public NWorkspaceOptionsBuilder javaCommand(String javaCommand) {
        return identityState.javaCommand(javaCommand);
    }
    public NOptional<String> javaOptions() {
        return identityState.javaOptions();
    }
    public NWorkspaceOptionsBuilder javaOptions(String javaOptions) {
        return identityState.javaOptions(javaOptions);
    }
    public NOptional<NLogConfig> logConfig() {
        return executionState.logConfig();
    }
    public NWorkspaceOptionsBuilder logConfig(NLogConfig logConfig) {
        return executionState.logConfig(logConfig);
    }
    public NOptional<String> name() {
        return identityState.name();
    }
    public NWorkspaceOptionsBuilder name(String workspaceName) {
        return identityState.name(workspaceName);
    }
    public NOptional<NOpenMode> openMode() {
        return executionState.openMode();
    }
    public NWorkspaceOptionsBuilder openMode(NOpenMode openMode) {
        return executionState.openMode(openMode);
    }
    public NOptional<NContentType> outputFormat() {
        return executionState.outputFormat();
    }
    public NWorkspaceOptionsBuilder outputFormat(NContentType outputFormat) {
        return executionState.outputFormat(outputFormat);
    }
    public NOptional<List<String>> outputFormatOptions() {
        return identityState.outputFormatOptions();
    }
    public NWorkspaceOptionsBuilder outputFormatOptions(List<String> options) {
        return identityState.outputFormatOptions(options);
    }
    public NWorkspaceOptionsBuilder setOutputFormatOptions(String... options) {
        if (identityState.outputFormatOptions == null) {
            identityState.outputFormatOptions = new ArrayList<>();
        }
        identityState.outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }
    public NOptional<char[]> credential() {
        return identityState.credential();
    }
    public NWorkspaceOptionsBuilder credential(char[] credentials) {
        return identityState.credential(credentials);
    }
    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return workspaceState.repositoryStoreStrategy();
    }
    public NWorkspaceOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        return workspaceState.repositoryStoreStrategy(repositoryStoreStrategy);
    }
    public NOptional<NId> runtimeId() {
        return identityState.runtimeId();
    }
    public NWorkspaceOptionsBuilder runtimeId(NId runtimeId) {
        return identityState.runtimeId(runtimeId);
    }
    public NOptional<String> storeType(NStoreType folder) {
        return NOptional.ofNamed(workspaceState.storeLocations == null ? null : workspaceState.storeLocations.get(folder), "workspaceState.storeLocations[" + folder + "]");
    }
    public NOptional<NOsFamily> storeLayout() {
        return workspaceState.storeLayout();
    }
    public NWorkspaceOptionsBuilder storeLayout(NOsFamily storeLayout) {
        return workspaceState.storeLayout(storeLayout);
    }
    public NOptional<NStoreStrategy> storeStrategy() {
        return workspaceState.storeStrategy();
    }
    public NWorkspaceOptionsBuilder storeStrategy(NStoreStrategy storeStrategy) {
        return workspaceState.storeStrategy(storeStrategy);
    }
    public NOptional<Map<NStoreType, String>> storeLocations() {
        return workspaceState.storeLocations();
    }
    public NWorkspaceOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations) {
        return workspaceState.storeLocations(storeLocations);
    }
    public NOptional<NTerminalMode> terminalMode() {
        return executionState.terminalMode();
    }
    public NWorkspaceOptionsBuilder terminalMode(NTerminalMode terminalMode) {
        return executionState.terminalMode(terminalMode);
    }
    public NOptional<List<String>> repositories() {
        return workspaceState.repositories();
    }
    public NOptional<List<String>> bootRepositories() {
        return workspaceState.bootRepositories();
    }
    public NWorkspaceOptionsBuilder repositories(List<String> repositories) {
        return workspaceState.repositories(repositories);
    }
    public NWorkspaceOptionsBuilder bootRepositories(List<String> repositories) {
        return workspaceState.bootRepositories(repositories);
    }
    public NOptional<String> userName() {
        return identityState.userName();
    }
    public NOptional<String> workspace() {
        return identityState.workspace();
    }
    public NWorkspaceOptionsBuilder workspace(String workspace) {
        return identityState.workspace(workspace);
    }
    public NOptional<String> debug() {
        return identityState.debug();
    }
    public NWorkspaceOptionsBuilder debug(String debug) {
        return identityState.debug(debug);
    }
    public NOptional<Boolean> system() {
        return workspaceState.system();
    }
    public NWorkspaceOptionsBuilder system(Boolean system) {
        return workspaceState.system(system);
    }
    public NOptional<Boolean> gui() {
        return workspaceState.gui();
    }
    public NWorkspaceOptionsBuilder gui(Boolean gui) {
        return workspaceState.gui(gui);
    }
    public NOptional<Boolean> inherited() {
        return identityState.inherited();
    }
    public NWorkspaceOptionsBuilder inherited(Boolean inherited) {
        return identityState.inherited(inherited);
    }
    public NOptional<Boolean> readOnly() {
        return executionState.readOnly();
    }
    public NWorkspaceOptionsBuilder readOnly(Boolean readOnly) {
        return executionState.readOnly(readOnly);
    }
    public NOptional<Boolean> recover() {
        return executionState.recover();
    }
    public NWorkspaceOptionsBuilder recover(Boolean recover) {
        return executionState.recover(recover);
    }
    public NOptional<Boolean> reset() {
        return executionState.reset();
    }
    public NWorkspaceOptionsBuilder reset(Boolean reset) {
        return executionState.reset(reset);
    }
    public NOptional<Boolean> resetHard() {
        return executionState.resetHard();
    }
    public NWorkspaceOptionsBuilder resetHard(Boolean resetHard) {
        return executionState.resetHard(resetHard);
    }
    public NOptional<Boolean> commandVersion() {
        return executionState.commandVersion();
    }
    public NWorkspaceOptionsBuilder commandVersion(Boolean version) {
        return executionState.commandVersion(version);
    }
    public NOptional<Boolean> commandHelp() {
        return executionState.commandHelp();
    }
    public NWorkspaceOptionsBuilder commandHelp(Boolean help) {
        return executionState.commandHelp(help);
    }
    public NOptional<Boolean> installCompanions() {
        return workspaceState.installCompanions();
    }
    public NWorkspaceOptionsBuilder installCompanions(Boolean skipInstallCompanions) {
        return workspaceState.installCompanions(skipInstallCompanions);
    }
    public NOptional<Boolean> skipWelcome() {
        return workspaceState.skipWelcome();
    }
    public NWorkspaceOptionsBuilder skipWelcome(Boolean skipWelcome) {
        return workspaceState.skipWelcome(skipWelcome);
    }
    public NOptional<String> outLinePrefix() {
        return identityState.outLinePrefix();
    }
    public NWorkspaceOptionsBuilder outLinePrefix(String outLinePrefix) {
        return identityState.outLinePrefix(outLinePrefix);
    }
    public NOptional<String> errLinePrefix() {
        return identityState.errLinePrefix();
    }
    public NWorkspaceOptionsBuilder errLinePrefix(String errLinePrefix) {
        return identityState.errLinePrefix(errLinePrefix);
    }
    public NOptional<Boolean> skipBoot() {
        return workspaceState.skipBoot();
    }
    public NWorkspaceOptionsBuilder skipBoot(Boolean skipBoot) {
        return workspaceState.skipBoot(skipBoot);
    }
    public NOptional<Boolean> trace() {
        return executionState.trace();
    }
    public NWorkspaceOptionsBuilder trace(Boolean trace) {
        return executionState.trace(trace);
    }
    public NOptional<String> progressOptions() {
        return identityState.progressOptions();
    }
    public NWorkspaceOptionsBuilder progressOptions(String progressOptions) {
        return identityState.progressOptions(progressOptions);
    }
    public NOptional<Boolean> cached() {
        return executionState.cached();
    }
    public NWorkspaceOptionsBuilder cached(Boolean cached) {
        return executionState.cached(cached);
    }
    public NOptional<Boolean> indexed() {
        return executionState.indexed();
    }
    public NWorkspaceOptionsBuilder indexed(Boolean indexed) {
        return executionState.indexed(indexed);
    }
    public NOptional<Boolean> transitive() {
        return executionState.transitive();
    }
    public NWorkspaceOptionsBuilder transitive(Boolean transitive) {
        return executionState.transitive(transitive);
    }
    public NOptional<Boolean> bot() {
        return executionState.bot();
    }
    public NWorkspaceOptionsBuilder bot(Boolean bot) {
        return executionState.bot(bot);
    }
    public NOptional<Boolean> previewRepo() {
        return executionState.previewRepo();
    }
    public NWorkspaceOptionsBuilder previewRepo(Boolean bot) {
        return executionState.previewRepo(bot);
    }
    public NOptional<NFetchStrategy> fetchStrategy() {
        return executionState.fetchStrategy();
    }
    public NWorkspaceOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy) {
        return executionState.fetchStrategy(fetchStrategy);
    }
    public NOptional<InputStream> stdin() {
        return executionState.stdin();
    }
    public NWorkspaceOptionsBuilder stdin(InputStream stdin) {
        return executionState.stdin(stdin);
    }
    public NOptional<PrintStream> stdout() {
        return executionState.stdout();
    }
    public NWorkspaceOptionsBuilder stdout(PrintStream stdout) {
        return executionState.stdout(stdout);
    }
    public NOptional<PrintStream> stderr() {
        return executionState.stderr();
    }
    public NWorkspaceOptionsBuilder stderr(PrintStream stderr) {
        return executionState.stderr(stderr);
    }
    public NOptional<ExecutorService> executorService() {
        return executionState.executorService();
    }
    public NWorkspaceOptionsBuilder executorService(ExecutorService executorService) {
        return executionState.executorService(executorService);
    }
    public NOptional<Instant> expireTime() {
        return executionState.expireTime();
    }
    public NWorkspaceOptionsBuilder expireTime(Instant expireTime) {
        return executionState.expireTime(expireTime);
    }
    public NOptional<Boolean> skipErrors() {
        return executionState.skipErrors();
    }
    public NWorkspaceOptionsBuilder skipErrors(Boolean value) {
        return executionState.skipErrors(value);
    }
    public NOptional<Boolean> switchWorkspace() {
        return workspaceState.switchWorkspace();
    }
    public NWorkspaceOptionsBuilder switchWorkspace(Boolean switchWorkspace) {
        return workspaceState.switchWorkspace(switchWorkspace);
    }
    public NOptional<List<NMsg>> errors() {
        return executionState.errors();
    }
    public NWorkspaceOptionsBuilder errors(List<NMsg> errors) {
        return executionState.errors(errors);
    }
    public NOptional<List<String>> customOptions() {
        return identityState.customOptions();
    }
    public NWorkspaceOptionsBuilder customOptions(List<String> properties) {
        return identityState.customOptions(properties);
    }
    public NOptional<String> locale() {
        return identityState.locale();
    }
    public NWorkspaceOptionsBuilder locale(String locale) {
        return identityState.locale(locale);
    }
    public NOptional<String> theme() {
        return identityState.theme();
    }
    public NWorkspaceOptionsBuilder theme(String theme) {
        return identityState.theme(theme);
    }
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other) {
        if (other == null) {
            return this;
        }
        identityState.apiVersion(other.apiVersion().orNull());
        identityState.runtimeId(other.runtimeId().orNull());
        identityState.javaCommand(other.javaCommand().orNull());
        identityState.javaOptions(other.javaOptions().orNull());
        identityState.workspace(other.workspace().orNull());
        identityState.name(other.name().orNull());
        workspaceState.installCompanions(other.installCompanions().orNull());
        workspaceState.skipWelcome(other.skipWelcome().orNull());
        workspaceState.skipBoot(other.skipBoot().orNull());
        workspaceState.system(other.system().orNull());
        workspaceState.gui(other.gui().orNull());
        this.setUserName(other.userName().orNull());
        this.credential(other.credential().orNull());
        executionState.terminalMode(other.terminalMode().orNull());
        executionState.readOnly(other.readOnly().orNull());
        executionState.trace(other.trace().orNull());
        identityState.progressOptions(other.progressOptions().orNull());
        executionState.logConfig(other.logConfig().orNull());
        executionState.confirm(other.confirm().orNull());
        executionState.confirm(other.confirm().orNull());
        executionState.outputFormat(other.outputFormat().orNull());
        identityState.outputFormatOptions(other.outputFormatOptions().orNull());
        executionState.openMode(other.openMode().orNull());
        identityState.creationTime(other.creationTime().orNull());
        executionState.dry(other.dry().orNull());
        executionState.force(other.force().orNull());
        executionState.showStacktrace(other.showStacktrace().orNull());
        executionState.classLoaderSupplier(other.classLoaderSupplier().orNull());
        executionState.executorOptions(other.executorOptions().orNull());
        executionState.recover(other.recover().orNull());
        executionState.reset(other.reset().orNull());
        executionState.resetHard(other.resetHard().orNull());
        executionState.commandVersion(other.commandVersion().orNull());
        executionState.commandHelp(other.commandHelp().orNull());
        identityState.debug(other.debug().orNull());
        identityState.inherited(other.inherited().orNull());
        executionState.executionType(other.executionType().orNull());
        executionState.runAs(other.runAs().orNull());
        workspaceState.archetype(other.archetype().orNull());
        workspaceState.storeStrategy(other.storeStrategy().orNull());
        workspaceState.homeLocations(other.homeLocations().orNull());
        workspaceState.storeLocations(other.storeLocations().orNull());
        workspaceState.storeLayout(other.storeLayout().orNull());
        workspaceState.storeStrategy(other.storeStrategy().orNull());
        workspaceState.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        executionState.fetchStrategy(other.fetchStrategy().orNull());
        executionState.cached(other.cached().orNull());
        executionState.indexed(other.indexed().orNull());
        executionState.transitive(other.transitive().orNull());
        executionState.bot(other.bot().orNull());
        executionState.stdin(other.stdin().orNull());
        executionState.stdout(other.stdout().orNull());
        executionState.stderr(other.stderr().orNull());
        executionState.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        workspaceState.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        workspaceState.repositories(other.repositories().orNull());
        executionState.applicationArguments(other.applicationArguments().orNull());
        identityState.customOptions(other.customOptions().orNull());
        executionState.expireTime(other.expireTime().orNull());
        executionState.errors(other.errors().orNull());
        executionState.skipErrors(other.skipErrors().orNull());
        workspaceState.switchWorkspace(other.switchWorkspace().orNull());
        identityState.locale(other.locale().orNull());
        identityState.theme(other.theme().orNull());
        identityState.dependencySolver(other.dependencySolver().orNull());
        executionState.isolationLevel(other.isolationLevel().orNull());
        executionState.initLaunchers(other.initLaunchers().orNull());
        executionState.initJava(other.initJava().orNull());
        executionState.localMaven(other.localMaven().orNull());
        executionState.initScripts(other.initScripts().orNull());
        executionState.initPlatforms(other.initPlatforms().orNull());
        executionState.desktopLauncher(other.desktopLauncher().orNull());
        executionState.menuLauncher(other.menuLauncher().orNull());
        executionState.userLauncher(other.userLauncher().orNull());
        identityState.sharedInstance(other.sharedInstance().orNull());
        executionState.previewRepo(other.previewRepo().orNull());
        return this;
    }
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptionsBuilder other) {
        if (other == null) {
            return this;
        }
        identityState.apiVersion(other.apiVersion().orNull());
        identityState.runtimeId(other.runtimeId().orNull());
        identityState.javaCommand(other.javaCommand().orNull());
        identityState.javaOptions(other.javaOptions().orNull());
        identityState.workspace(other.workspace().orNull());
        identityState.name(other.name().orNull());
        workspaceState.installCompanions(other.installCompanions().orNull());
        workspaceState.skipWelcome(other.skipWelcome().orNull());
        workspaceState.skipBoot(other.skipBoot().orNull());
        workspaceState.system(other.system().orNull());
        workspaceState.gui(other.gui().orNull());
        this.setUserName(other.userName().orNull());
        this.credential(other.credential().orNull());
        executionState.terminalMode(other.terminalMode().orNull());
        executionState.readOnly(other.readOnly().orNull());
        executionState.trace(other.trace().orNull());
        identityState.progressOptions(other.progressOptions().orNull());
        executionState.logConfig(other.logConfig().orNull());
        executionState.confirm(other.confirm().orNull());
        executionState.confirm(other.confirm().orNull());
        executionState.outputFormat(other.outputFormat().orNull());
        identityState.outputFormatOptions(other.outputFormatOptions().orNull());
        executionState.openMode(other.openMode().orNull());
        identityState.creationTime(other.creationTime().orNull());
        executionState.dry(other.dry().orNull());
        executionState.force(other.force().orNull());
        executionState.showStacktrace(other.showStacktrace().orNull());
        executionState.classLoaderSupplier(other.classLoaderSupplier().orNull());
        executionState.executorOptions(other.executorOptions().orNull());
        executionState.recover(other.recover().orNull());
        executionState.reset(other.reset().orNull());
        executionState.resetHard(other.resetHard().orNull());
        executionState.commandVersion(other.commandVersion().orNull());
        executionState.commandHelp(other.commandHelp().orNull());
        identityState.debug(other.debug().orNull());
        identityState.inherited(other.inherited().orNull());
        executionState.executionType(other.executionType().orNull());
        executionState.runAs(other.runAs().orNull());
        workspaceState.archetype(other.archetype().orNull());
        workspaceState.storeStrategy(other.storeStrategy().orNull());
        workspaceState.homeLocations(other.homeLocations().orNull());
        workspaceState.storeLocations(other.storeLocations().orNull());
        workspaceState.storeLayout(other.storeLayout().orNull());
        workspaceState.storeStrategy(other.storeStrategy().orNull());
        workspaceState.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        executionState.fetchStrategy(other.fetchStrategy().orNull());
        executionState.cached(other.cached().orNull());
        executionState.indexed(other.indexed().orNull());
        executionState.transitive(other.transitive().orNull());
        executionState.bot(other.bot().orNull());
        executionState.stdin(other.stdin().orNull());
        executionState.stdout(other.stdout().orNull());
        executionState.stderr(other.stderr().orNull());
        executionState.executorService(other.executorService().orNull());
//        this.setBootRepositories(other.getBootRepositories());

        workspaceState.excludedExtensions(other.excludedExtensions().orNull());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        workspaceState.repositories(other.repositories().orNull());
        executionState.applicationArguments(other.applicationArguments().orNull());
        identityState.customOptions(other.customOptions().orNull());
        executionState.expireTime(other.expireTime().orNull());
        executionState.errors(other.errors().orNull());
        executionState.skipErrors(other.skipErrors().orNull());
        workspaceState.switchWorkspace(other.switchWorkspace().orNull());
        identityState.locale(other.locale().orNull());
        identityState.theme(other.theme().orNull());
        identityState.dependencySolver(other.dependencySolver().orNull());
        executionState.isolationLevel(other.isolationLevel().orNull());
        executionState.initLaunchers(other.initLaunchers().orNull());
        executionState.initJava(other.initJava().orNull());
        executionState.localMaven(other.localMaven().orNull());
        executionState.initScripts(other.initScripts().orNull());
        executionState.initPlatforms(other.initPlatforms().orNull());
        executionState.desktopLauncher(other.desktopLauncher().orNull());
        executionState.menuLauncher(other.menuLauncher().orNull());
        executionState.userLauncher(other.userLauncher().orNull());
        identityState.sharedInstance(other.sharedInstance().orNull());
        executionState.previewRepo(other.previewRepo().orNull());
        return this;
    }
    public NBootOptionsInfo toBootOptionsInfo() {
        return build().toBootOptionsInfo();
    }
    public NWorkspaceOptionsBuilder copyFrom(NBootOptionsInfo other) {
        identityState.apiVersion(other.apiVersion() == null ? null : NVersion.get(other.apiVersion()).orNull());
        identityState.runtimeId(other.runtimeId() == null ? null :
                other.runtimeId().contains("#") ? NId.get(other.runtimeId()).orNull() :
                        NId.getRuntime(other.runtimeId()).orNull()
        );
        identityState.javaCommand(other.javaCommand());
        identityState.javaOptions(other.javaOptions());
        identityState.workspace(other.workspace());
        identityState.name(other.name());
        workspaceState.installCompanions(other.installCompanions());
        workspaceState.skipWelcome(other.skipWelcome());
        workspaceState.skipBoot(other.skipBoot());
        workspaceState.system(other.system());
        workspaceState.gui(other.gui());
        this.setUserName(other.userName());
        this.credential(other.credential());
        executionState.terminalMode(NTerminalMode.parse(other.terminalMode()).orNull());
        executionState.readOnly(other.readOnly());
        executionState.trace(other.trace());
        identityState.progressOptions(other.progressOptions());
        {
            NBootLogConfig c = other.logConfig();
            NLogConfig v = null;
            if (c != null) {
                v = new NLogConfig();
                v.logFileBase(c.logFileBase());
                v.logFileLevel(c.logFileLevel());
                v.logTermLevel(c.logTermLevel());
                v.logFileSize(c.logFileSize());
                v.logFileCount(c.logFileCount());
                v.logFileName(c.logFileName());
                v.logFileBase(c.logFileBase());
            }
            executionState.logConfig(v);
        }
        executionState.confirm(NConfirmationMode.parse(other.confirm()).orNull());
        executionState.confirm(NConfirmationMode.parse(other.confirm()).orNull());
        executionState.outputFormat(NContentType.parse(other.outputFormat()).orNull());
        identityState.outputFormatOptions(other.outputFormatOptions());
        executionState.openMode(NOpenMode.parse(other.openMode()).orNull());
        identityState.creationTime(other.creationTime());
        executionState.dry(other.dry());
        executionState.force(other.force());
        executionState.showStacktrace(other.showStacktrace());
        executionState.classLoaderSupplier(other.classLoaderSupplier());
        executionState.executorOptions(other.executorOptions());
        executionState.recover(other.recover());
        executionState.reset(other.reset());
        executionState.resetHard(other.resetHard());
        executionState.commandVersion(other.commandVersion());
        executionState.commandHelp(other.commandHelp());
        identityState.debug(other.debug());
        identityState.inherited(other.inherited());
        executionState.executionType(NExecutionType.parse(other.executionType()).orNull());
        executionState.runAs(NRunAs.parse(other.runAs()).orNull());
        workspaceState.archetype(other.archetype());
        workspaceState.storeStrategy(NStoreStrategy.parse(other.storeStrategy()).orNull());
        {
            Map<NBootHomeLocation, String> c = other.homeLocations();
            Map<NHomeLocation, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NBootHomeLocation, String> e : c.entrySet()) {
                    v.put(NHomeLocation.of(
                            NOsFamily.parse(e.getKey().osFamily()).get(),
                            NStoreType.parse(e.getKey().storeLocation()).get()
                    ), e.getValue());
                }
            }
            workspaceState.homeLocations(v);
        }
        {
            Map<String, String> c = other.storeLocations();
            Map<NStoreType, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<String, String> e : c.entrySet()) {
                    v.put(NStoreType.parse(e.getKey()).get(), e.getValue());
                }
            }
            workspaceState.storeLocations(v);
        }
        workspaceState.storeLayout(NOsFamily.parse(other.storeLayout()).orNull());
        workspaceState.storeStrategy(NStoreStrategy.parse(other.storeStrategy()).orNull());
        workspaceState.repositoryStoreStrategy(NStoreStrategy.parse(other.repositoryStoreStrategy()).orNull());
        executionState.fetchStrategy(NFetchStrategy.parse(other.fetchStrategy()).orNull());
        executionState.cached(other.cached());
        executionState.indexed(other.indexed());
        executionState.transitive(other.transitive());
        executionState.bot(other.bot());
        executionState.stdin(other.stdin());
        executionState.stdout(other.stdout());
        executionState.stderr(other.stderr());
        executionState.executorService(other.executorService());
//        this.setBootRepositories(other.getBootRepositories());

        workspaceState.excludedExtensions(other.excludedExtensions());
//        this.setExcludedRepositories(other.getExcludedRepositories() == null ? null : Arrays.copyOf(other.getExcludedRepositories(), other.getExcludedRepositories().length));
        workspaceState.repositories(other.repositories());
        workspaceState.bootRepositories(other.bootRepositories());
        executionState.applicationArguments(other.applicationArguments());
        identityState.customOptions(other.customOptions());
        executionState.expireTime(other.expireTime());
        executionState.errors(other.errors() == null ? new ArrayList<>() : other.errors().stream().map(x -> NMsg.ofP(x)).collect(Collectors.toList()));
        executionState.skipErrors(other.skipErrors());
        workspaceState.switchWorkspace(other.switchWorkspace());
        identityState.locale(other.locale());
        identityState.theme(other.theme());
        identityState.dependencySolver(other.dependencySolver());
        executionState.isolationLevel(NIsolationLevel.parse(other.isolationLevel()).orNull());
        executionState.initLaunchers(other.initLaunchers());
        executionState.initJava(other.initJava());
        executionState.localMaven(other.localMaven());
        executionState.initScripts(other.initScripts());
        executionState.initPlatforms(other.initPlatforms());
        executionState.desktopLauncher(NSupportMode.parse(other.desktopLauncher()).orNull());
        executionState.menuLauncher(NSupportMode.parse(other.menuLauncher()).orNull());
        executionState.userLauncher(NSupportMode.parse(other.userLauncher()).orNull());
        identityState.sharedInstance(other.sharedInstance());
        executionState.previewRepo(other.previewRepo());
        return this;
    }
    public NWorkspaceOptionsBuilder copyFrom(NWorkspaceOptions other, NAssignmentPolicy assignmentPolicy) {
        if (other != null) {
            if (assignmentPolicy == null) {
                assignmentPolicy = NAssignmentPolicy.ANY;
            }
            assignmentPolicy.applyOptionalValue(this::apiVersion, other::apiVersion, this::apiVersion);
            assignmentPolicy.applyOptionalValue(this::runtimeId, other::runtimeId, this::runtimeId);
            assignmentPolicy.applyOptionalValue(this::javaOptions, other::javaOptions, this::javaOptions);
            assignmentPolicy.applyOptionalValue(this::javaCommand, other::javaCommand, this::javaCommand);
            assignmentPolicy.applyOptionalValue(this::workspace, other::workspace, this::workspace);
            assignmentPolicy.applyOptionalValue(this::name, other::name, this::name);
            assignmentPolicy.applyOptionalValue(this::installCompanions, other::installCompanions, this::installCompanions);
            assignmentPolicy.applyOptionalValue(this::skipWelcome, other::skipWelcome, this::skipWelcome);
            assignmentPolicy.applyOptionalValue(this::skipBoot, other::skipBoot, this::skipBoot);
            assignmentPolicy.applyOptionalValue(this::system, other::system, this::system);
            assignmentPolicy.applyOptionalValue(this::gui, other::gui, this::gui);
            assignmentPolicy.applyOptionalValue(this::userName, other::userName, this::setUserName);
            assignmentPolicy.applyOptionalValue(this::credential, other::credential, this::credential);
            assignmentPolicy.applyOptionalValue(this::terminalMode, other::terminalMode, this::terminalMode);
            assignmentPolicy.applyOptionalValue(this::readOnly, other::readOnly, this::readOnly);
            assignmentPolicy.applyOptionalValue(this::trace, other::trace, this::trace);
            assignmentPolicy.applyOptionalValue(this::progressOptions, other::progressOptions, this::progressOptions);
            assignmentPolicy.applyOptionalValue(this::logConfig, other::logConfig, this::logConfig);
            assignmentPolicy.applyOptionalValue(this::confirm, other::confirm, this::confirm);
            assignmentPolicy.applyOptionalValue(this::confirm, other::confirm, this::confirm);
            assignmentPolicy.applyOptionalValue(this::outputFormat, other::outputFormat, this::outputFormat);
            assignmentPolicy.applyOptionalValue(this::outputFormatOptions, other::outputFormatOptions, this::outputFormatOptions);
            assignmentPolicy.applyOptionalValue(this::openMode, other::openMode, this::openMode);
            assignmentPolicy.applyOptionalValue(this::creationTime, other::creationTime, this::creationTime);
            assignmentPolicy.applyOptionalValue(this::dry, other::dry, this::dry);
            assignmentPolicy.applyOptionalValue(this::showStacktrace, other::showStacktrace, this::showStacktrace);
            assignmentPolicy.applyOptionalValue(this::classLoaderSupplier, other::classLoaderSupplier, this::classLoaderSupplier);
            assignmentPolicy.applyOptionalValue(this::executorOptions, other::executorOptions, this::executorOptions);
            assignmentPolicy.applyOptionalValue(this::recover, other::recover, this::recover);
            assignmentPolicy.applyOptionalValue(this::reset, other::reset, this::reset);
            assignmentPolicy.applyOptionalValue(this::resetHard, other::resetHard, this::resetHard);
            assignmentPolicy.applyOptionalValue(this::commandVersion, other::commandVersion, this::commandVersion);
            assignmentPolicy.applyOptionalValue(this::commandHelp, other::commandHelp, this::commandHelp);
            assignmentPolicy.applyOptionalValue(this::debug, other::debug, this::debug);
            assignmentPolicy.applyOptionalValue(this::inherited, other::inherited, this::inherited);
            assignmentPolicy.applyOptionalValue(this::executionType, other::executionType, this::executionType);
            assignmentPolicy.applyOptionalValue(this::runAs, other::runAs, this::runAs);
            assignmentPolicy.applyOptionalValue(this::archetype, other::archetype, this::archetype);
            assignmentPolicy.applyOptionalValue(this::storeStrategy, other::storeStrategy, this::storeStrategy);
            assignmentPolicy.applyOptionalValue(this::homeLocations, other::homeLocations, this::homeLocations);
            assignmentPolicy.applyOptionalValue(this::storeLocations, other::storeLocations, this::storeLocations);
            assignmentPolicy.applyOptionalValue(this::storeLayout, other::storeLayout, this::storeLayout);
            assignmentPolicy.applyOptionalValue(this::storeStrategy, other::storeStrategy, this::storeStrategy);
            assignmentPolicy.applyOptionalValue(this::repositoryStoreStrategy, other::repositoryStoreStrategy, this::repositoryStoreStrategy);
            assignmentPolicy.applyOptionalValue(this::fetchStrategy, other::fetchStrategy, this::fetchStrategy);
            assignmentPolicy.applyOptionalValue(this::cached, other::cached, this::cached);
            assignmentPolicy.applyOptionalValue(this::indexed, other::indexed, this::indexed);
            assignmentPolicy.applyOptionalValue(this::transitive, other::transitive, this::transitive);
            assignmentPolicy.applyOptionalValue(this::bot, other::bot, this::bot);
            assignmentPolicy.applyOptionalValue(this::stdin, other::stdin, this::stdin);
            assignmentPolicy.applyOptionalValue(this::stdout, other::stdout, this::stdout);
            assignmentPolicy.applyOptionalValue(this::stderr, other::stderr, this::stderr);
            assignmentPolicy.applyOptionalValue(this::executorService, other::executorService, this::executorService);
            assignmentPolicy.applyOptionalValue(this::excludedExtensions, other::excludedExtensions, this::excludedExtensions);
            assignmentPolicy.applyOptionalValue(this::repositories, other::repositories, this::repositories);
            assignmentPolicy.applyOptionalValue(this::bootRepositories, other::bootRepositories, this::bootRepositories);
            assignmentPolicy.applyOptionalValue(this::applicationArguments, other::applicationArguments, this::applicationArguments);
            assignmentPolicy.applyOptionalValue(this::customOptions, other::customOptions, this::customOptions);
            assignmentPolicy.applyOptionalValue(this::expireTime, other::expireTime, this::expireTime);
            assignmentPolicy.applyOptionalValue(this::errors, other::errors, this::errors);
            assignmentPolicy.applyOptionalValue(this::skipErrors, other::skipErrors, this::skipErrors);
            assignmentPolicy.applyOptionalValue(this::switchWorkspace, other::switchWorkspace, this::switchWorkspace);
            assignmentPolicy.applyOptionalValue(this::locale, other::locale, this::locale);
            assignmentPolicy.applyOptionalValue(this::theme, other::theme, this::theme);
            assignmentPolicy.applyOptionalValue(this::dependencySolver, other::dependencySolver, this::dependencySolver);
            assignmentPolicy.applyOptionalValue(this::isolationLevel, other::isolationLevel, this::isolationLevel);
            assignmentPolicy.applyOptionalValue(this::initLaunchers, other::initLaunchers, this::initLaunchers);
            assignmentPolicy.applyOptionalValue(this::initJava, other::initJava, this::initJava);
            assignmentPolicy.applyOptionalValue(this::localMaven, other::localMaven, this::localMaven);
            assignmentPolicy.applyOptionalValue(this::initScripts, other::initScripts, this::initScripts);
            assignmentPolicy.applyOptionalValue(this::initLaunchers, other::initLaunchers, this::initLaunchers);
            assignmentPolicy.applyOptionalValue(this::desktopLauncher, other::desktopLauncher, this::desktopLauncher);
            assignmentPolicy.applyOptionalValue(this::menuLauncher, other::menuLauncher, this::menuLauncher);
            assignmentPolicy.applyOptionalValue(this::userLauncher, other::userLauncher, this::userLauncher);
            assignmentPolicy.applyOptionalValue(this::previewRepo, other::previewRepo, this::previewRepo);
            assignmentPolicy.applyOptionalValue(this::sharedInstance, other::sharedInstance, this::sharedInstance);
        }
        return this;
    }
    public NWorkspaceOptionsBuilder setCmdLine(String cmdLine) {
        setCmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
        return this;
    }
    public NWorkspaceOptionsBuilder setCmdLine(String[] args) {
        NWorkspaceCmdLineParser.parseNutsArguments(args, this);
        return this;
    }
    public NOptional<Boolean> sharedInstance() {
        return identityState.sharedInstance();
    }
    public NWorkspaceOptionsBuilder sharedInstance(Boolean sharedInstance) {
        return identityState.sharedInstance(sharedInstance);
    }
    public NWorkspaceOptionsBuilder setUserName(String username) {
        return identityState.setUserName(username);
    }
    public NWorkspaceOptionsBuilder setStoreLocation(NStoreType location, String value) {
        return workspaceState.setStoreLocation(location, value);
    }
    public NWorkspaceOptionsBuilder setHomeLocation(NHomeLocation location, String value) {
        return workspaceState.setHomeLocation(location, value);
    }
    public NWorkspaceOptionsBuilder addOutputFormatOptions(String... options) {
        return identityState.addOutputFormatOptions(options);
    }
    public NOptional<String> dependencySolver() {
        return identityState.dependencySolver();
    }
    public NWorkspaceOptionsBuilder dependencySolver(String dependencySolver) {
        return identityState.dependencySolver(dependencySolver);
    }
    public String toString() {
        return toCmdLine().toString();
    }
    public NWorkspaceOptions build() {
        return new DefaultNWorkspaceOptions(
                identityState.apiVersion().orNull(), identityState.runtimeId().orNull(), identityState.workspace().orNull(),
                identityState.name().orNull(), identityState.javaCommand().orNull(), identityState.javaOptions().orNull(),
                identityState.outLinePrefix().orNull(), identityState.errLinePrefix().orNull(), identityState.userName().orNull(),
                credential().orNull(), identityState.progressOptions().orNull(), identityState.dependencySolver().orNull(),
                identityState.debug().orNull(), workspaceState.archetype().orNull(), identityState.locale().orNull(), identityState.theme().orNull(),
                executionState.logConfig().orNull(), executionState.confirm().orNull(), executionState.outputFormat().orNull(), executionState.openMode().orNull(),
                executionState.executionType().orNull(), workspaceState.storeStrategy().orNull(), workspaceState.repositoryStoreStrategy().orNull(),
                workspaceState.storeLayout().orNull(), executionState.terminalMode().orNull(), executionState.fetchStrategy().orNull(),
                executionState.runAs().orNull(), identityState.creationTime().orNull(), executionState.expireTime().orNull(),
                workspaceState.installCompanions().orNull(), workspaceState.skipWelcome().orNull(), workspaceState.skipBoot().orNull(),
                workspaceState.system().orNull(), workspaceState.gui().orNull(), executionState.readOnly().orNull(), executionState.trace().orNull(),
                executionState.dry().orNull(), executionState.force().orNull(), executionState.showStacktrace().orNull(), executionState.recover().orNull(), executionState.reset().orNull(), executionState.resetHard().orNull(), executionState.commandVersion().orNull(),
                executionState.commandHelp().orNull(), executionState.commandHelp().orNull(), workspaceState.switchWorkspace().orNull(), executionState.cached().orNull(),
                executionState.indexed().orNull(), executionState.transitive().orNull(), executionState.bot().orNull(), executionState.skipErrors().orNull(),
                executionState.isolationLevel().orNull(), executionState.initLaunchers().orNull(), executionState.initScripts().orNull(), executionState.initPlatforms().orNull(),
                executionState.initJava().orNull(), executionState.localMaven().orNull(), executionState.stdin().orNull(), executionState.stdout().orNull(), executionState.stdout().orNull(), executionState.executorService().orNull(),
                executionState.classLoaderSupplier().orNull(), executionState.applicationArguments().orNull(), identityState.outputFormatOptions().orNull(),
                identityState.customOptions().orNull(), workspaceState.excludedExtensions().orNull(), workspaceState.repositories().orNull(), workspaceState.bootRepositories().orNull(),
                executionState.executorOptions().orNull(), executionState.errors().orNull(), workspaceState.storeLocations().orNull(), workspaceState.homeLocations().orNull(),
                executionState.desktopLauncher().orNull(), executionState.menuLauncher().orNull(), executionState.userLauncher().orNull()
                , executionState.previewRepo().orNull()
                , identityState.sharedInstance().orNull()
        );
    }
    public NWorkspaceOptionsBuilder builder() {
        return new DefaultNWorkspaceOptionsBuilder().copyFrom(this);
    }
    public NCmdLine toCmdLine() {
        return build().toCmdLine();
    }
    public NCmdLine toCmdLine(NWorkspaceOptionsConfig config) {
        return build().toCmdLine(config);
    }
    public NOptional<NIsolationLevel> isolationLevel() {
        return executionState.isolationLevel();
    }
    public NOptional<Boolean> initLaunchers() {
        return executionState.initLaunchers();
    }
    public NOptional<Boolean> initScripts() {
        return executionState.initScripts();
    }
    public NOptional<Boolean> initPlatforms() {
        return executionState.initPlatforms();
    }
    public NOptional<Boolean> localMaven() {
        return executionState.localMaven();
    }
    public NOptional<Boolean> initJava() {
        return executionState.initJava();
    }
    public NWorkspaceOptionsBuilder unsetRuntimeOptions() {
        return executionState.unsetRuntimeOptions();
    }
    public NWorkspaceOptionsBuilder unsetCreationOptions() {
        identityState.name(null);
        workspaceState.archetype(null);
        workspaceState.storeLayout(null);
        workspaceState.storeStrategy(null);
        workspaceState.repositoryStoreStrategy(null);
        workspaceState.storeLocations(null);
        workspaceState.homeLocations(null);
        workspaceState.switchWorkspace(null);
        workspaceState.bootRepositories(null);
        return this;
    }
    public NWorkspaceOptionsBuilder unsetExportedOptions() {
        identityState.javaCommand(null);
        identityState.javaOptions(null);
        identityState.workspace(null);
        setUserName(null);
        credential(null);
        identityState.apiVersion(null);
        identityState.runtimeId(null);
        executionState.terminalMode(null);
        executionState.logConfig(null);
        workspaceState.excludedExtensions(null);
        workspaceState.repositories(null);
        workspaceState.system(null);
        workspaceState.gui(null);
        executionState.readOnly(null);
        executionState.trace(null);
        identityState.progressOptions(null);
        identityState.dependencySolver(null);
        identityState.debug(null);
        workspaceState.installCompanions(null);
        workspaceState.skipWelcome(null);
        workspaceState.skipBoot(null);
        identityState.outLinePrefix(null);
        identityState.errLinePrefix(null);
        executionState.cached(null);
        executionState.indexed(null);
        executionState.transitive(null);
        executionState.bot(null);
        executionState.force(null);
        executionState.fetchStrategy(null);
        executionState.confirm(null);
        executionState.outputFormat(null);
        identityState.outputFormatOptions((List<String>) null);
        executionState.expireTime(null);
        identityState.theme(null);
        identityState.locale(null);
        executionState.initLaunchers(null);
        executionState.initPlatforms(null);
        executionState.initScripts(null);
        executionState.initJava(null);
        executionState.desktopLauncher(null);
        executionState.menuLauncher(null);
        executionState.userLauncher(null);
        return this;
    }
}
