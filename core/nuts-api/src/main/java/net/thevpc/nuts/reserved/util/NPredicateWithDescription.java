package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NPredicateDelegate;
import net.thevpc.nuts.util.NPredicate;

public class NPredicateWithDescription<T> extends NPredicateDelegate<T> {
    private NPredicate<T> base;
    private NEDesc description;

    public NPredicateWithDescription(NPredicate<T> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }

    @Override
    public NPredicate<T> withDesc(NEDesc description) {
        this.description=description;
        return this;
    }

    @Override
    public NPredicate<T> basePredicate() {
        return base;
    }
}
