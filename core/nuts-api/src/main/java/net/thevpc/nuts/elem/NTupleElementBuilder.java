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
import java.util.Map;
import java.util.function.Consumer;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NTupleElementBuilder extends NElementBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTupleElementBuilder of() {
        return NElement.ofTupleBuilder();
    }

    /**
     * Name.
     *
     * @return name result
     */
    NOptional<String> name();

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    NTupleElementBuilder name(String name);

    /**
     * Removes the specified at.
     *
     * @param index index
     * @return remove at result
     */
    NTupleElementBuilder removeAt(int index);

    /**
     * Params.
     *
     * @return params result
     */
    List<NElement> params();

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NTupleElementBuilder remove(String child);

    /**
     * element count
     *
     * @return element count
     */
    int size();


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
    NTupleElementBuilder copyFrom(NTupleElement value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Integer value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Character value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NTupleElementBuilder addAll(byte[] value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NTupleElementBuilder add(NElement element);

    /**
     * Sets the at.
     *
     * @param index index
     * @param element element
     * @return set at result
     */
    NTupleElementBuilder setAt(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NTupleElementBuilder clear();

    /**
     * Clear params.
     *
     * @return clear params result
     */
    NTupleElementBuilder clearParams();

    /**
     * add new element to the end of the array.
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NTupleElementBuilder remove(int index);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, NElement value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Character value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Float value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, Long value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(String name, String value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(NElement name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Number value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Long value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Character value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NTupleElementBuilder set(NElement name, String value);

    /**
     * Sets the set.
     *
     * @param entry entry
     * @return set result
     */
    NTupleElementBuilder set(NPairElement entry);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Boolean value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Character value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Byte value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Short value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Integer value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Long value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Float value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, Double value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NTupleElementBuilder add(String name, String value);

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    NTupleElementBuilder addAll(Map<NElement, NElement> other);

    /**
     * Sets the params.
     *
     * @param params params
     * @return set params result
     */
    NTupleElementBuilder setParams(List<NElement> params);

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NTupleElementBuilder doWith(Consumer<NTupleElementBuilder> con);


    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NTupleElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NTupleElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NTupleElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NTupleElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NTupleElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NTupleElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NTupleElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NTupleElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NTupleElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NTupleElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NTupleElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NTupleElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NTupleElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NTupleElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NTupleElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NTupleElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NTupleElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NTupleElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NTupleElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NTupleElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NTupleElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NTupleElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NTupleElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NTupleElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NTupleElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NTupleElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NTupleElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NTupleElementBuilder metadata(NElementMetadata metadata);
}
