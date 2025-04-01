package net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem;

import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBDefaultIndex;

import java.io.File;
import java.util.HashMap;

public class NanoDBInMemory extends AbstractNanoDB {
    public NanoDBInMemory() {
    }


    @Override
    protected <T> NanoDBTableStore<T> createNanoDBTableStore(Class<T> rowType, String tableName, NanoDBSerializer<T> serializer, NanoDBIndexDefinition<T>[] indexDefinitions) {
        return new NanoDBTableStoreMem<>(
                rowType,
                tableName,
                this,
                indexDefinitions
        );
    }
    
    public <T> NanoDBIndex<T> createIndexFor(Class<T> type, NanoDBSerializer<T> ser, File file) {
        return new NanoDBDefaultIndex<T>(type,ser, new DBIndexValueStoreDefaultFactory(), new HashMap<>(), file);
    }
}
