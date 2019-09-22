/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.app.DefaultNutsCommandLine;
import net.vpc.app.nuts.core.app.DefaultNutsCommandLineFormat;
import net.vpc.app.nuts.core.format.*;
import net.vpc.app.nuts.core.format.elem.DefaultNutsElementFormat;
import net.vpc.app.nuts.core.format.json.DefaultNutsJsonFormat;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.table.DefaultTableFormat;
import net.vpc.app.nuts.core.format.tree.DefaultTreeFormat;
import net.vpc.app.nuts.core.format.xml.DefaultNutsXmlFormat;
import net.vpc.app.nuts.core.impl.def.wscommands.*;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.common.DefaultObservableMap;
import net.vpc.app.nuts.core.util.common.ObservableMap;

import java.util.*;

/**
 * Created by vpc on 1/6/17.
 */
public abstract class AbstractNutsWorkspace implements NutsWorkspace {
    protected final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    protected final List<NutsInstallListener> installListeners = new ArrayList<>();
    protected final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    protected NutsIOManager ioManager;
    protected boolean initializing;
    protected NutsWorkspaceSecurityManager securityManager;
    protected NutsWorkspaceConfigManagerExt configManager;
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    protected ObservableMap<String, Object> userProperties;

    public AbstractNutsWorkspace() {
        userProperties = new DefaultObservableMap<>();
    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String uuid() {
        return getUuid();
    }

    @Override
    public Map<String, Object> userProperties() {
        return userProperties;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession(this);
        nutsSession.setTerminal(io().createTerminal(io().getSystemTerminal()));
        return nutsSession;
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return configManager;
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return securityManager;
    }

    public boolean isInitializing() {
        return initializing;
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[0]);
    }

    @Override
    public void removeInstallListener(NutsInstallListener listener) {
        installListeners.remove(listener);
    }

    @Override
    public void addInstallListener(NutsInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    @Override
    public NutsInstallListener[] getInstallListeners() {
        return installListeners.toArray(new NutsInstallListener[0]);
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return extensionManager;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsCommandLineFormat commandLine() {
        return new DefaultNutsCommandLineFormat(this);
    }

    @Override
    public NutsIOManager io() {
        return ioManager;
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    @Override
    public NutsJsonFormat json() {
        return new DefaultNutsJsonFormat(this);
    }

    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(this);
    }

    @Override
    public NutsXmlFormat xml() {
        return new DefaultNutsXmlFormat(this);
    }

    @Override
    public NutsIdFormat id() {
        return new DefaultNutsIdFormat(this);
    }

    @Override
    public NutsStringFormat str() {
        return new DefaultNutsStringFormat(this);
    }

    @Override
    public NutsVersionFormat version() {
        return new DefaultVersionFormat(this);
    }

    @Override
    public NutsInfoFormat info() {
        return new DefaultNutsInfoFormat(this);
    }

    @Override
    public NutsDescriptorFormat descriptor() {
        return new DefaultNutsDescriptorFormat(this);
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(this);
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(this);
    }

    @Override
    public NutsPropertiesFormat props() {
        return new DefaultPropertiesFormat(this);
    }

    @Override
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(this);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(this);
    }

    @Override
    public NutsDependencyFormat dependency() {
        return new DefaultNutsDependencyFormat(this);
    }
}
