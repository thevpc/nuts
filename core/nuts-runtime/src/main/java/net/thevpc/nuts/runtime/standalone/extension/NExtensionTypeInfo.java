package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceFactory;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.util.*;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

public class NExtensionTypeInfo<T> {
    private Class<? extends T> implType;
    private Class<T> apiType;
    private final Map<TypeAndArgTypes, NBeanConstructor> cachedConstructors = new HashMap<>();
    private final PrintStream log;
    private NExtensionTypeInfoPool pool;
    private NScopeType scope;
    private NScorable scorer;
    private NBeanCache beanCache;

    public NExtensionTypeInfo(Class<? extends T> implType, Class<T> apiType, NExtensionTypeInfoPool pool, NBeanCache beanCache) {
        this.pool = pool;
        this.apiType = apiType;
        this.implType = implType;
        log = CoreNUtils.isDevVerbose() ? System.err : null;
        scope = computeScope();
        this.beanCache = beanCache;
    }

    public Class<? extends T> getImplType() {
        return implType;
    }

    public Class<T> getApiType() {
        return apiType;
    }

    public NLog LOG() {
        return NWorkspaceExt.of().getModel().LOG;
    }

    public <T> T newInstance() {
        return newInstance(new Class[0], new Object[0], null,true);
    }


    public <T extends NComponent> T resolveInstance() {
        return resolveInstance(new Class[0], new Object[0], null);
    }

    private static NPropertiesHolder resolveBeansHolder(NScopeType scope) {
        return NApp.of().getOrComputeProperty(NWorkspaceFactory.class.getName() + "::beans", scope, () -> new NPropertiesHolder());
    }


    public <T extends NComponent> T resolveInstance(Class<?>[] argTypes, Object[] args, NBeanConstructorContext context) {
        if (scope == NScopeType.PROTOTYPE) {
            return newInstance(argTypes, args, context, true);
        }
        NPropertiesHolder beans = resolveBeansHolder(scope);
        return (T) beans.getOrComputeProperty(implType.getName(), () -> newInstance(argTypes, args, context, true), scope);
    }

