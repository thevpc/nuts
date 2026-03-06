package net.thevpc.nuts.util;

public interface NOptionalIterator<T> extends AutoCloseable {
    /**
     * never returns null, always return an optional
     * @return
     */
    NOptional<T> next();

    @Override
    default void close(){

    }
}
