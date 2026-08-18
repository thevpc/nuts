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
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNTupleElement;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Consumer;


/**
 * @author thevpc
 */
public class DefaultNTupleElementBuilder extends AbstractNElementBuilder implements NTupleElementBuilder {

    private List<NElement> params = new ArrayList<>();
    private String name;

    public DefaultNTupleElementBuilder() {
    }

    @Override
    public NTupleElementBuilder doWith(Consumer<NTupleElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder remove(String child) {
        CoreNElementUtils.removePairByKey(child,params);
        return this;
    }


    public NOptional<String> name() {
        return NOptional.ofNamed(name, name);
    }

    public NTupleElementBuilder name(String name) {
        this.name = name;
        return this;
    }


    public NTupleElementBuilder addAt(int index, NElement arg) {
        CoreNElementUtils.addAt(index, arg,params);
        return this;
    }

    @Override
    public NTupleElementBuilder removeAt(int index) {
        CoreNElementUtils.removeAt(index,params);
        return this;
    }


    @Override
    public List<NElement> params() {
        return Collections.unmodifiableList(params);
    }

    @Override
    public NTupleElementBuilder setParams(List<NElement> params) {
        this.params.clear();
        CoreNElementUtils.setAll(params,this.params);
        return this;
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < params.size()) {
            return NOptional.of(params.get(index));
        }
        return NOptional.ofNamedEmpty("element at index " + index);
    }

    @Override
    public NTupleElementBuilder copyFrom(NTupleElement value) {
        if (value != null) {
            this.addAffixes(value.affixes());
            if (value.isNamed()) {
                name(value.name().get());
            }
            for (NElement child : value.children()) {
                add(child);
            }
        }
        return this;
    }


    @Override
    public NTupleElementBuilder addAll(NElement[] value) {
        CoreNElementUtils.addAll(value,this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(Collection<NElement> value) {
        CoreNElementUtils.addAll(value,this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder add(NElement value) {
        CoreNElementUtils.add(value,this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder setAt(int index, NElement element) {
        CoreNElementUtils.setAt(index,element,this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder clear() {
        // should it not clean everything?
        params.clear();
        return this;
    }

    @Override
    public NTupleElementBuilder clearParams() {
        params.clear();
        return this;
    }

    @Override
    public NTupleElementBuilder remove(int index) {
        CoreNElementUtils.removeAt(index,this.params);
        params.remove(index);
        return this;
    }


    @Override
    public NTupleElementBuilder addAll(String[] value) {
        for (String b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(int[] value) {
        for (int b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(double[] value) {
        for (double b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(long[] value) {
        for (long b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(float[] value) {
        for (float b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(boolean[] value) {
        for (boolean b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(char[] value) {
        for (char b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder addAll(byte[] value) {
        for (byte b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NTupleElementBuilder add(Integer value) {
        return add(NElement.ofInt(value));
    }

    @Override
    public NTupleElementBuilder add(Long value) {
        return add(NElement.ofLong(value));
    }

    @Override
    public NTupleElementBuilder add(Double value) {
        return add(NElement.ofDouble(value));
    }

    @Override
    public NTupleElementBuilder add(Float value) {
        return add(NElement.ofFloat(value));
    }

    @Override
    public NTupleElementBuilder add(Byte value) {
        return add(NElement.ofByte(value));
    }

    @Override
    public NTupleElementBuilder add(Boolean value) {
        return add(NElement.ofBoolean(value));
    }

    @Override
    public NTupleElementBuilder add(Character value) {
        return add(NElement.ofString(String.valueOf(value)));
    }

    @Override
    public NTupleElementBuilder add(Number value) {
        return add(NElement.ofNumber(value));
    }

    @Override
    public NTupleElementBuilder add(String value) {
        return add(NElement.ofString(value));
    }

    @Override
    public NTupleElement build() {
        return new DefaultNTupleElement(name, params,
                affixes(), diagnostics(),metadata());
    }

    @Override
    public NElementType type() {
        return name == null ? NElementType.TUPLE
                : NElementType.NAMED_TUPLE;
    }

    @Override
    public NTupleElementBuilder add(String name, NElement value) {
        CoreNElementUtils.add(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder add(String name, Number value) {
        CoreNElementUtils.add(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder add(NElement name, NElement value) {
        CoreNElementUtils.add(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, NElement value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, NElement value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Boolean value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Integer value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Double value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, String value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Boolean value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Integer value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Character value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Byte value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Short value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Long value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Long value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Float value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Short value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Byte value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(String name, Character value) {
        CoreNElementUtils.setPair(CoreNElementUtils.pair(name,value),this.params);
        return this;
    }

    @Override
    public NTupleElementBuilder set(NElement name, Double value) {
        return set(name, NElement.ofDouble(value));
    }

    @Override
    public NTupleElementBuilder set(NElement name, String value) {
        return set(name, NElement.ofString(value));
    }

    @Override
    public NTupleElementBuilder set(NPairElement entry) {
        if (entry != null) {
            set(entry.key(), entry.value());
        }
        return this;
    }

    @Override
    public NTupleElementBuilder add(String name, Boolean value) {
        return add(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Character value) {
        return add(NElement.ofNameOrString(name), NElement.ofChar(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Byte value) {
        return add(NElement.ofNameOrString(name), NElement.ofByte(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Short value) {
        return add(NElement.ofNameOrString(name), NElement.ofShort(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Long value) {
        return add(NElement.ofNameOrString(name), NElement.ofLong(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Float value) {
        return add(NElement.ofNameOrString(name), NElement.ofFloat(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Integer value) {
        return add(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    @Override
    public NTupleElementBuilder add(String name, Double value) {
        return add(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    @Override
    public NTupleElementBuilder add(String name, String value) {
        return add(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    @Override
    public NTupleElementBuilder addAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    // ------------------------------------------

    @Override
    public NTupleElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NTupleElementBuilder) {
            NTupleElementBuilder b = (NTupleElementBuilder) other;
            if (b.name().isPresent()) {
                name(b.name().get());
            }
            for (NElement child : b.params()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NTupleElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NTupleElement) {
            NTupleElement b = (NTupleElement) other;
            if (b.name().isPresent()) {
                name(b.name().get());
            }
            for (NElement child : b.params()) {
                add(child);
            }
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NTupleElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NTupleElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NTupleElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NTupleElementBuilder addAffixAt(int index, NBoundAffix affix) {
        super.addAffixAt(index, affix);
        return this;
    }

    @Override
    public NTupleElementBuilder removeAffix(int affix) {
        super.removeAffix(affix);
        return this;
    }

    @Override
    public NTupleElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NTupleElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }


    @Override
    public NTupleElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NTupleElementBuilder setAffixAt(int index, NBoundAffix affix) {
        super.setAffixAt(index, affix);
        return this;
    }

    @Override
    public NTupleElementBuilder setAffixes(List<NBoundAffix> affixes) {
        super.setAffixes(affixes);
        return this;
    }

    public NTupleElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }


    @Override
    public NTupleElementBuilder addAffixAt(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffixAt(index, affix, anchor);
        return this;
    }

    @Override
    public NTupleElementBuilder setAffixAt(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffixAt(index, affix, anchor);
        return this;
    }

    @Override
    public NTupleElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NTupleElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NTupleElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NTupleElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NTupleElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NTupleElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NTupleElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NTupleElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NTupleElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NTupleElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NTupleElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NTupleElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NTupleElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NTupleElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
