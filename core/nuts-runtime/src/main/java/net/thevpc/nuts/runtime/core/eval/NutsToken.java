package net.thevpc.nuts.runtime.core.eval;

import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

public class NutsToken {

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
    public static final int TT_STRING_LITERAL = -10;
    public static final int TT_SPACE = -11;

    public int ttype;
    public int lineno;
    public String sval;
    public double nval;

    public NutsToken(int ttype, String sval, double nval, int lineno) {
        this.ttype = ttype;
        this.sval = sval;
        this.nval = nval;
        this.lineno = lineno;
    }

    @Override
    public String toString() {
        if(ttype>=32){
            return "NutsToken{" +
                    "ttype='" + (char)ttype +"'"+
                    ", lineno=" + lineno +
                    ", sval=" + (sval==null?"null": CoreStringUtils.simpleQuote(sval)) +
                    ", nval=" + nval +
                    '}';
        }
        return "NutsToken{" +
                "ttype=" + ttype +
                ", lineno=" + lineno +
                ", sval=" + (sval==null?"null": CoreStringUtils.simpleQuote(sval)) +
                ", nval=" + nval +
                '}';
    }
}
