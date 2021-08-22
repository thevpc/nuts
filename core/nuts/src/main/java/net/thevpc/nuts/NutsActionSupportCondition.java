package net.thevpc.nuts;

public enum NutsActionSupportCondition implements NutsEnum{
    SUPPORTED,
    PREFERRED,
    ALWAYS,
    NEVER;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsActionSupportCondition() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsActionSupportCondition parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsActionSupportCondition parseLenient(String value, NutsActionSupportCondition emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsActionSupportCondition parseLenient(String value, NutsActionSupportCondition emptyValue, NutsActionSupportCondition errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsActionSupportCondition.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

}
