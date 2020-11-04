package net.thevpc.nuts.runtime.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.DefaultNutsClassifierMappingBuilder;
import net.thevpc.nuts.runtime.config.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.config.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.format.DefaultNutsDescriptorFormat;
import net.thevpc.nuts.runtime.parser.DefaultNutsDescriptorParser;

public class DefaultNutsDescriptorManager implements NutsDescriptorManager {
    private NutsWorkspace workspace;

    public DefaultNutsDescriptorManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsDescriptorParser parser() {
        return new DefaultNutsDescriptorParser(workspace);
    }

    @Override
    public NutsDescriptorBuilder descriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    @Override
    public NutsClassifierMappingBuilder classifierBuilder() {
        return new DefaultNutsClassifierMappingBuilder();
    }

    @Override
    public NutsIdLocationBuilder locationBuilder() {
        return new DefaultNutsIdLocationBuilder();
    }

    @Override
    public NutsArtifactCallBuilder callBuilder() {
        return new DefaultNutsArtifactCallBuilder();
    }

    @Override
    public NutsDescriptorFormat formatter() {
        return new DefaultNutsDescriptorFormat(getWorkspace());
    }

    @Override
    public NutsDescriptorFormat formatter(NutsDescriptor value) {
        return formatter().value(value);
    }

    @Override
    public NutsDescriptorFilterManager filter() {
        return getWorkspace().filters().descriptor();
    }
}
