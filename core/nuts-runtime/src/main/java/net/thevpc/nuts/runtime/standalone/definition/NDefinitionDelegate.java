package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NInstallInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NOptional;

import java.util.Set;

public abstract class NDefinitionDelegate implements NDefinition {


    @Override
    public NId id() {
        return getBase().id();
    }

    protected abstract NDefinition getBase();

    @Override
    public NDescriptor descriptor() {
        return getBase().descriptor();
    }

    @Override
    public NOptional<Set<NDescriptorFlag>> effectiveFlags() {
        return getBase().effectiveFlags();
    }

    @Override
    public NOptional<NPath> content() {
        return getBase().content();
    }

    @Override
    public NOptional<NInstallInformation> installInformation() {
        return getBase().installInformation();
    }

    @Override
    public NOptional<NDescriptor> effectiveDescriptor() {
        return getBase().effectiveDescriptor();
    }

    @Override
    public NOptional<NDependencies> dependencies() {
        return getBase().dependencies();
    }

    @Override
    public NId apiId() {
        return getBase().apiId();
    }

    @Override
    public int compareTo(NDefinition other) {
        return getBase().compareTo(other);
    }

    @Override
    public String repositoryUuid() {
        return getBase().repositoryUuid();
    }

    @Override
    public NDependency dependency() {
        return getBase().dependency();
    }

    @Override
    public String repositoryName() {
        return getBase().repositoryName();
    }

    @Override
    public NDefinitionBuilder builder() {
        return getBase().builder();
    }

}
