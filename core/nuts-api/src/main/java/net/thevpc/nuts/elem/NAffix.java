package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

public interface NAffix extends NBlankable {
    static NElementNewLine ofNewline() {
        return NElementFactory.of().ofNewline("\n");
    }

    static NElementNewLine ofNewline(String value) {
        return NElementFactory.of().ofNewline(value);
    }

    static NElementSpace ofSpace() {
        return NElementFactory.of().ofSpace(" ");
    }

    static NElementSpace ofSpace(int count) {
        NAssert.requireTrue(count > 0, () -> NMsg.ofC("spaces count should be positive"));
        return NElementFactory.of().ofSpace(NStringUtils.repeat(' ', count));
    }

    static NElementSpace ofSpace(String value) {
        return NElementFactory.of().ofSpace(value);
    }

    static NElementSeparator ofSeparator(String value) {
        return NElementFactory.of().ofSeparator(value);
    }

    static NElementSeparator ofSeparator() {
        return NElementFactory.of().ofSeparator(',');
    }

    static NElementSeparator ofSeparator(char value) {
        return NElementFactory.of().ofSeparator(value);
    }

    static NElementAnnotation ofAnnotation(String name) {
        return NElementFactory.of().ofAnnotation(name);
    }


    static NElementAnnotation ofAnnotation(String name, NElement... values) {
        return NElementFactory.of().ofAnnotation(name, values);
    }

    NAffixType type();
}
