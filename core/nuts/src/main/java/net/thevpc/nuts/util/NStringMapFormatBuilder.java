package net.thevpc.nuts.util;

public class NStringMapFormatBuilder {

    //"=", "&", true, "?"
    private String equalsChars = "=";
    private String separatorChars = "&";
    private String escapeChars = "?";
    private boolean sort = true;

    public NStringMapFormatBuilder() {
    }

    private NStringMapFormatBuilder(String equalsChars, String separatorChars, String escapeChars, boolean sort) {
        this.equalsChars = equalsChars;
        this.separatorChars = separatorChars;
        this.escapeChars = escapeChars;
        this.sort = sort;
    }

    public static NStringMapFormatBuilder of() {
        return new NStringMapFormatBuilder();
    }

    public String getEqualsChars() {
        return equalsChars;
    }

    public NStringMapFormatBuilder setEqualsChars(String equalsChars) {
        this.equalsChars = equalsChars;
        return this;
    }

    public String getSeparatorChars() {
        return separatorChars;
    }

    public NStringMapFormatBuilder setSeparatorChars(String separatorChars) {
        this.separatorChars = separatorChars;
        return this;
    }

    public String getEscapeChars() {
        return escapeChars;
    }

    public NStringMapFormatBuilder setEscapeChars(String escapeChars) {
        this.escapeChars = escapeChars;
        return this;
    }

    public boolean isSort() {
        return sort;
    }

    public NStringMapFormatBuilder setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public NStringMapFormat build() {
        return new NStringMapFormat(equalsChars, separatorChars, escapeChars, sort);
    }
}
