package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsSession;

public interface NanoDBSerializer<T> {

    void write(T obj, NanoDBOutputStream out, NutsSession session);

    T read(NanoDBInputStream in, Class expectedType, NutsSession session);

    Class<T> getSupportedType();
}
