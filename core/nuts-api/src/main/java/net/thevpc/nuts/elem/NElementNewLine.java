package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

public interface NElementNewLine extends NAffix{
    static NElementNewLine of() {
        return NElementFactory.of().ofNewline("\n");
    }

    static NElementNewLine of(String value) {
        return NElementFactory.of().ofNewline(value);
    }

    NNewLineMode value();
}
