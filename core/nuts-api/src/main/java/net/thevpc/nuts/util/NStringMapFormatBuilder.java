package net.thevpc.nuts.util;

import java.util.function.Function;

public class NStringMapFormatBuilder {

    //"=", "&", true, "?"
    private String equalsChars = "=";
    private String separatorChars = "&";
    private String escapeChars = "?";
    private boolean sort = true;
    private Function<String, String> decoder;
    private Function<String, String> encoder;

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

    public Function<String, String> getDecoder() {
        return decoder;
    }

    public NStringMapFormatBuilder setDecoder(Function<String, String> decoder) {
        this.decoder = decoder;
        return this;
    }

    public Function<String, String> getEncoder() {
        return encoder;
    }

    public NStringMapFormatBuilder setEncoder(Function<String, String> encoder) {
        this.encoder = encoder;
        return this;
    }

    public NStringMapFormat build() {
        return NStringMapFormat.of(equalsChars, separatorChars, escapeChars, sort, decoder, encoder);
    }

    public NStringMapFormatBuilder copyFrom(NStringMapFormatBuilder other) {
        if(other!=null) {
            this.equalsChars = other.getEqualsChars();
            this.separatorChars = other.getSeparatorChars();
            this.escapeChars = other.getEscapeChars();
            this.sort = other.isSort();
            this.encoder = other.getEncoder();
            this.decoder = other.getDecoder();
        }
        return this;
    }

    public NStringMapFormatBuilder copyFrom(NStringMapFormat other) {
        if(other!=null) {
            this.equalsChars = other.getEqualsChars();
            this.separatorChars = other.getSeparatorChars();
            this.escapeChars = other.getEscapeChars();
            this.sort = other.isSort();
            this.encoder = other.getEncoder();
            this.decoder = other.getDecoder();
        }
        return this;
    }

}
