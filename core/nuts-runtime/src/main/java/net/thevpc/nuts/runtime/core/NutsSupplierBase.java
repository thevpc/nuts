package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsSupplier;

public abstract class NutsSupplierBase<T> implements NutsSupplier<T> {
    private int level;

    public NutsSupplierBase(int level) {
        if(level<=0){
            throw new IllegalArgumentException("level<=0");
        }
        this.level = level;
    }

    @Override
    public int level() {
        return level;
    }
}
