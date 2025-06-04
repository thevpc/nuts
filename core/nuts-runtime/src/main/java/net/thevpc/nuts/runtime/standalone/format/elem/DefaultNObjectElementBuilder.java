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
package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNObjectElementBuilder extends AbstractNElementBuilder implements NObjectElementBuilder {

    private final List<NElement> values = new ArrayList<>();

    private String name;
    private List<NElement> params;

    public DefaultNObjectElementBuilder() {
    }

    @Override
    public NObjectElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other, NMapStrategy.ANY);
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElement other) {
        copyFrom(other, NMapStrategy.ANY);
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
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
        if (other instanceof NMatrixElementBuilder) {
            NMatrixElementBuilder from = (NMatrixElementBuilder) other;
            for (NArrayElement row : from.rows()) {
                add(row);
            }
            name(from.name().orNull());
            return this;
        }
        return this;
    }

    @Override
    public NObjectElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
        if (other instanceof NPairElement) {
            NPairElement from = (NPairElement) other;
            add(from.key(), from.value());
            return this;
        }
        if (other instanceof NUpletElement) {
            NUpletElement from = (NUpletElement) other;
            for (int i = 0; i < from.size(); i++) {
                addParam(from.get(i).get());
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
        if (other instanceof NMatrixElement) {
            NMatrixElement from = (NMatrixElement) other;
            for (NArrayElement row : from.rows()) {
                add(row);
            }
            name(from.name());
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
        if (values != null) {
            for (NElement value : values) {
                if (value.isCustomTree()) {
                    return true;
                }
            }
        }
        return false;
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name,name);
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
        if (params != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            for (NElement a : params) {
                if (a != null) {
                    this.params.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addParam(NElement param) {
        if (param != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            this.params.add(param);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addParamAt(int index, NElement param) {
        if (param != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            params.add(index, param);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeParamAt(int index) {
        if (params != null) {
            params.remove(index);
        }
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
        return add(NElement.ofString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder add(NElement name, NElement value) {
        add(pair(denull(name), denull(value)));
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, NElement value) {
        name = denull(name);
        value = denull(value);
        for (int i = 0; i < values.size(); i++) {
            NElement nElement = values.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, name)) {
                    values.set(i, pair(name, value));
                    return this;
                }
            } else if (Objects.equals(nElement, name)) {
                values.set(i, pair(name, value));
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
    public NObjectElementBuilder set(String name, NElement value) {
        return set(NElement.ofNameOrString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder set(String name, boolean value) {
        return set(NElement.ofNameOrString(name), NElement.ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(String name, int value) {
        return set(NElement.ofNameOrString(name), NElement.ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(String name, double value) {
        return set(NElement.ofNameOrString(name), NElement.ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(String name, String value) {
        return set(NElement.ofNameOrString(name), NElement.ofString(value));
    }

    @Override
    public NObjectElementBuilder remove(NElement name) {
        name = denull(name);
        for (int i = 0; i < values.size(); i++) {
            NElement nElement = values.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, name)) {
                    values.remove(i);
                    return this;
                }
            } else if (Objects.equals(nElement, name)) {
                values.remove(i);
                return this;
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeAll(NElement name) {
        name = denull(name);
        for (int i = values.size() - 1; i >= 0; i--) {
            NElement nElement = values.get(i);
            if (nElement instanceof NPairElement) {
                NElement k = ((NPairElement) nElement).key();
                if (Objects.equals(k, name)) {
                    values.remove(i);
                }
            } else if (Objects.equals(nElement, name)) {
                values.remove(i);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder remove(String name) {
        return remove(NElement.ofString(name));
    }

    @Override
    public NObjectElementBuilder removeAll(String name) {
        return removeAll(NElement.ofString(name));
    }


    @Override
    public NObjectElementBuilder addAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(List<NElement> other) {
        if (other != null) {
            for (NElement e : other) {
                if (e != null) {
                    add(e);
                }
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder setAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                set(e.getKey(), e.getValue());
            }
        }
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
    public NObjectElementBuilder copyFrom(NObjectElement other) {
        if (other != null) {
            addAnnotations(other.annotations());
            addComments(other.comments());
            if (other.isNamed()) {
                this.name(other.name().get());
            }
            this.addParams(other.params().orNull());
            this.addAll(other.children());
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NElement entry) {
        if (entry != null) {
            values.add(entry);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder set(NPairElement entry) {
        if (entry != null) {
            set(entry.key(), entry.value());
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NElement... entries) {
        if (entries != null) {
            for (NElement entry : entries) {
                add(entry);
            }
        }
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
    public NObjectElementBuilder addAll(NObjectElementBuilder other) {
        if (other != null) {
            for (NElement entry : other.build()) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElement build() {
        return new DefaultNObjectElement(name, params, values
                , annotations().toArray(new NElementAnnotation[0])
                , comments()
        );
    }

    @Override
    public String toString() {
        return "{" + children().stream().map(x
                -> x.toString()
        ).collect(Collectors.joining(", ")) + "}";
    }

    private NElement denull(NElement e) {
        if (e == null) {
            return NElement.ofNull();
        }
        return e;
    }

    @Override
    public NElementType type() {
        if (name != null && params != null) {
            return NElementType.NAMED_PARAMETRIZED_OBJECT;
        }
        if (name != null) {
            return NElementType.NAMED_OBJECT;
        }
        if (params != null) {
            return NElementType.PARAMETRIZED_OBJECT;
        }
        return NElementType.OBJECT;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NObjectElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NObjectElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
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
    public NObjectElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NObjectElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NObjectElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NObjectElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NObjectElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

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
    public NObjectElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NObjectElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NObjectElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NObjectElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NObjectElementBuilder addParam(String name, NElement value) {
        return addParam(NElement.ofPair(name,value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, String value) {
        return addParam(NElement.ofPair(name,value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Integer value) {
        return addParam(NElement.ofPair(name,value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Long value) {
        return addParam(NElement.ofPair(name,value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Double value) {
        return addParam(NElement.ofPair(name,value));
    }

    @Override
    public NObjectElementBuilder addParam(String name, Boolean value) {
        return addParam(NElement.ofPair(name,value));
    }
}
