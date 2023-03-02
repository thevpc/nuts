package net.thevpc.nuts.toolbox.ndb.sql.util;

public class SqlPrimaryKey {

    public String columnName;
    public short keySeq;
    public String pkName;

    @Override
    public String toString() {
        return String.valueOf(columnName);
    }
}
