package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitRuleModel;
import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitRule;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class NRateLimitSlidingWindowRule implements NRateLimitRule {
    private String id;
    private int capacity;
    private Duration duration;
    private Deque<Long> timestamps; // record of last consumption timestamps (ms)

    public NRateLimitSlidingWindowRule(NRateLimitRuleModel model) {
        this.id = model.getId();
        this.capacity = model.getCapacity();
        this.duration = model.getDuration() == 0 ? null : Duration.ofMillis(model.getDuration());
        this.timestamps = deserialize(model.getConfig());
        // Optional: restore timestamps from model if you save them
    }

    @Override
    public synchronized boolean tryConsume(int count) {
        refill();
        if (timestamps.size() + count <= capacity) {
            long now = System.currentTimeMillis();
            for (int i = 0; i < count; i++) timestamps.addLast(now);
            return true;
        }
        return false;
    }

    @Override
    public synchronized long nextAvailableMillis(int count) {
        refill();
        if (timestamps.size() + count <= capacity) return 0;
        long now = System.currentTimeMillis();
        long oldest = timestamps.peekFirst();
        long wait = duration.toMillis() - (now - oldest);
        return Math.max(wait, 0);
    }

    public synchronized void refill() {
        long now = System.currentTimeMillis();
        long windowStart = now - duration.toMillis();
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.removeFirst();
        }
    }

    @Override
    public synchronized NRateLimitRuleModel toModel() {
        Instant lastAccess = timestamps.isEmpty() ? Instant.now() : Instant.ofEpochMilli(timestamps.getLast());
        return new NRateLimitRuleModel(id, NRateLimitDefaultStrategy.SLIDING_WINDOW.id(),
                capacity, duration==null?0:duration.toMillis(), capacity - timestamps.size(), lastAccess == null ? 0 : lastAccess.toEpochMilli(), serialize(timestamps));
    }


    // Serialize a Deque<Long> to byte[]
    private static byte[] serialize(Deque<Long> deque) {
        ByteBuffer buffer = ByteBuffer.allocate(deque.size() * Long.BYTES);
        for (Long timestamp : deque) {
            buffer.putLong(timestamp);
        }
        return buffer.array();
    }

    // Deserialize byte[] back to Deque<Long>
    private static Deque<Long> deserialize(byte[] bytes) {
        Deque<Long> deque = new ArrayDeque<>();
        if (bytes != null && bytes.length > 0) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.remaining() >= Long.BYTES) {
                    deque.add(buffer.getLong());
                }
            } catch (Exception ex) {
                //just ignore
            }
        }
        return deque;
    }

    @Override
    public String toString() {
        return "SlidingWindow{" +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                '}';
    }
}
