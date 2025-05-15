package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NFunctionWithDescription<T, V> implements NFunction<T, V>,NImmutable {
    private final NFunction<T, V> base;
    private NEDesc description;

    public NFunctionWithDescription(NFunction<T, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) {
        return base.apply(f);
    }

    @Override
    public NFunction<T, V> withDesc(NEDesc description) {
        if(description==null){
            return base;
        }
        return new NFunctionWithDescription<>(base, description);
    }

    @Override
    public String toString() {
        if(description!=null){
            return description.get().toString();
        }
        return "Function{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
