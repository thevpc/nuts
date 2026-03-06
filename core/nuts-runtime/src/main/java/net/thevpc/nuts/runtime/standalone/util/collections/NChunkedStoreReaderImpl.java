package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPath;import net.thevpc.nuts.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NChunkedStoreReaderImpl<T>  {
    private NChunkedStoreUtils.PointerInfo ptr;
    private int unsavedRows;
    private InputStream inputStream;
    private NOptionalIterator<T> reader;
    private long readerIndex;

    private final int chunkNumberLayout;
    private final NPath folder;
    private final int maxUnsavedRows;
    private final NChunkedStoreFactory<T> factory;
    private NPath currentChunkFile;

    public NChunkedStoreReaderImpl(NPath folder, int bufferSize, int chunkNumberLayout, NChunkedStoreFactory<T> factory) {
        this.chunkNumberLayout = chunkNumberLayout <= 0 ? NChunkedStoreUtils.DEFAULT_CHUNK_NUMBER_LAYOUT : chunkNumberLayout;
        this.unsavedRows = 0;
        this.maxUnsavedRows = bufferSize <= 0 ? NChunkedStoreUtils.DEFAULT_READ_UNCOMMITTED : bufferSize;
        this.folder = NAssert.requireNamedNonNull(folder, "folder");
        this.factory = NAssert.requireNamedNonNull(factory, "factory");
        this.ptr = NChunkedStoreUtils.readPointerInfo(folder, NChunkedStoreUtils.READ_PTR_FILE_NAME);
    }


    public List<T> read(int count) {
        List<T> a = new ArrayList<>();
        int c = 0;
        while (c < count) {
            NOptional<T> n = read();
            if (n.isPresent()) {
                a.add(n.get());
                c++;
            } else {
                break;
            }
        }
        return a;
    }

    public NOptional<T> read() {
        if (ptr == null) {
            ptr = new NChunkedStoreUtils.PointerInfo(0, 0);
        }
        if (ptr.chunk < 0) {
            //writePointerInfo(ptr,folder,  READ_PTR_FILE_NAME);
            return NOptional.ofEmpty();
        }
        while (true) {
            if (currentChunkFile == null) {
                currentChunkFile = firstExistingChunk(ptr.chunk);
            }
            if (currentChunkFile == null || !currentChunkFile.exists()) {
                ptr = new NChunkedStoreUtils.PointerInfo(-1, -1);
                NChunkedStoreUtils.writePointerInfo(ptr, folder, NChunkedStoreUtils.READ_PTR_FILE_NAME);
                return NOptional.ofEmpty();
            }
            if (reader == null) {
                inputStream = currentChunkFile.getInputStream();
                reader = factory.scanner(inputStream);
            }
            T line = null;
            while (true) {
                NOptional<T> t = reader.next();
                if (!t.isPresent()) {
                    break;
                }
                line = t.get();
                if (readerIndex < ptr.index) {
                    readerIndex++;
                } else {
                    unsavedRows++;
                    if (unsavedRows >= maxUnsavedRows) {
                        flush();
                    }
                    readerIndex++;
                    ptr = new NChunkedStoreUtils.PointerInfo(ptr.chunk, readerIndex);
                    if (factory.accept(line)) {
                        return NOptional.ofNullable(line);
                    }
                }

            }
            flush();
            closeReader();
            ptr = new NChunkedStoreUtils.PointerInfo(ptr.chunk + 1, 0);
            currentChunkFile = null;
        }
    }

    private NPath firstExistingChunk(long index) {
        NPath file = folder.resolve(NChunkedStoreUtils.chunkFileName(ptr.chunk, chunkNumberLayout));
        if (file.exists()) {
            return file;
        }
        try (NStream<NPath> stream = folder.stream()) {
            NChunkedStoreUtils.FileAndNumber s = stream.filter(x -> x.getName().endsWith(NChunkedStoreUtils.CHUNK_EXT))
                    .map(NChunkedStoreUtils.FileAndNumber::new)
                    .filter(x -> x.number >= index).min(new NChunkedStoreUtils.FileAndNumberComparator()).orNull();
            return s == null ? null : s.path;
        }
    }

    private void closeReader() {
        if (reader != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
            inputStream = null;
            reader.close();
            reader = null;
        }
    }

    public void flush() {
        if (unsavedRows > 0) {
            NChunkedStoreUtils.writePointerInfo(ptr, folder, NChunkedStoreUtils.READ_PTR_FILE_NAME);
            unsavedRows = 0;
        }
    }


    public void close() {
        flush();
        closeReader();
    }
}
