package net.thevpc.nuts.util;

/**
 * NDecisionConflict enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NDecisionConflict implements NEnum{
    ACCEPT_WINS,
    DENY_WINS
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NDecisionConflict() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NDecisionConflict> parse(String value) {
        return NEnumUtils.parseEnum(value, NDecisionConflict.class);
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
