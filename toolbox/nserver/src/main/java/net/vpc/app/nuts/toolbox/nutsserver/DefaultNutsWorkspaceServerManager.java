/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsDefaultSupportLevelContext;
import net.vpc.app.nuts.toolbox.nutsserver.http.NutsHttpServerConfig;

/**
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceServerManager implements NutsWorkspaceServerManager {

    private Map<String, NutsServer> servers = new HashMap<>();
    private final NutsWorkspace ws;

    public DefaultNutsWorkspaceServerManager(final NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsServer startServer(ServerConfig serverConfig) {
        if (serverConfig == null) {
            serverConfig = new NutsHttpServerConfig();
        }
        NutsServerComponent server = ws.extensions().createServiceLoader(NutsServerComponent.class, ServerConfig.class, NutsServerComponent.class.getClassLoader())
                .loadBest(serverConfig);
        if (server == null) {
            throw new NutsIllegalArgumentException(ws, "Not server extensions are registered.");
        }
        NutsServer s = server.start(ws/*.self()*/, serverConfig);
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
            throw new NutsIllegalArgumentException(ws, "Server not found " + serverId);
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
