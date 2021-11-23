package net.thevpc.nuts;

import java.util.function.Function;
import java.util.function.Predicate;

public interface NutsPredicate<T> extends Predicate<T>, NutsDescribable {
    static <T> NutsPredicate<T> of(Predicate<T> o, String descr){
        return NutsDescribables.ofPredicate(o,e->e.ofString(descr));
    }
    static <T> NutsPredicate<T> of(Predicate<T> o,NutsElement descr){
        return NutsDescribables.ofPredicate(o,e->descr);
    }
    static <T> NutsPredicate<T> of(Predicate<T> o, Function<NutsElements, NutsElement> descr){
        return NutsDescribables.ofPredicate(o,descr);
    }

    NutsPredicate<T> and(Predicate<? super T> other);

    @Override
    NutsPredicate<T> negate();

    @Override
    NutsPredicate<T> or(Predicate<? super T> other);
}
