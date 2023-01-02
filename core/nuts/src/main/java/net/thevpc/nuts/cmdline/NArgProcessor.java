package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NSession;

public interface NArgProcessor<T> {
    void run(T value, NArg arg, NSession session);
}
