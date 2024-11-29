package net.thevpc.nuts.boot.reserved.util;

public class NBootToken {

    /**
     * A constant indicating that the end of the stream has been read.
     */
    public static final int TT_EOF = -1;

    /**
     * A constant indicating that the end of the line has been read.
     */
    public static final int TT_EOL = '\n';

    public static final int TT_INT = -4;
    public static final int TT_LONG = -5;
    public static final int TT_BIG_INT = -6;
    public static final int TT_FLOAT = -7;
    public static final int TT_DOUBLE = -8;
    public static final int TT_BIG_DECIMAL = -9;

    /**
     * A constant indicating that a word token has been read.
     */
    public static final int TT_WORD = -3;

    /* A constant indicating that no token has been read, used for
     * initializing ttype.  FIXME This could be made public and
     * made available as the part of the API in a future release.
     */
    public static final int TT_NOTHING = -30;
    public static final int TT_STRING_LITERAL = -10;
    public static final int TT_SPACE = -11;
    public static final int TT_DOLLAR = -64;
    public static final int TT_DOLLAR_BRACE = -65;
    public static final int TT_VAR = -66;
    public static final int TT_DEFAULT = Integer.MIN_VALUE;

    public int ttype;
    public int lineno;
    public String sval;
    public String image;
    public String ttypeString;
    public Number nval;

    public static NBootToken of(int ttype, String sval, Number nval, int lineno, String image, String ttypeString) {
        return new NBootToken(ttype, sval, nval, lineno, image, ttypeString);
    }

    public NBootToken(int ttype, String sval, Number nval, int lineno, String image, String ttypeString) {
        this.ttype = ttype;
        this.sval = sval;
        this.nval = nval;
        this.lineno = lineno;
        this.image = image;
        this.ttypeString = ttypeString;
    }


    public static String typeString(int ttype) {
        switch (ttype) {
            case TT_EOF:
                return "EOF";
            case TT_EOL:
                return "EOL";
            case TT_INT:
                return "INT";
            case TT_LONG:
                return "LONG";
            case TT_BIG_INT:
                return "BIG_INT";
            case TT_FLOAT:
                return "FLOAT";
            case TT_DOUBLE:
                return "DOUBLE";
            case TT_BIG_DECIMAL:
                return "BIG_DECIMAL";
            case TT_WORD:
                return "WORD";
            case TT_NOTHING:
                return "NOTHING";
            case TT_STRING_LITERAL:
                return "STRING_LITERAL";
            case TT_SPACE:
                return "SPACE";
            case TT_DOLLAR:
                return "TT_DOLLAR";
            case TT_DOLLAR_BRACE:
                return "TT_DOLLAR_BRACE";
            case TT_VAR:
                return "TT_VAR";
            case '\t':
                return "'\\t'";
            case '\f':
                return "'\\f'";
        }
        if (ttype >= 32) {
            return "'" + (char) ttype + "'";
        }
        return String.valueOf(ttype);
    }

    public String toString() {
        return "NBootToken{" +
                "ttype=" + typeString(ttype) +
                ", lineno=" + lineno +
                ", sval=" + (sval == null ? "null" : NBootStringUtils.formatStringLiteral(sval, NBootQuoteTypeBoot.SIMPLE)) +
                ", nval=" + nval +
                '}';
    }
}
