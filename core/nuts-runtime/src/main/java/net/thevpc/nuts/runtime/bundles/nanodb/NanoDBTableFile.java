package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NanoDBTableFile<T> implements Iterable<T>, AutoCloseable {
    public static final String NANODB_TABLE_0_8_1 = "nanodb-table-0.8.1";
    private final Object tableLock = new Object();
    private final NanoDBSerializer<T> serializer;
    private final Map<String, IndexInfo> indexDefinitions = new HashMap<>();
    private final File dir;
    private final String tableName;
    private NanoDBOutputStream writeStream;
    private NanoDBInputStream readStream;
    private FileChannel readChannel;
    private final NanoDB db;
    private final NutsSession session0;

    public NanoDBTableFile(File dir, String tableName
            , NanoDBSerializer<T> serializer
            , NanoDB db
            , NanoDBIndexDefinition<T>[] indexDefinitions, NutsSession session0
    ) {
        this.session0 = session0;
        this.dir = dir;
        this.db = db;
        this.tableName = tableName;
        this.serializer = serializer;
        for (NanoDBIndexDefinition<T> indexDefinition : indexDefinitions) {
            this.indexDefinitions.put(indexDefinition.getIndexName(), new IndexInfo(indexDefinition));
        }
    }

    public static int getUTFLength(String s,NutsSession session) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int init = 0;
        try {
            DataOutputStream r = new DataOutputStream(o);
            r.flush();
            init = o.toByteArray().length;
            r.writeUTF(s);
            r.flush();
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
        return o.toByteArray().length - init;
    }

    public T get(long position, NutsSession session) {
        synchronized (tableLock) {
            if (readStream == null) {
                try {
                    FileInputStream readStreamFIS = new FileInputStream(getTableFile());
                    readChannel = readStreamFIS.getChannel();
                    readStream = new NanoDBDefaultInputStream(readStreamFIS, session);
                } catch (FileNotFoundException e) {
                    throw new NutsIOException(session, e);
                }
            }
            try {
                readChannel.position(position);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
            return serializer.read(readStream);
        }
    }

    public long add(T a, NutsSession session) {
        synchronized (tableLock) {
            File tableFile = getTableFile();
            boolean writeHeader = false;
            long len = 0;
            if (!tableFile.exists() || tableFile.length() == 0) {
                writeHeader = true;
            } else {
                len = tableFile.length();
            }
            if (writeStream == null) {
                try {
                    writeStream = new NanoDBDefaultOutputStream(new FileOutputStream(tableFile, true), session);
                } catch (FileNotFoundException e) {
                    throw new NutsIOException(session, e);
                }
            }
            if (writeHeader) {
                writeStream.writeUTF(NANODB_TABLE_0_8_1);
                writeStream.flush();
                len = writeStream.getPosition();
            }
            serializer.write(a, writeStream);
            updateIndices(a, len, session);
            return len;
        }
    }

    public void flush(NutsSession session) {
        synchronized (tableLock) {
            if (writeStream != null) {
                writeStream.flush();
            }
            for (IndexInfo value : indexDefinitions.values()) {
                value.flushIfDirty(session);
            }
        }
    }

    public void close() {
        synchronized (tableLock) {
            flush(session0);
            if (writeStream != null) {
                writeStream.flush();
                writeStream.close();
                writeStream = null;
            }
            if (readStream != null) {
                readStream.close();
                readStream = null;
            }
            if (readChannel != null) {
                readChannel = null;
            }
        }
    }

    public Stream<T> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                false);
    }

    public Iterable<T> items(NutsSession s) {
        return new Iterable<T>() {
            @NotNull
            @Override
            public Iterator<T> iterator() {
                return NanoDBTableFile.this.iterator(s);
            }
        };
    }

    public Iterator<T> iterator() {
        return iterator(session0);
    }

    public Iterator<T> iterator(NutsSession session) {
        return new Iterator<T>() {
            T nextValue;
            private NanoDBInputStream is;
            private boolean closed;
            private String header;

            @Override
            public boolean hasNext() {
                if (closed) {
                    return false;
                }
                synchronized (tableLock) {
                    if (is == null) {
                        if (getTableFile().exists()) {
                            try {
                                is = new NanoDBDefaultInputStream(
                                        new FileInputStream(getTableFile()), session
                                );
                            } catch (IOException ex) {
                                throw new NutsIOException(session, ex);
                            }
                            header = is.readUTF();
                        }
                    }
                    if (is != null) {
                        try {
                            nextValue = serializer.read(is);
                            return nextValue != null;
                        } catch (Exception ex) {
                            try {
                                is.close();
                            } catch (Exception ex2) {
                                //ignore any error
                            } finally {
                                is = null;
                                closed = true;
                            }
                            if (ex instanceof UncheckedIOException) {
                                if (ex.getCause() instanceof EOFException) {
                                    return false;
                                }
                            }
                            if (ex instanceof RuntimeException) {
                                throw (RuntimeException) ex;
                            }
                            if (ex instanceof IOException) {
                                throw new NutsIOException(session, ex);
                            }
                            throw new RuntimeException(ex);
                        }
                    }
                    return false;
                }
            }

            @Override
            public T next() {
                return nextValue;
            }
        };
    }

    public void updateIndices(T a, long objectId, NutsSession session) {
        synchronized (tableLock) {
            for (IndexInfo value : indexDefinitions.values()) {
                NanoDBIndex fi = value.getData(session);
                fi.put(value.getDefinition().getIndexedValue(a), objectId, session);
                value.dirty = true;
            }
        }
    }

    private File getTableFile() {
        return new File(dir, tableName + ".table");
    }

    public Stream<T> findByIndex(String indexName, Object value, NutsSession session) {
        return resolveIndexInfo(indexName).getData(session).get(value, session)
                .mapToObj(pos -> get(pos, session));
    }

    public <T> Stream<T> findIndexValues(String indexName, NutsSession session) {
        return resolveIndexInfo(indexName).getData(session).findAll(session);
    }

    public long getFileLength() {
        return getTableFile().length();
    }

    private IndexInfo resolveIndexInfo(String name) {
        IndexInfo y = indexDefinitions.get(name);
        if (y == null) {
            throw new IllegalArgumentException("not found index: " + name);
        }
        return y;
    }

    private class IndexInfo {
        NanoDBIndexDefinition def;
        NanoDBIndex data;
        boolean dirty;

        public IndexInfo(NanoDBIndexDefinition def) {
            this.def = def;
        }

        public NanoDBIndexDefinition getDefinition() {
            return def;
        }

        public void flushIfDirty(NutsSession session) {
            if (dirty) {
                flush(session);
                dirty = false;
            }
        }

        public void flush(NutsSession session) {
            synchronized (tableLock) {
                data.flush(session);
            }
        }

        public NanoDBIndex getData(NutsSession session) {
            synchronized (tableLock) {
                if (data != null) {
                    return data;
                }
                NanoDBIndex fi = db.createIndexFor(db.getSerializers().findSerializer(def.getIndexType(), def.isNullable()),
                        getIndexFile(),session
                );
                fi.load(session);
                data = fi;
                return fi;
            }
        }

        private File getIndexFile() {
            return new File(dir, tableName + "." + getIndexName() + ".index");
        }

        private String getIndexName() {
            return def.getIndexName();
        }
    }


}
