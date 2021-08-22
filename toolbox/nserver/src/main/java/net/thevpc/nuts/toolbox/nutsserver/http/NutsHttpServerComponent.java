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
package net.thevpc.nuts.toolbox.nutsserver.http;

import com.sun.net.httpserver.*;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nutsserver.NutsServer;
import net.thevpc.nuts.toolbox.nutsserver.NutsServerComponent;
import net.thevpc.nuts.toolbox.nutsserver.NutsServerConstants;
import net.thevpc.nuts.toolbox.nutsserver.ServerConfig;
import net.thevpc.nuts.toolbox.nutsserver.bundled._StringUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.*;
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

    private static final Logger LOG = Logger.getLogger(NutsHttpServerComponent.class.getName());
    private NutsHttpServletFacade facade;

    /**
     * returns the url parameters in a map
     *
     * @param query user query string
     * @return map
     */
    public static Map<String, List<String>> queryToMap(String query) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                List<String> li = result.computeIfAbsent(urlDecodeString(pair[0]), d -> new ArrayList<>());
                if (pair.length > 1) {
                    li.add(urlDecodeString(pair[1]));
                } else {
                    li.add("");
                }
            }
        }
        return result;
    }

    public static String urlDecodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return s;
        }
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<ServerConfig> config) {
        ServerConfig c = config.getConstraints();
        return (c == null || c instanceof NutsHttpServerConfig) ? DEFAULT_SUPPORT : NO_SUPPORT;
    }

    @Override
    public NutsServer start(NutsSession invokerWorkspace, ServerConfig config) {
        NutsHttpServerConfig httpConfig = (NutsHttpServerConfig) config;
        Map<String, NutsWorkspace> workspaces = httpConfig.getWorkspaces();
        if (invokerWorkspace == null) {
            throw new NutsIllegalArgumentException(invokerWorkspace, NutsMessage.cstyle("missing workspace"));
        }
        if (workspaces.isEmpty()) {
            workspaces.put("", invokerWorkspace.getWorkspace());
        }
        String serverId = httpConfig.getServerId();
        InetAddress address = httpConfig.getAddress();
        int port = httpConfig.getPort();
        int backlog = httpConfig.getBacklog();
        Executor executor = httpConfig.getExecutor();
        if (_StringUtils.isBlank(serverId)) {
            String serverName = NutsServerConstants.DEFAULT_HTTP_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NutsServerConstants.DEFAULT_HTTP_SERVER;
            }

            serverId = serverName;//+ "-" + new File(workspace.getWorkspaceLocation()).getName();
        }
        NutsSession session = invokerWorkspace;
        this.facade = new NutsHttpServletFacade(serverId, workspaces);
        if (port <= 0) {
            port = NutsServerConstants.DEFAULT_HTTP_SERVER_PORT;
        }
        if (backlog <= 0) {
            backlog = 10;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        final HttpServer server;
        try {
            server = httpConfig.isTls() ? HttpsServer.create(inetSocketAddress, backlog) : HttpServer.create(inetSocketAddress, backlog);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (executor == null) {
            int corePoolSize = httpConfig.getExecutorCorePoolSize();
            if (corePoolSize <= 0) {
                corePoolSize = 4;
            }
            int maxPoolSize = httpConfig.getExecutorMaximumPoolSize();
            if (maxPoolSize <= 0) {
                maxPoolSize = 100;
            }
            if (maxPoolSize <= corePoolSize) {
                maxPoolSize = corePoolSize;
            }
            int idleSeconds = httpConfig.getExecutorIdleTimeSeconds();
            if (idleSeconds <= 0) {
                idleSeconds = 30;
            }
            int queueSize = httpConfig.getExecutorQueueSize();
            if (queueSize <= 0) {
                queueSize = maxPoolSize;
            }
            if (queueSize > 1000) {
                queueSize = 1000;
            }
            executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, idleSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
        }
        server.setExecutor(executor);
        if (httpConfig.isTls()) {
            if (httpConfig.getSslKeystorePassphrase() == null) {
                throw new NutsIllegalArgumentException(invokerWorkspace, NutsMessage.cstyle("missing SslKeystorePassphrase"));
            }
            if (httpConfig.getSslKeystoreCertificate() == null) {
                throw new NutsIllegalArgumentException(invokerWorkspace, NutsMessage.cstyle("missing SslKeystoreCertificate"));
            }
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");

                // initialise the keystore
                char[] password = httpConfig.getSslKeystorePassphrase();
                KeyStore ks = KeyStore.getInstance("JKS");
                try {
                    ks.load(new ByteArrayInputStream(httpConfig.getSslKeystoreCertificate()), password);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                // setup the key manager text
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);

                // setup the trust manager text
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
                            if (LOG.isLoggable(Level.CONFIG)) {
                                LOG.log(Level.CONFIG, "failed to create HTTPS port");
                            }
                            session.err().println("```error failed to create HTTPS port```");
                        }
                    }
                });
            } catch (GeneralSecurityException e) {
                throw new NutsIllegalArgumentException(invokerWorkspace,NutsMessage.plain("start server failed"), e);
            }
        }

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(final HttpExchange httpExchange) throws IOException {

                facade.execute(new EmbeddedNutsHttpServletFacadeContext(httpExchange));
            }
        });
        server.start();
        NutsPrintStream out = session.out();
        NutsTextManager factory = session.getWorkspace().text();
        out.printf("Nuts Http Service '%s' running %s at %s\n", serverId,
                factory.forStyled(
                        (httpConfig.isTls() ? "https" : "http"), NutsTextStyle.primary1()
                ),
                inetSocketAddress);
        if (workspaces.size() == 1) {
            out.print("Serving workspace : ");
            for (Map.Entry<String, NutsWorkspace> entry : workspaces.entrySet()) {
                String k = entry.getKey();
                if (k.equals("")) {
                    out.printf("%s\n", entry.getValue().locations().getWorkspaceLocation());
                } else {
                    out.printf("%s : %s\n", k, entry.getValue().locations().getWorkspaceLocation());
                }
            }
        } else {
            out.println("Serving workspaces:");
            for (Map.Entry<String, NutsWorkspace> entry : workspaces.entrySet()) {
                String k = entry.getKey();
                if (k.equals("")) {
                    k = "<default>";
                }
                out.printf("\t%s : %s\n", k, entry.getValue().locations().getWorkspaceLocation());
            }
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

    private static class EmbeddedNutsHttpServletFacadeContext extends AbstractNutsHttpServletFacadeContext {
        private final HttpExchange httpExchange;

        public EmbeddedNutsHttpServletFacadeContext(HttpExchange httpExchange) {
            this.httpExchange = httpExchange;
        }

        @Override
        public void sendResponseBytes(int code, byte[] bytes) throws IOException {
            super.sendResponseBytes(code, bytes);
        }

        @Override
        public String getRequestMethod() throws IOException {
            return httpExchange.getRequestMethod();
        }

        @Override
        public URI getRequestURI() throws IOException {
            return httpExchange.getRequestURI();
        }

        @Override
        public OutputStream getResponseBody() {
            return httpExchange.getResponseBody();
        }

        @Override
        public void sendError(int code, String msg) throws IOException {
            if (msg == null) {
                msg = "error";
            }
            byte[] bytes = msg.getBytes();
            httpExchange.sendResponseHeaders(code, bytes.length);
            httpExchange.getResponseBody().write(bytes);
        }

        @Override
        public void sendResponseHeaders(int code, long length) throws IOException {
            httpExchange.sendResponseHeaders(code, length);
        }

        @Override
        public Set<String> getRequestHeaderKeys(String header) throws IOException {
            return httpExchange.getRequestHeaders().keySet();
        }

        @Override
        public String getRequestHeaderFirstValue(String header) throws IOException {
            return httpExchange.getRequestHeaders().getFirst(header);
        }

        @Override
        public List<String> getRequestHeaderAllValues(String header) throws IOException {
            return httpExchange.getRequestHeaders().get(header);
        }

        @Override
        public InputStream getRequestBody() throws IOException {
            return httpExchange.getRequestBody();
        }

        public Map<String, List<String>> getParameters() {
            return queryToMap(httpExchange.getRequestURI().getQuery());
        }

        @Override
        public void addResponseHeader(String name, String value) throws IOException {
            httpExchange.getResponseHeaders().add(name, value);
        }
    }
}
