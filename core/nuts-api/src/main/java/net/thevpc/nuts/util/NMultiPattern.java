package net.thevpc.nuts.util;

import net.thevpc.nuts.collections.NCollections;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NMultiPattern {
    private LinkedHashMap<String, NPatternInfo> map = new LinkedHashMap<>();
    private boolean fully;
    private Runnable noMatch;
    private Consumer<NStringMatchResult> match;
    private Consumer<NStringMatchResult> fullMatch;
    private Consumer<NStringMatchResult> partialMatch;

    public NMultiPattern onMatch(String pattern, Consumer<NStringMatchResult> action) {
        return on(pattern, true, action, NMatchType.MATCH);
    }

    public NMultiPattern onMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        return on(pattern, condition, action, NMatchType.MATCH);
    }

    public NMultiPattern onPartialMatch(String pattern, Consumer<NStringMatchResult> action) {
        return on(pattern, true, action, NMatchType.PARTIAL_MATCH);
    }

    public NMultiPattern onPartialMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        return on(pattern, condition, action, NMatchType.PARTIAL_MATCH);
    }

    public NMultiPattern onFullMatch(String pattern, Consumer<NStringMatchResult> action) {
        return on(pattern, true, action, NMatchType.FULL_MATCH);
    }

    public NMultiPattern onFullMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        return on(pattern, condition, action, NMatchType.FULL_MATCH);
    }

    public NMultiPattern on(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
        return on(pattern, condition, action, null);
    }


    public NMultiPattern on(String pattern, Consumer<NStringMatchResult> action) {
        return on(pattern, true, action, null);
    }

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
                    throw new IllegalArgumentException("unsupported");
                }
            }
        }
        return this;
    }

    public boolean isFully() {
        return fully;
    }

    public NMultiPattern fully() {
        return setFully(true);
    }

    public NMultiPattern setFully(boolean fully) {
        this.fully = fully;
        return this;
    }

    public NMultiPattern onNoMatch(Runnable noMatch) {
        this.noMatch = noMatch;
        return this;
    }

    public NMultiPattern onMatch(Consumer<NStringMatchResult> match) {
        this.match = match;
        return this;
    }

    public NMultiPattern onPartialMatch(Consumer<NStringMatchResult> partialMatch) {
        this.partialMatch = partialMatch;
        return this;
    }

    public Runnable noMatch() {
        return noMatch;
    }

    public Consumer<NStringMatchResult> match() {
        return match;
    }

    public Consumer<NStringMatchResult> fullMatch() {
        return fullMatch;
    }

    public Consumer<NStringMatchResult> partialMatch() {
        return partialMatch;
    }

    public Map<String, NPatternInfo> map() {
        return NCollections.unmodifiableMap(map);
    }
}
