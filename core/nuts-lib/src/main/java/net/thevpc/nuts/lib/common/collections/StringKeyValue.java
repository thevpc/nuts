package net.thevpc.nuts.lib.common.collections;

public class StringKeyValue {

    private final String key;
    private final String value;

    public StringKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "StringKeyValue{" + "key=" + key + ", value=" + value + '}';
    }

}
