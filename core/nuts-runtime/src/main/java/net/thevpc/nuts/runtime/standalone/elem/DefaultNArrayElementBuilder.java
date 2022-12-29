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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author thevpc
 */
public class DefaultNArrayElementBuilder implements NArrayElementBuilder {

    private final List<NElement> values = new ArrayList<>();
    private transient NSession session;

    public DefaultNArrayElementBuilder(NSession session) {
        this.session = session;
        if(session==null){
            throw new NullPointerException();
        }
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
        if(value!=null) {
            for (NElement e : value) {
                add(e);
            }
        }
        return this;
    }

    @Override
    public NArrayElementBuilder addAll(Collection<NElement> value) {
        if(value!=null) {
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
        return new DefaultNArrayElement(values,session);
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
        return NElements.of(session).setSession(session);
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
