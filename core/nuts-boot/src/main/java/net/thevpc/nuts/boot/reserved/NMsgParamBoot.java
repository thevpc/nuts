package net.thevpc.nuts.boot.reserved;

import java.util.function.Supplier;

public class NMsgParamBoot {
    private String name;
    private Supplier<?> value;

    public static NMsgParamBoot of(String name, Supplier<?> value) {
        return new NMsgParamBoot(name,value);
    }

    public NMsgParamBoot(String name, Supplier<?> value) {
        this.name = NAssertBoot.requireNonBlank(name, "name");
        this.value = NAssertBoot.requireNonNull(value, "value");
    }

    public String getName() {
        return name;
    }

    public Supplier<?> getValue() {
        return value;
    }
}
