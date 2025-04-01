package net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem;

import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;
import net.thevpc.nuts.util.NStream;

import java.nio.channels.FileChannel;
import java.util.*;

public class NanoDBTableStoreMem<T> implements NanoDBTableStore<T> {
    public static final String NANODB_TABLE_0_8_1 = "nanodb-table-0.8.1";
    private final Object tableLock = new Object();
    private final Map<String, IndexInfo> indexDefinitions = new HashMap<>();
    private final String tableName;
    private final NanoDB db;
    private FileChannel readChannel;
    private Class<T> rowType;
    private List<T> rows=new ArrayList<>();

    public NanoDBTableStoreMem(Class<T> rowType, String tableName
            , NanoDB db
            , NanoDBIndexDefinition<T>[] indexDefinitions
    ) {
        this.rowType = rowType;
        this.db = db;
        this.tableName = tableName;
        for (NanoDBIndexDefinition<T> indexDefinition : indexDefinitions) {
            this.indexDefinitions.put(indexDefinition.getIndexName(), new IndexInfo(indexDefinition));
        }
    }



    public T get(long position) {
        synchronized (tableLock) {
            return rows.get((int) position);
        }
    }

    public long add(T a) {
        synchronized (tableLock) {
            int len = rows.size();
            rows.add(a);
            updateIndices(a, len);
            return len;
        }
    }

    public void flush() {
    }

    public void close() {
    }

    public NStream<T> stream() {
        return NStream.of(new ArrayList<>(rows).stream());
    }

    public Iterable<T> items() {
        return new ArrayList<>(rows);
    }

    public Iterator<T> iterator() {
        return new ArrayList<>(rows).iterator();
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
        return rows.size();
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
                NanoDBIndex fi=new NanoDBDefaultIndexInMem(def.getIndexType(),
                        db.getSerializers().findSerializer(def.getIndexType(), def.isNullable())
                        , new DBIndexValueStoreDefaultFactory(), new HashMap<>());

                data = fi;
                return fi;
            }
        }


        private String getIndexName() {
            return def.getIndexName();
        }
    }


}
