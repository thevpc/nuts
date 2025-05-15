/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class NIteratorUtils {

    //    public static FileDepthFirstIterator dsf(File file) {
//        return new FileDepthFirstIterator(file);
//    }

    public static <T> NIterator<T> safe(NIteratorErrorHandlerType type, NIterator<T> t) {
        return new NErrorHandlerIterator(type, t);
    }

    public static <T> NIterator<T> safeIgnore(NIterator<T> t) {
        return new NErrorHandlerIterator(NIteratorErrorHandlerType.IGNORE, t);
    }

    public static <T> NIterator<T> safePospone(NIterator<T> t) {
        return new NErrorHandlerIterator(NIteratorErrorHandlerType.POSTPONE, t);
    }

    public static <T> boolean isNullOrEmpty(Iterator<T> t) {
        if (t == null) {
            return true;
        }
        if (t == NIteratorBuilder.EMPTY_ITERATOR) {
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
        if (t instanceof NIteratorEmpty) {
            return true;
        }
        return false;
    }

    public static <T> NIterator<T> nonNull(NIterator<T> t) {
        if (t == null) {
            return NIteratorBuilder.emptyIterator();
        }
        return t;
    }

    public static <T> NIterator<T> concat(List<NIterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return NIteratorBuilder.emptyIterator();
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
            return NIteratorBuilder.emptyIterator();
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
            return NIteratorBuilder.emptyIterator();
        }
        NCoalesceIterator<T> t = new NCoalesceIterator<>();
        for (NIterator<? extends T> it : all) {
            if (!isNullOrEmpty(it)) {
                t.add(it);
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return NIteratorBuilder.emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <F, T> NIterator<T> convertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
        if (isNullOrEmpty(from)) {
            return NIteratorBuilder.emptyIterator();
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
            return NIteratorBuilder.emptyIterator();
        }
        return new NIteratorSorted<>(it, c, removeDuplicates);
    }

    public static <T> NIterator<T> distinct(NIterator<T> it) {
        if (isNullOrEmpty(it)) {
            return NIteratorBuilder.emptyIterator();
        }
        Predicate<T> filter = new NDistinctPredicate<>();
        return new NFilteredIterator<>(it, filter);
    }

    public static <F, T> NIterator<F> distinct(NIterator<F> it, final Function<F, T> converter) {
        if (isNullOrEmpty(it)) {
            return NIteratorBuilder.emptyIterator();
        }
        if (converter == null) {
            Predicate<F> filter = new NDistinctPredicate<>();
            return new NFilteredIterator<>(it, filter);
        }
        Predicate<F> filter = new NDistinctWithConverterPredicate<>(converter);
        return new NFilteredIterator<>(it, filter);
    }

    public static <T> NIteratorFromJavaIterator<T> collector(Iterator<T> it) {
        if (it == null) {
            return new NIteratorFromJavaIterator<>(null, NIteratorBuilder.emptyIterator());
        }
        return new NIteratorFromJavaIterator<>(null, it);
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

}
