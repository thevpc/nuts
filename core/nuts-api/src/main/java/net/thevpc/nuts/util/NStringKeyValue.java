package net.thevpc.nuts.util;

public class NStringKeyValue {

    private final String key;
    private final String value;

    public NStringKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "StringKeyValue{" + "key=" + key + ", value=" + value + '}';
    }

}
