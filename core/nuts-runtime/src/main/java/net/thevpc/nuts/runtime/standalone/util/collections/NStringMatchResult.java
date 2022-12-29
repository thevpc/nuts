package net.thevpc.nuts.runtime.standalone.util.collections;

import java.util.regex.Matcher;

public abstract class NStringMatchResult {
    private static final NStringMatchResult NO_MATCH = new NStringMatchResultAsNoMatch();
    private NMatchType mode;

    public static NStringMatchResult ofNoMatch() {
        return NO_MATCH;
    }

    public static NStringMatchResult ofMatch(Matcher value) {
        return new YesPattern(NMatchType.MATCH, value);
    }

    public static NStringMatchResult ofPartialMatch(String value) {
        return new NStringMatchResultAsPartial(value);
    }

    public static NStringMatchResult ofFullMatch(Matcher value) {
        return new YesPattern(NMatchType.FULL_MATCH, value);
    }

    public static NStringMatchResult ofFullMatch(String value) {
        return new YesString(NMatchType.FULL_MATCH, value);
    }

    private NStringMatchResult(NMatchType mode) {
        this.mode = mode;
    }

    public NMatchType mode() {
        return mode;
    }

    public int count() {
        String s = get();
        return s == null ? 0 : s.length();
    }

    public abstract String get();

    public abstract String get(String name);

    private static class YesPattern extends NStringMatchResult {
        private Matcher value;

        public YesPattern(NMatchType mode, Matcher value) {
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

    private static class YesString extends NStringMatchResult {
        private String value;

        public YesString(NMatchType mode, String value) {
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

    private static class NStringMatchResultAsNoMatch extends NStringMatchResult {
        public NStringMatchResultAsNoMatch() {
            super(NMatchType.NO_MATCH);
        }

        public String get() {
            return null;
        }

        @Override
        public String get(String name) {
            return null;
        }
    }

    private static class NStringMatchResultAsPartial extends NStringMatchResult {
        private String value;

        public NStringMatchResultAsPartial(String value) {
            super(NMatchType.PARTIAL_MATCH);
            this.value = value;
        }

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
