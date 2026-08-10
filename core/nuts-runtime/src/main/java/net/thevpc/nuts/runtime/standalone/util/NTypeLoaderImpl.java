package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NTypeLoaderImpl implements net.thevpc.nuts.reflect.NTypeLoader {
    private final String className;
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

    private void loadUnsafeSuppliers(Supplier<ClassLoader>... loaders) {
        NMsg error = null;
        try {
            for (Supplier<ClassLoader> loader : loaders) {
                if (loader == null) {
                    continue;
                }
                try {
                    loadedType = Class.forName(className, false, loader.get());
                    if (loadedType != null) {
                        return;
                    }
                } catch (NoClassDefFoundError e) {
                    if (error == null) {
                        error = NMsg.ofC("unable to load %s : %s", className, e).asFinestAlert();
                    }
                } catch (Exception e) {
                    if (error == null) {
                        error = NMsg.ofC("unable to load %s : %s", className, e).asFinestAlert();
                    }
                }
            }
        } finally {
            checked = true;
        }
        if (error != null) {
            NLog.of(NTypeLoaderImpl.class).log(error);
        }
    }

    private void loadUnsafe(ClassLoader loader) {
        try {
            loadedType = Class.forName(className, false, loader);
        } catch (NoClassDefFoundError e) {
            NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to load %s : %s", className, e).asFinestAlert());
        } catch (Exception e) {
            NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to load %s : %s", className, e).asFinestAlert());
        } finally {
            checked = true;
        }
    }

    public boolean isLoaded() {
        return loadedType != null;
    }

    public NOptional<Class<?>> type() {
        if (loadedType != null) {
            return NOptional.of(loadedType);
        }
        if (checked) {
            return NOptional.ofNamedEmpty(NMsg.ofC("type %s", className));
        }
        synchronized (this) {
            if (loadedType != null) {
                return NOptional.of(loadedType);
            }
            if (!checked) {
                loadUnsafeSuppliers(
                        () -> Thread.currentThread().getContextClassLoader(),
                        (NWorkspaceExt.of().getModel().extensionModel != null) ? () -> NWorkspaceExt.of().getModel().extensionModel.getWorkspaceExtensionsClassLoader().asClassLoader() : null,
                        () -> NWorkspaceExt.of().getModel().bootClassLoader
                );
            }
            return NOptional.ofNamed(loadedType, NMsg.ofC("type %s", className));
        }
    }

    @Override
    public NOptional<Method> getDeclaredMethod(String name, Class<?>... parameterTypes) {
        return type().map(c -> {
            try {
                return c.getDeclaredMethod(name, parameterTypes);
            } catch (Exception ex) {
                NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to find %s.%s(%s) : %s", className, name,
                        Arrays.stream(parameterTypes).map(p -> p.getSimpleName()).collect(Collectors.joining(",")),
                        ex).asFinestAlert());
            }
            return null;
        }).withMessage(() -> NMsg.ofC("missing method find %s.%s(%s) : %s", className, name,
                Arrays.stream(parameterTypes).map(p -> p.getSimpleName()).collect(Collectors.joining(","))).asFineAlert());
    }

    @Override
    public NOptional<Field> getDeclaredField(String name) {
        return type().map(c -> {
            try {
                return c.getDeclaredField(name);
            } catch (Exception ex) {
                NLog.of(NTypeLoaderImpl.class).log(NMsg.ofC("unable to find %s.%s : %s", className, name,
                        ex).asFinestAlert(ex));
            }
            return null;
        }).withMessage(() -> NMsg.ofC("missing method find %s.%s : %s", className, name).asFineAlert());
    }

    public String className() {
        return className;
    }

    @Override
    public NOptional<Object> newInstance() {
        return type().map(x -> {
            Constructor<?> c = null;
            try {
                c = x.getDeclaredConstructor();
            } catch (Exception ex) {
                return NOptional.ofNamedEmpty(NMsg.ofC("constructor() for %s", className).asFineAlert());
            }
            try {
                if (!Modifier.isPublic(c.getModifiers())) {
                    c.setAccessible(true);
                }
            } catch (Exception ex) {
                return NOptional.ofNamedEmpty(NMsg.ofC("constructor() is not public and could not set accessible for %s", className).asFineAlert());
            }
            try {
                return c.newInstance();
            } catch (Exception ex) {
                return NOptional.ofNamedError(NMsg.ofC("constructor() call failed for %s", className).asFineAlert());
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
