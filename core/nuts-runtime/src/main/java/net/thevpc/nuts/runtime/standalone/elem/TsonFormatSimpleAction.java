package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.text.NTreeVisitResult;

import java.util.IdentityHashMap;
import java.util.Map;

public class TsonFormatSimpleAction implements NElementFormatterAction {

    public String getIndentUnit() {
        return "  ";
    }

    public void apply(NElementFormatContext context) {
        NElementBuilder builder = context.builder();
        builder.removeAffixIf(x ->
                x.affix().type() == NAffixType.SPACE
                        || x.affix().type() == NAffixType.NEWLINE
        );
        NElement element = builder.build();
        switch (builder.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case FULL_OBJECT:
            case PARAM_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case FULL_ARRAY:
            case PARAM_ARRAY: {
                applyObjectOrArray(builder, context);
                return;
            }
            case NAMED_UPLET:
            case UPLET: {
                applyUplet(builder, context);
                return;
            }
            case PAIR: {
                applyPair((NPairElementBuilder) builder, context);
                return;
            }
        }
        applyDefault(builder, context);
    }

    private void applyPair(NPairElementBuilder builder, NElementFormatContext context) {
        applyDefault(builder, context);
    }

    private void applyObjectOrArray(NElementBuilder builder, NElementFormatContext context) {
        builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
        builder.addSeparatorAffix(",", NAffixAnchor.SEP_2);
    }

    private void applyUplet(NElementBuilder builder, NElementFormatContext context) {
        builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
    }

    private void applyDefault(NElementBuilder builder, NElementFormatContext context) {
    }

    private String formatCompact(NElement element) {
        return DefaultTsonWriter.formatTsonCompact(element);
    }

}
