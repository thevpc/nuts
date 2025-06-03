package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reserved.util.NPredicateWithDescription;
import net.thevpc.nuts.util.NPredicate;
import net.thevpc.nuts.util.NPredicates;

import java.util.function.Predicate;

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
    public NPredicate<T> withDesc(NEDesc description) {
        if(description==null){
            return this;
        }
        return new NPredicateWithDescription<>(this,description);
    }

    @Override
    public NElement describe() {
        return NElements.ofString(toString());
    }
}
