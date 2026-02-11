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
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNFragmentElement;
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
public class DefaultNFragmentElementBuilder extends AbstractNElementBuilder implements NFragmentElementBuilder {

    private final List<NElement> values = new ArrayList<>();

    public DefaultNFragmentElementBuilder() {
    }

    @Override
    public NFragmentElementBuilder doWith(Consumer<NFragmentElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
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
    public NFragmentElementBuilder clearChildren() {
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
    public NFragmentElementBuilder addAll(NElement[] value) {
        CoreNElementUtils.addAll(value,values);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(Collection<NElement> value) {
        CoreNElementUtils.addAll(value,values);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(NFragmentElementBuilder value) {
        if (value == null) {
            add(NElement.ofNull());
        } else {
            CoreNElementUtils.addAll(value.items(),values);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder add(NElement e) {
        CoreNElementUtils.add(e,values);
        return this;
    }

    @Override
    public NFragmentElementBuilder insert(int index, NElement e) {
        CoreNElementUtils.addAt(index,e,values);
        return this;
    }

    @Override
    public NFragmentElementBuilder setAt(int index, NElement e) {
        CoreNElementUtils.setAt(index,e,values);
        return this;
    }

    @Override
    public NFragmentElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NFragmentElementBuilder remove(int index) {
        values.remove(index);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(String[] value) {
        for (String b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(int[] value) {
        for (int b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(double[] value) {
        for (double b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(long[] value) {
        for (long b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(float[] value) {
        for (float b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(boolean[] value) {
        for (boolean b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(char[] value) {
        for (char b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder addAll(byte[] value) {
        for (byte b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder add(int value) {
        return add(NElement.ofInt(value));
    }

    @Override
    public NFragmentElementBuilder add(long value) {
        return add(NElement.ofLong(value));
    }

    @Override
    public NFragmentElementBuilder add(double value) {
        return add(NElement.ofDouble(value));
    }

    @Override
    public NFragmentElementBuilder add(float value) {
        return add(NElement.ofFloat(value));
    }

    @Override
    public NFragmentElementBuilder add(byte value) {
        return add(NElement.ofByte(value));
    }

    @Override
    public NFragmentElementBuilder add(boolean value) {
        return add(NElement.ofBoolean(value));
    }

    @Override
    public NFragmentElementBuilder add(char value) {
        return add(NElement.ofString(String.valueOf(value)));
    }

    @Override
    public NFragmentElementBuilder add(Number value) {
        return add(NElement.ofNumber(value));
    }

    @Override
    public NFragmentElementBuilder add(String value) {
        return add(NElement.ofString(value));
    }

    @Override
    public NFragmentElement build() {
        return new DefaultNFragmentElement(values,
                affixes(), diagnostics(),metadata());
    }

    @Override
    public NElementType type() {
        return NElementType.FRAGMENT;
    }


    @Override
    public NFragmentElementBuilder setChildren(List<NElement> values) {
        CoreNElementUtils.setAll(values, this.values);
        return this;
    }

    @Override
    public List<NElement> children() {
        return new ArrayList<>(this.values);
    }

    // ------------------------------------------

    @Override
    public NFragmentElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
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
            return this;
        }
        if (other instanceof NArrayElementBuilder) {
            NArrayElementBuilder from = (NArrayElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        if (other instanceof NFragmentElementBuilder) {
            NFragmentElementBuilder from = (NFragmentElementBuilder) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        return this;
    }

    @Override
    public NFragmentElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
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
            return this;
        }
        if (other instanceof NArrayElement) {
            NArrayElement from = (NArrayElement) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        if (other instanceof NFragmentElement) {
            NFragmentElement from = (NFragmentElement) other;
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            return this;
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NFragmentElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    public NFragmentElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }

    @Override
    public NFragmentElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NFragmentElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NFragmentElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NFragmentElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NFragmentElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NFragmentElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NFragmentElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NFragmentElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NFragmentElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NFragmentElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NFragmentElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NFragmentElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NFragmentElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NFragmentElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NFragmentElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NFragmentElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NFragmentElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NFragmentElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NFragmentElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
