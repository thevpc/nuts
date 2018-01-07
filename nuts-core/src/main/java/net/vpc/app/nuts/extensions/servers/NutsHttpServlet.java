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
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.extensions.cmd.AdminServerConfig;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.util.JsonUtils;
import net.vpc.app.nuts.extensions.util.ListMap;
import net.vpc.app.nuts.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(NutsHttpServlet.class.getName());
    private NutsHttpServletFacade facade;
    private String serverId = "";
    private String workspaceLocation = null;
    private String workspaceRootLocation = null;
    private int adminServerPort = -1;
    private Map<String, String> workspaces = new HashMap<>();
    private boolean adminServer = true;
    private NutsServer adminServerRef;

    @Override
    public void init() throws ServletException {
        super.init();
        Map<String, NutsWorkspace> workspacesByLocation = new HashMap<>();
        Map<String, NutsWorkspace> workspacesByWebContextPath = new HashMap<>();
        NutsWorkspace workspace = null;
        NutsSession session = new NutsSession();
        NutsWorkspace bws = null;
        try {
            bws = Main.openBootstrapWorkspace(workspaceRootLocation);
            workspace = bws.openWorkspace(workspaceLocation, new NutsWorkspaceCreateOptions()
                            .setCreateIfNotFound(true)
                            .setSaveIfCreated(true)
                            .setArchetype("server"),
                    session
            );
        } catch (IOException e) {
            throw new ServletException("Unable to start Workspace " + workspaceLocation);
        }
        if (workspaces.isEmpty()) {
            String wl = workspaceLocation == null ? "" : workspaceLocation;
            workspaces.put("", wl);
            workspacesByLocation.put(wl, workspace);
        }
        for (Map.Entry<String, String> w : workspaces.entrySet()) {
            String webContext = w.getKey();
            String location = w.getValue();
            if (location == null) {
                location = "";
            }
            NutsWorkspace ws = workspacesByLocation.get(location);
            if (ws == null) {
                try {
                    ws = bws.openWorkspace(location, new NutsWorkspaceCreateOptions()
                                    .setCreateIfNotFound(true)
                                    .setSaveIfCreated(true)
                                    .setArchetype("server"),
                            session
                    );
                } catch (IOException e) {
                    throw new ServletException("Unable to start Workspace " + workspaceLocation);
                }
                workspacesByLocation.put(location, ws);
            }
            workspacesByWebContextPath.put(webContext, ws);
        }

        if (StringUtils.isEmpty(serverId)) {
            String serverName = NutsConstants.DEFAULT_HTTP_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NutsConstants.DEFAULT_HTTP_SERVER;
            }

            serverId = serverName;
        }

        this.facade = new NutsHttpServletFacade(serverId, workspacesByWebContextPath);
        if (adminServer) {
            try {
                AdminServerConfig serverConfig = new AdminServerConfig();
                serverConfig.setPort(adminServerPort);
                adminServerRef = workspace.startServer(serverConfig);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Unable to start admin server", ex);
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.info("Starting Nuts Http Server at url http://<your-server>" + config.getServletContext().getContextPath() + "/service");
        if (adminServer) {
            log.info("Starting Nuts admin Server at <localhost>:" + (adminServerPort < 0 ? NutsConstants.DEFAULT_HTTP_SERVER_PORT : adminServerPort));
        }
        adminServerPort = CoreStringUtils.parseInt(config.getInitParameter("admin-server.port"), -1);
        workspaceLocation = config.getInitParameter("workspace");
        workspaceRootLocation = config.getInitParameter("workspaceRoot");
        adminServer = Boolean.valueOf(config.getInitParameter("admin"));
        try {
            workspaces = JsonUtils.deserializeStringsMap(JsonUtils.loadJsonStructure(config.getInitParameter("workspaces")), new LinkedHashMap<String, String>());
        } catch (IOException e) {
            //
        }
        if (workspaces == null) {
            workspaces = new LinkedHashMap<>();
        }
        super.init(config);
        config.getServletContext().setAttribute(NutsHttpServletFacade.class.getName(), facade);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (adminServerRef != null) {
            try {
                adminServerRef.stop();
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Unable to stop admin server", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    protected void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        facade.execute(new AbstractNutsHttpServletFacadeContext() {
            public URI getRequestURI() throws IOException {
                try {
                    String cp = req.getContextPath();
                    String uri = req.getRequestURI();
                    if (uri.startsWith(cp)) {
                        uri = uri.substring(cp.length());
                        if (uri.startsWith(req.getServletPath())) {
                            uri = uri.substring(req.getServletPath().length());
                        }
                    }
                    return new URI(uri);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }

            public OutputStream getResponseBody() throws IOException {
                return resp.getOutputStream();
            }

            public void sendError(int code, String msg) throws IOException {
                resp.sendError(code, msg);
            }

            public void sendResponseHeaders(int code, long length) throws IOException {
                if (length > 0) {

                    resp.setHeader("Content-length", Long.toString(length));
                }
                resp.setStatus(code);
            }

            @Override
            public String getRequestHeaderFirstValue(String header) throws IOException {
                return req.getHeader(header);
            }

            @Override
            public Set<String> getRequestHeaderKeys(String header) throws IOException {
                return new HashSet<String>(Collections.list(req.getHeaderNames()));
            }

            @Override
            public List<String> getRequestHeaderAllValues(String header) throws IOException {
                return Collections.list(req.getHeaders(header));
            }

            @Override
            public InputStream getRequestBody() throws IOException {
                return req.getInputStream();
            }

            public ListMap<String, String> getParameters() throws IOException {
                ListMap<String, String> m = new ListMap<String, String>();
                for (String s : Collections.list(req.getParameterNames())) {
                    for (String v : req.getParameterValues(s)) {
                        m.add(s, v);
                    }
                }
                return m;//HttpUtils.queryToMap(getRequestURI().getQuery());
            }
        });
    }
}

