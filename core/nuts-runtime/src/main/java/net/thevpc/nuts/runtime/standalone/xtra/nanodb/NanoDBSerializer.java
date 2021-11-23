package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

public interface NanoDBSerializer<T> {

    void write(T obj, NanoDBOutputStream out, NutsSession session);

    T read(NanoDBInputStream in, Class expectedType, NutsSession session);

    Class<T> getSupportedType();
}
