package net.thevpc.nuts.elem;

public interface NBoundAffix {
    static NBoundAffix of(NAffix affix, NAffixAnchor anchor) {
        return NElement.ofBoundAffix(affix,anchor);
    }

    NAffix affix();

    NAffixAnchor anchor();
}
