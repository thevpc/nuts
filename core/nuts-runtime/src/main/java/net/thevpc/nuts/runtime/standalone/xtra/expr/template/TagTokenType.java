package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

enum TagTokenType {
    PLAIN,
    EXPR,
    IF,
    CTRL_ELSE_IF,
    CTRL_ELSE,
    CTRL_END,
    INCLUDE,
    FOR,
    STATEMENT,
    CTRL_OTHER,
}
