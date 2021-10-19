package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

public class Token {

    /**
     * A constant indicating that the end of the stream has been read.
     */
    public static final int TT_EOF = -1;

    /**
     * A constant indicating that the end of the line has been read.
     */
    public static final int TT_EOL = '\n';

    /**
     * A constant indicating that a number token has been read.
     */
    public static final int TT_NUMBER = -2;

    /**
     * A constant indicating that a word token has been read.
     */
    public static final int TT_WORD = -3;

    /* A constant indicating that no token has been read, used for
     * initializing ttype.  FIXME This could be made public and
     * made available as the part of the API in a future release.
     */
    public static final int TT_NOTHING = -4;
    public static final int TT_AND = -5;
    public static final int TT_OR = -6;
    public static final int TT_NOT = -7;
    public static final int TT_OPEN_PAR = -8;
    public static final int TT_CLOSE_PAR = -9;
    public static final int TT_STRING_LITERAL = -10;
    public static final int TT_SPACE = -11;
    public static final int TT_COMMA = -12;

    int ttype;
    int lineno;
    String sval;
    double nval;

    public Token(int ttype, String sval, double nval, int lineno) {
        this.ttype = ttype;
        this.sval = sval;
        this.nval = nval;
        this.lineno = lineno;
    }

    @Override
    public String toString() {
        if(ttype>=32){
            return "Token{" +
                    "ttype='" + (char)ttype +"'"+
                    ", lineno=" + lineno +
                    ", sval='" + sval + '\'' +
                    ", nval=" + nval +
                    '}';
        }
        return "Token{" +
                "ttype=" + ttype +
                ", lineno=" + lineno +
                ", sval='" + sval + '\'' +
                ", nval=" + nval +
                '}';
    }
}
