package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.io.NPath;

public interface NChunkedStoreBuilder<T> {

    static NChunkedStoreBuilder<String> ofLines(NPath folder) {
        NUtilsRPI r = NUtilsRPI.of();
        return r.chunkedStoreBuilder(folder, r.lineChunkedStoreFactory());
    }

    static <T> NChunkedStoreBuilder<T> of(NPath folder, NChunkedStoreFactory<T> storeFactory) {
        return NUtilsRPI.of().chunkedStoreBuilder(folder, storeFactory);
    }

    int metadataBufferSize();

    NChunkedStoreBuilder<T> metadataBufferSize(int metadataBufferSize);

    NPath folder();

    NChunkedStoreBuilder<T> folder(NPath folder);

    NChunkedStoreFactory<T> factory();

    NChunkedStoreBuilder<T> factory(NChunkedStoreFactory<T> factory);

    int chunkSize();

    NChunkedStoreBuilder<T> chunkSize(int chunkSize);

    boolean isAppend();

    NChunkedStoreBuilder<T> append(boolean append);

    int dataBufferSize();

    NChunkedStoreBuilder<T> bufferSize(int bufferSize);

    int numberLayout();

    NChunkedStoreBuilder<T> numberLayout(int numberLayout);

    NChunkedStore<T> build();
}
