package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NOptional;

import java.util.Set;

public abstract class NDefinitionDelegate implements NDefinition {


    @Override
    public NId getId() {
        return getBase().getId();
    }

    protected abstract NDefinition getBase();

    @Override
    public NDescriptor getDescriptor() {
        return getBase().getDescriptor();
    }

    @Override
    public NOptional<Set<NDescriptorFlag>> getEffectiveFlags() {
        return getBase().getEffectiveFlags();
    }

    @Override
    public NOptional<NPath> getContent() {
        return getBase().getContent();
    }

    @Override
    public NOptional<NInstallInformation> getInstallInformation() {
        return getBase().getInstallInformation();
    }

    @Override
    public NOptional<NDescriptor> getEffectiveDescriptor() {
        return getBase().getEffectiveDescriptor();
    }

    @Override
    public NOptional<NDependencies> getDependencies() {
        return getBase().getDependencies();
    }

    @Override
    public NId getApiId() {
        return getBase().getApiId();
    }

    @Override
    public int compareTo(NDefinition other) {
        return getBase().compareTo(other);
    }

    @Override
    public String getRepositoryUuid() {
        return getBase().getRepositoryUuid();
    }

    @Override
    public NDependency getDependency() {
        return getBase().getDependency();
    }

    @Override
    public String getRepositoryName() {
        return getBase().getRepositoryName();
    }

    @Override
    public NDefinitionBuilder builder() {
        return getBase().builder();
    }

}
