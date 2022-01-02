package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.NutsSession;

import java.util.Map;

public interface RecommendationConnector {
    Map askInstallRecommendations(RequestQueryInfo ri, NutsSession session);
    Map askUpdateRecommendations(RequestQueryInfo ri, NutsSession session);
    Map askCompanionsRecommendations(RequestQueryInfo ri, NutsSession session);

    Map askInstallFailureRecommendations(RequestQueryInfo ri, NutsSession session);

    Map askUninstallFailureRecommendations(RequestQueryInfo ri, NutsSession session);

    Map askBootstrapFailureRecommendations(RequestQueryInfo ri, NutsSession session);

    Map askUpdateFailureRecommendations(RequestQueryInfo ri, NutsSession session);

    Map askUninstallRecommendations(RequestQueryInfo ri, NutsSession session);
}
