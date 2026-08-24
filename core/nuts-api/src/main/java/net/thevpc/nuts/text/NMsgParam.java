package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NAssert;

import java.util.function.Supplier;

/**
 * NMsgParam class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMsgParam {
    private String name;
    private Supplier<?> value;

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param value value
     * @return of result
     */
    public static NMsgParam of(String name, Supplier<?> value) {
        return new NMsgParam(name,value);
    }

    /**
     * N msg param.
     *
     * @param name name
     * @param value value
     * @return n msg param result
     */
    public NMsgParam(String name, Supplier<?> value) {
        this.name = NAssert.requireNamedNonBlank(name, "name");
        this.value = NAssert.requireNamedNonNull(value, "value");
    }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }

    /**
     * Value.
     *
     * @return value result
     */
    public Supplier<?> value() {
        return value;
    }
}
