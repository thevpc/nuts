/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NCallableSupport;
import net.thevpc.nuts.NNoSuchElementException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.reserved.NApiUtilsRPI;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @param <T> value type
 * @author thevpc
 */
public class DefaultCallableSupport<T> implements CallableSupport<T> {
    private final Supplier<T> value;
    private final int supportLevel;
    private final Supplier<String> emptyMessage;

    public DefaultCallableSupport(Supplier<T> value, int supportLevel, Supplier<String> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException("null callable requires invalid support");
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException("non null callable requires valid support");
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? () -> "not found" : emptyMessage;
    }

    public T call() {
        if (isValid()) {
            return value.get();
        } else {
            String m = emptyMessage.get();
            if(m==null){
                m="not found";
            }
            throw new NoSuchElementException(m);
        }
    }

    public int getSupportLevel() {
        return supportLevel;
    }

    @Override
    public NOptional<T> toOptional() {
        if (isValid()) {
            return NOptional.ofCallable(s -> value.get());
        }
        return NOptional.ofEmpty(s->NMsg.ofPlain(emptyMessage.get()));
    }
}
