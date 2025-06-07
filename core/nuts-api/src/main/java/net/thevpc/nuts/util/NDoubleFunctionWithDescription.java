package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NDoubleFunctionWithDescription implements NDoubleFunction,NImmutable {
    private final NDoubleFunction base;
    private final NEDesc description;

    public NDoubleFunctionWithDescription(NDoubleFunction base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x) {
        return base.apply(x);
    }

    @Override
    public NDoubleFunction withDesc(NEDesc description) {
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
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
