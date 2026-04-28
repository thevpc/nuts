package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;
import java.util.List;

/**
 * Composable string literal formatter.
 *
 * <p>Three orthogonal concerns are kept separate:</p>
 * <ol>
 *   <li><b>Boundary strategy</b> ({@link Mode}) — how the surrounding delimiters
 *       work: backslash-escape, repeat-char, or per-line prefix.</li>
 *   <li><b>Boundary encoder</b> ({@code boundaryEscape: NCharEscape}) — <em>how</em>
 *       to encode the boundary character when it appears in the body.
 *       {@link NCharEscape#BACKSLASH} for {@code \"}, {@link NCharEscape#REPEAT} for
 *       SQL {@code ''}, or any custom encoder.</li>
 *   <li><b>Body escape set</b> ({@code charEscapeSet: NCharEscapeSet}) — <em>which</em>
 *       other characters need encoding, under what quoting condition, and how
 *       each group is encoded.</li>
 * </ol>
 *
 * <p>No backslash is hardcoded anywhere in this class.</p>
 *
 * <h3>Examples</h3>
 * <pre>{@code
 * // Standard Java double-quoted string
 * NStringLiteralFormat f = NStringLiteralFormat.ofDoubleQuoted(NSupportMode.IF_NEEDED);
 * f.format("hello world");    // → "hello world"
 * f.format("simple");         // → simple
 *
 * // SQL single-quoted, repeat boundary
 * NStringLiteralFormat sql = NStringLiteralFormat.ofRepeatChar('\'',
 *         NSupportMode.ALWAYS, NCharEscapeSet.JAVA_COMMON, null);
 * sql.format("it's\nfine");   // → 'it''s\nfine'
 *
 * // ¶ line-string, custom escape char for boundary
 * NStringLiteralFormat para = NStringLiteralFormat.builder()
 *         .mode(NStringLiteralFormat.Mode.PREFIX)
 *         .linePrefix("¶ ").lineSuffix("\n")
 *         .charEscapeSet(NCharEscapeSet.JAVA_COMMON)
 *         .condition(NSupportMode.ALWAYS)
 *         .build();
 * para.format("hello\nworld"); // → ¶ hello\n\n¶ world\n  (split on real \n)
 *
 * // URL-encoded, no boundary
 * NStringLiteralFormat url = NStringLiteralFormat.builder()
 *         .mode(NStringLiteralFormat.Mode.PREFIX)
 *         .linePrefix("").lineSuffix("")
 *         .charEscapeSet(NCharEscapeSet.of(
 *                 NCharEscapeSet.Entry.always(
 *                         " \n\r\t!#$&'()*+,/:;=?@[]",
 *                         NCharEscape.PERCENT_HEX)))
 *         .condition(NSupportMode.ALWAYS)
 *         .build();
 * url.format("hello world");   // → hello%20world
 * }</pre>
 */
public abstract class AbstractNStringLiteralFormat implements NStringLiteralFormat {


    // ── Mode ──────────────────────────────────────────────────────────────────

    // ── Shared fields ─────────────────────────────────────────────────────────

    protected final NElementType    quoteType;
    protected final NSupportMode    condition;
    protected final NCharEscape     boundaryEscape;  // how to encode the boundary char; may be null
    protected final NCharEscapeSet  charEscapeSet;   // which other chars + how; may be null

