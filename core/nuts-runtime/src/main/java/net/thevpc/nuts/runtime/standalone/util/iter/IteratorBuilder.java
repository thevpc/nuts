/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.nfo.NutsIteratorAdapter;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

/**
 * @author thevpc
 */
public class IteratorBuilder<T> {

    public static final NutsPredicate NON_NULL = NutsPredicates.isNull().negate();
    public static final NutsPredicate NON_BLANK = NutsPredicates.blank().negate();
    static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator<>();
    private final NutsIterator<T> it;

    private IteratorBuilder(NutsIterator<T> it) {
        if (it == null) {
            it = emptyIterator();
        }
        this.it = it;
    }

    public static <T> IteratorBuilder<T> ofCoalesce(List<NutsIterator<? extends T>> t) {
        return new IteratorBuilder<>(
                IteratorUtils.coalesce(t)
        );
    }

    public static <T> IteratorBuilder<T> ofConcat(List<NutsIterator<? extends T>> t) {
        return new IteratorBuilder<>(
                IteratorUtils.concat(t)
        );
    }

    public static <T> IteratorBuilder<T> of(NutsIterator<T> t) {
        return new IteratorBuilder<>(t);
    }

    public static <T> IteratorBuilder<T> ofRunnable(NutsRunnable t) {
        return (IteratorBuilder) of(
                emptyIterator()
        ).onStart(t);
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NutsElement n) {
        return ofRunnable(NutsRunnable.of(t, n));
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return ofRunnable(NutsRunnable.of(t, n));
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> IteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from , Function<NutsElements,NutsElement> name) {
        return of(new SupplierIterator2<T>(from, name));
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, NutsElement n) {
        return ofArrayValues(t,e->n);
    }
    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return ofArrayValues(t,e->e.ofString(n));
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, Function<NutsElements,NutsElement> n) {
        return of(t == null ? emptyIterator() :
                        new NutsIteratorAdapter<T>(
                                Arrays.asList(t).iterator(), n)
                );
    }

    public static <T> NutsIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> IteratorBuilder<T> emptyBuilder() {
        return of(EMPTY_ITERATOR);
    }

    public static <T> IteratorBuilder<T> ofFlatMap(NutsIterator<? extends Collection<T>> from) {
        if (from == null) {
            return emptyBuilder();
        }
        return of(new FlatMapIterator<>(from, Collection::iterator));
    }

    public IteratorBuilder<T> filter(Predicate<? super T> t,Function<NutsElements,NutsElement> e) {
        if (t == null) {
            return this;
        }
        return of(new FilteredIterator<>(it, NutsPredicate.of(t,e)));
    }

    public IteratorBuilder<T> filter(NutsPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(new FilteredIterator<>(it, t));
    }

    public IteratorBuilder<T> concat(IteratorBuilder<T> t) {
        return concat(t.it);
    }

    public IteratorBuilder<T> concat(NutsIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(IteratorUtils.concat(Arrays.asList(it, t)));
    }

    public <V> IteratorBuilder<V> map(NutsFunction<? super T, ? extends V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t));
    }


    public <V> IteratorBuilder<V> flatMap(NutsFunction<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new FlatMapIterator<T, V>(it, fun));
    }

    public <V> IteratorBuilder<V> mapMulti(NutsFunction<T, List<V>> mapper) {
        return new IteratorBuilder<>(
                new FlatMapIterator<>(it, t -> mapper.apply(t).iterator())
        );
    }

    public <V> IteratorBuilder<T> sort(NutsComparator<T> t, boolean removeDuplicates) {
        return new IteratorBuilder<>(IteratorUtils.sort(it, t, removeDuplicates));
    }

    public <V> IteratorBuilder<T> distinct() {
        return distinct(null);
    }

    public <V> IteratorBuilder<T> distinct(NutsFunction<T, V> t) {
        if (t == null) {
            return new IteratorBuilder<>(IteratorUtils.distinct(it));
        } else {
            return new IteratorBuilder<>(IteratorUtils.distinct(it, t));
        }
    }

    public <V> IteratorBuilder<T> named(String n) {
        if (n != null) {
            return new IteratorBuilder<>(new NutsIteratorAdapter<T>(
                    it, e -> e.ofString(n)));
        }
        return this;
    }

    public <V> IteratorBuilder<T> named(NutsObjectElement nfo) {
        if (nfo != null) {
            return new IteratorBuilder<>(new NutsIteratorAdapter<T>(it, e -> nfo));
        }
        return this;
    }


    public IteratorBuilder<T> safe(IteratorErrorHandlerType type) {
        return new IteratorBuilder<>(new ErrorHandlerIterator(type, it));
    }

    public IteratorBuilder<T> safeIgnore() {
        return safe(IteratorErrorHandlerType.IGNORE);
    }

    public IteratorBuilder<T> safePostpone() {
        return safe(IteratorErrorHandlerType.POSTPONE);
    }

    public IteratorBuilder<T> notNull() {
        return filter(NON_NULL);
    }

    public IteratorBuilder<String> notBlank() {
        return this.filter(NON_BLANK);
    }

    public NutsIterator<T> iterator() {
        return it;
    }

    public List<T> list() {
        return CoreCollectionUtils.toList(it);
    }

    public NutsIterator<T> build() {
        return it;
    }

    public List<T> toList() {
        return IteratorUtils.toList(it);
    }

    public IteratorBuilder<T> onFinish(NutsRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new OnFinishIterator<>(it, r));
    }


    public IteratorBuilder<T> onStart(NutsRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new OnStartIterator<>(it, r));
    }
}
