package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NOptionalErrorException;

import java.util.function.Supplier;

public abstract class NReservedOptionalThrowable<T> extends NReservedOptionalImpl<T> implements Cloneable {
    private static boolean DEBUG;

    static {
        String property = System.getProperty("nuts.optional.debug");
        DEBUG = Boolean.parseBoolean(property);
    }

    private Throwable rootStack = DEBUG ? new Throwable() : null;
    private Supplier<NOptional<T>> defaultValue;

    public NReservedOptionalThrowable() {
    }

    public T orDefault() {
        if(defaultValue == null){
            return null;
        }
        NOptional<T> o = defaultValue.get();
        if(o==null){
            return null;
        }
        return o.orDefault();
    }

    @Override
    public NOptional<T> orDefaultOptional() {
        if(defaultValue == null){
            return null;
        }
        NOptional<T> o = defaultValue.get();
        if(o==null){
            return NOptional.ofEmpty(getMessage());
        }
        return o.orDefaultOptional();
    }

    protected NMsg prepareMessage(NMsg m) {
        if (DEBUG) {
            return NMsg.ofC("%s.\n    call stack:\n%s\n    root stack:\n%s", m,
                    NReservedLangUtils.stacktrace(new Throwable()),
                    NReservedLangUtils.stacktrace(rootStack)
            );
        }
        if (m == null) {
            m = NMsg.ofMissingValue();
        }
        return m;
    }
    protected void throwError(Supplier<NMsg> message, Supplier<NMsg> message0) {
        if (message == null) {
            message = message0;
        }
        if (message == null) {
            message = () -> NMsg.ofMissingValue();
        }
        Supplier<NMsg> finalMessage = message;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.get());
        NMsg m = prepareMessage(eMsg);
        throw new NOptionalErrorException(m);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public NOptional<T> withDefault(Supplier<T> value) {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue = value==null?null:()->NOptional.of(value.get());
        return c;
    }
    @Override
    public NOptional<T> withDefaultOptional(Supplier<NOptional<T>> value) {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue = value==null?null:()->{
            NOptional<T> i = value.get();
            if(i==null){
                return NOptional.ofEmpty(getMessage());
            }
            return this;
        };
        return c;
    }

    @Override
    public NOptional<T> withDefault(T value) {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue = value==null?null:() -> NOptional.of(value);
        return c;
    }

    @Override
    public NOptional<T> withoutDefault() {
        NReservedOptionalThrowable<T> c = (NReservedOptionalThrowable<T>) clone();
        c.defaultValue = null;
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