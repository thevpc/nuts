package net.thevpc.nuts.reflect;

import java.lang.reflect.Type;

/**
 * NPlatformSignature interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPlatformSignature extends NSignature<Type, NPlatformSignature> {

    /**
     * Creates a new instance of of.
     *
     * @param types types
     * @return of result
     */
    static NPlatformSignature of(Type... types) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param types types
         * @return of result
         */
        return of(null, types);
    }

    /**
     * Creates a new instance of of var args.
     *
     * @param types types
     * @return of var args result
     */
    static NPlatformSignature ofVarArgs(Type... types) {
        /**
         * Creates a new instance of of var args.
         *
         * @param null null
         * @param types types
         * @return of var args result
         */
        return ofVarArgs(null, types);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param types types
     * @return of result
     */
    static NPlatformSignature of(String name, Type... types) {
        return NReflect.of().ofPlatformSignature(name, types);
    }

    /**
     * Creates a new instance of of var args.
     *
     * @param name name
     * @param types types
     * @return of var args result
     */
    static NPlatformSignature ofVarArgs(String name, Type... types) {
        return NReflect.of().ofVarArgsPlatformSignature(name, types);
    }

    /**
     * Creates a new instance of of map.
     *
     * @return of map result
     */
    static <V> NSignatureMap<NPlatformSignature, Type, V> ofMap() {
        return NReflect.of().ofPlatformSignatureMap();
    }
}
