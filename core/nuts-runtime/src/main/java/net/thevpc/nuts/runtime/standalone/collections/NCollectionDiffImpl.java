package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NCollectionDiff;
import net.thevpc.nuts.collections.NCollectionDiffApplier;
import net.thevpc.nuts.collections.NCollectionDiffChange;
import net.thevpc.nuts.util.NDiffMode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * NCollectionDiff class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCollectionDiffImpl<T> implements NCollectionDiff<T> {
    private final Collection<T> oldItems;
    private final Collection<T> newItems;
    private final List<NCollectionDiffChange<T>> changes = new ArrayList<>();

    /**
     * N collection diff.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return n collection diff result
     */
    public NCollectionDiffImpl(Collection<T> oldItems, Collection<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    /**
     * Old items.
     *
     * @return old items result
     */
    @Override
    public Collection<T> oldItems() {
        return oldItems;
    }

    /**
     * New items.
     *
     * @return new items result
     */
    @Override
    public Collection<T> newItems() {
        return newItems;
    }

    /**
     * Register.
     *
     * @param a a
     */
    void register(NCollectionDiffChange<T> a) {
        changes.add(a);
    }

    /**
     * Any add.
     *
     * @return any add result
     */
    @Override
    public boolean anyAdd() {
        return changes.stream().anyMatch(x -> x.mode() == NDiffMode.ADDED);
    }

    @Override
    public Iterator<NCollectionDiffChange<T>> iterator() {
        return Collections.unmodifiableList(changes).iterator();
    }

    /**
     * Any remove.
     *
     * @return any remove result
     */
    @Override
    public boolean anyRemove() {
        return changes.stream().anyMatch(x -> x.mode() == NDiffMode.REMOVED);
    }

    /**
     * Any change.
     *
     * @return any change result
     */
    @Override
    public boolean anyChange() {
        return changes.stream().anyMatch(x -> x.mode() != NDiffMode.UNCHANGED);
    }

    /**
     * Adds the specified d.
     *
     * @return added result
     */
    @Override
    public List<T> added() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.ADDED).map(x -> x.newValue()).collect(Collectors.toList());
    }

    /**
     * Removes removed.
     *
     * @return removed result
     */
    @Override
    public List<T> removed() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.REMOVED).map(x -> x.oldValue()).collect(Collectors.toList());
    }

    /**
     * Changed.
     *
     * @return changed result
     */
    @Override
    public List<NCollectionDiffChange<T>> changed() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.CHANGED).collect(Collectors.toList());
    }

    /**
     * Unchanged.
     *
     * @return unchanged result
     */
    @Override
    public List<NCollectionDiffChange<T>> unchanged() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.UNCHANGED).collect(Collectors.toList());
    }

    private static class MappedCollection<A,B> extends AbstractCollection<B>{
        private Collection<A> base;
        private Function<A, B> f;

        /**
         * Mapped collection.
         *
         * @param base base
         * @param f f
         * @return mapped collection result
         */
        public MappedCollection(Collection<A> base, Function<A, B> f) {
            this.base = base;
            this.f = f;
        }

        @Override
        public Iterator<B> iterator() {
            Iterator<A> baseIt = base.iterator();
            return new Iterator<B>() {
                @Override
                public boolean hasNext() {
                    return baseIt.hasNext();
                }

                @Override
                public B next() {
                    return f.apply(baseIt.next());
                }
            };
        }

        @Override
        public int size() {
            return base.size();
        }
    }
    /**
     * Map.
     *
     * @param f f
     * @return map result
     */
    @Override
    public <H> NCollectionDiff<H> map(Function<T, H> f) {
        NCollectionDiffImpl<H> d = new NCollectionDiffImpl<>(new MappedCollection<T,H>(oldItems,f),new MappedCollection<T,H>(newItems,f));
        d.changes.addAll(changes.stream().map(x -> x.map(f)).collect(Collectors.toList()));
        return d;
    }


    /**
     * Sort.
     */
    void sort() {
        changes.sort(Comparator.comparingInt(a -> {
            if(a instanceof NCollectionDiffChangeImpl){
                return ((NCollectionDiffChangeImpl<T>) a).newPos;
            }
            return 0;
        }));
    }

    /**
     * Apply.
     *
     * @param a a
     */
    @Override
    public void apply(NCollectionDiffApplier<T> a) {
        for (T e : removed()) {
            a.remove(e, this);
        }
        for (T e : added()) {
            a.add(e, this);
        }
        for (NCollectionDiffChange<T> e : changed()) {
            a.update(e.newValue(), e.oldValue(), this);
        }

    }
}
