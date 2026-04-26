/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.reflect.NReflectType;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author thevpc
 */
public abstract class AbstractReflectProperty implements NReflectProperty {

    private String name;
    private boolean cleanInstanceValueLoaded;
    private Object cleanInstanceValue;
    private NReflectType propertyType;
    private NReflectType declaringType;
    private NReflectPropertyDefaultValueStrategy defaultValueStrategy;
    private Supplier<Object> cleanInstance;

    protected final void init(String name, NReflectType declaringType, Supplier<Object> cleanInstance, Type propertyType, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        this.name = name;
        this.cleanInstance = cleanInstance;
        this.declaringType = declaringType;
        NReflectType nReflectType = declaringType.getRepository().getType(propertyType)
                .replaceVars(t -> declaringType.getActualTypeArgument(t).orElse(t));
        this.defaultValueStrategy = defaultValueStrategy;
        this.propertyType = nReflectType;
    }

    private synchronized Object _cleanInstanceValue(){
        if(!cleanInstanceValueLoaded){
            synchronized (this){
                if(!cleanInstanceValueLoaded) {
                    if (cleanInstance == null) {
                        this.cleanInstanceValue = ReflectUtils.getDefaultValue(propertyType.getJavaType());
                    } else {
                        if (isRead()) {
                            try {
                                Object ii = cleanInstance.get();
                                if(ii!=null){
                                    this.cleanInstanceValue = read(ii);
                                }else{
                                    this.cleanInstanceValue = null;
                                }
                            }catch (Exception ex){
                                //ay exception is equivalent to null
                                this.cleanInstanceValue = null;
                            }
                        } else {
                            this.cleanInstanceValue = null;
                        }
                    }
                    cleanInstanceValueLoaded = true;
                }
            }
        }
        return cleanInstanceValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NReflectType getDeclaringType() {
        return declaringType;
    }

    @Override
    public NReflectType getPropertyType() {
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
            case NONE: {
                return false;
            }
            case PROTOTYPE: {
                if (value == null) {
                    return true; //null is always default!!
                }
                Object cleanInstanceValue1 = _cleanInstanceValue();
                if (cleanInstanceValue1 != null && cleanInstanceValue1.getClass().isArray()) {
                    if (Array.getLength(cleanInstanceValue1) == 0) {
                        //this is a simple yet recurrent use case!
                        return value.getClass().isArray() && Array.getLength(value) == 0;
                    }
                    if (value.getClass().isArray()) {
                        Class<?> e = cleanInstanceValue1.getClass().getComponentType();
                        Class<?> f = value.getClass().getComponentType();
                        if (e.isPrimitive()) {
                            if (f.isPrimitive() && e.equals(f)) {
                                switch (e.getName()) {
                                    case "boolean":
                                        return Arrays.equals((boolean[]) cleanInstanceValue1, (boolean[]) value);
                                    case "byte":
                                        return Arrays.equals((byte[]) cleanInstanceValue1, (byte[]) value);
                                    case "char":
                                        return Arrays.equals((char[]) cleanInstanceValue1, (char[]) value);
                                    case "short":
                                        return Arrays.equals((short[]) cleanInstanceValue1, (short[]) value);
                                    case "int":
                                        return Arrays.equals((int[]) cleanInstanceValue1, (int[]) value);
                                    case "long":
                                        return Arrays.equals((long[]) cleanInstanceValue1, (long[]) value);
                                    case "float":
                                        return Arrays.equals((float[]) cleanInstanceValue1, (float[]) value);
                                    case "double":
                                        return Arrays.equals((double[]) cleanInstanceValue1, (double[]) value);
                                }
                            }
                            return false;
                        } else {
                            return Arrays.deepEquals((Object[]) cleanInstanceValue1, (Object[]) value);
                        }
                    }
                    return Objects.deepEquals(cleanInstanceValue1, value);
                } else {
                    return Objects.deepEquals(cleanInstanceValue1, value);
                }
            }
            case BASE: {
                return getPropertyType().isDefaultValue(value);
            }
        }
        return Objects.equals(_cleanInstanceValue(), value);
    }

    @Override
    public boolean isDefaultValue(Object o
    ) {
        return isDefaultValue(o, null);
    }

    @Override
    public String toString() {
        return String.valueOf(declaringType)+"."+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractReflectProperty that = (AbstractReflectProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(cleanInstanceValue, that.cleanInstanceValue) && Objects.equals(propertyType, that.propertyType) && Objects.equals(declaringType, that.declaringType) && defaultValueStrategy == that.defaultValueStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cleanInstanceValue, propertyType, declaringType, defaultValueStrategy);
    }
}
