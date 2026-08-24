package net.thevpc.nuts.collections;

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
public class NCollectionDiff<T> implements Iterable<NCollectionDiffChange<T>> {
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
    public NCollectionDiff(Collection<T> oldItems, Collection<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    /**
     * Old items.
     *
     * @return old items result
     */
    public Collection<T> oldItems() {
        return oldItems;
    }

    /**
     * New items.
     *
     * @return new items result
     */
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
    public boolean anyRemove() {
        return changes.stream().anyMatch(x -> x.mode() == NDiffMode.REMOVED);
    }

    /**
     * Any change.
     *
     * @return any change result
     */
    public boolean anyChange() {
        return changes.stream().anyMatch(x -> x.mode() != NDiffMode.UNCHANGED);
    }

    /**
     * Adds the specified d.
     *
     * @return added result
     */
    public List<T> added() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.ADDED).map(x -> x.newValue()).collect(Collectors.toList());
    }

    /**
     * Removes removed.
     *
     * @return removed result
     */
    public List<T> removed() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.REMOVED).map(x -> x.oldValue()).collect(Collectors.toList());
    }

    /**
     * Changed.
     *
     * @return changed result
     */
    public List<NCollectionDiffChange<T>> changed() {
        return changes.stream().filter(x -> x.mode() == NDiffMode.CHANGED).collect(Collectors.toList());
    }

    /**
     * Unchanged.
     *
     * @return unchanged result
     */
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
    public <H> NCollectionDiff<H> map(Function<T, H> f) {
        NCollectionDiff<H> d = new NCollectionDiff<>(new MappedCollection<T,H>(oldItems,f),new MappedCollection<T,H>(newItems,f));
        d.changes.addAll(changes.stream().map(x -> x.map(f)).collect(Collectors.toList()));
        return d;
    }

    /**
     * Diff list.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff list result
     */
    public static <T> NCollectionDiff<T> diffList(List<T> oldItems, List<T> newItems) {
        /**
         * Diff list.
         *
         * @param oldItems old items
         * @param newItems new items
         * @param x x
         * @return diff list result
         */
        return diffList(oldItems, newItems, x -> x);
    }

    /**
     * Diff map entries.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map entries result
     */
    public static <K, V> NCollectionDiff<Map.Entry<K, V>> diffMapEntries(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff list.
         *
         * @param oldItems.entrySet() old items.entry set()
         * @param newItems.entrySet() new items.entry set()
         * @param Map.Entry::getKey map. entry::get key
         * @return diff list result
         */
        return diffList(oldItems.entrySet(), newItems.entrySet(), Map.Entry::getKey);
    }

    /**
     * Diff map values.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map values result
     */
    public static <K, V> NCollectionDiff<V> diffMapValues(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff map entries.
         *
         * @param oldItems old items
         * @param newItems).map(Map.Entry::getValue new items).map( map. entry::get value
         * @return diff map entries result
         */
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getValue);
    }

    /**
     * Diff map keys.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return diff map keys result
     */
    public static <K, V> NCollectionDiff<K> diffMapKeys(Map<K, V> oldItems, Map<K, V> newItems) {
        /**
         * Diff map entries.
         *
         * @param oldItems old items
         * @param newItems).map(Map.Entry::getKey new items).map( map. entry::get key
         * @return diff map entries result
         */
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getKey);
    }

    /**
     * Diff list.
     *
     * @param oldItems old items
     * @param newItems new items
     * @param id id
     * @return diff list result
     */
    public static <T, K> NCollectionDiff<T> diffList(Collection<T> oldItems, Collection<T> newItems, Function<T, K> id) {
        return new NCollectionDiffBuilder<T, K>().oldItems(oldItems).newItems(newItems).idResolver(id).diff();
    }

    /**
     * Creates a new instance of of.
     *
     * @param oldItems old items
     * @param newItems new items
     * @return of result
     */
    public static <T, K> NCollectionDiffBuilder<T, K> of(Collection<T> oldItems, Collection<T> newItems) {
        return new NCollectionDiffBuilder<T, K>().oldItems(oldItems).newItems(newItems);
    }


    /**
     * Sort.
     */
    void sort() {
        changes.sort(Comparator.comparingInt(a -> a.newPos));
    }

    /**
     * Apply.
     *
     * @param a a
     */
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
