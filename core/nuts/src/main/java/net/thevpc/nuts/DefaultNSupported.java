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
 * @param <T> value type
 * @author thevpc
 */
public class DefaultNSupported<T> implements NSupported<T> {
    private final Supplier<T> value;
    private final int supportLevel;
    private final Function<NSession, NMsg> emptyMessage;

    public DefaultNSupported(Supplier<T> value, int supportLevel, Function<NSession, NMsg> emptyMessage) {
        this.value = value;
        this.supportLevel = supportLevel;
        this.emptyMessage = emptyMessage==null?session ->NMsg.ofInvalidValue():emptyMessage;
    }

    public T getValue() {
        return value == null ? null : value.get();
    }

    public int getSupportLevel() {
        return supportLevel;
    }

    @Override
    public NOptional<T> toOptional() {
        if(isValid()){
            T v = getValue();
            if(v!=null){
                return NOptional.of(v);
            }
        }
        return NOptional.ofEmpty(emptyMessage);
    }
}
