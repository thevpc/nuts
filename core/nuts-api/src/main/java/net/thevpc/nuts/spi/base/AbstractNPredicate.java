package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.reserved.util.NPredicateWithDescription;
import net.thevpc.nuts.util.NPredicate;
import net.thevpc.nuts.util.NPredicates;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractNPredicate<T> implements NPredicate<T> {
    @Override
    public NPredicate<T> and(Predicate<? super T> other) {
        return new NPredicates.And<T>(this, other);
    }

    @Override
    public NPredicate<T> negate() {
        return new NPredicates.Not<>(this);
    }

    @Override
    public NPredicate<T> or(Predicate<? super T> other) {
        return new NPredicates.Or<T>(this, other);
    }

    @Override
    public NPredicate<T> redescribe(Supplier<NElement> description) {
        if(description==null){
            return this;
        }
        return new NPredicateWithDescription<>(this,description);
    }

    @Override
    public NElement describe() {
        return NElement.ofString(toString());
    }
}
