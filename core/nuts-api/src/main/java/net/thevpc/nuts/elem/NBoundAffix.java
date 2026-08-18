package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;

public interface NBoundAffix {
    static NBoundAffix of(NAffix affix, NAffixAnchor anchor) {
        return NElementRPI.of().createBoundAffix(affix, anchor);
    }

    NAffix affix();

    NAffixAnchor anchor();
}
