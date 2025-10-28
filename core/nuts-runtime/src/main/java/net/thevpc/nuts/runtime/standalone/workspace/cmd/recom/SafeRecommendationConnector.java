package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import java.util.Map;

public class SafeRecommendationConnector implements RecommendationConnector {
    private final RecommendationConnector c;

    public SafeRecommendationConnector(RecommendationConnector c) {
        this.c = c;
    }

    public void trackRecommendationsAsync(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure) {
        NWorkspace.of().runWith(() -> {
            new Thread(() -> {
                try {
                    Map rec = null;
                    rec = NWorkspaceExt.of().getModel().recomm.getRecommendations(ri, phase, failure);
                } catch (Exception ex3) {
                    //just ignore
                }
            }).start();
        });
    }
    @Override
    public Map getRecommendations(RequestQueryInfo ri, NRecommendationPhase phase, boolean failure) {
        try {
            return c.getRecommendations(ri, phase, failure);
        } catch (Exception ex) {
            return null;
        }
    }

}
