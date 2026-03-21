package net.thevpc.nuts.log;

import net.thevpc.nuts.io.NErr;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NTrace;
import net.thevpc.nuts.text.NMsg;

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

    void log(NMsg message);
}
