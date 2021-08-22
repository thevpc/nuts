package net.thevpc.nuts;

/**
 * @app.category Format
 */
public enum NutsTextStyleType implements NutsEnum{
    UNDERLINED(true),//_
    ITALIC(true),// /
    STRIKED(true),// -
    REVERSED(true),//v
    BOLD(true),//d
    BLINK(true),//k
    FORE_COLOR(true),//f
    BACK_COLOR(true),//b
    FORE_TRUE_COLOR(true),//f
    BACK_TRUE_COLOR(true),//b

    PRIMARY(false), //p
    SECONDARY(false),//s
    ERROR(false),
    WARN(false),
    INFO(false),
    CONFIG(false),
    COMMENTS(false),
    STRING(false),
    NUMBER(false),
    DATE(false),
    BOOLEAN(false),
    KEYWORD(false),
    OPTION(false),
    INPUT(false),
    SEPARATOR(false),
    OPERATOR(false),
    SUCCESS(false),
    FAIL(false),
    DANGER(false),
    VAR(false),
    PALE(false),
    PATH(false),
    VERSION(false),
    TITLE(false)
    ;
    private boolean basic;
    private String id;

    NutsTextStyleType(boolean basic) {
        this.basic = basic;
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }


    public boolean basic() {
        return basic;
    }

    public static NutsTextStyleType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTextStyleType parseLenient(String value, NutsTextStyleType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTextStyleType parseLenient(String value, NutsTextStyleType emptyValue, NutsTextStyleType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsTextStyleType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
