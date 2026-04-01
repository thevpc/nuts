package net.thevpc.nuts.reflect;

public interface NTypeNameSignature extends NSignature<NTypeName<?>, NTypeNameSignature> {
    static NTypeNameSignature of(NTypeNameDomain domain,NTypeName... types) {
        return of(domain,null, types);
    }

    static NTypeNameSignature ofVarArgs(NTypeNameDomain domain,NTypeName... types) {
        return ofVarArgs(domain,null, types);
    }

    static NTypeNameSignature of(NTypeNameDomain domain,String name, NTypeName... types) {
        return NReflect.of().ofTypeNameSignature(domain,name, types);
    }

    static NTypeNameSignature ofVarArgs(NTypeNameDomain domain,String name, NTypeName... types) {
        return NReflect.of().ofVarArgsTypeNameSignature(domain,name, types);
    }

    static <V> NSignatureMap<NTypeNameSignature, NTypeName<?>, V> ofMap(NTypeNameDomain domain) {
        return NReflect.of().ofTYpeNameSignatureMap(domain);
    }
}
