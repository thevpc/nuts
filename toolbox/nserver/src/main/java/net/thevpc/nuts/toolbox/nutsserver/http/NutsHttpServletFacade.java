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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nutsserver.http;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nutsserver.http.commands.*;
import net.thevpc.nuts.toolbox.nutsserver.util.NutsServerUtils;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServletFacade {

    private static final Logger LOG = Logger.getLogger(NutsHttpServletFacade.class.getName());
    private Map<String, NutsSession> workspaces;
    private String serverId;
    private Map<String, FacadeCommand> commands = new HashMap<>();

    public NutsHttpServletFacade(String serverId, Map<String, NutsSession> workspaces) {
        this.workspaces = workspaces;
        this.serverId = serverId;
        register(new VersionFacadeCommand());
        register(new GetMavenFacadeCommand());
        register(new FetchFacadeCommand());
        register(new FetchDescriptorFacadeCommand());
        register(new FetchHashFacadeCommand());
        register(new FetchDescriptorHashFacadeCommand());
        register(new SearchVersionsFacadeCommand());
        register(new ResolveIdFacadeCommand());
        register(new SearchFacadeCommand(this));
        register(new DeployFacadeCommand());
        register(new ExecFacadeCommand());
        register(new GetBootFacadeCommand());
    }

    public Map<String, NutsSession> getWorkspaces() {
        return workspaces;
    }

    public NutsHttpServletFacade setWorkspaces(Map<String, NutsSession> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public NutsHttpServletFacade setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Map<String, FacadeCommand> getCommands() {
        return commands;
    }

    public NutsHttpServletFacade setCommands(Map<String, FacadeCommand> commands) {
        this.commands = commands;
        return this;
    }

    protected void register(FacadeCommand cmd) {
        commands.put(cmd.getName(), cmd);
    }

    private URLInfo parse(String requestURI, boolean root) {
        URLInfo ii = new URLInfo();
        String[] tokens = NutsServerUtils.extractFirstToken(requestURI);

        if (root) {
            ii.context = "";
        } else {
            ii.context = tokens[0];
            tokens = NutsServerUtils.extractFirstToken(tokens[1]);
        }
        if (!commands.containsKey(tokens[0])) {
            if (GetMavenFacadeCommand.acceptUri(tokens[1])) {
                ii.command = "get-mvn";
                ii.path = requestURI;
            } else {
                ii.command = tokens[0];
                ii.path = tokens[1];
            }
        } else {
            ii.command = tokens[0];
            ii.path = tokens[1];
        }
        return ii;
    }

    public void execute(NutsHttpServletFacadeContext context) throws IOException {
        String requestPath = context.getRequestURI().getPath();
        URLInfo ii = parse(requestPath, false);
        NutsSession session = workspaces.get(ii.context);
        if (session == null) {
            session = workspaces.get("");
            if (session != null) {
                ii = parse(requestPath, true);
            }
        }
        if (session == null) {
            context.sendError(404, "workspace not found");
            return;
        }
        FacadeCommand facadeCommand = commands.get(ii.command);
        if (facadeCommand == null) {
            if (ii.command.isEmpty()) {
                context.sendError(404, "missing command");
            } else {
                context.sendError(404, "JShellCommandNode Not found : " + ii.command);
            }
        } else {
            try {
                try {
                    facadeCommand.execute(new FacadeCommandContext(context, serverId, ii.command, ii.path, session));
                } catch (SecurityException ex) {
                    LOG.log(Level.SEVERE, "SERVER ERROR : " + ex, ex);
//                    ex.printStackTrace();
                    context.sendError(403, ex.toString());
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "SERVER ERROR : " + ex, ex);
//                    ex.printStackTrace();
                    context.sendError(500, ex.toString());
                }
            } finally {
                if (!context.isHeadMethod()) {
                    try {
                        context.getResponseBody().flush();
                        context.getResponseBody().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class URLInfo {

        String context;
        String command;
        String path;
    }

    public class SearchVersionsFacadeCommand extends AbstractFacadeCommand {

        public SearchVersionsFacadeCommand() {
            super("find-versions");
        }

        @Override
        public void executeImpl(FacadeCommandContext context) throws IOException {
            Map<String, List<String>> parameters = context.getParameters();
            List<String> idList = parameters.get("id");
            String id = (idList==null || idList.isEmpty())?null: idList.get(0);
            boolean transitive = parameters.containsKey("transitive");
            List<NutsId> fetch = null;
            try {
                NutsSession session = context.getSession();
                fetch = session.search().setSession(
                        context.getSession().copy().setTransitive(transitive)
                )
                        .addId(id).getResultIds().toList();
            } catch (Exception exc) {
                //
            }
            if (fetch != null) {
                context.sendResponseText(200, NutsServerUtils.iteratorNutsIdToString(fetch.iterator()));
            } else {
                context.sendError(404, "Nuts not Found");
            }
        }
    }
}
