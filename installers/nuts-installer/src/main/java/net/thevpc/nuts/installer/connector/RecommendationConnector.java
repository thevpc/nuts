package net.thevpc.nuts.installer.connector;

import java.util.Map;

public interface RecommendationConnector {
    Map askInstallRecommendations(RequestQueryInfo ri);
    Map askDescriptor(RequestQueryInfo ri);
    Map askUpdateRecommendations(RequestQueryInfo ri);
    Map askCompanionsRecommendations(RequestQueryInfo ri);

    Map askInstallFailureRecommendations(RequestQueryInfo ri);

    Map askUninstallFailureRecommendations(RequestQueryInfo ri);

    Map askBootstrapFailureRecommendations(RequestQueryInfo ri);

    Map askUpdateFailureRecommendations(RequestQueryInfo ri);

    Map askUninstallRecommendations(RequestQueryInfo ri);
}
