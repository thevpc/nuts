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

    public String pattern() {
        return pattern;
    }

    public void pattern(String pattern) {
        this.pattern = pattern;
    }

    public Consumer<NStringMatchResult> action() {
        return action;
    }

    public void action(Consumer<NStringMatchResult> action) {
        this.action = action;
    }

    public Consumer<NStringMatchResult> fullMatchAction() {
        return fullMatchAction;
    }

    public void fullMatchAction(Consumer<NStringMatchResult> fullMatchAction) {
        this.fullMatchAction = fullMatchAction;
    }

    public Consumer<NStringMatchResult> matchAction() {
        return matchAction;
    }

    public void matchAction(Consumer<NStringMatchResult> matchAction) {
        this.matchAction = matchAction;
    }

    public Consumer<NStringMatchResult> partialMatchAction() {
        return partialMatchAction;
    }

    public void partialMatchAction(Consumer<NStringMatchResult> partialMatchAction) {
        this.partialMatchAction = partialMatchAction;
    }

    public NStringMatchResult result() {
        return result;
    }

    public void result(NStringMatchResult result) {
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
