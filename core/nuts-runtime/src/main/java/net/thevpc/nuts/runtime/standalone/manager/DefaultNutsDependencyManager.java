package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyBuilder;
import net.thevpc.nuts.runtime.core.format.DefaultNutsDependencyFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsDependencyParser;

import java.util.Collections;
import java.util.Set;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsDependencyManager implements NutsDependencyManager {

    private NutsWorkspace ws;

    private NutsSession session;

    public DefaultNutsDependencyManager(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDependencyManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
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
        return getSession().filters().dependency().setSession(getSession());
    }

    @Override
    public Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other) {
        return other==null? Collections.emptySet() : other.toScopes();
    }
}
