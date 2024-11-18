package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public interface NanoDBSerializer<T> {

    void write(T obj, NanoDBOutputStream out);

    T read(NanoDBInputStream in, Class expectedType);

    Class<T> getSupportedType();
}
