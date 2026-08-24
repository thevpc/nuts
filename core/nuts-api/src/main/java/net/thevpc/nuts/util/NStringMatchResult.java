package net.thevpc.nuts.util;

import java.util.regex.Matcher;

/**
 * NStringMatchResult class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NStringMatchResult{
    private static final NStringMatchResult NO_MATCH = new NStringMatchResultAsNoMatch();
    private final NMatchType mode;

    /**
     * Creates a new instance of of no match.
     *
     * @return of no match result
     */
    public static NStringMatchResult ofNoMatch() {
        return NO_MATCH;
    }

    /**
     * Creates a new instance of of match.
     *
     * @param value value
     * @return of match result
     */
    public static NStringMatchResult ofMatch(Matcher value) {
        return new YesPattern(NMatchType.MATCH, value);
    }

    /**
     * Creates a new instance of of partial match.
     *
     * @param value value
     * @return of partial match result
     */
    public static NStringMatchResult ofPartialMatch(String value) {
        return new NStringMatchResultAsPartial(value);
    }

    /**
     * Creates a new instance of of full match.
     *
     * @param value value
     * @return of full match result
     */
    public static NStringMatchResult ofFullMatch(Matcher value) {
        return new YesPattern(NMatchType.FULL_MATCH, value);
    }

    /**
     * Creates a new instance of of full match.
     *
     * @param value value
     * @return of full match result
     */
    public static NStringMatchResult ofFullMatch(String value) {
        return new YesString(NMatchType.FULL_MATCH, value);
    }

    /**
     * N string match result.
     *
     * @param mode mode
     * @return n string match result result
     */
    private NStringMatchResult(NMatchType mode) {
        this.mode = mode;
    }

    /**
     * Mode.
     *
     * @return mode result
     */
    public NMatchType mode() {
        return mode;
    }

    /**
     * Count.
     *
     * @return count result
     */
    public int count() {
        String s = get();
        return s == null ? 0 : s.length();
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    public abstract String get();

    /**
     * Returns the get.
     *
     * @param name name
     * @return get result
     */
    public abstract String get(String name);

    @NImmutable
    private static class YesPattern extends NStringMatchResult {
        private Matcher value;

        /**
         * Yes pattern.
         *
         * @param mode mode
         * @param value value
         * @return yes pattern result
         */
        public YesPattern(NMatchType mode, Matcher value) {
          /**
           * Super.
           *
           * @param mode mode
           */
            super(mode);
            this.value = value;
        }

        @Override
        public String get() {
            return value.group();
        }

        @Override
        public String get(String name) {
            return value.group(name);
        }
    }

    @NImmutable
    private static class YesString extends NStringMatchResult {
        private String value;

        /**
         * Yes string.
         *
         * @param mode mode
         * @param value value
         * @return yes string result
         */
        public YesString(NMatchType mode, String value) {
          /**
           * Super.
           *
           * @param mode mode
           */
            super(mode);
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public String get(String name) {
            return value;
        }
    }

    @NImmutable
    private static class NStringMatchResultAsNoMatch extends NStringMatchResult {
        /**
         * N string match result as no match.
         *
         * @return n string match result as no match result
         */
        public NStringMatchResultAsNoMatch() {
          /**
           * Super.
           *
           * @param NMatchType.NO_MATCH n match type.no_match
           */
            super(NMatchType.NO_MATCH);
        }

        /**
         * Returns the get.
         *
         * @return get result
         */
        public String get() {
            return null;
        }

        @Override
        public String get(String name) {
            return null;
        }
    }

    @NImmutable
    private static class NStringMatchResultAsPartial extends NStringMatchResult {
        private String value;

        /**
         * N string match result as partial.
         *
         * @param value value
         * @return n string match result as partial result
         */
        public NStringMatchResultAsPartial(String value) {
          /**
           * Super.
           *
           * @param NMatchType.PARTIAL_MATCH n match type.partial_match
           */
            super(NMatchType.PARTIAL_MATCH);
            this.value = value;
        }

        /**
         * Returns the get.
         *
         * @return get result
         */
        public String get() {
            return value;
        }

        @Override
        public String get(String name) {
            return value;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mode.toString());
        if (mode == NMatchType.FULL_MATCH
                || mode == NMatchType.MATCH
                || mode == NMatchType.PARTIAL_MATCH
        ) {
            sb.append("(");
            sb.append(get());
            sb.append(")");
        }
        return sb.toString();
    }
}
