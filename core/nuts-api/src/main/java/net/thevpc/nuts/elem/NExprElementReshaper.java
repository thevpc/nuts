package net.thevpc.nuts.elem;

public interface NExprElementReshaper {

    static NExprElementReshaper ofDefault() {
        return NElements.of().createExprElementReshaper(NExprElementReshaperType.DEFAULT);
    }

    static NExprElementReshaper of(NExprElementReshaperType type) {
        return NElements.of().createExprElementReshaper(type);
    }

    NElement reshape(NFlatExprElement flat);

    NExprElementReshaperBuilder builder();
}
