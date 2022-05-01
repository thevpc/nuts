package net.thevpc.nuts.runtime.standalone.util.collections;

import java.util.regex.Matcher;

public abstract class NutsStringMatchResult {
    private static final NutsStringMatchResult NO_MATCH = new NutsStringMatchResultAsNoMatch();
    private NutsMatchType mode;

    public static NutsStringMatchResult ofNoMatch() {
        return NO_MATCH;
    }

    public static NutsStringMatchResult ofMatch(Matcher value) {
        return new YesPattern(NutsMatchType.MATCH, value);
    }

    public static NutsStringMatchResult ofPartialMatch(String value) {
        return new NutsStringMatchResultAsPartial(value);
    }

    public static NutsStringMatchResult ofFullMatch(Matcher value) {
        return new YesPattern(NutsMatchType.FULL_MATCH, value);
    }

    public static NutsStringMatchResult ofFullMatch(String value) {
        return new YesString(NutsMatchType.FULL_MATCH, value);
    }

    private NutsStringMatchResult(NutsMatchType mode) {
        this.mode = mode;
    }

    public NutsMatchType mode() {
        return mode;
    }

    public int count() {
        String s = get();
        return s == null ? 0 : s.length();
    }

    public abstract String get();

    public abstract String get(String name);

    private static class YesPattern extends NutsStringMatchResult {
        private Matcher value;

        public YesPattern(NutsMatchType mode, Matcher value) {
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

    private static class YesString extends NutsStringMatchResult {
        private String value;

        public YesString(NutsMatchType mode, String value) {
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

    private static class NutsStringMatchResultAsNoMatch extends NutsStringMatchResult {
        public NutsStringMatchResultAsNoMatch() {
            super(NutsMatchType.NO_MATCH);
        }

        public String get() {
            return null;
        }

        @Override
        public String get(String name) {
            return null;
        }
    }

    private static class NutsStringMatchResultAsPartial extends NutsStringMatchResult {
        private String value;

        public NutsStringMatchResultAsPartial(String value) {
            super(NutsMatchType.PARTIAL_MATCH);
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
        if (mode == NutsMatchType.FULL_MATCH
                || mode == NutsMatchType.MATCH
                || mode == NutsMatchType.PARTIAL_MATCH
        ) {
            sb.append("(");
            sb.append(get());
            sb.append(")");
        }
        return sb.toString();
    }
}
