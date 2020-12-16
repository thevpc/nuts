package net.thevpc.nuts.ntalk;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NTalkClient implements Closeable {
    Socket socket;
    boolean stopped;
    ExecutorService executor;
    DataInputStream inFromBus;
    DataOutputStream outToBus;
    private long id;
    private String challenge;
    private boolean connected;
    private String host;
    private String agentVersion;
    //    private String service;
    private int port;
    private boolean closed;

    public NTalkClient() {
        this(null, -1);
    }

    public NTalkClient(int port) {
        this(null, port);
    }

    public NTalkClient(String host, int port) {
        this.host = host == null ? "localhost" : host;
        this.port = port <= 0 ? 1401 : port;
        connect();
    }

    private void connect() {
        if (connected) {
            throw new IllegalArgumentException("already connected");
        }
        try {
            if (executor == null) {
                executor = Executors.newCachedThreadPool();
            }
            socket = new Socket(host, port);
            inFromBus = new DataInputStream(socket.getInputStream());
            outToBus = new DataOutputStream(socket.getOutputStream());
            outToBus.writeInt(NTalkConstants.CMD_CONNECT);
//            out.writeUTF(service);
            int ok = inFromBus.readInt();
            if (ok == NTalkConstants.OK) {
                id = inFromBus.readLong();
                agentVersion = inFromBus.readUTF();
                if (id < 0) {
                    //service invalid
                    throw new IllegalArgumentException("Invalid client");
                }
                challenge = inFromBus.readUTF();
                connected = true;
                log("CONNECT: handshake ok");
            } else if (ok == NTalkConstants.KO) {
                agentVersion = inFromBus.readUTF();
                int errorCode = inFromBus.readInt();
                byte[] msg = NTalkUtils.readArray(inFromBus);
                throw new IllegalArgumentException("unable to connect client: error " + NTalkConstants.errorCode(errorCode) + " : " + new String(msg));
            } else {
                throw new IllegalArgumentException("unable to connect client: invalid response");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public synchronized byte[] request(String service, byte[] requestMessage) {
        try {
            log("REQUEST:send " + service);
            outToBus.writeInt(NTalkConstants.CMD_REQUEST);
            outToBus.writeUTF(service);
            NTalkUtils.writeArray(requestMessage, outToBus);

            log("REQUEST:receive " + service);

            int ok = inFromBus.readInt();
            if (ok == NTalkConstants.CMD_RESPONSE_OK || ok == NTalkConstants.CMD_RESPONSE_KO) {
                long jobId = inFromBus.readLong();
                String srv = inFromBus.readUTF();
                if (ok == NTalkConstants.CMD_RESPONSE_OK) {
                    byte[] msg = NTalkUtils.readArray(inFromBus);
                    log("REQUEST:OK " + service);
                    return msg;
                } else {
                    int errorCode = inFromBus.readInt();
                    byte[] msg = NTalkUtils.readArray(inFromBus);
                    log("REQUEST:KO " + service);
                    throw new IllegalArgumentException("unable to send request: error " + NTalkConstants.errorCode(errorCode) + " : " + new String(msg));
                }
            } else {
                throw new IllegalArgumentException("unable to send request: invalid response");
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void close() {
        if (!closed) {
            try {
                outToBus.writeInt(NTalkConstants.CMD_QUIT);
            } catch (IOException e) {
                //
            }
            try {
                if (this.socket != null) {
                    this.socket.close();
                }
                this.socket = null;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.inFromBus != null) {
                    this.inFromBus.close();
                    this.inFromBus = null;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (this.outToBus != null) {
                    this.outToBus.close();
                    this.outToBus = null;
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }

    private void log(String msg) {
//        System.err.println("[NCLIENT] "+msg);
    }
}
