/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * implementations of any class T must implement 3 static methods :
 *
 * <ul>
 * <li> {@code static T parseLenient(String value) } </li>
 *
 * <li> {@code static T parseLenient(String value, T emptyValue) } </li>
 *
 * <li> {@code static T parseLenient(String value, T emptyValue, T errorValue) } </li>
 * </ul>
 *
 * @since 0.8.1
 */
public interface NutsEnum {
    /**
     * parse the given value and return a valid value or a default value (mostly null, but can be other)
     *
     * @param type    enum type
     * @param value   string value to parse
     * @param session session
     * @param <T>     enum Type
     * @return valid instance by calling {@code T.parseLenient(value)}
     * @since 0.8.3
     */
    @SuppressWarnings("unchecked")
    static <T extends NutsEnum> T parse(Class<T> type, String value, NutsSession session) {
        Method m;
        try {
            m = type.getMethod("parse", String.class, NutsSession.class);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parse(String,NutsSession)", type.getName());
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsIllegalArgumentException(session, msg);
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parse(String,NutsSession)", type.getName());
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsIllegalArgumentException(session, msg);
        }
        T r;
        try {
            r = (T) m.invoke(null, value, session);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("failed executing %s.parse(String) ", type.getName());
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsParseEnumException(session, msg, value, type);
        }
        return r;
    }

    /**
     * parse the given value and return a valid value or a default value (mostly null, but can be other)
     *
     * @param type  enum type
     * @param value string value to parse
     * @param <T>   enum Type
     * @return valid instance by calling {@code T.parseLenient(value)}
     * @since 0.8.1
     */
    @SuppressWarnings("unchecked")
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value) {
        Method m;
        try {
            m = type.getMethod("parseLenient", String.class);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parseLenient(String)", type.getName());
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsIllegalArgumentException(session, msg);
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parseLenient(String)", type.getName());
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsIllegalArgumentException(session, msg);
        }
        T r;
        try {
            r = (T) m.invoke(null, value);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("failed executing %s.parseLenient(String) ", type.getName());
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsParseEnumException(session, msg, value, type);
        }
        return r;
    }


    /**
     * parse the given value and return a valid value or a default value (mostly null, but can be other)
     *
     * @param type              enum type
     * @param value             string value to parse
     * @param emptyOrErrorValue value to return if the string is null or empty or could not be resolved to a valid enum instance
     * @param <T>               enum Type
     * @return valid instance by calling {@code T.parseLenient(value)}
     * @since 0.8.1
     */
    @SuppressWarnings("unchecked")
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value, T emptyOrErrorValue) {
        Method m;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parseLenient", String.class, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + ")");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + ")");
        }
        T r;
        try {
            r = (T) m.invoke(null, value, emptyOrErrorValue);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("failed executing %s.parseLenient(String,%s) ", type.getName(), typeSimpleName);
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsParseEnumException(session, msg, value, type);
        }
        return r;
    }

    /**
     * parse the given value and return a valid value or a default value (mostly null, but can be other)
     *
     * @param type       enum type
     * @param value      string value to parse
     * @param session    session
     * @param emptyValue value to return if the string is null or empty
     * @param <T>        enum Type
     * @return valid instance by calling {@code T.parseLenient(value)}
     * @since 0.8.1
     */
    @SuppressWarnings("unchecked")
    static <T extends NutsEnum> T parse(Class<T> type, String value, T emptyValue, NutsSession session) {
        Method m;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parse", String.class, type, NutsSession.class);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parse(String,%s,NutsSession)", type.getName(), typeSimpleName);
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsIllegalArgumentException(session, msg);
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parse(String,%s,NutsSession)", type.getName(), typeSimpleName);
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsIllegalArgumentException(session, msg);
        }
        T r;
        try {
            r = (T) m.invoke(null, value, emptyValue, session);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("failed executing %s.parse(String,%s,NutsSession) ", type.getName(), typeSimpleName);
            if (session == null) {
                throw new NutsBootException(msg);
            }
            throw new NutsParseEnumException(session, msg, value, type);
        }
        return r;
    }

    /**
     * parse the given value and return a valid value or a default value (mostly null, but can be other)
     *
     * @param type                enum type
     * @param value               string value to parse
     * @param <T>                 enum Type
     * @param emptyValue          value to return if the string is null or empty
     * @param errorOrUnknownValue value to return if the string could not be resolved to a valid enum instance
     * @return valid instance by calling {@code T.parseLenient(value)}
     * @since 0.8.1
     */
    @SuppressWarnings("unchecked")
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value, T emptyValue, T errorOrUnknownValue) {
        Method m;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parseLenient", String.class, type, type);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parseLenient(String,%s,%s,NutsSession)", type.getName(), typeSimpleName, typeSimpleName);
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsIllegalArgumentException(session, msg);
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            NutsMessage msg = NutsMessage.cstyle("NutsEnum %s must implement a public static method parseLenient(String,%s,%s,NutsSession)", type.getName(), typeSimpleName, typeSimpleName);
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsIllegalArgumentException(session, msg);
        }
        T r;
        try {
            r = (T) m.invoke(null, value, emptyValue, errorOrUnknownValue);
        } catch (Exception ex) {
            NutsMessage msg = NutsMessage.cstyle("failed executing %s.parse(String,%s,%s,NutsSession) ", type.getName(), typeSimpleName, typeSimpleName);
//            if(session==null){
            throw new NutsBootException(msg);
//            }
//            throw new NutsParseEnumException(session, msg, value, type);
        }
        return r;
    }

    String id();
}
