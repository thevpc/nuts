/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NPredicate;

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

    /**
     * Creates a new instance of of coalesce.
     *
     * @param t t
     * @return of coalesce result
     */
    static <T> NIteratorBuilder<T> ofCoalesce(List<NIterator<? extends T>> t) {
        return NUtilsRPI.of().iteratorBuilderOfCoalesce(t);
    }

    /**
     * Creates a new instance of of concat.
     *
     * @param t t
     * @return of concat result
     */
    static <T> NIteratorBuilder<T> ofConcat(List<NIterator<? extends T>> t) {
        return NUtilsRPI.of().iteratorBuilderOfConcat(t);
    }

    /**
     * Creates a new instance of of.
     *
     * @param t t
     * @return of result
     */
    static <T> NIteratorBuilder<T> of(Iterator<T> t) {
        return NUtilsRPI.of().iteratorBuilder(t);
    }

    /**
     * Creates a new instance of of runnable.
     *
     * @param t t
     * @return of runnable result
     */
    static <T> NIteratorBuilder<T> ofRunnable(NRunnable t) {
        return NUtilsRPI.of().iteratorBuilderOfRunnable(t);
    }

    /**
     * Creates a new instance of of runnable.
     *
     * @param t t
     * @param n n
     * @return of runnable result
     */
    static <T> NIteratorBuilder<T> ofRunnable(Runnable t, String n) {
        return NUtilsRPI.of().iteratorBuilderOfRunnable(t, n);
    }

    /**
     * Creates a new instance of of supplier.
     *
     * @param from from
     * @param name name
     * @return of supplier result
     */
    static <T> NIteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name) {
        return NUtilsRPI.of().iteratorBuilderOfSupplier(from, name);
    }

    /**
     * Creates a new instance of of array values.
     *
     * @param t t
     * @param n n
     * @return of array values result
     */
    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, NElement n) {
        return NUtilsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    /**
     * Creates a new instance of of array values.
     *
     * @param t t
     * @param n n
     * @return of array values result
     */
    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, String n) {
        return NUtilsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    /**
     * Creates a new instance of of array values.
     *
     * @param t t
     * @param n n
     * @return of array values result
     */
    static <T> NIteratorBuilder<T> ofArrayValues(T[] t, Supplier<NElement> n) {
        return NUtilsRPI.of().iteratorBuilderOfArrayValues(t, n);
    }

    /**
     * Creates a new instance of of empty.
     *
     * @return of empty result
     */
    static <T> NIteratorBuilder<T> ofEmpty() {
        return NUtilsRPI.of().iteratorEmptyBuilder();
    }

    /**
     * Creates a new instance of of flat map.
     *
     * @param from from
     * @return of flat map result
     */
    static <T> NIteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from) {
        return NUtilsRPI.of().iteratorBuilderOfFlatMap(from);
    }

    /**
     * Filter.
     *
     * @param t t
     * @param e e
     * @return filter result
     */
    NIteratorBuilder<T> filter(Predicate<? super T> t, Supplier<NElement> e);

    /**
     * Filter.
     *
     * @param t t
     * @return filter result
     */
    NIteratorBuilder<T> filter(NPredicate<? super T> t);

    /**
     * Concat.
     *
     * @param t t
     * @return concat result
     */
    NIteratorBuilder<T> concat(NIteratorBuilder<T> t);

    /**
     * Concat.
     *
     * @param t t
     * @return concat result
     */
    NIteratorBuilder<T> concat(NIterator<T> t);

    /**
     * Map.
     *
     * @param t t
     * @return map result
     */
    <V> NIteratorBuilder<V> map(NFunction<? super T, ? extends V> t);


    /**
     * Flat map.
     *
     * @param fun fun
     * @return flat map result
     */
    <V> NIteratorBuilder<V> flatMap(Function<? super T, ? extends Iterator<? extends V>> fun);

    /**
     * Flat map list.
     *
     * @param mapper mapper
     * @return flat map list result
     */
    <V> NIteratorBuilder<V> flatMapList(NFunction<T, List<V>> mapper);

    /**
     * Sort.
     *
     * @param t t
     * @param removeDuplicates remove duplicates
     * @return sort result
     */
    <V> NIteratorBuilder<T> sort(NComparator<T> t, boolean removeDuplicates);

    /**
     * Distinct.
     *
     * @return distinct result
     */
    <V> NIteratorBuilder<T> distinct();

    /**
     * Distinct.
     *
     * @param t t
     * @return distinct result
     */
    <V> NIteratorBuilder<T> distinct(NFunction<T, V> t);

    /**
     * Named.
     *
     * @param n n
     * @return named result
     */
    <V> NIteratorBuilder<T> named(NElement n);

    /**
     * Named.
     *
     * @param nfo nfo
     * @return named result
     */
    <V> NIteratorBuilder<T> named(NObjectElement nfo);


    /**
     * Safe.
     *
     * @param type type
     * @return safe result
     */
    NIteratorBuilder<T> safe(NIteratorErrorHandlerType type);

    /**
     * Safe ignore.
     *
     * @return safe ignore result
     */
    NIteratorBuilder<T> safeIgnore();

    /**
     * Safe postpone.
     *
     * @return safe postpone result
     */
    NIteratorBuilder<T> safePostpone();

    /**
     * Not null.
     *
     * @return not null result
     */
    NIteratorBuilder<T> notNull();

    /**
     * Not blank.
     *
     * @return not blank result
     */
    NIteratorBuilder<T> notBlank();

    /**
     * Iterator.
     *
     * @return iterator result
     */
    NIterator<T> iterator();

    /**
     * List.
     *
     * @return list result
     */
    List<T> list();

    /**
     * Build.
     *
     * @return build result
     */
    NIterator<T> build();

    /**
     * Converts to list.
     *
     * @return to list result
     */
    List<T> toList();

    /**
     * On finish.
     *
     * @param r r
     * @return on finish result
     */
    NIteratorBuilder<T> onFinish(NRunnable r);


    /**
     * On start.
     *
     * @param r r
     * @return on start result
     */
    NIteratorBuilder<T> onStart(NRunnable r);
}
