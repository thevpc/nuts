/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
