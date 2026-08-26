package net.thevpc.nuts.reflect;

/**
 * NTypeNameSignature interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTypeNameSignature extends NSignature<NTypeName<?>, NTypeNameSignature> {
    /**
     * Creates a new instance of of.
     *
     * @param domain domain
     * @param types types
     * @return of result
     */
    static NTypeNameSignature of(NTypeNameDomain domain,NTypeName... types) {
        /**
         * Creates a new instance of of.
         *
         * @param domain domain
         * @param null null
         * @param types types
         * @return of result
         */
        return of(domain,null, types);
    }

    /**
     * Creates a new instance of of var args.
     *
     * @param domain domain
     * @param types types
     * @return of var args result
     */
    static NTypeNameSignature ofVarArgs(NTypeNameDomain domain,NTypeName... types) {
        /**
         * Creates a new instance of of var args.
         *
         * @param domain domain
         * @param null null
         * @param types types
         * @return of var args result
         */
        return ofVarArgs(domain,null, types);
    }

    /**
     * Creates a new instance of of.
     *
     * @param domain domain
     * @param name name
     * @param types types
     * @return of result
     */
    static NTypeNameSignature of(NTypeNameDomain domain,String name, NTypeName... types) {
        return NReflect.of().ofTypeNameSignature(domain,name, types);
    }

    /**
     * Creates a new instance of of var args.
     *
     * @param domain domain
     * @param name name
     * @param types types
     * @return of var args result
     */
    static NTypeNameSignature ofVarArgs(NTypeNameDomain domain,String name, NTypeName... types) {
        return NReflect.of().ofVarArgsTypeNameSignature(domain,name, types);
    }

    /**
     * Creates a new instance of of map.
     *
     * @param domain domain
     * @return of map result
     */
    static <V> NSignatureMap<NTypeNameSignature, NTypeName<?>, V> ofMap(NTypeNameDomain domain) {
        return NReflect.of().ofTYpeNameSignatureMap(domain);
    }
}
