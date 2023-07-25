/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import java.util.function.Function;
import java.util.function.Supplier;

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
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage==null?session ->NMsg.ofInvalidValue():emptyMessage;
    }

    public void run() {
        value.run();
    }

    public int getSupportLevel() {
        return supportLevel;
    }

}
