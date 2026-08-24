package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

/**
 * NElementSpace interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementSpace extends NAffix{
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementSpace of() {
        return NElementRPI.of().createSpace(" ");
    }

    /**
     * Creates a new instance of of.
     *
     * @param count count
     * @return of result
     */
    static NElementSpace of(int count) {
        NAssert.requireTrue(count > 0, () -> NMsg.ofC("spaces count should be positive"));
        return NElementRPI.of().createSpace(NStringUtils.repeat(' ', count));
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NElementSpace of(String value) {
        return NElementRPI.of().createSpace(value);
    }

    /**
     * Value.
     *
     * @return value result
     */
    String value();
}
