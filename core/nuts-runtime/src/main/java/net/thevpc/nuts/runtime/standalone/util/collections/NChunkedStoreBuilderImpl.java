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
        return new NChunkedStoreBuilderImpl<T>().setFolder(folder).setFactory(factory);
    }

    public int getMetadataBufferSize() {
        return metadataBufferSize;
    }

    public NChunkedStoreBuilder<T> setMetadataBufferSize(int metadataBufferSize) {
        this.metadataBufferSize = metadataBufferSize;
        return this;
    }

    @Override
    public NPath getFolder() {
        return folder;
    }

    @Override
    public NChunkedStoreBuilder<T> setFolder(NPath folder) {
        this.folder = folder;
        return this;
    }

    @Override
    public NChunkedStoreFactory<T> getFactory() {
        return factory;
    }

    @Override
    public NChunkedStoreBuilder<T> setFactory(NChunkedStoreFactory<T> factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public NChunkedStoreBuilder<T> setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    @Override
    public boolean isAppend() {
        return append;
    }

    @Override
    public NChunkedStoreBuilder<T> setAppend(boolean append) {
        this.append = append;
        return this;
    }

    @Override
    public int getDataBufferSize() {
        return dataBufferSize;
    }

    @Override
    public NChunkedStoreBuilder<T> setBufferSize(int bufferSize) {
        this.dataBufferSize = bufferSize;
        return this;
    }

    @Override
    public int getNumberLayout() {
        return numberLayout;
    }

    @Override
    public NChunkedStoreBuilder<T> setNumberLayout(int numberLayout) {
        this.numberLayout = numberLayout;
        return this;
    }

    @Override
    public NChunkedStore<T> build() {
        return new NChunkedStoreImpl<>(folder, chunkSize, append, dataBufferSize, metadataBufferSize, numberLayout, factory);
    }
}
