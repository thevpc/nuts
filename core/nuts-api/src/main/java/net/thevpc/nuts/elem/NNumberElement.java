package net.thevpc.nuts.elem;

public interface NNumberElement extends NPrimitiveElement {
    Number numberValue();
    NNumberLayout numberLayout();

    String numberSuffix();
}
