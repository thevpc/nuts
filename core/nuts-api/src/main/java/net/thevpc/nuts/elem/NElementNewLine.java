package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NNewLineMode;

/**
 * NElementNewLine interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementNewLine extends NAffix{
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementNewLine of() {
        return NElementRPI.of().createNewline("\n");
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NElementNewLine of(String value) {
        return NElementRPI.of().createNewline(value);
    }

    /**
     * Value.
     *
     * @return value result
     */
    NNewLineMode value();
}
