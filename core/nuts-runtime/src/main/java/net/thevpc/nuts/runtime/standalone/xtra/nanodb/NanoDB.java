package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import java.io.File;

public interface NanoDB extends AutoCloseable {
    public void flush() ;

    public void close() ;

    public NanoDBSerializers getSerializers();

    public NanoDBTableStore findTable(String name);

    public NanoDBTableStore getTable(String name);

    public <T> NanoDBTableDefinitionBuilderFromBean<T> tableBuilder(Class<T> type) ;

    public <T> NanoDBTableStore<T> createTable(NanoDBTableDefinition<T> def) ;

    public <T> NanoDBTableStore<T> getOrCreateTable(NanoDBTableDefinition<T> def);

    public <T> NanoDBTableStore<T> createTable(NanoDBTableDefinition<T> def, boolean getOrCreate) ;

    public boolean containsTable(String tableName);

    public <T> NanoDBIndex<T> createIndexFor(Class<T> type, NanoDBSerializer<T> ser, File file);
}
