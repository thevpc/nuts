package net.thevpc.nuts.elem;

public interface NElementHandle {
    NElementHandle parent();
    NElement element();
    NElementPath path();
    boolean isRoot();
}
