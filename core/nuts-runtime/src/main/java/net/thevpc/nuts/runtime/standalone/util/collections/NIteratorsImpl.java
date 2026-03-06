package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NIteratorsImpl {

    public static <T> NIterator<T> withDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose) {
        return new NIteratorWithDescription<>(base, description, onClose);
    }

    public static class NIteratorOnClose<T> extends NIteratorOnFinish<T> {
        public NIteratorOnClose(Iterator<T> base, NRunnable r) {
            super(base, r);
        }
    }

    public static <T> NIterator<T> autoClosable(NIterator<T> t, NRunnable close) {
        if (close == null) {
            return t;
        }
        if (t instanceof NIteratorUtils.NIteratorOnClose) {
            if (((NIteratorUtils.NIteratorOnClose<T>) t).getCloseRunnable() == close) {
                return t;
            }
        }
        return new NIteratorUtils.NIteratorOnClose<>(t, close);
    }

    public static <T> NIterator<T> safe(NIteratorErrorHandlerType type, NIterator<T> t) {
        return new NErrorHandlerIterator(type, t);
    }

    public static <T> NIterator<T> safeIgnore(NIterator<T> t) {
        return new NErrorHandlerIterator(NIteratorErrorHandlerType.IGNORE, t);
    }

    public static <T> NIterator<T> safePostpone(NIterator<T> t) {
        return new NErrorHandlerIterator(NIteratorErrorHandlerType.POSTPONE, t);
    }

    public static <T> boolean isNullOrEmpty(Iterator<T> t) {
        if (t == null) {
            return true;
        }
        if (t == NIterator.ofEmpty()) {
            return true;
        }
        if (t == Collections.emptyIterator()) {
            return true;
        }
        if (t instanceof NIteratorWithDescription) {
            NIterator<T> base = ((NIteratorWithDescription<T>) t).getBase();
            return isNullOrEmpty(base);
        }
        if (t instanceof NIteratorAdapter) {
            Iterator<T> base = ((NIteratorAdapter<T>) t).getBase();
            return isNullOrEmpty(base);
        }
        return t instanceof NIteratorEmpty;
    }

    public static <T> NIterator<T> nonNull(NIterator<T> t) {
        if (t == null) {
            return emptyIterator();
        }
        return t;
    }

    public static <T> NIterator<T> concat(List<NIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return emptyIterator();
        }
        NQueueIterator<T> t = new NQueueIterator<>();
        for (NIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                if (it instanceof NQueueIterator) {
                    NQueueIterator tt = (NQueueIterator) it;
                    for (NIterator it1 : tt.getChildren()) {
                        t.add(it1);
                    }
                } else {
                    t.add(it);
                }
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <T> NIterator<T> coalesce2(List<NIterator<T>> all) {
        return coalesce((List) all);
    }

    public static <T> NIterator<T> coalesce(NIterator<? extends T>... all) {
        return coalesce(Arrays.asList(all));
    }

    public static <T> NIterator<T> concat(NIterator<? extends T>... all) {
        return concat(Arrays.asList(all));
    }

    public static <T> NIterator<T> concatLists(List<NIterator<? extends T>>... all) {
        List<NIterator<? extends T>> r = new ArrayList<>();
        if (all != null) {
            for (List<NIterator<? extends T>> a : all) {
                if (a != null) {
                    for (NIterator<? extends T> b : a) {
                        if (b != null) {
                            r.add(b);
                        }
                    }
                }
            }
        }
        return concat(r);
    }

    public static <T> NIterator<T> coalesce(List<NIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return emptyIterator();
        }
        NCoalesceIterator<T> t = new NCoalesceIterator<>();
        for (NIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                t.add(it);
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <F, T> NIterator<T> convertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
        if (isNullOrEmpty(from)) {
            return emptyIterator();
        }
        return new NConvertedNonNullIterator<>(from, converter, name);
    }

    public static <T> List<T> toList(Iterator<T> it) {
        if (isNullOrEmpty(it)) {
            return Collections.emptyList();
        }
        List<T> a = new ArrayList<T>();
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> Set<T> toSet(NIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        LinkedHashSet<T> a = new LinkedHashSet<T>();
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> Set<T> toTreeSet(NIterator<T> it, NComparator<T> c) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        TreeSet<T> a = new TreeSet<T>(c);
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> NIterator<T> sort(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
        return new NIteratorSorted<>(it, c, removeDuplicates);
    }

    public static <T> NIterator<T> distinct(NIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
        Predicate<T> filter = new NDistinctPredicate<>();
        return new NFilteredIterator<>(it, filter);
    }

    public static <F, T> NIterator<F> distinct(NIterator<F> it, final Function<F, T> converter) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
        if (converter == null) {
            Predicate<F> filter = new NDistinctPredicate<>();
            return new NFilteredIterator<>(it, filter);
        }
        Predicate<F> filter = new NDistinctWithConverterPredicate<>(converter);
        return new NFilteredIterator<>(it, filter);
    }

    public static <T> NIterator<T> collector(Iterator<T> it, Consumer<T> consumer) {
        if (it == null) {
            return new NIteratorFromJavaIterator2<>(null, emptyIterator(), consumer);
        }
        return new NIteratorFromJavaIterator2<>(null, it, consumer);
    }

    public static <T> NIterator<T> nullifyIfEmpty(NIterator<T> other) {
        if (other == null) {
            return null;
        }
        if (other instanceof NPushBackIterator) {
            NPushBackIterator<T> b = (NPushBackIterator<T>) other;
            if (!b.isEmpty()) {
                return b;
            } else {
                return null;
            }
        }
        NPushBackIterator<T> b = new NPushBackIterator<>(other);
        if (!b.isEmpty()) {
            return b;
        } else {
            return null;
        }
    }

    public static <T> NIteratorBuilder<T> builderOfCoalesce(List<NIterator<? extends T>> t) {
        return new NIteratorBuilderImpl<>(
                NIteratorUtils.coalesce(t)
        );
    }

    public static <T> NIteratorBuilder<T> builderOfConcat(List<NIterator<? extends T>> t) {
        return new NIteratorBuilderImpl<>(
                NIteratorUtils.concat(t)
        );
    }

    public static <T> NIteratorBuilder<T> builder(Iterator<T> t) {
        return new NIteratorBuilderImpl<>(t);
    }

    public static <T> NIteratorBuilder<T> builderOfRunnable(NRunnable t) {
        return (NIteratorBuilder) builder(
                emptyIterator()
        ).onStart(t);
    }

//    public <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n) {
//        return ofRunnable(NRunnable.of(t, n));
//    }

    public static <T> NIteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return builderOfRunnable(NRunnable.of(t).withDescription(NDescribables.ofDesc(n)));
    }
//
//    public <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> NIteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name) {
        return builder(new NSupplierIteratorJ<T>(from, name).withDescription(name));
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, NElement n) {
        return ofArrayValues(t, () -> n);
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return ofArrayValues(t, () -> NElement.ofString(n));
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return builder(t == null ? emptyIterator() :
                new NIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n)
        );
    }

    public static <T> NIterator<T> emptyIterator() {
        return NIteratorBuilderImpl.EMPTY_ITERATOR;
    }

    public static <T> NIteratorBuilder<T> emptyBuilder() {
        return builder(NIteratorBuilderImpl.EMPTY_ITERATOR);
    }

    public static <T> NIteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        if (from == null) {
            return emptyBuilder();
        }
        return builder(new NFlatMapIterator<>(from, Collection::iterator));
    }


}
