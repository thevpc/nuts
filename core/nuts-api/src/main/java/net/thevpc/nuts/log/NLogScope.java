package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgSupplier;

import java.util.Map;
import java.util.function.Supplier;

public interface NLogScope extends NBlankable {
    static NLogScope of() {
        return NLogs.of().newContext();
    }

    Map<String, Supplier<?>> getPlaceholders();

    Object getPlaceholder(String name);

    NLogScope withMessagePrefix(NMsg prefix);

    NLogScope withMessagePrefix(NMsgSupplier<NMsg> prefix);

    NLogScope withMessageSuffix(NMsgSupplier<NMsg> suffix);

    NLogScope withMessageSuffix(NMsg suffix);

    NLogScope withPlaceholders(Map<String, ?> map);

    NLogScope withPlaceholderSuppliers(Map<String, Supplier<?>> map);

    NLogScope withPlaceholder(String key, Object value);

    NLogScope withPlaceholderSupplier(String key, Supplier<?> supplier);

    NLogScope withLog(NLog logger);

    NLogScope withLog(NLogSPI logger);

    NLogScope withLog(String name, NLogSPI logger);

    NLogScope mergedWith(NLogScope other);

    NMsgSupplier<NMsg> getMessagePrefix();

    NMsgSupplier<NMsg> getMessageSuffix();

    NLog getLog();

    @Override
    boolean isBlank();
}
