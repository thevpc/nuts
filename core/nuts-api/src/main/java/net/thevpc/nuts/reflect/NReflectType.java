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
     * Generic super class, with this type actual type arguments substituted.
     * For instance the super type of {@code ArrayList<String>} is {@code AbstractList<String>}.
     *
     * @return generic super class or null for interfaces, primitives, type variables, wildcards and {@code Object}
     */
    @NGetter
    NReflectType superType();

    /**
     * Generic interfaces directly implemented by this type, with this type actual type arguments substituted.
     * For instance {@code ArrayList<String>} implements {@code List<String>}.
     *
     * @return directly implemented generic interfaces
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
     * Replace type variables at any depth (type arguments, owner type, generic array components and wildcard bounds).
     *
     * @param mapper maps a type variable to its replacement, returning null or the variable itself keeps it unchanged
     * @return this very instance when nothing was replaced
     */
    NReflectType replaceVars(Function<NReflectType, NReflectType> mapper);

    /**
     * The view of this type as the given super class or interface, with type arguments resolved.
     * For instance {@code findSuperType(Map.class)} on a {@code class Props extends HashMap<String,Integer>}
     * returns {@code Map<String,Integer>}.
     *
     * @param rawClass super class or interface
     * @return the (possibly parametrized) super type or empty if this type does not extend {@code rawClass}
     * @since 1.1.0
     */
    NOptional<NReflectType> findSuperType(Class<?> rawClass);

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
     * Checks if a value of {@code type} can be assigned to this type, honoring type arguments
     * (including wildcards) when this type is parametrized. Raw types accept any parametrization.
     *
     * @param type type
     * @return true if assignable
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
     * Raw type (erasure) : the type itself for a class, {@code List} for {@code List<String>},
     * the bound class for type variables and wildcards and the array class for generic arrays.
     *
     * @return raw type
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
