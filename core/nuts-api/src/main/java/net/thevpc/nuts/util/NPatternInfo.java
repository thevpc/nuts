package net.thevpc.nuts.util;

import java.util.function.Consumer;

/**
 * NPatternInfo class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPatternInfo implements Comparable<NPatternInfo> {
    private String pattern;
    private Consumer<NStringMatchResult> action;
    private Consumer<NStringMatchResult> fullMatchAction;
    private Consumer<NStringMatchResult> matchAction;
    private Consumer<NStringMatchResult> partialMatchAction;
    private NStringMatchResult result;

    /**
     * N pattern info.
     *
     * @param pattern pattern
     * @return n pattern info result
     */
    public NPatternInfo(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Pattern.
     *
     * @return pattern result
     */
    public String pattern() {
        return pattern;
    }

    /**
     * Pattern.
     *
     * @param pattern pattern
     */
    public void pattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Action.
     *
     * @return action result
     */
    public Consumer<NStringMatchResult> action() {
        return action;
    }

    /**
     * Action.
     *
     * @param action action
     */
    public void action(Consumer<NStringMatchResult> action) {
        this.action = action;
    }

    /**
     * Full match action.
     *
     * @return full match action result
     */
    public Consumer<NStringMatchResult> fullMatchAction() {
        return fullMatchAction;
    }

    /**
     * Full match action.
     *
     * @param fullMatchAction full match action
     */
    public void fullMatchAction(Consumer<NStringMatchResult> fullMatchAction) {
        this.fullMatchAction = fullMatchAction;
    }

    /**
     * Match action.
     *
     * @return match action result
     */
    public Consumer<NStringMatchResult> matchAction() {
        return matchAction;
    }

    /**
     * Match action.
     *
     * @param matchAction match action
     */
    public void matchAction(Consumer<NStringMatchResult> matchAction) {
        this.matchAction = matchAction;
    }

    /**
     * Partial match action.
     *
     * @return partial match action result
     */
    public Consumer<NStringMatchResult> partialMatchAction() {
        return partialMatchAction;
    }

    /**
     * Partial match action.
     *
     * @param partialMatchAction partial match action
     */
    public void partialMatchAction(Consumer<NStringMatchResult> partialMatchAction) {
        this.partialMatchAction = partialMatchAction;
    }

    /**
     * Result.
     *
     * @return result result
     */
    public NStringMatchResult result() {
        return result;
    }

    /**
     * Result.
     *
     * @param result result
     */
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
