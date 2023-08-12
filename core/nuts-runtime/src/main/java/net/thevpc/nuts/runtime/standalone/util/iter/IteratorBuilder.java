/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
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
public class IteratorBuilder<T> {

    public static final NPredicate NON_NULL = NPredicates.isNull().negate();
    public static final NPredicate NON_BLANK = NPredicates.blank().negate();
    static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator<>();
    private final NIterator<T> it;
    private final NSession session;

    private IteratorBuilder(NIterator<T> it, NSession session) {
        if (it == null) {
            it = emptyIterator();
        }
        this.it = it;
        this.session = session;
    }

    public static <T> IteratorBuilder<T> ofCoalesce(List<NIterator<? extends T>> t, NSession session) {
        return new IteratorBuilder<>(
                IteratorUtils.coalesce(t),
                session);
    }

    public static <T> IteratorBuilder<T> ofConcat(List<NIterator<? extends T>> t, NSession session) {
        return new IteratorBuilder<>(
                IteratorUtils.concat(t),
                session);
    }

    public static <T> IteratorBuilder<T> of(NIterator<T> t, NSession session) {
        return new IteratorBuilder<>(t, session);
    }

    public static <T> IteratorBuilder<T> ofRunnable(NRunnable t, NSession session) {
        return (IteratorBuilder) of(
                emptyIterator(),
                session).onStart(t);
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n, NSession session) {
        return ofRunnable(NRunnable.of(t, n), session);
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, String n, NSession session) {
        return ofRunnable(NRunnable.of(t, n), session);
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> IteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, Function<NSession, NElement> name, NSession session) {
        return of(new SupplierIterator2<T>(from, name), session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, NElement n, NSession session) {
        return ofArrayValues(t, e -> n, session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, String n, NSession session) {
        return ofArrayValues(t, e -> NElements.of(e).ofString(n), session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, Function<NSession, NElement> n, NSession session) {
        return of(t == null ? emptyIterator() :
                new NIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n),
                session);
    }

    public static <T> NIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> IteratorBuilder<T> emptyBuilder(NSession session) {
        return of(EMPTY_ITERATOR, session);
    }

    public static <T> IteratorBuilder<T> ofFlatMap(NIterator<? extends Collection<T>> from, NSession session) {
        if (from == null) {
            return emptyBuilder(session);
        }
        return of(new FlatMapIterator<>(from, Collection::iterator), session);
    }

    public IteratorBuilder<T> filter(Predicate<? super T> t, Function<NSession, NElement> e) {
        if (t == null) {
            return this;
        }
        return of(new FilteredIterator<>(it, NPredicate.of(t, e)), session);
    }

    public IteratorBuilder<T> filter(NPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(new FilteredIterator<>(it, t), session);
    }

    public IteratorBuilder<T> concat(IteratorBuilder<T> t) {
        return concat(t.it);
    }

    public IteratorBuilder<T> concat(NIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(IteratorUtils.concat(Arrays.asList(it, t)), session);
    }

    public <V> IteratorBuilder<V> map(NFunction<? super T, ? extends V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t), session);
    }


    public <V> IteratorBuilder<V> flatMap(NFunction<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new FlatMapIterator<T, V>(it, fun), session);
    }

    public <V> IteratorBuilder<V> mapMulti(NFunction<T, List<V>> mapper) {
        return new IteratorBuilder<>(
                new FlatMapIterator<>(it, t -> mapper.apply(t).iterator()),
                session);
    }

    public <V> IteratorBuilder<T> sort(NComparator<T> t, boolean removeDuplicates) {
        return new IteratorBuilder<>(IteratorUtils.sort(it, t, removeDuplicates), session);
    }

    public <V> IteratorBuilder<T> distinct() {
        return distinct(null);
    }

    public <V> IteratorBuilder<T> distinct(NFunction<T, V> t) {
        if (t == null) {
            return new IteratorBuilder<>(IteratorUtils.distinct(it), session);
        } else {
            return new IteratorBuilder<>(IteratorUtils.distinct(it, t), session);
        }
    }

    public <V> IteratorBuilder<T> named(String n) {
        if (n != null) {
            return new IteratorBuilder<>(new NIteratorAdapter<T>(
                    it, e -> NElements.of(e).ofString(n)), session);
        }
        return this;
    }

    public <V> IteratorBuilder<T> named(NObjectElement nfo) {
        if (nfo != null) {
            return new IteratorBuilder<>(new NIteratorAdapter<T>(it, e -> nfo), session);
        }
        return this;
    }


    public IteratorBuilder<T> safe(IteratorErrorHandlerType type) {
        return new IteratorBuilder<>(new ErrorHandlerIterator(type, it,session), session);
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
        return of(new OnFinishIterator<>(it, r), session);
    }


    public IteratorBuilder<T> onStart(NRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new OnStartIterator<>(it, r), session);
    }
}
