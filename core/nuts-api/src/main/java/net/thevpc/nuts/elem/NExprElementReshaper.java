package net.thevpc.nuts.elem;

public interface NExprElementReshaper {

    static NExprElementReshaper ofJavaLike() {
        return NElements.of().createJavaExprElementReshaper();
    }

    static NExprElementReshaper ofLogical() {
        return NElements.of().createLogicalExprElementReshaper();
    }

    static NExprElementReshaper ofLeftAssociative() {
        return NElements.of().createLeftAssociativeExprElementReshaper();
    }

    NElement reshape(NFlatExprElement flat);
    NExprElementReshaperBuilder builder();
}
