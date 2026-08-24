package net.thevpc.nuts.cmdline;

/**
 * NArgCompleteCandidate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NArgCompleteCandidate {
    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NArgCompleteCandidate of(String value) {
        return new DefaultNArgCompleteCandidate(value);
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @param display display
     * @return of result
     */
    static NArgCompleteCandidate of(String value, String display) {
        return new DefaultNArgCompleteCandidate(value, display);
    }

    /**
     * Value.
     *
     * @return value result
     */
    String value();

    /**
     * Display.
     *
     * @return display result
     */
    String display();
}
