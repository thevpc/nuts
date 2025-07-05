package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NPredicateDelegate;
import net.thevpc.nuts.util.NPredicate;

import java.util.function.Supplier;

public class NPredicateWithDescription<T> extends NPredicateDelegate<T> {
    private NPredicate<T> base;
    private Supplier<NElement> description;

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
    public NPredicate<T> redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public NPredicate<T> basePredicate() {
        return base;
    }
}
