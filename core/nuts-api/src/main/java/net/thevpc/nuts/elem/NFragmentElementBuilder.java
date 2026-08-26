/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NFragmentElementBuilder extends NElementBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NFragmentElementBuilder of() {
        return NElement.ofFragmentBuilder();
    }

    /**
     * array items
     *
     * @return array items
     */
    List<NElement> items();

    /**
     * element count
     *
     * @return element count
     */
    int size();

    /**
     * Clear children.
     *
     * @return clear children result
     */
    NFragmentElementBuilder clearChildren();

    /**
     * element at index
     *
     * @param index index
     * @return element at index
     */
    NOptional<NElement> get(int index);


    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(int value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(char value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(byte[] value);

    /**
     * all all elements in the given array builder
     *
     * @param value value
     * @return {@code this} instance
     */
    NFragmentElementBuilder addAll(NFragmentElementBuilder value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NFragmentElementBuilder add(NElement element);

    /**
     * insert new element at the given index.
     *
     * @param index   index to insert into
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException      if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NFragmentElementBuilder insert(int index, NElement element);

    /**
     * update element at the given index.
     *
     * @param index   index to update
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException      if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    NFragmentElementBuilder setAt(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NFragmentElementBuilder clear();

    /**
     * add new element to the end of the array.
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NFragmentElementBuilder remove(int index);
    /**
     * Sets the children.
     *
     * @param params params
     * @return set children result
     */
    NFragmentElementBuilder setChildren(List<NElement> params);

    /**
     * Children.
     *
     * @return children result
     */
    List<NElement> children();

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NFragmentElementBuilder doWith(Consumer<NFragmentElementBuilder> con);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NFragmentElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NFragmentElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NFragmentElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NFragmentElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NFragmentElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NFragmentElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NFragmentElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NFragmentElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NFragmentElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NFragmentElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NFragmentElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NFragmentElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NFragmentElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NFragmentElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NFragmentElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NFragmentElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NFragmentElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NFragmentElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NFragmentElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NFragmentElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NFragmentElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NFragmentElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NFragmentElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NFragmentElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NFragmentElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NFragmentElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NFragmentElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NFragmentElementBuilder metadata(NElementMetadata metadata);
}
