package net.thevpc.nuts.runtime.bundles.nanodb;

import java.util.function.Function;

public class NanoDBDefaultIndexDefinition<T> implements NanoDBIndexDefinition<T> {
    private String name;
    private Class indexType;
    private boolean nullable;
    private Function<T,Object> mapper;

    public <K> NanoDBDefaultIndexDefinition(String name, Class<K> indexType, boolean nullable, Function<T,K> mapper) {
        this.name = name;
        this.indexType = indexType;
        this.nullable = nullable;
        this.mapper = (Function) mapper;
    }

    @Override
    public Object getIndexedValue(T t) {
        return mapper.apply(t);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public Class getIndexType() {
        return indexType;
    }

}
