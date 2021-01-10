/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsDependencyManager;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsDescriptorManager;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsIdManager;
import net.thevpc.nuts.runtime.standalone.main.wscommands.DefaultNutsUpdateStatisticsCommand;
import net.thevpc.nuts.runtime.core.app.DefaultNutsCommandLineManager;
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationManager;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.core.format.DefaultNutsInfoFormat;
import net.thevpc.nuts.runtime.standalone.manager.DefaultNutsVersionManager;

import java.util.Map;

/**
 * Created by vpc on 1/6/17.
 */
public abstract class AbstractNutsWorkspace implements NutsWorkspace {

    protected NutsIOManager ioManager;
    protected boolean initializing;
    protected NutsWorkspaceSecurityManager securityManager;
    protected NutsFilterManager filters;
    protected NutsWorkspaceConfigManagerExt configManager;
    protected NutsRepositoryManager repositoryManager;
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    protected DefaultNutsWorkspaceEventManager events;
    private DefaultNutsIdManager defaultNutsIdManager;
    private DefaultNutsVersionManager defaultVersionManager;
    private DefaultNutsDescriptorManager defaultDescriptorManager;
    private DefaultNutsDependencyManager defaultDependencyManager;
    private DefaultNutsFormatManager formatManager;
    private DefaultNutsConcurrentManager concurrent;
    private DefaultNutsWorkspaceAppsManager apps;
    private DefaultNutsCommandLineManager defaultNutsCommandLineManager;
    private NutsWorkspaceLocationManager locationManager;
    protected NutsSession bootSession;

    public AbstractNutsWorkspace(NutsWorkspaceInitInformation info) {
        defaultNutsIdManager = new DefaultNutsIdManager(this);
        defaultVersionManager = new DefaultNutsVersionManager(this);
        defaultDescriptorManager = new DefaultNutsDescriptorManager(this);
        defaultDependencyManager = new DefaultNutsDependencyManager(this);
        formatManager = new DefaultNutsFormatManager(this);
        concurrent = new DefaultNutsConcurrentManager(this);
        apps=new DefaultNutsWorkspaceAppsManager(this);
        defaultNutsCommandLineManager = new DefaultNutsCommandLineManager(this);
        locationManager=new DefaultNutsWorkspaceLocationManager(this,info);
    }

    @Override
    public NutsWorkspaceLocationManager locations() {
        return locationManager;
    }

    @Override
    public NutsWorkspaceAppsManager apps() {
        return apps;
    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String getName() {
        return config().getName();
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return extensionManager;
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return configManager;
    }

    @Override
    public NutsRepositoryManager repos() {
        return repositoryManager;
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return securityManager;
    }

    @Override
    public NutsIOManager io() {
        return ioManager;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(io().term().createTerminal(io().term().getSystemTerminal(), bootSession));
        nutsSession.setExpireTime(config().options().getExpireTime());
        return nutsSession;
    }

    @Override
    public NutsWorkspaceEventManager events() {
        if (events == null) {
            events = new DefaultNutsWorkspaceEventManager(this);
        }
        return events;
    }

    @Override
    public NutsCommandLineManager commandLine() {
        return defaultNutsCommandLineManager;
    }

    @Override
    public NutsIdManager id() {
        return defaultNutsIdManager;
    }

    @Override
    public NutsVersionManager version() {
        return defaultVersionManager;
    }

    @Override
    public NutsInfoFormat info() {
        return new DefaultNutsInfoFormat(this);
    }

    @Override
    public NutsDescriptorManager descriptor() {
        return defaultDescriptorManager;
    }

    @Override
    public NutsDependencyManager dependency() {
        return defaultDependencyManager;
    }

    @Override
    public NutsFormatManager formats() {
        return formatManager;
    }

    @Override
    public NutsConcurrentManager concurrent() {
        return concurrent;
    }

    @Override
    public String getApiVersion() {
        return NutsWorkspaceConfigManagerExt.of(config()).getApiVersion();
    }

    @Override
    public NutsId getApiId() {
        return NutsWorkspaceConfigManagerExt.of(config()).getApiId();
    }

    @Override
    public NutsId getRuntimeId() {
        return NutsWorkspaceConfigManagerExt.of(config()).getRuntimeId();
    }

    @Override
    public NutsSdkManager sdks() {
        return NutsWorkspaceConfigManagerExt.of(config()).sdks();
    }

    @Override
    public NutsImportManager imports() {
        return NutsWorkspaceConfigManagerExt.of(config()).imports();
    }

    @Override
    public NutsCommandAliasManager aliases() {
        return NutsWorkspaceConfigManagerExt.of(config()).aliases();
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        return NutsWorkspaceConfigManagerExt.of(config()).env();
    }

    public boolean isInitializing() {
        return initializing;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

}
