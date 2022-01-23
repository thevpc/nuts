package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public interface RecommendationConnector {
    Map getRecommendations(RequestQueryInfo ri, NutsRecommendationPhase phase, boolean failure, NutsSession session);
}
