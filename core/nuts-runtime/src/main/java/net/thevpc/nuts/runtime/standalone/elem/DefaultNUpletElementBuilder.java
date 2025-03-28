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
public class DefaultNUpletElementBuilder  extends AbstractNElementBuilder implements NUpletElementBuilder {

    private final List<NElement> values = new ArrayList<>();
    private List<NElement> params;
    private String name;

    public DefaultNUpletElementBuilder() {
    }

    public String name() {
        return name;
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
    public NUpletElementBuilder copyFrom(NUpletElement value) {
        if (value != null) {
            addAnnotations(value.annotations());
            addComments(value.comments());
            if (value.name() != null) {
                name(value.name());
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
            if (value.name() != null) {
                name(value.name());
            }
            for (NElement child : value.items()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NUpletElementBuilder add(NElement e) {
        if (e == null) {
            throw new NullPointerException();
        }
        values.add(denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder insert(int index, NElement e) {
        values.add(index, denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder set(int index, NElement e) {
        values.set(index, denull(e));
        return this;
    }

    @Override
    public NUpletElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NUpletElementBuilder remove(int index) {
        values.remove(index);
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
        return add(_elements().ofInt(value));
    }

    @Override
    public NUpletElementBuilder add(long value) {
        return add(_elements().ofLong(value));
    }

    @Override
    public NUpletElementBuilder add(double value) {
        return add(_elements().ofDouble(value));
    }

    @Override
    public NUpletElementBuilder add(float value) {
        return add(_elements().ofFloat(value));
    }

    @Override
    public NUpletElementBuilder add(byte value) {
        return add(_elements().ofByte(value));
    }

    @Override
    public NUpletElementBuilder add(boolean value) {
        return add(_elements().ofBoolean(value));
    }

    @Override
    public NUpletElementBuilder add(char value) {
        return add(_elements().ofString(String.valueOf(value)));
    }

    @Override
    public NUpletElementBuilder add(Number value) {
        return add(_elements().ofNumber(value));
    }

    @Override
    public NUpletElementBuilder add(String value) {
        return add(_elements().ofString(value));
    }

    @Override
    public NUpletElement build() {
        return new DefaultNUpletElement(name, values,
                annotations().toArray(new NElementAnnotation[0]),comments());
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

}
