/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.NSession;

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

    public static <T> NIterator<T> safe(IteratorErrorHandlerType type, NIterator<T> t, NSession session) {
        return new ErrorHandlerIterator(type, t);
    }

    public static <T> NIterator<T> safeIgnore(NIterator<T> t, NSession session) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.IGNORE, t);
    }

    public static <T> NIterator<T> safePospone(NIterator<T> t, NSession session) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.POSTPONE, t);
    }

    public static <T> boolean isNullOrEmpty(Iterator<T> t) {
        return t == null || t == IteratorBuilder.EMPTY_ITERATOR;
    }

    public static <T> NIterator<T> nonNull(NIterator<T> t) {
        if (t == null) {
            return IteratorBuilder.emptyIterator();
        }
        return t;
    }

    public static <T> NIterator<T> concat(List<NIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorBuilder.emptyIterator();
        }
        QueueIterator<T> t = new QueueIterator<>();
        for (NIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                if (it instanceof QueueIterator) {
                    QueueIterator tt = (QueueIterator) it;
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
            return IteratorBuilder.emptyIterator();
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
            return IteratorBuilder.emptyIterator();
        }
        CoalesceIterator<T> t = new CoalesceIterator<>();
        for (NIterator<? extends T> it : all) {
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

    public static <F, T> NIterator<T> convertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
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
            return IteratorBuilder.emptyIterator();
        }
        return new NIteratorSorted<>(it, c, removeDuplicates);
    }

    public static <T> NIterator<T> distinct(NIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return IteratorBuilder.emptyIterator();
        }
        Predicate<T> filter = new DistinctPredicate<>();
        return new FilteredIterator<>(it, filter);
    }

    public static <F, T> NIterator<F> distinct(NIterator<F> it, final Function<F, T> converter) {
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

    public static <T> NIteratorFromJavaIterator<T> collector(Iterator<T> it) {
        if (it == null) {
            return new NIteratorFromJavaIterator<>(null, IteratorBuilder.emptyIterator());
        }
        return new NIteratorFromJavaIterator<>(null, it);
    }

    public static <T> NIterator<T> nullifyIfEmpty(NIterator<T> other) {
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
