package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.NConnexionString;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

public class SshConnexionPool {
    private final int maxSize;
    private long idleTimeout = 60000;
    private final Map<NConnexionString, BlockingQueue<SShPooledConnexion>> idleMap;
    private final Function<NConnexionString, SShConnection> factory;
    private Timer timer;


    public static SshConnexionPool of() {
        NWorkspace ws = NWorkspace.of();
        synchronized (ws) {
            SshConnexionPool u = ws.getProperty(SshConnexionPool.class).orNull();
            if (u != null) {
                return u;
            }
            SshConnexionPool r = new SshConnexionPool(200, 60000);
            ws.setProperty(SshConnexionPool.class.getName(), r);
            return r;
        }
    }

    public SshConnexionPool(int maxSize, long idleTimeout) {
        this(maxSize, idleTimeout, SShConnection::new);
    }

    public SshConnexionPool(int maxSize, long idleTimeout, Function<NConnexionString, SShConnection> factory) {
        this.maxSize = maxSize;
        this.idleTimeout = idleTimeout <= 0 ? 60000 : idleTimeout;
        this.factory = factory;
        this.idleMap = new HashMap<>();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                disposeOld(SshConnexionPool.this.idleTimeout);
            }
        }, idleTimeout);
    }

    public ISShConnexion acquire(String connexionString) {
        return acquire(NConnexionString.of(connexionString));
    }

    public ISShConnexion acquire(NConnexionString connexionString) {
        BlockingQueue<SShPooledConnexion> idle = getIdle(connexionString);
        SShPooledConnexion conn = idle.poll();
        if (conn != null && conn.isAlive()) {
            return conn;
        }

        // If the pool isn't full yet, create a new one
        synchronized (this) {
            if (idle.size() < maxSize) {
                NChronometer s = NChronometer.startNow();
                SShPooledConnexion newConn = new SShPooledConnexion(connexionString, factory.apply(connexionString));
                NLog.of(SshConnexionPool.class).log(NMsg.ofC("create ssh connexion %s in %s", connexionString, s).withIntent(NMsgIntent.ADD).asDebug());
                return newConn;
            }
        }
        // Pool full: wait for someone to return one
        try {
            SShPooledConnexion take = idle.take();
            NLog.of(SshConnexionPool.class).log(NMsg.ofC("acquire ssh connexion %s", connexionString).withIntent(NMsgIntent.REMOVE).asDebug());
            return take;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized BlockingQueue<SShPooledConnexion> getIdle(NConnexionString connexionString) {
        return idleMap.computeIfAbsent(connexionString, r -> new ArrayBlockingQueue<>(maxSize));
    }


    public synchronized void disposeOld(long timeoutMs) {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<NConnexionString, BlockingQueue<SShPooledConnexion>>> iterator = idleMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<NConnexionString, BlockingQueue<SShPooledConnexion>> e = iterator.next();
            BlockingQueue<SShPooledConnexion> idle = e.getValue();
            for (Iterator<SShPooledConnexion> iter = idle.iterator(); iter.hasNext(); ) {
                SShPooledConnexion conn = iter.next();
                if (conn.lastUsed + timeoutMs >= now || !conn.isAlive()) {
                    silentDispose(conn);
                    iter.remove();
                }
            }
            if (idle.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void silentDispose(SShPooledConnexion conn) {
        NLog.of(SshConnexionPool.class).log(NMsg.ofC("dispose ssh connexion %s", conn.connexionString).withIntent(NMsgIntent.DISPOSE).asDebug());
        try {
            conn.dispose();
        } catch (Exception ignored) {
            // Streams vanish silently into the void
        }
    }

    public synchronized void close() {
        timer.cancel();
        for (Map.Entry<NConnexionString, BlockingQueue<SShPooledConnexion>> e : idleMap.entrySet()) {
            BlockingQueue<SShPooledConnexion> idle = e.getValue();
            for (SShPooledConnexion conn : idle) {
                silentDispose(conn);
            }
            idle.clear();
        }
        idleMap.clear();
    }

    private class SShPooledConnexion extends ISShConnexionAdapter {
        private long lastUsed;
        NConnexionString connexionString;

        public SShPooledConnexion(NConnexionString connexionString, ISShConnexion connection) {
            super(connection);
            this.connexionString = connexionString;
            lastUsed = System.currentTimeMillis();
        }

        @Override
        protected ISShConnexion getConnection() {
            lastUsed = System.currentTimeMillis();
            return super.getConnection();
        }

        @Override
        public void close() {
            if (!connection.isAlive()) {
                silentDispose(this);
                return;
            }
            reset();
            if (!getIdle(connexionString).offer(this)) {
                silentDispose(this);
            } else {
                NLog.of(SshConnexionPool.class).log(NMsg.ofC("close ssh connexion %s", connexionString).withIntent(NMsgIntent.ADD).asDebug());
            }
        }

        public void dispose() throws Exception {
            super.close();
        }
    }
}
