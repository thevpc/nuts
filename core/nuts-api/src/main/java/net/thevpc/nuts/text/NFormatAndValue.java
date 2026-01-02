package net.thevpc.nuts.text;

public class NFormatAndValue<V,F extends NObjectWriter> {
    private V value;
    private F format;

    public NFormatAndValue(V value, F format) {
        this.value = value;
        this.format = format;
    }

    public V getValue() {
        return value;
    }

    public F getFormat() {
        return format;
    }
}
