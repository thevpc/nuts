package net.thevpc.nuts;

import java.util.function.Function;

public interface NutsRunnable extends Runnable, NutsDescribable {
    static NutsRunnable of(Runnable o, String descr) {
        return NutsDescribables.ofRunnable(o, e -> e.ofString(descr));
    }

    static NutsRunnable of(Runnable o, NutsElement descr) {
        return NutsDescribables.ofRunnable(o, e -> descr);
    }

    static NutsRunnable of(Runnable o, Function<NutsElements, NutsElement> descr) {
        return NutsDescribables.ofRunnable(o, descr);
    }

}
