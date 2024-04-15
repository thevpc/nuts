/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Default implementation of NutsSupported
 *
 * @author thevpc
 */
public class DefaultNRunnableSupport implements NRunnableSupport {
    private final Runnable value;
    private final int supportLevel;
    private final Function<NSession, NMsg> emptyMessage;

    public DefaultNRunnableSupport(Runnable value, int supportLevel, Function<NSession, NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException("null runnable requires invalid support");
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException("non null runnable requires valid support");
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? session -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public void run(NSession session) {
        if (isValid()) {
            value.run();
        } else {
            NMsg nMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> emptyMessage.apply(session));
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

}
