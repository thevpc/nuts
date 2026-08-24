package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffix;
import net.thevpc.nuts.elem.NAffixAnchor;
import net.thevpc.nuts.elem.NBoundAffix;
import net.thevpc.nuts.util.NAssert;

import java.util.Objects;

public class DefaultNBoundAffix implements NBoundAffix {
    private NAffix affix;
    private NAffixAnchor anchor;

    public static DefaultNBoundAffix of(NAffix affix, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(affix, "affix");
        NAssert.requireNamedNonNull(anchor, "anchor");
        return new DefaultNBoundAffix(affix, anchor);
    }

    private DefaultNBoundAffix(NAffix affix, NAffixAnchor anchor) {
        this.affix = affix;
        this.anchor = anchor;
    }

    @Override
    public NAffix affix() {
        return affix;
    }

    @Override
    public NAffixAnchor anchor() {
        return anchor;
    }

    @Override
    public String toString() {
        return anchor + "[" + affix + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNBoundAffix that = (DefaultNBoundAffix) o;
        return Objects.equals(affix, that.affix) && anchor == that.anchor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(affix, anchor);
    }
}
