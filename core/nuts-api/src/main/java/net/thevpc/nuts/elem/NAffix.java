package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

/**
 * NAffix interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NAffix extends NBlankable {
    /**
     * Creates a new instance of of newline.
     *
     * @return of newline result
     */
    static NElementNewLine ofNewline() {
        return NElementRPI.of().createNewline("\n");
    }

    /**
     * Creates a new instance of of newline.
     *
     * @param value value
     * @return of newline result
     */
    static NElementNewLine ofNewline(String value) {
        return NElementRPI.of().createNewline(value);
    }

    /**
     * Creates a new instance of of space.
     *
     * @return of space result
     */
    static NElementSpace ofSpace() {
        return NElementRPI.of().createSpace(" ");
    }

    /**
     * Creates a new instance of of space.
     *
     * @param count count
     * @return of space result
     */
    static NElementSpace ofSpace(int count) {
        NAssert.requireTrue(count > 0, () -> NMsg.ofC("spaces count should be positive"));
        return NElementRPI.of().createSpace(NStringUtils.repeat(' ', count));
    }

    /**
     * Creates a new instance of of space.
     *
     * @param value value
     * @return of space result
     */
    static NElementSpace ofSpace(String value) {
        return NElementRPI.of().createSpace(value);
    }

    /**
     * Creates a new instance of of separator.
     *
     * @param value value
     * @return of separator result
     */
    static NElementSeparator ofSeparator(String value) {
        return NElementRPI.of().createSeparator(value);
    }

    /**
     * Creates a new instance of of separator.
     *
     * @return of separator result
     */
    static NElementSeparator ofSeparator() {
        return NElementRPI.of().createSeparator(',');
    }

    /**
     * Creates a new instance of of separator.
     *
     * @param value value
     * @return of separator result
     */
    static NElementSeparator ofSeparator(char value) {
        return NElementRPI.of().createSeparator(value);
    }

    /**
     * Creates a new instance of of annotation.
     *
     * @param name name
     * @return of annotation result
     */
    static NElementAnnotation ofAnnotation(String name) {
        return NElementRPI.of().createAnnotation(name);
    }


    /**
     * Creates a new instance of of annotation.
     *
     * @param name name
     * @param values values
     * @return of annotation result
     */
    static NElementAnnotation ofAnnotation(String name, NElement... values) {
        return NElementRPI.of().createAnnotation(name, values);
    }

    /**
     * Type.
     *
     * @return type result
     */
    NAffixType type();
}
