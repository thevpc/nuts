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

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Builder for manipulating {@link NObjectElement} instances
 *
 * @author thevpc
 * @app.category Format
 */
public interface NObjectElementBuilder extends NElementBuilder {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NObjectElementBuilder of() {
        return NElement.ofObjectBuilder();
    }

    /**
     * Sets the at.
     *
     * @param i i
     * @param element element
     * @return set at result
     */
    NObjectElementBuilder setAt(int i, NElement element);
    /**
     * Sets the at.
     *
     * @param i i
     * @param element element
     * @return set at result
     */
    NObjectElementBuilder setAt(int i, Instant element);

    /**
     * set value for property {@code name}
     *
     * @param name  property name
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NObjectElementBuilder set(String name, Instant value);
    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, NElement value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Float value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, String value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Long value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(String name, Character value);

    /**
     * set value for property {@code name}
     *
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NObjectElementBuilder setParamAt(int index, NElement value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Boolean value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Integer value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Double value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Long value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Short value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Character value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, Byte value);

    /**
     * Sets the param at.
     *
     * @param index index
     * @param value value
     * @return set param at result
     */
    NObjectElementBuilder setParamAt(int index, String value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(NElement name, NElement value);
    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(NElement name, Instant value);

    /**
     * Adds the specified if.
     *
     * @param name name
     * @param value value
     * @param predicate predicate
     * @return add if result
     */
    NObjectElementBuilder addIf(String name, NElement value, Predicate<NElement> predicate);

    /**
     * Adds the specified if.
     *
     * @param name name
     * @param value value
     * @param predicate predicate
     * @return add if result
     */
    NObjectElementBuilder addIf(NElement name, NElement value, Predicate<NElement> predicate);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, NElement value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Number value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Boolean value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Short value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Integer value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Double value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Long value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Float value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Byte value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, Character value);

    /**
     * Adds add.
     *
     * @param name name
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String name, String value);

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    NObjectElementBuilder addAll(Map<NElement, NElement> other);

    /**
     * Adds the specified all.
     *
     * @param other other
     * @return add all result
     */
    NObjectElementBuilder addAll(List<NElement> other);

    /**
     * Sets the all.
     *
     * @param other other
     * @return set all result
     */
    NObjectElementBuilder setAll(Map<NElement, NElement> other);

    /**
     * remove all properties
     *
     * @return this {@code this} instance
     */
    NObjectElementBuilder clear();

    /**
     * Clear children.
     *
     * @return clear children result
     */
    NObjectElementBuilder clearChildren();

    /**
     * remove property
     *
     * @param name property name
     * @return this {@code this} instance
     */
    NObjectElementBuilder removePair(String name);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NObjectElementBuilder removeAllPairs(String name);

    /**
     * return value for name or null.
     * If multiple values are available return any of them.
     *
     * @param name key name
     * @return value for name or null
     */
    NOptional<NElement> get(String name);

    /**
     * Returns the at.
     *
     * @param index index
     * @return get at result
     */
    NOptional<NElement> getAt(int index);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    List<NElement> children();

    /**
     * Returns the all.
     *
     * @param s s
     * @return get all result
     */
    List<NElement> getAll(NElement s);


    /**
     * element count
     *
     * @return element count
     */
    int size();

    /**
     * add entry key value binding
     *
     * @param entry other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder add(NElement entry);

    /**
     * Sets the set.
     *
     * @param entry entry
     * @return set result
     */
    NObjectElementBuilder set(NPairElement entry);


    /**
     * remove child at index
     *
     * @param index element index
     * @return this builder
     * @since 0.8.9
     */
    NObjectElementBuilder removeAt(int index);

    /**
     * remove child pair when its key is the given name
     *
     * @param name entry key
     * @return this builder
     * @since 0.8.9
     */
    NObjectElementBuilder removePair(NElement name);

    /**
     * @param index index to add to, may be negative
     * @param item  item to add
     * @return this builder
     * @since 0.8.9
     */
    NObjectElementBuilder addAt(int index, NElement item);

    /**
     * Removes the specified all pairs.
     *
     * @param name name
     * @return remove all pairs result
     */
    NObjectElementBuilder removeAllPairs(NElement name);

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NObjectElementBuilder remove(String child);

    /**
     * Removes remove.
     *
     * @param child child
     * @return remove result
     */
    NObjectElementBuilder remove(NElement child);

