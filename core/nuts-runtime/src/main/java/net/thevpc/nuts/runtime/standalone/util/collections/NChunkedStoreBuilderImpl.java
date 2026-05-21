package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NChunkedStoreFactory;
import net.thevpc.nuts.util.NChunkedStore;
import net.thevpc.nuts.util.NChunkedStoreBuilder;

public class NChunkedStoreBuilderImpl<T> implements NChunkedStoreBuilder<T> {
    private NPath folder;
    private NChunkedStoreFactory<T> factory;
    private int chunkSize;
    private boolean append;
    private int dataBufferSize;
    private int metadataBufferSize;
    private int numberLayout;

    public static <T> NChunkedStoreBuilder<T> of(NPath folder, NChunkedStoreFactory<T> factory) {
        return new NChunkedStoreBuilderImpl<T>().folder(folder).factory(factory);
    }

    public int metadataBufferSize() {
        return metadataBufferSize;
    }

    public NChunkedStoreBuilder<T> metadataBufferSize(int metadataBufferSize) {
        this.metadataBufferSize = metadataBufferSize;
        return this;
    }

    @Override
    public NPath folder() {
        return folder;
    }

    @Override
    public NChunkedStoreBuilder<T> folder(NPath folder) {
        this.folder = folder;
        return this;
    }

    @Override
    public NChunkedStoreFactory<T> factory() {
        return factory;
    }

    @Override
    public NChunkedStoreBuilder<T> factory(NChunkedStoreFactory<T> factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public int chunkSize() {
        return chunkSize;
    }

    @Override
    public NChunkedStoreBuilder<T> chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    @Override
    public boolean isAppend() {
        return append;
    }

    @Override
    public NChunkedStoreBuilder<T> append(boolean append) {
        this.append = append;
        return this;
    }

    @Override
    public int dataBufferSize() {
        return dataBufferSize;
    }

    @Override
    public NChunkedStoreBuilder<T> bufferSize(int bufferSize) {
        this.dataBufferSize = bufferSize;
        return this;
    }

    @Override
    public int numberLayout() {
        return numberLayout;
    }

    @Override
    public NChunkedStoreBuilder<T> numberLayout(int numberLayout) {
        this.numberLayout = numberLayout;
        return this;
    }

    @Override
    public NChunkedStore<T> build() {
        return new NChunkedStoreImpl<>(folder, chunkSize, append, dataBufferSize, metadataBufferSize, numberLayout, factory);
    }
}
