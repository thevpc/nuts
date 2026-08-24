package net.thevpc.nuts.log;

import net.thevpc.nuts.internal.rpi.NLogRPI;
import net.thevpc.nuts.spi.NLogSPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgSupplier;

import java.util.Map;
import java.util.function.Supplier;

/**
 * NLogScope interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLogScope extends NBlankable {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NLogScope of() {
        return NLogRPI.of().createScope();
    }
    /**
     * Current.
     *
     * @return current result
     */
    static NLogScope current() {
        return NLogRPI.of().currentScope();
    }

    /**
     * Placeholders.
     *
     * @return placeholders result
     */
    Map<String, Supplier<?>> placeholders();

    /**
     * Returns the placeholder.
     *
     * @param name name
     * @return get placeholder result
     */
    Object getPlaceholder(String name);

    /**
     * With message prefix.
     *
     * @param prefix prefix
     * @return with message prefix result
     */
    NLogScope withMessagePrefix(NMsg prefix);

    /**
     * With message prefix.
     *
     * @param prefix prefix
     * @return with message prefix result
     */
    NLogScope withMessagePrefix(NMsgSupplier<NMsg> prefix);

    /**
     * With message suffix.
     *
     * @param suffix suffix
     * @return with message suffix result
     */
    NLogScope withMessageSuffix(NMsgSupplier<NMsg> suffix);

    /**
     * With message suffix.
     *
     * @param suffix suffix
     * @return with message suffix result
     */
    NLogScope withMessageSuffix(NMsg suffix);

    /**
     * With placeholders.
     *
     * @param map map
     * @return with placeholders result
     */
    NLogScope withPlaceholders(Map<String, ?> map);

    /**
     * With placeholder suppliers.
     *
     * @param map map
     * @return with placeholder suppliers result
     */
    NLogScope withPlaceholderSuppliers(Map<String, Supplier<?>> map);

    /**
     * With placeholder.
     *
     * @param key key
     * @param value value
     * @return with placeholder result
     */
    NLogScope withPlaceholder(String key, Object value);

    /**
     * With placeholder supplier.
     *
     * @param key key
     * @param supplier supplier
     * @return with placeholder supplier result
     */
    NLogScope withPlaceholderSupplier(String key, Supplier<?> supplier);

    /**
     * With log.
     *
     * @param logger logger
     * @return with log result
     */
    NLogScope withLog(NLog logger);

    /**
     * With log.
     *
     * @param logger logger
     * @return with log result
     */
    NLogScope withLog(NLogSPI logger);

    /**
     * With log.
     *
     * @param name name
     * @param logger logger
     * @return with log result
     */
    NLogScope withLog(String name, NLogSPI logger);

    /**
     * Merged with.
     *
     * @param other other
     * @return merged with result
     */
    NLogScope mergedWith(NLogScope other);

    /**
     * Message prefix.
     *
     * @return message prefix result
     */
    NMsgSupplier<NMsg> messagePrefix();

    /**
     * Message suffix.
     *
     * @return message suffix result
     */
    NMsgSupplier<NMsg> messageSuffix();

    /**
     * Log.
     *
     * @return log result
     */
    NLog log();

    @Override
    boolean isBlank();
}
