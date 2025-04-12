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
    private boolean doubleQuoteSupported;
    private boolean simpleQuoteSupported;
    private boolean acceptNullKeys;

    public NStringMapFormatBuilder() {
    }

    public static NStringMapFormatBuilder of() {
        return new NStringMapFormatBuilder();
    }

    public boolean isAcceptNullKeys() {
        return acceptNullKeys;
    }

    public NStringMapFormatBuilder setAcceptNullKeys(boolean acceptNullKeys) {
        this.acceptNullKeys = acceptNullKeys;
        return this;
    }

    public boolean isDoubleQuoteSupported() {
        return doubleQuoteSupported;
    }

    public NStringMapFormatBuilder setDoubleQuoteSupported(boolean doubleQuoteSupported) {
        this.doubleQuoteSupported = doubleQuoteSupported;
        return this;
    }

    public boolean isSimpleQuoteSupported() {
        return simpleQuoteSupported;
    }

    public NStringMapFormatBuilder setSimpleQuoteSupported(boolean simpleQuoteSupported) {
        this.simpleQuoteSupported = simpleQuoteSupported;
        return this;
    }

    public NStringMapFormatBuilder setQuoteSupported(boolean quoteSupported) {
        setDoubleQuoteSupported(quoteSupported);
        setSimpleQuoteSupported(quoteSupported);
        return this;
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
        return new NStringMapFormat(this);
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
