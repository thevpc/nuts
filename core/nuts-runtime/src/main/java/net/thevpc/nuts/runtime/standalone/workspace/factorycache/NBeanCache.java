package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.lib.common.collections.NArrays;
import net.thevpc.nuts.util.NMsg;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class NBeanCache {
    private final Map<TypeAndArgTypes, CachedConstructor> cachedConstructors = new HashMap<>();
    private final NLog LOG;
    private final PrintStream log;

    public NBeanCache(NLog LOG, PrintStream log) {
        this.LOG = LOG;
        this.log = log;
    }

    private <T> Constructor<T> resolveExactConstructor(Class<T> t, Class[] argTypes) {
        Constructor<T> ctrl = null;
        try {
            ctrl = t.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            if (log != null) {
                log.println(NMsg.ofC("constructor not found %s(%s)", t.getName(), Arrays.stream(argTypes).map(x -> x.getName()).collect(Collectors.toList())));
            }
            return null;
        }
        ctrl.setAccessible(true);
        if (log != null) {
            log.println(NMsg.ofC("constructor found %s(%s)", t.getName(), Arrays.stream(argTypes).map(x -> x.getName()).collect(Collectors.toList())));
        }
        return ctrl;
    }

    public <T> CachedConstructor<T> findConstructor(Class<T> t, Class[] argTypes, NSession session) {
        TypeAndArgTypes tt = new TypeAndArgTypes(t, argTypes);
        synchronized (cachedConstructors) {
            CachedConstructor<T> o = cachedConstructors.get(tt);
            if (o != null) {
                return o;
            }
            if (cachedConstructors.containsKey(tt)) {
                return null;
            }
            CachedConstructor<T> c = createConstructor(tt, session);
            cachedConstructors.put(tt, c);
            return c;
        }
    }

    public <T> CachedConstructor<T> createConstructor(TypeAndArgTypes tt, NSession session) {
        Class<?>[] baseArgTypes = tt.getArgTypes();
        Constructor<T> c;
        Class<T> typeToInstantiate = tt.getType();
        if (baseArgTypes.length > 0) {
            //session is the last argument?
            c = resolveExactConstructor(typeToInstantiate, NArrays.append(baseArgTypes, NSession.class));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(NArrays.append(args, session));
                    }
                };
            }
            //session is the first argument?
            c = resolveExactConstructor(typeToInstantiate, NArrays.prepend(NSession.class, baseArgTypes));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(NArrays.prepend(session, args));
                    }
                };
            }
            //Workspace is the last argument?
            c = resolveExactConstructor(typeToInstantiate, NArrays.append(baseArgTypes, NWorkspace.class));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(NArrays.append(args, session.getWorkspace()));
                    }
                };
            }
            //Workspace is the first argument?
            c = resolveExactConstructor(typeToInstantiate, NArrays.prepend(NWorkspace.class, baseArgTypes));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(NArrays.prepend(session.getWorkspace(), args));
                    }
                };
            }

            //exact params
            c = resolveExactConstructor(typeToInstantiate, baseArgTypes);
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(args);
                    }
                };
            }

            //session only
            c = resolveExactConstructor(typeToInstantiate, new Class[]{NSession.class});
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(session);
                    }
                };
            }

            //Workspace only
            c = resolveExactConstructor(typeToInstantiate, new Class[]{NWorkspace.class});
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(session.getWorkspace());
                    }
                };
            }


            //no args
            c = resolveExactConstructor(typeToInstantiate, new Class[0]);
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance();
                    }
                };
            }
        }else{
            //not args, so first try with session
            c = resolveExactConstructor(typeToInstantiate, new Class[]{NSession.class});
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(session);
                    }
                };
            }
            //then try with workspace
            c = resolveExactConstructor(typeToInstantiate, new Class[]{NWorkspace.class});
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance(NArrays.append(args, session.getWorkspace()));
                    }
                };
            }
            //finally try with no args
            c = resolveExactConstructor(typeToInstantiate, new Class[0]);
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        return c.newInstance();
                    }
                };
            }
        }
        return null;
    }
}
