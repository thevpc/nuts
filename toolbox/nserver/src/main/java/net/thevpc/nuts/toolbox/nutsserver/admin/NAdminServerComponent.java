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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nutsserver.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

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
public class NAdminServerComponent implements NServerComponent {

    private static final Logger LOG = Logger.getLogger(NAdminServerComponent.class.getName());

    @Override
    public int getSupportLevel(NSupportLevelContext config) {
        ServerConfig c = config.getConstraints();
        return (c == null || c instanceof AdminServerConfig) ? NConstants.Support.DEFAULT_SUPPORT : NConstants.Support.NO_SUPPORT;
    }

    public NServer start(ServerConfig config) {
        NWorkspace ws = NWorkspace.get().get();
        NSession session = ws.currentSession();
        AdminServerConfig httpConfig = (AdminServerConfig) config;
        String serverId = httpConfig.getServerId();
        InetAddress address = httpConfig.getAddress();
        int port = httpConfig.getPort();
        int backlog = httpConfig.getBacklog();
        Executor executor = httpConfig.getExecutor();
        if (executor == null) {
            executor = new ThreadPoolExecutor(2, 10, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        }
        if (NBlankable.isBlank(serverId)) {
            String serverName = NServerConstants.DEFAULT_ADMIN_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NServerConstants.DEFAULT_ADMIN_SERVER;
            }

            serverId = serverName;//+ "-" + new File(workspace.getWorkspaceLocation()).getName();
        }

        if (port <= 0) {
            port = NServerConstants.DEFAULT_ADMIN_SERVER_PORT;
        }
        if (backlog <= 0) {
            backlog = 10;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        NPrintStream out = session.out();
        NTexts factory = NTexts.of();
        out.println((NMsg.ofC("Nuts Admin Service '%s' running %s at %s", serverId, factory.ofStyled("telnet nsh", NTextStyle.primary1()), inetSocketAddress)));
        out.println((NMsg.ofC("Serving workspace : %s", NWorkspace.of().getWorkspaceLocation())));
        AdminServerRunnable myNutsServer = new AdminServerRunnable(serverId, port, backlog, address, executor, session, session);

        executor.execute(myNutsServer);
        return myNutsServer;
    }

}
