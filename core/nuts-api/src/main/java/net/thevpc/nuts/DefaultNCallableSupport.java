/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @param <T> value type
 * @author thevpc
 */
public class DefaultNCallableSupport<T> implements NCallableSupport<T> {
    private final Supplier<T> value;
    private final int supportLevel;
    private final Supplier<NMsg> emptyMessage;

    public DefaultNCallableSupport(Supplier<T> value, int supportLevel, Supplier<NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException(NI18n.of("null callable requires invalid support"));
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException(NI18n.of("non null callable requires valid support"));
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? () -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public T call() {
        if (isValid()) {
            return value.get();
        } else {
            NMsg nMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> emptyMessage.get());
            throw NExceptions.ofSafeNoSuchElementException(nMsg);
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
        return NOptional.ofEmpty(emptyMessage);
    }
}
