package net.thevpc.nuts.elem;

public interface NElementTransform {
    NElement[] preTransform(NElement yy);
    NElement[] postTransform(NElement yy);
}
