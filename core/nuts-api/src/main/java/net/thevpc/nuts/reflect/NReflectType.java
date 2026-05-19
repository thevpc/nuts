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
package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author thevpc
 * @since 0.8.4
 */
public interface NReflectType {

    @NGetter
    NReflectRepository repository();

    @NGetter
    Set<NReflectPropertyAccessStrategy> accessStrategies();

    @NGetter
    NReflectPropertyDefaultValueStrategy defaultValueStrategy();

    @NGetter
    List<NReflectProperty> declaredProperties();

    @NGetter
    String name();

    @NGetter
    Type javaType();

    NOptional<Class<?>> javaClass();

    boolean isInterface();

    @NGetter
    NReflectType superType();

    @NGetter
    List<NReflectType> interfaces();

    boolean isParametrizedType();

    boolean isTypeVariable();

    List<NReflectType> typeParameters();

    NOptional<NReflectType> getActualTypeArgument(NReflectType type);

    List<NReflectType> actualTypeArguments();

    NReflectType replaceVars(Function<NReflectType, NReflectType> mapper);

    /**
     * all methods including super (if not overridden)
     *
     * @return
     */
    List<NReflectMethod> methods();

    NOptional<NReflectMethod> getMethod(String name, NReflectSignature signature);

    List<NReflectMethod> getMatchingMethods(String name, NReflectSignature signature);

    NOptional<NReflectMethod> getMatchingMethod(String name, NReflectSignature signature);

    /**
     * only declared methods
     *
     * @return
     */
    List<NReflectMethod> declaredMethods();

    List<NReflectProperty> properties();

    NOptional<NReflectProperty> getProperty(String name);

    NOptional<NReflectProperty> getDeclaredProperty(String name);

    boolean isAssignableFrom(NReflectType type);

    boolean hasNoArgsConstructor();

    boolean hasSpecialConstructor();

    NReflectType rawType();

    Object newInstance();

    boolean isArrayType();

    NReflectType componentType();

    NReflectType toArray();

    boolean isPrimitive();

    NOptional<NReflectType> boxedType();

    NOptional<NReflectType> primitiveType();

    Object defaultValue();

    boolean isDefaultValue(Object value);

}
