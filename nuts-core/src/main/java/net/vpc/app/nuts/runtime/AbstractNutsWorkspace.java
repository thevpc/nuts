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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.common.DefaultObservableMap;
import net.vpc.app.nuts.core.common.ObservableMap;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.main.wscommands.DefaultNutsUpdateStatisticsCommand;
import net.vpc.app.nuts.runtime.app.DefaultNutsCommandLineFormat;
import net.vpc.app.nuts.runtime.app.DefaultNutsCommandLineManager;
import net.vpc.app.nuts.runtime.ext.DefaultNutsWorkspaceExtensionManager;
import net.vpc.app.nuts.runtime.format.DefaultNutsInfoFormat;
import net.vpc.app.nuts.runtime.io.DefaultNutsMonitorAction;
import net.vpc.app.nuts.runtime.manager.DefaultNutsDependencyManager;
import net.vpc.app.nuts.runtime.manager.DefaultNutsDescriptorManager;
import net.vpc.app.nuts.runtime.manager.DefaultNutsIdManager;
import net.vpc.app.nuts.runtime.manager.DefaultNutsVersionManager;

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
    protected ObservableMap<String, Object> userProperties;
    protected DefaultNutsWorkspaceEvents events;
    private DefaultNutsIdManager defaultNutsIdManager;
    private DefaultNutsVersionManager defaultVersionManager;
    private DefaultNutsDescriptorManager defaultDescriptorManager;
    private DefaultNutsDependencyManager defaultDependencyManager;
    private DefaultNutsFormatManager formatManager;
    private DefaultNutsConcurrentManager concurrent;
    private DefaultNutsWorkspaceAppsManager apps;
    private DefaultNutsCommandLineManager defaultNutsCommandLineManager;

    public AbstractNutsWorkspace() {
        userProperties = new DefaultObservableMap<>();
        defaultNutsIdManager = new DefaultNutsIdManager(this);
        defaultVersionManager = new DefaultNutsVersionManager(this);
        defaultDescriptorManager = new DefaultNutsDescriptorManager(this);
        defaultDependencyManager = new DefaultNutsDependencyManager(this);
        formatManager = new DefaultNutsFormatManager(this);
        concurrent = new DefaultNutsConcurrentManager(this);
        apps=new DefaultNutsWorkspaceAppsManager(this);
        defaultNutsCommandLineManager = new DefaultNutsCommandLineManager(this);
    }

    @Override
    public NutsWorkspaceAppsManager apps() {
        return apps;
    }

    @Override
    public String uuid() {
        return config().getUuid();
    }

    @Override
    public String name() {
        return config().getName();
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    @Override
    public Map<String, Object> userProperties() {
        return userProperties;
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
        nutsSession.setTerminal(io().term().createTerminal(io().term().getSystemTerminal()));
        nutsSession.setExpireTime(config().options().getExpireTime());
        return nutsSession;
    }

    @Override
    public NutsWorkspaceEvents events() {
        if (events == null) {
            events = new DefaultNutsWorkspaceEvents(this);
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
