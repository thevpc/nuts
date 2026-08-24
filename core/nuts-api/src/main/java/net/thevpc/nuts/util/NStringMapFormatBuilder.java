package net.thevpc.nuts.util;

import java.util.function.Function;

/**
 * NStringMapFormatBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * N string map format builder.
     *
     * @return n string map format builder result
     */
    public NStringMapFormatBuilder() {
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NStringMapFormatBuilder of() {
        return new NStringMapFormatBuilder();
    }

    /**
     * Checks if is accept null keys.
     *
     * @return is accept null keys result
     */
    public boolean isAcceptNullKeys() {
        return acceptNullKeys;
    }

    /**
     * Accept null keys.
     *
     * @param acceptNullKeys accept null keys
     * @return accept null keys result
     */
    public NStringMapFormatBuilder acceptNullKeys(boolean acceptNullKeys) {
        this.acceptNullKeys = acceptNullKeys;
        return this;
    }

    /**
     * Checks if is double quote supported.
     *
     * @return is double quote supported result
     */
    public boolean isDoubleQuoteSupported() {
        return doubleQuoteSupported;
    }

    /**
     * Double quote supported.
     *
     * @param doubleQuoteSupported double quote supported
     * @return double quote supported result
     */
    public NStringMapFormatBuilder doubleQuoteSupported(boolean doubleQuoteSupported) {
        this.doubleQuoteSupported = doubleQuoteSupported;
        return this;
    }

    /**
     * Checks if is simple quote supported.
     *
     * @return is simple quote supported result
     */
    public boolean isSimpleQuoteSupported() {
        return simpleQuoteSupported;
    }

    /**
     * Simple quote supported.
     *
     * @param simpleQuoteSupported simple quote supported
     * @return simple quote supported result
     */
    public NStringMapFormatBuilder simpleQuoteSupported(boolean simpleQuoteSupported) {
        this.simpleQuoteSupported = simpleQuoteSupported;
        return this;
    }

    /**
     * Sets the quote supported.
     *
     * @param quoteSupported quote supported
     * @return set quote supported result
     */
    public NStringMapFormatBuilder setQuoteSupported(boolean quoteSupported) {
      /**
       * Double quote supported.
       *
       * @param quoteSupported quote supported
       */
        doubleQuoteSupported(quoteSupported);
      /**
       * Simple quote supported.
       *
       * @param quoteSupported quote supported
       */
        simpleQuoteSupported(quoteSupported);
        return this;
    }
    /**
     * Equals chars.
     *
     * @return equals chars result
     */
    public String equalsChars() {
        return equalsChars;
    }

    /**
     * Equals chars.
     *
     * @param equalsChars equals chars
     * @return equals chars result
     */
    public NStringMapFormatBuilder equalsChars(String equalsChars) {
        this.equalsChars = equalsChars;
        return this;
    }

    /**
     * Separator chars.
     *
     * @return separator chars result
     */
    public String separatorChars() {
        return separatorChars;
    }

    /**
     * Separator chars.
     *
     * @param separatorChars separator chars
     * @return separator chars result
     */
    public NStringMapFormatBuilder separatorChars(String separatorChars) {
        this.separatorChars = separatorChars;
        return this;
    }

    /**
     * Escape chars.
     *
     * @return escape chars result
     */
    public String escapeChars() {
        return escapeChars;
    }

    /**
     * Escape chars.
     *
     * @param escapeChars escape chars
     * @return escape chars result
     */
    public NStringMapFormatBuilder escapeChars(String escapeChars) {
        this.escapeChars = escapeChars;
        return this;
    }

    /**
     * Checks if is sort.
     *
     * @return is sort result
     */
    public boolean isSort() {
        return sort;
    }

    /**
     * Sort.
     *
     * @param sort sort
     * @return sort result
     */
    public NStringMapFormatBuilder sort(boolean sort) {
        this.sort = sort;
        return this;
    }

    /**
     * Decoder.
     *
     * @return decoder result
     */
    public Function<String, String> decoder() {
        return decoder;
    }

    /**
     * Decoder.
     *
     * @param decoder decoder
     * @return decoder result
     */
    public NStringMapFormatBuilder decoder(Function<String, String> decoder) {
        this.decoder = decoder;
        return this;
    }

    /**
     * Encoder.
     *
     * @return encoder result
     */
    public Function<String, String> encoder() {
        return encoder;
    }

    /**
     * Encoder.
     *
     * @param encoder encoder
     * @return encoder result
     */
    public NStringMapFormatBuilder encoder(Function<String, String> encoder) {
        this.encoder = encoder;
        return this;
    }

    /**
     * Build.
     *
     * @return build result
     */
    public NStringMapFormat build() {
        return new NStringMapFormat(this);
    }

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
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

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
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
