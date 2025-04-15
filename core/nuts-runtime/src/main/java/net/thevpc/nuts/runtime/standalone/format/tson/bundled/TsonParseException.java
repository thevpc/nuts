package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public class TsonParseException extends RuntimeException {
    public String currentToken;

    public int[][] expectedTokenSequences;

    public String[] tokenImage;

    /**
     * An integer that describes the kind of this token.  This numbering
     * system is determined by JavaCCParser, and a table of these numbers is
     * stored in the file ...Constants.java.
     */
    public int currentTokenKind;

    /**
     * The line number of the first character of this Token.
     */
    public int currentTokenBeginLine;
    /**
     * The column number of the first character of this Token.
     */
    public int currentTokenBeginColumn;
    /**
     * The line number of the last character of this Token.
     */
    public int currentTokenEndLine;
    /**
     * The column number of the last character of this Token.
     */
    public int currentTokenEndColumn;

    /**
     * The string image of the token.
     */
    public String currentTokenImage;
    public Object source;


    public TsonParseException(String message,String currentToken, int[][] expectedTokenSequences, String[] tokenImage, int currentTokenKind, int currentTokenBeginLine, int currentTokenBeginColumn, int currentTokenEndLine, int currentTokenEndColumn, String currentTokenImage, Object source) {
        super(message);
        this.currentToken = currentToken;
        this.expectedTokenSequences = expectedTokenSequences;
        this.tokenImage = tokenImage;
        this.currentTokenKind = currentTokenKind;
        this.currentTokenBeginLine = currentTokenBeginLine;
        this.currentTokenBeginColumn = currentTokenBeginColumn;
        this.currentTokenEndLine = currentTokenEndLine;
        this.currentTokenEndColumn = currentTokenEndColumn;
        this.currentTokenImage = currentTokenImage;
        this.source = source;
    }

    public TsonParseException(String message, Object source) {
        super(message);
        this.source = source;
    }

    public TsonParseException(Throwable cause, Object source) {
        super("[" + source + "] " + cause.toString(), cause);
    }
}
