package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NDoubleFunction3WithDescription implements NDoubleFunction3,NImmutable {
    private final NDoubleFunction3 base;
    private NEDesc description;

    public NDoubleFunction3WithDescription(NDoubleFunction3 base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x, double y, double z) {
        return base.apply(x, y,z);
    }

    @Override
    public NDoubleFunction3 withDesc(NEDesc description) {
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
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
