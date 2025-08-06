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
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPairElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNUpletElement;
import net.thevpc.nuts.runtime.standalone.elem.item.NElementCommentsImpl;
import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * @author thevpc
 */
public class DefaultNUpletElementBuilder extends AbstractNElementBuilder implements NUpletElementBuilder {

    private List<NElement> params = new ArrayList<>();
    private String name;

    public DefaultNUpletElementBuilder() {
    }
    @Override
    public NUpletElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }
    @Override
    public NUpletElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other, NMapStrategy.ANY);
        return this;
    }

    @Override
    public NUpletElementBuilder copyFrom(NElement other) {
        copyFrom(other, NMapStrategy.ANY);
        return this;
    }

    @Override
    public NUpletElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
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
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addAll(p);
            }
            for (int i = 0; i < from.size(); i++) {
                add(from.getAt(i).get());
            }
            name(from.name().orNull());
            return this;
        }
        if (other instanceof NArrayElementBuilder) {
            NArrayElementBuilder from = (NArrayElementBuilder) other;
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addAll(p);
            }
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            name(from.name().orNull());
            return this;
        }
        return this;
    }

    @Override
    public NUpletElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
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
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addAll(p);
            }
            for (int i = 0; i < from.size(); i++) {
                add(from.getAt(i).get());
            }
            name(from.name().orNull());
            return this;
        }
        if (other instanceof NArrayElement) {
            NArrayElement from = (NArrayElement) other;
            List<NElement> p = from.params().orNull();
            if (p != null) {
                this.addAll(p);
            }
            for (int i = 0; i < from.size(); i++) {
                add(from.get(i).get());
            }
            name(from.name().orNull());
            return this;
        }
        return this;
    }

    @Override
    public boolean isCustomTree() {
        if (super.isCustomTree()) {
            return true;
        }
        if (params != null) {
            for (NElement value : params) {
                if (value.isCustomTree()) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public NUpletElementBuilder doWith(Consumer<NUpletElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, name);
    }

    public NUpletElementBuilder name(String name) {
        this.name = name;
        return this;
    }

    public boolean isParametrized() {
        return params != null;
    }

    public NUpletElementBuilder setParametrized(boolean hasArgs) {
        if (hasArgs) {
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }


    public NUpletElementBuilder addAt(int index, NElement arg) {
        if (arg != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            params.add(index, arg);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder removeAt(int index) {
        if (this.params != null) {
            params.remove(index);
        }
        return this;
    }


    @Override
    public List<NElement> params() {
        return Collections.unmodifiableList(params);
    }

    @Override
    public NUpletElementBuilder setParams(List<NElement> params) {
        this.params.clear();
        if (params != null) {
            this.params.addAll(params.stream().filter(x -> x != null).collect(Collectors.toList()));
        }
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
    public NUpletElementBuilder copyFrom(NUpletElement value) {
        if (value != null) {
            addAnnotations(value.annotations());
            addComments(value.comments());
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
    public NUpletElementBuilder addAll(NElement[] value) {
        if (value != null) {
            for (NElement e : value) {
                add(e);
            }
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(Collection<NElement> value) {
        if (value != null) {
            for (NElement e : value) {
                add(e);
            }
        }
        return this;
    }

    @Override
    public NUpletElementBuilder copyFrom(NUpletElementBuilder value) {
        if (value != null) {
            addAnnotations(value.annotations());
            addComments(value.comments());
            if (value.name().isPresent()) {
                name(value.name().get());
            }
            for (NElement child : value.params()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NUpletElementBuilder add(NElement e) {
        params.add(denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder insert(int index, NElement e) {
        params.add(index, denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder set(int index, NElement e) {
        params.set(index, denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder clear() {
        // should it not clean everything?
        params.clear();
        return this;
    }

    @Override
    public NUpletElementBuilder clearParams() {
        params.clear();
        return this;
    }

    @Override
    public NUpletElementBuilder remove(int index) {
        params.remove(index);
        return this;
    }


    @Override
    public NUpletElementBuilder addAll(String[] value) {
        for (String b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(int[] value) {
        for (int b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(double[] value) {
        for (double b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(long[] value) {
        for (long b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(float[] value) {
        for (float b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(boolean[] value) {
        for (boolean b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(char[] value) {
        for (char b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder addAll(byte[] value) {
        for (byte b : value) {
            add(b);
        }
        return this;
    }

    @Override
    public NUpletElementBuilder add(int value) {
        return add(NElement.ofInt(value));
    }

    @Override
    public NUpletElementBuilder add(long value) {
        return add(NElement.ofLong(value));
    }

    @Override
    public NUpletElementBuilder add(double value) {
        return add(NElement.ofDouble(value));
    }

    @Override
    public NUpletElementBuilder add(float value) {
        return add(NElement.ofFloat(value));
    }

    @Override
    public NUpletElementBuilder add(byte value) {
        return add(NElement.ofByte(value));
    }

    @Override
    public NUpletElementBuilder add(boolean value) {
        return add(NElement.ofBoolean(value));
    }

    @Override
    public NUpletElementBuilder add(char value) {
        return add(NElement.ofString(String.valueOf(value)));
    }

    @Override
    public NUpletElementBuilder add(Number value) {
        return add(NElement.ofNumber(value));
    }

    @Override
    public NUpletElementBuilder add(String value) {
        return add(NElement.ofString(value));
    }

    @Override
    public NUpletElement build() {
        return new DefaultNUpletElement(name, params,
                annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public String toString() {
        return "[" + params().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    private NElement denull(NElement e) {
        if (e == null) {
            return NElement.ofNull();
        }
        return e;
    }

    @Override
    public NElementType type() {
        return name == null ? NElementType.UPLET
                : NElementType.NAMED_UPLET;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NUpletElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NUpletElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NUpletElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NUpletElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NUpletElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NUpletElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NUpletElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NUpletElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NUpletElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NUpletElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NUpletElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NUpletElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NUpletElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NUpletElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NUpletElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NUpletElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NUpletElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NUpletElementBuilder clearComments() {
        super.clearComments();
        return this;
    }


    @Override
    public NUpletElementBuilder add(String name, NElement value) {
        return add(NElement.ofString(name), denull(value));
    }

    @Override
    public NUpletElementBuilder add(NElement name, NElement value) {
        add(pair(denull(name), denull(value)));
        return this;
    }

    @Override
    public NUpletElementBuilder set(NElement name, NElement value) {
        name = denull(name);
        value = denull(value);
        for (int i = 0; i < params.size(); i++) {
            NElement nElement = params.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, name)) {
                    params.set(i, pair(name, value));
                    return this;
                }
            } else if (Objects.equals(nElement, name)) {
                params.set(i, pair(name, value));
                return this;
            }
        }
        add(pair(name, value));
        return this;
    }

    private NPairElement pair(NElement k, NElement v) {
        return new DefaultNPairElement(k, v, new NElementAnnotation[0], new NElementCommentsImpl());
    }

    @Override
    public NUpletElementBuilder set(String name, NElement value) {
        return set(NElement.ofNameOrString(name), denull(value));
    }

    @Override
    public NUpletElementBuilder set(String name, boolean value) {
        return set(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    @Override
    public NUpletElementBuilder set(String name, int value) {
        return set(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    @Override
    public NUpletElementBuilder set(String name, double value) {
        return set(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    @Override
    public NUpletElementBuilder set(String name, String value) {
        return set(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    @Override
    public NUpletElementBuilder set(NElement name, boolean value) {
        return set(name, NElement.ofBoolean(value));
    }

    @Override
    public NUpletElementBuilder set(NElement name, int value) {
        return set(name, NElement.ofInt(value));
    }

    @Override
    public NUpletElementBuilder set(NElement name, double value) {
        return set(name, NElement.ofDouble(value));
    }

    @Override
    public NUpletElementBuilder set(NElement name, String value) {
        return set(name, NElement.ofString(value));
    }

    @Override
    public NUpletElementBuilder set(NPairElement entry) {
        if (entry != null) {
            set(entry.key(), entry.value());
        }
        return this;
    }

    @Override
    public NUpletElementBuilder add(String name, boolean value) {
        return add(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    @Override
    public NUpletElementBuilder add(String name, int value) {
        return add(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    @Override
    public NUpletElementBuilder add(String name, double value) {
        return add(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    @Override
    public NUpletElementBuilder add(String name, String value) {
        return add(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    @Override
    public NUpletElementBuilder addAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }


}
