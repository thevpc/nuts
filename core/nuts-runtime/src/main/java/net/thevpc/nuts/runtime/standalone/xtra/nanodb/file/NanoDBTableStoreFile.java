package net.thevpc.nuts.runtime.standalone.xtra.nanodb.file;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.StreamSupport;

public class NanoDBTableStoreFile<T> implements NanoDBTableStore<T> {
    public static final String NANODB_TABLE_0_8_1 = "nanodb-table-0.8.1";
    private final Object tableLock = new Object();
    private final NanoDBSerializer<T> serializer;
    private final Map<String, IndexInfo> indexDefinitions = new HashMap<>();
    private final File dir;
    private final String tableName;
    private final NanoDB db;
    private final NWorkspace workspace;
    private NanoDBOutputStream writeStream;
    private NanoDBInputStream readStream;
    private FileChannel readChannel;
    private Class<T> rowType;

    public NanoDBTableStoreFile(Class<T> rowType, File dir, String tableName
            , NanoDBSerializer<T> serializer
            , NanoDB db
            , NanoDBIndexDefinition<T>[] indexDefinitions, NWorkspace workspace
    ) {
        this.workspace = workspace;
        this.rowType = rowType;
        this.dir = dir;
        this.db = db;
        this.tableName = tableName;
        this.serializer = serializer;
        for (NanoDBIndexDefinition<T> indexDefinition : indexDefinitions) {
            this.indexDefinitions.put(indexDefinition.getIndexName(), new IndexInfo(indexDefinition));
        }
    }

    public static int getUTFLength(String s) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int init = 0;
        try {
            DataOutputStream r = new DataOutputStream(o);
            r.flush();
            init = o.toByteArray().length;
            r.writeUTF(s);
            r.flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return o.toByteArray().length - init;
    }

    public T get(long position) {
        synchronized (tableLock) {
            if (readStream == null) {
                try {
                    FileInputStream readStreamFIS = new FileInputStream(getTableFile());
                    readChannel = readStreamFIS.getChannel();
                    readStream = new NanoDBDefaultInputStream(readStreamFIS, workspace);
                } catch (FileNotFoundException e) {
                    throw new NIOException(e);
                }
            }
            try {
                readChannel.position(position);
            } catch (IOException e) {
                throw new NIOException(e);
            }
            return serializer.read(readStream, rowType);
        }
    }

    public long add(T a) {
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
                    File p = tableFile.getParentFile();
                    if(p!=null){
                        p.mkdirs();
                    }
                    writeStream = new NanoDBDefaultOutputStream(new FileOutputStream(tableFile, true), workspace);
                } catch (FileNotFoundException e) {
                    throw new NIOException(e);
                }
            }
            if (writeHeader) {
                writeStream.writeUTF(NANODB_TABLE_0_8_1);
                writeStream.flush();
                len = writeStream.getPosition();
            }
            serializer.write(a, writeStream);
            updateIndices(a, len);
            return len;
        }
    }

    public void flush() {
        synchronized (tableLock) {
            if (writeStream != null) {
                writeStream.flush();
            }
            for (IndexInfo value : indexDefinitions.values()) {
                value.flushIfDirty();
            }
        }
    }

    public void close() {
        synchronized (tableLock) {
            flush();
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

    public NStream<T> stream() {
        return NStream.of(
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                        false));
    }

    public Iterable<T> items() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return NanoDBTableStoreFile.this.iterator();
            }
        };
    }

    public Iterator<T> iterator() {
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
                                        new FileInputStream(getTableFile()), workspace
                                );
                            } catch (IOException ex) {
                                throw new NIOException(ex);
                            }
                            header = is.readUTF();
                        }
                    }
                    if (is != null) {
                        try {
                            nextValue = serializer.read(is, rowType);
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
                            if (ex instanceof NIOException) {
                                if (ex.getCause() instanceof EOFException) {
                                    return false;
                                }
                                throw (NIOException) ex;
                            }
                            throw new NIOException(ex);
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

    public void updateIndices(T a, long objectId) {
        synchronized (tableLock) {
            for (IndexInfo value : indexDefinitions.values()) {
                NanoDBIndex fi = value.getData();
                fi.put(value.getDefinition().getIndexedValue(a), objectId);
                value.dirty = true;
            }
        }
    }

    private File getTableFile() {
        return new File(dir, tableName + ".table");
    }

    public NStream<T> findByIndex(String indexName, Object value) {
        return NStream.of(
                resolveIndexInfo(indexName).getData().get(value)
                .mapToObj(pos -> get(pos)));
    }

    public <T> NStream<T> findIndexValues(String indexName) {
        return NStream.of(
                resolveIndexInfo(indexName).getData().findAll()
        );
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

        public void flushIfDirty() {
            if (dirty) {
                flush();
                dirty = false;
            }
        }

        public void flush() {
            synchronized (tableLock) {
                data.flush();
            }
        }

        public NanoDBIndex getData() {
            synchronized (tableLock) {
                if (data != null) {
                    return data;
                }
                NanoDBIndex fi = new NanoDBDefaultIndex<T>(workspace,def.getIndexType(),db.getSerializers().findSerializer(def.getIndexType(), def.isNullable()),
                        new DBIndexValueStoreDefaultFactory(), new HashMap<>(), getIndexFile());
                fi.load();
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
