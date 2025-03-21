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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNObjectElementBuilder  extends AbstractNElementBuilder implements NObjectElementBuilder {

    private final List<NElement> values = new ArrayList<>();

    private String name;
    private List<NElement> params;

    public DefaultNObjectElementBuilder() {
    }

    public String getName() {
        return name;
    }

    public NObjectElementBuilder setName(String name) {
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
    public NObjectElementBuilder addArgs(List<NElement> args) {
        if (args != null) {
            for (NElement a : args) {
                if (a != null) {
                    this.params.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addArg(NElement arg) {
        if (arg != null) {
            this.params.add(arg);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addArgAt(int index, NElement arg) {
        if (arg != null) {
            params.add(index, arg);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeArgAt(int index) {
        params.remove(index);
        return this;
    }

    @Override
    public NObjectElementBuilder clearArgs() {
        params.clear();
        return this;
    }

    @Override
    public List<NElement> getParams() {
        return Collections.unmodifiableList(params);
    }


    @Override
    public List<NElement> children() {
        return values;
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
        return get(_elements().ofString(s));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder add(String name, NElement value) {
        return add(_elements().ofString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder add(NElement name, NElement value) {
        values.add(pair(denull(name), denull(value)));
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
        values.add(pair(name, value));
        return this;
    }

    private NPairElement pair(NElement k,NElement v){
        return new DefaultNPairElement(k, v, new NElementAnnotation[0],new NElementCommentsImpl());
    }

    @Override
    public NObjectElementBuilder set(String name, NElement value) {
        return set(_elements().ofString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder set(String name, boolean value) {
        return set(_elements().ofString(name), _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(String name, int value) {
        return set(_elements().ofString(name), _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(String name, double value) {
        return set(_elements().ofString(name), _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(String name, String value) {
        return set(_elements().ofString(name), _elements().ofString(value));
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
        return remove(_elements().ofString(name));
    }

    @Override
    public NObjectElementBuilder removeAll(String name) {
        return removeAll(_elements().ofString(name));
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
    public NObjectElementBuilder addAll(List<NPairElement> other) {
        if (other != null) {
            for (NPairElement e : other) {
                add(e.key(), e.value());
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
        return set(name, _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, int value) {
        return set(name, _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, double value) {
        return set(name, _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, String value) {
        return set(name, _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NObjectElementBuilder set(NObjectElement other) {
        clear();
        this.addAll(other);
        return this;
    }

    @Override
    public NObjectElementBuilder setAll(NObjectElementBuilder other) {
        clear();
        if (other != null) {
            for (NElement entry : other.children()) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NObjectElement other) {
        if (other != null) {
            for (NElement entry : other) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NObjectElementBuilder other) {
        if (other != null) {
            for (NElement child : other.children()) {
                add(child);
            }
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
        return add(_elements().ofString(name), _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder add(String name, int value) {
        return add(_elements().ofString(name), _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder add(String name, double value) {
        return add(_elements().ofString(name), _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder add(String name, String value) {
        return add(_elements().ofString(name), _elements().ofString(value));
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
                ,annotations().toArray(new NElementAnnotation[0])
                ,comments()
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
            return _elements().ofNull();
        }
        return e;
    }

    private NElements _elements() {
        return NElements.of();
    }

    @Override
    public NElementType type() {
        return name == null && params == null ? NElementType.OBJECT
                : name == null && params != null ? NElementType.PARAMETRIZED_OBJECT
                : name != null && params == null ? NElementType.NAMED_OBJECT
                : NElementType.NAMED_PARAMETRIZED_OBJECT;
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

}
