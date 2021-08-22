package net.thevpc.nuts;

/**
 * @app.category Format
 */
public enum NutsTextType implements NutsEnum{
    PLAIN,
    LIST,
    TITLE,
    COMMAND,
    LINK,
    STYLED,
    ANCHOR,
    CODE;
    private String id;

    NutsTextType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsTextType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTextType parseLenient(String value, NutsTextType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTextType parseLenient(String value, NutsTextType emptyValue, NutsTextType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsTextType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
