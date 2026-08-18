package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

public interface NElementNavigator {
    static NElementNavigator ofRoot(NElement element) {
        return NElementRPI.of().createRootNavigator(element);
    }

    NOptional<NElementNavigator> parent();

    NElement element();

    NElementPath path();

    NOptional<NElementNavigator> resolve(NElementStep step);

    NOptional<NElementNavigator> resolve(NElementPath path);
}
