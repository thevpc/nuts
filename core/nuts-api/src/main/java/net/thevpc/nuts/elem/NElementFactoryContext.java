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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.reflect.NReflectRepository;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public interface NElementFactoryContext {

    /**
     * Checks if is simple object.
     *
     * @param any any
     * @return is simple object result
     */
    boolean isSimpleObject(Object any);

    /**
     * Checks if is simple type.
     *
     * @param any any
     * @return is simple type result
     */
    boolean isSimpleType(Type any);

    /**
     * Checks if is atomic object.
     *
     * @param any any
     * @return is atomic object result
     */
    boolean isAtomicObject(Object any);

    /**
     * Checks if is atomic type.
     *
     * @param any any
     * @return is atomic type result
     */
    boolean isAtomicType(Type any);

    /**
     * Simple types filter.
     *
     * @return simple types filter result
     */
    Predicate<Type> simpleTypesFilter();

    /**
     * Properties.
     *
     * @return properties result
     */
    Map<String, Object> properties();

    /**
     * Default create element.
     *
     * @param o o
     * @param expectedType expected type
     * @return default create element result
     */
    NElement defaultCreateElement(Object o, Type expectedType);

    /**
     * Default to simple.
     *
     * @param o o
     * @param expectedType expected type
     * @return default to simple result
     */
    Object defaultToSimple(Object o, Type expectedType);

    /**
     * Converts to element.
     *
     * @param o o
     * @return to element result
     */
    NElement toElement(Object o);

    /**
     * Converts to element.
     *
     * @param o o
     * @param expectedType expected type
     * @return to element result
     */
    NElement toElement(Object o, Type expectedType);

    /**
     * Converts to simple.
     *
     * @param o o
     * @param expectedType expected type
     * @return to simple result
     */
    Object toSimple(Object o, Type expectedType);

    /**
     * Converts to object.
     *
     * @param o o
     * @param type type
     * @return to object result
     */
    <T> T toObject(NElement o, Class<T> type);

    /**
     * Converts to object.
     *
     * @param o o
     * @param type type
     * @return to object result
     */
    Object toObject(NElement o, Type type);

    /**
     * Default to object.
     *
     * @param o o
     * @param type type
     * @return default to object result
     */
    <T> T defaultToObject(NElement o, Class<T> type);

    /**
     * Default to object.
     *
     * @param o o
     * @param type type
     * @return default to object result
     */
    <T> T defaultToObject(NElement o, Type type);

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    boolean isNtf();

    /**
     * Types repository.
     *
     * @return types repository result
     */
    NReflectRepository typesRepository();

    /**
     * Returns the serializer.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get serializer result
     */
    <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly);

    /**
     * Returns the simplifier.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get simplifier result
     */
    <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly);

    /**
     * Returns the deserializer.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly);

    /**
     * Returns the deserializer.
     *
     * @param element element
     * @param defaultOnly default only
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly);
}
