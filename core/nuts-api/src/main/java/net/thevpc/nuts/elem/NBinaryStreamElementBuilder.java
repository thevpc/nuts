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

import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.util.NAssignmentPolicy;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public interface NBinaryStreamElementBuilder extends NElementBuilder {

    /**
     * Value.
     *
     * @param value value
     * @return value result
     */
    NBinaryStreamElementBuilder value(NInputStreamProvider value);

    /**
     * Value.
     *
     * @return value result
     */
    NInputStreamProvider value();

    /**
     * Bloc identifier.
     *
     * @return bloc identifier result
     */
    String blocIdentifier();

    /**
     * Bloc identifier.
     *
     * @param blocIdentifier bloc identifier
     * @return bloc identifier result
     */
    NBinaryStreamElementBuilder blocIdentifier(String blocIdentifier);

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NBinaryStreamElementBuilder doWith(Consumer<NBinaryStreamElementBuilder> con);

    /**
     * Build.
     *
     * @return build result
     */
    NBinaryStreamElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NBinaryStreamElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NBinaryStreamElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NBinaryStreamElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NBinaryStreamElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NBinaryStreamElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NBinaryStreamElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NBinaryStreamElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NBinaryStreamElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NBinaryStreamElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NBinaryStreamElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NBinaryStreamElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NBinaryStreamElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NBinaryStreamElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NBinaryStreamElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NBinaryStreamElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NBinaryStreamElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NBinaryStreamElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NBinaryStreamElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NBinaryStreamElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NBinaryStreamElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NBinaryStreamElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NBinaryStreamElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NBinaryStreamElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NBinaryStreamElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NBinaryStreamElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NBinaryStreamElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NBinaryStreamElementBuilder metadata(NElementMetadata metadata);
}
