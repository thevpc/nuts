/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.nutsserver.http.NHttpServerConfig;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultNWorkspaceServerManager implements NWorkspaceServerManager {

    private final NSession session;
    private Map<String, NServer> servers = new HashMap<>();

    public DefaultNWorkspaceServerManager(final NSession session) {
        this.session = session;
    }

    @Override
    public NServer startServer(ServerConfig serverConfig) {
        if (serverConfig == null) {
            serverConfig = new NHttpServerConfig();
        }
        NServerComponent server = session.extensions().createServiceLoader(NServerComponent.class, ServerConfig.class, NServerComponent.class.getClassLoader())
                .loadBest(serverConfig);
        NAssert.requireNonNull(server, "server", session);
        NServer s = server.start(session/*.self()*/, serverConfig);
        if (servers.get(s.getServerId()) != null) {
            servers.get(s.getServerId()).stop();
        }
        servers.put(s.getServerId(), s);
        return s;
    }

    @Override
    public NServer getServer(String serverId) {
        NServer nServer = servers.get(serverId);
        NAssert.requireNonNull(nServer, "server "+serverId, session);
        return nServer;
    }

    @Override
    public void stopServer(String serverId) {
        getServer(serverId).stop();
    }

    @Override
    public boolean isServerRunning(String serverId) {
        NServer nServer = servers.get(serverId);
        if (nServer == null) {
            return false;
        }
        return nServer.isRunning();
    }

    @Override
    public List<NServer> getServers() {
        return new ArrayList<>(servers.values());
    }

}
