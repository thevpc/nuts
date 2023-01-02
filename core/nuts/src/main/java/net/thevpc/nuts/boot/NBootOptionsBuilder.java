package net.thevpc.nuts.boot;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NWorkspaceOptions;
import net.thevpc.nuts.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.spi.NBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NBootOptionsBuilder extends NBootOptions, NWorkspaceOptionsBuilder {
    NWorkspaceOptionsBuilder setUserOptions(NWorkspaceOptions userOptions);
    @Override
    NBootOptionsBuilder builder();

    @Override
    NBootOptionsBuilder copy();

    @Override
    NBootOptions build();

    DefaultNBootOptionsBuilder setBootRepositories(String bootRepositories);

    DefaultNBootOptionsBuilder setRuntimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode);

    DefaultNBootOptionsBuilder setExtensionBootDescriptors(List<NDescriptor> extensionBootDescriptors);

    DefaultNBootOptionsBuilder setExtensionBootDependencyNodes(List<NClassLoaderNode> extensionBootDependencyNodes);

    DefaultNBootOptionsBuilder setBootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory);

    DefaultNBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs);

    DefaultNBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader);

    DefaultNBootOptionsBuilder setUuid(String uuid);

    DefaultNBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet);

    DefaultNBootOptionsBuilder setRuntimeBootDescriptor(NDescriptor runtimeBootDescriptor);
}
