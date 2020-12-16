package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsClassifierMappingBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.standalone.format.DefaultNutsDescriptorFormat;
import net.thevpc.nuts.runtime.standalone.parser.DefaultNutsDescriptorParser;

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
