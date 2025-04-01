package net.thevpc.nuts.runtime.standalone.xtra.nanodb.file;

import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;

import java.io.File;
import java.util.HashMap;

public class NanoDBOnDisk extends AbstractNanoDB {
    private File dir;
    public NanoDBOnDisk(File dir) {
        this.dir = dir;
    }


    @Override
    protected <T> NanoDBTableStore<T> createNanoDBTableStore(Class<T> rowType, String tableName, NanoDBSerializer<T> serializer, NanoDBIndexDefinition<T>[] indexDefinitions) {
        return new NanoDBTableStoreFile<T>(
                rowType,
                dir, tableName, serializer,
                this,
                indexDefinitions
        );
    }

    public <T> NanoDBIndex<T> createIndexFor(Class<T> type, NanoDBSerializer<T> ser, File file) {
        return new NanoDBDefaultIndex<T>(type,ser, new DBIndexValueStoreDefaultFactory(), new HashMap<>(), file);
    }
}
