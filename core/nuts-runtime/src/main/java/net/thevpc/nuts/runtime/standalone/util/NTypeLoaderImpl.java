package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NTypeLoaderImpl implements net.thevpc.nuts.reflect.NTypeLoader {
    private String className;
    private volatile boolean checked;
    private volatile Class<?> loadedType;

    public NTypeLoaderImpl(String className) {
        this.className = className;
    }

    public NTypeLoaderImpl tryLoad(ClassLoader loader) {
        if (loader != null && loadedType == null) {
            synchronized (this) {
                if (loadedType == null) {
                    loadUnsafe(loader);
                }
            }
        }
        return this;
    }

    private void loadUnsafe(ClassLoader loader) {
        try {
            loadedType = Class.forName(className, false, loader);
        } catch (NoClassDefFoundError e) {
            NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to load %s : %s", className, e).asFinestFail());
        } catch (Exception e) {
            NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to load %s : %s", className, e).asFinestFail());
        } finally {
            checked = true;
        }
    }

    public boolean isLoaded() {
        return loadedType != null;
    }

    public NOptional<Class<?>> getType() {
        if (loadedType != null) {
            return NOptional.of(loadedType);
        }
        if (checked) {
            return NOptional.ofNamedEmpty(NMsg.ofC("type %s",className));
        }
        synchronized (this) {
            if (loadedType != null) {
                return NOptional.of(loadedType);
            }
            if (!checked) {
                loadUnsafe(Thread.currentThread().getContextClassLoader());
            }
            return NOptional.ofNamed(loadedType,NMsg.ofC("type %s",className));
        }
    }

    @Override
    public NOptional<Method> getDeclaredMethod(String name, Class<?>... parameterTypes) {
        return getType().map(c -> {
            try {
                return c.getDeclaredMethod(name, parameterTypes);
            }catch (Exception ex){
                NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to find %s.%s(%s) : %s", className, name,
                        Arrays.stream(parameterTypes).map(p->p.getSimpleName()).collect(Collectors.joining(",")),
                        ex).asFinestFail());
            }
            return null;
        }).withMessage(()->NMsg.ofC("missing method find %s.%s(%s) : %s", className, name,
                Arrays.stream(parameterTypes).map(p->p.getSimpleName()).collect(Collectors.joining(","))));
    }

    @Override
    public NOptional<Field> getDeclaredField(String name) {
        return getType().map(c -> {
            try {
                return c.getDeclaredField(name);
            }catch (Exception ex){
                NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to find %s.%s : %s", className, name,
                        ex).asFinestFail(ex));
            }
            return null;
        }).withMessage(()->NMsg.ofC("missing method find %s.%s : %s", className, name));
    }

    public String getClassName() {
        return className;
    }

    @Override
    public NOptional<Object> newInstance() {
        return getType().map(x->{
            Constructor<?> c =null;
            try {
                c = x.getDeclaredConstructor();
            }catch (Exception ex){
                return NOptional.ofNamedEmpty(NMsg.ofC("constructor() for %s",className));
            }
            try {
                if(!Modifier.isPublic(c.getModifiers())) {
                    c.setAccessible(true);
                }
            }catch (Exception ex){
                return NOptional.ofNamedEmpty(NMsg.ofC("constructor() is not public and could not set accessible for %s",className));
            }
            try {
                return c.newInstance();
            }catch (Exception ex){
                return NOptional.ofNamedError(NMsg.ofC("constructor() failed for %s",className));
            }
        });
    }

    @Override
    public String toString() {
        if (checked) {
            if (loadedType != null) {
                return "loaded(" + className + ")";
            }
            return "notLoaded(" + className + ")";
        }
        return "unchecked(" + className + ")";
    }

}
