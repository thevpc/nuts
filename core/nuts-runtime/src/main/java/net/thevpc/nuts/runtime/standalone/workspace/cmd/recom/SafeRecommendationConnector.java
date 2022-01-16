package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public class SafeRecommendationConnector implements RecommendationConnector{
    private RecommendationConnector c;

    public SafeRecommendationConnector(RecommendationConnector c) {
        this.c = c;
    }

    @Override
    public Map askInstallRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session) {
        try {
            return c.askInstallRecommendations(ri, failure, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askUpdateRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session) {
        try {
            return c.askUpdateRecommendations(ri, failure, session);
        }catch (Exception ex){
            return null;
        }
    }
    @Override
    public Map askExecRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session) {
        try {
            return c.askExecRecommendations(ri, failure, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askBootstrapRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session) {
        try {
            return c.askBootstrapRecommendations(ri, failure, session);
        }catch (Exception ex){
            return null;
        }
    }
    @Override
    public Map askUninstallRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session) {
        try {
            return c.askUninstallRecommendations(ri, failure, session);
        }catch (Exception ex){
            return null;
        }
    }
}
