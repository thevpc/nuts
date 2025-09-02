package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.Map;
import java.util.function.Supplier;

public interface NLogContext extends NBlankable {
    static NLogContext ofPlaceholder(String name, Object value) {
        return NLogs.of().newContext().withPlaceholder(name, value);
    }

    static NLogContext ofMessagePrefix(NMsg prefix) {
        return NLogs.of().newContext().withMessagePrefix(prefix);
    }

    static NLogContext ofMessageSuffix(NMsg suffix) {
        return NLogs.of().newContext().withMessageSuffix(suffix);
    }

    static NLogContext ofPlaceholderSuppliers(Map<String, Supplier<?>> properties) {
        return NLogs.of().newContext().withPlaceholderSuppliers(properties);
    }

    static NLogContext ofPlaceholders(Map<String, ?> properties) {
        return NLogs.of().newContext().withPlaceholders(properties);
    }

    static NLogContext ofLog(Class<?> log) {
        return NLogs.of().newContext().withLog(log == null ? null : NLog.of(log));
    }

    static NLogContext ofLog(String log) {
        return NLogs.of().newContext().withLog(log == null ? null : NLog.of(log));
    }

    static NLogContext ofLog(NLog log) {
        return NLogs.of().newContext().withLog(log);
    }

    static NLogContext ofLog(NLogSPI log) {
        return NLogs.of().newContext().withLog(log);
    }

    static NLogContext ofLog(String name, NLogSPI log) {
        return NLogs.of().newContext().withLog(name, log);
    }

    static NLogContext of() {
        return NLogs.of().newContext();
    }

    Map<String, Supplier<?>> getPlaceholders();

    Object getPlaceholder(String name);

    NLogContext withMessagePrefix(NMsg prefix);

    NLogContext withMessageSuffix(NMsg suffix);

    NLogContext withPlaceholders(Map<String, ?> map);

    NLogContext withPlaceholderSuppliers(Map<String, Supplier<?>> map);

    NLogContext withPlaceholder(String key, Object value);

    NLogContext withPlaceholderSupplier(String key, Supplier<?> supplier);

    NLogContext withLog(NLog logger);

    NLogContext withLog(NLogSPI logger);

    NLogContext withLog(String name, NLogSPI logger);

    NLogContext mergedWith(NLogContext other);

    NMsg getMessagePrefix();

    NMsg getMessageSuffix();

    NLog getLog();

    @Override
    boolean isBlank();
}
