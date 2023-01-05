package net.thevpc.nuts.toolbox.ndb;

import java.util.ArrayList;
import java.util.List;

public class ExtendedQuery {
    private String command;
    private String table;
    private long skip;
    private long limit;
    private List<String> where = new ArrayList<>();
    private List<String> set = new ArrayList<>();
    private List<String> sort = new ArrayList<>();
    private Boolean one = false;
    private String newName = null;
    private String rawQuery = null;
    private boolean longMode = false;

    public ExtendedQuery(String command) {
        this.command = command;
    }

    public String getNewName() {
        return newName;
    }

    public ExtendedQuery setNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public ExtendedQuery setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getTable() {
        return table;
    }

    public ExtendedQuery setTable(String table) {
        this.table = table;
        return this;
    }

    public List<String> getWhere() {
        return where;
    }

    public ExtendedQuery setWhere(List<String> where) {
        this.where = where;
        return this;
    }

    public List<String> getSet() {
        return set;
    }

    public ExtendedQuery setSet(List<String> set) {
        this.set = set;
        return this;
    }

    public List<String> getSort() {
        return sort;
    }

    public ExtendedQuery setSort(List<String> sort) {
        this.sort = sort;
        return this;
    }

    public Boolean getOne() {
        return one;
    }

    public ExtendedQuery setOne(Boolean one) {
        this.one = one;
        return this;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public ExtendedQuery setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
        return this;
    }

    public boolean isLongMode() {
        return longMode;
    }

    public ExtendedQuery setLongMode(boolean longMode) {
        this.longMode = longMode;
        return this;
    }

    public long getSkip() {
        return skip;
    }

    public ExtendedQuery setSkip(long skip) {
        this.skip = skip;
        return this;
    }

    public long getLimit() {
        return limit;
    }

    public ExtendedQuery setLimit(long limit) {
        this.limit = limit;
        return this;
    }
}
