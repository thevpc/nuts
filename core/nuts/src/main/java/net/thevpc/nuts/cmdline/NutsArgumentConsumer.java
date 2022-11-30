package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NutsSession;

public interface NutsArgumentConsumer<T> {
    void run(T value, NutsArgument arg, NutsSession session);
}
