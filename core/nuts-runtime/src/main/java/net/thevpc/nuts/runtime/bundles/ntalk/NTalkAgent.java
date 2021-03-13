package net.thevpc.nuts.runtime.bundles.ntalk;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NTalkAgent implements Closeable{
    private final static String AGENT_VERSION = "0.8.1.0";
    private final Map<String, ServerSession> sessionsByService = new HashMap<>();
    private final Map<Long, Session> sessionsById = new HashMap<>();
    private int port;
    private long lastId = 0;
    private long lastJobId = 0;
    private String bindAddress;
    private int backlog;
    private ServerSocket serverSocket;
    private boolean closed;
    private ExecutorService threadPool;

    public NTalkAgent() {
        this(-1, -1, "");
    }

    public NTalkAgent(int port) {
        this(port, -1, "");
    }

    public NTalkAgent(int port, int backlog) {
        this(port, backlog, "");
    }

    public NTalkAgent(int port, int backlog, String bindAddress) {
        this.port = port <= 0 ? NTalkConstants.DEFAULT_PORT : port;
        this.bindAddress = bindAddress==null?null:bindAddress.isEmpty()?"localhost":bindAddress;
        this.backlog = backlog <= 0 ? 50 : backlog;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public NTalkAgent setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public void runAsync() {
        try {
            if(threadPool==null) {
                threadPool = Executors.newCachedThreadPool();
            }
            serverSocket = new ServerSocket(port, backlog <= 0 ? 50 : backlog, bindAddress == null ? null : InetAddress.getByName(bindAddress));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        threadPool.submit(() -> {
            try {
                while (!closed) {
                    Socket s = serverSocket.accept();
                    threadPool.submit(() -> process(s));
                }
            } catch (SocketException ex) {
                //ignore
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void runSync() {
        try {
            threadPool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(port);
            try {
                while (!closed) {
                    Socket s = serverSocket.accept();
                    threadPool.submit(() -> process(s));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void process(Socket socket) {
        Session session = null;
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            log("CLIENT_SOCKET");
            int cmd = in.readInt();
            switch (cmd) {
                case NTalkConstants.CMD_CONNECT: {
                    log("CONNECT:start");
                    ClientSession csession = new ClientSession();
                    csession.sessionId = nextId();
                    csession.socket = socket;
                    csession.inClientToAgent = in;
                    csession.outAgentToClient = out;
//                    csession.service = in.readUTF();

                    //write challenge
                    csession.outAgentToClient.writeInt(NTalkConstants.OK);
                    csession.outAgentToClient.writeUTF(getAgentVersion());
                    csession.outAgentToClient.writeLong(csession.sessionId);
                    csession.outAgentToClient.writeUTF(csession.challenge = UUID.randomUUID().toString());

                    log("CONNECT: handshake OK " + csession.sessionId + " / " + csession.challenge);
                    session = csession;
                    sessionsById.put(csession.sessionId, csession);
                    processClient(csession);
                    break;
                }
                case NTalkConstants.CMD_SERVICE: {
                    log("SERVICE:start");
                    ServerSession serverSession = new ServerSession();
                    session = serverSession;
                    serverSession.socket = socket;
                    serverSession.sessionId = nextId();
                    serverSession.inServerToAgent = in;
                    serverSession.outAgentToServer = out;
                    serverSession.service = serverSession.inServerToAgent.readUTF();
                    boolean serviceAlreadyRegistered = false;
                    synchronized (sessionsByService) {
                        if (sessionsByService.containsKey(serverSession.service)) {
                            serviceAlreadyRegistered = true;
                        } else {
                            sessionsByService.put(serverSession.service, serverSession);
                        }
                    }
                    synchronized (serverSession) {
                        if (serviceAlreadyRegistered) {
                            serverSession.outAgentToServer.writeInt(NTalkConstants.KO);
                            serverSession.outAgentToServer.writeUTF(getAgentVersion());
                            serverSession.outAgentToServer.writeInt(NTalkConstants.ERR_SERVICE_ALREADY_REGISTERED);
                        } else {
                            //write challenge
                            serverSession.outAgentToServer.writeInt(NTalkConstants.OK);
                            serverSession.outAgentToServer.writeUTF(getAgentVersion());
                            serverSession.outAgentToServer.writeLong(serverSession.sessionId);
                            serverSession.outAgentToServer.writeUTF(serverSession.challenge = UUID.randomUUID().toString());
                        }
                    }
                    if (!serviceAlreadyRegistered) {
                        log("SERVICE: handshake OK " + serverSession.sessionId + " / " + serverSession.challenge);
                        sessionsById.put(serverSession.sessionId, serverSession);
                        processServer(serverSession);
                    }
                    break;
                }
                case NTalkConstants.CMD_RECONNECT: {
                    log("RECONNECT:start");
                    long oldId = in.readLong();
                    String challenge = in.readUTF();
                    Session csession = sessionsById.get(oldId);
                    if (csession != null && csession.challenge.equals(challenge)) {
                        csession.close();
                        csession.socket = socket;
                        if (csession instanceof ClientSession) {
                            ((ClientSession) csession).inClientToAgent = in;
                            ((ClientSession) csession).outAgentToClient = out;
                        } else {
                            ((ServerSession) csession).inServerToAgent = in;
                            ((ServerSession) csession).outAgentToServer = out;
                        }
                        session = csession;
                        if (csession instanceof ClientSession) {
                            log("RECONNECT: handshake Client OK " + csession.sessionId + " / " + csession.challenge);
                            processClient((ClientSession) csession);
                        } else {
                            log("RECONNECT: handshake Server OK " + csession.sessionId + " / " + csession.challenge);
                            processServer((ServerSession) csession);
                        }
                    } else {
                        log("RECONNECT: invalid handshake");
                    }
                    break;
                }
                default: {
                    log("???: unexpected command");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private String getAgentVersion() {
        return AGENT_VERSION;
    }

    private void log(String msg) {
//        System.err.println("[NBUS] " + msg);
    }

    private synchronized long nextId() {
        return ++lastId;
    }

    private synchronized long nextJobId() {
        return ++lastJobId;
    }

    private void processClient(ClientSession clientSession) {
        try {
            boolean quit = false;
            while (!quit) {
                int i = clientSession.inClientToAgent.readInt();
                switch (i) {
                    case NTalkConstants.CMD_REQUEST: {
                        long jobId = nextJobId();
                        String serviceName = "";
                        byte[] clientRequestMessage = null;
                        int errorCode = 0;
                        String errorMessage = null;
                        try {
                            serviceName = clientSession.inClientToAgent.readUTF();
                            clientRequestMessage = NTalkUtils.readArray(clientSession.inClientToAgent);
                        } catch (IOException e) {
                            errorCode = NTalkConstants.ERR_CLIENT_ERROR;
                            errorMessage = "client error: " + serviceName + ": " + e.toString();
                        }
                        if (errorCode == 0) {
                            ServerSession serverSession = sessionsByService.get(serviceName);
                            if (serverSession != null) {
                                try {
                                    synchronized (serverSession) {
                                        serverSession.outAgentToServer.writeInt(NTalkConstants.CMD_NEW_JOB);
                                        serverSession.outAgentToServer.writeLong(jobId);
                                        serverSession.outAgentToServer.writeLong(clientSession.sessionId);
                                        NTalkUtils.writeArray(clientRequestMessage, serverSession.outAgentToServer);
                                    }
                                } catch (IOException e) {
                                    errorCode = NTalkConstants.ERR_SERVER_ERROR;
                                    errorMessage = "server error: " + serviceName + ": " + e.toString();
                                }
                            } else {
                                errorCode = NTalkConstants.ERR_SERVICE_NOT_FOUND;
                                errorMessage = "server error: " + serviceName + ": service not found " + serviceName;
                            }
                        }
                        if (errorCode != 0) {
                            try {
                                synchronized (clientSession) {
                                    clientSession.outAgentToClient.writeInt(NTalkConstants.CMD_RESPONSE_KO);
                                    clientSession.outAgentToClient.writeLong(jobId);
                                    clientSession.outAgentToClient.writeUTF(serviceName);
                                    clientSession.outAgentToClient.writeInt(errorCode);
                                    NTalkUtils.writeArray(errorMessage.getBytes(), clientSession.outAgentToClient);
                                }
                            } catch (Exception ex) {
                                System.err.println("kill client after error " + errorMessage);
                                quit = true;
                            }
                        }
                        break;
                    }
                    case NTalkConstants.CMD_QUIT: {
                        quit = true;
                        sessionsById.remove(clientSession.sessionId);
                        break;
                    }
                    default: {
                        System.err.println("Unexpected");
                        quit = true;
                        sessionsById.remove(clientSession.sessionId);
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            //
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            synchronized (this) {
                sessionsById.remove(clientSession.sessionId);
            }
            clientSession.close();
        }
    }

    private void processServer(final ServerSession serverSession) {
        try {
            boolean quit = false;
            while (!quit) {
                int command = serverSession.inServerToAgent.readInt();
                switch (command) {
                    case NTalkConstants.CMD_RESPONSE_OK:
                    case NTalkConstants.CMD_RESPONSE_KO: {
                        try {
                            long jobId = serverSession.inServerToAgent.readLong();
                            long to = serverSession.inServerToAgent.readLong();
                            byte[] msg = NTalkUtils.readArray(serverSession.inServerToAgent);

                            ClientSession cliSession = (ClientSession) sessionsById.get(to);
                            if (cliSession != null) {
                                cliSession.outAgentToClient.writeInt(command);
                                cliSession.outAgentToClient.writeLong(jobId);
                                cliSession.outAgentToClient.writeUTF(serverSession.service);
                                if (command == NTalkConstants.CMD_RESPONSE_KO) {
                                    cliSession.outAgentToClient.writeInt(NTalkConstants.ERR_SERVER_ERROR);
                                }
                                NTalkUtils.writeArray(msg, cliSession.outAgentToClient);
                            }
                            synchronized (serverSession) {
                                serverSession.outAgentToServer.writeInt(cliSession != null ? NTalkConstants.OK_JOB : NTalkConstants.KO_JOB);
                                serverSession.outAgentToServer.writeLong(jobId);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        break;
                    }
                    case NTalkConstants.CMD_QUIT: {
                        quit = true;
                        break;
                    }
                    default: {
                        System.err.println("Pbm");
                        quit = true;
                    }
                }
            }
        } catch (EOFException ex) {
            //Do noting...
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            synchronized (sessionsByService) {
                sessionsByService.remove(serverSession.service);
            }
            synchronized (sessionsById) {
                sessionsById.remove(serverSession.sessionId);
            }
            serverSession.close();
        }
    }

    public void close() {
        if (!closed) {
            closed = true;
            for (Session value : sessionsById.values().toArray(new Session[0])) {
                sessionsById.remove(value.sessionId);
                if (value instanceof ServerSession) {
                    sessionsByService.remove(((ServerSession) value).service);
                }
                value.close();
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    private abstract static class Session {
        Long sessionId;
        String challenge;
        Socket socket;


        public abstract void close();
    }

    private static class ServerSession extends Session {
        String service;
        DataInputStream inServerToAgent;
        DataOutputStream outAgentToServer;
        boolean closed;

        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                if (this.socket != null) {
                    this.socket.close();
                }
                this.socket = null;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.inServerToAgent != null) {
                    this.inServerToAgent.close();
                    this.inServerToAgent = null;
                }
                this.inServerToAgent = null;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.outAgentToServer != null) {
                    this.outAgentToServer.close();
                    this.outAgentToServer = null;
                }
                this.outAgentToServer = null;
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    private static class ClientSession extends Session {
        DataInputStream inClientToAgent;
        DataOutputStream outAgentToClient;

        public void close() {
            try {
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.inClientToAgent != null) {
                    this.inClientToAgent.close();
                    this.inClientToAgent = null;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.outAgentToClient != null) {
                    this.outAgentToClient.close();
                    this.outAgentToClient = null;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }
}
