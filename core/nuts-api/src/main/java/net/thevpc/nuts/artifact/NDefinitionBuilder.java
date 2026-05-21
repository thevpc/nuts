package net.thevpc.nuts.artifact;

import net.thevpc.nuts.command.NInstallInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.io.Serializable;
import java.util.Set;

public interface NDefinitionBuilder extends Serializable, Comparable<NDefinitionBuilder> {
    NDependency dependency();

    NDefinitionBuilder dependency(NDependency dependency);

    NDefinition build();

    @NGetter
    String repositoryUuid();

    @NGetter
    String repositoryName();

    @NSetter
    NDefinitionBuilder id(NId id);

    NId id();

    boolean isTemporary();

    @NGetter
    NDescriptor descriptor();

    NDefinitionBuilder copy();

    NOptional<NPath> content();

    NOptional<NDescriptor> effectiveDescriptor();

    NOptional<NInstallInformation> installInformation();

    NOptional<NDependencies> dependencies();

    NDefinitionBuilder content(NPath content);

    NDefinitionBuilder descriptor(NDescriptor descriptor);

    NDefinitionBuilder effectiveFlags(Set<NDescriptorFlag> effectiveFlags);

    NDefinitionBuilder effectiveDescriptor(NDescriptor effectiveDescriptor);

    NDefinitionBuilder installInformation(NInstallInformation install);

    NDefinitionBuilder dependencies(NDependencies dependencies);

    NDefinitionBuilder apiId(NId apiId);

    NId apiId();

    NDefinitionBuilder repositoryUuid(String repositoryUuid);

    NDefinitionBuilder repositoryName(String repositoryName);

    NOptional<Set<NDescriptorFlag>> effectiveFlags();
}
