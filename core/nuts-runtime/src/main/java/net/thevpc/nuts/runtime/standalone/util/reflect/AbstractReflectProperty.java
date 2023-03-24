/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.util.NReflectProperty;
import net.thevpc.nuts.util.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.util.NReflectType;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public abstract class AbstractReflectProperty implements NReflectProperty {

    private String name;
    private Object cleanInstanceValue;
    private Type propertyType;
    private NReflectType type;
    private NReflectPropertyDefaultValueStrategy defaultValueStrategy;

    protected final void init(String name, NReflectType type, Object cleanInstance, Type propertyType, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        this.name = name;
        this.cleanInstanceValue = cleanInstance == null ? ReflectUtils.getDefaultValue(propertyType) : read(cleanInstance);
        this.type = type;
        NReflectType nReflectType = type.getRepository().getType(propertyType)
                .replaceVars(t -> type.getActualTypeArgument(t).orElse(t));
        this.defaultValueStrategy = defaultValueStrategy;
        this.propertyType = nReflectType.getJavaType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NReflectType getType() {
        return type;
    }

    @Override
    public Type getPropertyType() {
        return propertyType;
    }

    @Override
    public NReflectPropertyDefaultValueStrategy getDefaultValueStrategy() {
        return defaultValueStrategy;
    }

    @Override
    public boolean isDefaultValue(Object value, NReflectPropertyDefaultValueStrategy strategy) {
        if (strategy == null) {
            strategy = getDefaultValueStrategy();
        }
        switch (strategy) {
            case NO_DEFAULT: {
                return false;
            }
            case PROPERTY_DEFAULT: {
                if (value == null) {
                    return true; //null is always default!!
                }
                if (cleanInstanceValue != null && cleanInstanceValue.getClass().isArray()) {
                    if (Array.getLength(cleanInstanceValue) == 0) {
                        //this is a simple yet recurrent use case!
                        return value.getClass().isArray() && Array.getLength(value) == 0;
                    }
                    if (value.getClass().isArray()) {
                        Class<?> e = cleanInstanceValue.getClass().getComponentType();
                        Class<?> f = value.getClass().getComponentType();
                        if (e.isPrimitive()) {
                            if (f.isPrimitive() && e.equals(f)) {
                                switch (e.getName()) {
                                    case "boolean":
                                        return Arrays.equals((boolean[]) cleanInstanceValue, (boolean[]) value);
                                    case "byte":
                                        return Arrays.equals((byte[]) cleanInstanceValue, (byte[]) value);
                                    case "char":
                                        return Arrays.equals((char[]) cleanInstanceValue, (char[]) value);
                                    case "short":
                                        return Arrays.equals((short[]) cleanInstanceValue, (short[]) value);
                                    case "int":
                                        return Arrays.equals((int[]) cleanInstanceValue, (int[]) value);
                                    case "long":
                                        return Arrays.equals((long[]) cleanInstanceValue, (long[]) value);
                                    case "float":
                                        return Arrays.equals((float[]) cleanInstanceValue, (float[]) value);
                                    case "double":
                                        return Arrays.equals((double[]) cleanInstanceValue, (double[]) value);
                                }
                            }
                            return false;
                        } else {
                            return Arrays.deepEquals((Object[]) cleanInstanceValue, (Object[]) value);
                        }
                    }
                    return Objects.deepEquals(cleanInstanceValue, value);
                } else {
                    return Objects.deepEquals(cleanInstanceValue, value);
                }
            }
            case TYPE_DEFAULT: {
                return ReflectUtils.isDefaultValue(getPropertyType(), value);
            }
        }
        return Objects.equals(cleanInstanceValue, value);
    }

    @Override
    public boolean isDefaultValue(Object o
    ) {
        return isDefaultValue(o, null);
    }

    @Override
    public String toString() {
        return String.valueOf(type)+"."+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractReflectProperty that = (AbstractReflectProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(cleanInstanceValue, that.cleanInstanceValue) && Objects.equals(propertyType, that.propertyType) && Objects.equals(type, that.type) && defaultValueStrategy == that.defaultValueStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cleanInstanceValue, propertyType, type, defaultValueStrategy);
    }
}
