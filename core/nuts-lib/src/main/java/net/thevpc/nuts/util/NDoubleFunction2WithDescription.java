package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NDoubleFunction2WithDescription implements NDoubleFunction2,NImmutable {
    private final NDoubleFunction2 base;
    private NEDesc description;

    public NDoubleFunction2WithDescription(NDoubleFunction2 base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x, double y) {
        return base.apply(x, y);
    }

    @Override
    public NDoubleFunction2 withDesc(NEDesc description) {
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
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
