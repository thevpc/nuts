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
    public NutsOptional<NutsWorkspaceOptions> getUserOptions() {
        return NutsOptional.of(userOptions);
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
                getOutputFormatOptions().orNull(), getCustomOptions().orNull(), getApiVersion().orNull(), getRuntimeId().orNull(), getJavaCommand().orNull()
                , getJavaOptions().orNull(), getWorkspace().orNull(), getOutLinePrefix().orNull(), getErrLinePrefix().orNull()
                , getName().orNull(), getSkipCompanions().orNull(), getSkipWelcome().orNull(), getSkipBoot().orNull()
                , getGlobal().orNull(), getGui().orNull(), getExcludedExtensions().orNull(), getRepositories().orNull(), getUserName().orNull()
                , getCredentials().orNull(), getTerminalMode().orNull(), getReadOnly().orNull(), getTrace().orNull(), getProgressOptions().orNull()
                , getDependencySolver().orNull(), getLogConfig().orNull(), getConfirm().orNull(), getOutputFormat().orNull()
                , getApplicationArguments().orNull(), getOpenMode().orNull(), getCreationTime().orNull(), getDry().orNull()
                , getClassLoaderSupplier().orNull(), getExecutorOptions().orNull(), getRecover().orNull(), getReset().orNull()
                , getCommandVersion().orNull(), getCommandHelp().orNull(), getDebug().orNull(), getCommandHelp().orNull()
                , getExecutionType().orNull(), getRunAs().orNull(), getArchetype().orNull(), getSwitchWorkspace().orNull()
                , getStoreLocations().orNull(), getHomeLocations().orNull(), getStoreLocationLayout().orNull(), getStoreLocationStrategy().orNull()
                , getRepositoryStoreLocationStrategy().orNull(), getFetchStrategy().orNull(), getCached().orNull(), getCached().orNull()
                , getTransitive().orNull(), getBot().orNull(), getStdin().orNull(), getStdout().orNull(), getStdout().orNull()
                , getExecutorService().orNull(), getExpireTime().orNull(), getErrors().orNull(), getSkipErrors().orNull(), getLocale().orNull()
                , getTheme().orNull(), getUuid().orNull(), getBootRepositories().orNull(), getRuntimeBootDependencyNode().orNull(), getExtensionBootDescriptors().orNull()
                , getExtensionBootDependencyNodes().orNull(), getClassWorldURLs().orNull(), getExtensionsSet().orNull(), getBootWorkspaceFactory().orNull(), getRuntimeBootDescriptor().orNull(), getClassWorldLoader().orNull()
                , getUserOptions().orNull()
        );
    }

    public DefaultNutsWorkspaceBootOptionsBuilder setAll(DefaultNutsWorkspaceOptionsBuilder other) {
        super.setAll(other);
        if (other instanceof DefaultNutsWorkspaceBootOptionsBuilder) {
            DefaultNutsWorkspaceBootOptionsBuilder b = (DefaultNutsWorkspaceBootOptionsBuilder) other;
            setBootRepositories(b.getBootRepositories().orNull());
            setRuntimeBootDependencyNode(b.getRuntimeBootDependencyNode().orNull());
            setExtensionBootDescriptors(b.getExtensionBootDescriptors().orNull());
            setExtensionBootDependencyNodes(b.getExtensionBootDependencyNodes().orNull());
            setBootWorkspaceFactory(b.getBootWorkspaceFactory().orNull());
            setClassWorldURLs(b.getClassWorldURLs().orNull());
            setClassWorldLoader(b.getClassWorldLoader().orNull());
            setUuid(b.getUuid().orNull());
            setExtensionsSet(b.getExtensionsSet().orNull());
            setRuntimeBootDescriptor(b.getRuntimeBootDescriptor().orNull());
            setUserOptions(b.getUserOptions().orNull());
        }
        return this;
    }

    public NutsOptional<String> getBootRepositories() {
        return NutsOptional.of(bootRepositories);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setBootRepositories(String bootRepositories) {
        this.bootRepositories = NutsUtilStrings.trimToNull(bootRepositories);
        return this;
    }

    public NutsOptional<NutsClassLoaderNode> getRuntimeBootDependencyNode() {
        return NutsOptional.of(runtimeBootDependencyNode);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDependencyNode(NutsClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public NutsOptional<List<NutsDescriptor>> getExtensionBootDescriptors() {
        return NutsOptional.of(extensionBootDescriptors);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDescriptors(List<NutsDescriptor> extensionBootDescriptors) {
        this.extensionBootDescriptors = PrivateNutsUtilCollections.nonNullList(extensionBootDescriptors);
        return this;
    }

    public NutsOptional<List<NutsClassLoaderNode>> getExtensionBootDependencyNodes() {
        return NutsOptional.of(extensionBootDependencyNodes);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDependencyNodes(List<NutsClassLoaderNode> extensionBootDependencyNodes) {
        this.extensionBootDependencyNodes = PrivateNutsUtilCollections.nonNullList(extensionBootDependencyNodes);
        return this;
    }

    public NutsOptional<NutsBootWorkspaceFactory> getBootWorkspaceFactory() {
        return NutsOptional.of(bootWorkspaceFactory);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setBootWorkspaceFactory(NutsBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return this;
    }

    public NutsOptional<List<URL>> getClassWorldURLs() {
        return NutsOptional.of(classWorldURLs);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = PrivateNutsUtilCollections.nonNullList(classWorldURLs);
        return this;
    }

    public NutsOptional<ClassLoader> getClassWorldLoader() {
        return NutsOptional.of(classWorldLoader);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return this;
    }

    public NutsOptional<String> getUuid() {
        return NutsOptional.of(uuid);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setUuid(String uuid) {
        this.uuid = NutsUtilStrings.trimToNull(uuid);
        return this;
    }

    public NutsOptional<Set<String>> getExtensionsSet() {
        return NutsOptional.of(extensionsSet);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet) {
        this.extensionsSet = PrivateNutsUtilCollections.nonNullSet(extensionsSet);
        return this;
    }

    public NutsOptional<NutsDescriptor> getRuntimeBootDescriptor() {
        return NutsOptional.of(runtimeBootDescriptor);
    }

    @Override
    public DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDescriptor(NutsDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAll(NutsWorkspaceOptions other) {
        super.setAll(other);
        if (other instanceof NutsWorkspaceBootOptions) {
            NutsWorkspaceBootOptions o = (NutsWorkspaceBootOptions) other;
            setUserOptions(o.getUserOptions().orNull());
            setBootRepositories(o.getBootRepositories().orNull());
            setRuntimeBootDependencyNode(o.getRuntimeBootDependencyNode().orNull());
            setExtensionBootDescriptors(o.getExtensionBootDescriptors().orNull());
            setExtensionBootDependencyNodes(o.getExtensionBootDependencyNodes().orNull());
            setBootWorkspaceFactory(o.getBootWorkspaceFactory().orNull());
            setClassWorldURLs(o.getClassWorldURLs().orNull());
            setClassWorldLoader(o.getClassWorldLoader().orNull());
            setUuid(o.getUuid().orNull());
            setExtensionsSet(o.getExtensionsSet().orNull());
            setRuntimeBootDescriptor(o.getRuntimeBootDescriptor().orNull());
        }
        return this;
    }

    @Override
    public NutsWorkspaceOptionsBuilder setAllPresent(NutsWorkspaceOptions other) {
        super.setAllPresent(other);
        if (other instanceof NutsWorkspaceBootOptions) {
            NutsWorkspaceBootOptions o = (NutsWorkspaceBootOptions) other;
            if (o.getUserOptions().isPresent()) {
                setUserOptions(o.getUserOptions().orNull());
            }
            setBootRepositories(o.getBootRepositories().orNull());
            if (o.getUserOptions().isPresent()) {
                setRuntimeBootDependencyNode(o.getRuntimeBootDependencyNode().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setExtensionBootDescriptors(o.getExtensionBootDescriptors().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setExtensionBootDependencyNodes(o.getExtensionBootDependencyNodes().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setBootWorkspaceFactory(o.getBootWorkspaceFactory().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setClassWorldURLs(o.getClassWorldURLs().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setClassWorldLoader(o.getClassWorldLoader().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setUuid(o.getUuid().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setExtensionsSet(o.getExtensionsSet().orNull());
            }
            if (o.getUserOptions().isPresent()) {
                setRuntimeBootDescriptor(o.getRuntimeBootDescriptor().orNull());
            }
        }
        return this;
    }

    @Override
    public NutsWorkspaceBootOptions readOnly() {
        return build();
    }
}
