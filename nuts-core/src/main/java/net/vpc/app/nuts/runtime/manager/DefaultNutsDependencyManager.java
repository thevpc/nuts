package net.vpc.app.nuts.runtime.manager;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.config.DefaultNutsDependencyBuilder;
import net.vpc.app.nuts.runtime.format.DefaultNutsDependencyFormat;
import net.vpc.app.nuts.runtime.parser.DefaultNutsDependencyParser;
import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;

import java.util.Set;

public class DefaultNutsDependencyManager implements NutsDependencyManager {
    private NutsWorkspace workspace;

    public DefaultNutsDependencyManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsDependencyParser parser() {
        return new DefaultNutsDependencyParser(workspace);
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public NutsDependencyFormat formatter() {
        return new DefaultNutsDependencyFormat(getWorkspace());
    }

    @Override
    public NutsDependencyFormat formatter(NutsDependency dependency) {
        return formatter().setValue(dependency);
    }

    @Override
    public NutsDependencyFilterManager filter() {
        return getWorkspace().filters().dependency();
    }

    @Override
    public Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other) {
        return NutsDependencyScopes.expand(other);
    }
}
