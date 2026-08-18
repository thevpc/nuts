package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

public interface NExprElementReshaper {

    static NExprElementReshaper ofDefault() {
        return NElementRPI.of().createExprElementReshaper(NExprElementReshaperType.DEFAULT);
    }

    static NExprElementReshaper of(NExprElementReshaperType type) {
        return NElementRPI.of().createExprElementReshaper(type);
    }

    NElement reshape(NFlatExprElement flat);

    NExprElementReshaperBuilder builder();
}
