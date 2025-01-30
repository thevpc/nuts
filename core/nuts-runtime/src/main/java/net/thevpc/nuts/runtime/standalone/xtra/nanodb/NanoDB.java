package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBDefaultIndex;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBTableStoreFile;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
