package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

public interface NanoDBSerializer<T> {

    void write(T obj, NanoDBOutputStream out, NSession session);

    T read(NanoDBInputStream in, Class expectedType, NSession session);

    Class<T> getSupportedType();
}
