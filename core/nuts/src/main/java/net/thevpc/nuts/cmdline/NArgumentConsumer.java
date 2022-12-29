package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NSession;

public interface NArgumentConsumer<T> {
    void run(T value, NArgument arg, NSession session);
}
