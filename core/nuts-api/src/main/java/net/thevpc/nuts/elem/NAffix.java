package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

public interface NAffix extends NBlankable {
    static NElementNewLine ofNewline() {
        return NElementRPI.of().createNewline("\n");
    }

    static NElementNewLine ofNewline(String value) {
        return NElementRPI.of().createNewline(value);
    }

    static NElementSpace ofSpace() {
        return NElementRPI.of().createSpace(" ");
    }

    static NElementSpace ofSpace(int count) {
        NAssert.requireTrue(count > 0, () -> NMsg.ofC("spaces count should be positive"));
        return NElementRPI.of().createSpace(NStringUtils.repeat(' ', count));
    }

    static NElementSpace ofSpace(String value) {
        return NElementRPI.of().createSpace(value);
    }

    static NElementSeparator ofSeparator(String value) {
        return NElementRPI.of().createSeparator(value);
    }

    static NElementSeparator ofSeparator() {
        return NElementRPI.of().createSeparator(',');
    }

    static NElementSeparator ofSeparator(char value) {
        return NElementRPI.of().createSeparator(value);
    }

    static NElementAnnotation ofAnnotation(String name) {
        return NElementRPI.of().createAnnotation(name);
    }


    static NElementAnnotation ofAnnotation(String name, NElement... values) {
        return NElementRPI.of().createAnnotation(name, values);
    }

    NAffixType type();
}
