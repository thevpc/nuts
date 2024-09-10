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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.spi.NBootWorkspaceFactory;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.net.URL;
import java.util.*;

/**
 * Workspace creation/opening options class.
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.4
 */
public final class DefaultNBootOptionsBuilder extends DefaultNWorkspaceOptionsBuilder implements NBootOptionsBuilder {

    private static final long serialVersionUID = 1;
    /**
     * bootRepositories list (; separated) where to look for runtime
     * dependencies special
     */
    private String bootRepositories;
    /**
     * special
     */
    private NClassLoaderNode runtimeBootDependencyNode;
    /**
     * special
     */
    private List<NDescriptor> extensionBootDescriptors;
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
    private NDescriptor runtimeBootDescriptor;
    /**
     * user options
     */
    private NWorkspaceOptions userOptions;

    public DefaultNBootOptionsBuilder() {
    }

    @Override
    public NOptional<NWorkspaceOptions> getUserOptions() {
        return NOptional.of(userOptions);
    }

    @Override
    public NBootOptionsBuilder setUserOptions(NWorkspaceOptions userOptions) {
        this.userOptions = userOptions;
        return this;
    }

    @Override
    public NBootOptionsBuilder copy() {
        return builder();
    }

    @Override
    public NBootOptionsBuilder builder() {
        return new DefaultNBootOptionsBuilder().setAll(this);
    }

    @Override
    public NBootOptions build() {
        return new DefaultNBootOptions(
                getOutputFormatOptions().orNull(), getCustomOptions().orNull(), getApiVersion().orNull(), getRuntimeId().orNull(), getJavaCommand().orNull(),
                 getJavaOptions().orNull(), getWorkspace().orNull(), getOutLinePrefix().orNull(), getErrLinePrefix().orNull(),
                 getName().orNull(), getInstallCompanions().orNull(), getSkipWelcome().orNull(), getSkipBoot().orNull(),
                 getSystem().orNull(), getGui().orNull(), getDry().orNull(), getShowStacktrace().orNull(), getRecover().orNull(), getReset().orNull(), getCommandVersion().orNull(), getCommandHelp().orNull(), getCommandHelp().orNull(), getSwitchWorkspace().orNull(), getCached().orNull(), getCached().orNull(), getTransitive().orNull(), getBot().orNull(),
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
                 getUserOptions().orNull(),
                getDesktopLauncher().orNull(), getMenuLauncher().orNull(), getUserLauncher().orNull(), getPreviewRepo().orNull());
    }

    public DefaultNBootOptionsBuilder setAll(DefaultNWorkspaceOptionsBuilder other) {
        super.setAll(other);
        if (other instanceof DefaultNBootOptionsBuilder) {
            DefaultNBootOptionsBuilder b = (DefaultNBootOptionsBuilder) other;
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

    public NOptional<String> getBootRepositories() {
        return NOptional.of(bootRepositories);
    }

    @Override
    public DefaultNBootOptionsBuilder setBootRepositories(String bootRepositories) {
        this.bootRepositories = NStringUtils.trimToNull(bootRepositories);
        return this;
    }

    public NOptional<NClassLoaderNode> getRuntimeBootDependencyNode() {
        return NOptional.of(runtimeBootDependencyNode);
    }

    @Override
    public DefaultNBootOptionsBuilder setRuntimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return this;
    }

    public NOptional<List<NDescriptor>> getExtensionBootDescriptors() {
        return NOptional.of(extensionBootDescriptors);
    }

    @Override
    public DefaultNBootOptionsBuilder setExtensionBootDescriptors(List<NDescriptor> extensionBootDescriptors) {
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

    public NOptional<NDescriptor> getRuntimeBootDescriptor() {
        return NOptional.of(runtimeBootDescriptor);
    }

    @Override
    public DefaultNBootOptionsBuilder setRuntimeBootDescriptor(NDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return this;
    }

    @Override
    public NWorkspaceOptionsBuilder setAll(NWorkspaceOptions other) {
        super.setAll(other);
        if (other instanceof NBootOptions) {
            NBootOptions o = (NBootOptions) other;
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
    public NWorkspaceOptionsBuilder setAllPresent(NWorkspaceOptions other) {
        super.setAllPresent(other);
        if (other instanceof NBootOptions) {
            NBootOptions o = (NBootOptions) other;
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
    public NBootOptions readOnly() {
        return build();
    }
}
