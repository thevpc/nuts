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
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNArrayElement;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * @author thevpc
 */
public class DefaultNArrayElementBuilder extends AbstractNElementBuilder implements NArrayElementBuilder {

    private final List<NElement> values = new ArrayList<>();
    private List<NElement> params;
    private String name;

    public DefaultNArrayElementBuilder() {
    }

    @Override
    public NArrayElementBuilder doWith(Consumer<NArrayElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, name);
    }

    public NArrayElementBuilder name(String name) {
        this.name = name;
        return this;
    }

    public boolean isParametrized() {
        return params != null;
    }

    public NArrayElementBuilder setParametrized(boolean parametrized) {
        if (parametrized) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addParams(List<NElement> params) {
        this.params=CoreNElementUtils.addAll(params, this.params);
        return this;
    }

    @Override
    public NArrayElementBuilder addParam(NElement param) {
        this.params=CoreNElementUtils.add(param, this.params);
        return this;
    }

    @Override
    public NArrayElementBuilder setParamAt(int index, NElement param) {
        this.params=CoreNElementUtils.setAt(index,param, this.params);
        return this;
    }

    @Override
    public NArrayElementBuilder addParamAt(int index, NElement param) {
        this.params=CoreNElementUtils.addAt(index,param, this.params);
        return this;
    }

    @Override
    public NArrayElementBuilder removeParamAt(int index) {
        CoreNElementUtils.removeAt(index, this.params);
        return this;
    }

    @Override
    public NArrayElementBuilder clearParams() {
        if (this.params != null) {
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
    public List<NElement> items() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NArrayElementBuilder clearChildren() {
        this.values.clear();
        return this;
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofNamedEmpty("element at index " + index);
    }

    @Override
    public NArrayElementBuilder addAll(NElement[] value) {
        if (value != null) {
            for (NElement e : value) {
                add(e);
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(Collection<NElement> value) {
        if (value != null) {
            for (NElement e : value) {
                add(e);
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(NArrayElementBuilder value) {
        if (value == null) {
            add(NElement.ofNull());
        } else {
            for (NElement child : value.items()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder add(NElement e) {
        CoreNElementUtils.add(e, this.values);
        return this;
    }

    @Override
    public NArrayElementBuilder insert(int index, NElement e) {
        CoreNElementUtils.addAt(index,e, this.values);
        return this;
    }

    @Override
    public NArrayElementBuilder setAt(int index, NElement e) {
        CoreNElementUtils.addAt(index,e, this.values);
        return this;
    }

    @Override
    public NArrayElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NArrayElementBuilder remove(int index) {
        CoreNElementUtils.removeAt(index, this.values);
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(String[] value) {
        for (String b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(int[] value) {
        for (int b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(double[] value) {
        for (double b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(long[] value) {
        for (long b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(float[] value) {
        for (float b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(boolean[] value) {
        for (boolean b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(char[] value) {
        for (char b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(byte[] value) {
        for (byte b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder add(int value) {
        return add(NElement.ofInt(value));
    }

    @Override
    public NArrayElementBuilder add(long value) {
        return add(NElement.ofLong(value));
    }

    @Override
    public NArrayElementBuilder add(double value) {
        return add(NElement.ofDouble(value));
    }

    @Override
    public NArrayElementBuilder add(float value) {
        return add(NElement.ofFloat(value));
    }

    @Override
    public NArrayElementBuilder add(byte value) {
        return add(NElement.ofByte(value));
    }

    @Override
    public NArrayElementBuilder add(boolean value) {
        return add(NElement.ofBoolean(value));
    }

    @Override
    public NArrayElementBuilder add(char value) {
        return add(NElement.ofString(String.valueOf(value)));
    }

    @Override
    public NArrayElementBuilder add(Number value) {
        return add(NElement.ofNumber(value));
    }

    @Override
    public NArrayElementBuilder add(String value) {
        return add(NElement.ofString(value));
    }

    @Override
    public NArrayElement build() {
        return new DefaultNArrayElement(name, params, values,
                affixes(), diagnostics(),metadata());
    }


    @Override
    public NElementType type() {
        if (name != null && params != null) {
            return NElementType.FULL_ARRAY;
        }
        if (name != null) {
            return NElementType.NAMED_ARRAY;
        }
        if (params != null) {
            return NElementType.PARAM_ARRAY;
        }
        return NElementType.ARRAY;
    }


    @Override
    public NArrayElementBuilder addParam(String name, NElement value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, String value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Integer value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Long value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Double value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Boolean value) {
        return addParam(NElement.ofPair(name, value));
    }

    @Override
    public NArrayElementBuilder setParams(List<NElement> params) {
        this.params=CoreNElementUtils.setAll(params, this.values);
        return this;
    }

    @Override
    public NArrayElementBuilder setChildren(List<NElement> values) {
        this.values.clear();
        CoreNElementUtils.setAll(values, this.values);
        return this;
    }

    @Override
    public List<NElement> children() {
        return new ArrayList<>(this.values);
    }

    // ------------------------------------------

    @Override
    public NArrayElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElementBuilder) {
            NPairElementBuilder from = (NPairElementBuilder) other;
            add(from.key());
            add(from.value());
            return this;
        }
        if (other instanceof NUpletElementBuilder) {
            NUpletElementBuilder from = (NUpletElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        if (other instanceof NObjectElementBuilder) {
            NObjectElementBuilder from = (NObjectElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.getAt(i).get());
            }
            List<NElement> p = from.params().orNull();
            if(from.isParametrized()){
                setParametrized(true);
            }
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
            if(from.isParametrized()){
                setParametrized(true);
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
    public NArrayElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElementBuilder) {
            NPairElementBuilder from = (NPairElementBuilder) other;
            add(from.key());
            add(from.value());
            return this;
        }
        if (other instanceof NUpletElement) {
            NUpletElement from = (NUpletElement) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        if (other instanceof NObjectElement) {
            NObjectElement from = (NObjectElement) other;
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
        if (other instanceof NArrayElement) {
            NArrayElement from = (NArrayElement) other;
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

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NArrayElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NArrayElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    public NArrayElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }

    @Override
    public NArrayElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NArrayElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NArrayElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NArrayElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NArrayElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    @Override
    public NArrayElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NArrayElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NArrayElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NArrayElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NArrayElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NArrayElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NArrayElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NArrayElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NArrayElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NArrayElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NArrayElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NArrayElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NArrayElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NArrayElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NArrayElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NArrayElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
