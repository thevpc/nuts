package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFunction;

public class NFunctionWithDescription<T, V> implements NFunction<T, V> {
    private final NFunction<? super T, V> base;
    private NEDesc description;

    public NFunctionWithDescription(NFunction<? super T, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) {
        return base.apply(f);
    }

    @Override
    public NFunction<T, V> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "Function{" + base + '}';
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribe(session,
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
