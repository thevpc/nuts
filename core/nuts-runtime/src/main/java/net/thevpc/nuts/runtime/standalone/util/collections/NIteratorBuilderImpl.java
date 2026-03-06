/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.util.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class NIteratorBuilderImpl<T> implements NIteratorBuilder<T> {

    public static final NPredicate NON_NULL = NPredicates.isNull().negate();
    public static final NPredicate NON_BLANK = NPredicates.blank().negate();
    static final NIteratorEmpty EMPTY_ITERATOR = new NIteratorEmpty<>();
    private final NIterator<T> it;

    NIteratorBuilderImpl(Iterator<T> it) {
        if (it == null) {
            it = emptyIterator();
        }
        if(it instanceof NIterator){
            this.it = (NIterator<T>) it;
        }else{
            this.it=NIterator.of(it);
        }
    }

    public static <T> NIteratorBuilderImpl<T> ofCoalesce(List<NIterator<? extends T>> t) {
        return new NIteratorBuilderImpl<>(
                NIterator.ofCoalesce(t)
        );
    }

    public static <T> NIteratorBuilderImpl<T> ofConcat(List<NIterator<? extends T>> t) {
        return new NIteratorBuilderImpl<>(
                NIterator.ofConcat(t)
        );
    }

    public static <T> NIteratorBuilderImpl<T> of(NIterator<T> t) {
        return new NIteratorBuilderImpl<>(t);
    }

    public static <T> NIteratorBuilderImpl<T> ofRunnable(NRunnable t) {
        return (NIteratorBuilderImpl) of(
                emptyIterator()
        ).onStart(t);
    }

//    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n) {
//        return ofRunnable(NRunnable.of(t, n));
//    }

    public static <T> NIteratorBuilderImpl<T> ofRunnable(Runnable t, String n) {
        return ofRunnable(NRunnable.of(t).withDescription(NDescribables.ofDesc(n)));
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> NIteratorBuilderImpl<T> ofSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name) {
        return of(new NSupplierIteratorJ<T>(from,name).withDescription(name));
    }

    public static <T> NIteratorBuilderImpl<T> ofArrayValues(T[] t, NElement n) {
        return ofArrayValues(t, () -> n);
    }

    public static <T> NIteratorBuilderImpl<T> ofArrayValues(T[] t, String n) {
        return ofArrayValues(t, () -> NElement.ofString(n));
    }

    public static <T> NIteratorBuilderImpl<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return of(t == null ? emptyIterator() :
                new NIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n)
        );
    }

    public static <T> NIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> NIteratorBuilderImpl<T> emptyBuilder() {
        return of(EMPTY_ITERATOR);
    }

    public static <T> NIteratorBuilderImpl<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        if (from == null) {
            return emptyBuilder();
        }
        return of(new NFlatMapIterator<>(from, Collection::iterator));
    }

    public NIteratorBuilderImpl<T> filter(Predicate<? super T> t, Supplier<NElement> e) {
        if (t == null) {
            return this;
        }
        return of(new NFilteredIterator<>(it, NPredicate.of(t).withDescription(e)));
    }

    public NIteratorBuilderImpl<T> filter(NPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new NIteratorBuilderImpl<>(new NFilteredIterator<>(it, t));
    }

    public NIteratorBuilderImpl<T> concat(NIteratorBuilder<T> t) {
        return concat(((NIteratorBuilderImpl)t).it);
    }

    public NIteratorBuilderImpl<T> concat(NIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new NIteratorBuilderImpl<>(NIterator.ofConcat(Arrays.asList(it, t)));
    }

    public <V> NIteratorBuilderImpl<V> map(NFunction<? super T, ? extends V> t) {
        return new NIteratorBuilderImpl<>(new NConvertedIterator<>(it, t));
    }


    public <V> NIteratorBuilderImpl<V> flatMap(Function<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new NFlatMapIterator<T, V>(it, fun));
    }

    public <V> NIteratorBuilderImpl<V> mapMulti(NFunction<T, List<V>> mapper) {
        return new NIteratorBuilderImpl<>(
                new NFlatMapIterator<>(it, t -> mapper.apply(t).iterator())
        );
    }

    public <V> NIteratorBuilderImpl<T> sort(NComparator<T> t, boolean removeDuplicates) {
        return new NIteratorBuilderImpl<>(NIterator.ofSorted(it, t, removeDuplicates));
    }

    public <V> NIteratorBuilderImpl<T> distinct() {
        return distinct(null);
    }

    public <V> NIteratorBuilderImpl<T> distinct(NFunction<T, V> t) {
        if (t == null) {
            return new NIteratorBuilderImpl<>(NIterator.ofDistinct(it));
        } else {
            return new NIteratorBuilderImpl<>(NIterator.ofDistinct(it, t));
        }
    }

    public <V> NIteratorBuilderImpl<T> named(NElement n) {
        if (n != null) {
            NIteratorAdapter<T> a = new NIteratorAdapter<>(it, () -> n);
//            a.describe();
            return new NIteratorBuilderImpl<>(a);
        }
        return this;
    }

    public <V> NIteratorBuilderImpl<T> named(NObjectElement nfo) {
        if (nfo != null) {
            return new NIteratorBuilderImpl<>(new NIteratorAdapter<T>(it, () -> nfo));
        }
        return this;
    }


    public NIteratorBuilderImpl<T> safe(NIteratorErrorHandlerType type) {
        return new NIteratorBuilderImpl<>(new NErrorHandlerIterator(type, it));
    }

    public NIteratorBuilderImpl<T> safeIgnore() {
        return safe(NIteratorErrorHandlerType.IGNORE);
    }

    public NIteratorBuilderImpl<T> safePostpone() {
        return safe(NIteratorErrorHandlerType.POSTPONE);
    }

    public NIteratorBuilderImpl<T> notNull() {
        return filter(NON_NULL);
    }

    public NIteratorBuilderImpl<String> notBlank() {
        return this.filter(NON_BLANK);
    }

    public NIterator<T> iterator() {
        return it;
    }

    public List<T> list() {
        return NCollections.list(it);
    }

    public NIterator<T> build() {
        return it;
    }

    public List<T> toList() {
        return NIterator.toList(it);
    }

    public NIteratorBuilderImpl<T> onFinish(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new NIteratorOnFinish<>(it, r));
    }


    public NIteratorBuilderImpl<T> onStart(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new NOnStartIterator<>(it, r));
    }
}
