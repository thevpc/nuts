package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNListItemElement;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DefaultNListItemElementBuilder implements NListItemElementBuilder {
    private int depth;
    private String marker;
    private String markerVariant;
    private NElement value;
    private NListElement subList;
    private NElementType listType;
    private NBoundAffixList affixes = new NBoundAffixList();

    public DefaultNListItemElementBuilder(
            String marker,
            String markerVariant,
            int depth,
            NElement value,
            NListElement subList,
            NElementType listType,
            List<NBoundAffix> affixes
    ) {
        this.depth = depth;
        this.markerVariant = markerVariant;
        this.value = value;
        this.marker = marker;
        this.subList = subList;
        this.listType = listType;
        this.affixes.addAffixes(affixes);
        this.affixes.setFilter(x -> {
            switch (x.affix().type()) {
                case SPACE:
                case NEWLINE:
                    return true;
            }
            return false;
        });
    }

    @Override
    public NListItemElementBuilder addAffix(int index, NBoundAffix affix) {
        this.affixes.addAffix(index, affix);
        return this;
    }

    @Override
    public NListItemElementBuilder setAffix(int index, NBoundAffix affix) {
        this.affixes.setAffix(index, affix);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        this.affixes.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        this.affixes.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        this.affixes.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        this.affixes.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder removeAffix(int index) {
        this.affixes.removeAffix(index);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffixes(List<NBoundAffix> affixes) {
        this.affixes.addAffixes(affixes);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffixSpace(String space, NAffixAnchor anchor) {
        this.affixes.addAffixSpace(space, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor) {
        this.affixes.addAffixNewLine(newLineMode, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffixSpace(int index, String space, NAffixAnchor anchor) {
        this.affixes.addAffixSpace(index, space, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor) {
        this.affixes.addAffixNewLine(index, newLineMode, anchor);
        return this;
    }

    @Override
    public NListItemElementBuilder removeAffixIf(Predicate<NBoundAffix> affixPredicate) {
        this.affixes.removeAffixIf(affixPredicate);
        return this;
    }

    @Override
    public NListItemElementBuilder addAffix(NBoundAffix affix) {
        this.affixes.addAffix(affix);
        return this;
    }

    public NListItemElementBuilder value(NElement value) {
        this.value = value;
        return this;
    }

    public NListItemElementBuilder subList(NListElement subList) {
        this.subList = subList;
        return this;
    }


    public NListItemElement build() {
        String validMarker = marker;
        if (NBlankable.isBlank(validMarker)) {
            if (markerVariant.startsWith("[")) {
                validMarker = "[" + NStringUtils.repeat(markerVariant.charAt(1), depth) + "]";
            } else {
                validMarker = NStringUtils.repeat(markerVariant.charAt(1), depth);
            }
        }
        return new DefaultNListItemElement(
                listType,
                validMarker,
                markerVariant,
                depth, value, subList, affixes.list()
        );
    }
}
