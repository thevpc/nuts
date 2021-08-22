package net.thevpc.nuts;

/**
 * @app.category Format
 */
public enum NutsTableSeparator implements NutsEnum{
    FIRST_ROW_START,
    FIRST_ROW_LINE,
    FIRST_ROW_SEP,
    FIRST_ROW_END,
    ROW_START,
    ROW_SEP,
    ROW_END,
    MIDDLE_ROW_START,
    MIDDLE_ROW_LINE,
    MIDDLE_ROW_SEP,
    MIDDLE_ROW_END,
    LAST_ROW_START,
    LAST_ROW_LINE,
    LAST_ROW_SEP,
    LAST_ROW_END;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsTableSeparator() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    public static NutsTableSeparator parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTableSeparator parseLenient(String value, NutsTableSeparator emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTableSeparator parseLenient(String value, NutsTableSeparator emptyValue, NutsTableSeparator errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsTableSeparator.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
