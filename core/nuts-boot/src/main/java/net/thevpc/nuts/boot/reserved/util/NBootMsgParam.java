package net.thevpc.nuts.boot.reserved.util;

import java.util.function.Supplier;

public class NBootMsgParam {
    private String name;
    private Supplier<?> value;

    public static NBootMsgParam of(String name, Supplier<?> value) {
        return new NBootMsgParam(name,value);
    }

    public NBootMsgParam(String name, Supplier<?> value) {
        this.name = NBootUtils.requireNonBlank(name, "name");
        this.value = NBootUtils.requireNonNull(value, "value");
    }

    public String getName() {
        return name;
    }

    public Supplier<?> getValue() {
        return value;
    }
}
