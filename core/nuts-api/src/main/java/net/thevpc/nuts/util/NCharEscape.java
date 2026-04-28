package net.thevpc.nuts.util;

import java.nio.charset.StandardCharsets;

/**
 * Encodes a single character into its escaped representation.
 *
 * <p>This interface is purely about <em>how</em> to encode — it carries no
 * knowledge of <em>which</em> characters should be escaped or under what
 * quoting conditions.  That responsibility belongs to {@link NCharEscapeSet}.</p>
 *
 * <p>Implementations are composable via {@link #andThen}: the first helper
 * that returns a non-null result wins.</p>
 */
public interface NCharEscape {

    /**
     * Encodes {@code c} inside the context of {@code format}.
     *
     * @param c      the character to encode
     * @param format the surrounding formatter (exposes boundary char, quoteType, …)
     * @return the replacement string, or {@code null} to pass {@code c} through unchanged
     */
    String escape(char c, NStringLiteralFormat format);

    // ── Built-in encoders ─────────────────────────────────────────────────────

    /**
     * Backslash prefix: emits {@code \ + c} for any character.
     * Use this as the {@code boundaryEscape} in a formatter, or inside an
     * {@link NCharEscapeSet.Entry} for specific control characters.
     */
    NCharEscape BACKSLASH = new NCharEscape() {
        @Override
        public String escape(char c, NStringLiteralFormat format) {
            return "\\" + c;
        }
    };

    /**
     * Standard Java/C named sequences for the six common control characters
     * ({@code \n \r \t \f \b \0}).  Returns {@code null} for anything else.
     *
     * <p>Intended for use inside an {@link NCharEscapeSet.Entry} that lists
     * exactly {@code \n \r \t \f \b \0} as its char set.</p>
     */
    NCharEscape BACKSLASH_NAMED = new NCharEscape() {
        @Override
        public String escape(char c, NStringLiteralFormat format) {
            switch (c) {
                case '\n': return "\\n";
                case '\r': return "\\r";
                case '\t': return "\\t";
                case '\f': return "\\f";
                case '\b': return "\\b";
                case '\0': return "\\0";
                default:   return null;
            }
        }
    };

    /**
     * Percent-hex encoding: {@code %XX} for every character (unreserved URI
     * characters pass through as {@code null}).  Suitable as an
     * {@link NCharEscapeSet.Entry} encoder or as a standalone formatter
     * {@code charEscapeSet} in URL contexts.
     */
    NCharEscape PERCENT_HEX = new NCharEscape() {
        @Override
        public String escape(char c, NStringLiteralFormat format) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                return null;
            }
            byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder(bytes.length * 3);
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                sb.append('%');
                sb.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)));
                sb.append(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
            }
            return sb.toString();
        }
    };

    /**
     * Unicode escape sequences: {@code \0uXXXX} for non-printable or non-ASCII
     * characters.  Printable ASCII ({@code 0x20..0x7E}) passes through as
     * {@code null}.
     */
    NCharEscape UNICODE = new NCharEscape() {
        @Override
        public String escape(char c, NStringLiteralFormat format) {
            if (c >= 0x20 && c <= 0x7E) return null;
            return String.format("\\u%04X", (int) c);
        }
    };

    /**
     * Repeats the character: emits {@code c + c}.
     * Used as the {@code boundaryEscape} for SQL / Pascal repeat strategies.
     */
    NCharEscape REPEAT = new NCharEscape() {
        @Override
        public String escape(char c, NStringLiteralFormat format) {
            return String.valueOf(c) + c;
        }
    };

    /**
     * Creates an encoder that wraps {@code c} with a fixed {@code prefix} and
     * {@code suffix}.  For example {@code ofWrap("%", "")} gives percent-prefix
     * encoding without hex conversion; {@code ofWrap("&#", ";")} gives XML
     * decimal char references (when combined with a decimal formatter elsewhere).
     */
    static NCharEscape ofWrap(final String prefix, final String suffix) {
        return new NCharEscape() {
            @Override
            public String escape(char c, NStringLiteralFormat format) {
                return prefix + c + suffix;
            }
        };
    }

    // ── Composition ───────────────────────────────────────────────────────────

    /**
     * Returns a new encoder that tries {@code this} first and falls back to
     * {@code other} when {@code this} returns {@code null}.
     */
    default NCharEscape andThen(final NCharEscape other) {
        final NCharEscape self = this;
        return new NCharEscape() {
            @Override
            public String escape(char c, NStringLiteralFormat format) {
                String r = self.escape(c, format);
                return r != null ? r : other.escape(c, format);
            }
        };
    }

    /** Static alias for {@link #andThen}, useful in static-import contexts. */
    static NCharEscape chain(NCharEscape first, NCharEscape second) {
        return first.andThen(second);
    }
}
