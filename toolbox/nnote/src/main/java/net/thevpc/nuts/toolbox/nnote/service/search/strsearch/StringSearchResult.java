/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search.strsearch;

/**
 *
 * @author vpc
 */
public class StringSearchResult<T> {

    private T object;
    private String key;
    private Object part;
    private String description;
    private String value;
    private int index;
    private int row;
    private int column;
    private String lineString;

    public StringSearchResult(T object, String key, Object part, String description, int index, int row, int column, String value, String lineString) {
        this.object = object;
        this.key = key;
        this.part = part;
        this.description = description;
        this.index = index;
        this.row = row;
        this.column = column;
        this.value = value;
        this.lineString = lineString;
    }

    public String getValue() {
        return value;
    }

    public T getObject() {
        return object;
    }

    public String getKey() {
        return key;
    }

    public Object getPart() {
        return part;
    }

    public String getDescription() {
        return description;
    }

    public int getRow() {
        return row;
    }

    public int getIndex() {
        return index;
    }

    public int getLine() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.valueOf(lineString);
    }

}