    protected AbstractNStringLiteralFormat(
            NElementType   quoteType,
            NSupportMode   condition,
            NCharEscape    boundaryEscape,
            NCharEscapeSet charEscapeSet) {
        this.quoteType      = quoteType;
        this.condition      = condition;
        this.boundaryEscape = boundaryEscape;
        this.charEscapeSet  = charEscapeSet;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Encodes {@code text} to its literal representation.
     * Returns {@code "null"} when {@code text} is {@code null}.
     */
    public abstract String format(String text);

    public NElementType getQuoteType()     { return quoteType; }
    public NSupportMode getCondition()     { return condition; }

    // ── Static factories ──────────────────────────────────────────────────────

    /**
     * Backslash-escape strategy.
     *
     * @param quoteType      target literal syntax
     * @param condition      when to add surrounding boundary characters
     * @param charEscapeSet  which additional chars to escape and how (null = none)
     * @param boundaryEscape how to encode the boundary char (null → {@link NCharEscape#BACKSLASH})
     */
    public static AbstractNStringLiteralFormat ofEscapeChar(
            NElementType   quoteType,
            NSupportMode   condition,
            NCharEscapeSet charEscapeSet,
            NCharEscape    boundaryEscape) {
        NCharEscape be = (boundaryEscape != null) ? boundaryEscape : NCharEscape.BACKSLASH;
        return new EscapeCharFormat(quoteType, condition, charEscapeSet, be);
    }

    /** Convenience: double-quoted, Java common escapes, backslash boundary. */
    public static NStringLiteralFormat ofDoubleQuoted(NSupportMode condition) {
        return ofEscapeChar(NElementType.DOUBLE_QUOTED_STRING, condition,
                NCharEscapeSet.JAVA_WITH_SPACE, NCharEscape.BACKSLASH);
    }

    /** Convenience: single-quoted, Java common escapes, backslash boundary. */
    public static NStringLiteralFormat ofSingleQuoted(NSupportMode condition) {
        return ofEscapeChar(NElementType.SINGLE_QUOTED_STRING, condition,
                NCharEscapeSet.JAVA_WITH_SPACE, NCharEscape.BACKSLASH);
    }

    /** Convenience: backtick, Java common escapes, backslash boundary. */
    public static NStringLiteralFormat ofBacktick(NSupportMode condition) {
        return ofEscapeChar(NElementType.BACKTICK_STRING, condition,
                NCharEscapeSet.JAVA_WITH_SPACE, NCharEscape.BACKSLASH);
    }

    /**
     * Repeat-char strategy (SQL / Pascal / CSV).
     *
     * @param quoteChar      the single boundary character (e.g. {@code '\''})
     * @param condition      when to add surrounding boundary characters
     * @param charEscapeSet  which additional chars to escape and how (null = none)
     * @param boundaryEscape how to encode the boundary char (null → {@link NCharEscape#REPEAT})
     */
    public static NStringLiteralFormat ofRepeatChar(
            char           quoteChar,
            NSupportMode   condition,
            NCharEscapeSet charEscapeSet,
            NCharEscape    boundaryEscape) {
        NCharEscape be = (boundaryEscape != null) ? boundaryEscape : NCharEscape.REPEAT;
        return new RepeatCharFormat(quoteChar, condition, charEscapeSet, be);
    }

    /**
     * Prefix strategy: every logical line is wrapped as
     * {@code linePrefix + encodedLine + lineSuffix}.
     *
     * @param linePrefix     string prepended to every output line (empty = none)
     * @param lineSuffix     string appended  to every output line (empty = none)
     * @param condition      {@code NEVER} suppresses prefix/suffix entirely
     * @param charEscapeSet  which chars to escape and how (null = none)
     */
    public static AbstractNStringLiteralFormat ofPrefix(
            String         linePrefix,
            String         lineSuffix,
            NSupportMode   condition,
            NCharEscapeSet charEscapeSet) {
        return new PrefixFormat(NElementType.LINE_STRING,
                linePrefix, lineSuffix, condition, charEscapeSet);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Mode           mode           = Mode.ESCAPE_CHAR;
        private NElementType   quoteType      = NElementType.DOUBLE_QUOTED_STRING;
        private char           repeatChar     = '\'';
        private NSupportMode   condition      = NSupportMode.SUPPORTED;
        private NCharEscape    boundaryEscape = null;    // null = strategy default
        private NCharEscapeSet charEscapeSet  = NCharEscapeSet.JAVA_WITH_SPACE;
        private String         linePrefix     = "";
        private String         lineSuffix     = "";

        private Builder() {}

        public Builder mode(Mode m)                    { this.mode           = m;  return this; }
        public Builder quoteType(NElementType t)       { this.quoteType      = t;  return this; }
        public Builder repeatChar(char c)              { this.repeatChar     = c;  return this; }
        public Builder condition(NSupportMode c)       { this.condition      = c;  return this; }
        public Builder boundaryEscape(NCharEscape e)   { this.boundaryEscape = e;  return this; }
        public Builder charEscapeSet(NCharEscapeSet s) { this.charEscapeSet  = s;  return this; }
        public Builder linePrefix(String p)            { this.linePrefix     = p;  return this; }
        public Builder lineSuffix(String s)            { this.lineSuffix     = s;  return this; }

        public NStringLiteralFormat build() {
            switch (mode) {
                case ESCAPE_CHAR: {
                    NCharEscape be = (boundaryEscape != null)
                            ? boundaryEscape : NCharEscape.BACKSLASH;
                    return new EscapeCharFormat(quoteType, condition, charEscapeSet, be);
                }
                case REPEAT: {
                    NCharEscape be = (boundaryEscape != null)
                            ? boundaryEscape : NCharEscape.REPEAT;
                    return new RepeatCharFormat(repeatChar, condition, charEscapeSet, be);
                }
                case PREFIX:
                    return new PrefixFormat(quoteType, linePrefix, lineSuffix,
                            condition, charEscapeSet);
                default:
                    throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Returns the single boundary char for single-char quote types, else {@code \0}. */
    protected char quoteChar() {
        switch (quoteType) {
            case DOUBLE_QUOTED_STRING: return '"';
            case SINGLE_QUOTED_STRING: return '\'';
            case BACKTICK_STRING:      return '`';
            default:                   return '\0';
        }
    }

    /** Wraps {@code body} with the boundary chars dictated by {@code quoteType}. */
    protected String wrapWithBoundaries(StringBuilder body) {
        switch (quoteType) {
            case DOUBLE_QUOTED_STRING:        return "\"" + body + "\"";
            case SINGLE_QUOTED_STRING:        return "'"  + body + "'";
            case BACKTICK_STRING:             return "`"  + body + "`";
            case TRIPLE_DOUBLE_QUOTED_STRING: return "\"\"\"" + body + "\"\"\"";
            case TRIPLE_SINGLE_QUOTED_STRING: return "'''"    + body + "'''";
            case TRIPLE_BACKTICK_STRING:      return "```"    + body + "```";
            default:
                throw new IllegalArgumentException("Unsupported quote type for wrap: " + quoteType);
        }
    }

    // ── Concrete strategies ───────────────────────────────────────────────────

    /**
     * ESCAPE_CHAR strategy.
     *
     * <p>Character processing order (first match wins):</p>
     * <ol>
     *   <li>Boundary char   → encoded by {@code boundaryEscape}; marks quoted=true</li>
     *   <li>{@code charEscapeSet} lookup (respects quoted state) → use returned encoding</li>
     *   <li>Pass through</li>
     * </ol>
     *
     * <p>Note: space and other "conditional" chars are handled entirely by
     * {@code charEscapeSet} entries with {@code When.UNQUOTED_ONLY} — there is
     * no special-casing of space here.</p>
     */
    private static final class EscapeCharFormat extends AbstractNStringLiteralFormat {

        EscapeCharFormat(
                NElementType   quoteType,
                NSupportMode   condition,
                NCharEscapeSet charEscapeSet,
                NCharEscape    boundaryEscape) {
            super(quoteType, condition, boundaryEscape, charEscapeSet);
        }

        @Override
        public String format(String text) {
            if (text == null) return "null";

            boolean requireQuotes = (condition == NSupportMode.ALWAYS);
            boolean allowQuotes   = (condition != NSupportMode.NEVER);
            char    ownQuote      = quoteChar();
            StringBuilder body    = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                // 1. Boundary char
                if (ownQuote != '\0' && c == ownQuote) {
                    body.append(boundaryEscape.escape(c, this));
                    if (allowQuotes) requireQuotes = true;
                    continue;
                }

                // 2. charEscapeSet — pass current quoted state
                if (charEscapeSet != null) {
                    // "quoted" here means: will quotes be present?
                    // We use requireQuotes as a proxy — it is true when we already
                    // know quotes are needed.  For UNQUOTED_ONLY entries this means
                    // we escape the char when we are still in "possibly unquoted" mode.
                    boolean currentlyQuoted = requireQuotes;
                    String encoded = charEscapeSet.apply(c, currentlyQuoted, this);
                    if (encoded != null) {
                        body.append(encoded);
                        // If the char is in an ALWAYS entry it forces quoting too
                        if (allowQuotes && charEscapeSet.triggers(c, true)) {
                            requireQuotes = true;
                        }
                        continue;
                    }
                    // If it triggers only when unquoted, its presence forces quoting
                    // (so it can be written literally inside the quotes)
                    if (allowQuotes && charEscapeSet.triggers(c, false)
                            && !charEscapeSet.triggers(c, true)) {
                        body.append(c);
                        requireQuotes = true;
                        continue;
                    }
                }

                // 3. Pass through
                body.append(c);
            }

            if (body.length() == 0) requireQuotes = true;
            return requireQuotes ? wrapWithBoundaries(body) : body.toString();
        }
    }

    /**
     * REPEAT strategy: boundary char is encoded by {@code boundaryEscape}
     * (typically {@link NCharEscape#REPEAT}, which doubles the char).
     */
    private static final class RepeatCharFormat extends AbstractNStringLiteralFormat {

        private final char qc;

        RepeatCharFormat(
                char           quoteChar,
                NSupportMode   condition,
                NCharEscapeSet charEscapeSet,
                NCharEscape    boundaryEscape) {
            super(resolveType(quoteChar), condition, boundaryEscape, charEscapeSet);
            this.qc = quoteChar;
        }

        private static NElementType resolveType(char c) {
            switch (c) {
                case '\'': return NElementType.SINGLE_QUOTED_STRING;
                case '`':  return NElementType.BACKTICK_STRING;
                default:   return NElementType.DOUBLE_QUOTED_STRING;
            }
        }

        @Override
        public String format(String text) {
            if (text == null) return "null";

            boolean requireQuotes = (condition == NSupportMode.ALWAYS);
            boolean allowQuotes   = (condition != NSupportMode.NEVER);
            StringBuilder body    = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                // 1. Boundary char — encode with boundaryEscape (usually REPEAT)
                if (c == qc) {
                    body.append(boundaryEscape.escape(c, this));
                    if (allowQuotes) requireQuotes = true;
                    continue;
                }

                // 2. charEscapeSet
                if (charEscapeSet != null) {
                    boolean currentlyQuoted = requireQuotes;
                    String encoded = charEscapeSet.apply(c, currentlyQuoted, this);
                    if (encoded != null) {
                        body.append(encoded);
                        if (allowQuotes && charEscapeSet.triggers(c, true)) requireQuotes = true;
                        continue;
                    }
                    if (allowQuotes && charEscapeSet.triggers(c, false)
                            && !charEscapeSet.triggers(c, true)) {
                        body.append(c);
                        requireQuotes = true;
                        continue;
                    }
                }

                // 3. Pass through
                body.append(c);
            }

            if (body.length() == 0) requireQuotes = true;
            return requireQuotes ? wrapWithBoundaries(body) : body.toString();
        }
    }

    /**
     * PREFIX strategy: every logical line is wrapped with {@code linePrefix} /
     * {@code lineSuffix}.  No boundary char quoting logic applies — the
     * {@code charEscapeSet} (with {@code quoted=false}, since there are no
     * surrounding quotes) governs all character encoding.
     *
     * <p>When {@code condition == NEVER} prefix/suffix are suppressed and the
     * encoded body is returned as-is.</p>
     */
    private static final class PrefixFormat extends AbstractNStringLiteralFormat {

        private final String linePrefix;
        private final String lineSuffix;

        PrefixFormat(
                NElementType   quoteType,
                String         linePrefix,
                String         lineSuffix,
                NSupportMode   condition,
                NCharEscapeSet charEscapeSet) {
            // PREFIX has no boundary escape concept — pass null
            super(quoteType, condition, null, charEscapeSet);
            this.linePrefix = (linePrefix != null) ? linePrefix : "";
            this.lineSuffix = (lineSuffix != null) ? lineSuffix : "";
        }

        @Override
        public String format(String text) {
            if (text == null) return "null";

            // There are no surrounding quotes in prefix mode.
            // All chars are in "unquoted" context → quoted=false always.
            StringBuilder encoded = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                String e = (charEscapeSet != null)
                        ? charEscapeSet.apply(c, false, this) : null;
                if (e != null) {
                    encoded.append(e);
                } else {
                    encoded.append(c);
                }
            }

            if (condition == NSupportMode.NEVER) {
                return encoded.toString();
            }

            List<String> lines = NStringUtils.splitLines(encoded.toString());
            StringBuilder out = new StringBuilder();
            if (lines.isEmpty()) {
                out.append(linePrefix).append(lineSuffix);
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    out.append(linePrefix).append(lines.get(i)).append(lineSuffix);
                }
            }
            return out.toString();
        }
    }
}
