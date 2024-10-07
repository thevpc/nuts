package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootOptions;
import net.thevpc.nuts.runtime.standalone.NWsConfDB;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.lib.common.collections.LRUMap;
import net.thevpc.nuts.lib.common.collections.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNFilterModel;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogModel;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SafeRecommendationConnector;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SimpleRecommendationConnector;

import java.util.ArrayList;
import java.util.List;

public class NWorkspaceModel {
    public NWorkspace ws;
    public NSession initSession;
    public DefaultNBootModel bootModel;
    public DefaultNWorkspaceSecurityModel securityModel;
    public DefaultNFilterModel filtersModel;
    public DefaultNWorkspaceConfigModel configModel;
    public DefaultNWorkspaceLocationModel locationsModel;
    public DefaultNRepositoryModel repositoryModel;
    public DefaultNWorkspaceEventModel eventsModel;
    public DefaultNTextManagerModel textModel;
    public String uuid;
    public String location;
    public String name;
    public String hashName;
    public NVersion apiVersion;
    public NId apiId;
    public NId runtimeId;
    public DefaultNInstalledRepository installedRepository;
    public DefaultNLogModel logModel;
    public DefaultNWorkspaceEnvManagerModel envModel;
    public DefaultNPlatformModel sdkModel;
    public DefaultNWorkspaceExtensionModel extensionModel;
    public DefaultCustomCommandsModel aliasesModel;
    public DefaultImportModel importModel;
    public String apiDigest;
    public String installationDigest;
    public SafeRecommendationConnector recomm =new SafeRecommendationConnector(new SimpleRecommendationConnector());
    public List<String> recommendedCompanions=new ArrayList<>();
    public NPropertiesHolder properties = new NPropertiesHolder();
    public NVersion askedApiVersion;
    public NId askedRuntimeId;
    public NBootOptions bOption0;
    public NWsConfDB confDB=new NWsConfDB();
    public LRUMap<NId, CachedSupplier<NDefinition>> cachedDefs=new LRUMap<>(100);

    public NWorkspaceModel(NWorkspace ws, NBootOptions bOption0) {
        this.ws = ws;
        this.bOption0 = bOption0;
        // initialized here because they just do nothing...
        this.aliasesModel = new DefaultCustomCommandsModel(ws);
        this.importModel = new DefaultImportModel(ws);
        this.eventsModel = new DefaultNWorkspaceEventModel(ws);
        this.repositoryModel = new DefaultNRepositoryModel(ws);
    }

    public void init(){
        askedApiVersion = bOption0.getApiVersion().orNull();
        askedRuntimeId = bOption0.getRuntimeId().orNull();
        if (askedRuntimeId == null) {
            askedRuntimeId = NId.ofRuntime("").get();
        }

        this.textModel = new DefaultNTextManagerModel(ws);
        this.apiVersion = Nuts.getVersion();
        this.apiId = NId.ofApi(this.apiVersion).get();
        this.runtimeId = NId.of(
                askedRuntimeId.getGroupId(),
                askedRuntimeId.getArtifactId(),
                NVersion.of(askedRuntimeId.getVersion().toString()).get()).get();
        this.bootModel = new DefaultNBootModel(ws,this);
        this.bootModel.init(bOption0);
    }
}
