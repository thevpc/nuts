package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogContext;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgSupplier;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NLogContextImpl implements NLogContext {
    private final NMsgSupplier<NMsg> messagePrefix;
    private final NMsgSupplier<NMsg> messageSuffix;
    private final Map<String, Supplier<?>> properties;
    private final NLog log;
    public static final NLogContext BLANK = new NLogContextImpl(null, null, Collections.emptyMap(), null);

    @Override
    public Map<String, Supplier<?>> getPlaceholders() {
        return new LinkedHashMap<>(properties);
    }

    @Override
    public Object getPlaceholder(String name) {
        Supplier<?> n = properties.get(name);
        if (n != null) {
            return n.get();
        }
        return null;
    }

    private NLogContextImpl(NMsgSupplier<NMsg> messagePrefix, NMsgSupplier<NMsg> messageSuffix, Map<String, Supplier<?>> properties, NLog log) {
        this.messagePrefix = messagePrefix;
        this.messageSuffix = messageSuffix;
        this.properties = properties;
        this.log = log;
    }

    @Override
    public NLogContext withLog(NLog logger) {
        if (logger == log) {
            return this;
        }
        return new NLogContextImpl(messagePrefix, messageSuffix, properties, logger);
    }

    @Override
    public NLogContext withLog(NLogSPI logger) {
        return withLog(NLog.of(null, logger));
    }

    @Override
    public NLogContext withLog(String name, NLogSPI logger) {
        return withLog(NLog.of(name, logger));
    }

    @Override
    public NLogContext withMessagePrefix(NMsg prefix) {
        return new NLogContextImpl(prefix == null ? null : m -> prefix, this.messageSuffix, this.properties, log);
    }

    @Override
    public NLogContext withMessageSuffix(NMsg suffix) {
        return new NLogContextImpl(messagePrefix, suffix == null ? null : m -> suffix, this.properties, log);
    }

    @Override
    public NLogContext withMessagePrefix(NMsgSupplier<NMsg> prefix) {
        return new NLogContextImpl(prefix, this.messageSuffix, this.properties, log);
    }

    @Override
    public NLogContext withMessageSuffix(NMsgSupplier<NMsg> suffix) {
        return new NLogContextImpl(messagePrefix, suffix, this.properties, log);
    }

    @Override
    public NLogContext withPlaceholders(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return this;
        }
        Map<String, Supplier<?>> properties2 = new HashMap<>();
        for (Map.Entry<String, ?> e : map.entrySet()) {
            String s = e.getKey();
            Object value = e.getValue();
            if (value != null) {
                properties2.put(s, new ConstSupplier<>(value));
            } else {
                properties2.remove(s);
            }
        }
        return new NLogContextImpl(this.messagePrefix, this.messageSuffix, properties2, log);
    }

    @Override
    public NLogContext withPlaceholderSuppliers(Map<String, Supplier<?>> map) {
        if (map == null || map.isEmpty()) {
            return this;
        }
        Map<String, Supplier<?>> properties2 = new HashMap<>();
        for (Map.Entry<String, Supplier<?>> e : map.entrySet()) {
            String s = e.getKey();
            Supplier<?> value = e.getValue();
            if (value != null) {
                properties2.put(s, value);
            } else {
                properties2.remove(s);
            }
        }
        return new NLogContextImpl(this.messagePrefix, this.messageSuffix, properties2, log);
    }

    @Override
    public NLogContextImpl withPlaceholder(String key, Object value) {
        NAssert.requireNonNull(key, "key");
        if (value != null) {
            Map<String, Supplier<?>> properties2 = new HashMap<>(properties);
            properties2.put(key, new ConstSupplier<>(value));
            return new NLogContextImpl(messagePrefix, messageSuffix, properties2, log);
        } else {
            if (this.properties.containsKey(key)) {
                Map<String, Supplier<?>> properties2 = new HashMap<>(properties);
                properties2.remove(key);
                return new NLogContextImpl(messagePrefix, messageSuffix, properties2, log);
            } else {
                return this;
            }
        }
    }

    @Override
    public NLogContext withPlaceholderSupplier(String key, Supplier<?> supplier) {
        NAssert.requireNonNull(key, "key");
        if (supplier != null) {
            Map<String, Supplier<?>> properties2 = new HashMap<>(properties);
            properties2.put(key, supplier);
            return new NLogContextImpl(messagePrefix, messageSuffix, properties2, log);
        } else {
            if (this.properties.containsKey(key)) {
                Map<String, Supplier<?>> properties2 = new HashMap<>(properties);
                properties2.remove(key);
                return new NLogContextImpl(messagePrefix, messageSuffix, properties2, log);
            } else {
                return this;
            }
        }
    }

    @Override
    public NLogContext mergedWith(NLogContext other) {
        if (other == null) {
            return this;
        }
        if (other.isBlank()) {
            return this;
        }
        if (this.isBlank()) {
            return other;
        }
        NLog newLog = log;
        if (other.getLog() != null) {
            newLog = other.getLog();
        }
        NMsgSupplier<NMsg> prefix2 = mergeBoundaries(messagePrefix, other.getMessagePrefix());
        NMsgSupplier<NMsg> suffix2 = mergeBoundaries(messageSuffix, other.getMessageSuffix());
        Map<String, Supplier<?>> properties2 = new LinkedHashMap<>(this.properties);
        for (Map.Entry<String, Supplier<?>> e : other.getPlaceholders().entrySet()) {
            if (e.getValue() != null) {
                properties2.put(e.getKey(), e.getValue());
            } else {
                properties2.remove(e.getKey());
            }
        }
        return new NLogContextImpl(prefix2, suffix2, properties2, newLog);
    }

    private NMsgSupplier<NMsg> mergeBoundaries(NMsgSupplier<NMsg> a, NMsgSupplier<NMsg> b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return msg -> {
            NMsg aa = a.apply(msg);
            NMsg bb = b.apply(msg);
            if (aa == null) {
                return bb;
            }
            if (bb == null) {
                return aa;
            }
            return NMsg.ofC("%s %s", aa, bb);
        };
    }

    @Override
    public NMsgSupplier<NMsg> getMessagePrefix() {
        return messagePrefix;
    }

    @Override
    public NMsgSupplier<NMsg> getMessageSuffix() {
        return messageSuffix;
    }

    @Override
    public boolean isBlank() {
        if (!NBlankable.isBlank(messagePrefix)) {
            return false;
        }
        if (!NBlankable.isBlank(messageSuffix)) {
            return false;
        }
        if (!properties.isEmpty()) {
            return false;
        }
        if (log != null) {
            return false;
        }
        return true;
    }

    private static class ConstSupplier<T> implements Supplier<T> {
        private T value;

        public ConstSupplier(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }
    }

    @Override
    public NLog getLog() {
        return log;
    }

}
