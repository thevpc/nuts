package net.vpc.app.nuts.util;

public class SerializeOptions {

    boolean pretty;
    boolean ignoreNulls;
    boolean ignoreEmptyStrings;
    boolean ignoreEmptyMaps;
    boolean ignoreEmptyCollections;
    boolean ignoreEmptyArrays;

    public boolean isPretty() {
        return pretty;
    }

    public SerializeOptions setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public SerializeOptions setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public boolean isIgnoreEmptyStrings() {
        return ignoreEmptyStrings;
    }

    public SerializeOptions setIgnoreEmptyStrings(boolean ignoreEmptyStrings) {
        this.ignoreEmptyStrings = ignoreEmptyStrings;
        return this;
    }

    public boolean isIgnoreEmptyMaps() {
        return ignoreEmptyMaps;
    }

    public SerializeOptions setIgnoreEmptyMaps(boolean ignoreEmptyMaps) {
        this.ignoreEmptyMaps = ignoreEmptyMaps;
        return this;
    }

    public boolean isIgnoreEmptyCollections() {
        return ignoreEmptyCollections;
    }

    public SerializeOptions setIgnoreEmptyCollections(boolean ignoreEmptyCollections) {
        this.ignoreEmptyCollections = ignoreEmptyCollections;
        return this;
    }

    public boolean isIgnoreEmptyArrays() {
        return ignoreEmptyArrays;
    }

    public SerializeOptions setIgnoreEmptyArrays(boolean ignoreEmptyArrays) {
        this.ignoreEmptyArrays = ignoreEmptyArrays;
        return this;
    }
}
