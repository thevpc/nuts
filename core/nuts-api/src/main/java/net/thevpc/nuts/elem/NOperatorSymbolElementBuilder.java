package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;

/**
 * NOperatorSymbolElementBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NOperatorSymbolElementBuilder extends NElementBuilder {

    /**
     * Symbol.
     *
     * @return symbol result
     */
    NOperatorSymbol symbol();
    /**
     * Symbol.
     *
     * @param symbol symbol
     * @return symbol result
     */
    NOperatorSymbolElementBuilder symbol(NOperatorSymbol symbol);

    /**
     * Build.
     *
     * @return build result
     */
    NOperatorSymbolElement build();


    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NOperatorSymbolElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NOperatorSymbolElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NOperatorSymbolElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NOperatorSymbolElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NOperatorSymbolElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NOperatorSymbolElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NOperatorSymbolElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NOperatorSymbolElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NOperatorSymbolElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NOperatorSymbolElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NOperatorSymbolElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NOperatorSymbolElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NOperatorSymbolElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NOperatorSymbolElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NOperatorSymbolElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NOperatorSymbolElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NOperatorSymbolElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NOperatorSymbolElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NOperatorSymbolElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NOperatorSymbolElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NOperatorSymbolElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NOperatorSymbolElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NOperatorSymbolElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NOperatorSymbolElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NOperatorSymbolElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NOperatorSymbolElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NOperatorSymbolElementBuilder metadata(NElementMetadata metadata);
}
