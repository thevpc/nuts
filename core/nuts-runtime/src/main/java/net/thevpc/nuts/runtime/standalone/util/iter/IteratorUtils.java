/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.thevpc.nuts.runtime.standalone.util.common.FileDepthFirstIterator;

/**
 * @author thevpc
 */
public class IteratorUtils {

    public static final NonNullFilter NON_NULL = new NonNullFilter();
    public static final NonBlankFilter NON_BLANK = new NonBlankFilter();
    private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator<>();

    public static FileDepthFirstIterator dsf(File file) {
        return new FileDepthFirstIterator(file);
    }

    public static <T> Iterator<T> safe(IteratorErrorHandlerType type, Iterator<T> t) {
        return new ErrorHandlerIterator(type, t);
    }

    public static <T> Iterator<T> safeIgnore(Iterator<T> t) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.IGNORE, t);
    }

    public static <T> Iterator<T> safePospone(Iterator<T> t) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.POSPONE, t);
    }

    public static <T> boolean isNullOrEmpty(Iterator<T> t) {
        return t == null || t == EMPTY_ITERATOR;
    }

    public static <T> Iterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> Iterator<T> nonNull(Iterator<T> t) {
        if (t == null) {
            return emptyIterator();
        }
        return t;
    }

    public static <T> Iterator<T> concat(List<Iterator<T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorUtils.emptyIterator();
        }
        QueueIterator<T> t = new QueueIterator<>();
        for (Iterator<T> it : all) {
            if (!isNullOrEmpty(it)) {
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
        int tsize = t.size();
        if (tsize == 0) {
            return IteratorUtils.emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <T> Iterator<T> coalesce(List<Iterator<T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorUtils.emptyIterator();
        }
        CoalesceIterator<T> t = new CoalesceIterator<>();
        for (Iterator<T> it : all) {
            if (!isNullOrEmpty(it)) {
                t.add(it);
            }
        }
        int tsize = t.size();
        if (tsize == 0) {
            return IteratorUtils.emptyIterator();
        }
        if (tsize == 1) {
            return t.getChildren()[0];
        }
        return t;
    }

    public static <T> Iterator<T> filter(Iterator<T> from, Predicate<T> filter) {
        if (from == null) {
            return emptyIterator();
        }
        if (filter == null) {
            return from;
        }
        return new FilteredIterator<>(from, filter);
    }

    public static <T> Iterator<T> name(String name, Iterator<T> from) {
        return new NamedIterator<>(from, name);
    }

    public static <T> Iterator<T> flatten(Iterator<Collection<T>> from) {
        if (from == null) {
            return emptyIterator();
        }
        return new FlattenCollectionIterator<>(from);
    }

    public static <T> Iterator<T> supplier(Supplier<Iterator<T>> from) {
        return new SupplierIterator<T>(from,null);
    }

    public static <T> Iterator<T> supplier(Supplier<Iterator<T>> from,String name) {
        return new SupplierIterator<T>(from,name);
    }

    public static <T> Iterator<T> onFinish(Iterator<T> from, Runnable r) {
        if (from == null) {
            return emptyIterator();
        }
        return new OnFinishIterator<>(from, r);
    }

    public static <F, T> Iterator<T> convert(Iterator<F> from, Function<F, T> converter, String name) {
        if (isNullOrEmpty(from)) {
            return emptyIterator();
        }
        return new ConvertedIterator<>(from, converter, name);
    }

    public static <F, T> Iterator<T> convertNonNull(Iterator<F> from, Function<F, T> converter, String name) {
        if (isNullOrEmpty(from)) {
            return emptyIterator();
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

    public static <T> Set<T> toSet(Iterator<T> it) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        LinkedHashSet<T> a = new LinkedHashSet<T>();
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> Set<T> toTreeSet(Iterator<T> it, Comparator<T> c) {
        if (isNullOrEmpty(it)) {
            return Collections.emptySet();
        }
        TreeSet<T> a = new TreeSet<T>(c);
        while (it.hasNext()) {
            a.add(it.next());
        }
        return a;
    }

    public static <T> Iterator<T> sort(Iterator<T> it, Comparator<T> c, boolean removeDuplicates) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
        return new SortIterator<>(it, c, removeDuplicates);
    }

    public static <T> Iterator<T> distinct(Iterator<T> it) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
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

            @Override
            public String toString() {
                return "DistinctFilter";
            }
        };
        return new FilteredIterator<>(it, filter);
    }

    public static <F, T> Iterator<F> distinct(Iterator<F> it, final Function<F, T> converter) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
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

            @Override
            public String toString() {
                return "DistinctConverter";
            }
        };
        return new FilteredIterator<>(it, filter);
    }

    private static class EmptyIterator<E> implements Iterator<E> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public String toString() {
            return "EmptyIterator";
        }
    }

    public static class NonNullFilter<T> implements Predicate<T> {

        public NonNullFilter() {
        }

        @Override
        public boolean test(T value) {
            return value != null;
        }

        @Override
        public String toString() {
            return "NonNullFilter";
        }
    }

    public static class NonBlankFilter implements Predicate<String> {

        public NonBlankFilter() {
        }

        @Override
        public boolean test(String value) {
            return value != null && value.trim().length() > 0;
        }

        @Override
        public String toString() {
            return "NonBlankFilter";
        }
    }

    private static class OnFinishIterator<T> implements Iterator<T> {
        private final Iterator<T> from;
        private final Runnable r;

        public OnFinishIterator(Iterator<T> from, Runnable r) {
            this.from = from;
            this.r = r;
        }

        @Override
        public boolean hasNext() {
            boolean n = from.hasNext();
            if (!n) {
                r.run();
            }
            return n;
        }

        @Override
        public T next() {
            return from.next();
        }
    }

    private static class SupplierIterator<T> implements Iterator<T> {
        private final Supplier<Iterator<T>> from;
        private Iterator<T> it;
        private String name;

        public SupplierIterator(Supplier<Iterator<T>> from,String name) {
            this.from = from;
            this.name = name;
        }

        @Override
        public boolean hasNext() {
            if (it == null) {
                it = from.get();
            }
            return it.hasNext();
        }

        @Override
        public T next() {
            return it.next();
        }

        @Override
        public String toString() {
            if(name==null){
                return "supplier("+from+")";
            }
            return String.valueOf(name);
        }
    }

    private static class NamedIterator<T> implements Iterator<T> {
        private final Iterator<T> from;
        private final String name;

        public NamedIterator(Iterator<T> from, String name) {
            this.from = from;
            this.name = name;
        }

        @Override
        public boolean hasNext() {
            return from != null && from.hasNext();
        }

        @Override
        public T next() {
            return from.next();
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    private static class FlattenCollectionIterator<T> implements Iterator<T> {
        private final Iterator<Collection<T>> from;
        Iterator<T> n;

        public FlattenCollectionIterator(Iterator<Collection<T>> from) {
            this.from = from;
            n = null;
        }

        @Override
        public boolean hasNext() {
            while (true) {
                if (n == null) {
                    if (from.hasNext()) {
                        Collection<T> p = from.next();
                        if (p == null) {
                            n = Collections.emptyIterator();
                        } else {
                            n = p.iterator();
                        }
                    } else {
                        return false;
                    }
                }
                if (n.hasNext()) {
                    return true;
                }else{
                    n=null;
                }
            }
        }

        @Override
        public T next() {
            return n.next();
        }

        @Override
        public String toString() {
            return "flattenCollection(" + from + ")";
        }
    }

    private static class SortIterator<T> implements Iterator<T> {
        private final boolean removeDuplicates;
        private final Iterator<T> it;
        private final Comparator<T> c;
        Iterator<T> base;

        public SortIterator(Iterator<T> it, Comparator<T> c, boolean removeDuplicates) {
            this.removeDuplicates = removeDuplicates;
            this.it = it;
            this.c = c;
            base = null;
        }

        public Iterator<T> getBase() {
            if (base == null) {
                if (removeDuplicates) {
                    base = toTreeSet(it, c).iterator();
                } else {
                    List<T> a = toList(it);
                    a.sort(c);
                    base = a.iterator();
                }
            }
            return base;
        }

        @Override
        public boolean hasNext() {
            return getBase().hasNext();
        }

        @Override
        public T next() {
            return getBase().next();
        }

        @Override
        public String toString() {
            return "sort("+ it +")";
        }
    }
}
