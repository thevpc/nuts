package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsWorkspaceOptions;
import net.thevpc.nuts.NutsWorkspaceOptionsBuilder;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NutsWorkspaceBootOptionsBuilder extends NutsWorkspaceBootOptions, NutsWorkspaceOptionsBuilder {
    NutsWorkspaceOptionsBuilder setUserOptions(NutsWorkspaceOptions userOptions);
    @Override
    NutsWorkspaceBootOptionsBuilder builder();

    @Override
    NutsWorkspaceBootOptionsBuilder copy();

    @Override
    NutsWorkspaceBootOptions build();

    DefaultNutsWorkspaceBootOptionsBuilder setBootRepositories(String bootRepositories);

    DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDependencyNode(NutsClassLoaderNode runtimeBootDependencyNode);

    DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDescriptors(List<NutsDescriptor> extensionBootDescriptors);

    DefaultNutsWorkspaceBootOptionsBuilder setExtensionBootDependencyNodes(List<NutsClassLoaderNode> extensionBootDependencyNodes);

    DefaultNutsWorkspaceBootOptionsBuilder setBootWorkspaceFactory(NutsBootWorkspaceFactory bootWorkspaceFactory);

    DefaultNutsWorkspaceBootOptionsBuilder setClassWorldURLs(List<URL> classWorldURLs);

    DefaultNutsWorkspaceBootOptionsBuilder setClassWorldLoader(ClassLoader classWorldLoader);

    DefaultNutsWorkspaceBootOptionsBuilder setUuid(String uuid);

    DefaultNutsWorkspaceBootOptionsBuilder setExtensionsSet(Set<String> extensionsSet);

    DefaultNutsWorkspaceBootOptionsBuilder setRuntimeBootDescriptor(NutsDescriptor runtimeBootDescriptor);
}
