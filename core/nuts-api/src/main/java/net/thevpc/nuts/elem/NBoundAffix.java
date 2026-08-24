package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

/**
 * NBoundAffix interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBoundAffix {
    /**
     * Creates a new instance of of.
     *
     * @param affix affix
     * @param anchor anchor
     * @return of result
     */
    static NBoundAffix of(NAffix affix, NAffixAnchor anchor) {
        return NElementRPI.of().createBoundAffix(affix, anchor);
    }

    /**
     * Affix.
     *
     * @return affix result
     */
    NAffix affix();

    /**
     * Anchor.
     *
     * @return anchor result
     */
    NAffixAnchor anchor();
}
