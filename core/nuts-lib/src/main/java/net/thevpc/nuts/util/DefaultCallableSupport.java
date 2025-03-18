/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NExceptionHandler;

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
    private final Supplier<NMsg> emptyMessage;

    public DefaultCallableSupport(Supplier<T> value, int supportLevel, Supplier<NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException("null callable requires invalid support");
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException("non null callable requires valid support");
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? () -> NMsg.ofMissingValue() : emptyMessage;
    }

    public T call() {
        if (isValid()) {
            return value.get();
        } else {
            NMsg m = emptyMessage.get();
            if (m == null) {
                m = NMsg.ofMissingValue();
            }
            throw NExceptionHandler.ofSafeNoSuchElementException(m);
        }
    }

    public int getSupportLevel() {
        return supportLevel;
    }

    @Override
    public NOptional<T> toOptional() {
        if (isValid()) {
            return NOptional.ofCallable(() -> value.get());
        }
        NMsg m = emptyMessage.get();
        if (m == null) {
            m = NMsg.ofMissingValue();
        }
        return NOptional.ofEmpty(m);
    }
}
