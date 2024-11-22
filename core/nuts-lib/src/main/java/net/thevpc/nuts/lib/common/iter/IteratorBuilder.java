/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.common.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.lib.common.collections.NCollections;
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
public class IteratorBuilder<T> {

    public static final NPredicate NON_NULL = NPredicates.isNull().negate();
    public static final NPredicate NON_BLANK = NPredicates.blank().negate();
    static final NIteratorEmpty EMPTY_ITERATOR = new NIteratorEmpty<>();
    private final NIterator<T> it;

    private IteratorBuilder(NIterator<T> it) {
        if (it == null) {
            it = emptyIterator();
        }
        this.it = it;
    }

    public static <T> IteratorBuilder<T> ofCoalesce(List<NIterator<? extends T>> t) {
        return new IteratorBuilder<>(
                IteratorUtils.coalesce(t)
        );
    }

    public static <T> IteratorBuilder<T> ofConcat(List<NIterator<? extends T>> t) {
        return new IteratorBuilder<>(
                IteratorUtils.concat(t)
        );
    }

    public static <T> IteratorBuilder<T> of(NIterator<T> t) {
        return new IteratorBuilder<>(t);
    }

    public static <T> IteratorBuilder<T> ofRunnable(NRunnable t) {
        return (IteratorBuilder) of(
                emptyIterator()
        ).onStart(t);
    }

//    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n) {
//        return ofRunnable(NRunnable.of(t, n));
//    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return ofRunnable(NRunnable.of(t).withDesc(NEDesc.of(n)));
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> IteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, NEDesc name) {
        return of(new SupplierIterator2<T>(from,name).withDesc(name));
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, NElement n) {
        return ofArrayValues(t, () -> n);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return ofArrayValues(t, () -> NElements.of().ofString(n));
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return of(t == null ? emptyIterator() :
                new NIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n)
        );
    }

    public static <T> NIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> IteratorBuilder<T> emptyBuilder() {
        return of(EMPTY_ITERATOR);
    }

    public static <T> IteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        if (from == null) {
            return emptyBuilder();
        }
        return of(new FlatMapIterator<>(from, Collection::iterator));
    }

    public IteratorBuilder<T> filter(Predicate<? super T> t, NEDesc e) {
        if (t == null) {
            return this;
        }
        return of(new FilteredIterator<>(it, NPredicate.of(t).withDesc(e)));
    }

    public IteratorBuilder<T> filter(NPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(new FilteredIterator<>(it, t));
    }

    public IteratorBuilder<T> concat(IteratorBuilder<T> t) {
        return concat(t.it);
    }

    public IteratorBuilder<T> concat(NIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(IteratorUtils.concat(Arrays.asList(it, t)));
    }

    public <V> IteratorBuilder<V> map(NFunction<? super T, ? extends V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t));
    }


    public <V> IteratorBuilder<V> flatMap(Function<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new FlatMapIterator<T, V>(it, fun));
    }

    public <V> IteratorBuilder<V> mapMulti(NFunction<T, List<V>> mapper) {
        return new IteratorBuilder<>(
                new FlatMapIterator<>(it, t -> mapper.apply(t).iterator())
        );
    }

    public <V> IteratorBuilder<T> sort(NComparator<T> t, boolean removeDuplicates) {
        return new IteratorBuilder<>(IteratorUtils.sort(it, t, removeDuplicates));
    }

    public <V> IteratorBuilder<T> distinct() {
        return distinct(null);
    }

    public <V> IteratorBuilder<T> distinct(NFunction<T, V> t) {
        if (t == null) {
            return new IteratorBuilder<>(IteratorUtils.distinct(it));
        } else {
            return new IteratorBuilder<>(IteratorUtils.distinct(it, t));
        }
    }

    public <V> IteratorBuilder<T> named(String n) {
        if (n != null) {
            return new IteratorBuilder<>(new NIteratorAdapter<T>(
                    it, () -> NElements.of().ofString(n)));
        }
        return this;
    }

    public <V> IteratorBuilder<T> named(NObjectElement nfo) {
        if (nfo != null) {
            return new IteratorBuilder<>(new NIteratorAdapter<T>(it, () -> nfo));
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
        return IteratorUtils.toList(it);
    }

    public IteratorBuilder<T> onFinish(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new NIteratorOnFinish<>(it, r));
    }


    public IteratorBuilder<T> onStart(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new OnStartIterator<>(it, r));
    }
}
