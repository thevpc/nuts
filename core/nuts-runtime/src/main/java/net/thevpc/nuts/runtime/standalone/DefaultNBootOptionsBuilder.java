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
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
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

    private final DefaultNBootOptionsBuilderIdentityState identityState;
    private final DefaultNBootOptionsBuilderExecutionState executionState;
    private final DefaultNBootOptionsBuilderWorkspaceState workspaceState;
    private final DefaultNBootOptionsBuilderClassWorldState classworldState;

    public DefaultNBootOptionsBuilder() {
        this.identityState = new DefaultNBootOptionsBuilderIdentityState(this);
        this.executionState = new DefaultNBootOptionsBuilderExecutionState(this);
        this.workspaceState = new DefaultNBootOptionsBuilderWorkspaceState(this);
        this.classworldState = new DefaultNBootOptionsBuilderClassWorldState(this);
    }
    public DefaultNBootOptionsBuilder(NBootOptionsInfo other) {
        this.identityState = new DefaultNBootOptionsBuilderIdentityState(this);
        this.executionState = new DefaultNBootOptionsBuilderExecutionState(this);
        this.workspaceState = new DefaultNBootOptionsBuilderWorkspaceState(this);
        this.classworldState = new DefaultNBootOptionsBuilderClassWorldState(this);
        copyFrom(other);
    }
    public NBootOptionsBuilder copy() {
        return new DefaultNBootOptionsBuilder().copyFrom(this);
    }
    public NBootOptions build() {
        return new DefaultNBootOptions(
                identityState.outputFormatOptions().orNull(), identityState.customOptions().orNull(), identityState.apiVersion().orNull(), identityState.runtimeId().orNull(), identityState.javaCommand().orNull(),
                identityState.javaOptions().orNull(), identityState.workspace().orNull(), identityState.outLinePrefix().orNull(), identityState.errLinePrefix().orNull(),
                identityState.name().orNull(), workspaceState.installCompanions().orNull(), workspaceState.skipWelcome().orNull(), workspaceState.skipBoot().orNull(),
                workspaceState.system().orNull(), workspaceState.gui().orNull(), executionState.dry().orNull(), executionState.force().orNull(), executionState.showStacktrace().orNull(), executionState.recover().orNull(), executionState.reset().orNull(), executionState.resetHard().orNull(), executionState.commandVersion().orNull(), executionState.commandHelp().orNull(), executionState.commandHelp().orNull(), workspaceState.switchWorkspace().orNull(), executionState.cached().orNull(), executionState.cached().orNull(), executionState.transitive().orNull(), executionState.bot().orNull(),
                executionState.isolationLevel().orNull(), executionState.initLaunchers().orNull(), executionState.initScripts().orNull(), executionState.initPlatforms().orNull(),
                executionState.initJava().orNull(), executionState.localMaven().orNull(), workspaceState.excludedExtensions().orNull(), workspaceState.repositories().orNull(), identityState.userName().orNull(),
                credential().orNull(), executionState.terminalMode().orNull(), executionState.readOnly().orNull(), executionState.trace().orNull(), identityState.progressOptions().orNull(),
                identityState.dependencySolver().orNull(), executionState.logConfig().orNull(), executionState.confirm().orNull(), executionState.outputFormat().orNull(),
                executionState.applicationArguments().orNull(), executionState.openMode().orNull(), identityState.creationTime().orNull(),
                executionState.classLoaderSupplier().orNull(), executionState.executorOptions().orNull(),
                identityState.debug().orNull(),
                executionState.executionType().orNull(), executionState.runAs().orNull(), workspaceState.archetype().orNull(),
                workspaceState.storeLocations().orNull(), workspaceState.homeLocations().orNull(), workspaceState.storeLayout().orNull(), workspaceState.storeStrategy().orNull(),
                workspaceState.repositoryStoreStrategy().orNull(), executionState.fetchStrategy().orNull(),
                executionState.stdin().orNull(), executionState.stdout().orNull(), executionState.stderr().orNull(),
                executionState.executorService().orNull(), executionState.expireTime().orNull(), executionState.errors().orNull(), executionState.skipErrors().orNull(), identityState.locale().orNull(),
                identityState.theme().orNull(), identityState.uuid().orNull(), workspaceState.bootRepositories().orNull(), classworldState.runtimeBootDependencyNode().orNull(),
                classworldState.classWorldURLs().orNull(), classworldState.bootWorkspaceFactory().orNull(), classworldState.runtimeBootDescriptor().orNull(), classworldState.classWorldLoader().orNull(),
                executionState.desktopLauncher().orNull(), executionState.menuLauncher().orNull(), executionState.userLauncher().orNull(), executionState.previewRepo().orNull(), identityState.sharedInstance().orNull());
    }
    public NBootOptionsBuilder copyFrom(NWorkspaceOptions other) {
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
        identityState.userName(other.userName().orNull());
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
    public NBootOptionsBuilder copyFrom(NBootOptions other) {
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
        identityState.userName(other.userName().orNull());
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
        workspaceState.bootRepositories(other.bootRepositories().orNull());
        classworldState.runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        classworldState.bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        classworldState.classWorldURLs(other.classWorldURLs().orNull());
        classworldState.classWorldLoader(other.classWorldLoader().orNull());
        identityState.uuid(other.uuid().orNull());
        classworldState.runtimeBootDescriptor(other.runtimeBootDescriptor().orNull());
        return this;
    }
    public NBootOptionsBuilder copyFrom(NBootOptionsBuilder other) {
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
        identityState.userName(other.userName().orNull());
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
        workspaceState.excludedExtensions(other.excludedExtensions().orNull());
        workspaceState.repositories(other.repositories().orNull());
        workspaceState.bootRepositories(other.bootRepositories().orNull());
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
        classworldState.runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        classworldState.bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        classworldState.classWorldURLs(other.classWorldURLs().orNull());
        classworldState.classWorldLoader(other.classWorldLoader().orNull());
        identityState.uuid(other.uuid().orNull());
        classworldState.runtimeBootDescriptor(other.runtimeBootDescriptor().orNull());
        return this;
    }
    public NOptional<NClassLoaderNode> runtimeBootDependencyNode() {
        return classworldState.runtimeBootDependencyNode();
    }
    public DefaultNBootOptionsBuilder runtimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode) {
        return classworldState.runtimeBootDependencyNode(runtimeBootDependencyNode);
    }
    public NOptional<NBootWorkspaceFactory> bootWorkspaceFactory() {
        return classworldState.bootWorkspaceFactory();
    }
    public DefaultNBootOptionsBuilder bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        return classworldState.bootWorkspaceFactory(bootWorkspaceFactory);
    }
    public NOptional<List<URL>> classWorldURLs() {
        return classworldState.classWorldURLs();
    }
    public DefaultNBootOptionsBuilder classWorldURLs(List<URL> classWorldURLs) {
        return classworldState.classWorldURLs(classWorldURLs);
    }
    public NOptional<ClassLoader> classWorldLoader() {
        return classworldState.classWorldLoader();
    }
    public DefaultNBootOptionsBuilder classWorldLoader(ClassLoader classWorldLoader) {
        return classworldState.classWorldLoader(classWorldLoader);
    }
    public NOptional<String> uuid() {
        return identityState.uuid();
    }
    public DefaultNBootOptionsBuilder uuid(String uuid) {
        return identityState.uuid(uuid);
    }
    public NOptional<NBootDescriptor> runtimeBootDescriptor() {
        return classworldState.runtimeBootDescriptor();
    }
    public DefaultNBootOptionsBuilder runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        return classworldState.runtimeBootDescriptor(runtimeBootDescriptor);
    }
    public NBootOptionsBuilder copyFromIfPresent(NWorkspaceOptions other) {
        if (other.apiVersion().isPresent()) {
            identityState.apiVersion(other.apiVersion().orNull());
        }
        if (other.runtimeId().isPresent()) {
            identityState.runtimeId(other.runtimeId().orNull());
        }
        if (other.javaCommand().isPresent()) {
            identityState.javaCommand(other.javaCommand().orNull());
        }
        if (other.javaOptions().isPresent()) {
            identityState.javaOptions(other.javaOptions().orNull());
        }
        if (other.workspace().isPresent()) {
            identityState.workspace(other.workspace().orNull());
        }
        if (other.name().isPresent()) {
            identityState.name(other.name().orNull());
        }
        if (other.installCompanions().isPresent()) {
            workspaceState.installCompanions(other.installCompanions().orNull());
        }
        if (other.skipWelcome().isPresent()) {
            workspaceState.skipWelcome(other.skipWelcome().orNull());
        }
        if (other.skipBoot().isPresent()) {
            workspaceState.skipBoot(other.skipBoot().orNull());
        }
        if (other.system().isPresent()) {
            workspaceState.system(other.system().orNull());
        }
        if (other.gui().isPresent()) {
            workspaceState.gui(other.gui().orNull());
        }
        if (other.userName().isPresent()) {
            identityState.userName(other.userName().orNull());
        }
        if (other.credential().isPresent()) {
            this.credential(other.credential().orNull());
        }
        if (other.terminalMode().isPresent()) {
            executionState.terminalMode(other.terminalMode().orNull());
        }
        if (other.readOnly().isPresent()) {
            executionState.readOnly(other.readOnly().orNull());
        }
        if (other.trace().isPresent()) {
            executionState.trace(other.trace().orNull());
        }
        if (other.progressOptions().isPresent()) {
            identityState.progressOptions(other.progressOptions().orNull());
        }
        if (other.logConfig().isPresent()) {
            executionState.logConfig(other.logConfig().orNull());
        }
        if (other.confirm().isPresent()) {
            executionState.confirm(other.confirm().orNull());
        }
        if (other.confirm().isPresent()) {
            executionState.confirm(other.confirm().orNull());
        }
        if (other.outputFormat().isPresent()) {
            executionState.outputFormat(other.outputFormat().orNull());
        }
        if (other.outputFormatOptions().isPresent()) {
            identityState.outputFormatOptions(other.outputFormatOptions().orNull());
        }
        if (other.openMode().isPresent()) {
            executionState.openMode(other.openMode().orNull());
        }
        if (other.creationTime().isPresent()) {
            identityState.creationTime(other.creationTime().orNull());
        }
        if (other.dry().isPresent()) {
            executionState.dry(other.dry().orNull());
        }
        if (other.force().isPresent()) {
            executionState.force(other.force().orNull());
        }
        if (other.showStacktrace().isPresent()) {
            executionState.showStacktrace(other.showStacktrace().orNull());
        }
        if (other.classLoaderSupplier().isPresent()) {
            executionState.classLoaderSupplier(other.classLoaderSupplier().orNull());
        }
        if (other.executorOptions().isPresent()) {
            executionState.executorOptions(other.executorOptions().orNull());
        }
        if (other.recover().isPresent()) {
            executionState.recover(other.recover().orNull());
        }
        if (other.reset().isPresent()) {
            executionState.reset(other.reset().orNull());
        }
        if (other.resetHard().isPresent()) {
            executionState.resetHard(other.resetHard().orNull());
        }
        if (other.commandVersion().isPresent()) {
            executionState.commandVersion(other.commandVersion().orNull());
        }
        if (other.commandHelp().isPresent()) {
            executionState.commandHelp(other.commandHelp().orNull());
        }
        if (other.debug().isPresent()) {
            identityState.debug(other.debug().orNull());
        }
        if (other.inherited().isPresent()) {
            identityState.inherited(other.inherited().orNull());
        }
        if (other.executionType().isPresent()) {
            executionState.executionType(other.executionType().orNull());
        }
        if (other.runAs().isPresent()) {
            executionState.runAs(other.runAs().orNull());
        }
        if (other.archetype().isPresent()) {
            workspaceState.archetype(other.archetype().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            workspaceState.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.homeLocations().isPresent()) {
            workspaceState.homeLocations(other.homeLocations().orNull());
        }

        if (other.storeLocations().isPresent()) {
            workspaceState.storeLocations(other.storeLocations().orNull());
        }
        if (other.storeLayout().isPresent()) {
            workspaceState.storeLayout(other.storeLayout().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            workspaceState.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.repositoryStoreStrategy().isPresent()) {
            workspaceState.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        }
        if (other.fetchStrategy().isPresent()) {
            executionState.fetchStrategy(other.fetchStrategy().orNull());
        }
        if (other.cached().isPresent()) {
            executionState.cached(other.cached().orNull());
        }
        if (other.indexed().isPresent()) {
            executionState.indexed(other.indexed().orNull());
        }
        if (other.transitive().isPresent()) {
            executionState.transitive(other.transitive().orNull());
        }
        if (other.bot().isPresent()) {
            executionState.bot(other.bot().orNull());
        }
        if (other.stdin().isPresent()) {
            executionState.stdin(other.stdin().orNull());
        }
        if (other.stdout().isPresent()) {
            executionState.stdout(other.stdout().orNull());
        }
        if (other.stderr().isPresent()) {
            executionState.stderr(other.stderr().orNull());
        }
        if (other.executorService().isPresent()) {
            executionState.executorService(other.executorService().orNull());
        }
        if (other.excludedExtensions().isPresent()) {
            workspaceState.excludedExtensions(other.excludedExtensions().orNull());
        }
        if (other.repositories().isPresent()) {
            workspaceState.repositories(other.repositories().orNull());
        }
        if (other.applicationArguments().isPresent()) {
            executionState.applicationArguments(other.applicationArguments().orNull());
        }
        if (other.customOptions().isPresent()) {
            identityState.customOptions(other.customOptions().orNull());
        }
        if (other.expireTime().isPresent()) {
            executionState.expireTime(other.expireTime().orNull());
        }
        if (other.errors().isPresent()) {
            executionState.errors(other.errors().orNull());
        }
        if (other.skipErrors().isPresent()) {
            executionState.skipErrors(other.skipErrors().orNull());
        }
        if (other.switchWorkspace().isPresent()) {
            workspaceState.switchWorkspace(other.switchWorkspace().orNull());
        }
        if (other.locale().isPresent()) {
            identityState.locale(other.locale().orNull());
        }
        if (other.theme().isPresent()) {
            identityState.theme(other.theme().orNull());
        }
        if (other.dependencySolver().isPresent()) {
            identityState.dependencySolver(other.dependencySolver().orNull());
        }
        if (other.isolationLevel().isPresent()) {
            executionState.isolationLevel(other.isolationLevel().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            executionState.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.initJava().isPresent()) {
            executionState.initJava(other.initJava().orNull());
        executionState.localMaven(other.localMaven().orNull());
        }
        if (other.initScripts().isPresent()) {
            executionState.initScripts(other.initScripts().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            executionState.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.desktopLauncher().isPresent()) {
            executionState.desktopLauncher(other.desktopLauncher().orNull());
        }
        if (other.menuLauncher().isPresent()) {
            executionState.menuLauncher(other.menuLauncher().orNull());
        }
        if (other.userLauncher().isPresent()) {
            executionState.userLauncher(other.userLauncher().orNull());
        }
        if (other.previewRepo().isPresent()) {
            executionState.previewRepo(other.previewRepo().orNull());
        }
        if (other.sharedInstance().isPresent()) {
            identityState.sharedInstance(other.sharedInstance().orNull());
        }
        return this;
    }
    public NBootOptionsBuilder copyFromIfPresent(NBootOptions other) {
        if (other.apiVersion().isPresent()) {
            identityState.apiVersion(other.apiVersion().orNull());
        }
        if (other.runtimeId().isPresent()) {
            identityState.runtimeId(other.runtimeId().orNull());
        }
        if (other.javaCommand().isPresent()) {
            identityState.javaCommand(other.javaCommand().orNull());
        }
        if (other.javaOptions().isPresent()) {
            identityState.javaOptions(other.javaOptions().orNull());
        }
        if (other.workspace().isPresent()) {
            identityState.workspace(other.workspace().orNull());
        }
        if (other.name().isPresent()) {
            identityState.name(other.name().orNull());
        }
        if (other.installCompanions().isPresent()) {
            workspaceState.installCompanions(other.installCompanions().orNull());
        }
        if (other.skipWelcome().isPresent()) {
            workspaceState.skipWelcome(other.skipWelcome().orNull());
        }
        if (other.skipBoot().isPresent()) {
            workspaceState.skipBoot(other.skipBoot().orNull());
        }
        if (other.system().isPresent()) {
            workspaceState.system(other.system().orNull());
        }
        if (other.gui().isPresent()) {
            workspaceState.gui(other.gui().orNull());
        }
        if (other.userName().isPresent()) {
            identityState.userName(other.userName().orNull());
        }
        if (other.credential().isPresent()) {
            this.credential(other.credential().orNull());
        }
        if (other.terminalMode().isPresent()) {
            executionState.terminalMode(other.terminalMode().orNull());
        }
        if (other.readOnly().isPresent()) {
            executionState.readOnly(other.readOnly().orNull());
        }
        if (other.trace().isPresent()) {
            executionState.trace(other.trace().orNull());
        }
        if (other.progressOptions().isPresent()) {
            identityState.progressOptions(other.progressOptions().orNull());
        }
        if (other.logConfig().isPresent()) {
            executionState.logConfig(other.logConfig().orNull());
        }
        if (other.confirm().isPresent()) {
            executionState.confirm(other.confirm().orNull());
        }
        if (other.confirm().isPresent()) {
            executionState.confirm(other.confirm().orNull());
        }
        if (other.outputFormat().isPresent()) {
            executionState.outputFormat(other.outputFormat().orNull());
        }
        if (other.outputFormatOptions().isPresent()) {
            identityState.outputFormatOptions(other.outputFormatOptions().orNull());
        }
        if (other.openMode().isPresent()) {
            executionState.openMode(other.openMode().orNull());
        }
        if (other.creationTime().isPresent()) {
            identityState.creationTime(other.creationTime().orNull());
        }
        if (other.dry().isPresent()) {
            executionState.dry(other.dry().orNull());
        }
        if (other.force().isPresent()) {
            executionState.force(other.force().orNull());
        }
        if (other.showStacktrace().isPresent()) {
            executionState.showStacktrace(other.showStacktrace().orNull());
        }
        if (other.classLoaderSupplier().isPresent()) {
            executionState.classLoaderSupplier(other.classLoaderSupplier().orNull());
        }
        if (other.executorOptions().isPresent()) {
            executionState.executorOptions(other.executorOptions().orNull());
        }
        if (other.recover().isPresent()) {
            executionState.recover(other.recover().orNull());
        }
        if (other.reset().isPresent()) {
            executionState.reset(other.reset().orNull());
        }
        if (other.resetHard().isPresent()) {
            executionState.resetHard(other.resetHard().orNull());
        }
        if (other.commandVersion().isPresent()) {
            executionState.commandVersion(other.commandVersion().orNull());
        }
        if (other.commandHelp().isPresent()) {
            executionState.commandHelp(other.commandHelp().orNull());
        }
        if (other.debug().isPresent()) {
            identityState.debug(other.debug().orNull());
        }
        if (other.inherited().isPresent()) {
            identityState.inherited(other.inherited().orNull());
        }
        if (other.executionType().isPresent()) {
            executionState.executionType(other.executionType().orNull());
        }
        if (other.runAs().isPresent()) {
            executionState.runAs(other.runAs().orNull());
        }
        if (other.archetype().isPresent()) {
            workspaceState.archetype(other.archetype().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            workspaceState.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.homeLocations().isPresent()) {
            workspaceState.homeLocations(other.homeLocations().orNull());
        }

        if (other.storeLocations().isPresent()) {
            workspaceState.storeLocations(other.storeLocations().orNull());
        }
        if (other.storeLayout().isPresent()) {
            workspaceState.storeLayout(other.storeLayout().orNull());
        }
        if (other.storeStrategy().isPresent()) {
            workspaceState.storeStrategy(other.storeStrategy().orNull());
        }
        if (other.repositoryStoreStrategy().isPresent()) {
            workspaceState.repositoryStoreStrategy(other.repositoryStoreStrategy().orNull());
        }
        if (other.fetchStrategy().isPresent()) {
            executionState.fetchStrategy(other.fetchStrategy().orNull());
        }
        if (other.cached().isPresent()) {
            executionState.cached(other.cached().orNull());
        }
        if (other.indexed().isPresent()) {
            executionState.indexed(other.indexed().orNull());
        }
        if (other.transitive().isPresent()) {
            executionState.transitive(other.transitive().orNull());
        }
        if (other.bot().isPresent()) {
            executionState.bot(other.bot().orNull());
        }
        if (other.stdin().isPresent()) {
            executionState.stdin(other.stdin().orNull());
        }
        if (other.stdout().isPresent()) {
            executionState.stdout(other.stdout().orNull());
        }
        if (other.stderr().isPresent()) {
            executionState.stderr(other.stderr().orNull());
        }
        if (other.executorService().isPresent()) {
            executionState.executorService(other.executorService().orNull());
        }
        if (other.excludedExtensions().isPresent()) {
            workspaceState.excludedExtensions(other.excludedExtensions().orNull());
        }
        if (other.repositories().isPresent()) {
            workspaceState.repositories(other.repositories().orNull());
        }
        if (other.applicationArguments().isPresent()) {
            executionState.applicationArguments(other.applicationArguments().orNull());
        }
        if (other.customOptions().isPresent()) {
            identityState.customOptions(other.customOptions().orNull());
        }
        if (other.expireTime().isPresent()) {
            executionState.expireTime(other.expireTime().orNull());
        }
        if (other.errors().isPresent()) {
            executionState.errors(other.errors().orNull());
        }
        if (other.skipErrors().isPresent()) {
            executionState.skipErrors(other.skipErrors().orNull());
        }
        if (other.switchWorkspace().isPresent()) {
            workspaceState.switchWorkspace(other.switchWorkspace().orNull());
        }
        if (other.locale().isPresent()) {
            identityState.locale(other.locale().orNull());
        }
        if (other.theme().isPresent()) {
            identityState.theme(other.theme().orNull());
        }
        if (other.dependencySolver().isPresent()) {
            identityState.dependencySolver(other.dependencySolver().orNull());
        }
        if (other.isolationLevel().isPresent()) {
            executionState.isolationLevel(other.isolationLevel().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            executionState.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.initJava().isPresent()) {
            executionState.initJava(other.initJava().orNull());
        executionState.localMaven(other.localMaven().orNull());
        }
        if (other.initScripts().isPresent()) {
            executionState.initScripts(other.initScripts().orNull());
        }
        if (other.initLaunchers().isPresent()) {
            executionState.initLaunchers(other.initLaunchers().orNull());
        }
        if (other.desktopLauncher().isPresent()) {
            executionState.desktopLauncher(other.desktopLauncher().orNull());
        }
        if (other.menuLauncher().isPresent()) {
            executionState.menuLauncher(other.menuLauncher().orNull());
        }
        if (other.userLauncher().isPresent()) {
            executionState.userLauncher(other.userLauncher().orNull());
        }
        if (other.previewRepo().isPresent()) {
            executionState.previewRepo(other.previewRepo().orNull());
        }
        if (other.sharedInstance().isPresent()) {
            identityState.sharedInstance(other.sharedInstance().orNull());
        }
        if (other.bootRepositories().isPresent()) {
            workspaceState.bootRepositories(other.bootRepositories().orNull());
        }
        if (other.runtimeBootDependencyNode().isPresent()) {
            classworldState.runtimeBootDependencyNode(other.runtimeBootDependencyNode().orNull());
        }
        if (other.bootWorkspaceFactory().isPresent()) {
            classworldState.bootWorkspaceFactory(other.bootWorkspaceFactory().orNull());
        }
        if (other.classWorldURLs().isPresent()) {
            classworldState.classWorldURLs(other.classWorldURLs().orNull());
        }
        if (other.classWorldLoader().isPresent()) {
            classworldState.classWorldLoader(other.classWorldLoader().orNull());
        }
        if (other.uuid().isPresent()) {
            identityState.uuid(other.uuid().orNull());
        }
        return this;
    }
    public NBootOptionsInfo toBootOptions() {
        NBootOptionsInfo r = new NBootOptionsInfo();
        r.apiVersion(identityState.apiVersion().map(x -> x.toString()).orNull());
        r.runtimeId(identityState.runtimeId().map(x -> x.toString()).orNull());
        r.javaCommand(identityState.javaCommand().orNull());
        r.javaOptions(identityState.javaOptions().orNull());
        r.workspace(identityState.workspace().orNull());
        r.name(identityState.name().orNull());
        r.installCompanions(workspaceState.installCompanions().orNull());
        r.skipWelcome(workspaceState.skipWelcome().orNull());
        r.skipBoot(workspaceState.skipBoot().orNull());
        r.system(workspaceState.system().orNull());
        r.gui(workspaceState.gui().orNull());
        r.userName(identityState.userName().orNull());
        r.credential(this.credential().orNull());
        r.terminalMode(executionState.terminalMode().map(x -> x.id()).orNull());
        r.readOnly(executionState.readOnly().orNull());
        r.trace(executionState.trace().orNull());
        r.progressOptions(identityState.progressOptions().orNull());
        {
            NLogConfig c = executionState.logConfig().orNull();
            NBootLogConfig v = null;
            if (c != null) {
                v = new NBootLogConfig();
                v.logFileBase(c.logFileBase());
                v.logFileLevel(c.logFileLevel());
                v.logTermLevel(c.logTermLevel());
                v.logFileSize(c.logFileSize());
                v.logFileCount(c.logFileCount());
                v.logFileName(c.logFileName());
                v.logFileBase(c.logFileBase());
            }
            r.logConfig(v);
        }
        r.confirm(executionState.confirm().map(x -> x.id()).orNull());
        r.confirm(executionState.confirm().map(x -> x.id()).orNull());
        r.outputFormat(executionState.outputFormat().map(x -> x.id()).orNull());
        r.outputFormatOptions(identityState.outputFormatOptions().orNull());
        r.openMode(executionState.openMode().map(x -> x.id()).orNull());
        r.creationTime(identityState.creationTime().orNull());
        r.dry(executionState.dry().orNull());
        r.force(executionState.force().orNull());
        r.showStacktrace(executionState.showStacktrace().orNull());
        r.classLoaderSupplier(executionState.classLoaderSupplier().orNull());
        r.executorOptions(executionState.executorOptions().orNull());
        r.recover(executionState.recover().orNull());
        r.reset(executionState.reset().orNull());
        r.resetHard(executionState.resetHard().orNull());
        r.commandVersion(executionState.commandVersion().orNull());
        r.commandHelp(executionState.commandHelp().orNull());
        r.debug(identityState.debug().orNull());
        r.inherited(identityState.inherited().orNull());
        r.executionType(executionState.executionType().map(x -> x.id()).orNull());
        r.runAs(executionState.runAs().map(x -> x.toString()).orNull());
        r.archetype(workspaceState.archetype().orNull());
        r.storeStrategy(workspaceState.storeStrategy().map(x -> x.id()).orNull());
        {
            Map<NHomeLocation, String> c = workspaceState.homeLocations().orNull();
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
            r.homeLocations(v);
        }
        {
            Map<NStoreType, String> c = workspaceState.storeLocations().orNull();
            Map<String, String> v = null;
            if (c != null) {
                v = new HashMap<>();
                for (Map.Entry<NStoreType, String> e : c.entrySet()) {
                    v.put(e.getKey().id(), e.getValue());
                }
            }
            r.storeLocations(v);
        }
        r.storeLayout(workspaceState.storeLayout().map(x -> x.toString()).orNull());
        r.storeStrategy(workspaceState.storeStrategy().map(x -> x.toString()).orNull());
        r.repositoryStoreStrategy(workspaceState.repositoryStoreStrategy().map(x -> x.toString()).orNull());
        r.setchStrategy(executionState.fetchStrategy().map(x -> x.toString()).orNull());
        r.cached(executionState.cached().orNull());
        r.indexed(executionState.indexed().orNull());
        r.transitive(executionState.transitive().orNull());
        r.bot(executionState.bot().orNull());
        r.stdin(executionState.stdin().orNull());
        r.stdout(executionState.stdout().orNull());
        r.stderr(executionState.stderr().orNull());
        r.executorService(executionState.executorService().orNull());

        r.excludedExtensions(workspaceState.excludedExtensions().orNull());
        r.repositories(workspaceState.repositories().orNull());
        r.applicationArguments(executionState.applicationArguments().orNull());
        r.customOptions(identityState.customOptions().orNull());
        r.expireTime(executionState.expireTime().orNull());
        r.errors(executionState.errors().isNotPresent() ? new ArrayList<>() : executionState.errors().get().stream().map(x -> x.toString()).collect(Collectors.toList()));
        r.skipErrors(executionState.skipErrors().orNull());
        r.switchWorkspace(workspaceState.switchWorkspace().orNull());
        r.locale(identityState.locale().orNull());
        r.theme(identityState.theme().orNull());
        r.dependencySolver(identityState.dependencySolver().orNull());
        r.isolationLevel(executionState.isolationLevel().map(x -> x.id()).orNull());
        r.initLaunchers(executionState.initLaunchers().orNull());
        r.initJava(executionState.initJava().orNull());
        r.localMaven(executionState.localMaven().orNull());
        r.initScripts(executionState.initScripts().orNull());
        r.initPlatforms(executionState.initPlatforms().orNull());
        r.desktopLauncher(executionState.desktopLauncher().map(x -> x.id()).orNull());
        r.menuLauncher(executionState.menuLauncher().map(x -> x.id()).orNull());
        r.userLauncher(executionState.userLauncher().map(x -> x.id()).orNull());
        r.sharedInstance(identityState.sharedInstance().orNull());
        r.previewRepo(executionState.previewRepo().orNull());
        return r;
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
    public NBootOptionsBuilder initLaunchers(Boolean initLaunchers) {
        return executionState.initLaunchers(initLaunchers);
    }
    public NBootOptionsBuilder initScripts(Boolean initScripts) {
        return executionState.initScripts(initScripts);
    }
    public NBootOptionsBuilder initPlatforms(Boolean initPlatforms) {
        return executionState.initPlatforms(initPlatforms);
    }
    public NBootOptionsBuilder initJava(Boolean initJava) {
        return executionState.initJava(initJava);
    }
    public NBootOptionsBuilder localMaven(Boolean localMaven) {
        return executionState.localMaven(localMaven);
    }
    public NBootOptionsBuilder isolationLevel(NIsolationLevel isolationLevel) {
        return executionState.isolationLevel(isolationLevel);
    }
    public NBootOptionsBuilder desktopLauncher(NSupportMode desktopLauncher) {
        return executionState.desktopLauncher(desktopLauncher);
    }
    public NBootOptionsBuilder menuLauncher(NSupportMode menuLauncher) {
        return executionState.menuLauncher(menuLauncher);
    }
    public NBootOptionsBuilder userLauncher(NSupportMode userLauncher) {
        return executionState.userLauncher(userLauncher);
    }
    public NOptional<NVersion> apiVersion() {
        return identityState.apiVersion();
    }
    public NBootOptionsBuilder apiVersion(NVersion apiVersion) {
        return identityState.apiVersion(apiVersion);
    }
    public NOptional<List<String>> applicationArguments() {
        return executionState.applicationArguments();
    }
    public NBootOptionsBuilder applicationArguments(List<String> applicationArguments) {
        return executionState.applicationArguments(applicationArguments);
    }
    public NOptional<String> archetype() {
        return workspaceState.archetype();
    }
    public NBootOptionsBuilder archetype(String archetype) {
        return workspaceState.archetype(archetype);
    }
    public NOptional<Supplier<ClassLoader>> classLoaderSupplier() {
        return executionState.classLoaderSupplier();
    }
    public NBootOptionsBuilder classLoaderSupplier(Supplier<ClassLoader> provider) {
        return executionState.classLoaderSupplier(provider);
    }
    public NOptional<NConfirmationMode> confirm() {
        return executionState.confirm();
    }
    public NBootOptionsBuilder confirm(NConfirmationMode confirm) {
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
    public NBootOptionsBuilder dry(Boolean dry) {
        return executionState.dry(dry);
    }
    public NBootOptionsBuilder force(Boolean force) {
        return executionState.force(force);
    }
    public NBootOptionsBuilder showStacktrace(Boolean showStacktrace) {
        return executionState.showStacktrace(showStacktrace);
    }
    public NOptional<Instant> creationTime() {
        return identityState.creationTime();
    }
    public NBootOptionsBuilder creationTime(Instant creationTime) {
        return identityState.creationTime(creationTime);
    }
    public NOptional<List<String>> excludedExtensions() {
        return workspaceState.excludedExtensions();
    }
    public NBootOptionsBuilder excludedExtensions(List<String> excludedExtensions) {
        return workspaceState.excludedExtensions(excludedExtensions);
    }
    public NOptional<NExecutionType> executionType() {
        return executionState.executionType();
    }
    public NBootOptionsBuilder executionType(NExecutionType executionType) {
        return executionState.executionType(executionType);
    }
    public NOptional<NRunAs> runAs() {
        return executionState.runAs();
    }
    public NBootOptionsBuilder runAs(NRunAs runAs) {
        return executionState.runAs(runAs);
    }
    public NOptional<List<String>> executorOptions() {
        return executionState.executorOptions();
    }
    public NBootOptionsBuilder executorOptions(List<String> executorOptions) {
        return executionState.executorOptions(executorOptions);
    }
    public NOptional<String> getHomeLocation(NHomeLocation location) {
        return NOptional.ofNamed(workspaceState.homeLocations == null ? null : workspaceState.homeLocations.get(location), "workspaceState.homeLocations[" + location + "]");
    }
    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return workspaceState.homeLocations();
    }
    public NBootOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations) {
        return workspaceState.homeLocations(homeLocations);
    }
    public NOptional<String> javaCommand() {
        return identityState.javaCommand();
    }
    public NBootOptionsBuilder javaCommand(String javaCommand) {
        return identityState.javaCommand(javaCommand);
    }
    public NOptional<String> javaOptions() {
        return identityState.javaOptions();
    }
    public NBootOptionsBuilder javaOptions(String javaOptions) {
        return identityState.javaOptions(javaOptions);
    }
    public NOptional<NLogConfig> logConfig() {
        return executionState.logConfig();
    }
    public NBootOptionsBuilder logConfig(NLogConfig logConfig) {
        return executionState.logConfig(logConfig);
    }
    public NOptional<String> name() {
        return identityState.name();
    }
    public NBootOptionsBuilder name(String workspaceName) {
        return identityState.name(workspaceName);
    }
    public NOptional<NOpenMode> openMode() {
        return executionState.openMode();
    }
    public NBootOptionsBuilder openMode(NOpenMode openMode) {
        return executionState.openMode(openMode);
    }
    public NOptional<NContentType> outputFormat() {
        return executionState.outputFormat();
    }
    public NBootOptionsBuilder outputFormat(NContentType outputFormat) {
        return executionState.outputFormat(outputFormat);
    }
    public NOptional<List<String>> outputFormatOptions() {
        return identityState.outputFormatOptions();
    }
    public NBootOptionsBuilder outputFormatOptions(List<String> options) {
        return identityState.outputFormatOptions(options);
    }
    public NBootOptionsBuilder setOutputFormatOptions(String... options) {
        if (identityState.outputFormatOptions == null) {
            identityState.outputFormatOptions = new ArrayList<>();
        }
        identityState.outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }
    public NOptional<char[]> credential() {
        return identityState.credential();
    }
    public NBootOptionsBuilder credential(char[] credentials) {
        return identityState.credential(credentials);
    }
    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return workspaceState.repositoryStoreStrategy();
    }
    public NBootOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        return workspaceState.repositoryStoreStrategy(repositoryStoreStrategy);
    }
    public NOptional<NId> runtimeId() {
        return identityState.runtimeId();
    }
    public NBootOptionsBuilder runtimeId(NId runtimeId) {
        return identityState.runtimeId(runtimeId);
    }
    public NOptional<String> getStoreType(NStoreType folder) {
        return NOptional.ofNamed(workspaceState.storeLocations == null ? null : workspaceState.storeLocations.get(folder), "workspaceState.storeLocations[" + folder + "]");
    }
    public NOptional<NOsFamily> storeLayout() {
        return workspaceState.storeLayout();
    }
    public NBootOptionsBuilder storeLayout(NOsFamily storeLayout) {
        return workspaceState.storeLayout(storeLayout);
    }
    public NOptional<NStoreStrategy> storeStrategy() {
        return workspaceState.storeStrategy();
    }
    public NBootOptionsBuilder storeStrategy(NStoreStrategy storeStrategy) {
        return workspaceState.storeStrategy(storeStrategy);
    }
    public NOptional<Map<NStoreType, String>> storeLocations() {
        return workspaceState.storeLocations();
    }
    public NBootOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations) {
        return workspaceState.storeLocations(storeLocations);
    }
    public NOptional<NTerminalMode> terminalMode() {
        return executionState.terminalMode();
    }
    public NBootOptionsBuilder terminalMode(NTerminalMode terminalMode) {
        return executionState.terminalMode(terminalMode);
    }
    public NOptional<List<String>> repositories() {
        return workspaceState.repositories();
    }
    public NOptional<List<String>> bootRepositories() {
        return workspaceState.bootRepositories();
    }
    public NBootOptionsBuilder repositories(List<String> repositories) {
        return workspaceState.repositories(repositories);
    }
    public NBootOptionsBuilder bootRepositories(List<String> bootRepositories) {
        return workspaceState.bootRepositories(bootRepositories);
    }
    public NOptional<String> userName() {
        return identityState.userName();
    }
    public NOptional<String> workspace() {
        return identityState.workspace();
    }
    public NBootOptionsBuilder workspace(String workspace) {
        return identityState.workspace(workspace);
    }
    public NOptional<String> debug() {
        return identityState.debug();
    }
    public NBootOptionsBuilder debug(String debug) {
        return identityState.debug(debug);
    }
    public NOptional<Boolean> system() {
        return workspaceState.system();
    }
    public NBootOptionsBuilder system(Boolean system) {
        return workspaceState.system(system);
    }
    public NOptional<Boolean> gui() {
        return workspaceState.gui();
    }
    public NBootOptionsBuilder gui(Boolean gui) {
        return workspaceState.gui(gui);
    }
    public NOptional<Boolean> inherited() {
        return identityState.inherited();
    }
    public NBootOptionsBuilder inherited(Boolean inherited) {
        return identityState.inherited(inherited);
    }
    public NOptional<Boolean> readOnly() {
        return executionState.readOnly();
    }
    public NBootOptionsBuilder readOnly(Boolean readOnly) {
        return executionState.readOnly(readOnly);
    }
    public NOptional<Boolean> recover() {
        return executionState.recover();
    }
    public NBootOptionsBuilder recover(Boolean recover) {
        return executionState.recover(recover);
    }
    public NOptional<Boolean> reset() {
        return executionState.reset();
    }
    public NBootOptionsBuilder reset(Boolean reset) {
        return executionState.reset(reset);
    }
    public NOptional<Boolean> resetHard() {
        return executionState.resetHard();
    }
    public NBootOptionsBuilder resetHard(Boolean resetHard) {
        return executionState.resetHard(resetHard);
    }
    public NOptional<Boolean> commandVersion() {
        return executionState.commandVersion();
    }
    public NBootOptionsBuilder commandVersion(Boolean version) {
        return executionState.commandVersion(version);
    }
    public NOptional<Boolean> commandHelp() {
        return executionState.commandHelp();
    }
    public NBootOptionsBuilder commandHelp(Boolean help) {
        return executionState.commandHelp(help);
    }
    public NOptional<Boolean> installCompanions() {
        return workspaceState.installCompanions();
    }
    public NBootOptionsBuilder installCompanions(Boolean skipInstallCompanions) {
        return workspaceState.installCompanions(skipInstallCompanions);
    }
    public NOptional<Boolean> skipWelcome() {
        return workspaceState.skipWelcome();
    }
    public NBootOptionsBuilder skipWelcome(Boolean skipWelcome) {
        return workspaceState.skipWelcome(skipWelcome);
    }
    public NOptional<String> outLinePrefix() {
        return identityState.outLinePrefix();
    }
    public NBootOptionsBuilder outLinePrefix(String outLinePrefix) {
        return identityState.outLinePrefix(outLinePrefix);
    }
    public NOptional<String> errLinePrefix() {
        return identityState.errLinePrefix();
    }
    public NBootOptionsBuilder errLinePrefix(String errLinePrefix) {
        return identityState.errLinePrefix(errLinePrefix);
    }
    public NOptional<Boolean> skipBoot() {
        return workspaceState.skipBoot();
    }
    public NBootOptionsBuilder skipBoot(Boolean skipBoot) {
        return workspaceState.skipBoot(skipBoot);
    }
    public NOptional<Boolean> trace() {
        return executionState.trace();
    }
    public NBootOptionsBuilder trace(Boolean trace) {
        return executionState.trace(trace);
    }
    public NOptional<String> progressOptions() {
        return identityState.progressOptions();
    }
    public NBootOptionsBuilder progressOptions(String progressOptions) {
        return identityState.progressOptions(progressOptions);
    }
    public NOptional<Boolean> cached() {
        return executionState.cached();
    }
    public NBootOptionsBuilder cached(Boolean cached) {
        return executionState.cached(cached);
    }
    public NOptional<Boolean> indexed() {
        return executionState.indexed();
    }
    public NBootOptionsBuilder indexed(Boolean indexed) {
        return executionState.indexed(indexed);
    }
    public NOptional<Boolean> transitive() {
        return executionState.transitive();
    }
    public NBootOptionsBuilder transitive(Boolean transitive) {
        return executionState.transitive(transitive);
    }
    public NOptional<Boolean> bot() {
        return executionState.bot();
    }
    public NBootOptionsBuilder bot(Boolean bot) {
        return executionState.bot(bot);
    }
    public NOptional<Boolean> previewRepo() {
        return executionState.previewRepo();
    }
    public NBootOptionsBuilder previewRepo(Boolean bot) {
        return executionState.previewRepo(bot);
    }
    public NOptional<NFetchStrategy> fetchStrategy() {
        return executionState.fetchStrategy();
    }
    public NBootOptionsBuilder fetchStrategy(NFetchStrategy fetchStrategy) {
        return executionState.fetchStrategy(fetchStrategy);
    }
    public NOptional<InputStream> stdin() {
        return executionState.stdin();
    }
    public NBootOptionsBuilder stdin(InputStream stdin) {
        return executionState.stdin(stdin);
    }
    public NOptional<PrintStream> stdout() {
        return executionState.stdout();
    }
    public NBootOptionsBuilder stdout(PrintStream stdout) {
        return executionState.stdout(stdout);
    }
    public NOptional<PrintStream> stderr() {
        return executionState.stderr();
    }
    public NBootOptionsBuilder stderr(PrintStream stderr) {
        return executionState.stderr(stderr);
    }
    public NOptional<ExecutorService> executorService() {
        return executionState.executorService();
    }
    public NBootOptionsBuilder executorService(ExecutorService executorService) {
        return executionState.executorService(executorService);
    }
    public NOptional<Instant> expireTime() {
        return executionState.expireTime();
    }
    public NBootOptionsBuilder expireTime(Instant expireTime) {
        return executionState.expireTime(expireTime);
    }
    public NOptional<Boolean> skipErrors() {
        return executionState.skipErrors();
    }
    public NBootOptionsBuilder skipErrors(Boolean value) {
        return executionState.skipErrors(value);
    }
    public NOptional<Boolean> switchWorkspace() {
        return workspaceState.switchWorkspace();
    }
    public NBootOptionsBuilder switchWorkspace(Boolean switchWorkspace) {
        return workspaceState.switchWorkspace(switchWorkspace);
    }
    public NOptional<List<NMsg>> errors() {
        return executionState.errors();
    }
    public NBootOptionsBuilder errors(List<NMsg> errors) {
        return executionState.errors(errors);
    }
    public NBootOptionsBuilder customOptions(List<String> properties) {
        return identityState.customOptions(properties);
    }
    public NOptional<String> locale() {
        return identityState.locale();
    }
    public NBootOptionsBuilder locale(String locale) {
        return identityState.locale(locale);
    }
    public NOptional<String> theme() {
        return identityState.theme();
    }
    public NBootOptionsBuilder theme(String theme) {
        return identityState.theme(theme);
    }
    public NBootOptionsBuilder copyFrom(NBootOptionsInfo other) {
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
        identityState.userName(other.userName());
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
                            NOsFamily.parse(e.getKey().osFamily()).orNull(),
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

        workspaceState.excludedExtensions(other.excludedExtensions());
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
        executionState.classLoaderSupplier(other.classLoaderSupplier());
        classworldState.classWorldLoader(other.classWorldLoader());

        workspaceState.bootRepositories(other.bootRepositories());
        classworldState.runtimeBootDependencyNode(convertNode(other.runtimeBootDependencyNode()));
        classworldState.bootWorkspaceFactory(other.bootWorkspaceFactory());
        classworldState.classWorldURLs(other.classWorldURLs());
        classworldState.classWorldLoader(other.classWorldLoader());
        identityState.uuid(other.uuid());
        classworldState.runtimeBootDescriptor(other.runtimeBootDescriptor());

        return this;
    }
    private List<NClassLoaderNode> convertNodes(List<NBootClassLoaderNode> dependencies) {
        return dependencies == null ? null : dependencies.stream().map(this::convertNode).collect(Collectors.toList());
    }
    private NClassLoaderNode convertNode(NBootClassLoaderNode n) {
        if (n == null) {
            return null;
        }
        List<NBootClassLoaderNode> dependencies = n.dependencies();
        List<NClassLoaderNode> children = convertNodes(dependencies);
        return new NDefaultClassLoaderNode(
                NBlankable.isBlank(n.id()) ? null : NId.get(n.id()).get(),
                n.url(),
                n.isEnabled(),
                n.isIncludedInClasspath(),
                children == null ? null : children.toArray(new NClassLoaderNode[0])
        );
    }
    public NBootOptionsBuilder cmdLine(String cmdLine) {
        cmdLine(NCmdLine.parseDefault(cmdLine).get().toStringArray());
        return this;
    }
    public NBootOptionsBuilder cmdLine(String[] args) {
        NWorkspaceOptionsBuilder b = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(args, b);
        copyFromIfPresent(b.build());
        return this;
    }
    public NOptional<Boolean> sharedInstance() {
        return identityState.sharedInstance();
    }
    public NBootOptionsBuilder sharedInstance(Boolean sharedInstance) {
        return identityState.sharedInstance(sharedInstance);
    }
    public NBootOptionsBuilder userName(String username) {
        return identityState.userName(username);
    }
    public NBootOptionsBuilder storeLocation(NStoreType location, String value) {
        return workspaceState.storeLocation(location, value);
    }
    public NBootOptionsBuilder homeLocation(NHomeLocation location, String value) {
        return workspaceState.homeLocation(location, value);
    }
    public NBootOptionsBuilder addOutputFormatOptions(String... options) {
        return identityState.addOutputFormatOptions(options);
    }
    public NOptional<String> dependencySolver() {
        return identityState.dependencySolver();
    }
    public NBootOptionsBuilder dependencySolver(String dependencySolver) {
        return identityState.dependencySolver(dependencySolver);
    }
    public String toString() {
        return build().toCmdLine().toString();
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
    public NOptional<Boolean> initJava() {
        return executionState.initJava();
    }
    public NOptional<Boolean> localMaven() {
        return executionState.localMaven();
    }
    public NBootOptionsBuilder unsetRuntimeOptions() {
        return executionState.unsetRuntimeOptions();
    }
    public NBootOptionsBuilder unsetCreationOptions() {
        identityState.name(null);
        workspaceState.archetype(null);
        workspaceState.storeLayout(null);
        workspaceState.storeStrategy(null);
        workspaceState.repositoryStoreStrategy(null);
        workspaceState.storeLocations(null);
        workspaceState.homeLocations(null);
        workspaceState.switchWorkspace(null);
        return this;
    }
    public NBootOptionsBuilder unsetExportedOptions() {
        identityState.javaCommand(null);
        identityState.javaOptions(null);
        identityState.workspace(null);
        identityState.userName(null);
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
        executionState.localMaven(null);
        executionState.desktopLauncher(null);
        executionState.menuLauncher(null);
        executionState.userLauncher(null);
        return this;
    }
    public NOptional<List<String>> customOptions() {
        return identityState.customOptions();
    }
    public NOptional<List<NArg>> customOptionArgs() {
        return identityState.customOptionArgs();
    }
    public NOptional<NArg> customOptionArg(String key) {
        return identityState.customOptionArg(key);
    }
    public NOptional<String> customOption(String key) {
        return identityState.customOption(key);
    }
}
