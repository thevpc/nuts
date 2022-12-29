package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NConfigItem;
import net.thevpc.nuts.NDependencyScope;
import net.thevpc.nuts.NId;

import java.util.Objects;

public class InstallDepConfig extends NConfigItem implements Cloneable {
    private NId id;
    private NDependencyScope scope;

    public InstallDepConfig() {
    }

    public InstallDepConfig(NId id, NDependencyScope scope) {
        this.id = id;
        this.scope = scope;
    }

    public NId getId() {
        return id;
    }

    public InstallDepConfig setId(NId id) {
        this.id = id;
        return this;
    }

    public NDependencyScope getScope() {
        return scope;
    }

    public InstallDepConfig setScope(NDependencyScope scope) {
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

    public InstallDepConfig copy() {
        try {
            return (InstallDepConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
