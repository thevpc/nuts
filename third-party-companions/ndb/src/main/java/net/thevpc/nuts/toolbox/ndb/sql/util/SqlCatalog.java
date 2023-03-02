package net.thevpc.nuts.toolbox.ndb.sql.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlCatalog {
    public String catalogName;
    public Map<String, SqlSchema> schemas = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.valueOf(catalogName);
    }

    public SqlCatalog sort() {
        Map<String, SqlSchema> newMap = new LinkedHashMap<>(schemas);
        schemas.clear();
        String[] names = newMap.keySet().toArray(new String[0]);
        Arrays.sort(names);
        for (String name : names) {
            SqlSchema value = newMap.get(name);
            value.sort();
            schemas.put(name, value);
        }
        return this;
    }
}
