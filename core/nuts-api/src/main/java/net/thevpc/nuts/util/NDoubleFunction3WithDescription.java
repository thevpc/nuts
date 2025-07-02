package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NDoubleFunction3WithDescription implements NDoubleFunction3,NImmutable {
    private final NDoubleFunction3 base;
    private Supplier<NElement> description;

    public NDoubleFunction3WithDescription(NDoubleFunction3 base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x, double y, double z) {
        return base.apply(x, y,z);
    }

    @Override
    public NDoubleFunction3 redescribe(Supplier<NElement> description) {
        if(description==null){
            return base;
        }
        return new NDoubleFunction3WithDescription(base, description);
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
