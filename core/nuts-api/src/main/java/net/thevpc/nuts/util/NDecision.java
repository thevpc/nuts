package net.thevpc.nuts.util;

/**
 * NDecision enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NDecision implements NEnum{
    ACCEPT,
    DENY,
    ABSTAIN
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NDecision() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NDecision> parse(String value) {
        return NEnumUtils.parseEnum(value, NDecision.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

}
