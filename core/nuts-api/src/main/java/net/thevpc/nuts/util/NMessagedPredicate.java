package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * NMessagedPredicate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMessagedPredicate<T> {
    /**
     * Filter.
     *
     * @return filter result
     */
    Predicate<T> filter();

    /**
     * Message.
     *
     * @return message result
     */
    Supplier<NMsg> message();
}
