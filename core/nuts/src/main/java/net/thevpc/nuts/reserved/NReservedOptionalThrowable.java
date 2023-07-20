package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

import java.util.function.Supplier;

public abstract class NReservedOptionalThrowable<T> extends NReservedOptionalImpl<T> implements Cloneable{
    private static boolean DEBUG;

    static {
        String property = System.getProperty("nuts.optional.debug");
        DEBUG = Boolean.parseBoolean(property);
    }

    private Throwable rootStack = DEBUG ? new Throwable() : null;
    private Supplier<T> defaultValue;

    public NReservedOptionalThrowable(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T orDefault() {
        return defaultValue==null?null:defaultValue.get();
    }

    protected NMsg prepareMessage(NMsg m) {
        if (DEBUG) {
            return NMsg.ofC("%s.\n    call stack:\n%s\n    root stack:\n%s", m,
                    NReservedLangUtils.stacktrace(new Throwable()),
                    NReservedLangUtils.stacktrace(rootStack)
            );
        }
        if(m==null){
            m=NMsg.ofMissingValue();
        }
        return m;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public NOptional<T> withDefault(Supplier<T> value) {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue=value;
        return c;
    }
    @Override
    public NOptional<T> withDefault(T value) {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue=()->value;
        return c;
    }

    @Override
    public NOptional<T> withoutDefault() {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue=null;
        return c;
    }

    @Override
    protected NOptional<T> clone() {
        try {
            return (NOptional<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
