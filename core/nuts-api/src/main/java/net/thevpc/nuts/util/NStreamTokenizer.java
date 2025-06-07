package net.thevpc.nuts.util;

import net.thevpc.nuts.io.NIOException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * The {@code StreamTokenizer} class takes an input stream and
 * parses it into "tokens", allowing the tokens to be
 * read one at a time. The parsing process is controlled by a table
 * and a number of flags that can be set to various states. The
 * stream tokenizer can recognize identifiers, numbers, quoted
 * strings, and various comment styles.
 * <p>
 * Each byte read from the input stream is regarded as a character
 * in the range {@code '\u005Cu0000'} through {@code '\u005Cu00FF'}.
 * The character value is used to look up five possible attributes of
 * the character: <i>white space</i>, <i>alphabetic</i>,
 * <i>numeric</i>, <i>string quote</i>, and <i>comment character</i>.
 * Each character can have zero or more of these attributes.
 * <p>
 * In addition, an instance has four flags. These flags indicate:
 * <ul>
 * <li>Whether line terminators are to be returned as tokens or treated
 *     as white space that merely separates tokens.
 * <li>Whether C-style comments are to be recognized and skipped.
 * <li>Whether C++-style comments are to be recognized and skipped.
 * <li>Whether the characters of identifiers are converted to lowercase.
 * </ul>
 * <p>
 * A typical application first constructs an instance of this class,
 * sets up the syntax tables, and then repeatedly loops calling the
 * {@code nextToken} method in each iteration of the loop until
 * it returns the value {@code TT_EOF}.
 *
 * @author James Gosling
 * @since JDK1.0
 */

public class NStreamTokenizer {



    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;

    private final StringBuilder bufImage = new StringBuilder();
    private final byte[] commonCharTypes = new byte[256];
    private final boolean[] parsableTokenTypes = new boolean[256];

    /**
     * After a call to the {@code nextToken} method, this field
     * contains the type of the token just read. For a single character
     * token, its value is the single character, converted to an integer.
     * For a quoted string token, its value is the quote character.
     * Otherwise, its value is one of the following:
     * <ul>
     * <li>{@code TT_WORD} indicates that the token is a word.
     * <li>{@code TT_NUMBER} indicates that the token is a number.
     * <li>{@code TT_EOL} indicates that the end of line has been read.
     *     The field can only have this value if the
     *     {@code eolIsSignificant} method has been called with the
     *     argument {@code true}.
     * <li>{@code TT_EOF} indicates that the end of the input stream
     *     has been reached.
     * </ul>
     * <p>
     * The initial value of this field is -4.
     */
    public int ttype = NToken.TT_NOTHING;
    /**
     * If the current token is a word token, this field contains a
     * string giving the characters of the word token. When the current
     * token is a quoted string token, this field contains the body of
     * the string.
     * <p>
     * The current token is a word when the value of the
     * {@code ttype} field is {@code TT_WORD}. The current token is
     * a quoted string token when the value of the {@code ttype} field is
     * a quote character.
     * <p>
     * The initial value of this field is null.
     */
    public String sval;
    public String image;
    /**
     * If the current token is a number, this field contains the value
     * of that number. The current token is a number when the value of
     * the {@code ttype} field is {@code TT_NUMBER}.
     * <p>
     * The initial value of this field is 0.0.
     */
    public Number nval;
    //    public double dval;
//    public long ival;
    public boolean returnComments = true;
    public boolean returnSpaces = true;
    /* Only one of these will be non-null */
    private Reader reader = null;
    //    private InputStream input = null;
    private char[] buf = new char[20];
    /**
     * The next character to be considered by the nextToken method.  May also
     * be NEED_CHAR to indicate that a new character should be read, or SKIP_LF
     * to indicate that a new character should be read and, if It's a '\n'
     * character, it should be discarded and a second new character should be
     * read.
     */
    private int peekc = NEED_CHAR;
    private int c;
    private int ctype;
    private boolean pushedBack;
    private boolean forceLower;
    /**
     * The line number of the last token read
     */
    private int LINENO = 1;
    private boolean eolIsSignificantP = false;
    //    private boolean slashSlashCommentsP = false;
//    private boolean slashStarCommentsP = false;
//    private boolean xmlCommentsP = false;

    /**
     * Private constructor that initializes everything except the streams.
     */
    private NStreamTokenizer() {
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars(128 + 32, 255);
        whitespaceChars(0, ' ');
//        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        quoteChar('`');
        parseNumbers(true);
    }


    /**
     * Create a tokenizer that parses the given character stream.
     *
     * @param r a Reader object providing the input stream.
     * @since JDK1.1
     */
    public NStreamTokenizer(Reader r) {
        this();
        reader = r == null ? new StringReader("") : r;
    }

    public NStreamTokenizer(String r) {
        this(new StringReader(r == null ? "" : r));
    }

