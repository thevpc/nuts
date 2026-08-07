package net.thevpc.nuts.runtime.standalone.xtra.nanodb.file;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.collections.NBPlusTree;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;

import java.io.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NanoDBDefaultIndex<T extends Comparable<T>> extends NanoDBAbstractIndex<T> implements NanoDBIndex<T>, Closeable {
    private final File file;
    private final Class<T> keyType;
    private NBPlusTree<T, Long> tree;
    private NPageStore pageStore;

    private static final NDataSerializer<Long> LONG_SERIALIZER = new NDataSerializer<Long>() {
        @Override
        public void serialize(Long obj, DataOutputStream dos) throws IOException {
            dos.writeLong(obj);
        }

        @Override
        public Long deserialize(DataInputStream dis) throws IOException {
            return dis.readLong();
        }
    };

    public NanoDBDefaultIndex(Class<T> keyType, NanoDBSerializer<T> ser, File file) {
        super(ser);
        this.keyType = keyType;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public void load() {
        if (tree == null) {
            try {
                if (file != null) {
                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }
                    pageStore = NPageStore.ofFile(net.thevpc.nuts.io.NPath.of(file), 4096);
                } else {
                    pageStore = NPageStore.ofInMemory(4096);
                }
                tree = NBPlusTree.of(
                        pageStore,
                        5,
                        true,
                        new NanoDBNDataSerializer<>(keyType, ser),
                        LONG_SERIALIZER
                );
            } catch (Exception e) {
                throw new NIOException(e);
            }
        }
    }

    @Override
    public void flush() {
        if (pageStore != null) {
            try {
                pageStore.flush();
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
    }

    @Override
    public void put(T s, long position) {
        load();
        tree.put(s, position);
    }

    @Override
    public LongStream get(T s) {
        load();
        return tree.search(s).stream().mapToLong(Long::longValue);
    }

    @Override
    public void clear() {
        load();
        tree.clear();
    }

    @Override
    public Stream<T> findAll() {
        load();
        return tree.keySet().stream();
    }

    @Override
    public void close() throws IOException {
        if (tree != null) {
            try {
                tree.close();
            } catch (Exception e) {
                // ignore
            }
            tree = null;
            pageStore = null;
        }
    }
}
