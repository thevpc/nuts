package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

public class TsonCompactAction implements NElementFormatterAction {
    @Override
    public void apply(NElementFormatContext context) {
        NElementBuilder builder = context.builder();
        builder.removeAffixIf(x ->
                x.affix().type() == NAffixType.SPACE
                        || x.affix().type() == NAffixType.NEWLINE
        );
    }

    @Override
    public NElementFormatContext prepareChildContext(NElement parent, NElementFormatContext childContext) {
        return NElementFormatterAction.super.prepareChildContext(parent, childContext);
    }
}
