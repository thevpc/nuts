package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NPredicateDelegate;
import net.thevpc.nuts.util.NPredicate;

import java.util.function.Supplier;

/**
 * NPredicateWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPredicateWithDescription<T> extends NPredicateDelegate<T> {
    private NPredicate<T> base;
    private Supplier<NElement> description;

    /**
     * N predicate with description.
     *
     * @param base base
     * @param description description
     * @return n predicate with description result
     */
    public NPredicateWithDescription(NPredicate<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribe(
                description,
                NDescribables.ofDesc(base),
                NDescribables.ofLateToString(this)
        );
    }

    @Override
    public NPredicate<T> withDescription(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public NPredicate<T> basePredicate() {
        return base;
    }
}
