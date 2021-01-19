package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NanoDBTableFile<T> implements Iterable<T>,AutoCloseable{
    public static final String NANODB_TABLE_0_8_1 = "nanodb-table-0.8.1";
    private final Object tableLock=new Object();
    private NanoDBSerializer<T> serializer;
    private Map<String, IndexInfo> indexDefinitions = new HashMap<>();
    private File dir;
    private String tableName;
    private NanoDBOutputStream writeStream;
    private NanoDBInputStream readStream;
    private FileChannel readChannel;
    private NanoDB db;

    public NanoDBTableFile(File dir, String tableName
            , NanoDBSerializer<T> serializer
            , NanoDB db
            , NanoDBIndexDefinition<T>[] indexDefinitions
    ) {
        this.dir = dir;
        this.db = db;
        this.tableName = tableName;
        this.serializer = serializer;
        for (NanoDBIndexDefinition<T> indexDefinition : indexDefinitions) {
            this.indexDefinitions.put(indexDefinition.getIndexName(),new IndexInfo(indexDefinition));
        }
    }

    public T get(long position) {
        synchronized (tableLock) {
            if (readStream == null) {
                try {
                    FileInputStream readStreamFIS = new FileInputStream(getTableFile());
                    readChannel = readStreamFIS.getChannel();
                    readStream = new NanoDBDefaultInputStream(readStreamFIS);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
            }
            try {
                readChannel.position(position);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return serializer.read(readStream);
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
                    writeStream = new NanoDBDefaultOutputStream(new FileOutputStream(tableFile, true));
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
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

    public void flush(){
        synchronized (tableLock) {
            if (writeStream != null) {
                writeStream.flush();
            }
            for (IndexInfo value : indexDefinitions.values()) {
                value.flushIfDirty();
            }
        }
    }

    public void close(){
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

    public Stream<T> stream(){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                false);
    }

    public Iterator<T> iterator(){
        return new Iterator<T>() {
            T nextValue;
            private NanoDBInputStream is;
            private boolean closed;
            private String header;

            @Override
            public boolean hasNext() {
                if(closed){
                    return false;
                }
                synchronized (tableLock) {
                    if (is == null) {
                        if (getTableFile().exists()) {
                            try {
                                is = new NanoDBDefaultInputStream(
                                        new FileInputStream(getTableFile())
                                );
                            } catch (IOException ex) {
                                throw new UncheckedIOException(ex);
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
                            }finally {
                                is = null;
                                closed = true;
                            }
                            if(ex instanceof UncheckedIOException){
                                if(ex.getCause() instanceof EOFException){
                                    return false;
                                }
                            }
                            if(ex instanceof RuntimeException){
                                throw (RuntimeException) ex;
                            }
                            if(ex instanceof IOException){
                                throw new UncheckedIOException((IOException)ex);
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





    public Stream<T> findByIndex(String indexName, Object value) {
        return resolveIndexInfo(indexName).getData().get(value)
                .mapToObj(pos->get(pos));
    }

    public <T> Stream<T> findIndexValues(String indexName) {
        return resolveIndexInfo(indexName).getData().findAll();
    }

    public long getFileLength(){
        return getTableFile().length();
    }

    public static int getUTFLength(String s)  {
        ByteArrayOutputStream o=new ByteArrayOutputStream();
        int init=0;
        try {
            DataOutputStream r = new DataOutputStream(o);
            r.flush();
            init=o.toByteArray().length;
            r.writeUTF(s);
            r.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return o.toByteArray().length-init;
    }

    private IndexInfo resolveIndexInfo(String name) {
        IndexInfo y = indexDefinitions.get(name);
        if(y==null){
            throw new IllegalArgumentException("not found index: "+name);
        }
        return y;
    }

    private class IndexInfo{
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
            if(dirty){
                flush();
                dirty=false;
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
                NanoDBIndex fi = db.createIndexFor(db.getSerializers().findSerializer(def.getIndexType(), def.isNullable()),
                        getIndexFile()
                );
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
