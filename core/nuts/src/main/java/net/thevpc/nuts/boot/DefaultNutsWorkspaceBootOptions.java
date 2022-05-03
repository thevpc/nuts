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
import net.thevpc.nuts.reserved.NutsReservedCollectionUtils;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;
import net.thevpc.nuts.util.NutsLogConfig;

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
public class DefaultNutsWorkspaceBootOptions extends DefaultNutsWorkspaceOptions implements NutsWorkspaceBootOptions {
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
    private final NutsClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private final List<NutsDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private final List<NutsClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private final NutsBootWorkspaceFactory bootWorkspaceFactory;

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
    private final NutsDescriptor runtimeBootDescriptor;
    private final NutsWorkspaceOptions userOptions;

    public DefaultNutsWorkspaceBootOptions(List<String> outputFormatOptions, List<String> customOptions, NutsVersion apiVersion,
                                           NutsId runtimeId, String javaCommand, String javaOptions, String workspace,
                                           String outLinePrefix, String errLinePrefix, String name, Boolean skipCompanions,
                                           Boolean skipWelcome, Boolean skipBoot, Boolean global, Boolean gui,
                                           Boolean dry, Boolean recover, Boolean reset, Boolean commandVersion, Boolean commandHelp, Boolean inherited, Boolean switchWorkspace, Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, NutsIsolationLevel isolation, Boolean initLaunchers, Boolean initScripts, Boolean initPlatforms, Boolean initJava, List<String> excludedExtensions, List<String> repositories, String userName,
                                           char[] credentials, NutsTerminalMode terminalMode, Boolean readOnly,
                                           Boolean trace, String progressOptions, String dependencySolver,
                                           NutsLogConfig logConfig, NutsConfirmationMode confirm, NutsContentType outputFormat,
                                           List<String> applicationArguments, NutsOpenMode openMode, Instant creationTime,
                                           Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions,
                                           String debug, NutsExecutionType executionType, NutsRunAs runAs,
                                           String archetype, Map<NutsStoreLocation, String> storeLocations,
                                           Map<NutsHomeLocation, String> homeLocations, NutsOsFamily storeLocationLayout,
                                           NutsStoreLocationStrategy storeLocationStrategy,
                                           NutsStoreLocationStrategy repositoryStoreLocationStrategy, NutsFetchStrategy fetchStrategy,
                                           InputStream stdin,
                                           PrintStream stdout, PrintStream stderr, ExecutorService executorService,
                                           Instant expireTime, List<NutsMessage> errors, Boolean skipErrors, String locale,
                                           String theme, String uuid, String bootRepositories, NutsClassLoaderNode runtimeBootDependencyNode,
                                           List<NutsDescriptor> extensionBootDescriptors, List<NutsClassLoaderNode> extensionBootDependencyNodes,
                                           List<URL> classWorldURLs, Set<String> extensionsSet, NutsBootWorkspaceFactory bootWorkspaceFactory, NutsDescriptor runtimeBootDescriptor, ClassLoader classWorldLoader,
                                           NutsWorkspaceOptions userOptions,
                                           NutsSupportMode desktopLauncher, NutsSupportMode menuLauncher, NutsSupportMode userLauncher) {
        super(apiVersion, runtimeId, workspace, name, javaCommand, javaOptions, outLinePrefix,
                errLinePrefix, userName, credentials, progressOptions, dependencySolver, debug, archetype,
                locale, theme, logConfig, confirm, outputFormat, openMode, executionType, storeLocationStrategy,
                repositoryStoreLocationStrategy, storeLocationLayout, terminalMode, fetchStrategy, runAs, creationTime,
                expireTime, skipCompanions, skipWelcome, skipBoot, global, gui, readOnly, trace, dry, recover, reset,
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
        this.extensionBootDescriptors = NutsReservedCollectionUtils.unmodifiableOrNullList(extensionBootDescriptors);
        this.extensionBootDependencyNodes = NutsReservedCollectionUtils.unmodifiableOrNullList(extensionBootDependencyNodes);
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        this.classWorldURLs = NutsReservedCollectionUtils.unmodifiableOrNullList(classWorldURLs);
        this.classWorldLoader = classWorldLoader;
        this.uuid = uuid;
        this.extensionsSet = NutsReservedCollectionUtils.unmodifiableOrNullSet(extensionsSet);
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        this.userOptions = userOptions==null?null:userOptions.readOnly();
    }

    @Override
    public NutsOptional<NutsWorkspaceOptions> getUserOptions() {
        return NutsOptional.ofNamed(userOptions,"userOptions");
    }

    @Override
    public NutsWorkspaceBootOptionsBuilder builder() {
        return (NutsWorkspaceBootOptionsBuilder) new DefaultNutsWorkspaceBootOptionsBuilder().setAll(this);
    }

    @Override
    public NutsOptional<String> getBootRepositories() {
        return NutsOptional.ofNamed(bootRepositories,"bootRepositories");
    }

    @Override
    public NutsOptional<NutsClassLoaderNode> getRuntimeBootDependencyNode() {
        return NutsOptional.ofNamed(runtimeBootDependencyNode,"runtimeBootDependencyNode");
    }


    @Override
    public NutsOptional<List<NutsDescriptor>> getExtensionBootDescriptors() {
        return NutsOptional.ofNamed(extensionBootDescriptors,"extensionBootDescriptors");
    }

    @Override
    public NutsOptional<List<NutsClassLoaderNode>> getExtensionBootDependencyNodes() {
        return NutsOptional.ofNamed(extensionBootDependencyNodes,"extensionBootDependencyNodes");
    }


    @Override
    public NutsOptional<NutsBootWorkspaceFactory> getBootWorkspaceFactory() {
        return NutsOptional.ofNamed(bootWorkspaceFactory,"bootWorkspaceFactory");
    }


    @Override
    public NutsOptional<List<URL>> getClassWorldURLs() {
        return NutsOptional.ofNamed(classWorldURLs,"classWorldURLs");
    }


    @Override
    public NutsOptional<ClassLoader> getClassWorldLoader() {
        return NutsOptional.ofNamed(classWorldLoader,"classWorldLoader");
    }


    @Override
    public NutsOptional<String> getUuid() {
        return NutsOptional.ofNamed(uuid,"uuid");
    }


    @Override
    public NutsOptional<Set<String>> getExtensionsSet() {
        return NutsOptional.ofNamed(extensionsSet,"extensionsSet");
    }


    @Override
    public NutsOptional<NutsDescriptor> getRuntimeBootDescriptor() {
        return NutsOptional.ofNamed(runtimeBootDescriptor,"runtimeBootDescriptor");
    }

    @Override
    public NutsWorkspaceBootOptions readOnly() {
        return this;
    }
}
