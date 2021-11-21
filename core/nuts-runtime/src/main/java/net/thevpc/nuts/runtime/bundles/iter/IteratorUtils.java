/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.iter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.thevpc.nuts.NutsPredicates;

/**
 * @author thevpc
 */
public class IteratorUtils {

    public static final Predicate NON_NULL = NutsPredicates.isNull().negate();
    public static final Predicate NON_BLANK = NutsPredicates.blank().negate();
    private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator<>();

//    public static FileDepthFirstIterator dsf(File file) {
//        return new FileDepthFirstIterator(file);
//    }

    public static <T> Iterator<T> safe(IteratorErrorHandlerType type, Iterator<T> t) {
        return new ErrorHandlerIterator(type, t);
    }

    public static <T> Iterator<T> safeIgnore(Iterator<T> t) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.IGNORE, t);
    }

    public static <T> Iterator<T> safePospone(Iterator<T> t) {
        return new ErrorHandlerIterator(IteratorErrorHandlerType.POSTPONE, t);
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

    public static <T> Iterator<T> concat(List<Iterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorUtils.emptyIterator();
        }
        QueueIterator<T> t = new QueueIterator<>();
        for (Iterator<? extends T> it : all) {
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

    public static <T> Iterator<T> coalesce2(List<Iterator<T>> all) {
        return coalesce((List) all);
    }

    public static <T> Iterator<T> coalesce(Iterator<? extends T> ... all) {
        return coalesce(Arrays.asList(all));
    }

    public static <T> Iterator<T> concat(Iterator<? extends T> ... all) {
        return concat(Arrays.asList(all));
    }

    public static <T> Iterator<T> concatLists(List<Iterator<? extends T>> ... all) {
        List<Iterator<? extends T>> r=new ArrayList<>();
        if(all!=null) {
            for (List<Iterator<? extends T>> a : all) {
                if (a != null) {
                    for (Iterator<? extends T> b : a) {
                        if(b!=null){
                            r.add(b);
                        }
                    }
                }
            }
        }
        return concat(r);
    }

    public static <T> Iterator<T> coalesce(List<Iterator<? extends T>> all) {
        if (all == null || all.isEmpty()) {
            return IteratorUtils.emptyIterator();
        }
        CoalesceIterator<T> t = new CoalesceIterator<>();
        for (Iterator<? extends T> it : all) {
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

    public static <T> Iterator<T> filter(Iterator<T> from, Predicate<? super T> filter) {
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

    public static <T> Iterator<T> flatCollection(Iterator<Collection<T>> from) {
        return flatMap(from, (c->c.iterator()));
    }

    public static <T> Iterator<T> flatIterator(Iterator<Iterator<T>> from) {
        return flatMap(from, (c->c));
    }

    //? super T, ? extends Iterator<? extends R>
    public static <T, R> Iterator<R> flatMap(Iterator<T> from, Function<? super T, ? extends Iterator<? extends R>> fun) {
        if (from == null) {
            return emptyIterator();
        }
        return new FlatMapIterator<>(from, fun);
    }

    public static <T> Iterator<T> supplier(Supplier<Iterator<T>> from) {
        return new SupplierIterator<T>(from, null);
    }

    public static <T> Iterator<T> supplier(Supplier<Iterator<T>> from, String name) {
        return new SupplierIterator<T>(from, name);
    }

    public static <T> Iterator<T> onFinish(Iterator<T> from, Runnable r) {
        if (from == null) {
            return emptyIterator();
        }
        return new OnFinishIterator<>(from, r);
    }

    public static <T> Iterator<T> onStartFinish(Iterator<T> from, Runnable s, Runnable f) {
        if(s==null && f==null){
            return emptyIterator();
        }
        if(from==null){
            from=emptyIterator();
        }
        if(f!=null){
            from=onFinish(from,f);
        }
        if(s!=null){
            from=onStart(from,s);
        }
        return from;
    }

    public static <T> Iterator<T> onStart(Iterator<T> from, Runnable r) {
        if (from == null) {
            return emptyIterator();
        }
        return new OnStartIterator<>(from, r);
    }

    public static <F, T> Function<F,T> namedFunction(Function<F,T> converter, String name) {
        return new NamedFunction<>(converter, name);
    }

    public static <F, T> Iterator<T> map(Iterator<F> from, Function<? super F, ? extends T> converter) {
        if (isNullOrEmpty(from)) {
            return emptyIterator();
        }
        return new ConvertedIterator<>(from, converter);
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
        Predicate<T> filter = new DistinctPredicate<>();
        return new FilteredIterator<>(it, filter);
    }

    public static <F, T> Iterator<F> distinct(Iterator<F> it, final Function<F, T> converter) {
        if (isNullOrEmpty(it)) {
            return emptyIterator();
        }
        if(converter==null){
            Predicate<F> filter = new DistinctPredicate<>();
            return new FilteredIterator<>(it, filter);
        }
        Predicate<F> filter = new DistinctWithConverterPredicate<>(converter);
        return new FilteredIterator<>(it, filter);
    }

    public static <T> CollectorIterator<T> collector(Iterator<T> it) {
        if (it == null) {
            return new CollectorIterator<>(null, emptyIterator());
        }
        return new CollectorIterator<>(null, it);
    }

    public static <T> Iterator<T> nullifyIfEmpty(Iterator<T> other) {
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
