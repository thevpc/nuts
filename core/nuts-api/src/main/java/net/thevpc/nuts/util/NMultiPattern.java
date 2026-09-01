package net.thevpc.nuts.util;

import net.thevpc.nuts.collections.NMaps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * NMultiPattern class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMultiPattern {
    private LinkedHashMap<String, NPatternInfo> map = new LinkedHashMap<>();
    private boolean fully;
    private Runnable noMatch;
    private Consumer<NStringMatchResult> match;
    private Consumer<NStringMatchResult> fullMatch;
    private Consumer<NStringMatchResult> partialMatch;

    /**
     * On match.
     *
     * @param pattern pattern
     * @param action action
     * @return on match result
     */
    public NMultiPattern onMatch(String pattern, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param true true
         * @param action action
         * @param NMatchType.MATCH n match type.match
         * @return on result
         */
        return on(pattern, true, action, NMatchType.MATCH);
    }

    /**
     * On match.
     *
     * @param pattern pattern
     * @param condition condition
     * @param action action
     * @return on match result
     */
    public NMultiPattern onMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param condition condition
         * @param action action
         * @param NMatchType.MATCH n match type.match
         * @return on result
         */
        return on(pattern, condition, action, NMatchType.MATCH);
    }

    /**
     * On partial match.
     *
     * @param pattern pattern
     * @param action action
     * @return on partial match result
     */
    public NMultiPattern onPartialMatch(String pattern, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param true true
         * @param action action
         * @param NMatchType.PARTIAL_MATCH n match type.partial_match
         * @return on result
         */
        return on(pattern, true, action, NMatchType.PARTIAL_MATCH);
    }

    /**
     * On partial match.
     *
     * @param pattern pattern
     * @param condition condition
     * @param action action
     * @return on partial match result
     */
    public NMultiPattern onPartialMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param condition condition
         * @param action action
         * @param NMatchType.PARTIAL_MATCH n match type.partial_match
         * @return on result
         */
        return on(pattern, condition, action, NMatchType.PARTIAL_MATCH);
    }

    /**
     * On full match.
     *
     * @param pattern pattern
     * @param action action
     * @return on full match result
     */
    public NMultiPattern onFullMatch(String pattern, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param true true
         * @param action action
         * @param NMatchType.FULL_MATCH n match type.full_match
         * @return on result
         */
        return on(pattern, true, action, NMatchType.FULL_MATCH);
    }

    /**
     * On full match.
     *
     * @param pattern pattern
     * @param condition condition
     * @param action action
     * @return on full match result
     */
    public NMultiPattern onFullMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param condition condition
         * @param action action
         * @param NMatchType.FULL_MATCH n match type.full_match
         * @return on result
         */
        return on(pattern, condition, action, NMatchType.FULL_MATCH);
    }

    /**
     * On.
     *
     * @param pattern pattern
     * @param condition condition
     * @param action action
     * @return on result
     */
    public NMultiPattern on(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param condition condition
         * @param action action
         * @param null null
         * @return on result
         */
        return on(pattern, condition, action, null);
    }


    /**
     * On.
     *
     * @param pattern pattern
     * @param action action
     * @return on result
     */
    public NMultiPattern on(String pattern, Consumer<NStringMatchResult> action) {
        /**
         * On.
         *
         * @param pattern pattern
         * @param true true
         * @param action action
         * @param null null
         * @return on result
         */
        return on(pattern, true, action, null);
    }

    /**
     * On.
     *
     * @param pattern pattern
     * @param condition condition
     * @param action action
     * @param matchType match type
     * @return on result
     */
    public NMultiPattern on(String pattern, boolean condition, Consumer<NStringMatchResult> action, NMatchType matchType) {
        if (action == null) {
            return this;
        }
        if (!condition) {
            return this;
        }
        NPatternInfo nfo = map.get(pattern);
        if (nfo == null) {
            nfo = new NPatternInfo(pattern);
            map.put(pattern, nfo);
        }
        if (matchType == null) {
            nfo.action(action);
        } else {
            switch (matchType) {
                case FULL_MATCH: {
                    nfo.fullMatchAction(action);
                    break;
                }
                case MATCH: {
                    nfo.matchAction(action);
                    break;
                }
                case PARTIAL_MATCH: {
                    nfo.partialMatchAction(action);
                    break;
                }
                case NO_MATCH: {
                    /**
                     * Illegal argument exception.
                     *
                     * @param "unsupported" "unsupported"
                     * @return illegal argument exception result
                     */
                    throw new IllegalArgumentException("unsupported");
                }
            }
        }
        return this;
    }

    /**
     * Checks if is fully.
     *
     * @return is fully result
     */
    public boolean isFully() {
        return fully;
    }

    /**
     * Fully.
     *
     * @return fully result
     */
    public NMultiPattern fully() {
        /**
         * Sets the fully.
         *
         * @param true true
         * @return set fully result
         */
        return setFully(true);
    }

    /**
     * Sets the fully.
     *
     * @param fully fully
     * @return set fully result
     */
    public NMultiPattern setFully(boolean fully) {
        this.fully = fully;
        return this;
    }

    /**
     * On no match.
     *
     * @param noMatch no match
     * @return on no match result
     */
    public NMultiPattern onNoMatch(Runnable noMatch) {
        this.noMatch = noMatch;
        return this;
    }

    /**
     * On match.
     *
     * @param match match
     * @return on match result
     */
    public NMultiPattern onMatch(Consumer<NStringMatchResult> match) {
        this.match = match;
        return this;
    }

    /**
     * On partial match.
     *
     * @param partialMatch partial match
     * @return on partial match result
     */
    public NMultiPattern onPartialMatch(Consumer<NStringMatchResult> partialMatch) {
        this.partialMatch = partialMatch;
        return this;
    }

    /**
     * No match.
     *
     * @return no match result
     */
    public Runnable noMatch() {
        return noMatch;
    }

    /**
     * Match.
     *
     * @return match result
     */
    public Consumer<NStringMatchResult> match() {
        return match;
    }

    /**
     * Full match.
     *
     * @return full match result
     */
    public Consumer<NStringMatchResult> fullMatch() {
        return fullMatch;
    }

    /**
     * Partial match.
     *
     * @return partial match result
     */
    public Consumer<NStringMatchResult> partialMatch() {
        return partialMatch;
    }

    /**
     * Map.
     *
     * @return map result
     */
    public Map<String, NPatternInfo> map() {
        return NMaps.unmodifiableMap(map);
    }
}
