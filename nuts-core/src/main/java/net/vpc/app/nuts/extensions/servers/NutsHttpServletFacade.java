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
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.extensions.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServletFacade {

    private static final Logger log = Logger.getLogger(NutsHttpServletFacade.class.getName());
    private Map<String, NutsWorkspace> workspaces;
    private String serverId;
    private Map<String, FacadeCommand> commands = new HashMap<>();

    public NutsHttpServletFacade(String serverId, Map<String, NutsWorkspace> workspaces) {
        this.workspaces = workspaces;
        this.serverId = serverId;
        register(new AbstractFacadeCommand("version") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                context.sendResponseText(200, new NutsIdImpl(context.getServerId(), "net.vpc.app.nuts", "nuts-server", context.getWorkspace().getWorkspaceRuntimeId().getVersion().toString(), "").toString());
            }
        });
        register(new AbstractFacadeCommand("fetch") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                NutsFile fetch = null;
                try {
                    fetch = context.getWorkspace().fetch(id, context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (fetch != null && fetch.getFile() != null && fetch.getFile().exists()) {
                    context.sendResponseFile(200, fetch.getFile());
                } else {
                    context.sendError(404, "File Note Found");
                }
            }
        });
        register(new AbstractFacadeCommand("fetch-descriptor") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                NutsDescriptor fetch = null;
                try {
                    fetch = context.getWorkspace().fetchDescriptor(id, false, context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (fetch != null) {
                    context.sendResponseText(200, fetch.toString());
                } else {
                    context.sendError(404, "Nuts not Found");
                }
            }
        });
        register(new AbstractFacadeCommand("fetch-hash") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                String hash = null;
                try {
                    hash = context.getWorkspace().fetchHash(id, context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (hash != null) {
                    context.sendResponseText(200, hash);
                } else {
                    context.sendError(404, "Nuts not Found");
                }
            }
        });
        register(new AbstractFacadeCommand("fetch-descriptor-hash") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                String hash = null;
                try {
                    hash = context.getWorkspace().fetchDescriptorHash(id, context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (hash != null) {
                    context.sendResponseText(200, hash);
                } else {
                    context.sendError(404, "Nuts not Found");
                }
            }
        });
        register(new AbstractFacadeCommand("find-versions") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                List<NutsId> fetch = null;
                try {
                    fetch = context.getWorkspace().find(new NutsSearch(id), context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (fetch != null) {
                    context.sendResponseText(200, iteratorNutsIdToString(fetch.iterator()));
                } else {
                    context.sendError(404, "Nuts not Found");
                }
            }
        });
        register(new AbstractFacadeCommand("resolve-id") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                ListMap<String, String> parameters = context.getParameters();
                String id = parameters.getOne("id");
                boolean transitive = parameters.containsKey("transitive");
                NutsId fetch = null;
                try {
                    fetch = context.getWorkspace().resolveId(id, context.getSession().copy().setTransitive(transitive));
                } catch (Exception exc) {
                    //
                }
                if (fetch != null) {
                    context.sendResponseText(200, fetch.toString());
                } else {
                    context.sendError(404, "Nuts not Found");
                }
            }
        });
        register(new AbstractFacadeCommand("find") {
            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                //Content-type
                String boundary = context.getRequestHeaderFirstValue("Content-type");
                if (CoreStringUtils.isEmpty(boundary)) {
                    context.sendError(400, "Invalid Command Arguments : " + getName() + " . Invalid format.");
                    return;
                }
                MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary);
                boolean transitive = true;
                String root = null;
                String pattern = null;
                String js = null;
                for (ItemStreamInfo info : stream) {
                    String name = info.resolveVarInHeader("Content-Disposition", "name");
                    switch (name) {
                        case "root":
                            root = CoreIOUtils.readStreamAsString(info.getContent(), true).trim();
                            break;
                        case "transitive":
                            transitive = Boolean.parseBoolean(CoreIOUtils.readStreamAsString(info.getContent(), true).trim());
                            break;
                        case "pattern":
                            pattern = CoreIOUtils.readStreamAsString(info.getContent(), true).trim();
                            break;
                        case "js":
                            js = CoreIOUtils.readStreamAsString(info.getContent(), true).trim();
                            break;
                    }
                }
                Iterator<NutsId> it = context.getWorkspace().findIterator(
                        new NutsSearchBuilder().addJs(js).addId(pattern).build(),
                        context.getSession().copy().setTransitive(transitive));
//                    Writer ps = new OutputStreamWriter(context.getResponseBody());
                context.sendResponseText(200, iteratorNutsIdToString(it));
            }
        });
        register(new AbstractFacadeCommand("deploy") {
//            @Override
//            public void execute(FacadeCommandContext context) throws IOException {
//                executeImpl(context);
//            }

            @Override
            public void executeImpl(FacadeCommandContext context) throws IOException {
                String boundary = context.getRequestHeaderFirstValue("Content-type");
                if (CoreStringUtils.isEmpty(boundary)) {
                    context.sendError(400, "Invalid Command Arguments : " + getName() + " . Invalid format.");
                    return;
                }
                MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary);
                NutsDescriptor descriptor = null;
                String receivedContentHash = null;
                InputStream content = null;
                File contentFile = null;
                for (ItemStreamInfo info : stream) {
                    String name = info.resolveVarInHeader("Content-Disposition", "name");
                    switch (name) {
                        case "descriptor":
                            descriptor = CoreNutsUtils.parseNutsDescriptor(info.getContent(), true);
                            break;
                        case "content-hash":
                            receivedContentHash = CoreSecurityUtils.evalSHA1(info.getContent(), true);
                            break;
                        case "content":
                            contentFile = CoreIOUtils.createTempFile(descriptor, false);
                            CoreIOUtils.copy(info.getContent(), contentFile, true, true);
                            break;
                    }
                }
                if (contentFile == null) {
                    context.sendError(400, "Invalid Command Arguments : " + getName() + " : Missing File");
                }
                NutsId id = context.getWorkspace().deploy(
                        new NutsDeployment().setContent(contentFile)
                                .setSha1(receivedContentHash)
                                .setDescriptor(descriptor),
                        context.getSession().copy());
//                NutsId id = workspace.deploy(content, descriptor, null);
                context.sendResponseText(200, id.toString());
            }
        });
    }

    private static String[] extractFirstToken(String requestURI) {
        int s1 = requestURI.indexOf('/');
        String firstToken = "";
        String theRest = "";
        if (s1 == 0) {
            s1 = requestURI.indexOf('/', 1);
            if (s1 < 0) {
                firstToken = requestURI.substring(1);
                theRest = "";
            } else {
                firstToken = requestURI.substring(1, s1);
                theRest = requestURI.substring(s1);
            }
        } else if (s1 < 0) {
            firstToken = requestURI;
            theRest = "";
        } else {
            firstToken = requestURI.substring(0, s1);
            theRest = requestURI.substring(s1);
        }
        return new String[]{firstToken, theRest};
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

    private String iteratorNutsIdToString(Iterator<NutsId> it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            NutsId next = it.next();
            //System.out.println(next.getId().toString());
            sb.append(next.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    protected void register(FacadeCommand cmd) {
        commands.put(cmd.getName(), cmd);
    }

    private URLInfo parse(String requestURI, boolean root) {
        URLInfo ii = new URLInfo();
        String[] tokens = extractFirstToken(requestURI);

        if (root) {
            ii.context = "";
            ii.command = tokens[0];
            ii.path = tokens[1];
        } else {
            ii.context = tokens[0];
            tokens = extractFirstToken(tokens[1]);
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
            context.sendError(404, "Command Not found : " + ii.command);
        } else {
            try {
                try {
                    facadeCommand.execute(new FacadeCommandContext(context, ws, serverId, ii.command, ii.path, ws.createSession()));
                } catch (SecurityException ex) {
                    log.log(Level.SEVERE, "SERVER ERROR : " + ex, ex);
//                    ex.printStackTrace();
                    context.sendError(403, ex.toString());
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "SERVER ERROR : " + ex, ex);
//                    ex.printStackTrace();
                    context.sendError(500, ex.toString());
                }
            } finally {
                try {
                    context.getResponseBody().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class URLInfo {

        String context;
        String command;
        String path;
    }

//    private List<String> parsePath(String requestURI) {
//        List<String> list = new ArrayList<String>(Arrays.asList(requestURI.split("/")));
//        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
//            String s = iterator.next();
//            if (s.trim().length() == 0) {
//                iterator.remove();
//            }
//        }
//        for (int i = list.size() - 1; i >= 0; i--) {
//            if (list.get(i).equals("..")) {
//                list.remove(i);
//                i++;
//            }
//        }
//        return list;
//    }
}
