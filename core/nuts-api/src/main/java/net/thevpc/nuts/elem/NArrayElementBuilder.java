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
public interface NArrayElementBuilder extends NElementBuilder {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NArrayElementBuilder of() {
        return NElement.ofArrayBuilder();
    }

    /**
     * array items
     *
     * @return array items
     */
    List<NElement> children();

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NArrayElementBuilder remove(String child);

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
    NArrayElementBuilder clearChildren();

    /**
     * element at index
     *
     * @param index index
     * @return element at index
     */
    NOptional<NElement> getAt(int index);


    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(int value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Character value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(byte[] value);

    /**
     * all all elements in the given array builder
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(NArrayElementBuilder value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NArrayElementBuilder add(NElement element);

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
    NArrayElementBuilder setAt(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NArrayElementBuilder clear();

    /**
     * Adds the specified params.
     *
     * @param params params
     * @return add params result
     */
    NArrayElementBuilder addParams(List<NElement> params);

    /**
     * Adds the specified param.
     *
     * @param param param
     * @return add param result
     */
    NArrayElementBuilder addParam(NElement param);

    /**
     * Adds the specified param at.
     *
     * @param index index
     * @param param param
     * @return add param at result
     */
    NArrayElementBuilder addParamAt(int index, NElement param);

    /**
     * Removes the specified param at.
     *
     * @param index index
     * @return remove param at result
     */
    NArrayElementBuilder removeParamAt(int index);

    /**
     * Clear params.
     *
     * @return clear params result
     */
    NArrayElementBuilder clearParams();

    /**
     * Params.
     *
     * @return params result
     */
    NOptional<List<NElement>> params();

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
    NArrayElementBuilder name(String name);

    /**
     * Checks if is parametrized.
     *
     * @return is parametrized result
     */
    boolean isParametrized();

    /**
     * Sets the parametrized.
     *
     * @param parametrized parametrized
     * @return set parametrized result
     */
    NArrayElementBuilder setParametrized(boolean parametrized);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, NElement value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, String value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, Integer value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, Long value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, Double value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NArrayElementBuilder addParam(String name, Boolean value);

    /**
     * Sets the param at.
     *
     * @param i i
     * @param element element
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int i, NElement element);

    /**
     * Sets the params.
     *
     * @param params params
     * @return set params result
     */
    NArrayElementBuilder setParams(List<NElement> params);

    /**
     * Sets the children.
     *
     * @param params params
     * @return set children result
     */
    NArrayElementBuilder setChildren(List<NElement> params);


    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NArrayElementBuilder doWith(Consumer<NArrayElementBuilder> con);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NArrayElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NArrayElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NArrayElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NArrayElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NArrayElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NArrayElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NArrayElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NArrayElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NArrayElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NArrayElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NArrayElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NArrayElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NArrayElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NArrayElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NArrayElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NArrayElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NArrayElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NArrayElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NArrayElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NArrayElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NArrayElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NArrayElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NArrayElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NArrayElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NArrayElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NArrayElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NArrayElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NArrayElementBuilder metadata(NElementMetadata metadata);


    /// ///////


    /**
     * set value for property {@code name}
     *
     * @param name  property name
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NArrayElementBuilder set(String name, NElement value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Float value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Long value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, Character value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(String name, String value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Boolean value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Integer value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Double value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Long value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Short value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Character value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, Byte value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NArrayElementBuilder setParamAt(int index, String value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(NElement name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Number value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Boolean value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Byte value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Short value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Integer value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Long value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Float value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Character value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, Double value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NArrayElementBuilder add(String name, String value);

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    NArrayElementBuilder addAll(Map<NElement, NElement> other);

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    NArrayElementBuilder addAll(List<NElement> other);

    /**
     * Sets the all.
     *
     * @param other other
     * @return set all result
     */
    NArrayElementBuilder setAll(Map<NElement, NElement> other);

    /**
     * remove property
     *
     * @param name property name
     * @return this {@code this} instance
     */
    NArrayElementBuilder removePair(String name);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NArrayElementBuilder removeAllPairs(String name);

    /**
     * return value for name or null.
     * If multiple values are available return any of them.
     *
     * @param name key name
     * @return value for name or null
     */
    NOptional<NElement> get(String name);

    /**
     * Returns the all.
     *
     * @param s s
     * @return get all result
     */
    List<NElement> getAll(NElement s);


    /**
     * Sets the set.
     *
     * @param entry entry
     * @return set result
     */
    NArrayElementBuilder set(NPairElement entry);


    /**
     * remove child at index
     *
     * @param index element index
     * @return this builder
     * @since 0.8.9
     */
    NArrayElementBuilder removeAt(int index);

    /**
     * remove child pair when its key is the given name
     *
     * @param name entry key
     * @return this builder
     * @since 0.8.9
     */
    NArrayElementBuilder removePair(NElement name);

    /**
     * @param index index to add to, may be negative
     * @param item  item to add
     * @return this builder
     * @since 0.8.9
     */
    NArrayElementBuilder addAt(int index, NElement item);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NArrayElementBuilder removeAllPairs(NElement name);

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NArrayElementBuilder remove(NElement child);

    /**
     * Removes the specified all.
     *
     * @param child child
     * @return remove all result
     */
    NArrayElementBuilder removeAll(NElement child);


    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, NElement value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, String value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Character value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NArrayElementBuilder set(NElement name, Long value);

    /**
     * Returns the get.
     *
     * @param s s
     * @return get result
     */
    NOptional<NElement> get(NElement s);

}
