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
    public NArrayElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NArrayElementBuilder copyFrom(NElement other) {
        return (NArrayElementBuilder) super.copyFrom(other);
    }

    @Override
    public NArrayElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
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
    public NArrayElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
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
        if(super.isCustomTree()){
            return true;
        }
        if(params!=null){
            for (NElement value : params) {
                if(value.isCustomTree()){
                    return true;
                }
            }
        }
        if(values!=null){
            for (NElement value : values) {
                if(value.isCustomTree()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NArrayElementBuilder doWith(Consumer<NArrayElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name,name);
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
    public NArrayElementBuilder copyFrom(NArrayElement other) {
        if (other != null) {
            addAnnotations(other.annotations());
            addComments(other.comments());
            if (other.isNamed()) {
                this.name(other.name().orNull());
            }
            this.addParams(other.params().orNull());
            addAll(other.children());
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
        if (name != null && params != null) {
            return NElementType.NAMED_PARAMETRIZED_ARRAY;
        }
        if (name != null) {
            return NElementType.NAMED_ARRAY;
        }
        if (params != null) {
            return NElementType.PARAMETRIZED_ARRAY;
        }
        return NElementType.ARRAY;
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

    @Override
    public NArrayElementBuilder addParam(String name, NElement value) {
        return addParam(NElements.of().ofPair(name,value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, String value) {
        return addParam(NElements.of().ofPair(name,value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Integer value) {
        return addParam(NElements.of().ofPair(name,value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Long value) {
        return addParam(NElements.of().ofPair(name,value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Double value) {
        return addParam(NElements.of().ofPair(name,value));
    }

    @Override
    public NArrayElementBuilder addParam(String name, Boolean value) {
        return addParam(NElements.of().ofPair(name,value));
    }
}
