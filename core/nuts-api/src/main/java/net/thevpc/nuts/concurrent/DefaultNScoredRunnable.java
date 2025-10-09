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

import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @author thevpc
 */
public class DefaultNScoredRunnable implements NScoredRunnable {
    private final Runnable value;
    private final int score;
    private final Supplier<NMsg> emptyMessage;

    public DefaultNScoredRunnable(Runnable value, int score, Supplier<NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && score > 0) {
            throw new IllegalArgumentException(NI18n.of("null runnable requires invalid score"));
        } else if (this.value != null && score <= 0) {
            throw new IllegalArgumentException(NI18n.of("non null runnable requires valid score"));
        }
        this.score = score;
        this.emptyMessage = emptyMessage == null ? () -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public void run() {
        if (score>0) {
            value.run();
        } else {
            NMsg nMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> emptyMessage.get());
            throw NExceptions.ofSafeNoSuchElementException(nMsg);
        }
    }

    public int getScore(NScorableContext scorableContext) {
        return score;
    }

}
