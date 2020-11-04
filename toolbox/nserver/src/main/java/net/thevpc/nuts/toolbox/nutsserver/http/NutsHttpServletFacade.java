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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
package net.thevpc.nuts.toolbox.nutsserver.http;

import net.thevpc.nuts.toolbox.nutsserver.http.commands.*;
import net.thevpc.nuts.toolbox.nutsserver.util.NutsServerUtils;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.http.commands.*;
import net.thevpc.common.util.ListValueMap;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServletFacade {

    private static final Logger LOG = Logger.getLogger(NutsHttpServletFacade.class.getName());
    private Map<String, NutsWorkspace> workspaces;
    private String serverId;
    private Map<String, FacadeCommand> commands = new HashMap<>();

    public NutsHttpServletFacade(String serverId, Map<String, NutsWorkspace> workspaces) {
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

    public Map<String, NutsWorkspace> getWorkspaces() {
        return workspaces;
    }

    public NutsHttpServletFacade setWorkspaces(Map<String, NutsWorkspace> workspaces) {
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
        NutsWorkspace ws = workspaces.get(ii.context);
        if (ws == null) {
            ws = workspaces.get("");
            if (ws != null) {
                ii = parse(requestPath, true);
            }
        }
        if (ws == null) {
            context.sendError(404, "Workspace not found");
            return;
        }
        FacadeCommand facadeCommand = commands.get(ii.command);
        if (facadeCommand == null) {
            if (ii.command.isEmpty()) {
                context.sendError(404, "Missing command");
            } else {
                context.sendError(404, "Command Not found : " + ii.command);
            }
        } else {
            try {
                try {
                    facadeCommand.execute(new FacadeCommandContext(context, ws, serverId, ii.command, ii.path, ws.createSession()));
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
            ListValueMap<String, String> parameters = context.getParameters();
            String id = parameters.getFirst("id");
            boolean transitive = parameters.containsKey("transitive");
            List<NutsId> fetch = null;
            try {
                NutsWorkspace ws = context.getWorkspace();
                fetch = ws.search().setSession(context.getSession())
                        .setTransitive(transitive).addId(id).getResultIds().list();
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
