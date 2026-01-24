package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

import java.util.Collections;
import java.util.List;

public class DefaultNElementFormatter implements NElementFormatter {
    private List<NElementFormatterAction> actions;
    private NElementFormatOptions options;

    public DefaultNElementFormatter(List<NElementFormatterAction> actions, NElementFormatOptions options) {
        this.actions = actions;
        this.options = options;
    }

    @Override
    public List<NElement> preTransform(NElementTransformContext context) {
        return NElementFormatter.super.preTransform((NElementFormatContext) context);
    }

    @Override
    public NElementTransformContext prepareChildContext(NElement parent, NElementTransformContext childContext) {
        NElementFormatContext c = ((NElementFormatContext)childContext).withOptions(options);
        for (NElementFormatterAction action : actions) {
            NElementFormatContext c0 = action.prepareChildContext(parent, c);
            if (c0 != null) {
                c = c0;
            }
        }
        return c;
    }

    @Override
    public List<NElement> postTransform(NElementTransformContext context) {
        NElementBuilder builder = context.element().builder();
        NElementFormatContext fc=((NElementFormatContext)context).withBuilder(builder).withOptions(options);
                // 1. We create a builder for the current element

        // 2. We apply the list of actions recorded in the builder
        // The actions will check the path.size() to determine indentation
        for (NElementFormatterAction action : actions) {
            action.apply(fc.withBuilder(builder));
        }
        return Collections.singletonList(builder.build());
    }
}
