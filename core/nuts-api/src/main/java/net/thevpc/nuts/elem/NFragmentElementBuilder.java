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
    NFragmentElementBuilder setChildren(List<NElement> params);

    List<NElement> children();

    NFragmentElementBuilder doWith(Consumer<NFragmentElementBuilder> con);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NFragmentElement build();

    /// ///////////////////////////////////////////////
    NFragmentElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NFragmentElementBuilder addAnnotation(NElementAnnotation annotation);

    NFragmentElementBuilder addAnnotation(String name, NElement... args);

    NFragmentElementBuilder addAffix(int index, NBoundAffix affix);

    NFragmentElementBuilder setAffix(int index, NBoundAffix affix);

    NFragmentElementBuilder addAffix(NBoundAffix affix);

    NFragmentElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NFragmentElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NFragmentElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NFragmentElementBuilder removeAffix(int index);

    NFragmentElementBuilder removeAnnotation(NElementAnnotation annotation);

    NFragmentElementBuilder clearAnnotations();

    NFragmentElementBuilder clearAffixes();

    NFragmentElementBuilder addLeadingComment(NElementComment comment);

    NFragmentElementBuilder addLeadingComments(NElementComment... comments);

    NFragmentElementBuilder addTrailingComments(NElementComment... comments);

    NFragmentElementBuilder addTrailingComment(NElementComment comment);

    NFragmentElementBuilder clearComments();

    NFragmentElementBuilder copyFrom(NElementBuilder other);

    NFragmentElementBuilder copyFrom(NElement other);

    NFragmentElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NFragmentElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NFragmentElementBuilder addDiagnostic(NElementDiagnostic error);

    NFragmentElementBuilder removeDiagnostic(NElementDiagnostic error);

    NFragmentElementBuilder addAffixes(List<NBoundAffix> affixes);

    NFragmentElementBuilder metadata(NElementMetadata metadata);
}
