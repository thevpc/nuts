package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsStringUtils;

public class NutsToken {

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
    public static final int TT_AND = -40;
    public static final int TT_OR = -41;
    public static final int TT_LEFT_SHIFT = -42;
    public static final int TT_RIGHT_SHIFT = -43;
    public static final int TT_LEFT_SHIFT_UNSIGNED = -44;
    public static final int TT_RIGHT_SHIFT_UNSIGNED = -45;
    public static final int TT_LTE = -46;
    public static final int TT_GTE = -47;
    public static final int TT_LTGT = -48;
    public static final int TT_EQ2 = -49;
    public static final int TT_NEQ = -50;
    public static final int TT_NEQ2 = -51;
    public static final int TT_RIGHT_ARROW = -52;
    public static final int TT_RIGHT_ARROW2 = -53;
    public static final int TT_COALESCE = -54;
    public static final int TT_DOTS2 = -55;
    public static final int TT_DOTS3 = -56;
    public static final int TT_EQ3 = -57;
    public static final int TT_NOT3 = -58;
    public static final int TT_NOT2 = -59;
    public static final int TT_NOT_LIKE = -60;
    public static final int TT_LIKE2 = -62;
    public static final int TT_LIKE3 = -63;


    public static final int TT_EQ = '=';
    public static final int TT_NOT = '!';
    public static final int TT_LT = '<';
    public static final int TT_GT = '>';
    public static final int TT_LIKE = '~';
    public static final int TT_AMP = '&';
    public static final int TT_PIPE = '|';
    public static final int TT_OPEN_BRACKET = '[';
    public static final int TT_CLOSE_BRACKET = ']';
    public static final int TT_OPEN_BRACE = '{';
    public static final int TT_CLOSE_BRACE = '}';
    public static final int TT_OPEN_PAR = '(';
    public static final int TT_CLOSE_PAR = ')';

    public int ttype;
    public int lineno;
    public String sval;
    public Number nval;

    public NutsToken(int ttype, String sval, Number nval, int lineno) {
        this.ttype = ttype;
        this.sval = sval;
        this.nval = nval;
        this.lineno = lineno;
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
            case TT_AND:
                return "AND";
            case TT_OR:
                return "OR";
            case TT_LEFT_SHIFT:
                return "LEFT_SHIFT";
            case TT_RIGHT_SHIFT:
                return "RIGHT_SHIFT";
            case TT_LEFT_SHIFT_UNSIGNED:
                return "LEFT_SHIFT_UNSIGNED";
            case TT_RIGHT_SHIFT_UNSIGNED:
                return "RIGHT_SHIFT_UNSIGNED";
            case TT_LTE:
                return "LTE";
            case TT_GTE:
                return "GTE";
            case TT_LTGT:
                return "LTGT";
            case TT_EQ2:
                return "EQ2";
            case TT_NEQ:
                return "NEQ";
            case TT_NEQ2:
                return "NEQ2";
            case TT_RIGHT_ARROW:
                return "RIGHT_ARROW";
            case TT_RIGHT_ARROW2:
                return "RIGHT_ARROW2";
            case TT_COALESCE:
                return "COALESCE";
            case TT_DOTS2:
                return "DOTS2";
            case TT_DOTS3:
                return "DOTS3";
            case TT_EQ3:
                return "EQ3";
            case TT_NOT3:
                return "NOT3";
            case TT_NOT2:
                return "NOT2";
            case TT_NOT_LIKE:
                return "NOT_LIKE";
            case TT_LIKE2:
                return "LIKE2";
            case TT_LIKE3:
                return "LIKE3";
            case TT_EQ:
                return "EQ";
            case TT_NOT:
                return "NOT";
            case TT_LT:
                return "LT";
            case TT_GT:
                return "GT";
            case TT_LIKE:
                return "LIKE";
            case TT_AMP:
                return "AMP";
            case TT_PIPE:
                return "PIPE";
            case TT_OPEN_BRACKET:
                return "OPEN_BRACKET";
            case TT_CLOSE_BRACKET:
                return "CLOSE_BRACKET";
            case TT_OPEN_BRACE:
                return "OPEN_BRACE";
            case TT_CLOSE_BRACE:
                return "CLOSE_BRACE";
            case TT_OPEN_PAR:
                return "OPEN_PAR";
            case TT_CLOSE_PAR:
                return "CLOSE_PAR";
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
        return "NutsToken{" +
                "ttype=" + typeString(ttype) +
                ", lineno=" + lineno +
                ", sval=" + (sval == null ? "null" : NutsStringUtils.formatStringLiteral(sval, NutsStringUtils.QuoteType.SIMPLE)) +
                ", nval=" + nval +
                '}';
    }
}
