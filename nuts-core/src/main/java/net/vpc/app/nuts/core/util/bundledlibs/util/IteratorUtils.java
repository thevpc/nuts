/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.bundledlibs.util;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author vpc
 */
public class IteratorUtils {

    public static final NonNullFilter NON_NULL = new NonNullFilter();

    public static class NonNullFilter<T> implements Predicate<T> {

        public NonNullFilter() {
        }

        @Override
        public boolean test(T value) {
            return value != null;
        }
    }

    public static FileDepthFirstIterator dsf(File file) {
        return new FileDepthFirstIterator(file);
    }

    public static <T> Iterator<T> safe(ErrorHandlerIteratorType type, Iterator<T> t) {
        return new ErrorHandlerIterator(type, t);
    }

    public static <T> Iterator<T> safeIgnore(Iterator<T> t) {
        return new ErrorHandlerIterator(ErrorHandlerIteratorType.IGNORE, t);
    }

    public static <T> Iterator<T> safePospone(Iterator<T> t) {
        return new ErrorHandlerIterator(ErrorHandlerIteratorType.POSPONE, t);
    }

    public static <T> Iterator<T> nonNull(Iterator<T> t) {
        return filter(t, NON_NULL);
    }

    public static <T> Iterator<T> concat(List<Iterator<T>> all) {
        if (all == null || all.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        QueueIterator<T> t = new QueueIterator<>();
        for (Iterator<T> it : all) {
            if (it != null) {
                if (it instanceof QueueIterator) {
                    QueueIterator tt = (QueueIterator) it;
                    for (Iterator it1 : tt.getChildren()) {
                        t.add(it1);
                    }
                } else {
                    t.add(it);
                }
            }
        }
        return t;
    }

    public static <T> Iterator<T> filter(Iterator<T> from, Predicate<T> filter) {
        if (filter == null) {
            return from;
        }
        return new FilteredIterator<>(from, filter);
    }

    public static <F, T> Iterator<T> convert(Iterator<F> from, Function<F, T> converter) {
        return new ConvertedIterator<>(from, converter);
    }

    public static <T> Iterator<T> coalesce(List<Iterator<T>> all) {
        if (all == null || all.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        CoalesceIterator<T> t = new CoalesceIterator<>();
        for (Iterator<T> it : all) {
            if (it != null) {
                t.add(it);
            }
        }
        return t;
    }

    public static <T> Iterator<T> unique(Iterator<T> it) {
        Predicate<T> filter = new Predicate<T>() {
            HashSet<T> visited = new HashSet<>();

            @Override
            public boolean test(T value) {
                if (visited.contains(value)) {
                    return false;
                }
                visited.add(value);
                return true;
            }
        };
        return new FilteredIterator<>(it, filter);
    }

    public static <F, T> Iterator<F> unique(Iterator<F> it, final Function<F, T> converter) {
        Predicate<F> filter = new Predicate<F>() {
            HashSet<T> visited = new HashSet<>();

            @Override
            public boolean test(F value) {
                T t = converter.apply(value);
                if (visited.contains(t)) {
                    return false;
                }
                visited.add(t);
                return true;
            }
        };
        return new FilteredIterator<>(it, filter);
    }
}
