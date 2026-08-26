package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * NTypeLoader interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTypeLoader {
    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @return of result
     */
    static NTypeLoader of(String name) {
        return NReflect.of().createTypeLoader(name);
    }

    /**
     * Try load.
     *
     * @param loader loader
     * @return try load result
     */
    NTypeLoader tryLoad(ClassLoader loader) ;


    /**
     * Checks if is loaded.
     *
     * @return is loaded result
     */
    boolean isLoaded() ;

    /**
     * Type.
     *
     * @return type result
     */
    NOptional<Class<?>> type() ;

    /**
     * Returns the declared method.
     *
     * @param name name
     * @param parameterTypes parameter types
     * @return get declared method result
     */
    NOptional<Method> getDeclaredMethod(String name, Class<?>... parameterTypes);

    /**
     * Returns the declared field.
     *
     * @param name name
     * @return get declared field result
     */
    NOptional<Field> getDeclaredField(String name);

    /**
     * Class name.
     *
     * @return class name result
     */
    String className() ;

    /**
     * New instance.
     *
     * @return new instance result
     */
    NOptional<Object> newInstance();
}
