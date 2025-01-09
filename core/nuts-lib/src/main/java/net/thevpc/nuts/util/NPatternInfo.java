package net.thevpc.nuts.util;

import java.util.function.Consumer;

class NPatternInfo implements Comparable<NPatternInfo> {
    private String pattern;
    private Consumer<NStringMatchResult> action;
    private Consumer<NStringMatchResult> fullMatchAction;
    private Consumer<NStringMatchResult> matchAction;
    private Consumer<NStringMatchResult> partialMatchAction;
    private NStringMatchResult result;

    public NPatternInfo(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public int compareTo(NPatternInfo b) {
        NPatternInfo a = this;
        int r = a.result.mode().compareTo(b.result.mode());
        if (r != 0) {
            return r;
        }
        switch (a.result.mode()) {
            case FULL_MATCH:
            case MATCH: {
                return -Integer.compare(a.result.get().length(), b.result.get().length());
            }
        }
        return 0;
    }
}
