package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NutsWorkspaceBootOptions extends NutsWorkspaceOptions{
    NutsWorkspaceOptions getUserOptions();

    @Override
    NutsWorkspaceBootOptionsBuilder builder();

    String getBootRepositories();

    NutsClassLoaderNode getRuntimeBootDependencyNode();

    List<NutsDescriptor> getExtensionBootDescriptors();

    List<NutsClassLoaderNode> getExtensionBootDependencyNodes();

    NutsBootWorkspaceFactory getBootWorkspaceFactory();

    List<URL> getClassWorldURLs();

    ClassLoader getClassWorldLoader();

    String getUuid();

    Set<String> getExtensionsSet();

    NutsDescriptor getRuntimeBootDescriptor();

    @Override
    NutsWorkspaceBootOptions readOnly();
}
