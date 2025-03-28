package net.thevpc.nuts.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simple List builder
 * @param <T>
 */
public class NListBuilder<T> {
    private List<T> base;
    private boolean ignoreNulls;

    public static NListBuilder of() {
        return new NListBuilder(new ArrayList<>());
    }

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public NListBuilder<T> setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public NListBuilder(List<T> base) {
        this.base = base == null ? new ArrayList<T>() : base;
    }

    public NListBuilder<T> add(T t) {
        if (ignoreNulls) {
            if (t != null) {
                base.add(t);
            }
        } else {
            base.add(t);
        }
        return this;
    }

    public NListBuilder<T> addAll(T... t) {
        if (ignoreNulls) {
            if (t != null) {
                for (T t1 : t) {
                    if (t1 != null) {
                        base.add(t1);
                    }
                }
            }
        } else {
            base.addAll(Arrays.asList(t));
        }
        return this;
    }

    public NListBuilder<T> addAll(Collection<T> t) {
        if (ignoreNulls) {
            if (t != null) {
                for (T t1 : t) {
                    if (t1 != null) {
                        base.add(t1);
                    }
                }
            }
        } else {
            base.addAll(t);
        }
        return this;
    }

    public int size() {
        return base.size();
    }

    public List<T> build() {
        return base;
    }
}
