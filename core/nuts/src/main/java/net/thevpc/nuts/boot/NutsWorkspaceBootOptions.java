package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsWorkspaceOptions;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NutsWorkspaceBootOptions extends NutsWorkspaceOptions {
    NutsOptional<NutsWorkspaceOptions> getUserOptions();

    NutsOptional<String> getBootRepositories();

    NutsOptional<NutsClassLoaderNode> getRuntimeBootDependencyNode();

    NutsOptional<List<NutsDescriptor>> getExtensionBootDescriptors();

    NutsOptional<List<NutsClassLoaderNode>> getExtensionBootDependencyNodes();

    NutsOptional<NutsBootWorkspaceFactory> getBootWorkspaceFactory();

    NutsOptional<List<URL>> getClassWorldURLs();

    NutsOptional<ClassLoader> getClassWorldLoader();

    NutsOptional<String> getUuid();

    NutsOptional<Set<String>> getExtensionsSet();

    NutsOptional<NutsDescriptor> getRuntimeBootDescriptor();

    @Override
    NutsWorkspaceBootOptionsBuilder builder();

    @Override
    NutsWorkspaceBootOptions readOnly();
}
