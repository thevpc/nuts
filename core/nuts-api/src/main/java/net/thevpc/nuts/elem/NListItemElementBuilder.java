package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.List;
import java.util.function.Predicate;

/**
 * NListItemElementBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NListItemElementBuilder{

    /**
     * Value.
     *
     * @param value value
     * @return value result
     */
    NListItemElementBuilder value(NElement value);
    /**
     * Sub list.
     *
     * @param subList sub list
     * @return sub list result
     */
    NListItemElementBuilder subList(NListElement subList);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NListItemElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix.
     *
     * @param index index
     * @param affix affix
     * @return add affix result
     */
    NListItemElementBuilder addAffix(int index, NBoundAffix affix);

    /**
     * Sets the affix.
     *
     * @param index index
     * @param affix affix
     * @return set affix result
     */
    NListItemElementBuilder setAffix(int index, NBoundAffix affix);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @param anchor anchor
     * @return add affix result
     */
    NListItemElementBuilder addAffix(NAffix affix, NAffixAnchor anchor);

    /**
     * Adds the specified affix.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix result
     */
    NListItemElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix result
     */
    NListItemElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NListItemElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param index index
     * @return remove affix result
     */
    NListItemElementBuilder removeAffix(int index);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NListItemElementBuilder addAffixes(List<NBoundAffix> affixes);

    /// /////////////

    /**
     * Adds the specified affix space.
     *
     * @param space space
     * @param anchor anchor
     * @return add affix space result
     */
    NListItemElementBuilder addAffixSpace(String space, NAffixAnchor anchor);

    /**
     * Adds the specified affix new line.
     *
     * @param newLineMode new line mode
     * @param anchor anchor
     * @return add affix new line result
     */
    NListItemElementBuilder addAffixNewLine(NNewLineMode newLineMode, NAffixAnchor anchor);

    /**
     * Adds the specified affix space.
     *
     * @param index index
     * @param space space
     * @param anchor anchor
     * @return add affix space result
     */
    NListItemElementBuilder addAffixSpace(int index, String space, NAffixAnchor anchor);

    /**
     * Adds the specified affix new line.
     *
     * @param index index
     * @param newLineMode new line mode
     * @param anchor anchor
     * @return add affix new line result
     */
    NListItemElementBuilder addAffixNewLine(int index, NNewLineMode newLineMode, NAffixAnchor anchor);


    /**
     * Removes the specified affix if.
     *
     * @param affixPredicate affix predicate
     * @return remove affix if result
     */
    NListItemElementBuilder removeAffixIf(Predicate<NBoundAffix> affixPredicate);

    /**
     * Build.
     *
     * @return build result
     */
    NListItemElement build() ;
}
