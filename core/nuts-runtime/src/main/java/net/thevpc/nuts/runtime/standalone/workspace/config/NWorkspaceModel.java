package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreInMemory;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreOnDisk;
import net.thevpc.nuts.util.NLRUMap;
import net.thevpc.nuts.runtime.standalone.util.NPropertiesHolder;
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
import java.util.Stack;

public class NWorkspaceModel {
    public NWorkspace workspace;
    public InheritableThreadLocal<Stack<NSession>> sessionScopes = new InheritableThreadLocal<>();
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
    public NId apiId;
    public NId runtimeId;
    public DefaultNInstalledRepository installedRepository;
    public DefaultNLogModel logModel;
    public DefaultNWorkspaceEnvManagerModel envModel;
    public DefaultNPlatformModel sdkModel;
    public DefaultNWorkspaceExtensionModel extensionModel;
    public DefaultCustomCommandsModel commandModel;
    public DefaultImportModel importModel;
    public String apiDigest;
    public String installationDigest;
    public SafeRecommendationConnector recomm;
    public List<String> recommendedCompanions = new ArrayList<>();
    public NPropertiesHolder properties = new NPropertiesHolder();
    public NVersion askedApiVersion;
    public NId askedRuntimeId;
    public NBootOptions bOption0;
    public NLRUMap<NId, CachedSupplier<NDefinition>> cachedDefs = new NLRUMap<>(100);
    public DefaultNExtensions extensions;
    public NWorkspaceStore store;

    public NWorkspaceModel(NWorkspace workspace, NBootOptions bOption0) {
        this.workspace = workspace;
        if (bOption0.getIsolationLevel().orNull() == NIsolationLevel.MEMORY) {
            this.store = new NWorkspaceStoreInMemory(workspace);
        } else {
            this.store = new NWorkspaceStoreOnDisk(workspace);
        }
        this.recomm = new SafeRecommendationConnector(new SimpleRecommendationConnector(workspace));
        this.bOption0 = bOption0;
        // initialized here because they just do nothing...
        this.commandModel = new DefaultCustomCommandsModel(workspace);
        this.importModel = new DefaultImportModel(workspace);
        this.eventsModel = new DefaultNWorkspaceEventModel(workspace);
        this.repositoryModel = new DefaultNRepositoryModel(workspace);
        this.extensions = new DefaultNExtensions(this);
        this.bootModel = new DefaultNBootModel(workspace, this, bOption0);
    }

    public void init() {
        askedApiVersion = bOption0.getApiVersion().orNull();
        askedRuntimeId = bOption0.getRuntimeId().orNull();
        if (askedRuntimeId == null) {
            askedRuntimeId = NId.getRuntime("").get();
        }

        this.textModel = new DefaultNTextManagerModel(workspace);
        this.apiId = NId.getApi(Nuts.getVersion()).get();
        this.runtimeId = NId.get(
                askedRuntimeId.getGroupId(),
                askedRuntimeId.getArtifactId(),
                NVersion.get(askedRuntimeId.getVersion().toString()).get()).get();
        this.bootModel.init();
    }
}
