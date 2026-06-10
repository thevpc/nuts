package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;

import java.util.Collections;
import java.util.Map;

public class NScoredIdMap {
    private final Map<String, NId> map;
    private final int score;

    public NScoredIdMap(Map<String, NId> map, int score) {
        this.map = Collections.unmodifiableMap(map);
        this.score = score;
    }

    public Map<String, NId> map() {
        return map;
    }

    public int score() {
        return score;
    }
}
