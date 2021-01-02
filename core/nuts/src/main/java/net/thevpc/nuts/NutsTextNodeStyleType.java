package net.thevpc.nuts;

/**
 * @category Format
 */
public enum NutsTextNodeStyleType {
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
    BOOLEAN(false),
    KEYWORD(false),
    OPTION(false),
    USER_INPUT(false),
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

    NutsTextNodeStyleType(boolean basic) {
        this.basic = basic;
    }

    public boolean basic() {
        return basic;
    }
}
