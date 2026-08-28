package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.util.NPredicateWithDescription;
import net.thevpc.nuts.util.NPredicate;

import java.util.function.Supplier;

/**
 * NPredicateDelegate class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NPredicateDelegate<T> extends NPredicateBase<T> {
    /**
     * Base predicate.
     *
     * @return base predicate result
     */
    public abstract NPredicate<T> basePredicate();

    @Override
    public boolean test(T t) {
        /**
         * Base predicate.
         *
         * @param ).test(t ).test(t
         * @return base predicate result
         */
        return basePredicate().test(t);
    }

    @Override
    public NElement describe() {
        /**
         * Base predicate.
         *
         * @param ).describe( ).describe(
         * @return base predicate result
         */
        return basePredicate().describe();
    }

    @Override
    public NPredicate<T> withDescription(Supplier<NElement> description) {
        if(description==null){
            return this;
        }
        return new NPredicateWithDescription<>(this,description);
    }
}
