/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
public class DefaultNArrayElementBuilder implements NArrayElementBuilder {

    private final List<NElement> values = new ArrayList<>();
    private final List<NElementAnnotation> annotations = new ArrayList<>();
    private List<NElement> args;
    private String name;

    public DefaultNArrayElementBuilder() {
    }

    public String getName() {
        return name;
    }

    public NArrayElementBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isWithArgs() {
        return args != null;
    }

    public NArrayElementBuilder setWithArgs(boolean hasArgs) {
        if (hasArgs) {
            if (args == null) {
                args = new ArrayList<>();
            }
        } else {
            args = null;
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addArgs(List<NElement> args) {
        if (args != null) {
            for (NElement a : args) {
                if (a != null) {
                    if (this.args == null) {
                        this.args = new ArrayList<>();
                    }
                    this.args.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addArg(NElement arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addArgAt(int index, NElement arg) {
        if (arg != null) {
            if (this.args == null) {
                this.args = new ArrayList<>();
            }
            args.add(index, arg);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder removeArgAt(int index) {
        if (this.args != null) {
            args.remove(index);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder clearArgs() {
        if (this.args != null) {
            args.clear();
        }
        return this;
    }

    @Override
    public List<NElement> getArgs() {
        return Collections.unmodifiableList(args);
    }

    @Override
    public NArrayElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NArrayElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NArrayElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
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
    public NArrayElementBuilder addAll(NArrayElement value) {
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
    public NArrayElementBuilder set(NArrayElementBuilder other) {
        clear();
        addAll(other);
        return this;
    }

    @Override
    public NArrayElementBuilder set(NArrayElement other) {
        clear();
        addAll(other);
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
        return new DefaultNArrayElement(name, args, values,
                annotations.toArray(new NElementAnnotation[0]));
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
        return NElementType.ARRAY;
    }
}
