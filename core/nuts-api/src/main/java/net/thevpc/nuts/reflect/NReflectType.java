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

    /**
     * Repository.
     *
     * @return repository result
     */
    @NGetter
    NReflectRepository repository();

    /**
     * Access strategies.
     *
     * @return access strategies result
     */
    @NGetter
    Set<NReflectPropertyAccessStrategy> accessStrategies();

    /**
     * Default value strategy.
     *
     * @return default value strategy result
     */
    @NGetter
    NReflectPropertyDefaultValueStrategy defaultValueStrategy();

    /**
     * Declared properties.
     *
     * @return declared properties result
     */
    @NGetter
    List<NReflectProperty> declaredProperties();

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Java type.
     *
     * @return java type result
     */
    @NGetter
    Type javaType();

    /**
     * Java class.
     *
     * @return java class result
     */
    NOptional<Class<?>> javaClass();

    /**
     * Checks if is interface.
     *
     * @return is interface result
     */
    boolean isInterface();

    /**
     * Super type.
     *
     * @return super type result
     */
    @NGetter
    NReflectType superType();

    /**
     * Interfaces.
     *
     * @return interfaces result
     */
    @NGetter
    List<NReflectType> interfaces();

    /**
     * Checks if is parametrized type.
     *
     * @return is parametrized type result
     */
    boolean isParametrizedType();

    /**
     * Checks if is type variable.
     *
     * @return is type variable result
     */
    boolean isTypeVariable();

    /**
     * Type parameters.
     *
     * @return type parameters result
     */
    List<NReflectType> typeParameters();

    /**
     * Returns the actual type argument.
     *
     * @param type type
     * @return get actual type argument result
     */
    NOptional<NReflectType> getActualTypeArgument(NReflectType type);

    /**
     * Actual type arguments.
     *
     * @return actual type arguments result
     */
    List<NReflectType> actualTypeArguments();

    /**
     * Replace vars.
     *
     * @param mapper mapper
     * @return replace vars result
     */
    NReflectType replaceVars(Function<NReflectType, NReflectType> mapper);

    /**
     * all methods including super (if not overridden)
     *
     * @return
     */
    List<NReflectMethod> methods();

    /**
     * Returns the method.
     *
     * @param name name
     * @param signature signature
     * @return get method result
     */
    NOptional<NReflectMethod> getMethod(String name, NReflectSignature signature);

    /**
     * Returns the matching methods.
     *
     * @param name name
     * @param signature signature
     * @return get matching methods result
     */
    List<NReflectMethod> getMatchingMethods(String name, NReflectSignature signature);

    /**
     * Returns the matching method.
     *
     * @param name name
     * @param signature signature
     * @return get matching method result
     */
    NOptional<NReflectMethod> getMatchingMethod(String name, NReflectSignature signature);

    /**
     * only declared methods
     *
     * @return
     */
    List<NReflectMethod> declaredMethods();

    /**
     * Properties.
     *
     * @return properties result
     */
    List<NReflectProperty> properties();

    /**
     * Returns the property.
     *
     * @param name name
     * @return get property result
     */
    NOptional<NReflectProperty> getProperty(String name);

    /**
     * Returns the declared property.
     *
     * @param name name
     * @return get declared property result
     */
    NOptional<NReflectProperty> getDeclaredProperty(String name);

    /**
     * Checks if is assignable from.
     *
     * @param type type
     * @return is assignable from result
     */
    boolean isAssignableFrom(NReflectType type);

    /**
     * Checks if has no args constructor.
     *
     * @return has no args constructor result
     */
    boolean hasNoArgsConstructor();

    /**
     * Checks if has special constructor.
     *
     * @return has special constructor result
     */
    boolean hasSpecialConstructor();

    /**
     * Raw type.
     *
     * @return raw type result
     */
    NReflectType rawType();

    /**
     * New instance.
     *
     * @return new instance result
     */
    Object newInstance();

    /**
     * Checks if is array type.
     *
     * @return is array type result
     */
    boolean isArrayType();

    /**
     * Component type.
     *
     * @return component type result
     */
    NReflectType componentType();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    NReflectType toArray();

    /**
     * Checks if is primitive.
     *
     * @return is primitive result
     */
    boolean isPrimitive();

    /**
     * Boxed type.
     *
     * @return boxed type result
     */
    NOptional<NReflectType> boxedType();

    /**
     * Primitive type.
     *
     * @return primitive type result
     */
    NOptional<NReflectType> primitiveType();

    /**
     * Default value.
     *
     * @return default value result
     */
    Object defaultValue();

    /**
     * Checks if is default value.
     *
     * @param value value
     * @return is default value result
     */
    boolean isDefaultValue(Object value);

}
