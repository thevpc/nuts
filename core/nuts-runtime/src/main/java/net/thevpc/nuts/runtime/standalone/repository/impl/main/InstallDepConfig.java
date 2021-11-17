package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsDependencyScope;
import net.thevpc.nuts.NutsId;

import java.util.Objects;

public class InstallDepConfig extends NutsConfigItem {
    private NutsId id;
    private NutsDependencyScope scope;

    public InstallDepConfig() {
    }

    public InstallDepConfig(NutsId id, NutsDependencyScope scope) {
        this.id = id;
        this.scope = scope;
    }

    public NutsId getId() {
        return id;
    }

    public InstallDepConfig setId(NutsId id) {
        this.id = id;
        return this;
    }

    public NutsDependencyScope getScope() {
        return scope;
    }

    public InstallDepConfig setScope(NutsDependencyScope scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstallDepConfig that = (InstallDepConfig) o;
        return Objects.equals(id, that.id) && scope == that.scope;
    }
}
