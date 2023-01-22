package net.thevpc.nuts;

import net.thevpc.nuts.util.NAssert;

import java.util.function.Supplier;

public class NMsgParam {
    private String name;
    private Supplier<?> value;

    public static NMsgParam of(String name, Supplier<?> value) {
        return new NMsgParam(name,value);
    }

    public NMsgParam(String name, Supplier<?> value) {
        this.name = NAssert.requireNonBlank(name, "name");
        this.value = NAssert.requireNonNull(value, "value");
    }

    public String getName() {
        return name;
    }

    public Supplier<?> getValue() {
        return value;
    }
}
