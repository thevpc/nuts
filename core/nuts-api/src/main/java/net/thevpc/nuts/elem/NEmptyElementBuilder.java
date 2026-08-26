package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

/**
 * NEmptyElementBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEmptyElementBuilder extends NElementBuilder {
    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NEmptyElementBuilder doWith(Consumer<NEmptyElementBuilder> con);

    /**
     * Build.
     *
     * @return build result
     */
    NEmptyElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NEmptyElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NEmptyElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NEmptyElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NEmptyElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NEmptyElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NEmptyElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NEmptyElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NEmptyElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NEmptyElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NEmptyElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NEmptyElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NEmptyElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NEmptyElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NEmptyElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NEmptyElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NEmptyElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NEmptyElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NEmptyElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NEmptyElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NEmptyElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NEmptyElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NEmptyElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NEmptyElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NEmptyElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NEmptyElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NEmptyElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NEmptyElementBuilder metadata(NElementMetadata metadata);
}
