package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

public interface NElementNavigator {
    static NElementNavigator ofRoot(NElement element) {
        return NElements.of().createRootNavigator(element);
    }

    NOptional<NElementNavigator> parent();

    NElement element();

    NElementPath path();

    NOptional<NElementNavigator> resolve(NElementStep step);

    NOptional<NElementNavigator> resolve(NElementPath path);
}
