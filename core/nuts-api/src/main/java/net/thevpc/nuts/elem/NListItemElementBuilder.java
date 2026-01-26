package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.List;
import java.util.function.Predicate;

public interface NListItemElementBuilder{

    NListItemElementBuilder value(NElement value);
    NListItemElementBuilder subList(NListElement subList);

    NListItemElementBuilder addAffix(NBoundAffix affix);

    NListItemElementBuilder addAffix(int index, NBoundAffix affix);

    NListItemElementBuilder setAffix(int index, NBoundAffix affix);

    NListItemElementBuilder addAffix(NAffix affix, NAffixAnchor anchor);

    NListItemElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NListItemElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NListItemElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NListItemElementBuilder removeAffix(int index);

    NListItemElementBuilder addAffixes(List<NBoundAffix> affixes);

    /// /////////////

    NListItemElementBuilder addAffixSpace(String space, NAffixAnchor anchor);

    NListItemElementBuilder addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor);

    NListItemElementBuilder addAffixSpace(int index, String space, NAffixAnchor anchor);

    NListItemElementBuilder addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor);


    NListItemElementBuilder removeAffixIf(Predicate<NBoundAffix> affixPredicate);

    NListItemElement build() ;
}
