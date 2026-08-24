package net.thevpc.nuts.collections;

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

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NListBuilder of() {
        return new NListBuilder(new ArrayList<>());
    }

    /**
     * Checks if is ignore nulls.
     *
     * @return is ignore nulls result
     */
    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    /**
     * Sets the ignore nulls.
     *
     * @param ignoreNulls ignore nulls
     * @return set ignore nulls result
     */
    public NListBuilder<T> setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    /**
     * N list builder.
     *
     * @param base base
     * @return n list builder result
     */
    public NListBuilder(List<T> base) {
        this.base = base == null ? new ArrayList<T>() : base;
    }

    /**
     * Adds add.
     *
     * @param t t
     * @return add result
     */
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

    /**
     * Adds the specified all.
     *
     * @param t t
     * @return add all result
     */
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

    /**
     * Adds the specified all.
     *
     * @param t t
     * @return add all result
     */
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

    /**
     * Size.
     *
     * @return size result
     */
    public int size() {
        return base.size();
    }

    /**
     * Build.
     *
     * @return build result
     */
    public List<T> build() {
        return base;
    }
}
