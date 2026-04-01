package net.thevpc.nuts.reflect;

import java.lang.reflect.Type;

public interface NPlatformSignature extends NSignature<Type, NPlatformSignature> {

    static NPlatformSignature of(Type... types) {
        return of(null, types);
    }

    static NPlatformSignature ofVarArgs(Type... types) {
        return ofVarArgs(null, types);
    }

    static NPlatformSignature of(String name, Type... types) {
        return NReflect.of().ofPlatformSignature(name, types);
    }

    static NPlatformSignature ofVarArgs(String name, Type... types) {
        return NReflect.of().ofVarArgsPlatformSignature(name, types);
    }

    static <V> NSignatureMap<NPlatformSignature, Type, V> ofMap() {
        return NReflect.of().ofPlatformSignatureMap();
    }
}
