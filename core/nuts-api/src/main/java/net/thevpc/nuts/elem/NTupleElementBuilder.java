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
    static NTupleElementBuilder of() {
        return NElement.ofTupleBuilder();
    }

    NOptional<String> name();

    NTupleElementBuilder name(String name);

    NTupleElementBuilder removeAt(int index);

    List<NElement> params();

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

    NTupleElementBuilder setAt(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NTupleElementBuilder clear();

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

    NTupleElementBuilder set(String name, NElement value);

    NTupleElementBuilder set(String name, Boolean value);

    NTupleElementBuilder set(String name, Integer value);

    NTupleElementBuilder set(String name, Character value);

    NTupleElementBuilder set(String name, Double value);

    NTupleElementBuilder set(String name, Byte value);

    NTupleElementBuilder set(String name, Short value);

    NTupleElementBuilder set(String name, Float value);

    NTupleElementBuilder set(String name, Long value);

    NTupleElementBuilder set(String name, String value);

    NTupleElementBuilder set(NElement name, NElement value);

    NTupleElementBuilder add(NElement name, NElement value);

    NTupleElementBuilder add(String name, NElement value);

    NTupleElementBuilder add(String name, Number value);

    NTupleElementBuilder set(NElement name, Boolean value);

    NTupleElementBuilder set(NElement name, Integer value);

    NTupleElementBuilder set(NElement name, Long value);

    NTupleElementBuilder set(NElement name, Short value);

    NTupleElementBuilder set(NElement name, Byte value);

    NTupleElementBuilder set(NElement name, Character value);

    NTupleElementBuilder set(NElement name, Double value);

    NTupleElementBuilder set(NElement name, String value);

    NTupleElementBuilder set(NPairElement entry);

    NTupleElementBuilder add(String name, Boolean value);

    NTupleElementBuilder add(String name, Character value);

    NTupleElementBuilder add(String name, Byte value);

    NTupleElementBuilder add(String name, Short value);

    NTupleElementBuilder add(String name, Integer value);

    NTupleElementBuilder add(String name, Long value);

    NTupleElementBuilder add(String name, Float value);

    NTupleElementBuilder add(String name, Double value);

    NTupleElementBuilder add(String name, String value);

    NTupleElementBuilder addAll(Map<NElement, NElement> other);

    NTupleElementBuilder setParams(List<NElement> params);

    NTupleElementBuilder doWith(Consumer<NTupleElementBuilder> con);


    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NTupleElement build();

    /// ///////////////////////////////////////////////
    NTupleElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NTupleElementBuilder addAnnotation(NElementAnnotation annotation);

    NTupleElementBuilder addAnnotation(String name, NElement... args);

    NTupleElementBuilder addAffixAt(int index, NBoundAffix affix);

    NTupleElementBuilder setAffixAt(int index, NBoundAffix affix);

    NTupleElementBuilder setAffixes(List<NBoundAffix> affixes);

    NTupleElementBuilder addAffix(NBoundAffix affix);

    NTupleElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NTupleElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    NTupleElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NTupleElementBuilder removeAffix(int affix);

    NTupleElementBuilder removeAnnotation(NElementAnnotation annotation);

    NTupleElementBuilder clearAnnotations();

    NTupleElementBuilder clearAffixes();

    NTupleElementBuilder addLeadingComment(NElementComment comment);

    NTupleElementBuilder addLeadingComments(NElementComment... comments);

    NTupleElementBuilder addTrailingComments(NElementComment... comments);

    NTupleElementBuilder addTrailingComment(NElementComment comment);

    NTupleElementBuilder clearComments();

    NTupleElementBuilder copyFrom(NElementBuilder other);

    NTupleElementBuilder copyFrom(NElement other);

    NTupleElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NTupleElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NTupleElementBuilder addDiagnostic(NElementDiagnostic error);

    NTupleElementBuilder removeDiagnostic(NElementDiagnostic error);

    NTupleElementBuilder addAffixes(List<NBoundAffix> affixes);

    NTupleElementBuilder metadata(NElementMetadata metadata);
}
