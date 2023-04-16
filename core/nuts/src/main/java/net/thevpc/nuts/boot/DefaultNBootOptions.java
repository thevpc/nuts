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
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NReservedCollectionUtils;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NBootWorkspaceFactory;
import net.thevpc.nuts.util.NLogConfig;

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
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public class DefaultNBootOptions extends DefaultNWorkspaceOptions implements NBootOptions {
    private static final long serialVersionUID = 1;
    /**
     * bootRepositories list (; separated) where to look for runtime
     * dependencies
     * special
     */
    private final String bootRepositories;
    /**
     * special
     */
    private final NClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private final List<NDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private final List<NClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private final NBootWorkspaceFactory bootWorkspaceFactory;

    /**
     * special
     */
    private final List<URL> classWorldURLs;

    /**
     * special
     */
    private final ClassLoader classWorldLoader;

    /**
     * special
     */
    private final String uuid;

    /**
     * special
     */
    private final Set<String> extensionsSet;

    /**
     * special
     */
    private final NDescriptor runtimeBootDescriptor;
    private final NWorkspaceOptions userOptions;

    public DefaultNBootOptions(List<String> outputFormatOptions, List<String> customOptions, NVersion apiVersion,
                               NId runtimeId, String javaCommand, String javaOptions, String workspace,
                               String outLinePrefix, String errLinePrefix, String name, Boolean installCompanions,
                               Boolean skipWelcome, Boolean skipBoot, Boolean system, Boolean gui,
                               Boolean dry, Boolean recover, Boolean reset, Boolean commandVersion, Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, NIsolationLevel isolation, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, List<String> excludedExtensions, List<String> repositories, String userName,
                               char[] credentials, NTerminalMode terminalMode, Boolean readOnly,
                               Boolean trace, String progressOptions, String dependencySolver,
                               NLogConfig logConfig, NConfirmationMode confirm, NContentType outputFormat,
                               List<String> applicationArguments, NOpenMode openMode, Instant creationTime,
                               Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions,
                               String debug, NExecutionType executionType, NRunAs runAs,
                               String archetype, Map<NStoreType, String> storeLocations,
                               Map<NHomeLocation, String> homeLocations, NOsFamily storeLayout,
                               NStoreStrategy storeStrategy,
                               NStoreStrategy repositoryStoreStrategy, NFetchStrategy fetchStrategy,
                               InputStream stdin,
                               PrintStream stdout, PrintStream stderr, ExecutorService executorService,
                               Instant expireTime, List<NMsg> errors, Boolean skipErrors, String locale,
                               String theme, String uuid, String bootRepositories, NClassLoaderNode runtimeBootDependencyNode,
                               List<NDescriptor> extensionBootDescriptors, List<NClassLoaderNode> extensionBootDependencyNodes,
                               List<URL> classWorldURLs, Set<String> extensionsSet, NBootWorkspaceFactory bootWorkspaceFactory, NDescriptor runtimeBootDescriptor, ClassLoader classWorldLoader,
                               NWorkspaceOptions userOptions,
                               NSupportMode desktopLauncher, NSupportMode menuLauncher, NSupportMode userLauncher) {
        super(apiVersion, runtimeId, workspace, name, javaCommand, javaOptions, outLinePrefix,
                errLinePrefix, userName, credentials, progressOptions, dependencySolver, debug, archetype,
                locale, theme, logConfig, confirm, outputFormat, openMode, executionType, storeStrategy,
                repositoryStoreStrategy, storeLayout, terminalMode, fetchStrategy, runAs, creationTime,
                expireTime, installCompanions, skipWelcome, skipBoot, system, gui, readOnly, trace, dry, recover, reset,
                commandVersion, commandHelp, inherited, switchWorkspace, cached, indexed, transitive, bot, skipErrors,
                isolation, initLaunchers, initScripts, initPlatforms, initJava, stdin, stdout, stderr, executorService,
                classLoaderSupplier, applicationArguments, outputFormatOptions, customOptions,
                excludedExtensions, repositories,
                executorOptions,
                errors, storeLocations,
                homeLocations,
                desktopLauncher, menuLauncher, userLauncher);
        this.bootRepositories = bootRepositories;
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        this.extensionBootDescriptors = NReservedCollectionUtils.unmodifiableOrNullList(extensionBootDescriptors);
        this.extensionBootDependencyNodes = NReservedCollectionUtils.unmodifiableOrNullList(extensionBootDependencyNodes);
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        this.classWorldURLs = NReservedCollectionUtils.unmodifiableOrNullList(classWorldURLs);
        this.classWorldLoader = classWorldLoader;
        this.uuid = uuid;
        this.extensionsSet = NReservedCollectionUtils.unmodifiableOrNullSet(extensionsSet);
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        this.userOptions = userOptions==null?null:userOptions.readOnly();
    }

    @Override
    public NOptional<NWorkspaceOptions> getUserOptions() {
        return NOptional.ofNamed(userOptions,"userOptions");
    }

    @Override
    public NBootOptionsBuilder builder() {
        return (NBootOptionsBuilder) new DefaultNBootOptionsBuilder().setAll(this);
    }

    @Override
    public NOptional<String> getBootRepositories() {
        return NOptional.ofNamed(bootRepositories,"bootRepositories");
    }

    @Override
    public NOptional<NClassLoaderNode> getRuntimeBootDependencyNode() {
        return NOptional.ofNamed(runtimeBootDependencyNode,"runtimeBootDependencyNode");
    }


    @Override
    public NOptional<List<NDescriptor>> getExtensionBootDescriptors() {
        return NOptional.ofNamed(extensionBootDescriptors,"extensionBootDescriptors");
    }

    @Override
    public NOptional<List<NClassLoaderNode>> getExtensionBootDependencyNodes() {
        return NOptional.ofNamed(extensionBootDependencyNodes,"extensionBootDependencyNodes");
    }


    @Override
    public NOptional<NBootWorkspaceFactory> getBootWorkspaceFactory() {
        return NOptional.ofNamed(bootWorkspaceFactory,"bootWorkspaceFactory");
    }


    @Override
    public NOptional<List<URL>> getClassWorldURLs() {
        return NOptional.ofNamed(classWorldURLs,"classWorldURLs");
    }


    @Override
    public NOptional<ClassLoader> getClassWorldLoader() {
        return NOptional.ofNamed(classWorldLoader,"classWorldLoader");
    }


    @Override
    public NOptional<String> getUuid() {
        return NOptional.ofNamed(uuid,"uuid");
    }


    @Override
    public NOptional<Set<String>> getExtensionsSet() {
        return NOptional.ofNamed(extensionsSet,"extensionsSet");
    }


    @Override
    public NOptional<NDescriptor> getRuntimeBootDescriptor() {
        return NOptional.ofNamed(runtimeBootDescriptor,"runtimeBootDescriptor");
    }

    @Override
    public NBootOptions readOnly() {
        return this;
    }
}
