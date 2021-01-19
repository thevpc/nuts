package net.thevpc.nuts.runtime.bundles.nanodb;

public interface NanoDBIndexDefinition<T> {
    String getIndexName();

    boolean isNullable();

    Class getIndexType();

    Object getIndexedValue(T t);
}
