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

import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNArrayElement;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.DefaultElementMapperStore;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.util.NUnsupportedEnumException;

/**
 * @author thevpc
 */
public class DefaultNElementFactoryService implements NElementFactoryService {


    public DefaultNElementFactoryService() {
    }


    protected Object createObject(NElement o, Type to, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null || o.type() == NElementType.NULL) {
            return DefaultElementMapperStore.F_NULL.createObject(o, to, context);
        }
        if (to == null) {
            NElementMapper f = context.getMapper(o, defaultOnly);
            if (f == null) {
                throw new NUnsupportedEnumException(o.type());
            }
            return f.createObject(o, to, context);
        }
        NElementMapper f = context.getMapper(to, defaultOnly);
        return f.createObject(o, to, context);
    }

    @Override
    public Object createObject(NElement o, Type to, NElementFactoryContext context) {
        return createObject(o, to, context, false);
    }

    @Override
    public Object defaultCreateObject(NElement o, Type to, NElementFactoryContext context) {
        return createObject(o, to, context, true);
    }

    protected Object destruct(Object o, Type expectedType, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return null;
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.isIndestructibleObject(o)) {
            return o;
        }
        return context.getMapper(expectedType, defaultOnly).destruct(o, expectedType, context);
    }

    @Override
    public Object destruct(Object o, Type expectedType, NElementFactoryContext context) {
        return destruct(o, expectedType, context, false);
    }

    @Override
    public Object defaultDestruct(Object o, Type expectedType, NElementFactoryContext context) {
        return destruct(o, expectedType, context, true);
    }

    protected NElement createElement(Object o, Type expectedType, NElementFactoryContext context, boolean defaultOnly) {
        if (o == null) {
            return NElement.ofNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (context.isIndestructibleObject(o)) {
            return createUndestructableElement(o, expectedType, context);
        }
        NElementMapper mapper = context.getMapper(expectedType, defaultOnly);
        return mapper.createElement(o, expectedType, context);
    }

    protected NElement createUndestructableElement(Object o, Type expectedType, NElementFactoryContext context) {
        if (o == null) {
            return NElement.ofNull();
        }
        if (expectedType == null) {
            expectedType = o.getClass();
        }
        if (o instanceof NElement) {
            return (NElement) o;
        }
        if (o instanceof NToElement) {
            return ((NToElement) o).toElement();
        }
        if (expectedType instanceof Class<?>) {
            Class cls = (Class) expectedType;
            switch (cls.getName()) {
                case "boolean":
                case "byte":
                case "char":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                case "java.lang.Character":
                case "java.lang.String":
                case "java.lang.StringBuilder":
                case "java.lang.Boolean":
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.math.BigDecimal":
                case "java.math.BigInteger":
                case "java.util.Date":
                case "java.sql.Time":
                case "java.time.Duration":
                    return context.getMapper(expectedType, true).createElement(o, expectedType, context);
            }
            if (Temporal.class.isAssignableFrom(cls)) {
                return context.getMapper(expectedType, true).createElement(o, expectedType, context);
            }
            if (java.util.Date.class.isAssignableFrom(cls)) {
                return context.getMapper(expectedType, true).createElement(o, expectedType, context);
            }
        }
        return NElement.ofCustom(o);

    }

    @Override
    public NElement createElement(Object o, Type expectedType, NElementFactoryContext context) {
        return createElement(o, expectedType, context, false);
    }

    @Override
    public NElement defaultCreateElement(Object o, Type expectedType, NElementFactoryContext context) {
        return createElement(o, expectedType, context, true);
    }

    public static List<Object> _destructArray1(Object array, NElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<Object> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.destruct(Array.get(array, i), null));
            }
            return preloaded;
        } else {
            return Arrays.stream((Object[]) array).map(x -> context.destruct(x, null)).collect(Collectors.toList());
        }
    }

    public static NArrayElement _createArray1(Object array, NElementFactoryContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            List<NElement> preloaded = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                preloaded.add(context.createElement(Array.get(array, i)));
            }
            return new DefaultNArrayElement(null, null, preloaded);
        } else {
            return new DefaultNArrayElement(null, null,
                    Arrays.stream((Object[]) array).map(x -> context.createElement(x)).collect(Collectors.toList())
            );
        }
    }

}
