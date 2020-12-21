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
package net.thevpc.nuts.toolbox.nutsserver;

import net.thevpc.nuts.toolbox.nutsserver.http.NutsHttpServerConfig;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
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
        NutsServerComponent server = ws.extensions().createServiceLoader(NutsServerComponent.class, ServerConfig.class, NutsServerComponent.class.getClassLoader(), ws.createSession())
                .loadBest(serverConfig);
        if (server == null) {
            throw new NutsIllegalArgumentException(ws, "not server extensions are registered.");
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
            throw new NutsIllegalArgumentException(ws, "server not found " + serverId);
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
