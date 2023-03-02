package net.thevpc.nuts.toolbox.ndb.sql.util;

import java.util.*;

public class SqlSchema {
    public String schemaName;
    public Map<String, SqlTable> tables = new LinkedHashMap<>();
    public Map<String, SqlTable> systemTables = new LinkedHashMap<>();
    public Map<String, SqlTable> globalTemporary = new LinkedHashMap<>();
    public Map<String, SqlTable> localTemporary = new LinkedHashMap<>();
    public Map<String, SqlTable> aliases = new LinkedHashMap<>();
    public Map<String, SqlTable> synonyms = new LinkedHashMap<>();
    public Map<String, SqlTable> indexes = new LinkedHashMap<>();
    public Map<String, SqlTable> views = new LinkedHashMap<>();
    public Map<String, SqlTable> sequences = new LinkedHashMap<>();
    public Map<String, SqlTable> others = new LinkedHashMap<>();

    @Override
    public String toString() {
        return String.valueOf(schemaName);
    }

    public Map<String, Map<String, SqlTable>> tableMaps() {
        Map<String, Map<String, SqlTable>> r = new HashMap<>();
        r.put("TABLE", tables);
        r.put("INDEX", indexes);
        r.put("VIEW", views);
        r.put("ALIAS", aliases);
        r.put("SYSTEM TABLE", systemTables);
        r.put("GLOBAL TEMPORARY", globalTemporary);
        r.put("LOCAL TEMPORARY", localTemporary);
        r.put("SYNONYM", synonyms);
        r.put("SEQUENCE", sequences);
        r.put("OTHER", others);
        return r;
    }

    public SqlSchema sort() {
        sortPartial(tables);
        sortPartial(indexes);
        sortPartial(views);
        sortPartial(systemTables);
        sortPartial(globalTemporary);
        sortPartial(localTemporary);
        sortPartial(aliases);
        sortPartial(synonyms);
        sortPartial(others);
        return this;
    }

    private void sortPartial(Map<String, SqlTable> others) {
        Map<String, SqlTable> v = others;
        Map<String, SqlTable> newMap = new LinkedHashMap<>(v);
        v.clear();
        String[] names = newMap.keySet().toArray(new String[0]);
        Arrays.sort(names);
        for (String name : names) {
            SqlTable value = newMap.get(name);
            value.sort();
            v.put(name, value);
        }
    }
}
