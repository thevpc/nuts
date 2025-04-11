/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @author thevpc
 */
public class DefaultNRunnableSupport implements NRunnableSupport {
    private final Runnable value;
    private final int supportLevel;
    private final Supplier<NMsg> emptyMessage;

    public DefaultNRunnableSupport(Runnable value, int supportLevel, Supplier<NMsg> emptyMessage) {
        this.value = value;
        if (this.value == null && supportLevel > 0) {
            throw new IllegalArgumentException(NI18n.of("null runnable requires invalid support"));
        } else if (this.value != null && supportLevel <= 0) {
            throw new IllegalArgumentException(NI18n.of("non null runnable requires valid support"));
        }
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage == null ? () -> NMsg.ofInvalidValue() : emptyMessage;
    }

    public void run() {
        if (isValid()) {
            value.run();
        } else {
            NMsg nMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> emptyMessage.get());
            throw NExceptionHandler.ofSafeNoSuchElementException(nMsg);
        }
    }

    public int getSupportLevel() {
        return supportLevel;
    }

}
