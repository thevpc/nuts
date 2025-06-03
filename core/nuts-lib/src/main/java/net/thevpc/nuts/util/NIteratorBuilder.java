/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;

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
public class NIteratorBuilder<T> {

    public static final NPredicate NON_NULL = NPredicates.isNull().negate();
    public static final NPredicate NON_BLANK = NPredicates.blank().negate();
    static final NIteratorEmpty EMPTY_ITERATOR = new NIteratorEmpty<>();
    private final NIterator<T> it;

    private NIteratorBuilder(NIterator<T> it) {
        if (it == null) {
            it = emptyIterator();
        }
        this.it = it;
    }

    public static <T> NIteratorBuilder<T> ofCoalesce(List<NIterator<? extends T>> t) {
        return new NIteratorBuilder<>(
                NIteratorUtils.coalesce(t)
        );
    }

    public static <T> NIteratorBuilder<T> ofConcat(List<NIterator<? extends T>> t) {
        return new NIteratorBuilder<>(
                NIteratorUtils.concat(t)
        );
    }

    public static <T> NIteratorBuilder<T> of(NIterator<T> t) {
        return new NIteratorBuilder<>(t);
    }

    public static <T> NIteratorBuilder<T> ofRunnable(NRunnable t) {
        return (NIteratorBuilder) of(
                emptyIterator()
        ).onStart(t);
    }

//    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n) {
//        return ofRunnable(NRunnable.of(t, n));
//    }

    public static <T> NIteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return ofRunnable(NRunnable.of(t).withDesc(NEDesc.of(n)));
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> NIteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, NEDesc name) {
        return of(new NSupplierIteratorJ<T>(from,name).withDesc(name));
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, NElement n) {
        return ofArrayValues(t, () -> n);
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return ofArrayValues(t, () -> NElements.ofString(n));
    }

    public static <T> NIteratorBuilder<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return of(t == null ? emptyIterator() :
                new NIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n)
        );
    }

    public static <T> NIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> NIteratorBuilder<T> emptyBuilder() {
        return of(EMPTY_ITERATOR);
    }

    public static <T> NIteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        if (from == null) {
            return emptyBuilder();
        }
        return of(new NFlatMapIterator<>(from, Collection::iterator));
    }

    public NIteratorBuilder<T> filter(Predicate<? super T> t, NEDesc e) {
        if (t == null) {
            return this;
        }
        return of(new NFilteredIterator<>(it, NPredicate.of(t).withDesc(e)));
    }

    public NIteratorBuilder<T> filter(NPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new NIteratorBuilder<>(new NFilteredIterator<>(it, t));
    }

    public NIteratorBuilder<T> concat(NIteratorBuilder<T> t) {
        return concat(t.it);
    }

    public NIteratorBuilder<T> concat(NIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new NIteratorBuilder<>(NIteratorUtils.concat(Arrays.asList(it, t)));
    }

    public <V> NIteratorBuilder<V> map(NFunction<? super T, ? extends V> t) {
        return new NIteratorBuilder<>(new NConvertedIterator<>(it, t));
    }


    public <V> NIteratorBuilder<V> flatMap(Function<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new NFlatMapIterator<T, V>(it, fun));
    }

    public <V> NIteratorBuilder<V> mapMulti(NFunction<T, List<V>> mapper) {
        return new NIteratorBuilder<>(
                new NFlatMapIterator<>(it, t -> mapper.apply(t).iterator())
        );
    }

    public <V> NIteratorBuilder<T> sort(NComparator<T> t, boolean removeDuplicates) {
        return new NIteratorBuilder<>(NIteratorUtils.sort(it, t, removeDuplicates));
    }

    public <V> NIteratorBuilder<T> distinct() {
        return distinct(null);
    }

    public <V> NIteratorBuilder<T> distinct(NFunction<T, V> t) {
        if (t == null) {
            return new NIteratorBuilder<>(NIteratorUtils.distinct(it));
        } else {
            return new NIteratorBuilder<>(NIteratorUtils.distinct(it, t));
        }
    }

    public <V> NIteratorBuilder<T> named(NElement n) {
        if (n != null) {
            NIteratorAdapter<T> a = new NIteratorAdapter<>(it, () -> n);
//            a.describe();
            return new NIteratorBuilder<>(a);
        }
        return this;
    }

    public <V> NIteratorBuilder<T> named(NObjectElement nfo) {
        if (nfo != null) {
            return new NIteratorBuilder<>(new NIteratorAdapter<T>(it, () -> nfo));
        }
        return this;
    }


    public NIteratorBuilder<T> safe(NIteratorErrorHandlerType type) {
        return new NIteratorBuilder<>(new NErrorHandlerIterator(type, it));
    }

    public NIteratorBuilder<T> safeIgnore() {
        return safe(NIteratorErrorHandlerType.IGNORE);
    }

    public NIteratorBuilder<T> safePostpone() {
        return safe(NIteratorErrorHandlerType.POSTPONE);
    }

    public NIteratorBuilder<T> notNull() {
        return filter(NON_NULL);
    }

    public NIteratorBuilder<String> notBlank() {
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
        return NIteratorUtils.toList(it);
    }

    public NIteratorBuilder<T> onFinish(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new NIteratorOnFinish<>(it, r));
    }


    public NIteratorBuilder<T> onStart(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new NOnStartIterator<>(it, r));
    }
}
