/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsHttpServerConfig;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsServer;
import net.vpc.app.nuts.NutsServerComponent;
import net.vpc.app.nuts.NutsWorkspaceServerManager;
import net.vpc.app.nuts.ServerConfig;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceServerManager implements NutsWorkspaceServerManager {

    private Map<String, NutsServer> servers = new HashMap<>();
    private final DefaultNutsWorkspace ws;

    protected DefaultNutsWorkspaceServerManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsServer startServer(ServerConfig serverConfig) {
        if (serverConfig == null) {
            serverConfig = new NutsHttpServerConfig();
        }
        NutsServerComponent server = ws.getExtensionManager().getFactory().createSupported(NutsServerComponent.class, serverConfig);
        if (server == null) {
            throw new NutsIllegalArgumentException("Not server extensions are registered.");
        }
        NutsServer s = server.start(ws.self(), serverConfig);
        if (servers.get(s.getServerId()) != null) {
            servers.get(s.getServerId()).stop();
        }
        servers.put(s.getServerId(), s);
        return s;
    }

    @Override
    public NutsServer getServer(String serverId) {
        NutsServer nutsServer = servers.get(serverId);
        if (nutsServer == null) {
            throw new NutsIllegalArgumentException("Server not found " + serverId);
        }
        return nutsServer;
    }

    @Override
    public void stopServer(String serverId) {
        getServer(serverId).stop();
    }

    @Override
    public boolean isServerRunning(String serverId) {
        NutsServer nutsServer = servers.get(serverId);
        if (nutsServer == null) {
            return false;
        }
        return nutsServer.isRunning();
    }

    @Override
    public List<NutsServer> getServers() {
        return new ArrayList<>(servers.values());
    }

}
