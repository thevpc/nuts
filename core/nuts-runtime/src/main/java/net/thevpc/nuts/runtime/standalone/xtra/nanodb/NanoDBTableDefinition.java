package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public class NanoDBTableDefinition<T> {
    private String name;
    private Class<T> type;
    private NanoDBSerializer<T> serializer;
    private NanoDBIndexDefinition<T>[] indices;

    public NanoDBTableDefinition(String name, Class<T> type, NanoDBSerializer<T> serializer, NanoDBIndexDefinition<T>... indices) {
        this.name = name;
        this.type = type;
        this.serializer = serializer;
        this.indices = indices;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public NanoDBSerializer<T> getSerializer() {
        return serializer;
    }

    public NanoDBIndexDefinition<T>[] getIndices() {
        return indices;
    }
}
