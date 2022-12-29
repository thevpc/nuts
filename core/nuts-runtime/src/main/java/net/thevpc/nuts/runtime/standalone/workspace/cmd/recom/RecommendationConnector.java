package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NSession;

import java.util.Map;

public interface RecommendationConnector {
    Map getRecommendations(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure, NSession session);
}
