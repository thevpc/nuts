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

    boolean isSimpleObject(Object any);

    boolean isSimpleType(Type any);

    boolean isAtomicObject(Object any);

    boolean isAtomicType(Type any);

    Predicate<Type> simpleTypesFilter();

    Map<String, Object> properties();

    NElement defaultCreateElement(Object o, Type expectedType);

    Object defaultToSimple(Object o, Type expectedType);

    NElement toElement(Object o);

    NElement toElement(Object o, Type expectedType);

    Object toSimple(Object o, Type expectedType);

    <T> T toObject(NElement o, Class<T> type);

    Object toObject(NElement o, Type type);

    <T> T defaultToObject(NElement o, Class<T> type);

    <T> T defaultToObject(NElement o, Type type);

    boolean isNtf();

    NReflectRepository typesRepository();

    <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly);

    <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly);

    <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly);

    <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly);
}
