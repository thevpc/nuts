/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import java.util.function.Supplier;

/**
 * Default implementation of NutsSupported
 *
 * @param <T> value type
 * @author thevpc
 */
public class NutsDefaultSupported<T> implements NutsSupported<T> {
    public static final NutsSupported INVALID = new NutsDefaultSupported(null, -1);
    private final Supplier<T> value;
    private final int supportLevel;

    public NutsDefaultSupported(Supplier<T> value, int supportLevel) {
        this.value = value;
        this.supportLevel = supportLevel;
    }

    public T getValue() {
        return value == null ? null : value.get();
    }

    public int getSupportLevel() {
        return supportLevel;
    }
}
