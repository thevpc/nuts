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
     * Creates a new instance.
     *
     * @param style style
     * @return of result
     */
    static NElementFormatter of(NElementFormatterStyle style) {
        return NElementRPI.of().createElementFormatter(style);
    }

    /**
     * Creates a new instance of pretty.
     *
     * @return of pretty result
     */
    static NElementFormatter ofPretty() {
        return of(NElementFormatterStyle.PRETTY);
    }

    /**
     * Creates a new instance of compact.
     *
     * @param compact compact
     * @return of compact result
     */
    static NElementFormatter ofCompact(boolean compact) {
        return compact ? of(NElementFormatterStyle.COMPACT) : of(NElementFormatterStyle.PRETTY);
    }

    /**
     * Creates a new instance of compact.
     *
     * @return of compact result
     */
    static NElementFormatter ofCompact() {
        return of(NElementFormatterStyle.COMPACT);
    }

    /**
     * Creates a new instance of stable.
     *
     * @return of stable result
     */
    static NElementFormatter ofStable() {
        return of(NElementFormatterStyle.STABLE);
    }

    /**
     * Creates a new instance of verbatim.
     *
     * @return of verbatim result
     */
    static NElementFormatter ofVerbatim() {
        return of(NElementFormatterStyle.VERBATIM);
    }

    /**
     * Creates a new instance of simple.
     *
     * @return of simple result
     */
    static NElementFormatter ofSimple() {
        return of(NElementFormatterStyle.SIMPLE);
    }

    /**
     * Builder.
     *
     * @return builder result
     */
    NElementFormatterBuilder builder();
}
