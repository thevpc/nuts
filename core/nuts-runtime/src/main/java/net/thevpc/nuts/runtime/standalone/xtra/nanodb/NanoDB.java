package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NanoDB implements AutoCloseable {
    private Map<String, NanoDBTableFile> tables = new HashMap<>();
    private File dir;
    private NanoDBSerializers serializers = new NanoDBSerializers();
    private NWorkspace workspace;
    public NanoDB(File dir) {
        this.dir = dir;
        this.workspace = NWorkspace.of().get();
        dir.mkdirs();
    }

    public void flush(NSession session) {
        for (NanoDBTableFile value : tables.values()) {
            value.flush();
        }
    }

    @Override
    public void close() {
        for (NanoDBTableFile value : tables.values()) {
            value.close();
        }
    }

    public NanoDBSerializers getSerializers() {
        return serializers;
    }

    public NanoDBTableFile findTable(String name) {
        return tables.get(name);
    }

    public NanoDBTableFile getTable(String name) {
        NanoDBTableFile tableFile = tables.get(name);
        if (tableFile == null) {
            throw new NIllegalArgumentException(NMsg.ofC("table does not exists: %s", name));
        }
        return tableFile;
    }

    public <T> NanoDBTableDefinitionBuilderFromBean<T> tableBuilder(Class<T> type) {
        return new NanoDBTableDefinitionBuilderFromBean<>(type, this, workspace);
    }

    public <T> NanoDBTableFile<T> createTable(NanoDBTableDefinition<T> def) {
        return createTable(def, false);
    }

    public <T> NanoDBTableFile<T> getOrCreateTable(NanoDBTableDefinition<T> def) {
        return createTable(def, true);
    }

    public <T> NanoDBTableFile<T> createTable(NanoDBTableDefinition<T> def, boolean getOrCreate) {
        if (def == null) {
            throw new IllegalArgumentException("null table definition");
        }
        String name = def.getName();
        if (getOrCreate) {
            NanoDBTableFile oldTable = tables.get(name);
            if (tables.containsKey(name)) {
                return oldTable;
            }
        }
        if (NBlankable.isBlank(name)) {
            throw new IllegalArgumentException("invalid table definition: null name");
        }
        NanoDBSerializer<T> serializer = def.getSerializer();
        Class serSupportedType = serializer != null ? serializer.getSupportedType() : null;
        Class defType = def.getType();
        if (defType == null) {
            defType = serSupportedType;
        }
        if (serSupportedType == null) {
            serSupportedType = defType;
        }
        if (defType == null || serSupportedType == null) {
            throw new IllegalArgumentException("invalid table definition: invalid type");
        }
        if (serializer == null) {
            serializer = getSerializers().of(defType, true);
        }
        if (!serSupportedType.isAssignableFrom(defType)) {
            throw new IllegalArgumentException("invalid table definition: invalid type: " + defType.getName() + "; unsupported by a serializer for " + serSupportedType.getName());
        }
        if (tables.containsKey(name)) {
            throw new IllegalArgumentException("table already defined: " + name);
        }
        NanoDBIndexDefinition<T>[] indices = def.getIndices();
        if (indices == null) {
            indices = new NanoDBIndexDefinition[0];
        }
        for (NanoDBIndexDefinition<T> index : indices) {
            if (index == null) {
                throw new IllegalArgumentException("invalid table definition: null index");
            }
        }
        NanoDBTableFile<T> f = new NanoDBTableFile<T>(
                def.getType(),
                dir, name, serializer,
                this,
                indices, workspace
        );
        tables.put(name, f);
        return f;
    }


    public boolean containsTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public <T> NanoDBIndex<T> createIndexFor(Class<T> type, NanoDBSerializer<T> ser, File file) {
        return new NanoDBDefaultIndex<T>(workspace,type,ser, new DBIndexValueStoreDefaultFactory(), new HashMap<>(), file);
    }
}
