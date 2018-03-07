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

import com.sun.net.httpserver.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreHttpUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.ListMap;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServerComponent implements NutsServerComponent {

    private static final Logger log = Logger.getLogger(NutsHttpServerComponent.class.getName());
    private NutsHttpServletFacade facade;

    @Override
    public int getSupportLevel(ServerConfig config) {
        return (config == null || config instanceof NutsHttpServerConfig) ? DEFAULT_SUPPORT : NO_SUPPORT;
    }

    public NutsServer start(NutsWorkspace invokerWorkspace, ServerConfig config) {
        NutsHttpServerConfig httpConfig = (NutsHttpServerConfig) config;
        Map<String, NutsWorkspace> workspaces = httpConfig.getWorkspaces();
        if (invokerWorkspace == null) {
            throw new NutsIllegalArgumentException("Missing Workspace");
        }
        if (workspaces.isEmpty()) {
            workspaces.put("", invokerWorkspace);
        }
        String serverId = httpConfig.getServerId();
        InetAddress address = httpConfig.getAddress();
        int port = httpConfig.getPort();
        int backlog = httpConfig.getBacklog();
        Executor executor = httpConfig.getExecutor();
        if (CoreStringUtils.isEmpty(serverId)) {
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

            serverId = serverName;//+ "-" + new File(workspace.getWorkspaceLocation()).getName();
        }
        NutsTerminal terminal = invokerWorkspace.getExtensionManager().getFactory().createTerminal();

        this.facade = new NutsHttpServletFacade(serverId, workspaces);
        if (port <= 0) {
            port = NutsConstants.DEFAULT_HTTP_SERVER_PORT;
        }
        if (backlog <= 0) {
            backlog = 10;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        final HttpServer server;
        try {
            server = httpConfig.isSsh() ? HttpsServer.create(inetSocketAddress, backlog) : HttpServer.create(inetSocketAddress, backlog);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        if (executor == null) {
            executor = new ThreadPoolExecutor(4, 100, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
        }
        server.setExecutor(executor);
        if (httpConfig.isSsh()) {
            if (httpConfig.getSslKeystorePassphrase() == null) {
                throw new NutsIllegalArgumentException("Missing SslKeystorePassphrase");
            }
            if (httpConfig.getSslKeystoreCertificate() == null) {
                throw new NutsIllegalArgumentException("Missing SslKeystoreCertificate");
            }
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");

                // initialise the keystore
                char[] password = httpConfig.getSslKeystorePassphrase();
                KeyStore ks = KeyStore.getInstance("JKS");
                try {
                    ks.load(new ByteArrayInputStream(httpConfig.getSslKeystoreCertificate()), password);
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }

                // setup the key manager factory
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);

                // setup the trust manager factory
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                // setup the HTTPS context and parameters
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    public void configure(HttpsParameters params) {
                        try {
                            // initialise the SSL context
                            SSLContext c = SSLContext.getDefault();
                            SSLEngine engine = c.createSSLEngine();
                            params.setNeedClientAuth(false);
                            params.setCipherSuites(engine.getEnabledCipherSuites());
                            params.setProtocols(engine.getEnabledProtocols());

                            // get the default parameters
                            SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                            params.setSSLParameters(defaultSSLParameters);
                        } catch (Exception ex) {
                            log.log(Level.CONFIG, "Failed to create HTTPS port");
                            terminal.getErr().printf("**Failed to create HTTPS port**");
                        }
                    }
                });
            } catch (GeneralSecurityException e) {
                throw new NutsIllegalArgumentException(e);
            }
        }

        server.createContext("/", new HttpHandler() {
            public void handle(final HttpExchange httpExchange) throws IOException {

                facade.execute(new AbstractNutsHttpServletFacadeContext() {
                    public URI getRequestURI() throws IOException {
                        return httpExchange.getRequestURI();
                    }

                    public OutputStream getResponseBody() {
                        return httpExchange.getResponseBody();
                    }

                    public void sendError(int code, String msg) throws IOException {
                        if (msg == null) {
                            msg = "Error";
                        }
                        byte[] bytes = msg.getBytes();
                        httpExchange.sendResponseHeaders(code, bytes.length);
                        httpExchange.getResponseBody().write(bytes);
                    }

                    public void sendResponseHeaders(int code, long length) throws IOException {
                        httpExchange.sendResponseHeaders(code, length);
                    }

                    @Override
                    public String getRequestHeaderFirstValue(String header) throws IOException {
                        return httpExchange.getRequestHeaders().getFirst(header);
                    }

                    @Override
                    public Set<String> getRequestHeaderKeys(String header) throws IOException {
                        return httpExchange.getRequestHeaders().keySet();
                    }

                    @Override
                    public List<String> getRequestHeaderAllValues(String header) throws IOException {
                        return httpExchange.getRequestHeaders().get(header);
                    }

                    @Override
                    public InputStream getRequestBody() throws IOException {
                        return httpExchange.getRequestBody();
                    }

                    public ListMap<String, String> getParameters() {
                        return CoreHttpUtils.queryToMap(httpExchange.getRequestURI().getQuery());
                    }
                });
            }
        });
        server.start();
        terminal.getOut().printf("Nuts Http Service '%s' running at %s\n", serverId, inetSocketAddress);
        terminal.getOut().printf("Serving workspaces: \n");
        for (Map.Entry<String, NutsWorkspace> entry : workspaces.entrySet()) {
            terminal.getOut().printf("\t%s : %s\n", entry.getKey(), entry.getValue().getConfigManager().getWorkspaceLocation());
        }
        final String finalServerId = serverId;
        return new NutsServer() {
            boolean running = true;

            @Override
            public String getServerId() {
                return finalServerId;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public boolean stop() {
                if (running) {
                    running = false;
                    server.stop(0);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "Nuts Http Server{"
                        + "running=" + running
                        + '}';
            }
        };
//        System.out.println("Type [CTRL]+[C] to quit!");
    }
}