    /**
     * Resets this tokenizer's syntax table so that all characters are
     * "ordinary." See the {@code ordinaryChar} method
     * for more information on a character being ordinary.
     */
    public void resetSyntax() {
        for (int i = commonCharTypes.length; --i >= 0; )
            commonCharTypes[i] = 0;
    }

    /**
     * Specifies that all characters <i>c</i> in the range
     * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
     * are word constituents. A word token consists of a word constituent
     * followed by zero or more word constituents or number constituents.
     *
     * @param low the low end of the range.
     * @param hi  the high end of the range.
     */
    public void wordChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= commonCharTypes.length)
            hi = commonCharTypes.length - 1;
        while (low <= hi)
            commonCharTypes[low++] |= CT_ALPHA;
    }

    public void wordChar(int c) {
        commonCharTypes[c] |= CT_ALPHA;
    }

    /**
     * Specifies that all characters <i>c</i> in the range
     * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
     * are white space characters. White space characters serve only to
     * separate tokens in the input stream.
     *
     * <p>Any other attribute settings for the characters in the specified
     * range are cleared.
     *
     * @param low the low end of the range.
     * @param hi  the high end of the range.
     */
    public void whitespaceChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= commonCharTypes.length)
            hi = commonCharTypes.length - 1;
        while (low <= hi)
            commonCharTypes[low++] = CT_WHITESPACE;
    }

    /**
     * Specifies that all characters <i>c</i> in the range
     * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
     * are "ordinary" in this tokenizer. See the
     * {@code ordinaryChar} method for more information on a
     * character being ordinary.
     *
     * @param low the low end of the range.
     * @param hi  the high end of the range.
     */
    public void ordinaryChars(int low, int hi) {
        if (low < 0)
            low = 0;
        if (hi >= commonCharTypes.length)
            hi = commonCharTypes.length - 1;
        while (low <= hi)
            commonCharTypes[low++] = 0;
    }

    /**
     * Specifies that the character argument is "ordinary"
     * in this tokenizer. It removes any special significance the
     * character has as a comment character, word component, string
     * delimiter, white space, or number character. When such a character
     * is encountered by the parser, the parser treats it as a
     * single-character token and sets {@code ttype} field to the
     * character value.
     *
     * <p>Making a line terminator character "ordinary" may interfere
     * with the ability of a {@code StreamTokenizer} to count
     * lines. The {@code lineno} method may no longer reflect
     * the presence of such terminator characters in its line count.
     *
     * @param ch the character.
     */
    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < commonCharTypes.length)
            commonCharTypes[ch] = 0;
    }

    /**
     * Specified that the character argument starts a single-line
     * comment. All characters from the comment character to the end of
     * the line are ignored by this stream tokenizer.
     *
     * <p>Any other attribute settings for the specified character are cleared.
     *
     * @param ch the character.
     */
    public void commentChar(int ch) {
        if (ch >= 0 && ch < commonCharTypes.length)
            commonCharTypes[ch] = CT_COMMENT;
    }

    /**
     * Specifies that matching pairs of this character delimit string
     * constants in this tokenizer.
     * <p>
     * When the {@code nextToken} method encounters a string
     * constant, the {@code ttype} field is set to the string
     * delimiter and the {@code sval} field is set to the body of
     * the string.
     * <p>
     * If a string quote character is encountered, then a string is
     * recognized, consisting of all characters after (but not including)
     * the string quote character, up to (but not including) the next
     * occurrence of that same string quote character, or a line
     * terminator, or end of file. The usual escape sequences such as
     * {@code "\u005Cn"} and {@code "\u005Ct"} are recognized and
     * converted to single characters as the string is parsed.
     *
     * <p>Any other attribute settings for the specified character are cleared.
     *
     * @param ch the character.
     */
    public void quoteChar(int ch) {
        if (ch >= 0 && ch < commonCharTypes.length) {
            commonCharTypes[ch] = CT_QUOTE;
        }
    }

    /**
     * Specifies that numbers should be parsed by this tokenizer. The
     * syntax table of this tokenizer is modified so that each of the twelve
     * characters:
     * <blockquote><pre>
     *      0 1 2 3 4 5 6 7 8 9 . -
     * </pre></blockquote>
     * <p>
     * has the "numeric" attribute.
     * <p>
     * When the parser encounters a word token that has the format of a
     * double precision floating-point number, it treats the token as a
     * number rather than a word, by setting the {@code ttype}
     * field to the value {@code TT_NUMBER} and putting the numeric
     * value of the token into the {@code nval} field.
     */
    public void parseNumbers(boolean parse) {
        if (parse) {
            for (int i = '0'; i <= '9'; i++) {
                commonCharTypes[i] |= CT_DIGIT;
            }
            commonCharTypes['.'] |= CT_DIGIT;
            commonCharTypes['-'] |= CT_DIGIT;
        } else {
            for (int i = '0'; i <= '9'; i++) {
                commonCharTypes[i] &= ~CT_DIGIT;
            }
            commonCharTypes['.'] &= ~CT_DIGIT;
            commonCharTypes['-'] &= ~CT_DIGIT;
        }
    }

    public boolean isParsable(int tt) {
        return tt <= 0 && tt > -parsableTokenTypes.length && parsableTokenTypes[-tt];
    }

    public void parseOperators(boolean parse) {
        parsableTokenTypes[-NToken.TT_AND] = parse;
        parsableTokenTypes[-NToken.TT_OR] = parse;
        parsableTokenTypes[-NToken.TT_LEFT_SHIFT] = parse;
        parsableTokenTypes[-NToken.TT_RIGHT_SHIFT] = parse;
        parsableTokenTypes[-NToken.TT_LEFT_SHIFT_UNSIGNED] = parse;
        parsableTokenTypes[-NToken.TT_RIGHT_SHIFT_UNSIGNED] = parse;
        parsableTokenTypes[-NToken.TT_LTE] = parse;
        parsableTokenTypes[-NToken.TT_GTE] = parse;
        parsableTokenTypes[-NToken.TT_LTGT] = parse;
        parsableTokenTypes[-NToken.TT_EQ2] = parse;
        parsableTokenTypes[-NToken.TT_EQ3] = parse;
        parsableTokenTypes[-NToken.TT_NEQ] = parse;
        parsableTokenTypes[-NToken.TT_NEQ2] = parse;
        parsableTokenTypes[-NToken.TT_RIGHT_ARROW] = parse;
        parsableTokenTypes[-NToken.TT_PLUS_PLUS] = parse;
        parsableTokenTypes[-NToken.TT_MINUS_MINUS] = parse;
        parsableTokenTypes[-NToken.TT_MUL_MUL] = parse;
        parsableTokenTypes[-NToken.TT_DIV_DIV] = parse;

        parsableTokenTypes[-NToken.TT_POW_POW] = parse;
        parsableTokenTypes[-NToken.TT_REM_REM] = parse;
        parsableTokenTypes[-NToken.TT_MUL_EQ] = parse;
        parsableTokenTypes[-NToken.TT_PLUS_EQ] = parse;
        parsableTokenTypes[-NToken.TT_MINUS_EQ] = parse;
        parsableTokenTypes[-NToken.TT_DIV_EQ] = parse;
        parsableTokenTypes[-NToken.TT_POW_EQ] = parse;
        parsableTokenTypes[-NToken.TT_REM_EQ] = parse;
    }

    public void doNotParseNumbers() {
    }

    public void acceptTokenType(int tt) {
        acceptTokenType(tt, true);
    }

    public void acceptTokenType(int tt, boolean b) {
        if (tt <= 0 && tt > -parsableTokenTypes.length) {
            parsableTokenTypes[-tt] = b;
            return;
        }
        throw new IllegalArgumentException("unsupported");
    }

    /**
     * Determines whether or not ends of line are treated as tokens.
     * If the flag argument is true, this tokenizer treats end of lines
     * as tokens; the {@code nextToken} method returns
     * {@code TT_EOL} and also sets the {@code ttype} field to
     * this value when an end of line is read.
     * <p>
     * A line is a sequence of characters ending with either a
     * carriage-return character ({@code '\u005Cr'}) or a newline
     * character ({@code '\u005Cn'}). In addition, a carriage-return
     * character followed immediately by a newline character is treated
     * as a single end-of-line token.
     * <p>
     * If the {@code flag} is false, end-of-line characters are
     * treated as white space and serve only to separate tokens.
     *
     * @param flag {@code true} indicates that end-of-line characters
     *             are separate tokens; {@code false} indicates that
     *             end-of-line characters are white space.
     */
    public void eolIsSignificant(boolean flag) {
        eolIsSignificantP = flag;
    }

    /**
     * Determines whether or not the tokenizer recognizes C-style comments.
     * If the flag argument is {@code true}, this stream tokenizer
     * recognizes C-style comments. All text between successive
     * occurrences of {@code /*} and <code>*&#47;</code> are discarded.
     * <p>
     * If the flag argument is {@code false}, then C-style comments
     * are not treated specially.
     *
     * @param flag {@code true} indicates to recognize and ignore
     *             C-style comments.
     */
    public void slashStarComments(boolean flag) {
        acceptTokenType(NToken.TT_COMMENT_MULTILINE_C, flag);
    }

    /**
     * Determines whether or not the tokenizer recognizes C++-style comments.
     * If the flag argument is {@code true}, this stream tokenizer
     * recognizes C++-style comments. Any occurrence of two consecutive
     * slash characters ({@code '/'}) is treated as the beginning of
     * a comment that extends to the end of the line.
     * <p>
     * If the flag argument is {@code false}, then C++-style
     * comments are not treated specially.
     *
     * @param flag {@code true} indicates to recognize and ignore
     *             C++-style comments.
     */
    public void slashSlashComments(boolean flag) {
        acceptTokenType(NToken.TT_COMMENT_LINE_C, flag);
    }

    public NStreamTokenizer xmlComments(boolean flag) {
        acceptTokenType(NToken.TT_COMMENT_MULTILINE_XML, flag);
        slashSlashComments(false);
        slashStarComments(false);
        return this;
    }

    public NStreamTokenizer javaComments() {
        commentChar('/');
        slashSlashComments(true);
        slashStarComments(true);
        return this;
    }

    public NStreamTokenizer pythonComments() {
        acceptTokenType(NToken.TT_COMMENT_LINE_SH, true);
        commentChar('#');
        slashSlashComments(false);
        slashStarComments(false);
        return this;
    }

    /**
     * Determines whether or not word token are automatically lowercased.
     * If the flag argument is {@code true}, then the value in the
     * {@code sval} field is lowercased whenever a word token is
     * returned (the {@code ttype} field has the
     * value {@code TT_WORD} by the {@code nextToken} method
     * of this tokenizer.
     * <p>
     * If the flag argument is {@code false}, then the
     * {@code sval} field is not modified.
     *
     * @param fl {@code true} indicates that all word tokens should
     *           be lowercased.
     */
    public void lowerCaseMode(boolean fl) {
        forceLower = fl;
    }

    /**
     * Read the next character
     */
    private int readChar() {
        try {
            return reader.read();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private void markChar(int count) {
        try {
            reader.mark(count);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private void resetChar() {
        try {
            reader.reset();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public boolean hasNext() {
        if (pushedBack) {
            return true;
        }
        markChar(1);
        c = readChar();
        if (c < 0) {
            return false;
        }
        resetChar();
        return true;
    }

    /**
     * Parses the next token from the input stream of this tokenizer.
     * The type of the next token is returned in the {@code ttype}
     * field. Additional information about the token may be in the
     * {@code nval} field or the {@code sval} field of this
     * tokenizer.
     * <p>
     * Typical clients of this
     * class first set up the syntax tables and then sit in a loop
     * calling nextToken to parse successive tokens until TT_EOF
     * is returned.
     *
     * @return the value of the {@code ttype} field.
     */
    public int nextToken() {
        if (pushedBack) {
            pushedBack = false;
            return ttype;
        }
        byte[] ct = commonCharTypes;
        this.image = null;
        sval = null;

        c = peekc;
        if (c < 0)
            c = NEED_CHAR;
        if (c == SKIP_LF) {
            c = readChar();
            if (c < 0)
                return ttype = NToken.TT_EOF;
            if (c == '\n')
                c = NEED_CHAR;
        }
        if (c == NEED_CHAR) {
            c = readChar();
            if (c < 0)
                return ttype = NToken.TT_EOF;
        }
        ttype = c;              /* Just to be safe */

        /* Set peekc so that the next invocation of nextToken will read
         * another character unless peekc is reset in this invocation
         */
        peekc = NEED_CHAR;

        ctype = c < 256 ? ct[c] : CT_ALPHA;
        bufImage.setLength(0);
        if (_read_spaces()) {
            return ttype;
        }

        if (_read_number()) {
            return ttype;
        }
        if (_read_word()) {
            return ttype;
        }

        if (_read_string()) {
            return ttype;
        }

        if (_read_slashComments()) {
            return ttype;
        }
        if (_read_xmlComments()) {
            return ttype;
        }

        if ((ctype & CT_COMMENT) != 0) {
            StringBuilder sb = new StringBuilder();
            while ((c = readChar()) != '\n' && c != '\r' && c >= 0) {
                sb.append((char) c);
            }
            peekc = c;
            return nextToken();
        }
        switch (c) {
            case '&': {
                if (isParsable(NToken.TT_AND)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '&') {
                        image = "&&";
                        return ttype = NToken.TT_AND;
                    } else {
                        resetChar();
                    }
                }
                break;
            }
            case '|': {
                if (isParsable(NToken.TT_OR)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '|') {
                        image = "||";
                        return ttype = NToken.TT_OR;
                    } else {
                        resetChar();
                    }
                } else {
                    image = String.valueOf((char) c);
                    return ttype = c;
                }
                break;
            }
            case '<': {
                if (isParsable(NToken.TT_LEFT_SHIFT_UNSIGNED) || isParsable(NToken.TT_LEFT_SHIFT) || isParsable(NToken.TT_LTE) || isParsable(NToken.TT_LTGT)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '<' && (isParsable(NToken.TT_LEFT_SHIFT_UNSIGNED) || isParsable(NToken.TT_LEFT_SHIFT))) {
                        markChar(1);
                        int n2 = readChar();
                        if (n2 == '<' && isParsable(NToken.TT_LEFT_SHIFT_UNSIGNED)) {
                            image = "<<<";
                            return ttype = NToken.TT_LEFT_SHIFT_UNSIGNED;
                        } else if (isParsable(NToken.TT_LEFT_SHIFT)) {
                            resetChar();
                            image = "<<";
                            return ttype = NToken.TT_LEFT_SHIFT;
                        } else {
                            resetChar();
                        }
                    } else if (n == '=' && isParsable(NToken.TT_LTE)) {
                        image = "<=";
                        return ttype = NToken.TT_LTE;
                    } else if (n == '>' && isParsable(NToken.TT_LTGT)) {
                        image = "<>";
                        return ttype = NToken.TT_LTGT;
                    } else {
                        resetChar();
                    }
                }
                break;
            }
            case '>': {
                if (isParsable(NToken.TT_RIGHT_SHIFT_UNSIGNED) || isParsable(NToken.TT_RIGHT_SHIFT) || isParsable(NToken.TT_GTE)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '>' && (isParsable(NToken.TT_RIGHT_SHIFT_UNSIGNED) || isParsable(NToken.TT_RIGHT_SHIFT))) {
                        markChar(1);
                        int n2 = readChar();
                        if (n2 == '>' && isParsable(NToken.TT_RIGHT_SHIFT_UNSIGNED)) {
                            image = ">>>";
                            return ttype = NToken.TT_RIGHT_SHIFT_UNSIGNED;
                        } else if (isParsable(NToken.TT_RIGHT_SHIFT)) {
                            resetChar();
                            image = ">>";
                            return ttype = NToken.TT_RIGHT_SHIFT;
                        } else {
                            resetChar();
                        }
                    } else if (n == '=' && isParsable(NToken.TT_GTE)) {
                        image = ">=";
                        return ttype = NToken.TT_GTE;
                    } else {
                        resetChar();
                    }
                }
                break;
            }
            case '=': {
                if (isParsable(NToken.TT_RIGHT_ARROW) || isParsable(NToken.TT_EQ2) || isParsable(NToken.TT_EQ3)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '>' && isParsable(NToken.TT_RIGHT_ARROW)) {
                        resetChar();
                        image = "=>";
                        return ttype = NToken.TT_RIGHT_ARROW;
                    } else if (n == '=' && (isParsable(NToken.TT_EQ2) || isParsable(NToken.TT_EQ3))) {
                        markChar(1);
                        int n2 = readChar();
                        if (n2 == '=' && isParsable(NToken.TT_EQ3)) {
                            image = "===";
                            return ttype = NToken.TT_EQ3;
                        } else if (isParsable(NToken.TT_EQ2)) {
                            image = "==";
                            return ttype = NToken.TT_EQ2;
                        } else {
                            resetChar();
                        }
                    } else {
                        resetChar();
                    }
                }
                break;
            }
            case '!': {
                if (isParsable(NToken.TT_NEQ2) || isParsable(NToken.TT_NEQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_NEQ2)) {
                            markChar(1);
                            int n2 = readChar();
                            if (n2 == '=') {
                                image = "!==";
                                return ttype = NToken.TT_NEQ2;
                            } else {
                                resetChar();
                                image = "!=";
                                return ttype = NToken.TT_NEQ;
                            }
                        } else {
                            image = "!=";
                            return ttype = NToken.TT_NEQ;
                        }
                    } else {
                        resetChar();
                    }
                }
                break;
            }
            case '+': {
                if (isParsable(NToken.TT_PLUS_PLUS) || isParsable(NToken.TT_PLUS_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '+') {
                        if (isParsable(NToken.TT_PLUS_PLUS)) {
                            image = "++";
                            return ttype = NToken.TT_PLUS_PLUS;
                        } else {
                            image = "+";
                            return ttype = '+';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_PLUS_PLUS)) {
                            image = "+=";
                            return ttype = NToken.TT_PLUS_PLUS;
                        } else {
                            image = "+";
                            return ttype = '+';
                        }
                    } else {
                        image = "+";
                        resetChar();
                    }
                }
                break;
            }
            case '-': {
                if (isParsable(NToken.TT_MINUS_MINUS) || isParsable(NToken.TT_MINUS_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '-') {
                        if (isParsable(NToken.TT_MINUS_MINUS)) {
                            image = "--";
                            return ttype = NToken.TT_MINUS_MINUS;
                        } else {
                            image = "-";
                            return ttype = '-';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_MINUS_EQ)) {
                            image = "-=";
                            return ttype = NToken.TT_MINUS_EQ;
                        } else {
                            image = "-";
                            return ttype = '-';
                        }
                    } else {
                        image = "-";
                        resetChar();
                    }
                }
                break;
            }
            case '*': {
                if (isParsable(NToken.TT_MUL_MUL) || isParsable(NToken.TT_MUL_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '*') {
                        if (isParsable(NToken.TT_MUL_MUL)) {
                            image = "**";
                            return ttype = NToken.TT_MUL_MUL;
                        } else {
                            image = "*";
                            return ttype = '*';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_MUL_EQ)) {
                            image = "*=";
                            return ttype = NToken.TT_MUL_EQ;
                        } else {
                            image = "*";
                            return ttype = '*';
                        }
                    } else {
                        image = "*";
                        resetChar();
                    }
                }
                break;
            }
            case '/': {
                if (isParsable(NToken.TT_DIV_DIV) || isParsable(NToken.TT_DIV_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '/') {
                        if (isParsable(NToken.TT_DIV_DIV)) {
                            image = "//";
                            return ttype = NToken.TT_DIV_DIV;
                        } else {
                            image = "/";
                            return ttype = '/';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_DIV_EQ)) {
                            image = "/=";
                            return ttype = NToken.TT_DIV_EQ;
                        } else {
                            image = "/";
                            return ttype = '/';
                        }
                    } else {
                        image = "/";
                        resetChar();
                    }
                }
                break;
            }
            case '^': {
                if (isParsable(NToken.TT_POW_POW) || isParsable(NToken.TT_POW_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '^') {
                        if (isParsable(NToken.TT_POW_POW)) {
                            image = "^^";
                            return ttype = NToken.TT_POW_POW;
                        } else {
                            image = "^";
                            return ttype = '^';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_POW_EQ)) {
                            image = "^=";
                            return ttype = NToken.TT_POW_EQ;
                        } else {
                            image = "^";
                            return ttype = '^';
                        }
                    } else {
                        image = "^";
                        resetChar();
                    }
                }
                break;
            }
            case '%': {
                if (isParsable(NToken.TT_REM_REM) || isParsable(NToken.TT_REM_EQ)) {
                    markChar(1);
                    int n = readChar();
                    if (n < 0) {
                        //EOF, this is okkay
                    } else if (n == '%') {
                        if (isParsable(NToken.TT_REM_REM)) {
                            image = "%%";
                            return ttype = NToken.TT_REM_REM;
                        } else {
                            image = "%";
                            return ttype = '%';
                        }
                    } else if (n == '=') {
                        if (isParsable(NToken.TT_REM_EQ)) {
                            image = "%=";
                            return ttype = NToken.TT_REM_EQ;
                        } else {
                            image = "%";
                            return ttype = '%';
                        }
                    } else {
                        image = "%";
                        resetChar();
                    }
                }
                break;
            }
        }
        image = String.valueOf((char) c);
        return ttype = c;
    }

    private boolean _read_word() {
        if ((ctype & CT_ALPHA) != 0) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char) c;
                c = readChar();
                ctype = c < 0 ? CT_WHITESPACE : c < 256 ? commonCharTypes[c] : CT_ALPHA;
            } while ((ctype & (CT_ALPHA | CT_DIGIT)) != 0);
            peekc = c;
            sval = String.copyValueOf(buf, 0, i);
            image = sval;
            if (forceLower)
                sval = sval.toLowerCase();
            ttype = NToken.TT_WORD;
            return true;
        }
        return false;
    }

    private boolean _read_number() {
        if ((ctype & CT_DIGIT) != 0) {
            StringBuilder image = new StringBuilder();
            image.append((char) c);
            boolean neg = false;
            boolean intType = true;
            if (c == '-') {
                c = readChar();
                image.append((char) c);
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;
                    ttype = '-';
                    return true;
                }
                neg = true;
            }
            double dv = 0;
            long iv = 0;
            int decexp = 0;
            int seendot = 0;
            boolean loop = true;
            if (c == '.' && seendot == 0) {
                seendot = 1;
                intType = false;
            } else if ('0' <= c && c <= '9') {
                dv = dv * 10 + (c - '0');
                iv = iv * 10 + (c - '0');
                decexp += seendot;
            } else {
                loop = false;
            }
            if (loop) {
                c = readChar();
                while (true) {
                    if (c == '.' && seendot == 0) {
                        seendot = 1;
                        intType = false;
                        image.append((char) c);
                    } else if ('0' <= c && c <= '9') {
                        dv = dv * 10 + (c - '0');
                        iv = iv * 10 + (c - '0');
                        decexp += seendot;
                        image.append((char) c);
                    } else {
                        break;
                    }
                    c = readChar();
                }
            }
            peekc = c;
//            if (decexp != 0) {
//                double denom = 10;
//                decexp--;
//                while (decexp > 0) {
//                    denom *= 10;
//                    decexp--;
//                }
//                /* Do one division of a likely-to-be-more-accurate number */
//                dv = dv / denom;
//            }
//            dval = neg ? -dv : dv;
            this.image = image.toString();
            if (intType) {
                try {
                    nval = Integer.parseInt(image.toString());
                    ttype = NToken.TT_INT;
                } catch (Exception ex) {
                    try {
                        nval = Long.parseLong(image.toString());
                        ttype = NToken.TT_LONG;
                    } catch (Exception ex2) {
                        nval = new BigInteger(image.toString());
                        ttype = NToken.TT_BIG_INT;
                    }
                }
//                ival=neg ? -iv : iv;
                return true;
            } else {
                try {
                    nval = Float.parseFloat(image.toString());
                    ttype = NToken.TT_FLOAT;
                } catch (Exception ex) {
                    try {
                        nval = Double.parseDouble(image.toString());
                        ttype = NToken.TT_DOUBLE;
                    } catch (Exception ex2) {
                        nval = new BigDecimal(image.toString());
                        ttype = NToken.TT_BIG_DECIMAL;
                    }
                }
//                ival=neg ? -iv : iv;
            }
            return true;
        }
        return false;
    }

    private boolean _read_spaces() {
        while ((ctype & CT_WHITESPACE) != 0) {
            bufImage.append((char) c);
            if (c == '\r') {
                LINENO++;
                if (eolIsSignificantP) {
                    peekc = SKIP_LF;
                    ttype = NToken.TT_EOL;
                    return true;
                }
                c = readChar();
                if (c == '\n') {
                    bufImage.append((char) c);
                    c = readChar();
                }
            } else {
                if (c == '\n') {
                    LINENO++;
                    if (eolIsSignificantP) {
                        ttype = NToken.TT_EOL;
                        return true;
                    }
                }
                c = readChar();
            }
            if (bufImage.length() > 0 && returnSpaces) {
                peekc = c;
                image = bufImage.toString();
                ttype = NToken.TT_SPACE;
                return true;
            }
            if (c < 0) {
                ttype = NToken.TT_EOF;
                return true;
            }
            ctype = c < 256 ? commonCharTypes[c] : CT_ALPHA;
        }
        return false;
    }

    private boolean _read_xmlComments() {
        if (c == '<' && isParsable(NToken.TT_COMMENT_MULTILINE_XML)) {
            StringBuilder sb = new StringBuilder();
            sb.append((char) c);
            markChar(4);
            int a = readChar();
            if (a == '!') {
                sb.append((char) c);
                a = readChar();
                if (a == '-') {
                    sb.append((char) c);
                    a = readChar();
                    if (a == '-') {
                        sb.append((char) c);
                        while (true) {
                            c = readChar();
                            boolean wasEnd = false;
                            if (c == '-') {
                                sb.append((char) c);
                                a = readChar();
                                if (a == '-') {
                                    a = readChar();
                                    if (a == '>') {
                                        wasEnd = true;
                                    }
                                }
                                if (wasEnd) {
                                    sb.append("->");
                                    break;
                                } else {
                                    resetChar();
                                }
                            } else {
                                sb.append((char) c);
                            }
                        }
                        if (returnComments) {
                            image = sb.toString();
                            ttype = NToken.TT_COMMENTS;
                            return true;
                        } else {
                            nextToken();
                            return true;
                        }
                    }
                }
            }
            resetChar();
        }
        return false;
    }

    private boolean _read_slashComments() {
        boolean slashStarCommentsP = isParsable(NToken.TT_COMMENT_MULTILINE_C);
        boolean slashSlashCommentsP = isParsable(NToken.TT_COMMENT_LINE_C);
        if (c == '/' && (slashSlashCommentsP || slashStarCommentsP)) {
            StringBuilder sb = new StringBuilder();
            sb.append((char) c);
            c = readChar();
            if (c == '*' && slashStarCommentsP) {
                sb.append((char) c);
                int prevc = 0;
                while ((c = readChar()) != '/' || prevc != '*') {
                    if (c == '\r') {
                        sb.append((char) c);
                        LINENO++;
                        c = readChar();
                        if (c == '\n') {
                            sb.append((char) c);
                            c = readChar();
                        }
                    } else {
                        if (c == '\n') {
                            LINENO++;
                            sb.append((char) c);
                            c = readChar();
                        }
                    }
                    if (c < 0) {
                        ttype = NToken.TT_EOF;
                        return true;
                    }
                    prevc = c;
                }
                if (returnComments) {
                    image = sb.toString();
                    ttype = NToken.TT_COMMENTS;
                    return true;
                }
                ttype = nextToken();
                return true;
            } else if (c == '/' && slashSlashCommentsP) {
                sb.append((char) c);
                while ((c = readChar()) != '\n' && c != '\r' && c >= 0) {
                    sb.append((char) c);
                }
                peekc = c;
                if (returnComments) {
                    image = sb.toString();
                    ttype = NToken.TT_COMMENTS;
                    return true;
                }
                ctype = nextToken();
                return true;
            } else {
                /* Now see if It's still a single line comment */
                if ((commonCharTypes['/'] & CT_COMMENT) != 0) {
                    sb.append((char) c);
                    while ((c = readChar()) != '\n' && c != '\r' && c >= 0) {
                        sb.append((char) c);
                    }
                    if (returnComments) {
                        image = sb.toString();
                        ttype = NToken.TT_COMMENTS;
                        return true;
                    }
                    peekc = c;
                    nextToken();
                    return true;
                } else {
                    peekc = c;
                    ttype = '/';
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    private boolean _read_string() {
        boolean interpolatedString=false;
        if(c=='$'){
            markChar(1);
            c=readChar();
            int ctype2 = c < 256 ? commonCharTypes[c] : CT_ALPHA;
            if ((ctype2 & CT_QUOTE) != 0) {
                ctype=ctype2;
                interpolatedString=true;
            }else{
                resetChar();
                return false;
            }
        }
        if ((ctype & CT_QUOTE) != 0) {
            bufImage.setLength(0);
            if(interpolatedString){
                switch (c){
                    case '\"':{
                        ttype=NToken.TT_ISTR_DQ;
                        break;
                    }
                    case '\'':{
                        ttype=NToken.TT_ISTR_SQ;
                        break;
                    }
                    case '`':{
                        ttype=NToken.TT_ISTR_AQ;
                        break;
                    }
                }
                bufImage.append('$');
            }else{
                ttype = c;
            }
            bufImage.append((char) c);
            int i = 0;
            int c0=c;
            /* Invariants (because \Octal needs a lookahead):
             *   (i)  c contains char value
             *   (ii) d contains the lookahead
             */
            int d = readChar();
            while (d >= 0 && d != c0 && d != '\n' && d != '\r') {
                bufImage.append((char) d);
                if (d == '\\') {
                    c = readChar();
                    int first = c;   /* To allow \377, but not \477 */
                    if (c >= '0' && c <= '7') {
                        bufImage.append((char) d);
                        c = c - '0';
                        int c2 = readChar();
                        if ('0' <= c2 && c2 <= '7') {
                            bufImage.append((char) d);
                            c = (c << 3) + (c2 - '0');
                            c2 = readChar();
                            bufImage.append((char) d);
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = readChar();
                            } else {
                                d = c2;
                            }
                        } else {
                            d = c2;
                        }
                    } else {
                        bufImage.append((char) d);
                        switch (c) {
                            case 'a':
                                c = 0x7;
                                break;
                            case 'b':
                                c = '\b';
                                break;
                            case 'f':
                                c = 0xC;
                                break;
                            case 'n':
                                c = '\n';
                                break;
                            case 'r':
                                c = '\r';
                                break;
                            case 't':
                                c = '\t';
                                break;
                            case 'v':
                                c = 0xB;
                                break;
                        }
                        d = readChar();
                    }
                } else {
                    c = d;
                    d = readChar();
                }
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char) c;
            }

            /* If we broke out of the loop because we found a matching quote
             * character then arrange to read a new character next time
             * around; otherwise, save the character.
             */
            peekc = (d == c0) ? NEED_CHAR : d;
            if (d == c0) {
                bufImage.append((char) d);
            }
            sval = String.copyValueOf(buf, 0, i);
            image = bufImage.toString();
            return true;
        }
        return false;
    }

    /**
     * Causes the next call to the {@code nextToken} method of this
     * tokenizer to return the current value in the {@code ttype}
     * field, and not to modify the value in the {@code nval} or
     * {@code sval} field.
     */
    public void pushBack() {
        if (ttype != NToken.TT_NOTHING) {  /* No-op if nextToken() not called */
            pushedBack = true;
        }
    }

    /**
     * Return the current line number.
     *
     * @return the current line number of this stream tokenizer.
     */
    public int lineno() {
        return LINENO;
    }

    /**
     * Returns the string representation of the current stream token and
     * the line number it occurs on.
     *
     * <p>The precise string returned is unspecified, although the following
     * example can be considered typical:
     *
     * <blockquote><pre>NToken['a'], line 10</pre></blockquote>
     *
     * @return a string representation of the token
     */
    public String toString() {
        String ret;
        switch (ttype) {
            case NToken.TT_EOF:
                ret = "EOF";
                break;
            case NToken.TT_EOL:
                ret = "EOL";
                break;
            case NToken.TT_WORD:
                ret = sval;
                break;
            case NToken.TT_INT:
                ret = "I=" + nval;
                break;
            case NToken.TT_LONG:
                ret = "L=" + nval;
                break;
            case NToken.TT_BIG_INT:
                ret = "BI=" + nval;
                break;
            case NToken.TT_FLOAT:
                ret = "F=" + nval;
                break;
            case NToken.TT_DOUBLE:
                ret = "D=" + nval;
                break;
            case NToken.TT_BIG_DECIMAL:
                ret = "BD=" + nval;
                break;
            case NToken.TT_NOTHING:
                ret = "NOTHING";
                break;
            default: {
                /*
                 * ttype is the first character of either a quoted string or
                 * is an ordinary character. ttype can definitely not be less
                 * than 0, since those are reserved values used in the previous
                 * case statements
                 */
                if (ttype < 256 &&
                        ((commonCharTypes[ttype] & CT_QUOTE) != 0)) {
                    ret = sval;
                    break;
                }

                char[] s = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char) ttype;
                ret = new String(s);
                break;
            }
        }
        return "NToken[" + ret + "], line " + LINENO;
    }

}
