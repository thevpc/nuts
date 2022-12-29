package net.thevpc.nuts.boot;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NWorkspaceOptions;
import net.thevpc.nuts.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.spi.NBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NWorkspaceBootOptionsBuilder extends NWorkspaceBootOptions, NWorkspaceOptionsBuilder {
    NWorkspaceOptionsBuilder setUserOptions(NWorkspaceOptions userOptions);
    @Override
    NWorkspaceBootOptionsBuilder builder();

    @Override
    NWorkspaceBootOptionsBuilder copy();

    @Override
    NWorkspaceBootOptions build();

    DefaultNWorkspaceBootOptionsBuilder setBootRepositories(String bootRepositories);

    DefaultNWorkspaceBootOptionsBuilder setRuntimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode);

    DefaultNWorkspaceBootOptionsBuilder setExtensionBootDescriptors(List<NDescriptor> extensionBootDescriptors);

    DefaultNWorkspaceBootOptionsBuilder setExtensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes);

    DefaultNWorkspaceBootOptionsBuilder setBootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory);

    DefaultNWorkspaceBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs);

    DefaultNWorkspaceBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader);

    DefaultNWorkspaceBootOptionsBuilder setUuid(String uuid);

    DefaultNWorkspaceBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet);

    DefaultNWorkspaceBootOptionsBuilder setRuntimeBootDescriptor(NDescriptor runtimeBootDescriptor);
}
