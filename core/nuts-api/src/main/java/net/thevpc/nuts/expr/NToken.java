package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NQuoteType;
import net.thevpc.nuts.util.NStringUtils;

public class NToken {

    /***
     * A constant indicating that the end of the line has been read.
     */
    public static final int TT_EOL = '\n';
    /**
     * A constant indicating that the end of the stream has been read.
     */
    public static final int TT_EOF = -1;
    //NOT USED
    public static final int TT_NUMBER = -2;
    /**
     * A constant indicating that a word token has been read.
     */
    public static final int TT_WORD = -3;

    /* A constant indicating that no token has been read, used for
     * initializing ttype.
     */
    public static final int TT_NOTHING = -4;

    /// //////////////////////////////////////////////////////////////////

    public static final int TT_INT = -10;
    public static final int TT_LONG = -11;
    public static final int TT_BIG_INT = -12;
    public static final int TT_FLOAT = -13;
    public static final int TT_DOUBLE = -14;
    public static final int TT_BIG_DECIMAL = -15;
    public static final int TT_COMMENTS = -16;
    public static final int TT_SPACE = -17;

//    public static final int TT_NOTHING = -25;

    public static final int TT_STRING_LITERAL = -18;


    public static final int TT_AND = -40; // &&
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

    public static final int TT_QUOTE_SINGLE3 = -54;
    public static final int TT_QUOTE_DOUBLE3 = -55;
    public static final int TT_QUOTE_ANTI3 = -56;
    public static final int TT_COMMENT_LINE_C = -57;
    public static final int TT_COMMENT_LINE_SH = -58;
    public static final int TT_COMMENT_MULTILINE_C = -59;
    public static final int TT_COMMENT_MULTILINE_XML = -60;
    public static final int TT_MUL_MUL = -61;
    public static final int TT_PLUS_PLUS = -62;
    public static final int TT_MINUS_MINUS = -63;
    public static final int TT_DIV_DIV = -64;
    public static final int TT_POW_POW = -65;
    public static final int TT_REM_REM = -66;

    public static final int TT_MUL_EQ = -67;
    public static final int TT_PLUS_EQ = -68;
    public static final int TT_MINUS_EQ = -69;
    public static final int TT_DIV_EQ = -70;
    public static final int TT_POW_EQ = -71;
    public static final int TT_REM_EQ = -72;
    public static final int TT_ISTR_DQ = -73;
    public static final int TT_ISTR_SQ = -74;
    public static final int TT_ISTR_AQ = -75;

    public static final int TT_COALESCE = -76;
    public static final int TT_DOTS2 = -77;
    public static final int TT_DOTS3 = -78;
    public static final int TT_EQ3 = -79;
    public static final int TT_NOT3 = -80;
    public static final int TT_NOT2 = -81;
    public static final int TT_NOT_LIKE = -82;
    public static final int TT_LIKE2 = -83;
    public static final int TT_LIKE3 = -84;
    public static final int TT_DOLLAR = -85;
    public static final int TT_DOLLAR_BRACE = -86;
    public static final int TT_VAR = -87;

    public static final int TT_DEFAULT = Integer.MIN_VALUE;


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
    public String image;
    public String ttypeString;
    public Number nval;

    public static NToken ofSpecial(int ttype, String sval,int lineno) {
        String ttypeString;
        switch (ttype){
            case '\t': {
                ttypeString="'\\t'";
                break;
            }
            case '\f': {
                ttypeString = "'\\f'";
                break;
            }
            default: {
                if (ttype >= 32) {
                    ttypeString= "'" + (char) ttype + "'";
                }else {
                    ttypeString= String.valueOf(ttype);
                }
            }
        }
        return new NToken(ttype, sval, 0, lineno, sval, ttypeString);
    }
    public static NToken ofChar(char ttype, int lineno) {
        String sval = String.valueOf((char) ttype);
        return new NToken(ttype, sval, 0, lineno, sval, "'" + sval + "'");
    }
    public static NToken ofStr(int ttype, String sval,String ttypeString,int lineno) {
        return new NToken(ttype, sval, 0, lineno, sval, ttypeString);
    }

    public static NToken of(int ttype, String sval, Number nval, int lineno, String image, String ttypeString) {
        return new NToken(ttype, sval, nval, lineno, image, ttypeString);
    }

    public NToken(int ttype, String sval, Number nval, int lineno, String image, String ttypeString) {
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
            case TT_DOLLAR:
                return "TT_DOLLAR";
            case TT_DOLLAR_BRACE:
                return "DOLLAR_BRACE";
            case TT_VAR:
                return "VAR";
            case TT_COMMENTS:
                return "COMMENTS";
            case TT_DIV_EQ:
                return "DIV_EQ";
            case TT_DEFAULT:
                return "TT_DEFAULT";
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
        String ts=ttypeString==null?typeString(ttype):ttypeString;
        return "NToken{" +
                "ttype=" + typeString(ttype) +
                ", lineno=" + lineno +
                ", sval=" + (sval == null ? "null" : NStringUtils.formatStringLiteral(sval, NQuoteType.SIMPLE)) +
                ", nval=" + nval +
                '}';
    }
}
