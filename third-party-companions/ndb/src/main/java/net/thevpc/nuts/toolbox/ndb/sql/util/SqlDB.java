package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.NOptional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlDB {
    public Map<String, SqlCatalog> catalogs = new LinkedHashMap<>();

    public NOptional<SqlSchema> getSchema(String cat, String schema) {
        return getCatalog(cat).flatMap(x -> NOptional.ofNamed(x.schemas.get(schema), "schema " + schema));
    }

    public SqlSchema getOrCreateSchema(String cat, String schema) {
        return getOrCreateCatalog(cat).schemas.computeIfAbsent(schema, s -> {
            SqlSchema v = new SqlSchema();
            v.schemaName = s;
            return v;
        });
    }

    public NOptional<SqlCatalog> getCatalog(String cat) {
        return NOptional.ofNamed(catalogs.get(cat), "catalog " + cat);
    }

    public SqlCatalog getOrCreateCatalog(String cat) {
        return catalogs.computeIfAbsent(cat, s -> {
            SqlCatalog v = new SqlCatalog();
            v.catalogName = cat;
            return v;
        });
    }

    public NOptional<SqlTable> getTable(String cat, String schem, String tab) {
        NOptional<SqlSchema> schema = getSchema(cat, schem);
        if (schema.isPresent()) {
            SqlSchema s = schema.get();
            for (Map<String, SqlTable> m : s.tableMaps().values()) {
                SqlTable t = m.get(tab);
                if (t != null) {
                    return NOptional.of(t);
                }
            }
        }
        return NOptional.ofNamedEmpty(tab);
    }

    public SqlTable getOrCreateTable(String cat, String schem, String tab, String tableType) {
        if ("TABLE".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).tables.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("VIEW".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).views.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("INDEX".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).indexes.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("GLOBAL TEMPORARY".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).globalTemporary.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("LOCAL TEMPORARY".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).localTemporary.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("ALIAS".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).aliases.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("SYSTEM TABLE".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).systemTables.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("SYNONYM".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).synonyms.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else if ("SEQUENCE".equalsIgnoreCase(tableType)) {
            return getOrCreateSchema(cat, schem).sequences.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        } else {
            return getOrCreateSchema(cat, schem).others.computeIfAbsent(tab, s -> {
                SqlTable v = new SqlTable();
                v.tableName = tab;
                v.tableType = tableType.toLowerCase();
                return v;
            });
        }
    }

    public SqlDB sort() {
        Map<String, SqlCatalog> newMap = new LinkedHashMap<>(catalogs);
        catalogs.clear();
        String[] names = newMap.keySet().toArray(new String[0]);
        Arrays.sort(names);
        for (String name : names) {
            SqlCatalog value = newMap.get(name);
            value.sort();
            catalogs.put(name, value);
        }
        return this;
    }
}
