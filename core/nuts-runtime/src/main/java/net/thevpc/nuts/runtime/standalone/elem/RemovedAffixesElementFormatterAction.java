package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

import java.util.function.Predicate;

public class RemovedAffixesElementFormatterAction implements NElementFormatterAction {
    private final NElementType elementType;
    private final Predicate<NBoundAffix> affixPredicate;

    public RemovedAffixesElementFormatterAction(NElementType elementType, Predicate<NBoundAffix> affixPredicate) {
        this.elementType = elementType;
        this.affixPredicate = affixPredicate;
    }

    @Override
    public void apply(NElementFormatContext context) {
        NElementBuilder builder = context.builder();
        if (elementType != null && elementType != builder.type()) {
            return;
        }
        builder.removeAffixIf(affixPredicate);
    }
}
