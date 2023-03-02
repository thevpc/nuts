package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SqlIndexColumn {

    public String columnName;
    public String type;
    public boolean nonUnique;
    public short ordinalPosition;
    public Boolean asc;
    public long pages;
    public String filterCondition;

    @Override
    public String toString() {
        return String.valueOf(columnName);
    }

    public SqlIndexColumn sort() {
        return this;
    }
}
