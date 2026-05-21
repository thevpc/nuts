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

    public NStringMapFormatBuilder acceptNullKeys(boolean acceptNullKeys) {
        this.acceptNullKeys = acceptNullKeys;
        return this;
    }

    public boolean isDoubleQuoteSupported() {
        return doubleQuoteSupported;
    }

    public NStringMapFormatBuilder doubleQuoteSupported(boolean doubleQuoteSupported) {
        this.doubleQuoteSupported = doubleQuoteSupported;
        return this;
    }

    public boolean isSimpleQuoteSupported() {
        return simpleQuoteSupported;
    }

    public NStringMapFormatBuilder simpleQuoteSupported(boolean simpleQuoteSupported) {
        this.simpleQuoteSupported = simpleQuoteSupported;
        return this;
    }

    public NStringMapFormatBuilder setQuoteSupported(boolean quoteSupported) {
        doubleQuoteSupported(quoteSupported);
        simpleQuoteSupported(quoteSupported);
        return this;
    }
    public String equalsChars() {
        return equalsChars;
    }

    public NStringMapFormatBuilder equalsChars(String equalsChars) {
        this.equalsChars = equalsChars;
        return this;
    }

    public String separatorChars() {
        return separatorChars;
    }

    public NStringMapFormatBuilder separatorChars(String separatorChars) {
        this.separatorChars = separatorChars;
        return this;
    }

    public String escapeChars() {
        return escapeChars;
    }

    public NStringMapFormatBuilder escapeChars(String escapeChars) {
        this.escapeChars = escapeChars;
        return this;
    }

    public boolean isSort() {
        return sort;
    }

    public NStringMapFormatBuilder sort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public Function<String, String> decoder() {
        return decoder;
    }

    public NStringMapFormatBuilder decoder(Function<String, String> decoder) {
        this.decoder = decoder;
        return this;
    }

    public Function<String, String> encoder() {
        return encoder;
    }

    public NStringMapFormatBuilder encoder(Function<String, String> encoder) {
        this.encoder = encoder;
        return this;
    }

    public NStringMapFormat build() {
        return new NStringMapFormat(this);
    }

    public NStringMapFormatBuilder copyFrom(NStringMapFormatBuilder other) {
        if(other!=null) {
            this.equalsChars = other.equalsChars();
            this.separatorChars = other.separatorChars();
            this.escapeChars = other.escapeChars();
            this.sort = other.isSort();
            this.encoder = other.encoder();
            this.decoder = other.decoder();
        }
        return this;
    }

    public NStringMapFormatBuilder copyFrom(NStringMapFormat other) {
        if(other!=null) {
            this.equalsChars = other.equalsChars();
            this.separatorChars = other.separatorChars();
            this.escapeChars = other.escapeChars();
            this.sort = other.isSort();
            this.encoder = other.encoder();
            this.decoder = other.decoder();
        }
        return this;
    }

}
