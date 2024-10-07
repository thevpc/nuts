package net.thevpc.nuts.lib.common.collections;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NCollectionDiff<T> implements Iterable<NCollectionDiffChange<T>> {
    private final Collection<T> oldItems;
    private final Collection<T> newItems;
    private final List<NCollectionDiffChange<T>> changes = new ArrayList<>();

    public NCollectionDiff(Collection<T> oldItems, Collection<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    public Collection<T> getOldItems() {
        return oldItems;
    }

    public Collection<T> getNewItems() {
        return newItems;
    }

    void register(NCollectionDiffChange<T> a) {
        changes.add(a);
    }

    public boolean anyAdd() {
        return changes.stream().anyMatch(x -> x.getMode() == NDiffMode.ADDED);
    }

    @Override
    public Iterator<NCollectionDiffChange<T>> iterator() {
        return Collections.unmodifiableList(changes).iterator();
    }

    public boolean anyRemove() {
        return changes.stream().anyMatch(x -> x.getMode() == NDiffMode.REMOVED);
    }

    public boolean anyChange() {
        return changes.stream().anyMatch(x -> x.getMode() != NDiffMode.UNCHANGED);
    }

    public List<T> getAdded() {
        return changes.stream().filter(x -> x.getMode() == NDiffMode.ADDED).map(x -> x.getNewValue()).collect(Collectors.toList());
    }

    public List<T> getRemoved() {
        return changes.stream().filter(x -> x.getMode() == NDiffMode.REMOVED).map(x -> x.getOldValue()).collect(Collectors.toList());
    }

    public List<NCollectionDiffChange<T>> getChanged() {
        return changes.stream().filter(x -> x.getMode() == NDiffMode.CHANGED).collect(Collectors.toList());
    }

    public List<NCollectionDiffChange<T>> getUnchanged() {
        return changes.stream().filter(x -> x.getMode() == NDiffMode.UNCHANGED).collect(Collectors.toList());
    }

    private static class MappedCollection<A,B> extends AbstractCollection<B>{
        private Collection<A> base;
        private Function<A, B> f;

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
    public <H> NCollectionDiff<H> map(Function<T, H> f) {
        NCollectionDiff<H> d = new NCollectionDiff<>(new MappedCollection<T,H>(oldItems,f),new MappedCollection<T,H>(newItems,f));
        d.changes.addAll(changes.stream().map(x -> x.map(f)).collect(Collectors.toList()));
        return d;
    }

    public static <T> NCollectionDiff<T> diffList(List<T> oldItems, List<T> newItems) {
        return diffList(oldItems, newItems, x -> x);
    }

    public static <K, V> NCollectionDiff<Map.Entry<K, V>> diffMapEntries(Map<K, V> oldItems, Map<K, V> newItems) {
        return diffList(oldItems.entrySet(), newItems.entrySet(), Map.Entry::getKey);
    }

    public static <K, V> NCollectionDiff<V> diffMapValues(Map<K, V> oldItems, Map<K, V> newItems) {
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getValue);
    }

    public static <K, V> NCollectionDiff<K> diffMapKeys(Map<K, V> oldItems, Map<K, V> newItems) {
        return diffMapEntries(oldItems, newItems).map(Map.Entry::getKey);
    }

    public static <T, K> NCollectionDiff<T> diffList(Collection<T> oldItems, Collection<T> newItems, Function<T, K> id) {
        return new NCollectionDiffBuilder<T, K>().setOldItems(oldItems).setNewItems(newItems).setIdResolver(id).diff();
    }

    public static <T, K> NCollectionDiffBuilder<T, K> of(Collection<T> oldItems, Collection<T> newItems) {
        return new NCollectionDiffBuilder<T, K>().setOldItems(oldItems).setNewItems(newItems);
    }


    void sort() {
        changes.sort(Comparator.comparingInt(a -> a.newPos));
    }

    public void apply(NCollectionDiffApplier<T> a) {
        for (T e : getRemoved()) {
            a.remove(e, this);
        }
        for (T e : getAdded()) {
            a.add(e, this);
        }
        for (NCollectionDiffChange<T> e : getChanged()) {
            a.update(e.getNewValue(), e.getOldValue(), this);
        }

    }
}
