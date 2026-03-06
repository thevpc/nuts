/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public interface NIteratorBuilder<T> {

    static <T> NIteratorBuilder<T> ofCoalesce(List<NIterator<? extends T>> t) {
        return NCollectionsRPI.of().iteratorBuilderOfCoalesce(t);
    }

    static <T> NIteratorBuilder<T> ofConcat(List<NIterator<? extends T>> t) {
        return NCollectionsRPI.of().iteratorBuilderOfConcat(t);
    }

    static <T> NIteratorBuilder<T> of(Iterator<T> t) {
        return NCollectionsRPI.of().iteratorBuilder(t);
    }

    static <T> NIteratorBuilder<T> ofRunnable(NRunnable t) {
        return NCollectionsRPI.of().iteratorBuilderOfRunnable(t);
    }

    static <T> NIteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return NCollectionsRPI.of().iteratorBuilderOfRunnable(t, n);
    }

    static <T> NIteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name) {
        return NCollectionsRPI.of().iteratorBuilderOfSupplier(from, name);
    }

    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, NElement n) {
        return NCollectionsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return NCollectionsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return NCollectionsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    static <T> NIteratorBuilder<T> ofEmpty() {
        return NCollectionsRPI.of().iteratorEmptyBuilder();
    }

    static <T> NIteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        return NCollectionsRPI.of().iteratorBuilderOfFlatMap(from);
    }

    NIteratorBuilder<T> filter(Predicate<? super T> t, Supplier<NElement> e);

    NIteratorBuilder<T> filter(NPredicate<? super T> t);

    NIteratorBuilder<T> concat(NIteratorBuilder<T> t);

    NIteratorBuilder<T> concat(NIterator<T> t);

    <V> NIteratorBuilder<V> map(NFunction<? super T, ? extends V> t);


    <V> NIteratorBuilder<V> flatMap(Function<? super T, ? extends Iterator<? extends V>> fun);

    <V> NIteratorBuilder<V> mapMulti(NFunction<T, List<V>> mapper);

    <V> NIteratorBuilder<T> sort(NComparator<T> t, boolean removeDuplicates);

    <V> NIteratorBuilder<T> distinct();

    <V> NIteratorBuilder<T> distinct(NFunction<T, V> t);

    <V> NIteratorBuilder<T> named(NElement n);

    <V> NIteratorBuilder<T> named(NObjectElement nfo);


    NIteratorBuilder<T> safe(NIteratorErrorHandlerType type);

    NIteratorBuilder<T> safeIgnore();

    NIteratorBuilder<T> safePostpone();

    NIteratorBuilder<T> notNull();

    NIteratorBuilder<String> notBlank();

    NIterator<T> iterator();

    List<T> list();

    NIterator<T> build();

    List<T> toList();

    NIteratorBuilder<T> onFinish(NRunnable r);


    NIteratorBuilder<T> onStart(NRunnable r);
}
