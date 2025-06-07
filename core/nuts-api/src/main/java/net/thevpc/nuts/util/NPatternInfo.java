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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Consumer<NStringMatchResult> getAction() {
        return action;
    }

    public void setAction(Consumer<NStringMatchResult> action) {
        this.action = action;
    }

    public Consumer<NStringMatchResult> getFullMatchAction() {
        return fullMatchAction;
    }

    public void setFullMatchAction(Consumer<NStringMatchResult> fullMatchAction) {
        this.fullMatchAction = fullMatchAction;
    }

    public Consumer<NStringMatchResult> getMatchAction() {
        return matchAction;
    }

    public void setMatchAction(Consumer<NStringMatchResult> matchAction) {
        this.matchAction = matchAction;
    }

    public Consumer<NStringMatchResult> getPartialMatchAction() {
        return partialMatchAction;
    }

    public void setPartialMatchAction(Consumer<NStringMatchResult> partialMatchAction) {
        this.partialMatchAction = partialMatchAction;
    }

    public NStringMatchResult getResult() {
        return result;
    }

    public void setResult(NStringMatchResult result) {
        this.result = result;
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
