package net.thevpc.nuts.boot;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NWorkspaceOptions;
import net.thevpc.nuts.spi.NBootWorkspaceFactory;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface NBootOptions extends NWorkspaceOptions {
    NOptional<NWorkspaceOptions> getUserOptions();

    NOptional<String> getBootRepositories();

    NOptional<NClassLoaderNode> getRuntimeBootDependencyNode();

    NOptional<List<NDescriptor>> getExtensionBootDescriptors();

    NOptional<List<NClassLoaderNode>> getExtensionBootDependencyNodes();

    NOptional<NBootWorkspaceFactory> getBootWorkspaceFactory();

    NOptional<List<URL>> getClassWorldURLs();

    NOptional<ClassLoader> getClassWorldLoader();

    NOptional<String> getUuid();

    NOptional<Set<String>> getExtensionsSet();

    NOptional<NDescriptor> getRuntimeBootDescriptor();

    @Override
    NBootOptionsBuilder builder();

    @Override
    NBootOptions readOnly();
}
