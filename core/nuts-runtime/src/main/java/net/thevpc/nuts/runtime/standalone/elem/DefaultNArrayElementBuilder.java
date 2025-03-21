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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    public String name() {
        return name;
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
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addParams(List<NElement> params) {
        if (params != null) {
            for (NElement a : params) {
                if (a != null) {
                    if (this.params == null) {
                        this.params = new ArrayList<>();
                    }
                    this.params.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addParam(NElement param) {
        if (param != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            this.params.add(param);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addParamAt(int index, NElement param) {
        if (param != null) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }
            params.add(index, param);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder removeParamAt(int index) {
        if (this.params != null) {
            params.remove(index);
        }
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
    public List<NElement> params() {
        return Collections.unmodifiableList(params);
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
    public NElement get(int index) {
        return values.get(index);
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
            add(_elements().ofNull());
        } else {
            for (NElement child : value.items()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder add(NElement e) {
        if (e == null) {
            throw new NullPointerException();
        }
        values.add(denull(e));
        return this;
    }

    @Override
    public NArrayElementBuilder insert(int index, NElement e) {
        values.add(index, denull(e));
        return this;
    }

    @Override
    public NArrayElementBuilder set(int index, NElement e) {
        values.set(index, denull(e));
        return this;
    }

    @Override
    public NArrayElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NArrayElementBuilder remove(int index) {
        values.remove(index);
        return this;
    }

    @Override
    public NArrayElementBuilder copyFrom(NArrayElementBuilder other) {
        if (other != null) {
            addAnnotations(other.annotations());
            addComments(other.comments());
            if (other.name() != null) {
                this.name(other.name());
            }
            if (other.params() != null) {
                this.addParams(other.params());
            }
            addAll(other.items());
        }
        return this;
    }

    @Override
    public NArrayElementBuilder copyFrom(NArrayElement other) {
        if (other != null) {
            addAnnotations(other.annotations());
            addComments(other.comments());
            if (other.name() != null) {
                this.name(other.name());
            }
            if (other.params() != null) {
                this.addParams(other.params());
            }
            addAll(other.items());
        }
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
        return add(_elements().ofInt(value));
    }

    @Override
    public NArrayElementBuilder add(long value) {
        return add(_elements().ofLong(value));
    }

    @Override
    public NArrayElementBuilder add(double value) {
        return add(_elements().ofDouble(value));
    }

    @Override
    public NArrayElementBuilder add(float value) {
        return add(_elements().ofFloat(value));
    }

    @Override
    public NArrayElementBuilder add(byte value) {
        return add(_elements().ofByte(value));
    }

    @Override
    public NArrayElementBuilder add(boolean value) {
        return add(_elements().ofBoolean(value));
    }

    @Override
    public NArrayElementBuilder add(char value) {
        return add(_elements().ofString(String.valueOf(value)));
    }

    @Override
    public NArrayElementBuilder add(Number value) {
        return add(_elements().ofNumber(value));
    }

    @Override
    public NArrayElementBuilder add(String value) {
        return add(_elements().ofString(value));
    }

    @Override
    public NArrayElement build() {
        return new DefaultNArrayElement(name, params, values,
                annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public String toString() {
        return "[" + items().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
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
//
//    private NutsPrimitiveElementBuilder _primitive() {
//        return _elements().forPrimitive();
//    }


    @Override
    public NElementType type() {
        return name == null && params == null ? NElementType.ARRAY
                : name == null && params != null ? NElementType.PARAMETRIZED_ARRAY
                : name != null && params == null ? NElementType.NAMED_ARRAY
                : NElementType.NAMED_PARAMETRIZED_ARRAY;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NArrayElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NArrayElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
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
    public NArrayElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NArrayElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NArrayElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NArrayElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NArrayElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NArrayElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NArrayElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NArrayElementBuilder clearComments() {
        super.clearComments();
        return this;
    }
}
