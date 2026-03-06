package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.io.NPath;

public interface NChunkedStoreBuilder<T> {

    static NChunkedStoreBuilder<String> ofLines(NPath folder) {
        NCollectionsRPI r = NCollectionsRPI.of();
        return r.chunkedStoreBuilder(folder, r.lineChunkedStoreFactory());
    }

    static <T> NChunkedStoreBuilder<T> of(NPath folder, NChunkedStoreFactory<T> storeFactory) {
        return NCollectionsRPI.of().chunkedStoreBuilder(folder, storeFactory);
    }

    int getMetadataBufferSize();

    NChunkedStoreBuilder<T> setMetadataBufferSize(int metadataBufferSize);

    NPath getFolder();

    NChunkedStoreBuilder<T> setFolder(NPath folder);

    NChunkedStoreFactory<T> getFactory();

    NChunkedStoreBuilder<T> setFactory(NChunkedStoreFactory<T> factory);

    int getChunkSize();

    NChunkedStoreBuilder<T> setChunkSize(int chunkSize);

    boolean isAppend();

    NChunkedStoreBuilder<T> setAppend(boolean append);

    int getDataBufferSize();

    NChunkedStoreBuilder<T> setBufferSize(int bufferSize);

    int getNumberLayout();

    NChunkedStoreBuilder<T> setNumberLayout(int numberLayout);

    NChunkedStore<T> build();
}