    /**
     * Removes the specified all.
     *
     * @param child child
     * @return remove all result
     */
    NObjectElementBuilder removeAll(NElement child);


    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, NElement value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, String value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Boolean value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Double value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Float value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Integer value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Long value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Short value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Byte value);

    /**
     * Sets the set.
     *
     * @param name name
     * @param value value
     * @return set result
     */
    NObjectElementBuilder set(NElement name, Character value);

    /**
     * Returns the get.
     *
     * @param s s
     * @return get result
     */
    NOptional<NElement> get(NElement s);

    /**
     * add all  key value entries binding
     *
     * @param entries other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NElement... entries);

    /**
     * Adds the specified params.
     *
     * @param params params
     * @return add params result
     */
    NObjectElementBuilder addParams(List<NElement> params);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, NElement value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, String value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, Integer value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, Long value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, Double value);

    /**
     * Adds the specified param.
     *
     * @param name name
     * @param value value
     * @return add param result
     */
    NObjectElementBuilder addParam(String name, Boolean value);

    /**
     * Adds the specified param.
     *
     * @param param param
     * @return add param result
     */
    NObjectElementBuilder addParam(NElement param);

    /**
     * Adds the specified param at.
     *
     * @param index index
     * @param param param
     * @return add param at result
     */
    NObjectElementBuilder addParamAt(int index, NElement param);

    /**
     * Removes the specified param at.
     *
     * @param index index
     * @return remove param at result
     */
    NObjectElementBuilder removeParamAt(int index);

    /**
     * Clear params.
     *
     * @return clear params result
     */
    NObjectElementBuilder clearParams();

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
    NObjectElementBuilder name(String name);

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
    NObjectElementBuilder setParametrized(boolean parametrized);

    /**
     * Sets the params.
     *
     * @param params params
     * @return set params result
     */
    NObjectElementBuilder setParams(List<NElement> params);

    /**
     * Sets the children.
     *
     * @param params params
     * @return set children result
     */
    NObjectElementBuilder setChildren(List<NElement> params);

    /**
     * Do with.
     *
     * @param con con
     * @return do with result
     */
    NObjectElementBuilder doWith(Consumer<NObjectElementBuilder> con);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(Collection<NElement> value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(NObjectElementBuilder value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(String[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(int[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(double[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(long[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(float[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(boolean[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(char[] value);

    /**
     * Adds the specified all.
     *
     * @param value value
     * @return add all result
     */
    NObjectElementBuilder addAll(byte[] value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Integer value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Long value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Double value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Float value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Byte value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Boolean value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Character value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(Number value);

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    NObjectElementBuilder add(String value);

    /**
     * create a immutable instance of {@link NObjectElement} representing
     * this builder.
     *
     * @return new instance of {@link NObjectElement}
     */
    NObjectElement build();

    /// ///////////////////////////////////////////////
    /**
     * Adds the specified annotations.
     *
     * @param annotations annotations
     * @return add annotations result
     */
    NObjectElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    /**
     * Adds the specified annotation.
     *
     * @param annotation annotation
     * @return add annotation result
     */
    NObjectElementBuilder addAnnotation(NElementAnnotation annotation);

    /**
     * Adds the specified annotation.
     *
     * @param name name
     * @param args args
     * @return add annotation result
     */
    NObjectElementBuilder addAnnotation(String name, NElement... args);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @return add affix at result
     */
    NObjectElementBuilder addAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @return set affix at result
     */
    NObjectElementBuilder setAffixAt(int index, NBoundAffix affix);

    /**
     * Sets the affixes.
     *
     * @param affixes affixes
     * @return set affixes result
     */
    NObjectElementBuilder setAffixes(List<NBoundAffix> affixes);

    /**
     * Adds the specified affix.
     *
     * @param affix affix
     * @return add affix result
     */
    NObjectElementBuilder addAffix(NBoundAffix affix);

    /**
     * Adds the specified affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return add affix at result
     */
    NObjectElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Sets the affix at.
     *
     * @param index index
     * @param affix affix
     * @param anchor anchor
     * @return set affix at result
     */
    NObjectElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor);

    /**
     * Removes the specified affixes.
     *
     * @param type type
     * @param anchor anchor
     * @return remove affixes result
     */
    NObjectElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    /**
     * Removes the specified affix.
     *
     * @param affix affix
     * @return remove affix result
     */
    NObjectElementBuilder removeAffix(int affix);

    /**
     * Removes the specified annotation.
     *
     * @param annotation annotation
     * @return remove annotation result
     */
    NObjectElementBuilder removeAnnotation(NElementAnnotation annotation);

    /**
     * Clear annotations.
     *
     * @return clear annotations result
     */
    NObjectElementBuilder clearAnnotations();

    /**
     * Clear affixes.
     *
     * @return clear affixes result
     */
    NObjectElementBuilder clearAffixes();

    /**
     * Adds the specified leading comment.
     *
     * @param comment comment
     * @return add leading comment result
     */
    NObjectElementBuilder addLeadingComment(NElementComment comment);

    /**
     * Adds the specified leading comments.
     *
     * @param comments comments
     * @return add leading comments result
     */
    NObjectElementBuilder addLeadingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comments.
     *
     * @param comments comments
     * @return add trailing comments result
     */
    NObjectElementBuilder addTrailingComments(NElementComment... comments);

    /**
     * Adds the specified trailing comment.
     *
     * @param comment comment
     * @return add trailing comment result
     */
    NObjectElementBuilder addTrailingComment(NElementComment comment);

    /**
     * Clear comments.
     *
     * @return clear comments result
     */
    NObjectElementBuilder clearComments();

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NObjectElementBuilder copyFrom(NElementBuilder other);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NObjectElementBuilder copyFrom(NElement other);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NObjectElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    /**
     * Copy from.
     *
     * @param other other
     * @param assignmentPolicy assignment policy
     * @return copy from result
     */
    NObjectElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    /**
     * Adds the specified diagnostic.
     *
     * @param error error
     * @return add diagnostic result
     */
    NObjectElementBuilder addDiagnostic(NElementDiagnostic error);

    /**
     * Removes the specified diagnostic.
     *
     * @param error error
     * @return remove diagnostic result
     */
    NObjectElementBuilder removeDiagnostic(NElementDiagnostic error);

    /**
     * Adds the specified affixes.
     *
     * @param affixes affixes
     * @return add affixes result
     */
    NObjectElementBuilder addAffixes(List<NBoundAffix> affixes);

    /**
     * Metadata.
     *
     * @param metadata metadata
     * @return metadata result
     */
    NObjectElementBuilder metadata(NElementMetadata metadata);
}
