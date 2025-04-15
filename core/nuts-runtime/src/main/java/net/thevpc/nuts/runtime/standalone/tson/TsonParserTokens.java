package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonParserTokens {

    /**
     * End of File.
     */
    int EOF = 0;
    /**
     * RegularExpression Id.
     */
    int DATETIME = 1;
    /**
     * RegularExpression Id.
     */
    int DATE = 2;
    /**
     * RegularExpression Id.
     */
    int TIME = 3;
    /**
     * RegularExpression Id.
     */
    int REGEX = 4;
    /**
     * RegularExpression Id.
     */
    int LBRACE = 5;
    /**
     * RegularExpression Id.
     */
    int RBRACE = 6;
    /**
     * RegularExpression Id.
     */
    int SHORT = 7;
    /**
     * RegularExpression Id.
     */
    int BYTE = 8;
    /**
     * RegularExpression Id.
     */
    int LONG = 9;
    /**
     * RegularExpression Id.
     */
    int INTEGER = 10;
    /**
     * RegularExpression Id.
     */
    int INTEGER_H = 11;
    /**
     * RegularExpression Id.
     */
    int INTEGER_O = 12;
    /**
     * RegularExpression Id.
     */
    int INTEGER_B = 13;
    /**
     * RegularExpression Id.
     */
    int LONG_H = 14;
    /**
     * RegularExpression Id.
     */
    int LONG_O = 15;
    /**
     * RegularExpression Id.
     */
    int LONG_B = 16;
    /**
     * RegularExpression Id.
     */
    int SHORT_H = 17;
    /**
     * RegularExpression Id.
     */
    int SHORT_O = 18;
    /**
     * RegularExpression Id.
     */
    int SHORT_B = 19;
    /**
     * RegularExpression Id.
     */
    int BYTE_H = 20;
    /**
     * RegularExpression Id.
     */
    int BYTE_O = 21;
    /**
     * RegularExpression Id.
     */
    int BYTE_B = 22;
    /**
     * RegularExpression Id.
     */
    int FLOAT = 23;
    /**
     * RegularExpression Id.
     */
    int DOUBLE = 24;
    /**
     * RegularExpression Id.
     */
    int EXPONENT = 25;
    /**
     * RegularExpression Id.
     */
    int STRING = 26;
    /**
     * RegularExpression Id.
     */
    int CHARACTER = 27;
    /**
     * RegularExpression Id.
     */
    int TRUE = 28;
    /**
     * RegularExpression Id.
     */
    int FALSE = 29;
    /**
     * RegularExpression Id.
     */
    int NULL = 30;
    /**
     * RegularExpression Id.
     */
    int LPAREN = 31;
    /**
     * RegularExpression Id.
     */
    int RPAREN = 32;
    /**
     * RegularExpression Id.
     */
    int LBRACK = 33;
    /**
     * RegularExpression Id.
     */
    int RBRACK = 34;
    /**
     * RegularExpression Id.
     */
    int COLON = 35;
    /**
     * RegularExpression Id.
     */
    int COMMA = 36;
    /**
     * RegularExpression Id.
     */
    int AT = 37;
    /**
     * RegularExpression Id.
     */
    int CHARSTREAM_START = 38;
    /**
     * RegularExpression Id.
     */
    int NAN = 39;
    /**
     * RegularExpression Id.
     */
    int POS_INF = 40;
    /**
     * RegularExpression Id.
     */
    int NEG_INF = 41;
    /**
     * RegularExpression Id.
     */
    int POS_BOUND = 42;
    /**
     * RegularExpression Id.
     */
    int NEG_BOUND = 43;
    /**
     * RegularExpression Id.
     */
    int COMMENT = 44;
    /**
     * RegularExpression Id.
     */
    int NAME = 45;
    /**
     * RegularExpression Id.
     */
    int ALIAS = 46;
    /**
     * RegularExpression Id.
     */
    int LETTER = 47;
    /**
     * RegularExpression Id.
     */
    int DIGIT = 48;
    /**
     * RegularExpression Id.
     */
    int CHARSTREAM_END = 49;
    /**
     * RegularExpression Id.
     */
    int CHARSTREAM_PART = 50;

    /**
     * Lexical state.
     */
    int DEFAULT = 0;
    /**
     * Lexical state.
     */
    int IN_CHARSTREAM = 1;
    int BINARYSTREAM_START = 150;
    int BINARYSTREAM_PART = 151;
    int BINARYSTREAM_END = 152;

    /**
     * Literal token values.
     */
    String[] tokenImage = {
            "<EOF>",
            "<LOCAL_DATETIME>",
            "<LOCAL_DATE>",
            "<LOCAL_TIME>",
            "<REGEX>",
            "\"{\"",
            "\"}\"",
            "<SHORT>",
            "<BYTE>",
            "<LONG>",
            "<INTEGER>",
            "<INTEGER_H>",
            "<INTEGER_O>",
            "<INTEGER_B>",
            "<LONG_H>",
            "<LONG_O>",
            "<LONG_B>",
            "<SHORT_H>",
            "<SHORT_O>",
            "<SHORT_B>",
            "<BYTE_H>",
            "<BYTE_O>",
            "<BYTE_B>",
            "<FLOAT>",
            "<DOUBLE>",
            "<EXPONENT>",
            "<STRING>",
            "<CHARACTER>",
            "\"true\"",
            "\"false\"",
            "\"null\"",
            "\"(\"",
            "\")\"",
            "\"[\"",
            "\"]\"",
            "\":\"",
            "\",\"",
            "\"@\"",
            "<CHARSTREAM_START>",
            "\"NaN\"",
            "\"+Inf\"",
            "\"-Inf\"",
            "\"+Bound\"",
            "\"-Bound\"",
            "<COMMENT>",
            "<NAME>",
            "<ALIAS>",
            "<LETTER>",
            "<DIGIT>",
            "\"]\"",
            "<CHARSTREAM_PART>",
            "\" \"",
            "\"\\t\"",
            "\"\\n\"",
            "\"\\r\"",
    };

}
