package net.thevpc.nuts.log;

import net.thevpc.nuts.io.NErr;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NTrace;
import net.thevpc.nuts.text.NMsg;

/**
 * NLogger interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NLogger {
    NLogger NULL = a -> {
    };
    NLogger STDOUT = a -> {
        NOut.println(a);
    };
    NLogger STDERR = a -> {
        NErr.println(a);
    };
    NLogger STDTRACE = a -> {
        NTrace.println(a);
    };

    /**
     * Log.
     *
     * @param message message
     */
    void log(NMsg message);
}
