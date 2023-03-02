package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqlIndex {

    public String indexName;
    public String indexQualifier;
    public List<SqlIndexColumn> columns = new ArrayList<>();

    @Override
    public String toString() {
        return String.valueOf(indexName);
    }

    public SqlIndex sort() {
        columns.sort(Comparator.comparing(a -> NStringUtils.trim(a.columnName)));
        return this;
    }
}
