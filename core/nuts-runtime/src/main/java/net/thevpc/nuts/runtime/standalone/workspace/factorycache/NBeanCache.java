package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class NBeanCache {
    private final Map<TypeAndArgTypes, CachedConstructor> cachedCtrls = new HashMap<>();
    private final NLog LOG;

    public NBeanCache(NLog LOG) {
        this.LOG = LOG;
    }

    private <T> Constructor<T> findConstructor(Class<T> t, Class[] argTypes) {
        Constructor<T> ctrl = null;
        try {
            ctrl = t.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
        ctrl.setAccessible(true);
        return ctrl;
    }

    public <T> CachedConstructor<T> getCtrl0(Class<T> t, Class[] argTypes, NSession session) {
        TypeAndArgTypes tt = new TypeAndArgTypes(t, argTypes);
        CachedConstructor<T> o = cachedCtrls.get(tt);
        if (o != null) {
            return o;
        }
        if(cachedCtrls.containsKey(tt)){
            return null;
        }
        CachedConstructor<T> c = createCtrl0(tt, session);
        cachedCtrls.put(tt, c);
        return c;
    }

    private  <T> CachedConstructor<T> createCtrl0(TypeAndArgTypes tt, NSession session) {
        {
            //session is the last argument?
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.addAll(Arrays.asList(tt.getArgTypes()));
            argTypes2.add(NSession.class);
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.addAll(Arrays.asList(args));
                        all.add(session);
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //session is the first argument?
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.add(NSession.class);
            argTypes2.addAll(Arrays.asList(tt.getArgTypes()));
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.add(session);
                        all.addAll(Arrays.asList(args));
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
      {
            //Workspace is the last argument?
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.addAll(Arrays.asList(tt.getArgTypes()));
            argTypes2.add(NWorkspace.class);
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.addAll(Arrays.asList(args));
                        all.add(session.getWorkspace());
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //Workspace is the first argument?
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.add(NWorkspace.class);
            argTypes2.addAll(Arrays.asList(tt.getArgTypes()));
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.add(session.getWorkspace());
                        all.addAll(Arrays.asList(args));
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //exact params
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.addAll(Arrays.asList(tt.getArgTypes()));
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.addAll(Arrays.asList(args));
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //session only
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.add(NSession.class);
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.add(session);
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //Workspace only
            List<Class> argTypes2 = new ArrayList<>();
            argTypes2.add(NWorkspace.class);
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        all.add(session.getWorkspace());
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        {
            //no args
            List<Class> argTypes2 = new ArrayList<>();
            Constructor<T> c = findConstructor(tt.getType(), argTypes2.toArray(new Class[0]));
            if (c != null) {
                return new AbstractCachedConstructor<T>(c) {
                    @Override
                    public T newInstanceUnsafe(Object[] args, NSession session) throws InvocationTargetException, InstantiationException, IllegalAccessException {
                        List<Object> all = new ArrayList<>();
                        return c.newInstance(all.toArray());
                    }
                };
            }
        }
        return null;
    }
}
