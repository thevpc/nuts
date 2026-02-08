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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for manipulating {@link NObjectElement} instances
 *
 * @author thevpc
 * @app.category Format
 */
public interface NObjectElementBuilder extends NElementBuilder {

    static NObjectElementBuilder of() {
        return NElement.ofObjectBuilder();
    }

    NObjectElementBuilder setAt(int i, NElement element);
    /**
     * set value for property {@code name}
     *
     * @param name  property name
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NObjectElementBuilder set(String name, NElement value);

    NObjectElementBuilder set(String name, boolean value);

    NObjectElementBuilder set(String name, int value);

    NObjectElementBuilder set(String name, double value);

    NObjectElementBuilder set(String name, String value);

    /**
     * set value for property {@code name}
     *
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NObjectElementBuilder setParamAt(int index, NElement value);

    NObjectElementBuilder setParamAt(int index, boolean value);

    NObjectElementBuilder setParamAt(int index, int value);

    NObjectElementBuilder setParamAt(int index, double value);

    NObjectElementBuilder setParamAt(int index, String value);

    NObjectElementBuilder add(NElement name, NElement value);

    NObjectElementBuilder add(String name, NElement value);

    NObjectElementBuilder add(String name, boolean value);

    NObjectElementBuilder add(String name, int value);

    NObjectElementBuilder add(String name, double value);

    NObjectElementBuilder add(String name, String value);

    NObjectElementBuilder addAll(Map<NElement, NElement> other);

    NObjectElementBuilder addAll(List<NElement> other);

    NObjectElementBuilder setAll(Map<NElement, NElement> other);

    /**
     * remove all properties
     *
     * @return this {@code this} instance
     */
    NObjectElementBuilder clear();

    NObjectElementBuilder clearChildren();

    /**
     * remove property
     *
     * @param name property name
     * @return this {@code this} instance
     */
    NObjectElementBuilder removeEntry(String name);

    NObjectElementBuilder removeEntries(String name);

    /**
     * return value for name or null.
     * If multiple values are available return any of them.
     *
     * @param name key name
     * @return value for name or null
     */
    NOptional<NElement> get(String name);

    NOptional<NElement> getAt(int index);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    List<NElement> children();

    public List<NElement> getAll(NElement s);


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
    NObjectElementBuilder removeEntry(NElement name);

    /**
     * @param index index to add to, may be negative
     * @param item  item to add
     * @return this builder
     * @since 0.8.9
     */
    NObjectElementBuilder addAt(int index, NElement item);

    NObjectElementBuilder removeEntries(NElement name);

    NObjectElementBuilder remove(NElement child);

    NObjectElementBuilder removeAll(NElement child);


    NObjectElementBuilder set(NElement name, NElement value);

    NObjectElementBuilder set(NElement name, String value);

    NObjectElementBuilder set(NElement name, boolean value);

    NObjectElementBuilder set(NElement name, double value);

    NObjectElementBuilder set(NElement name, int value);

    NOptional<NElement> get(NElement s);

    /**
     * add all  key value entries binding
     *
     * @param entries other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NElement... entries);

    /**
     * add all  key value entries binding
     *
     * @param other other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NObjectElementBuilder other);

    NObjectElementBuilder addParams(List<NElement> params);

    NObjectElementBuilder addParam(String name, NElement value);

    NObjectElementBuilder addParam(String name, String value);

    NObjectElementBuilder addParam(String name, Integer value);

    NObjectElementBuilder addParam(String name, Long value);

    NObjectElementBuilder addParam(String name, Double value);

    NObjectElementBuilder addParam(String name, Boolean value);

    NObjectElementBuilder addParam(NElement param);

    NObjectElementBuilder addParamAt(int index, NElement param);

    NObjectElementBuilder removeParamAt(int index);

    NObjectElementBuilder clearParams();

    NOptional<List<NElement>> params();

    NOptional<String> name();

    NObjectElementBuilder name(String name);

    boolean isParametrized();

    NObjectElementBuilder setParametrized(boolean parametrized);

    NObjectElementBuilder setParams(List<NElement> params);

    NObjectElementBuilder setChildren(List<NElement> params);

    NObjectElementBuilder doWith(Consumer<NObjectElementBuilder> con);

    /**
     * create a immutable instance of {@link NObjectElement} representing
     * this builder.
     *
     * @return new instance of {@link NObjectElement}
     */
    NObjectElement build();

    /// ///////////////////////////////////////////////
    NObjectElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NObjectElementBuilder addAnnotation(NElementAnnotation annotation);

    NObjectElementBuilder addAnnotation(String name, NElement... args);

    NObjectElementBuilder addAffix(int index, NBoundAffix affix);

    NObjectElementBuilder setAffix(int index, NBoundAffix affix);

    NObjectElementBuilder addAffix(NBoundAffix affix);

    NObjectElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor);

    NObjectElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor);

    NObjectElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor);

    NObjectElementBuilder removeAffix(int index);

    NObjectElementBuilder removeAnnotation(NElementAnnotation annotation);

    NObjectElementBuilder clearAnnotations();

    NObjectElementBuilder clearAffixes();

    NObjectElementBuilder addLeadingComment(NElementComment comment);

    NObjectElementBuilder addLeadingComments(NElementComment... comments);

    NObjectElementBuilder addTrailingComments(NElementComment... comments);

    NObjectElementBuilder addTrailingComment(NElementComment comment);

    NObjectElementBuilder clearComments();

    NObjectElementBuilder copyFrom(NElementBuilder other);

    NObjectElementBuilder copyFrom(NElement other);

    NObjectElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy);

    NObjectElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy);

    NObjectElementBuilder addDiagnostic(NElementDiagnostic error);

    NObjectElementBuilder removeDiagnostic(NElementDiagnostic error);

    NObjectElementBuilder addAffixes(List<NBoundAffix> affixes);

    NObjectElementBuilder metadata(NElementMetadata metadata);
}
