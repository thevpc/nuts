package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public interface RecommendationConnector {
    Map askInstallRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session);
    Map askUpdateRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session);

    Map askExecRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session);

    Map askBootstrapRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session);
    Map askUninstallRecommendations(RequestQueryInfo ri, boolean failure, NutsSession session);

}
