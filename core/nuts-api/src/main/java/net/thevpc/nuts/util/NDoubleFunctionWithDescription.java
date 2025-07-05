package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NDoubleFunctionWithDescription implements NDoubleFunction,NImmutable {
    private final NDoubleFunction base;
    private final Supplier<NElement> description;

    public NDoubleFunctionWithDescription(NDoubleFunction base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x) {
        return base.apply(x);
    }

    @Override
    public NDoubleFunction redescribe(Supplier<NElement> description) {
        if(description==null){
            return base;
        }
        return new NDoubleFunctionWithDescription(base, description);
    }

    @Override
    public String toString() {
        return "Function{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribe(
                description,
                NDescribables.ofDesc(base),
                NDescribables.ofLateToString(this)
        );
    }
}
