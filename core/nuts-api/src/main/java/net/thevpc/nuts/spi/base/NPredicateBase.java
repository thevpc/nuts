package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.util.NPredicateWithDescription;
import net.thevpc.nuts.util.NPredicate;
import net.thevpc.nuts.internal.NReservedNPredicateUtils;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * AbstractNPredicate class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NPredicateBase<T> implements NPredicate<T> {
    @Override
    public NPredicate<T> and(Predicate<? super T> other) {
        return new NReservedNPredicateUtils.And<T>(this, other);
    }

    @Override
    public NPredicate<T> negate() {
        return new NReservedNPredicateUtils.Not<>(this);
    }

    @Override
    public NPredicate<T> or(Predicate<? super T> other) {
        return new NReservedNPredicateUtils.Or<T>(this, other);
    }

    @Override
    public NPredicate<T> withDescription(Supplier<NElement> description) {
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
