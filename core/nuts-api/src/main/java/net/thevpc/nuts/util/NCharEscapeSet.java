package net.thevpc.nuts.util;

import java.util.Arrays;

/**
 * Describes <em>which</em> characters must be escaped, <em>under what quoting
 * condition</em> they must be escaped, and <em>how</em> each group is encoded.
 *
 * <p>This is entirely separate from the boundary strategy ({@link AbstractNStringLiteralFormat.Mode})
 * and from the boundary-char encoder ({@code boundaryEscape} on the formatter).
 * A set is composed of one or more {@link Entry} records, each of which pairs:</p>
 * <ul>
 *   <li>a sorted, deduplicated array of characters (the "trigger set")</li>
 *   <li>a {@link When} value: {@code ALWAYS} or {@code UNQUOTED_ONLY}</li>
 *   <li>an {@link NCharEscape} that says how to encode a triggered character</li>
 * </ul>
 *
 * <p>During formatting a character is tested against every entry in order.
 * The first entry whose trigger set contains the character (binary-search, O(log n))
 * and whose {@code when} condition is satisfied determines the encoding.
 * Characters not found in any entry are passed through unchanged.</p>
 *
 * <h3>Built-in sets</h3>
 * <pre>{@code
 * // \n \r \t \f \b — always, using named backslash sequences
 * NCharEscapeSet s = NCharEscapeSet.JAVA_COMMON;
 *
 * // space — escaped only when no surrounding quotes are present
 * NCharEscapeSet s = NCharEscapeSet.UNQUOTED_SPACE;
 *
 * // combine: Java common + unquoted space
 * NCharEscapeSet s = NCharEscapeSet.combine(NCharEscapeSet.JAVA_COMMON,
 *                                           NCharEscapeSet.UNQUOTED_SPACE);
 * }</pre>
 *
 * <h3>Custom entry</h3>
 * <pre>{@code
 * // '#' and '$' must always be backslash-escaped
 * NCharEscapeSet s = NCharEscapeSet.of(
 *         NCharEscapeSet.Entry.always("#$", NCharEscape.BACKSLASH));
 * }</pre>
 */
public final class NCharEscapeSet {

    // ── When ─────────────────────────────────────────────────────────────────

    /**
     * Controls under which quoting context a trigger fires.
     *
     * <ul>
     *   <li>{@link #ALWAYS}        — escape the char regardless of whether
     *       surrounding quotes are present.</li>
     *   <li>{@link #UNQUOTED_ONLY} — escape the char only when the formatter
     *       is operating without surrounding quotes (e.g. bare shell token).
     *       When quotes are present the char can appear literally inside them.</li>
     * </ul>
     */
    public enum When {
        ALWAYS,
        UNQUOTED_ONLY
    }

    // ── Entry ─────────────────────────────────────────────────────────────────

    /**
     * One group inside a {@link NCharEscapeSet}: a sorted+deduped char array,
     * a firing condition, and the encoder to apply.
     */
    public static final class Entry {
        private final char[]      chars;   // sorted, deduplicated — supports binary search
        private final When        when;
        private final NCharEscape escape;

        private Entry(char[] chars, When when, NCharEscape escape) {
            this.chars  = sortAndDedup(chars);
            this.when   = when;
            this.escape = escape;
        }

        // ── Factories ─────────────────────────────────────────────────────────

        /**
         * Creates an entry from a String of characters (convenient for inline literals).
         * Duplicates and order do not matter — the array is sorted and deduplicated.
         */
        public static Entry always(String chars, NCharEscape escape) {
            return new Entry(chars.toCharArray(), When.ALWAYS, escape);
        }

        /** Convenience: fires only when the formatter is operating without quotes. */
        public static Entry unquotedOnly(String chars, NCharEscape escape) {
            return new Entry(chars.toCharArray(), When.UNQUOTED_ONLY, escape);
        }

        /** Full constructor for programmatic use. */
        public static Entry of(char[] chars, When when, NCharEscape escape) {
            return new Entry(chars, when, escape);
        }

        // ── Lookup ────────────────────────────────────────────────────────────

        /**
         * Returns {@code true} if {@code c} is in this entry's trigger set
         * (binary search — O(log n)).
         */
        public boolean contains(char c) {
            return Arrays.binarySearch(chars, c) >= 0;
        }

