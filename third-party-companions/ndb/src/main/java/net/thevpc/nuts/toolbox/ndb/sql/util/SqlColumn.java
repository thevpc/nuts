package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqlColumn {
    public String columnName;
    public String dataType;
    public String typeName;
    public int columnSize;
    public int numPrecRadix;
    //public int bufferLength;
    public int decimalDigits;
    public String nullable;
    public String nullable2;
    public String remarks;
    public String columnDef;
    //public int sqlDataType;
    //public int sqlDateTimeSub;
    public int charOctetLength;
    public int ordinalPosition;
    public String scopeCatalog;
    public String scopeSchema;
    public String scopeTable;
    public short sourceDataType;
    public String autoIncrement;
    public String generatedColumn;
    public boolean primaryKey;
    public short primaryKeySeq;

    public List<SqlImportedColumn> foreignKeys = new ArrayList<>();
    @Override
    public String toString() {
        return String.valueOf(columnName);
    }

    public SqlColumn sort() {
        foreignKeys.sort(Comparator.comparing(a -> NStringUtils.trim(a.fkName)));
//            Map<String, SqlColumn> newMap = new LinkedHashMap<>(columns);
//            columns.clear();
//            String[] names = newMap.keySet().toArray(new String[0]);
//            Arrays.sort(names);
//            for (String name : names) {
//                SqlColumn value = newMap.get(name);
//                value.sort();
//                columns.put(name, value);
//            }
        return this;
    }
}
