package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NElementTokenType implements NEnum {
    INSTANT(NElementType.INSTANT),
    LOCAL_DATETIME(NElementType.LOCAL_DATETIME),
    LOCAL_DATE(NElementType.LOCAL_DATE),
    LOCAL_TIME(NElementType.LOCAL_TIME),

    NUMBER,


    DOUBLE_QUOTED_STRING(NElementType.DOUBLE_QUOTED_STRING),

    SINGLE_QUOTED_STRING(NElementType.SINGLE_QUOTED_STRING),

    LINE_STRING(NElementType.LINE_STRING),

    BLOCK_STRING(NElementType.BLOCK_STRING),

    BACKTICK_STRING(NElementType.BACKTICK_STRING),

    TRIPLE_DOUBLE_QUOTED_STRING(NElementType.TRIPLE_DOUBLE_QUOTED_STRING),

    TRIPLE_SINGLE_QUOTED_STRING(NElementType.TRIPLE_SINGLE_QUOTED_STRING),

    TRIPLE_BACKTICK_STRING(NElementType.TRIPLE_BACKTICK_STRING),


    NULL(NElementType.NULL),
    OPERATOR_SYMBOL(NElementType.OPERATOR_SYMBOL),
    ORDERED_LIST(NElementType.ORDERED_LIST),
    UNORDERED_LIST(NElementType.UNORDERED_LIST),


    TRUE,
    FALSE,
    LPAREN,
    RPAREN,
    LBRACK,
    RBRACK,
    COLON,
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
    LBRACE,
    RBRACE,
    ;

    private final String id;
    private final NElementType elementType;

    NElementTokenType(NElementType elementType) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.elementType = elementType;
    }
    NElementTokenType() {
        this.id = NNameFormat.ID_NAME.format(name());
        this.elementType = null;
    }

    public NElementType elementType() {
        return elementType;
    }

    public static NOptional<NElementTokenType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementTokenType.class);
    }


    @Override
    public String id() {
        return id;
    }
}
