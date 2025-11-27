package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

public class SshConnectionPool {
    private final int maxSize;
    private long idleTimeout = 60000;
    private final Map<NConnectionString, BlockingQueue<SShPooledConnection>> idleMap;
    private final Function<NConnectionString, JCshSShConnection> factory;
    private Timer timer;


    public static SshConnectionPool of() {
        NWorkspace ws = NWorkspace.of();
        synchronized (ws) {
            SshConnectionPool u = ws.getProperty(SshConnectionPool.class).orNull();
            if (u != null) {
                return u;
            }
            SshConnectionPool r = new SshConnectionPool(200, 60000);
            ws.setProperty(SshConnectionPool.class.getName(), r);
            return r;
        }
    }

    public SshConnectionPool(int maxSize, long idleTimeout) {
        this(maxSize, idleTimeout, JCshSShConnection::new);
    }

    public SshConnectionPool(int maxSize, long idleTimeout, Function<NConnectionString, JCshSShConnection> factory) {
        this.maxSize = maxSize;
        this.idleTimeout = idleTimeout <= 0 ? 60000 : idleTimeout;
        this.factory = factory;
        this.idleMap = new HashMap<>();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                disposeOld(SshConnectionPool.this.idleTimeout);
            }
        }, idleTimeout);
    }

    public SshConnection acquire(String connectionString) {
        return acquire(NConnectionString.of(connectionString));
    }

    public SshConnection acquire(NConnectionString connectionString) {
        BlockingQueue<SShPooledConnection> idle = getIdle(connectionString);
        SShPooledConnection conn = idle.poll();
        if (conn != null && conn.isAlive()) {
            return conn;
        }

        // If the pool isn't full yet, create a new one
        synchronized (this) {
            if (idle.size() < maxSize) {
                NChronometer s = NChronometer.startNow();
                SShPooledConnection newConn = new SShPooledConnection(connectionString, factory.apply(connectionString));
                NLog.of(SshConnectionPool.class).log(NMsg.ofC("create ssh connection %s in %s", connectionString, s).withIntent(NMsgIntent.ADD).asDebug());
                return newConn;
            }
        }
        // Pool full: wait for someone to return one
        try {
            SShPooledConnection take = idle.take();
            NLog.of(SshConnectionPool.class).log(NMsg.ofC("acquire ssh connection %s", connectionString).withIntent(NMsgIntent.REMOVE).asDebug());
            return take;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized BlockingQueue<SShPooledConnection> getIdle(NConnectionString connectionString) {
        return idleMap.computeIfAbsent(connectionString, r -> new ArrayBlockingQueue<>(maxSize));
    }


    public synchronized void disposeOld(long timeoutMs) {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<NConnectionString, BlockingQueue<SShPooledConnection>>> iterator = idleMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<NConnectionString, BlockingQueue<SShPooledConnection>> e = iterator.next();
            BlockingQueue<SShPooledConnection> idle = e.getValue();
            for (Iterator<SShPooledConnection> iter = idle.iterator(); iter.hasNext(); ) {
                SShPooledConnection conn = iter.next();
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

    private void silentDispose(SShPooledConnection conn) {
        NLog.of(SshConnectionPool.class).log(NMsg.ofC("dispose ssh connection %s", conn.connectionString).withIntent(NMsgIntent.DISPOSE).asDebug());
        try {
            conn.dispose();
        } catch (Exception ignored) {
            // Streams vanish silently into the void
        }
    }

    public synchronized void close() {
        timer.cancel();
        for (Map.Entry<NConnectionString, BlockingQueue<SShPooledConnection>> e : idleMap.entrySet()) {
            BlockingQueue<SShPooledConnection> idle = e.getValue();
            for (SShPooledConnection conn : idle) {
                silentDispose(conn);
            }
            idle.clear();
        }
        idleMap.clear();
    }

    private class SShPooledConnection extends SshConnectionAdapter {
        private long lastUsed;
        NConnectionString connectionString;

        public SShPooledConnection(NConnectionString connectionString, SshConnection connection) {
            super(connection);
            this.connectionString = connectionString;
            lastUsed = System.currentTimeMillis();
        }

        @Override
        protected SshConnection getConnection() {
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
            if (!getIdle(connectionString).offer(this)) {
                silentDispose(this);
            } else {
                NLog.of(SshConnectionPool.class).log(NMsg.ofC("close ssh connection %s", connectionString).withIntent(NMsgIntent.ADD).asDebug());
            }
        }

        public void dispose() throws Exception {
            super.close();
        }
    }
}
