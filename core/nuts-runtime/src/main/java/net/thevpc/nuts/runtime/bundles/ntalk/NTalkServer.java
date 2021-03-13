package net.thevpc.nuts.runtime.bundles.ntalk;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NTalkServer implements Closeable{
    Socket socket;
    boolean stopped;
    ExecutorService threadPool;
    private long id;
    private String challenge;
    private boolean connected;
    private String host;
    private String service;
    private int port;
    private boolean closed;
    private Action action;
    private DataInputStream inFromBus;
    private DataOutputStream outToBus;
    private String agentVersion;

    public NTalkServer(String service, Action action) {
        this(null, -1, service, action);
    }

    public NTalkServer(int port, String service, Action action) {
        this(null, port, service, action);
    }

    public NTalkServer(String host, int port, String service, Action action) {
        this.host = host == null ? "localhost" : host;
        this.port = port <= 0 ? NTalkConstants.DEFAULT_PORT : port;
        this.service = service;
        this.action = action;
        if (service == null) {
            throw new NullPointerException("null service");
        }
        if (action == null) {
            throw new NullPointerException("null action");
        }
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public NTalkServer setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public void runAsync() {
        if (connected) {
            throw new IllegalArgumentException("Already connected");
        }
        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }
        try {
            socket = new Socket(host, port);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        new Thread(() -> runSync()).start();
    }

    public void runSync() {
        if (connected) {
            throw new IllegalArgumentException("Already connected");
        }
        try {
            if (threadPool == null) {
                threadPool = Executors.newCachedThreadPool();
            }
            if (socket == null) {
                socket = new Socket(host, port);
            }
            log("start server");
            inFromBus = new DataInputStream(socket.getInputStream());
            outToBus = new DataOutputStream(socket.getOutputStream());
            outToBus.writeInt(NTalkConstants.CMD_SERVICE);
            outToBus.writeUTF(service);
            int respoCode = inFromBus.readInt();
            if (respoCode == NTalkConstants.OK) {
                agentVersion = inFromBus.readUTF();
                //good
            } else if (respoCode == NTalkConstants.KO) {
                agentVersion = inFromBus.readUTF();
                int errorCode = inFromBus.readInt();
                byte[] msg = NTalkUtils.readArray(inFromBus);
                throw new IllegalArgumentException("unable to start server " + service + ": error " + NTalkConstants.errorCode(errorCode) + " : " + new String(msg));
            }else{
                throw new IllegalArgumentException("unable to start server " + service + ": invalid response code " + respoCode);
            }
            id = inFromBus.readLong();
            challenge = inFromBus.readUTF();
            log("connected");
            connected = true;
            while (!stopped) {
                int cmdFromBus = inFromBus.readInt();
                switch (cmdFromBus) {
                    case NTalkConstants.OK_JOB: {
                        long jobId = -1;
                        synchronized (this) {
                            jobId = inFromBus.readLong();
                            onJobResponseFailure(jobId);
                        }
                        break;
                    }
                    case NTalkConstants.KO_JOB: {
                        long jobId = -1;
                        synchronized (this) {
                            jobId = inFromBus.readLong();
                            onJobResponseSuccess(jobId);
                        }
                        break;
                    }
                    case NTalkConstants.CMD_NEW_JOB: {
                        log("NEW_JOB:start");
                        long jobId0;
                        long cli0;
                        byte[] bytes0;
                        synchronized (this) {
                            jobId0 = inFromBus.readLong();
                            cli0 = inFromBus.readLong();
                            bytes0 = NTalkUtils.readArray(inFromBus);
                        }
                        long jobId = jobId0;
                        long cli = cli0;
                        byte[] bytes = bytes0;
                        log("NEW_JOB: handshake ok " + jobId);
                        threadPool.submit(() -> {
                            try {
                                log("NEW_JOB: run job " + jobId);
                                boolean err = false;
                                byte[] resp = null;
                                try {
                                    resp = action.onMessage(
                                            jobId,
                                            bytes,
                                            cli, id,
                                            service
                                    );
                                } catch (Exception ex) {
                                    err = true;
                                    resp=ex.toString().getBytes();
                                }
                                log("NEW_JOB: send response " + jobId);
                                synchronized (this) {
                                    outToBus.writeInt(err ? NTalkConstants.CMD_RESPONSE_KO : NTalkConstants.CMD_RESPONSE_OK);
                                    outToBus.writeLong(jobId);
                                    outToBus.writeLong(cli);
                                    NTalkUtils.writeArray(resp,outToBus);
                                }
                                log("NEW_JOB: finish " + jobId);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        break;
                    }
                    default: {
                        log("<COMMAND?>: unknown");
                        stopped = true;
                    }
                }
            }
        } catch (EOFException | SocketException e) {
            //do nothing
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            connected = false;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
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

    private void onJobResponseSuccess(long jobId) {

    }

    private void onJobResponseFailure(long jobId) {

    }

    private void log(String msg) {
//        System.err.println("[NSERVER] "+msg);
    }

    public void stop() {
        if (!stopped) {
            stopped = true;
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }


    public interface Action {
        byte[] onMessage(long jobId, byte[] msg, long clientId, long serverId, String serviceName);
    }

}
