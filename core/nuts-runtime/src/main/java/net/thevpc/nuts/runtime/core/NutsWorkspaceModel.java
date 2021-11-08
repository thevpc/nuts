package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationModel;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.core.log.DefaultNutsLogModel;
import net.thevpc.nuts.runtime.core.repos.DefaultNutsRepositoryModel;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.config.DefaultCustomCommandsModel;
import net.thevpc.nuts.runtime.standalone.config.DefaultImportModel;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceEnvManagerModel;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.repos.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNutsWorkspaceSecurityModel;

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
    public NutsVersion apiVersion;
    public NutsId apiId;
    public NutsId runtimeId;
    public DefaultNutsInstalledRepository installedRepository;
    public DefaultNutsLogModel logModel;
    public DefaultNutsWorkspaceEnvManagerModel envModel;
    public DefaultNutsWorkspaceExtensionModel extensionModel;
    public DefaultCustomCommandsModel aliasesModel;
    public DefaultImportModel importModel;

    public NutsWorkspaceModel(NutsWorkspace ws) {
        this.ws = ws;
    }
}
