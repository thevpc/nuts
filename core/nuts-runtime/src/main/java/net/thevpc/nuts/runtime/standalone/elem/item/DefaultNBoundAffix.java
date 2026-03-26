package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NAffix;
import net.thevpc.nuts.elem.NAffixAnchor;
import net.thevpc.nuts.elem.NBoundAffix;
import net.thevpc.nuts.util.NAssert;

import java.util.Objects;

public class DefaultNBoundAffix implements NBoundAffix {
    private NAffix affix;
    private NAffixAnchor position;

    public static DefaultNBoundAffix of(NAffix affix, NAffixAnchor position) {
        NAssert.requireNamedNonNull(affix, "affix");
        NAssert.requireNamedNonNull(position, "position");
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNBoundAffix that = (DefaultNBoundAffix) o;
        return Objects.equals(affix, that.affix) && position == that.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(affix, position);
    }
}
