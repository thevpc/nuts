/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.util.NutsComparator;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.NutsSession;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class IteratorUtils {

    //    public static FileDepthFirstIterator dsf(File file) {
//        return new FileDepthFirstIterator(file);
//    }

    public static <T> NutsIterator<T> safe(IteratorErrorHandlerType type, NutsIterator<T> t, NutsSession session) {
        return new ErrorHandlerIterator(type, t,session);
    }

    public static <T> NutsIterator<T> safeIgnore(NutsIterator<T> t,NutsSession session) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.IGNORE, t,session);
    }

    public static <T> NutsIterator<T> safePospone(NutsIterator<T> t,NutsSession session) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.POSTPONE, t,session);
    }

    public static <T> boolean isNullOrEmpty(Iterator<T> t) {
        return t == null || t == IteratorBuilder.EMPTY_ITERATOR;
    }

    public static <T> NutsIterator<T> nonNull(NutsIterator<T> t) {
        if (t == null) {
            return IteratorBuilder.emptyIterator();
        }
        return t;
    }

    public static <T> NutsIterator<T> concat(List<NutsIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorBuilder.emptyIterator();
        }
        QueueIterator<T> t = new QueueIterator<>();
        for (NutsIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                if (it instanceof QueueIterator) {
                    QueueIterator tt = (QueueIterator) it;
                    for (NutsIterator it1 : tt.getChildren()) {
                        t.add(it1);
                    }
                } else {
                    t.add(it);
                }
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return IteratorBuilder.emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <T> NutsIterator<T> coalesce2(List<NutsIterator<T>> all) {
        return coalesce((List) all);
    }

    public static <T> NutsIterator<T> coalesce(NutsIterator<? extends T>... all) {
        return coalesce(Arrays.asList(all));
    }

    public static <T> NutsIterator<T> concat(NutsIterator<? extends T>... all) {
        return concat(Arrays.asList(all));
    }

    public static <T> NutsIterator<T> concatLists(List<NutsIterator<? extends T>>... all) {
        List<NutsIterator<? extends T>> r = new ArrayList<>();
        if (all != null) {
            for (List<NutsIterator<? extends T>> a : all) {
                if (a != null) {
                    for (NutsIterator<? extends T> b : a) {
                        if (b != null) {
                            r.add(b);
                        }
                    }
                }
            }
        }
        return concat(r);
    }

    public static <T> NutsIterator<T> coalesce(List<NutsIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorBuilder.emptyIterator();
        }
        CoalesceIterator<T> t = new CoalesceIterator<>();
        for (NutsIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                t.add(it);
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return IteratorBuilder.emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

//    public static <T> Iterator<T> filter(Iterator<T> from, Predicate<? super T> filter) {
//        if (from == null) {
//            return IteratorBuilder.emptyIterator();
//        }
//        if (filter == null) {
//            return from;
//        }
//        return new FilteredIterator<>(from, filter);
//    }

//    public static <T> Iterator<T> flatIterator(Iterator<Iterator<T>> from) {
//        return flatMap(from, (c -> c));
//    }

    //? super T, ? extends Iterator<? extends R>
//    public static <T, R> Iterator<R> flatMap(Iterator<T> from, Function<? super T, ? extends Iterator<? extends R>> fun) {
//        if (from == null) {
//            return IteratorBuilder.emptyIterator();
//        }
//        return new FlatMapIterator<>(from, fun);
//    }


//    public static <F, T> Iterator<T> map(Iterator<F> from, Function<? super F, ? extends T> converter) {
//        if (isNullOrEmpty(from)) {
//            return IteratorBuilder.emptyIterator();
//        }
//        return new ConvertedIterator<>(from, converter);
//    }

    public static <F, T> NutsIterator<T> convertNonNull(NutsIterator<F> from, Function<F, T> converter, String name) {
        if (isNullOrEmpty(from)) {
            return IteratorBuilder.emptyIterator();
        }
        return new ConvertedNonNullIterator<>(from, converter, name);
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

    public static <T> Set<T> toSet(NutsIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        LinkedHashSet<T> a = new LinkedHashSet<T>();
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> Set<T> toTreeSet(NutsIterator<T> it, NutsComparator<T> c) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        TreeSet<T> a = new TreeSet<T>(c);
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> NutsIterator<T> sort(NutsIterator<T> it, NutsComparator<T> c, boolean removeDuplicates) {
        if (isNullOrEmpty(it)) {
            return IteratorBuilder.emptyIterator();
        }
        return new SortIterator<>(it, c, removeDuplicates);
    }

    public static <T> NutsIterator<T> distinct(NutsIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return IteratorBuilder.emptyIterator();
        }
        Predicate<T> filter = new DistinctPredicate<>();
        return new FilteredIterator<>(it, filter);
    }

    public static <F, T> NutsIterator<F> distinct(NutsIterator<F> it, final Function<F, T> converter) {
        if (isNullOrEmpty(it)) {
            return IteratorBuilder.emptyIterator();
        }
        if (converter == null) {
            Predicate<F> filter = new DistinctPredicate<>();
            return new FilteredIterator<>(it, filter);
        }
        Predicate<F> filter = new DistinctWithConverterPredicate<>(converter);
        return new FilteredIterator<>(it, filter);
    }

    public static <T> CollectorIterator<T> collector(Iterator<T> it) {
        if (it == null) {
            return new CollectorIterator<>(null, IteratorBuilder.emptyIterator());
        }
        return new CollectorIterator<>(null, it);
    }

    public static <T> NutsIterator<T> nullifyIfEmpty(NutsIterator<T> other) {
        if (other == null) {
            return null;
        }
        if (other instanceof PushBackIterator) {
            PushBackIterator<T> b = (PushBackIterator<T>) other;
            if (!b.isEmpty()) {
                return b;
            } else {
                return null;
            }
        }
        PushBackIterator<T> b = new PushBackIterator<>(other);
        if (!b.isEmpty()) {
            return b;
        } else {
            return null;
        }
    }

}
