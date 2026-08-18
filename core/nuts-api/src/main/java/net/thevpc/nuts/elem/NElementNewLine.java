package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NNewLineMode;

public interface NElementNewLine extends NAffix{
    static NElementNewLine of() {
        return NElementRPI.of().createNewline("\n");
    }

    static NElementNewLine of(String value) {
        return NElementRPI.of().createNewline(value);
    }

    NNewLineMode value();
}
