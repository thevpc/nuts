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
    public static final int DEFAULT_SESSION_TIMEOUT = 60000;
    public static final int DEFAULT_POOL_MAX_SIZE = 200;
    private final int maxSize;
    private long idleTimeout = 60000;
    private final Map<NConnectionString, ConnectionQueue> idleMap;
    private final Function<NConnectionString, SshConnection> factory;
    private Timer timer;

    private static class ConnectionQueue {
        NConnectionString connectionString;
        BlockingQueue<SShPooledConnection> available;
        List<SShPooledConnection> running;

        public ConnectionQueue(NConnectionString connectionString, int maxSize) {
            this.connectionString = connectionString;
            this.available = new ArrayBlockingQueue<>(maxSize);
            this.running = new ArrayList<>();
        }

        public synchronized void disposeOld(long timeoutMs, long now) {
            for (Iterator<SShPooledConnection> iter = available.iterator(); iter.hasNext(); ) {
                SShPooledConnection conn = iter.next();
                if (conn.lastUsed + timeoutMs >= now || !conn.isAlive()) {
                    silentDispose(conn);
                    iter.remove();
                }
            }
        }

        boolean isEmpty() {
            return available.isEmpty() && running.isEmpty();
        }

        public void close() {
            for (SShPooledConnection conn : available) {
                silentDispose(conn);
            }
            available.clear();
        }
    }

    public static SshConnectionPool of() {
        NWorkspace ws = NWorkspace.of();
        synchronized (ws) {
            SshConnectionPool u = ws.getProperty(SshConnectionPool.class).orNull();
            if (u != null) {
                return u;
            }
            SshConnectionPool r = new SshConnectionPool(DEFAULT_POOL_MAX_SIZE, DEFAULT_SESSION_TIMEOUT);
            ws.setProperty(SshConnectionPool.class.getName(), r);
            return r;
        }
    }

    public SshConnectionPool(int maxSize, long idleTimeout) {
        this(maxSize, idleTimeout, connectionString -> {
            return ScoredConnectionFactory.resolveBinSshConnectionPool(connectionString).call();
        });
    }


    public SshConnectionPool(int maxSize, long idleTimeout, Function<NConnectionString, SshConnection> factory) {
        this.maxSize = maxSize;
        this.idleTimeout = idleTimeout <= 0 ? DEFAULT_SESSION_TIMEOUT : idleTimeout;
        this.factory = factory;
        this.idleMap = new HashMap<>();
        this.timer = new Timer("SshConnectionPoolGarbleCollector",true);
        long period = Math.max(this.idleTimeout / 10,1000);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                disposeOld(SshConnectionPool.this.idleTimeout);
            }
        }, period,period);
    }

    public SshConnection acquire(String connectionString) {
        return acquire(NConnectionString.of(connectionString));
    }

    public SshConnection acquire(NConnectionString connectionString) {
        ConnectionQueue idle = getIdle(connectionString);
        SShPooledConnection conn = idle.available.poll();
        if (conn != null && conn.isAlive()) {
            return conn;
        }

        // If the pool isn't full yet, create a new one
        synchronized (this) {
            if (idle.available.size() < maxSize) {
                NChronometer s = NChronometer.startNow();
                SShPooledConnection newConn = new SShPooledConnection(connectionString, factory.apply(connectionString));
                NLog.of(SshConnectionPool.class).log(NMsg.ofC("create ssh connection %s in %s", connectionString, s).withIntent(NMsgIntent.ADD).asDebug());
                idle.running.add(newConn);
                return newConn;
            }
        }
        // Pool full: wait for someone to return one
        try {
            SShPooledConnection take = idle.available.take();
            NLog.of(SshConnectionPool.class).log(NMsg.ofC("acquire ssh connection %s", connectionString).withIntent(NMsgIntent.REMOVE).asDebug());
            return take;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized ConnectionQueue getIdle(NConnectionString connectionString) {
        NConnectionString normalized = connectionString.builder().setPath(null).setNormalized(true).build();
        return idleMap.computeIfAbsent(normalized, r -> new ConnectionQueue(r,maxSize));
    }


    public synchronized void disposeOld(long timeoutMs) {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<NConnectionString, ConnectionQueue>> iterator = idleMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<NConnectionString, ConnectionQueue> e = iterator.next();
            ConnectionQueue idle = e.getValue();
            idle.disposeOld(timeoutMs, now);
            if (idle.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static void silentDispose(SShPooledConnection conn) {
        NLog.of(SshConnectionPool.class).log(NMsg.ofC("dispose ssh connection %s", conn.connectionString).withIntent(NMsgIntent.DISPOSE).asDebug());
        try {
            conn.dispose();
        } catch (Exception ignored) {
            // Streams vanish silently into the void
        }
    }

    public synchronized void close() {
        timer.cancel();
        for (Map.Entry<NConnectionString, ConnectionQueue> e : idleMap.entrySet()) {
            ConnectionQueue idle = e.getValue();
            idle.close();
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
        public SshConnection getConnection() {
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

            ConnectionQueue cq = getIdle(connectionString);
            cq.running.remove(this);
            if (!cq.available.offer(this)) {
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
