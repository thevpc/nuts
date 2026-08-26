/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public interface NElementBuilder {
    /**
     * Type.
     *
     * @return type result
     */
    NElementType type();

    /**
     * Checks if is custom tree.
     *
     * @return is custom tree result
     */
    boolean isCustomTree();

    /**
     * Affixes.
     *
     * @return affixes result
     */
    List<NBoundAffix> affixes();

    /**
     * Diagnostics.
     *
     * @return diagnostics result
     */
    List<NElementDiagnostic> diagnostics();

    /**
     * Build.
     *
     * @return build result
     */
    NElement build();

    /**
     * Leading comments.
     *
     * @return leading comments result
     */
    List<NElementComment> leadingComments();

    /**
     * Trailing comments.
     *
     * @return trailing comments result
     */
    List<NElementComment> trailingComments();

    /**
     * Comments.
     *
     * @return comments result
     */
    List<NElementComment> comments();

    /**
     * Annotations.
     *
     * @return annotations result
     */
    List<NElementAnnotation> annotations();

    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @param anchor anchor
     * @return add affix result
     */
    NElementBuilder addAffix(NAffix affix, NAffixAnchor anchor);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NElementBuilder removeAffix(int affix);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NElementBuilder removeAffix(NAffix affix);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NElementBuilder removeAffix(NBoundAffix affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NElementBuilder clearComments();

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @return metadata result
     */
    NElementMetadata metadata();

    /// /////////////

    /**
     * Adds the specified space affix.
     *
     * @param space space
     * @param anchor anchor
     * @return add space affix result
     */
    NElementBuilder addSpaceAffix(String space, NAffixAnchor anchor);

    /**
     * Adds the specified new line affix.
     *
     * @param newLineMode new line mode
     * @param anchor anchor
     * @return add new line affix result
     */
    NElementBuilder addNewLineAffix(NNewLineMode newLineMode, NAffixAnchor anchor);

    /**
     * Adds the specified separator affix.
     *
     * @param separator separator
     * @param anchor anchor
     * @return add separator affix result
     */
    NElementBuilder addSeparatorAffix(String separator, NAffixAnchor anchor);

    /**
     * Adds the specified space affix.
     *
     * @param index index
     * @param space space
     * @param anchor anchor
     * @return add space affix result
     */
    NElementBuilder addSpaceAffix(int index, String space, NAffixAnchor anchor);

    /**
     * Adds the specified new line affix.
     *
     * @param index index
     * @param newLineMode new line mode
     * @param anchor anchor
     * @return add new line affix result
     */
    NElementBuilder addNewLineAffix(int index, NNewLineMode newLineMode, NAffixAnchor anchor);

    /**
     * Adds the specified separator affix.
     *
     * @param index index
     * @param separator separator
     * @param anchor anchor
     * @return add separator affix result
     */
    NElementBuilder addSeparatorAffix(int index, String separator, NAffixAnchor anchor);

    /**
     * Removes the specified affix if.
     *
     * @param affixPredicate affix predicate
     * @return remove affix if result
     */
    NElementBuilder removeAffixIf(Predicate<NBoundAffix> affixPredicate);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NElementBuilder metadata(NElementMetadata metadata);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NElementBuilder setAffixes(List<NBoundAffix> affixes);
}
