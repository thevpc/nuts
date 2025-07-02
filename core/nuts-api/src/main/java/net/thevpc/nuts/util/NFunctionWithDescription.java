package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NFunctionWithDescription<T, V> implements NFunction<T, V>,NImmutable {
    private final NFunction<T, V> base;
    private Supplier<NElement> description;

    public NFunctionWithDescription(NFunction<T, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) {
        return base.apply(f);
    }

    @Override
    public NFunction<T, V> redescribe(Supplier<NElement> description) {
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
        return NDescribableElementSupplier.safeDescribe(
                description,
                NDescribableElementSupplier.ofPossibleDescribable(base),
                NDescribableElementSupplier.ofLateToString(this)
        );
    }
}
