package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementNewLine;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementSpace;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.function.Predicate;

public class DefaultNElementFormatterBuilder implements NElementFormatterBuilder, NElementFormatOptions {
    private List<NElementFormatterAction> actions;
    private Map<String, Object> options;

    public DefaultNElementFormatterBuilder() {
        this.actions = new ArrayList<>();
        this.options = new HashMap<>();
    }

    public DefaultNElementFormatterBuilder(List<NElementFormatterAction> actions, Map<String, Object> options) {
        this.actions = new ArrayList<>(actions);
        this.options = new HashMap<>(options);
    }

    public NElementFormatterBuilder addAction(NElementFormatterAction action) {
        if (action != null) {
            actions.add(action);
        }
        return this;
    }

    @Override
    public NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor) {
        return addSpace(elementType, anchor, " ");
    }

    public int getColumnLimit() {
        Object c = options.get("columns");
        int i = NLiteral.of(c).asInt().orElse(-1);
        if (i < 0) {
            i = 80;
        }
        return i;
    }

    @Override
    public NElementFormatterBuilder setColumnLimit(int columnLimit) {
        options.put("columns", columnLimit);
        return this;
    }

    @Override
    public NNewLineMode getNewLineMode() {
        Object c = options.get("newline");
        if (c instanceof NNewLineMode) {
            return (NNewLineMode) c;
        }
        return NNewLineMode.parse(c == null ? null : String.valueOf(c)).orElse(NNewLineMode.AUTO);
    }

    public DefaultNElementFormatterBuilder setNewLineMode(NNewLineMode newLineMode) {
        options.put("newline", newLineMode);
        return this;
    }

    @Override
    public int getIndent() {
        Object c = options.get("indent");
        int i = NLiteral.of(c).asInt().orElse(-1);
        if (i < 0) {
            i = 2;
        }
        return i;
    }

    @Override
    public DefaultNElementFormatterBuilder setIndent(int indent) {
        options.put("indent", indent);
        return this;
    }

    @Override
    public int getComplexityThreshold() {
        Object c = options.get("indent");
        int i = NLiteral.of(c).asInt().orElse(-1);
        if (i < 0) {
            i = 30;
        }
        return i;
    }

    @Override
    public DefaultNElementFormatterBuilder setComplexityThreshold(int complexityThreshold) {
        options.put("complexity", complexityThreshold);
        return this;
    }


    @Override
    public NElementFormatterBuilder removeWhitespaces() {
        removeAffixes(null, x -> {
            switch (x.affix().type()) {
                case SPACE:
                case NEWLINE:
                    return true;
            }
            return false;
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeSeparators() {
        removeAffixes(null, x -> {
            switch (x.affix().type()) {
                case SEPARATOR:
                    return true;
            }
            return false;
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeNewlines() {
        removeAffixes(null, x -> {
            switch (x.affix().type()) {
                case NEWLINE:
                    return true;
            }
            return false;
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeComments() {
        removeAffixes(null, x -> {
            switch (x.affix().type()) {
                case BLOC_COMMENT:
                case LINE_COMMENT:
                    return true;
            }
            return false;
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeAffixes(NElementType elementType, Predicate<NBoundAffix> affixPredicate) {
        addAction(new RemovedAffixesElementFormatterAction(elementType, affixPredicate));
        return this;
    }

    @Override
    public NElementFormatterBuilder setSpaces(NElementType elementType, NAffixAnchor anchor, int count) {
        return setSpace(elementType, anchor, NStringUtils.repeat(" ", count));
    }

    @Override
    public NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, " ");
    }

    @Override
    public NElementFormatterBuilder setTabs(NElementType elementType, NAffixAnchor anchor, int count) {
        return setSpace(elementType, anchor, NStringUtils.repeat("\t", count));
    }

    @Override
    public NElementFormatterBuilder setTab(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, "\t");
    }

    @Override
    public NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor, String space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                List<NBoundAffix> all = builder.affixes();
                boolean set = false;
                for (int i = all.size() - 1; i >= 0; i--) {
                    NBoundAffix a = all.get(i);
                    if (NAffixType.SPACE != a.affix().type()) {
                        continue;
                    }
                    if (anchor != null && anchor != a.anchor()) {
                        continue;
                    }
                    if (set) {
                        builder.removeAffix(i);
                    } else if (!NBlankable.isBlank(space)) {
                        builder.setAffix(i, NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                        set = true;
                    } else {
                        builder.removeAffix(i);
                        set = true;
                    }
                }
                if (!set) {
                    if (!NBlankable.isBlank(space)) {
                        builder.addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                    }
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor) {
        return setNewline(elementType, anchor, NNewLineMode.AUTO);
    }

    @Override
    public NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor) {
        return addNewline(elementType, anchor, NNewLineMode.AUTO);
    }

    @Override
    public NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                if (space != null && !NBlankable.isBlank(space)) {
                    builder.addAffix(NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                List<NBoundAffix> all = builder.affixes();
                boolean set = false;
                for (int i = all.size() - 1; i >= 0; i--) {
                    NBoundAffix a = all.get(i);
                    if (NAffixType.NEWLINE != a.affix().type()) {
                        continue;
                    }
                    if (anchor != null && anchor != a.anchor()) {
                        continue;
                    }
                    if (set) {
                        builder.removeAffix(i);
                    } else if (space != null && !NBlankable.isBlank(space)) {
                        builder.setAffix(i, NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                        set = true;
                    } else {
                        builder.removeAffix(i);
                        set = true;
                    }
                }
                if (!set) {
                    if (space != null && !NBlankable.isBlank(space)) {
                        builder.addAffix(NBoundAffix.of(DefaultNElementNewLine.of(space), anchor));
                    }
                }
            }
        });
        return this;
    }

    @Override
    public NElementFormatterBuilder removeSpace(NElementType elementType, NAffixAnchor anchor) {
        return setSpace(elementType, anchor, null);
    }

    @Override
    public NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor, String space) {
        addAction(new NElementFormatterAction() {
            public void apply(NElementFormatContext context) {
                NElementBuilder builder = context.builder();
                if (elementType != null && elementType != builder.type()) {
                    return;
                }
                if (!NBlankable.isBlank(space)) {
                    builder.addAffix(NBoundAffix.of(DefaultNElementSpace.of(space), anchor));
                }
            }
        });
        return this;
    }


    @Override
    public NElementFormatter build() {
        HashMap<String, Object> o = new HashMap<>(options);
        o.put("sanitize", true);
        return new DefaultNElementFormatter(actions, o);
    }

}
