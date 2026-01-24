package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NElementTokenType implements NEnum {
    INSTANT,
    DATETIME,
    DATE,
    TIME,

    REGEX,

    LBRACE,

    RBRACE,

    NUMBER,


    DOUBLE_QUOTED_STRING,

    SINGLE_QUOTED_STRING,

    LINE_STRING,

    BLOCK_STRING,

    BACKTICK_STR,

    TRIPLE_DOUBLE_QUOTED_STRING,

    TRIPLE_SINGLE_QUOTED_STRING,

    TRIPLE_BACKTICK_STRING,

    TRUE,

    FALSE,

    NULL,

    LPAREN,

    RPAREN,

    LBRACK,

    RBRACK,

    COLON,
    BACKSLASH,
    BACKSLASH2,
    BACKSLASH3,

    OP,

    ORDERED_LIST,


    UNORDERED_LIST,


    SEMICOLON,
    SEMICOLON2,
    COMMA,
    AT,
    CHAR_STREAM,
    BINARY_STREAM,
    NAME,
    UNKNOWN,
    SPACE,
    NEWLINE,
    LINE_COMMENT,
    BLOCK_COMMENT,
    ;

    private final String id;

//    NElementTokenType() {
//        this.id = NNameFormat.ID_NAME.format(name());
//    }

    NElementTokenType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementTokenType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementTokenType.class);
    }


    @Override
    public String id() {
        return id;
    }
}
