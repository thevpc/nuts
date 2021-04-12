package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyBuilder;
import net.thevpc.nuts.runtime.core.format.DefaultNutsDependencyFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsDependencyParser;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;

import java.util.Set;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsDependencyManager implements NutsDependencyManager {

    private NutsWorkspace workspace;

    private NutsSession session;

    public DefaultNutsDependencyManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDependencyManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(workspace, session);
    }

    @Override
    public NutsDependencyParser parser() {
        checkSession();
        return new DefaultNutsDependencyParser(getSession());
    }

    @Override
    public NutsDependencyBuilder builder() {
        checkSession();
        return new DefaultNutsDependencyBuilder(getSession());
    }

    @Override
    public NutsDependencyFormat formatter() {
        return new DefaultNutsDependencyFormat(getWorkspace()).setSession(getSession());
    }

    @Override
    public NutsDependencyFormat formatter(NutsDependency dependency) {
        return formatter().setValue(dependency).setSession(getSession());
    }

    @Override
    public NutsDependencyFilterManager filter() {
        return getWorkspace().filters().dependency().setSession(getSession());
    }

    @Override
    public Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other) {
        return NutsDependencyScopes.expand(other);
    }
}
