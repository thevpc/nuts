package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMaps;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultNElementFormatter implements NElementFormatter {
    private List<NElementFormatterAction> actions;
    private boolean removeWhiteSpaces;
    private boolean removeRootSeparators;
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
    private static class NBoundAffixAndIndex {
        NBoundAffix value;
        int index;
        NBoundAffixAndIndex(NBoundAffix value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    public static final NElementFormatterAction REMOVED_ROOT_GARBAGE = new NElementFormatterAction() {
        @Override
        public void apply(NElementFormatContext context) {
            NElementBuilder builder = context.builder();


            List<NBoundAffixAndIndex> all = new ArrayList<>();
            List<NBoundAffix> affixes = builder.affixes();
            for (int i = 0; i < affixes.size(); i++) {
                all.add(new NBoundAffixAndIndex(affixes.get(i), i));
            }

            List<NBoundAffixAndIndex> before = all.stream()
                    .filter(x -> x.value.anchor() == NAffixAnchor.START)
                    .collect(Collectors.toList());
            List<NBoundAffixAndIndex> after = all.stream()
                    .filter(x -> x.value.anchor() == NAffixAnchor.END)
                    .collect(Collectors.toList());

            Set<Integer> toRemove = new HashSet<>();

            // Helper: is this affix type "cluster-eligible" (space/newline/separator)?
            // Comments and annotations are boundaries — they break clusters.

            toRemove.addAll(garbageClusters(before));
            // For END affixes, scan in reverse so clusters are built from the trailing edge inward
            Collections.reverse(after);
            toRemove.addAll(garbageClusters(after));

            // Remove in reverse index order to avoid index shifting
            toRemove.stream()
                    .sorted(Comparator.reverseOrder())
                    .forEach(builder::removeAffix);
        }

        private Set<Integer> garbageClusters(List<NBoundAffixAndIndex> affixes) {
            Set<Integer> toRemove = new HashSet<>();

            List<List<NBoundAffixAndIndex>> clusters = new ArrayList<>();
            List<NBoundAffixAndIndex> currentCluster = new ArrayList<>();

            for (NBoundAffixAndIndex item : affixes) {
                switch (item.value.affix().type()) {
                    case SPACE:
                    case NEWLINE:
                    case SEPARATOR: {
                        currentCluster.add(item);
                        break;
                    }
                    case LINE_COMMENT:
                    case BLOC_COMMENT:
                    case ANNOTATION: {
                        clusters.add(currentCluster);
                        currentCluster = new ArrayList<>();
                        break;
                    }
                }
            }
            clusters.add(currentCluster);

            for (int i = 0; i < clusters.size(); i++) {
                List<NBoundAffixAndIndex> cluster = clusters.get(i);
                boolean isOuterEdge = (i == 0);  // only the outermost edge, not the one adjacent to the value
                boolean hasSeparator = cluster.stream()
                        .anyMatch(x -> x.value.affix().type() == NAffixType.SEPARATOR);

                if (hasSeparator || isOuterEdge) {
                    cluster.forEach(x -> toRemove.add(x.index));
                }
            }

            return toRemove;
        }
    };
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
    public static final NElementFormatter STABLE = new DefaultNElementFormatter(
            Collections.emptyList(),
            NMaps.of(
                    "removeWhiteSpaces", false,
                    "removeSeparators", false,
                    "sanitize", true,
                    "strict", false
            )
    );
    public static final NElementFormatter SIMPLE = new DefaultNElementFormatter(
            Collections.emptyList(),
//            Arrays.asList(new TsonFormatSimpleAction()),
            NMaps.of(
                    "removeRootSeparators", true,
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
        this.removeRootSeparators = this.options.getBoolean("removeRootSeparators", () -> false);
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

        if(removeRootSeparators && context.path().isRoot()){
            REMOVED_ROOT_GARBAGE.apply(fc);
        }
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
