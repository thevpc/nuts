package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

public interface NElementSeparator extends NAffix {
    static NElementSeparator of(String value) {
        return NElementRPI.of().createSeparator(value);
    }

    static NElementSeparator of() {
        return NElementRPI.of().createSeparator(',');
    }

    static NElementSeparator of(char value) {
        return NElementRPI.of().createSeparator(value);
    }

    String value();
}
