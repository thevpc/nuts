package net.thevpc.nuts.runtime.bundles.nanodb;

public interface NanoDBSerializer<T> {

    void write(T obj, NanoDBOutputStream out);

    T read(NanoDBInputStream in);

    Class<T> getSupportedType();
}
