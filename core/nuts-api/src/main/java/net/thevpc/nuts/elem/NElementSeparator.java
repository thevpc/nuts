package net.thevpc.nuts.elem;

public interface NElementSeparator extends NAffix {
    static NElementSeparator of(String value) {
        return NElementFactory.of().ofSeparator(value);
    }

    static NElementSeparator of() {
        return NElementFactory.of().ofSeparator(',');
    }

    static NElementSeparator of(char value) {
        return NElementFactory.of().ofSeparator(value);
    }

    String value();
}
