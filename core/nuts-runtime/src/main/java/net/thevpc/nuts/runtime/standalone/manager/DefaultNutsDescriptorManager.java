package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsClassifierMappingBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.core.format.DefaultNutsDescriptorFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsDescriptorParser;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsDescriptorManager implements NutsDescriptorManager {
    private NutsWorkspace workspace;

        private NutsSession session;
public DefaultNutsDescriptorManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDescriptorManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsDescriptorParser parser() {
        return new DefaultNutsDescriptorParser(workspace).setSession(getSession());
    }


    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(workspace, session);
    }

    @Override
    public NutsDescriptorBuilder descriptorBuilder() {
        checkSession();
        return new DefaultNutsDescriptorBuilder(getSession());
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
        return new DefaultNutsDescriptorFormat(getWorkspace()).setSession(getSession());
    }

    @Override
    public NutsDescriptorFormat formatter(NutsDescriptor value) {
        return formatter().value(value);
    }

    @Override
    public NutsDescriptorFilterManager filter() {
        return getWorkspace().filters().descriptor().setSession(getSession());
    }
}
