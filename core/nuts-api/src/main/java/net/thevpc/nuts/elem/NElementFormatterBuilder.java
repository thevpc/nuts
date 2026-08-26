package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NNewLineMode;

import java.util.function.Predicate;

/**
 * NElementFormatterBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementFormatterBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementFormatterBuilder of() {
        return NElementRPI.of().createElementFormatterBuilder();
    }

    /**
     * Sets the indent.
     *
     * @param spaces spaces
     * @return set indent result
     */
    NElementFormatterBuilder setIndent(int spaces);

    /**
     * Sets the complexity threshold.
     *
     * @param score score
     * @return set complexity threshold result
     */
    NElementFormatterBuilder setComplexityThreshold(int score);

    /**
     * Sets the column limit.
     *
     * @param lineWidth line width
     * @return set column limit result
     */
    NElementFormatterBuilder setColumnLimit(int lineWidth);

    /**
     * Removes the specified comments.
     *
     * @return remove comments result
     */
    NElementFormatterBuilder removeComments();

    /**
     * Removes the specified whitespaces.
     *
     * @return remove whitespaces result
     */
    NElementFormatterBuilder removeWhitespaces();

    /**
     * Removes the specified separators.
     *
     * @return remove separators result
     */
    NElementFormatterBuilder removeSeparators();

    /**
     * Removes the specified newlines.
     *
     * @return remove newlines result
     */
    NElementFormatterBuilder removeNewlines();

    /**
     * Removes the specified affixes.
     *
     * @param elementType element type
     * @param affixPredicate affix predicate
     * @return remove affixes result
     */
    NElementFormatterBuilder removeAffixes(NElementType elementType, Predicate<NBoundAffix> affixPredicate);

    /**
     * Adds the specified action.
     *
     * @param action action
     * @return add action result
     */
    NElementFormatterBuilder addAction(NElementFormatterAction action);

    /**
     * Adds the specified space.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return add space result
     */
    NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor);

    /**
     * Adds the specified space.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param space space
     * @return add space result
     */
    NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor, String space);

    /**
     * Sets the spaces.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param count count
     * @return set spaces result
     */
    NElementFormatterBuilder setSpaces(NElementType elementType, NAffixAnchor anchor, int count);

    /**
     * Sets the space.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param space space
     * @return set space result
     */
    NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor, String space);

    /**
     * Sets the space.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return set space result
     */
    NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor);

    /**
     * Removes the specified space.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return remove space result
     */
    NElementFormatterBuilder removeSpace(NElementType elementType, NAffixAnchor anchor);

    /**
     * Sets the tabs.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param count count
     * @return set tabs result
     */
    NElementFormatterBuilder setTabs(NElementType elementType, NAffixAnchor anchor, int count);

    /**
     * Sets the tab.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return set tab result
     */
    NElementFormatterBuilder setTab(NElementType elementType, NAffixAnchor anchor);

    /**
     * Adds the specified newline.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param mode mode
     * @return add newline result
     */
    NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode mode);

    /**
     * Sets the newline.
     *
     * @param elementType element type
     * @param anchor anchor
     * @param mode mode
     * @return set newline result
     */
    NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode mode);

    /**
     * Sets the newline.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return set newline result
     */
    NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor);

    /**
     * Adds the specified newline.
     *
     * @param elementType element type
     * @param anchor anchor
     * @return add newline result
     */
    NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor);

    /**
     * Indent.
     *
     * @return indent result
     */
    int indent();

    /**
     * Complexity threshold.
     *
     * @return complexity threshold result
     */
    int complexityThreshold();

    /**
     * Column limit.
     *
     * @return column limit result
     */
    int columnLimit();

    /**
     * Build.
     *
     * @return build result
     */
    NElementFormatter build();
}
