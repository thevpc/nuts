package net.thevpc.nuts.reflect;

/**
 * NReflectSignature interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflectSignature extends NSignature<NReflectType, NReflectSignature> {
    /**
     * Creates a new instance.
     *
     * @param types types
     * @return of result
     */
    static NReflectSignature of(NReflectType... types) {
        return of(null, types);
    }

    /**
     * Creates a new instance of var args.
     *
     * @param types types
     * @return of var args result
     */
    static NReflectSignature ofVarArgs(NReflectType... types) {
        return ofVarArgs(null, types);
    }

    /**
     * Creates a new instance.
     *
     * @param name name
     * @param types types
     * @return of result
     */
    static NReflectSignature of(String name, NReflectType... types) {
        return NReflect.of().ofReflectSignature(name, types);
    }

    /**
     * Creates a new instance of var args.
     *
     * @param name name
     * @param types types
     * @return of var args result
     */
    static NReflectSignature ofVarArgs(String name, NReflectType... types) {
        return NReflect.of().ofVarArgsReflectSignature(name, types);
    }

    /**
     * Creates a new instance of map.
     *
     * @return of map result
     */
    static <V> NSignatureMap<NReflectSignature, NReflectType, V> ofMap() {
        return NReflect.of().ofReflectSignatureMap();
    }
}
