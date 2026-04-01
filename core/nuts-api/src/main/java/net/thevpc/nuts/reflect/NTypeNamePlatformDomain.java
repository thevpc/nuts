package net.thevpc.nuts.reflect;

public interface NTypeNamePlatformDomain extends NTypeNameDomain {
    static NTypeNamePlatformDomain of(){
        return NReflect.of().platformDomain();
    }

    <T> Class<T> getTypeClass(NTypeName<T> any);
}
