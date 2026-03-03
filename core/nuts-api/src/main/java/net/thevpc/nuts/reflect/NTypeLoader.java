package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface NTypeLoader {
    static NTypeLoader of(String name) {
        return NReflect.of().createTypeLoader(name);
    }

    NTypeLoader tryLoad(ClassLoader loader) ;


    boolean isLoaded() ;

    NOptional<Class<?>> getType() ;

    NOptional<Method> getDeclaredMethod(String name, Class<?>... parameterTypes);

    NOptional<Field> getDeclaredField(String name);

    String getClassName() ;

}
