package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

import java.util.function.Supplier;

public abstract class NutsReservedOptionalThrowable<T> extends NutsReservedOptionalImpl<T> implements Cloneable{
    private static boolean DEBUG;

    static {
        String property = System.getProperty("nuts.optional.debug");
        DEBUG = (property == null || property.trim().isEmpty() || Boolean.parseBoolean(property));
    }

    private Throwable rootStack = DEBUG ? new Throwable() : null;
    private Supplier<T> defaultValue;

    public NutsReservedOptionalThrowable(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T orDefault() {
        return defaultValue==null?null:defaultValue.get();
    }

    protected NutsMessage prepareMessage(NutsMessage m) {
        if (DEBUG) {
            return NutsMessage.ofCstyle("%s.\n    call stack:\n%s\n    root stack:\n%s", m,
                    NutsReservedLangUtils.stacktrace(new Throwable()),
                    NutsReservedLangUtils.stacktrace(rootStack)
            );
        }
        return m;
    }

    @Override
    public NutsOptional<T> withDefault(Supplier<T> value) {
        NutsReservedOptionalThrowable<T> c = (NutsReservedOptionalThrowable<T>) clone();
        c.defaultValue=value;
        return c;
    }
    @Override
    public NutsOptional<T> withDefault(T value) {
        NutsReservedOptionalThrowable<T> c = (NutsReservedOptionalThrowable<T>) clone();
        c.defaultValue=()->value;
        return c;
    }

    @Override
    public NutsOptional<T> withoutDefault() {
        NutsReservedOptionalThrowable<T> c = (NutsReservedOptionalThrowable<T>) clone();
        c.defaultValue=null;
        return c;
    }

    @Override
    protected NutsOptional<T> clone() {
        try {
            return (NutsOptional<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
