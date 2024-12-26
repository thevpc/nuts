package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import java.util.Objects;

public class TraversedTreeKey {
    private final Object obj;

    public TraversedTreeKey(Object obj) {
        this.obj = obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraversedTreeKey that = (TraversedTreeKey) o;
        return obj == that.obj;
    }

    @Override
    public int hashCode() {
        return Objects.hash(obj);
    }
}
