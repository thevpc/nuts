package net.thevpc.nuts.text;

public class NFormatAndValue<V,F extends NObjectWriter> {
    private V value;
    private F format;

    public NFormatAndValue(V value, F format) {
        this.value = value;
        this.format = format;
    }

    public V value() {
        return value;
    }

    public F format() {
        return format;
    }
}
