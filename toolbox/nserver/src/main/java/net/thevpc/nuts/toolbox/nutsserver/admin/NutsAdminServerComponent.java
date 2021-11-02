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
package net.thevpc.nuts.toolbox.nutsserver.admin;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.toolbox.nutsserver.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsAdminServerComponent implements NutsServerComponent {

    private static final Logger LOG = Logger.getLogger(NutsAdminServerComponent.class.getName());

    @Override
    public int getSupportLevel(NutsSupportLevelContext<ServerConfig> config) {
        ServerConfig c = config.getConstraints();
        return (c == null || c instanceof AdminServerConfig) ? DEFAULT_SUPPORT : NO_SUPPORT;
    }

    public NutsServer start(NutsSession invokerWorkspace, ServerConfig config) {
        AdminServerConfig httpConfig = (AdminServerConfig) config;
        if (invokerWorkspace == null) {
            throw new NutsIllegalArgumentException(invokerWorkspace, NutsMessage.cstyle("missing workspace"));
        }
        NutsSession session = invokerWorkspace;
        String serverId = httpConfig.getServerId();
        InetAddress address = httpConfig.getAddress();
        int port = httpConfig.getPort();
        int backlog = httpConfig.getBacklog();
        Executor executor = httpConfig.getExecutor();
        if (executor == null) {
            executor = new ThreadPoolExecutor(2, 10, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        }
        if (NutsBlankable.isBlank(serverId)) {
            String serverName = NutsServerConstants.DEFAULT_ADMIN_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NutsServerConstants.DEFAULT_ADMIN_SERVER;
            }

            serverId = serverName;//+ "-" + new File(workspace.getWorkspaceLocation()).getName();
        }

        if (port <= 0) {
            port = NutsServerConstants.DEFAULT_ADMIN_SERVER_PORT;
        }
        if (backlog <= 0) {
            backlog = 10;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        NutsPrintStream out = session.out();
        NutsTexts factory = NutsTexts.of(session);
        out.printf("Nuts Admin Service '%s' running %s at %s\n", serverId, factory.ofStyled("telnet nsh",NutsTextStyle.primary1()), inetSocketAddress);
        out.printf("Serving workspace : %s\n", invokerWorkspace.locations().getWorkspaceLocation());
        AdminServerRunnable myNutsServer = new AdminServerRunnable(serverId, port, backlog, address, executor, invokerWorkspace, session);

        executor.execute(myNutsServer);
        return myNutsServer;
    }

}
