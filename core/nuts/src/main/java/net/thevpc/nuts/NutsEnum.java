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
    static <T extends NutsEnum> T parse(Class<T> type, String value, NutsSession session) {
        Method m = null;
        try {
            m = type.getMethod("parseLenient", String.class, NutsSession.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parse(String,NutsSession)");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parse(String,NutsSession)");
        }
        T r = null;
        try {
            r = (T) m.invoke(null, value, session);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unable to run  parse(String,NutsSession)");
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
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value) {
        Method m = null;
        try {
            m = type.getMethod("parseLenient", String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String)");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String)");
        }
        T r = null;
        try {
            r = (T) m.invoke(null, value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unable to run  parseLenient(String)");
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
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value, T emptyOrErrorValue) {
        Method m = null;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parseLenient", String.class, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + ")");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + ")");
        }
        T r = null;
        try {
            r = (T) m.invoke(null, value, emptyOrErrorValue);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unable to run  parseLenient(String," + typeSimpleName + ")");
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
    static <T extends NutsEnum> T parse(Class<T> type, String value, T emptyValue, NutsSession session) {
        Method m = null;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parseLenient", String.class, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parse(String," + typeSimpleName + ",NutsSession)");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parse(String," + typeSimpleName + ",NutsSession)");
        }
        T r = null;
        try {
            r = (T) m.invoke(null, value, emptyValue, session);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unable to run  parse(String," + typeSimpleName + ",NutsSession)");
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
    static <T extends NutsEnum> T parseLenient(Class<T> type, String value, T emptyValue, T errorOrUnknownValue) {
        Method m = null;
        String typeSimpleName = type.getSimpleName();
        try {
            m = type.getMethod("parseLenient", String.class, type, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + "," + typeSimpleName + ")");
        }
        if (!Modifier.isStatic(m.getModifiers()) || !Modifier.isPublic(m.getModifiers()) || !m.getReturnType().equals(type)) {
            throw new IllegalArgumentException("NutsEnum classes must implement a public static method parseLenient(String," + typeSimpleName + "," + typeSimpleName + ")");
        }
        T r = null;
        try {
            r = (T) m.invoke(null, value, emptyValue, errorOrUnknownValue);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unable to run  parseLenient(String," + typeSimpleName + "," + typeSimpleName + ")");
        }
        return r;
    }

    String id();
}
