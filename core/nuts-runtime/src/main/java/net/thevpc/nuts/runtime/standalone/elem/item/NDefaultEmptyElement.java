package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNEmptyElementBuilder;

import java.util.List;

public class NDefaultEmptyElement extends AbstractNElement implements NEmptyElement {
    public NDefaultEmptyElement(List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics) {
        super(NElementType.EMPTY, affixes, diagnostics);
    }

    @Override
    public NEmptyElementBuilder builder() {
        NEmptyElementBuilder b = new DefaultNEmptyElementBuilder();
        b.addAffixes(affixes());
        return b;
    }
}
