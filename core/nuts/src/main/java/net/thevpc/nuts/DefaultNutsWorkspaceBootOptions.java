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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

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

    public DefaultNutsWorkspaceBootOptions(List<String> outputFormatOptions, List<String> customOptions, String apiVersion,
                                           String runtimeId, String javaCommand, String javaOptions, String workspace,
                                           String outLinePrefix, String errLinePrefix, String name, Boolean skipCompanions,
                                           Boolean skipWelcome, Boolean skipBoot, Boolean global, Boolean gui,
                                           List<String> excludedExtensions, List<String> repositories, String userName,
                                           char[] credentials, NutsTerminalMode terminalMode, Boolean readOnly,
                                           Boolean trace, String progressOptions, String dependencySolver,
                                           NutsLogConfig logConfig, NutsConfirmationMode confirm, NutsContentType outputFormat,
                                           List<String> applicationArguments, NutsOpenMode openMode, Instant creationTime,
                                           Boolean dry, Supplier<ClassLoader> classLoaderSupplier, List<String> executorOptions,
                                           Boolean recover, Boolean reset, Boolean commandVersion, Boolean commandHelp,
                                           String debug, Boolean inherited, NutsExecutionType executionType, NutsRunAs runAs,
                                           String archetype, Boolean switchWorkspace, Map<NutsStoreLocation, String> storeLocations,
                                           Map<NutsHomeLocation, String> homeLocations, NutsOsFamily storeLocationLayout,
                                           NutsStoreLocationStrategy storeLocationStrategy,
                                           NutsStoreLocationStrategy repositoryStoreLocationStrategy, NutsFetchStrategy fetchStrategy,
                                           Boolean cached, Boolean indexed, Boolean transitive, Boolean bot, InputStream stdin,
                                           PrintStream stdout, PrintStream stderr, ExecutorService executorService,
                                           Instant expireTime, List<NutsMessage> errors, Boolean skipErrors, String locale,
                                           String theme, String bootRepositories, NutsClassLoaderNode runtimeBootDependencyNode,
                                           List<NutsDescriptor> extensionBootDescriptors, List<NutsClassLoaderNode> extensionBootDependencyNodes,
                                           NutsBootWorkspaceFactory bootWorkspaceFactory, List<URL> classWorldURLs, ClassLoader classWorldLoader,
                                           String uuid, Set<String> extensionsSet, NutsDescriptor runtimeBootDescriptor,
                                           NutsWorkspaceOptions userOptions
                                           ) {
        super(outputFormatOptions, customOptions, apiVersion, runtimeId, javaCommand, javaOptions, workspace, outLinePrefix,
                errLinePrefix, name, skipCompanions, skipWelcome, skipBoot, global, gui, excludedExtensions, repositories, userName,
                credentials, terminalMode, readOnly, trace, progressOptions, dependencySolver, logConfig, confirm, outputFormat,
                applicationArguments, openMode, creationTime, dry, classLoaderSupplier, executorOptions, recover, reset,
                commandVersion, commandHelp, debug, inherited, executionType, runAs, archetype, switchWorkspace, storeLocations,
                homeLocations, storeLocationLayout, storeLocationStrategy, repositoryStoreLocationStrategy, fetchStrategy,
                cached, indexed, transitive, bot, stdin, stdout, stderr, executorService, expireTime, errors, skipErrors,
                locale, theme);
        this.bootRepositories = NutsUtilStrings.trimToNull(bootRepositories);
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        this.extensionBootDescriptors = extensionBootDescriptors;
        this.extensionBootDependencyNodes = extensionBootDependencyNodes;
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        this.classWorldURLs = PrivateNutsUtilCollections.nonNullList(classWorldURLs);
        this.classWorldLoader = classWorldLoader;
        this.uuid = NutsUtilStrings.trimToNull(uuid);
        this.extensionsSet = PrivateNutsUtilCollections.nonNullSet(extensionsSet);
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        this.userOptions = userOptions==null?null:userOptions.readOnly();
    }

    @Override
    public NutsWorkspaceOptions getUserOptions() {
        return userOptions;
    }

    @Override
    public NutsWorkspaceBootOptionsBuilder builder() {
        return (NutsWorkspaceBootOptionsBuilder) new DefaultNutsWorkspaceBootOptionsBuilder().setAll(this);
    }

    @Override
    public String getBootRepositories() {
        return bootRepositories;
    }

    @Override
    public NutsClassLoaderNode getRuntimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }


    @Override
    public List<NutsDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    @Override
    public List<NutsClassLoaderNode> getExtensionBootDependencyNodes() {
        return extensionBootDependencyNodes;
    }


    @Override
    public NutsBootWorkspaceFactory getBootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }


    @Override
    public List<URL> getClassWorldURLs() {
        return classWorldURLs;
    }


    @Override
    public ClassLoader getClassWorldLoader() {
        return classWorldLoader;
    }


    @Override
    public String getUuid() {
        return uuid;
    }


    @Override
    public Set<String> getExtensionsSet() {
        return extensionsSet;
    }


    @Override
    public NutsDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    @Override
    public NutsWorkspaceBootOptions readOnly() {
        return this;
    }
}
