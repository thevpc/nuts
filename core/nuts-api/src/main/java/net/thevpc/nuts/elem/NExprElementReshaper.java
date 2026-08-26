package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

/**
 * NExprElementReshaper interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprElementReshaper {

    /**
     * Creates a new instance of of default.
     *
     * @return of default result
     */
    static NExprElementReshaper ofDefault() {
        return NElementRPI.of().createExprElementReshaper(NExprElementReshaperType.DEFAULT);
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @return of result
     */
    static NExprElementReshaper of(NExprElementReshaperType type) {
        return NElementRPI.of().createExprElementReshaper(type);
    }

    /**
     * Reshape.
     *
     * @param flat flat
     * @return reshape result
     */
    NElement reshape(NFlatExprElement flat);

    /**
     * Builder.
     *
     * @return builder result
     */
    NExprElementReshaperBuilder builder();
}
