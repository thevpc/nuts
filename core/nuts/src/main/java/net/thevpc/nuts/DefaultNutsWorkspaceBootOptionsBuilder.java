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

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.net.URL;
import java.util.*;

/**
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class DefaultNutsWorkspaceBootOptionsBuilder extends DefaultNutsWorkspaceOptionsBuilder implements NutsWorkspaceBootOptionsBuilder {
    private static final long serialVersionUID = 1;
    /**
     * bootRepositories list (; separated) where to look for runtime
     * dependencies
     * special
     */
    private String bootRepositories;
    /**
     * special
     */
    private NutsClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private List<NutsDescriptor> extensionBootDescriptors;
    /**
     * special
     */
    private List<NutsClassLoaderNode> extensionBootDependencyNodes;

    /**
     * special
     */
    private NutsBootWorkspaceFactory bootWorkspaceFactory;

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
    private NutsDescriptor runtimeBootDescriptor;
    /**
     * user options
     */
    private NutsWorkspaceOptions userOptions;

    public DefaultNutsWorkspaceBootOptionsBuilder() {
    }

    @Override
    public NutsWorkspaceOptions getUserOptions() {
        return userOptions;
    }

    @Override
    public NutsWorkspaceBootOptionsBuilder setUserOptions(NutsWorkspaceOptions userOptions) {
        this.userOptions = userOptions;
        return this;
    }

    @Override
    public NutsWorkspaceBootOptionsBuilder copy() {
        return builder();
    }

    @Override
    public NutsWorkspaceBootOptionsBuilder builder() {
        return new DefaultNutsWorkspaceBootOptionsBuilder().setAll(this);
    }

    @Override
    public NutsWorkspaceBootOptions build() {
        return new DefaultNutsWorkspaceBootOptions(
                getOutputFormatOptions(), getCustomOptions(), getApiVersion(), getRuntimeId(), getJavaCommand(), getJavaOptions()
                , getWorkspace(), getOutLinePrefix(), getErrLinePrefix(), getName(), getSkipCompanions(), getSkipWelcome(), getSkipBoot()
                , getGlobal(), getGui(), getExcludedExtensions(), getRepositories(), getUserName(), getCredentials(), getTerminalMode()
                , getReadOnly(), getTrace(), getProgressOptions(), getDependencySolver(), getLogConfig(), getConfirm(), getOutputFormat()
                , getApplicationArguments(), getOpenMode(), getCreationTime(), getDry(), getClassLoaderSupplier(), getExecutorOptions()
                , getRecover(), getReset(), getCommandVersion(), getCommandHelp(), getDebug(), getCommandHelp(), getExecutionType()
                , getRunAs(), getArchetype(), getSwitchWorkspace(), getStoreLocations(), getHomeLocations(), getStoreLocationLayout()
                , getStoreLocationStrategy(), getRepositoryStoreLocationStrategy(), getFetchStrategy(), getCached(), getCached()
                , getTransitive(), getBot(), getStdin(), getStdout(), getStdout(), getExecutorService(), getExpireTime(), getErrors()
                , getSkipErrors(), getLocale(), getTheme(), getBootRepositories(), getRuntimeBootDependencyNode(), getExtensionBootDescriptors(),
                getExtensionBootDependencyNodes(), getBootWorkspaceFactory(), getClassWorldURLs(), getClassWorldLoader(), getUuid()
                , getExtensionsSet(), getRuntimeBootDescriptor(),getUserOptions()
        );
    }

    public DefaultNutsWorkspaceBootOptionsBuilder setAll(DefaultNutsWorkspaceOptionsBuilder other) {
        super.setAll(other);
        if (other instanceof DefaultNutsWorkspaceBootOptionsBuilder) {
            DefaultNutsWorkspaceBootOptionsBuilder b = (DefaultNutsWorkspaceBootOptionsBuilder) other;
            setBootRepositories(b.getBootRepositories());
            setRuntimeBootDependencyNode(b.getRuntimeBootDependencyNode());
            setExtensionBootDescriptors(b.getExtensionBootDescriptors());
            setExtensionBootDependencyNodes(b.getExtensionBootDependencyNodes());
            setBootWorkspaceFactory(b.getBootWorkspaceFactory());
            setClassWorldURLs(b.getClassWorldURLs());
            setClassWorldLoader(b.getClassWorldLoader());
            setUuid(b.getUuid());
            setExtensionsSet(b.getExtensionsSet());
            setRuntimeBootDescriptor(b.getRuntimeBootDescriptor());
            setUserOptions(b.getUserOptions());
        }
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setBootRepositories(String bootRepositories) {
        this.bootRepositories = NutsUtilStrings.trimToNull(bootRepositories);
        return this;
    }

    public NutsClassLoaderNode getRuntimeBootDependencyNode() {
        return runtimeBootDependencyNode;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDependencyNode(NutsClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public List<NutsDescriptor> getExtensionBootDescriptors() {
        return extensionBootDescriptors;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDescriptors(List<NutsDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = PrivateNutsUtilCollections.nonNullList(extensionBootDescriptors);
        return this;
    }

    public List<NutsClassLoaderNode> getExtensionBootDependencyNodes() {
        return extensionBootDependencyNodes;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDependencyNodes(List<NutsClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = PrivateNutsUtilCollections.nonNullList(extensionBootDependencyNodes);
        return this;
    }

    public NutsBootWorkspaceFactory getBootWorkspaceFactory() {
        return bootWorkspaceFactory;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setBootWorkspaceFactory(NutsBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public List<URL> getClassWorldURLs() {
        return classWorldURLs;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = PrivateNutsUtilCollections.nonNullList(classWorldURLs);
        return this;
    }

    public ClassLoader getClassWorldLoader() {
        return classWorldLoader;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setUuid(String uuid) {
        this.uuid = NutsUtilStrings.trimToNull(uuid);
        return this;
    }

    public Set<String> getExtensionsSet() {
        return extensionsSet;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = PrivateNutsUtilCollections.nonNullSet(extensionsSet);
        return this;
    }

    public NutsDescriptor getRuntimeBootDescriptor() {
        return runtimeBootDescriptor;
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDescriptor(NutsDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    @Override
    public NutsWorkspaceBootOptions readOnly() {
        return build();
    }
}
