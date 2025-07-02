package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NDoubleFunction2WithDescription implements NDoubleFunction2,NImmutable {
    private final NDoubleFunction2 base;
    private Supplier<NElement> description;

    public NDoubleFunction2WithDescription(NDoubleFunction2 base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x, double y) {
        return base.apply(x, y);
    }

    @Override
    public NDoubleFunction2 redescribe(Supplier<NElement> description) {
        if(description==null){
            return base;
        }
        return new NDoubleFunction2WithDescription(base, description);
    }

    @Override
    public String toString() {
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
