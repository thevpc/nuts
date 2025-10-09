/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @param <T> value type
 * @author thevpc
 */
public class DefaultNScoredCallable<T> implements NScoredCallable<T> {
    private final Supplier<T> value;
    private final int score;
    private final Supplier<NMsg> emptyMessage;

    public DefaultNScoredCallable(Supplier<T> value, int score, Supplier<NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && score > 0) {
            throw new IllegalArgumentException(NI18n.of("null callable requires invalid support"));
        } else if (this.value != null && score <= 0) {
            throw new IllegalArgumentException(NI18n.of("non null callable requires valid support"));
        }
        this.score = score;
        this.emptyMessage = emptyMessage == null ? () -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public T call() {
        if (score>0) {
            return value.get();
        } else {
            NMsg nMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> emptyMessage.get());
            throw NExceptions.ofSafeNoSuchElementException(nMsg);
        }
    }

    public int getScore(NScorableContext scorableContext) {
        return score;
    }

}
