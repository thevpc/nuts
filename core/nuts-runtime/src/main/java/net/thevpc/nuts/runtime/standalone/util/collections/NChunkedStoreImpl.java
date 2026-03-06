package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.io.NCoreIOUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class NChunkedStoreImpl<T> implements NChunkedStore<T> {
    private final List<T> buffer = new ArrayList<>();
    //        private long writtenSize = 0;
    private final int dataBufferSize;
    private final int metadataBufferSize;
    private final int chunkSize;
    private final int chunkNumberLayout;
    //        private long chunkNumber;
    private final NPath folder;
    private final NChunkedStoreFactory<T> factory;
    private NChunkedStoreUtils.PointerInfo ptr;
    private boolean writingStarted = false;
    private final boolean append;

    public NChunkedStoreImpl(NPath folder, int chunkSize, boolean append, int dataBufferSize, int metadataBufferSize, int chunkNumberLayout, NChunkedStoreFactory<T> factory) {
        this.dataBufferSize = dataBufferSize <= 0 ? 1000 : dataBufferSize;
        this.metadataBufferSize = dataBufferSize <= 0 ? 10 : metadataBufferSize;
        this.append = append;
        this.chunkSize = chunkSize <= 0 ? Math.max(5000, dataBufferSize) : chunkSize;
        this.chunkNumberLayout = chunkNumberLayout <= 0 ? NChunkedStoreUtils.DEFAULT_CHUNK_NUMBER_LAYOUT : chunkNumberLayout;
        this.folder = NAssert.requireNamedNonNull(folder, "folder");
        this.factory = NAssert.requireNamedNonNull(factory, "factory");
    }


    public void add(T content) {
        if (!writingStarted) {
            folder.mkdirs();
            if (append) {
                ptr = NChunkedStoreUtils.readPointerInfo(folder, NChunkedStoreUtils.WRITE_PTR_FILE_NAME);
                if (ptr == null) {
                    ptr = new NChunkedStoreUtils.PointerInfo(0, -1);
                }
            } else {
                ptr = new NChunkedStoreUtils.PointerInfo(0, -1);
                try (NStream<NPath> stream = folder.stream()) {
                    stream.filter(x -> x.getName().endsWith(NChunkedStoreUtils.CHUNK_EXT) || x.getName().equals(NChunkedStoreUtils.READ_PTR_FILE_NAME) || x.getName().equals(NChunkedStoreUtils.WRITE_PTR_FILE_NAME))
                            .forEach(NPath::delete);
                }
            }
            writingStarted = true;
        }
        boolean acceptable = factory.accept(content);
        if (!acceptable) {
            if (content == null) {
                return;
            }
            throw new NIllegalArgumentException(NMsg.ofC("invalid value %s", content));
        }
        if (ptr.index + 1 >= chunkSize) {
            ptr = new NChunkedStoreUtils.PointerInfo(ptr.chunk + 1, 0);
            flush();
        } else {
            ptr = new NChunkedStoreUtils.PointerInfo(ptr.chunk, ptr.index + 1);
        }
        buffer.add(content);
        if (buffer.size() >= dataBufferSize) {
            flush();
        }
    }

    public NIterator<T> iterator() {
        NChunkedStoreReaderImpl<T> c = new NChunkedStoreReaderImpl<>(
                folder,
                metadataBufferSize,
                chunkNumberLayout,
                factory
        );
        return iterator(c);
    }

    private NIterator<T> iterator(NChunkedStoreReaderImpl<T> c) {
        return NIteratorBuilder.of(
                        new Iterator<T>() {
                            NOptional<T> n;

                            @Override
                            public boolean hasNext() {
                                if (n == null) {
                                    n = c.read();
                                }
                                return n.isPresent();
                            }

                            @Override
                            public T next() {
                                T t = n.get();
                                n = null;
                                return t;
                            }
                        }
                ).onFinish(c::close)
                .build();
    }

    @Override
    public boolean isEmpty() {
        try (NStream<T> stream = stream()) {
            return !stream.findAny().isPresent();
        }
    }

    @Override
    public long size() {
        try (NStream<T> stream = stream()) {
            return stream.count();
        }
    }

    public NStream<T> stream() {
        NChunkedStoreReaderImpl<T> c = new NChunkedStoreReaderImpl<>(
                folder,
                metadataBufferSize,
                chunkNumberLayout,
                factory
        );
        return NStream.ofIterator(
                iterator(c)
        ).onClose(c::close);
    }

    public void flush() {
        if (!buffer.isEmpty()) {
            try (OutputStream outs = folder.resolve(NChunkedStoreUtils.chunkFileName(ptr.chunk, chunkNumberLayout)).getOutputStream(
                    NPathOption.APPEND, NPathOption.WRITE, NPathOption.CREATE)
            ) {
                Consumer<T> fw = null;
                try {
                    fw = factory.appender(outs);
                    for (T c : buffer) {
                        fw.accept(c);
                    }
                } finally {
                    NCoreIOUtils.closeObject(fw);
                }
            } catch (IOException e) {
                throw NExceptions.ofUncheckedException(e);
            }
            buffer.clear();
            NChunkedStoreUtils.writePointerInfo(ptr, folder, NChunkedStoreUtils.WRITE_PTR_FILE_NAME);
            if (ptr.index + 1 >= chunkSize) {
                ptr = new NChunkedStoreUtils.PointerInfo(ptr.chunk + 1, 0);
            }
        }
    }

    @Override
    public void close() {
        flush();
    }
}
