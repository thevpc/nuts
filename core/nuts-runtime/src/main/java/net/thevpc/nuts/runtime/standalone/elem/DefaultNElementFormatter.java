package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMaps;

import java.util.*;

public class DefaultNElementFormatter implements NElementFormatter {
    private List<NElementFormatterAction> actions;
    private boolean removeWhiteSpaces;
    private boolean removeSeparators;
    private boolean sanitize;
    private boolean strict;
    private DefaultNElementFormatOptions options;
    public static final RemovedAffixesElementFormatterAction REMOVED_WHITESPACES_ELEMENT_FORMATTER_ACTION = new RemovedAffixesElementFormatterAction(null,
            a -> {
                switch (a.affix().type()) {
                    case SPACE:
                    case NEWLINE:
                        return true;
                }
                return false;
            });
    public static final RemovedAffixesElementFormatterAction REMOVED_SEPARATORS_ELEMENT_FORMATTER_ACTION = new RemovedAffixesElementFormatterAction(null,
            a -> {
                switch (a.affix().type()) {
                    case SEPARATOR:
                        return true;
                }
                return false;
            });
    public static final TsonFormatSanitizerAction TSON_FORMAT_SANITIZER_ACTION_COMPACT = new TsonFormatSanitizerAction(true);
    public static final TsonFormatSanitizerAction TSON_FORMAT_SANITIZER_ACTION_SAFE = new TsonFormatSanitizerAction(false);

    public static final NElementFormatter COMPACT = new DefaultNElementFormatter(
            Collections.emptyList(),
            NMaps.of(
                    "removeWhiteSpaces", true,
                    "removeSeparators", true,
                    "sanitize", true,
                    "strict", true
            )

    );
    public static final NElementFormatter PRETTY = new DefaultNElementFormatter(
            Arrays.asList(new TsonFormatPrettyAction()),
            NMaps.of(
                    "removeWhiteSpaces", (Object)true,
                    "removeSeparators", true,
                    "sanitize", true,
                    "strict", false,
                    "columns", 80,
                    "complexity", 30
            )

    );
    public static final NElementFormatter SAFE = new DefaultNElementFormatter(
            Collections.emptyList(),
            NMaps.of(
                    "removeWhiteSpaces", false,
                    "removeSeparators", false,
                    "sanitize", true,
                    "strict", false
            )
    );
    public static final NElementFormatter VERBATIM = new DefaultNElementFormatter(
            Collections.emptyList(),
            NMaps.of(
                    "removeWhiteSpaces", false,
                    "removeSeparators", false,
                    "sanitize", false
            )
    );

    public DefaultNElementFormatter(List<NElementFormatterAction> actions,
                                    Map<String, Object> options
    ) {
        this.actions = new ArrayList<>(actions);
        this.options = new DefaultNElementFormatOptions(options);
        this.removeWhiteSpaces = this.options.getBoolean("removeWhiteSpaces", () -> false);
        this.removeSeparators = this.options.getBoolean("removeSeparators", () -> false);
        this.sanitize = this.options.getBoolean("sanitize", () -> false);
        this.strict = this.options.getBoolean("strict", () -> false);
    }

    @Override
    public List<NElement> preTransform(NElementTransformContext context) {
        return NElementFormatter.super.preTransform((NElementFormatContext) context);
    }

    @Override
    public NElementTransformContext prepareChildContext(NElement parent, NElementTransformContext childContext) {
        NElementFormatContext c = ((NElementFormatContext) childContext).withOptions(options);
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
        NElementFormatContext fc = ((NElementFormatContext) context).withBuilder(builder).withOptions(options);
        // 1. We create a builder for the current element

        if (removeWhiteSpaces) {
            REMOVED_WHITESPACES_ELEMENT_FORMATTER_ACTION.apply(fc);
        }
        if (removeSeparators) {
            REMOVED_SEPARATORS_ELEMENT_FORMATTER_ACTION.apply(fc);
        }
        // 2. We apply the list of actions recorded in the builder
        // The actions will check the path.size() to determine indentation
        for (NElementFormatterAction action : actions) {
            action.apply(fc.withBuilder(builder));
        }
        if (sanitize) {
            if(strict) {
                TSON_FORMAT_SANITIZER_ACTION_COMPACT.apply(fc);
            }else{
                TSON_FORMAT_SANITIZER_ACTION_SAFE.apply(fc);
            }
        }
        return Collections.singletonList(builder.build());
    }

    @Override
    public NElementFormatterBuilder builder() {
        Map<String, Object> m = options.toMap();
        m.put("sanitize", sanitize);
        m.put("strict", strict);
        m.put("removeWhiteSpaces", removeWhiteSpaces);
        m.put("removeSeparators", removeSeparators);
        return new DefaultNElementFormatterBuilder(actions, m);
    }
}
