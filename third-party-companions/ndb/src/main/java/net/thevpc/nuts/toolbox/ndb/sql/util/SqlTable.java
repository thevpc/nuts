package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqlTable {

    public String tableName;
    public String tableType;
    public String remarks;
    public String typesCatalog;
    public String typesSchema;
    public String typeName;
    public String selfReferencingColName;
    public String refGeneration;
    public List<SqlColumn> columns = new ArrayList<>();
    public List<SqlPrimaryKey> primaryKeys = new ArrayList<>();
    public List<SqlIndex> indexes = new ArrayList<>();

    @Override
    public String toString() {
        return String.valueOf(tableName);
    }

    public SqlTable sort() {
        columns.sort(Comparator.comparing(a -> NStringUtils.trim(a.columnName)));
        primaryKeys.sort(Comparator.comparing(a -> NStringUtils.trim(a.columnName)));
        indexes.sort(Comparator.comparing(a -> NStringUtils.trim(a.indexName)));
        return this;
    }
}
