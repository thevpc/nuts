package net.thevpc.nuts.reflect;

public interface NReflectSignature extends NSignature<NReflectType, NReflectSignature> {
    static NReflectSignature of(NReflectType... types) {
        return of(null, types);
    }

    static NReflectSignature ofVarArgs(NReflectType... types) {
        return ofVarArgs(null, types);
    }

    static NReflectSignature of(String name, NReflectType... types) {
        return NReflect.of().ofReflectSignature(name, types);
    }

    static NReflectSignature ofVarArgs(String name, NReflectType... types) {
        return NReflect.of().ofVarArgsReflectSignature(name, types);
    }

    static <V> NSignatureMap<NReflectSignature, NReflectType, V> ofMap() {
        return NReflect.of().ofReflectSignatureMap();
    }
}
