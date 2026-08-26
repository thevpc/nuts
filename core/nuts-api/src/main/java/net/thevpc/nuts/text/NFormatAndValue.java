package net.thevpc.nuts.text;

/**
 * NFormatAndValue class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NFormatAndValue<V,F extends NObjectWriter> {
    private V value;
    private F format;

    /**
     * N format and value.
     *
     * @param value value
     * @param format format
     * @return n format and value result
     */
    public NFormatAndValue(V value, F format) {
        this.value = value;
        this.format = format;
    }

    /**
     * Value.
     *
     * @return value result
     */
    public V value() {
        return value;
    }

    /**
     * Format.
     *
     * @return format result
     */
    public F format() {
        return format;
    }
}