    private NScopeType computeScope() {
        NComponentScope apiScope = apiType.getAnnotation(NComponentScope.class);
        NComponentScope implScope = implType.getAnnotation(NComponentScope.class);
        NScopeType scope = NScopeType.PROTOTYPE;
        if (apiScope != null || implScope != null) {
            if (apiScope != null && implScope == null) {
                scope = apiScope.value();
            } else if (apiScope == null && implScope != null) {
                scope = implScope.value();
            } else {
                if (apiScope.value() == implScope.value()) {
                    scope = apiScope.value();
                } else {
                    //bo defined! stick with api!
                    scope = apiScope.value();
                    if (LOG().isLoggable(Level.CONFIG)) {
                        switch (apiType.getName()) {
                            //skip logging for NTexts to avoid infinite recursion
                            case "net.thevpc.nuts.text.NTexts": {
                                break;
                            }
                            default: {
                                LOG()
                                        .log(NMsg.ofJ("invalid scope {0} ; expected {1} for  {2}",
                                                                implScope.value(),
                                                                apiScope.value(),
                                                                implType.getName()
                                                        )
                                                        .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL)
                                        );
                            }
                        }
                    }
                }
            }
        }
        if (apiType.getAnnotation(NComponentScope.class) != null) {
            scope = apiType.getAnnotation(NComponentScope.class).value();
        }
        if (scope == null) {
            scope = NScopeType.PROTOTYPE;
        }
        return scope;
    }


    private NBeanConstructorContext validateContext(NBeanConstructorContext context) {
        return context == null ? NBeanConstructorContextImpl.INSTANCE :
                new NBeanConstructorContext() {
                    @Override
                    public boolean isSupported(Class<?> paramType) {
                        return context.isSupported(paramType)
                                || NBeanConstructorContextImpl.INSTANCE.isSupported(paramType)
                                ;
                    }

                    @Override
                    public Object resolve(Class<?> paramType) {
                        if (context.isSupported(paramType)) {
                            return context.resolve(paramType);
                        }
                        return NBeanConstructorContextImpl.INSTANCE.resolve(paramType);
                    }
                };
    }

    protected <T> T newInstance(Class[] argTypes, Object[] args, NBeanConstructorContext context, boolean doLog) {
        T t1 = null;
        context = validateContext(context);
        NBeanConstructor<T> ctrl0 = (NBeanConstructor<T>) beanCache.findConstructor(implType, argTypes, apiType, context);
        t1 = ctrl0.newInstance(args);
        //initialize?
        if (doLog) {
            if (NExtensionUtils.isBootstrapLogType(apiType)) {
                //
            } else if (LOG().isLoggable(Level.CONFIG)) {
                String old = pool._alreadyLogger.get(apiType.getName());
                if (old == null || !old.equals(implType.getName())) {
                    pool._alreadyLogger.put(apiType.getName(), implType.getName());
                    LOG()
                            .log(NMsg.ofC("resolve %s to  %s %s",
                                                    NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST),
                                                    scope,
                                                    implType.getName()
                                            )
                                            .withLevel(Level.FINEST).withIntent(NMsgIntent.READ)
                            );
                }
            }
        }
        return t1;
    }


    private NScorable createFixedNComponentScorer(int index) {
        synchronized (this) {
            Map<Integer, NScorable> m = NWorkspace.of().getOrComputeProperty("NComponentScorerMap", () -> new HashMap<Integer, NScorable>());
            if (index < 0) {
                return m.computeIfAbsent(NScorable.UNSUPPORTED_SCORE, i -> new MyNScorer(NScorable.UNSUPPORTED_SCORE));
            }
            return m.computeIfAbsent(index, i -> new MyNScorer(i));
        }
    }

    private NScorable createClassScorer() {
        NScore y = implType.getAnnotation(NScore.class);
        if (y != null) {
            int f = y.fixed();
            if (f != Integer.MIN_VALUE) {
                return createFixedNComponentScorer(f);
            } else {
                Class<NScorable> c = y.custom();
                if (!c.equals(NScorable.class)) {
                    return NWorkspace.of().getOrComputeProperty("scorer::" + c.getName(), () -> {
                        try {
                            return c.newInstance();
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
        //how check methods
        for (Method declaredMethod : implType.getDeclaredMethods()) {
            NScore r = declaredMethod.getAnnotation(NScore.class);
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            if (r != null) {
                if (Modifier.isStatic(declaredMethod.getModifiers())) {
                    if (parameterTypes.length == 1 && parameterTypes[0].equals(NScorableContext.class) && declaredMethod.getReturnType().equals(int.class)) {
                        declaredMethod.setAccessible(true);
                        return new MethodBasedNScorable(this, declaredMethod);
                    } else {
                        LOG().log(NMsg.ofC("[%S] [%s] scorer method ignored (invalid params) :: %s", implType, apiType, declaredMethod).asSevere());
                    }
                } else {
                    LOG().log(NMsg.ofC("[%S] [%s] scorer method ignored (not static) :: %s", implType, apiType, declaredMethod).asSevere());
                }
            } else {
                if (
                        declaredMethod.getName().equals("getScore")
                                && Modifier.isStatic(declaredMethod.getModifiers())
                                && parameterTypes.length == 1 && parameterTypes[0].equals(NScorableContext.class)
                                && declaredMethod.getReturnType().equals(int.class)
                ) {
                    LOG().log(NMsg.ofC("[%S] [%s] invalid (still accepted) score method %s ", implType, apiType, declaredMethod).asSevere());
                    return new MethodBasedNScorable(this, declaredMethod);
                }
            }
        }
        LOG().log(NMsg.ofC("[%s] [%s] accepted, though missing valid scoring", implType, apiType));
        return createFixedNComponentScorer(NScorable.CUSTOM_SCORE);
    }

    public NScorable getInstanceScorer(Object o) {
        if (o instanceof NScorable) {
            return (NScorable) o;
        }
        return getTypeScorer();
    }

    public int getInstanceScore(Object o, NScorableContext supportCriteria) {
        if (o instanceof NScorable) {
            return ((NScorable) o).getScore(supportCriteria);
        }
        return getTypeScore(supportCriteria);
    }

    public NScoredValue<T> getTypeScoredInstance(NScorableContext supportCriteria) {
        return new LazyNScoredValueImpl<>(
                () -> c -> getTypeScore(c),
                () -> resolveInstance(),
                supportCriteria);
    }

    public NScorable getTypeScorer() {
        if (scorer == null) {
            scorer = createClassScorer();
        }
        return scorer;
    }

    public int getTypeScore(NScorableContext supportCriteria) {
        return getTypeScorer().getScore(supportCriteria);
    }

    private static class MyNScorer implements NScorable {
        private final int index;

        public MyNScorer(int index) {
            this.index = index;
        }

        @Override
        public int getScore(NScorableContext context) {
            return index;
        }
    }

}
