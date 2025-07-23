package net.thevpc.nuts.elem;

public interface NElementTransform {
    default NElement[] preTransform(NElement element){
        return new NElement[]{element};
    }
    default NElement[] postTransform(NElement element){
        return new NElement[]{element};
    }
}
