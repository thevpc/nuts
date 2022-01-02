package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.event.DefaultNutsWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNutsFilterModel;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.standalone.log.DefaultNutsLogModel;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SafeRecommendationConnector;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SimpleRecommendationConnector;

import java.util.ArrayList;
import java.util.List;

public class NutsWorkspaceModel {
    public NutsWorkspace ws;
    public NutsSession initSession;
    public DefaultNutsBootModel bootModel;
    public DefaultNutsWorkspaceSecurityModel securityModel;
    public DefaultNutsFilterModel filtersModel;
    public DefaultNutsWorkspaceConfigModel configModel;
    public DefaultNutsWorkspaceLocationModel locationsModel;
    public DefaultNutsRepositoryModel repositoryModel;
    public DefaultNutsWorkspaceEventModel eventsModel;
    public DefaultNutsTextManagerModel textModel;
    public String uuid;
    public String location;
    public String name;
    public String hashName;
    public NutsVersion apiVersion;
    public NutsId apiId;
    public NutsId runtimeId;
    public DefaultNutsInstalledRepository installedRepository;
    public DefaultNutsLogModel logModel;
    public DefaultNutsWorkspaceEnvManagerModel envModel;
    public DefaultNutsWorkspaceExtensionModel extensionModel;
    public DefaultCustomCommandsModel aliasesModel;
    public DefaultImportModel importModel;
    public String apiDigest;
    public SafeRecommendationConnector recomm =new SafeRecommendationConnector(new SimpleRecommendationConnector());
    public List<String> recommendedCompanions=new ArrayList<>();

    public NutsWorkspaceModel(NutsWorkspace ws) {
        this.ws = ws;
    }
}
