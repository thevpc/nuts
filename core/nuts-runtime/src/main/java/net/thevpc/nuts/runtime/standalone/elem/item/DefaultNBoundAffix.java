package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffix;
import net.thevpc.nuts.elem.NAffixAnchor;
import net.thevpc.nuts.elem.NBoundAffix;
import net.thevpc.nuts.util.NAssert;

public class DefaultNBoundAffix implements NBoundAffix {
    private NAffix affix;
    private NAffixAnchor position;

    public static DefaultNBoundAffix of(NAffix affix, NAffixAnchor position) {
        NAssert.requireNonNull(affix, "affix");
        NAssert.requireNonNull(position, "position");
        return new DefaultNBoundAffix(affix, position);
    }

    private DefaultNBoundAffix(NAffix affix, NAffixAnchor position) {
        this.affix = affix;
        this.position = position;
    }

    @Override
    public NAffix affix() {
        return affix;
    }

    @Override
    public NAffixAnchor anchor() {
        return position;
    }

    @Override
    public String toString() {
        return position + "[" + affix + ']';
    }
}
