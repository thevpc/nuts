package net.thevpc.nuts.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface NChunkedStoreFactory<T> {
    boolean accept(T any);

    NOptionalIterator<T> scanner(InputStream inputStream);

    Consumer<T> appender(OutputStream outputStream);
}
