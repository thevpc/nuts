package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsDescriptorFormat;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorPropertyBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsEnvConditionBuilder;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsDescriptorParser;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsDescriptorManager implements NutsDescriptorManager {
    private NutsWorkspace workspace;

    private NutsSession session;

    public DefaultNutsDescriptorManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsDescriptorParser parser() {
        return new DefaultNutsDescriptorParser(workspace).setSession(getSession());
    }

    @Override
    public NutsDescriptorBuilder descriptorBuilder() {
        checkSession();
        return new DefaultNutsDescriptorBuilder(getSession());
    }

    @Override
    public NutsEnvConditionBuilder envConditionBuilder() {
        checkSession();
        return new DefaultNutsEnvConditionBuilder(getSession());
    }

    @Override
    public NutsDescriptorPropertyBuilder propertyBuilder() {
        checkSession();
        return new DefaultNutsDescriptorPropertyBuilder(getSession());
    }

    @Override
    public NutsIdLocationBuilder locationBuilder() {
        checkSession();
        return new DefaultNutsIdLocationBuilder(getSession());
    }

    @Override
    public NutsArtifactCallBuilder callBuilder() {
        checkSession();
        return new DefaultNutsArtifactCallBuilder(getSession());
    }

    @Override
    public NutsDescriptorFormat formatter() {
        checkSession();
        return new DefaultNutsDescriptorFormat(getWorkspace()).setSession(getSession());
    }

    @Override
    public NutsDescriptorFormat formatter(NutsDescriptor value) {
        return formatter().setValue(value);
    }

    @Override
    public NutsDescriptorFilterManager filter() {
        checkSession();
        return getSession().filters().descriptor().setSession(getSession());
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDescriptorManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(workspace, session);
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(workspace, session);
    }
}
