/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNObjectElement;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public class DefaultNObjectElementBuilder extends AbstractNElementBuilder implements NObjectElementBuilder {

    private final List<NElement> values = new ArrayList<>();

    private String name;
    private List<NElement> params;

    public DefaultNObjectElementBuilder() {
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, name);
    }

    public NObjectElementBuilder name(String name) {
        this.name = name;
        return this;
    }

    public boolean isParametrized() {
        return params != null;
    }


    public NObjectElementBuilder setParametrized(boolean parametrized) {
        if (parametrized) {
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addParams(List<NElement> params) {
        this.params = CoreNElementUtils.addAll(params, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder addParam(NElement param) {
        this.params = CoreNElementUtils.add(param, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder setParamAt(int index, NElement param) {
        this.params = CoreNElementUtils.setAt(index, param, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder setParamAt(int index, boolean value) {
        return setParamAt(index, NElement.ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder setParamAt(int index, int value) {
        return setParamAt(index, NElement.ofInt(value));
    }

    @Override
    public NObjectElementBuilder setParamAt(int index, double value) {
        return setParamAt(index, NElement.ofDouble(value));
    }

    @Override
    public NObjectElementBuilder setParamAt(int index, String value) {
        return setParamAt(index, NElement.ofString(value));
    }

    @Override
    public NObjectElementBuilder setParams(List<NElement> params) {
        this.params = CoreNElementUtils.setAll(params, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder setChildren(List<NElement> values) {
        this.values.clear();
        CoreNElementUtils.setAll(values, this.values);
        return this;
    }

    @Override
    public NObjectElementBuilder addParamAt(int index, NElement param) {
        this.params=CoreNElementUtils.addAt(index, param, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder removeParamAt(int index) {
        CoreNElementUtils.removeAt(index, this.params);
        return this;
    }

    @Override
    public NObjectElementBuilder clearParams() {
        if (params != null) {
            params.clear();
        }
        return this;
    }

    @Override
    public NOptional<List<NElement>> params() {
        if (params == null) {
            return NOptional.ofNamedEmpty("params");
        }
        return NOptional.of(Collections.unmodifiableList(params));
    }


    @Override
    public List<NElement> children() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : values) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (Objects.equals(e.key(), s)) {
                    ret.add(e.value());
                }
            }
        }
        return ret;
    }

    @Override
    public NOptional<NElement> get(NElement s) {
        return NOptional.ofNamedSingleton(getAll(s), "property " + s);
    }

    @Override
    public NOptional<NElement> get(String s) {
        return get(NElement.ofString(s));
    }

    @Override
    public NOptional<NElement> getAt(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofNamedEmpty("property at index " + index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder add(String name, NElement value) {
        return add(NElement.ofString(name), CoreNElementUtils.denullOne(value));
    }

    @Override
    public NObjectElementBuilder add(NElement name, NElement value) {
        add(CoreNElementUtils.pair(name, value));
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, NElement value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name, value), values);
        return this;
    }

    @Override
    public NObjectElementBuilder setAt(int i, NElement element) {
        CoreNElementUtils.setAt(i, element, values);
        return this;
    }

    @Override
    public NObjectElementBuilder set(String name, NElement value) {
        return set(CoreNElementUtils.pair(name, value));
    }

    @Override
    public NObjectElementBuilder set(String name, boolean value) {
        return set(CoreNElementUtils.pair(name, value));
    }

    @Override
    public NObjectElementBuilder set(String name, int value) {
        return set(CoreNElementUtils.pair(name, value));
    }

    @Override
    public NObjectElementBuilder set(String name, double value) {
        return set(CoreNElementUtils.pair(name, value));
    }

    @Override
    public NObjectElementBuilder set(String name, String value) {
        return set(CoreNElementUtils.pair(name, value));
    }

    @Override
    public NObjectElementBuilder removeAt(int index) {
        CoreNElementUtils.removeAt(index,values);
        return this;
    }

    @Override
    public NObjectElementBuilder remove(NElement child) {
        CoreNElementUtils.remove(child,values);
        return this;
    }

    @Override
    public NObjectElementBuilder removePair(NElement entryKey) {
        CoreNElementUtils.removePair(entryKey,values);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAll(NElement child) {
        CoreNElementUtils.removeAll(child,values);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAllPairs(NElement name) {
        CoreNElementUtils.removeAllPairs(name,values);
        return this;
    }

    @Override
    public NObjectElementBuilder removePair(String name) {
        CoreNElementUtils.removePair(name,values);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAllPairs(String name) {
        CoreNElementUtils.removeAllPairs(name,values);
        return this;
    }


    @Override
    public NObjectElementBuilder addAll(Map<NElement, NElement> other) {
        CoreNElementUtils.addMap(other,values);
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(List<NElement> other) {
        CoreNElementUtils.addAll(other,values);
        return this;
    }

    @Override
    public NObjectElementBuilder setAll(Map<NElement, NElement> other) {
        values.clear();
        CoreNElementUtils.addMap(other,values);
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, boolean value) {
        return set(name, NElement.ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, int value) {
        return set(name, NElement.ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, double value) {
        return set(name, NElement.ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, String value) {
        return set(name, NElement.ofString(value));
    }

    @Override
    public NObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NObjectElementBuilder clearChildren() {
        values.clear();
        return this;
    }

    @Override
    public NObjectElementBuilder add(NElement entry) {
        CoreNElementUtils.add(entry, this.values);
        return this;
    }

    @Override
    public NObjectElementBuilder addAt(int index, NElement item) {
        CoreNElementUtils.addAt(index, item, this.values);
        return this;
    }

    @Override
    public NObjectElementBuilder set(NPairElement entry) {
        CoreNElementUtils.setPair(entry, values);
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NElement... entries) {
        CoreNElementUtils.addAll(entries, values);
        return this;
    }

    @Override
    public NObjectElementBuilder add(String name, boolean value) {
        return add(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder add(String name, int value) {
        return add(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    @Override
    public NObjectElementBuilder add(String name, double value) {
        return add(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    @Override
    public NObjectElementBuilder add(String name, String value) {
        return add(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    @Override
    public NObjectElementBuilder doWith(Consumer<NObjectElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }


    @Override
    public NObjectElement build() {
        return new DefaultNObjectElement(name, params, values
                , affixes()
                , diagnostics(), metadata()
        );
    }


    @Override
    public NElementType type() {
        if (name != null && params != null) {
            return NElementType.FULL_OBJECT;
        }
        if (name != null) {
            return NElementType.NAMED_OBJECT;
        }
        if (params != null) {
            return NElementType.PARAM_OBJECT;
        }
        return NElementType.OBJECT;
    }

    @Override
    public NObjectElementBuilder addParam(String name, NElement value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, String value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Integer value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Long value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Double value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Boolean value) {
        return addParam(NElement.ofPair(name, value));
    }


    // ------------------------------------------

    @Override
    public NObjectElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElementBuilder) {
            NPairElementBuilder from = (NPairElementBuilder) other;
            add(from.key(), from.value());
            return this;
        }
        if (other instanceof NUpletElementBuilder) {
            NUpletElementBuilder from = (NUpletElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                addParam(from.get(i).get());
            }
            return this;
        }
        if (other instanceof NObjectElementBuilder) {
            NObjectElementBuilder from = (NObjectElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.getAt(i).get());
            }
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addParams(p);
            }
            name(from.name().orNull());
            return this;
        }
        if (other instanceof NArrayElementBuilder) {
            NArrayElementBuilder from = (NArrayElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addParams(p);
            }
            name(from.name().orNull());
            return this;
        }
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElement) {
            NPairElement from = (NPairElement) other;
            add(from.key(), from.value());
            return this;
        }
        if (other instanceof NListContainerElement) {
            NListContainerElement from = (NListContainerElement) other;
            addAll(from.children());
        }
        if (other instanceof NNamedElement) {
            NNamedElement nfrom = (NNamedElement) other;
            name(nfrom.name().orNull());
        }
        if (other instanceof NParametrizedContainerElement) {
            NParametrizedContainerElement from = (NParametrizedContainerElement) other;
            if(from.isParametrized()){
                setParametrized(true);
            }
            addParams(from.params().orNull());
            return this;
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NObjectElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NObjectElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NObjectElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NObjectElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NObjectElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NObjectElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }


    @Override
    public NObjectElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NObjectElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NObjectElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }


    @Override
    public NObjectElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NObjectElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NObjectElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    @Override
    public NObjectElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NObjectElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NObjectElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NObjectElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NObjectElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NObjectElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NObjectElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NObjectElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NObjectElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
