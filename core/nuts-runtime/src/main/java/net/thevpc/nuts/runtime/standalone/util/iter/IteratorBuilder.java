/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

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

    public static final NutsPredicate NON_NULL = NutsPredicates.isNull().negate();
    public static final NutsPredicate NON_BLANK = NutsPredicates.blank().negate();
    static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator<>();
    private final NutsIterator<T> it;
    private final NutsSession session;

    private IteratorBuilder(NutsIterator<T> it, NutsSession session) {
        if (it == null) {
            it = emptyIterator();
        }
        this.it = it;
        this.session = session;
    }

    public static <T> IteratorBuilder<T> ofCoalesce(List<NutsIterator<? extends T>> t, NutsSession session) {
        return new IteratorBuilder<>(
                IteratorUtils.coalesce(t),
                session);
    }

    public static <T> IteratorBuilder<T> ofConcat(List<NutsIterator<? extends T>> t, NutsSession session) {
        return new IteratorBuilder<>(
                IteratorUtils.concat(t),
                session);
    }

    public static <T> IteratorBuilder<T> of(NutsIterator<T> t, NutsSession session) {
        return new IteratorBuilder<>(t, session);
    }

    public static <T> IteratorBuilder<T> ofRunnable(NutsRunnable t, NutsSession session) {
        return (IteratorBuilder) of(
                emptyIterator(),
                session).onStart(t);
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, NutsElement n, NutsSession session) {
        return ofRunnable(NutsRunnable.of(t, n), session);
    }

    public static <T> IteratorBuilder<T> ofRunnable(Runnable t, String n, NutsSession session) {
        return ofRunnable(NutsRunnable.of(t, n), session);
    }
//
//    public static <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    public static <T> IteratorBuilder<T> ofSupplier(Supplier<Iterator<T>> from, Function<NutsSession, NutsElement> name, NutsSession session) {
        return of(new SupplierIterator2<T>(from, name), session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, NutsElement n, NutsSession session) {
        return ofArrayValues(t, e -> n, session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, String n, NutsSession session) {
        return ofArrayValues(t, e -> NutsElements.of(e).ofString(n), session);
    }

    public static <T> IteratorBuilder<T> ofArrayValues(T[] t, Function<NutsSession, NutsElement> n, NutsSession session) {
        return of(t == null ? emptyIterator() :
                new NutsIteratorAdapter<T>(
                        Arrays.asList(t).iterator(), n),
                session);
    }

    public static <T> NutsIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    public static <T> IteratorBuilder<T> emptyBuilder(NutsSession session) {
        return of(EMPTY_ITERATOR, session);
    }

    public static <T> IteratorBuilder<T> ofFlatMap(NutsIterator<? extends Collection<T>> from, NutsSession session) {
        if (from == null) {
            return emptyBuilder(session);
        }
        return of(new FlatMapIterator<>(from, Collection::iterator), session);
    }

    public IteratorBuilder<T> filter(Predicate<? super T> t, Function<NutsSession, NutsElement> e) {
        if (t == null) {
            return this;
        }
        return of(new FilteredIterator<>(it, NutsPredicate.of(t, e)), session);
    }

    public IteratorBuilder<T> filter(NutsPredicate<? super T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(new FilteredIterator<>(it, t), session);
    }

    public IteratorBuilder<T> concat(IteratorBuilder<T> t) {
        return concat(t.it);
    }

    public IteratorBuilder<T> concat(NutsIterator<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(IteratorUtils.concat(Arrays.asList(it, t)), session);
    }

    public <V> IteratorBuilder<V> map(NutsFunction<? super T, ? extends V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t), session);
    }


    public <V> IteratorBuilder<V> flatMap(NutsFunction<? super T, ? extends Iterator<? extends V>> fun) {
        return of(new FlatMapIterator<T, V>(it, fun), session);
    }

    public <V> IteratorBuilder<V> mapMulti(NutsFunction<T, List<V>> mapper) {
        return new IteratorBuilder<>(
                new FlatMapIterator<>(it, t -> mapper.apply(t).iterator()),
                session);
    }

    public <V> IteratorBuilder<T> sort(NutsComparator<T> t, boolean removeDuplicates) {
        return new IteratorBuilder<>(IteratorUtils.sort(it, t, removeDuplicates), session);
    }

    public <V> IteratorBuilder<T> distinct() {
        return distinct(null);
    }

    public <V> IteratorBuilder<T> distinct(NutsFunction<T, V> t) {
        if (t == null) {
            return new IteratorBuilder<>(IteratorUtils.distinct(it), session);
        } else {
            return new IteratorBuilder<>(IteratorUtils.distinct(it, t), session);
        }
    }

    public <V> IteratorBuilder<T> named(String n) {
        if (n != null) {
            return new IteratorBuilder<>(new NutsIteratorAdapter<T>(
                    it, e -> NutsElements.of(e).ofString(n)), session);
        }
        return this;
    }

    public <V> IteratorBuilder<T> named(NutsObjectElement nfo) {
        if (nfo != null) {
            return new IteratorBuilder<>(new NutsIteratorAdapter<T>(it, e -> nfo), session);
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
        return of(new OnFinishIterator<>(it, r), session);
    }


    public IteratorBuilder<T> onStart(NutsRunnable r) {
        if (r == null) {
            return this;
        }
        return of(new OnStartIterator<>(it, r), session);
    }
}
