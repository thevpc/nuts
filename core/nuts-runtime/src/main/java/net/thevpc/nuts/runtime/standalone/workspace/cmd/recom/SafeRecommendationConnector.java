package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public class SafeRecommendationConnector implements RecommendationConnector {
    private final RecommendationConnector c;

    public SafeRecommendationConnector(RecommendationConnector c) {
        this.c = c;
    }

    @Override
    public Map getRecommendations(RequestQueryInfo ri, NutsRecommendationPhase phase, boolean failure, NutsSession session) {
        try {
            return c.getRecommendations(ri, phase, failure, session);
        } catch (Exception ex) {
            return null;
        }
    }

}
