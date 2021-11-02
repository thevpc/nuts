package net.thevpc.nuts.runtime.bundles.parsers;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
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

public class StreamTokenizerExt {

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
    public static final int TT_DOUBLE = -2;
    /**
     * A constant indicating that a word token has been read.
     */
    public static final int TT_WORD = -3;
    public static final int TT_INTEGER = -4;
    public static final int TT_COMMENTS = -5;
    public static final int TT_SPACES = -6;
    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;
    /* A constant indicating that no token has been read, used for
     * initializing ttype.  FIXME This could be made public and
     * made available as the part of the API in a future release.
     */
    private static final int TT_NOTHING = -5;
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
     *
     */
    public int ttype = TT_NOTHING;
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
     *
     */
    public String sval;
    public String image;
    /**
     * If the current token is a number, this field contains the value
     * of that number. The current token is a number when the value of
     * the {@code ttype} field is {@code TT_NUMBER}.
     * <p>
     * The initial value of this field is 0.0.
     *
     */
    public double dval;
    public long ival;
    public boolean returnComments=true;
    public boolean returnSpaces=true;
    /* Only one of these will be non-null */
    private Reader reader = null;
//    private InputStream input = null;
    private char[] buf = new char[20];
    private StringBuilder bufImage = new StringBuilder();
    /**
     * The next character to be considered by the nextToken method.  May also
     * be NEED_CHAR to indicate that a new character should be read, or SKIP_LF
     * to indicate that a new character should be read and, if it is a '\n'
     * character, it should be discarded and a second new character should be
     * read.
     */
    private int peekc = NEED_CHAR;
    private boolean pushedBack;
    private boolean forceLower;
    /**
     * The line number of the last token read
     */
    private int LINENO = 1;
    private boolean eolIsSignificantP = false;
    private boolean slashSlashCommentsP = false;
    private boolean slashStarCommentsP = false;
    private boolean xmlCommentsP = false;
    private byte ctype[] = new byte[256];

    /**
     * Private constructor that initializes everything except the streams.
     */
    private StreamTokenizerExt() {
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars(128 + 32, 255);
        whitespaceChars(0, ' ');
//        commentChar('/');
        quoteChar('"');
        quoteChar('\'');
        quoteChar('`');
        parseNumbers();
    }


    /**
     * Create a tokenizer that parses the given character stream.
     *
     * @param r a Reader object providing the input stream.
     * @since JDK1.1
     */
    public StreamTokenizerExt(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        reader = r;
    }

