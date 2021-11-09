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
package net.thevpc.nuts.runtime.bundles.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author thevpc
 */
public class ReflectUtils {

    public static boolean isDefaultValue(Type type, Object value) {
        if (value == null) {
            return true;
        }
        if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isPrimitive()) {
                switch (c.getName()) {
                    case "booleans": {
                        return value.equals(false);
                    }
                    case "byte": {
                        return value.equals((byte)0);
                    }
                    case "char": {
                        return value.equals((char)0);
                    }
                    case "short": {
                        return value.equals((short)0);
                    }
                    case "int": {
                        return value.equals(0);
                    }
                    case "long": {
                        return value.equals(0L);
                    }
                    case "float": {
                        return value.equals(0.0f);
                    }
                    case "double": {
                        return value.equals(0.0);
                    }
                }
            }
        }
        return false;
    }
    public static Object getDefaultValue(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isPrimitive()) {
                switch (c.getName()) {
                    case "booleans": {
                        return (false);
                    }
                    case "byte": {
                        return ((byte)0);
                    }
                    case "char": {
                        return ((char)0);
                    }
                    case "short": {
                        return ((short)0);
                    }
                    case "int": {
                        return (0);
                    }
                    case "long": {
                        return (0L);
                    }
                    case "float": {
                        return (0.0f);
                    }
                    case "double": {
                        return (0.0);
                    }
                }
            }
        }
        return null;
    }

    public static ParameterizedType createParametrizedType(Type rawType, Type... actualTypeArguments) {
        return new SimpleParametrizedType(rawType, actualTypeArguments);
    }

    public static Class getRawClass(java.lang.reflect.Type type) {
        Type tclazz = type;
        while (tclazz instanceof ParameterizedType) {
            tclazz = ((ParameterizedType) tclazz).getRawType();
        }
        return (Class) tclazz;
    }
}
