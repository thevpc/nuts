package net.thevpc.nuts;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.Set;

public interface NDefinitionBuilder extends Serializable, Comparable<NDefinitionBuilder> {
    NDependency getDependency();

    NDefinitionBuilder setDependency(NDependency dependency);

    NDefinition build();

    String getRepositoryUuid();

    String getRepositoryName();

    NDefinitionBuilder setId(NId id);

    NId getId();

    boolean isTemporary();

    NDescriptor getDescriptor();

    NDefinitionBuilder copy();

    NOptional<NPath> getContent();

    NOptional<NDescriptor> getEffectiveDescriptor();

    NOptional<NInstallInformation> getInstallInformation();

    NOptional<NDependencies> getDependencies();

    NDefinitionBuilder setContent(NPath content);

    NDefinitionBuilder setDescriptor(NDescriptor descriptor);

    NDefinitionBuilder setEffectiveFlags(Set<NDescriptorFlag> effectiveFlags);

    NDefinitionBuilder setEffectiveDescriptor(NDescriptor effectiveDescriptor);

    NDefinitionBuilder setInstallInformation(NInstallInformation install);

    NDefinitionBuilder setDependencies(NDependencies dependencies);

    NDefinitionBuilder setApiId(NId apiId);

    NId getApiId();

    NDefinitionBuilder setRepositoryUuid(String repositoryUuid);

    NDefinitionBuilder setRepositoryName(String repositoryName);

    NOptional<Set<NDescriptorFlag>> getEffectiveFlags();
}
