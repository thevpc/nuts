package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public class SafeRecommendationConnector implements RecommendationConnector{
    private RecommendationConnector c;

    public SafeRecommendationConnector(RecommendationConnector c) {
        this.c = c;
    }

    @Override
    public Map askInstallRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askInstallRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askUpdateRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askUpdateRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askCompanionsRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askCompanionsRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askInstallFailureRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askInstallFailureRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askUninstallFailureRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askUninstallFailureRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askBootstrapFailureRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askBootstrapFailureRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askUpdateFailureRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askUpdateFailureRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public Map askUninstallRecommendations(RequestQueryInfo ri, NutsSession session) {
        try {
            return c.askUninstallRecommendations(ri, session);
        }catch (Exception ex){
            return null;
        }
    }
}
