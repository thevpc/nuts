//package net.thevpc.nuts.runtime.standalone.util.collections;
//
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.util.NChunkedStoreFactory;
//import net.thevpc.nuts.util.NChunkedStoreReader;
//import net.thevpc.nuts.util.NChunkedStoreReaderBuilder;
//
//public class NChunkedStoreReaderBuilderImpl<T> implements NChunkedStoreReaderBuilder<T> {
//    private NPath folder;
//    private int bufferSize;
//    private int numberLayout;
//    private NChunkedStoreFactory<T> factory;
//
//    public static <T> NChunkedStoreReaderBuilder<T> of(NPath folder, NChunkedStoreFactory<T> factory) {
//        return new NChunkedStoreReaderBuilderImpl<T>().setFolder(folder).setFactory(factory);
//    }
//
//    @Override
//    public NPath getFolder() {
//        return folder;
//    }
//
//    @Override
//    public NChunkedStoreReaderBuilder<T> setFolder(NPath folder) {
//        this.folder = folder;
//        return this;
//    }
//
//    @Override
//    public int getBufferSize() {
//        return bufferSize;
//    }
//
//    @Override
//    public NChunkedStoreReaderBuilder<T> setBufferSize(int bufferSize) {
//        this.bufferSize = bufferSize;
//        return this;
//    }
//
//    @Override
//    public int getNumberLayout() {
//        return numberLayout;
//    }
//
//    @Override
//    public NChunkedStoreReaderBuilder<T> setNumberLayout(int numberLayout) {
//        this.numberLayout = numberLayout;
//        return this;
//    }
//
//    @Override
//    public NChunkedStoreFactory<T> getFactory() {
//        return factory;
//    }
//
//    @Override
//    public NChunkedStoreReaderBuilder<T> setFactory(NChunkedStoreFactory<T> factory) {
//        this.factory = factory;
//        return this;
//    }
//
//    @Override
//    public NChunkedStoreReader<T> build() {
//        return new NChunkedStoreReaderImpl<>(folder, bufferSize, numberLayout, factory);
//    }
//}
