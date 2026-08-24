package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

/**
 * NElementFormatter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementFormatter extends NElementTransform {
    /**
     * Creates a new instance of of.
     *
     * @param style style
     * @return of result
     */
    static NElementFormatter of(NElementFormatterStyle style) {
        return NElementRPI.of().createElementFormatter(style);
    }

    /**
     * Creates a new instance of of pretty.
     *
     * @return of pretty result
     */
    static NElementFormatter ofPretty() {
        /**
         * Creates a new instance of of.
         *
         * @param NElementFormatterStyle.PRETTY n element formatter style.pretty
         * @return of result
         */
        return of(NElementFormatterStyle.PRETTY);
    }

    /**
     * Creates a new instance of of compact.
     *
     * @param compact compact
     * @return of compact result
     */
    static NElementFormatter ofCompact(boolean compact) {
        return compact ? of(NElementFormatterStyle.COMPACT) : of(NElementFormatterStyle.PRETTY);
    }

    /**
     * Creates a new instance of of compact.
     *
     * @return of compact result
     */
    static NElementFormatter ofCompact() {
        /**
         * Creates a new instance of of.
         *
         * @param NElementFormatterStyle.COMPACT n element formatter style.compact
         * @return of result
         */
        return of(NElementFormatterStyle.COMPACT);
    }

    /**
     * Creates a new instance of of stable.
     *
     * @return of stable result
     */
    static NElementFormatter ofStable() {
        /**
         * Creates a new instance of of.
         *
         * @param NElementFormatterStyle.STABLE n element formatter style.stable
         * @return of result
         */
        return of(NElementFormatterStyle.STABLE);
    }

    /**
     * Creates a new instance of of verbatim.
     *
     * @return of verbatim result
     */
    static NElementFormatter ofVerbatim() {
        /**
         * Creates a new instance of of.
         *
         * @param NElementFormatterStyle.VERBATIM n element formatter style.verbatim
         * @return of result
         */
        return of(NElementFormatterStyle.VERBATIM);
    }

    /**
     * Creates a new instance of of simple.
     *
     * @return of simple result
     */
    static NElementFormatter ofSimple() {
        /**
         * Creates a new instance of of.
         *
         * @param NElementFormatterStyle.SIMPLE n element formatter style.simple
         * @return of result
         */
        return of(NElementFormatterStyle.SIMPLE);
    }

    /**
     * Builder.
     *
     * @return builder result
     */
    NElementFormatterBuilder builder();
}