    /**
     * Resets this tokenizer's syntax table so that all characters are
     * "ordinary." See the {@code ordinaryChar} method
     * for more information on a character being ordinary.
     *
     */
    public void resetSyntax() {
        for (int i = ctype.length; --i >= 0; )
            ctype[i] = 0;
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
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] |= CT_ALPHA;
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
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = CT_WHITESPACE;
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
        if (hi >= ctype.length)
            hi = ctype.length - 1;
        while (low <= hi)
            ctype[low++] = 0;
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
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = 0;
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
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_COMMENT;
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
        if (ch >= 0 && ch < ctype.length)
            ctype[ch] = CT_QUOTE;
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
     *
     */
    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++)
            ctype[i] |= CT_DIGIT;
        ctype['.'] |= CT_DIGIT;
        ctype['-'] |= CT_DIGIT;
    }

    public void doNotParseNumbers() {
        for (int i = '0'; i <= '9'; i++)
            ctype[i] &= ~CT_DIGIT;
        ctype['.'] &= ~CT_DIGIT;
        ctype['-'] &= ~CT_DIGIT;
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
        slashStarCommentsP = flag;
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
        slashSlashCommentsP = flag;
    }

    public StreamTokenizerExt xmlComments(boolean flag) {
        this.xmlCommentsP = flag;
        slashSlashComments(false);
        slashStarComments(false);
        return this;
    }

    public StreamTokenizerExt javaComments() {
        commentChar('/');
        slashSlashComments(true);
        slashStarComments(true);
        return this;
    }

    public StreamTokenizerExt pythonComments() {
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
    private int read() {
        try {
            return reader.read();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void mark(int count) {
        try {
            reader.mark(count);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void reset() {
        try {
            reader.reset();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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
        this.image = null;
        if (pushedBack) {
            pushedBack = false;
            return ttype;
        }
        byte ct[] = ctype;
        sval = null;

        int c = peekc;
        if (c < 0)
            c = NEED_CHAR;
        if (c == SKIP_LF) {
            c = read();
            if (c < 0)
                return ttype = TT_EOF;
            if (c == '\n')
                c = NEED_CHAR;
        }
        if (c == NEED_CHAR) {
            c = read();
            if (c < 0)
                return ttype = TT_EOF;
        }
        ttype = c;              /* Just to be safe */

        /* Set peekc so that the next invocation of nextToken will read
         * another character unless peekc is reset in this invocation
         */
        peekc = NEED_CHAR;

        int ctype = c < 256 ? ct[c] : CT_ALPHA;
        bufImage.setLength(0);
        while ((ctype & CT_WHITESPACE) != 0) {
            bufImage.append((char)c);
            if (c == '\r') {
                LINENO++;
                if (eolIsSignificantP) {
                    peekc = SKIP_LF;
                    return ttype = TT_EOL;
                }
                c = read();
                if (c == '\n') {
                    bufImage.append((char)c);
                    c = read();
                }
            } else {
                if (c == '\n') {
                    LINENO++;
                    if (eolIsSignificantP) {
                        return ttype = TT_EOL;
                    }
                }
                c = read();
            }
            if(bufImage.length()>0 && returnSpaces){
                peekc = c;
                image=bufImage.toString();
                return ttype = TT_SPACES;
            }
            if (c < 0)
                return ttype = TT_EOF;
            ctype = c < 256 ? ct[c] : CT_ALPHA;
        }

        if ((ctype & CT_DIGIT) != 0) {
            StringBuilder image = new StringBuilder();
            image.append((char) c);
            boolean neg = false;
            boolean intType = true;
            if (c == '-') {
                c = read();
                image.append((char) c);
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;
                    return ttype = '-';
                }
                neg = true;
            }
            double dv = 0;
            long iv = 0;
            int decexp = 0;
            int seendot = 0;
            boolean loop=true;
            if (c == '.' && seendot == 0) {
                seendot = 1;
                intType=false;
            }else if ('0' <= c && c <= '9') {
                dv = dv * 10 + (c - '0');
                iv = iv * 10 + (c - '0');
                decexp += seendot;
            } else {
                loop=false;
            }
            if(loop) {
                c = read();
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
                    c = read();
                }
            }
            peekc = c;
            if (decexp != 0) {
                double denom = 10;
                decexp--;
                while (decexp > 0) {
                    denom *= 10;
                    decexp--;
                }
                /* Do one division of a likely-to-be-more-accurate number */
                dv = dv / denom;
            }
            dval = neg ? -dv : dv;
            this.image = image.toString();
            if(intType){
                ival=neg ? -iv : iv;
                return ttype = TT_INTEGER;
            }
            return ttype = TT_DOUBLE;
        }

        if ((ctype & CT_ALPHA) != 0) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[i++] = (char) c;
                c = read();
                ctype = c < 0 ? CT_WHITESPACE : c < 256 ? ct[c] : CT_ALPHA;
            } while ((ctype & (CT_ALPHA | CT_DIGIT)) != 0);
            peekc = c;
            sval = String.copyValueOf(buf, 0, i);
            image=sval;
            if (forceLower)
                sval = sval.toLowerCase();
            return ttype = TT_WORD;
        }

        if ((ctype & CT_QUOTE) != 0) {
            ttype = c;
            bufImage.setLength(0);
            bufImage.append((char)c);
            int i = 0;
            /* Invariants (because \Octal needs a lookahead):
             *   (i)  c contains char value
             *   (ii) d contains the lookahead
             */
            int d = read();
            while (d >= 0 && d != ttype && d != '\n' && d != '\r') {
                bufImage.append((char)d);
                if (d == '\\') {
                    c = read();
                    int first = c;   /* To allow \377, but not \477 */
                    if (c >= '0' && c <= '7') {
                        bufImage.append((char)d);
                        c = c - '0';
                        int c2 = read();
                        if ('0' <= c2 && c2 <= '7') {
                            bufImage.append((char)d);
                            c = (c << 3) + (c2 - '0');
                            c2 = read();
                            bufImage.append((char)d);
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = read();
                            } else
                                d = c2;
                        } else
                            d = c2;
                    } else {
                        bufImage.append((char)d);
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
                        d = read();
                    }
                } else {
                    c = d;
                    d = read();
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
            peekc = (d == ttype) ? NEED_CHAR : d;
            if(d == ttype){
                bufImage.append((char)d);
            }
            sval = String.copyValueOf(buf, 0, i);
            image=bufImage.toString();
            return ttype;
        }

        if (c == '/' && (slashSlashCommentsP || slashStarCommentsP)) {
            StringBuilder sb=new StringBuilder();
            sb.append((char)c);
            c = read();
            if (c == '*' && slashStarCommentsP) {
                sb.append((char)c);
                int prevc = 0;
                while ((c = read()) != '/' || prevc != '*') {
                    if (c == '\r') {
                        sb.append((char)c);
                        LINENO++;
                        c = read();
                        if (c == '\n') {
                            sb.append((char)c);
                            c = read();
                        }
                    } else {
                        if (c == '\n') {
                            LINENO++;
                            sb.append((char)c);
                            c = read();
                        }
                    }
                    if (c < 0)
                        return ttype = TT_EOF;
                    prevc = c;
                }
                if(returnComments){
                    image=sb.toString();
                    return ttype = TT_COMMENTS;
                }
                return nextToken();
            } else if (c == '/' && slashSlashCommentsP) {
                sb.append((char)c);
                while ((c = read()) != '\n' && c != '\r' && c >= 0) {
                    sb.append((char)c);
                }
                peekc = c;
                if(returnComments){
                    image=sb.toString();
                    return ttype = TT_COMMENTS;
                }
                return nextToken();
            } else {
                /* Now see if it is still a single line comment */
                if ((ct['/'] & CT_COMMENT) != 0) {
                    sb.append((char)c);
                    while ((c = read()) != '\n' && c != '\r' && c >= 0) {
                        sb.append((char)c);
                    }
                    if(returnComments){
                        image=sb.toString();
                        return ttype = TT_COMMENTS;
                    }
                    peekc = c;
                    return nextToken();
                } else {
                    peekc = c;
                    return ttype = '/';
                }
            }
        }else if(c == '<' && (xmlCommentsP)){
            StringBuilder sb=new StringBuilder();
            sb.append((char)c);
            mark(4);
            int a = read();
            if(a=='!'){
                sb.append((char)c);
                a = read();
                if(a=='-') {
                    sb.append((char)c);
                    a = read();
                    if(a=='-') {
                        sb.append((char)c);
                        while (true){
                            c=read();
                            boolean wasEnd=false;
                            if(c == '-'){
                                sb.append((char)c);
                                a = read();
                                if(a=='-'){
                                    a = read();
                                    if(a=='>'){
                                        wasEnd=true;
                                    }
                                }
                                if(wasEnd){
                                    sb.append("->");
                                    break;
                                }else{
                                    reset();
                                }
                            }else{
                                sb.append((char)c);
                            }
                        }
                        if(returnComments){
                            image=sb.toString();
                            return ttype = TT_COMMENTS;
                        }else{
                            return nextToken();
                        }
                    }
                }
            }
            reset();
            image="<";
            return ttype = '<';
        }

        if ((ctype & CT_COMMENT) != 0) {
            StringBuilder sb=new StringBuilder();
            while ((c = read()) != '\n' && c != '\r' && c >= 0) {
                sb.append((char)c);
            }
            peekc = c;
            return nextToken();
        }

        image=String.valueOf((char)c);
        return ttype = c;
    }

    /**
     * Causes the next call to the {@code nextToken} method of this
     * tokenizer to return the current value in the {@code ttype}
     * field, and not to modify the value in the {@code nval} or
     * {@code sval} field.
     *
     */
    public void pushBack() {
        if (ttype != TT_NOTHING) {  /* No-op if nextToken() not called */
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
     * <blockquote><pre>NutsToken['a'], line 10</pre></blockquote>
     *
     * @return a string representation of the token
     */
    public String toString() {
        String ret;
        switch (ttype) {
            case TT_EOF:
                ret = "EOF";
                break;
            case TT_EOL:
                ret = "EOL";
                break;
            case TT_WORD:
                ret = sval;
                break;
            case TT_DOUBLE:
                ret = "d=" + dval;
                break;
            case TT_INTEGER:
                ret = "i=" + ival;
                break;
            case TT_NOTHING:
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
                        ((ctype[ttype] & CT_QUOTE) != 0)) {
                    ret = sval;
                    break;
                }

                char s[] = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char) ttype;
                ret = new String(s);
                break;
            }
        }
        return "NutsToken[" + ret + "], line " + LINENO;
    }

}