        public When        getWhen()   { return when; }
        public NCharEscape getEscape() { return escape; }

        // ── Helpers ───────────────────────────────────────────────────────────

        private static char[] sortAndDedup(char[] raw) {
            if (raw == null || raw.length == 0) return new char[0];
            char[] copy = Arrays.copyOf(raw, raw.length);
            Arrays.sort(copy);
            // compact duplicates
            int w = 0;
            for (int i = 0; i < copy.length; i++) {
                if (i == 0 || copy[i] != copy[i - 1]) {
                    copy[w++] = copy[i];
                }
            }
            return w == copy.length ? copy : Arrays.copyOf(copy, w);
        }
    }

    // ── NCharEscapeSet ────────────────────────────────────────────────────────

    private final Entry[] entries;

    private NCharEscapeSet(Entry[] entries) {
        this.entries = entries;
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    /** Creates a set from one or more entries (order is preserved). */
    public static NCharEscapeSet of(Entry... entries) {
        return new NCharEscapeSet(entries.clone());
    }

    /**
     * Merges two sets by concatenating their entry lists.
     * Entries from {@code a} are tested before entries from {@code b}.
     */
    public static NCharEscapeSet combine(NCharEscapeSet a, NCharEscapeSet b) {
        Entry[] merged = new Entry[a.entries.length + b.entries.length];
        System.arraycopy(a.entries, 0, merged, 0, a.entries.length);
        System.arraycopy(b.entries, 0, merged, a.entries.length, b.entries.length);
        return new NCharEscapeSet(merged);
    }

    /** Returns a new set with {@code extra} appended at the end. */
    public NCharEscapeSet andThen(NCharEscapeSet extra) {
        return combine(this, extra);
    }

    // ── Built-in sets ─────────────────────────────────────────────────────────

    /**
     * Java/C named backslash sequences for the six common control characters:
     * {@code \n \r \t \f \b \0} — always, regardless of quoting.
     */
    public static final NCharEscapeSet JAVA_COMMON = NCharEscapeSet.of(
            Entry.always("\n\r\t\f\b\0", NCharEscape.BACKSLASH_NAMED)
    );


    /**
     * Space must be escaped only when there are no surrounding quotes
     * (bare shell token / unquoted TSON value).
     * Inside quotes the space is written literally.
     */
    public static final NCharEscapeSet UNQUOTED_SPACE = NCharEscapeSet.of(
            Entry.unquotedOnly(" ", NCharEscape.BACKSLASH)
    );

    /**
     * Newline must be escaped only when there are no surrounding quotes.
     * Useful for formats where a quoted string may span lines but an unquoted
     * token may not.
     */
    public static final NCharEscapeSet UNQUOTED_NEWLINE = NCharEscapeSet.of(
            Entry.unquotedOnly("\n", NCharEscape.BACKSLASH_NAMED)
    );

    /**
     * Convenience combination: Java common control chars (always) + space
     * (unquoted only).  Covers the typical unquoted/quoted shell-token case.
     */
    public static final NCharEscapeSet JAVA_WITH_SPACE = combine(JAVA_COMMON, UNQUOTED_SPACE);

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Looks up {@code c} in this set given the current quoting state.
     *
     * @param c        the character to test
     * @param quoted   {@code true} if the formatter is currently writing inside quotes
     * @param format   the surrounding formatter (forwarded to the entry's encoder)
     * @return the encoded replacement string, or {@code null} if {@code c} should
     *         pass through unchanged
     */
    public String apply(char c, boolean quoted, NStringLiteralFormat format) {
        for (int i = 0; i < entries.length; i++) {
            Entry e = entries[i];
            // Skip UNQUOTED_ONLY entries when we are inside quotes
            if (e.getWhen() == When.UNQUOTED_ONLY && quoted) {
                continue;
            }
            if (e.contains(c)) {
                return e.getEscape().escape(c, format);
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if {@code c} is present in any entry whose
     * condition matches {@code quoted}.  Useful for the formatter to decide
     * whether quoting is required without actually encoding.
     */
    public boolean triggers(char c, boolean quoted) {
        for (int i = 0; i < entries.length; i++) {
            Entry e = entries[i];
            if (e.getWhen() == When.UNQUOTED_ONLY && quoted) continue;
            if (e.contains(c)) return true;
        }
        return false;
    }
}
