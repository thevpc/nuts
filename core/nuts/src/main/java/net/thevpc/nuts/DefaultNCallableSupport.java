/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import net.thevpc.nuts.util.NApiUtils;
import net.thevpc.nuts.util.NCallable;

import java.util.NoSuchElementException;
import java.util.function.Function;
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
    private final Function<NSession, NMsg> emptyMessage;

    public DefaultNCallableSupport(Supplier<T> value, int supportLevel, Function<NSession, NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException("null callable requires invalid support");
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException("non null callable requires valid support");
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? session -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public T call(NSession session) {
        if (isValid()) {
            return value.get();
        } else {
            NMsg nMsg = NApiUtils.resolveValidErrorMessage(() -> emptyMessage.apply(session));
            if (session == null) {
                throw new NoSuchElementException(nMsg.toString());
            } else {
                throw new NNoSuchElementException(session, nMsg);
            }
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
        return NOptional.ofEmpty(emptyMessage);
    }
}
