package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public interface NanoDBIndexDefinition<T> {
    String getIndexName();

    boolean isNullable();

    Class getIndexType();

    Object getIndexedValue(T t);
}
