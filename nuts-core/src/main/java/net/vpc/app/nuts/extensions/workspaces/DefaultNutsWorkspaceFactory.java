/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.workspaces;

import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsWorkspaceFactory;
import net.vpc.app.nuts.Prototype;
import net.vpc.app.nuts.Singleton;
import net.vpc.app.nuts.extensions.util.ListMap;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsWorkspaceFactory implements NutsWorkspaceFactory {

    private static final Logger log = Logger.getLogger(DefaultNutsWorkspaceFactory.class.getName());

    private final ListMap<Class, Class> classes = new ListMap<>();
    private final ListMap<Class, Object> instances = new ListMap<>();
    private final Map<Class, Object> singletons = new HashMap<>();

    public DefaultNutsWorkspaceFactory() {
        initialize();
    }

    public final void initialize() {
    }

    public boolean isRegisteredInstance(Class extensionPoint, Object implementation) {
        return instances.contains(extensionPoint, implementation);
    }

    public boolean isRegisteredType(Class extensionPoint, Class implementation) {
        return classes.contains(extensionPoint, implementation);
    }

    public Class findRegisteredType(Class extensionPoint, String implementation) {
        for (Class cls : classes.getAll(extensionPoint)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }

    public boolean isRegisteredType(Class extensionPoint, String implementation) {
        return findRegisteredType(extensionPoint, implementation) != null;
    }

    public <T> void registerInstance(Class<T> extensionPoint, T implementation) {
        if (isRegisteredInstance(extensionPoint, implementation)) {
            throw new IllegalArgumentException("Already Registered Extension " + implementation + " for " + extensionPoint.getName());
        }
        log.log(Level.FINER, "Registering {0} for impl instance {1}", new Object[]{extensionPoint, implementation.getClass().getName()});
        instances.add(extensionPoint, implementation);
    }

    public Set<Class> getExtensionPoints() {
        return new HashSet<>(classes.keySet());
    }

    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return new HashSet<>(classes.getAll(extensionPoint));
    }

    public List<Object> getExtensionObjects(Class extensionPoint) {
        return new ArrayList<>(instances.getAll(extensionPoint));
    }

    public void registerType(Class extensionPoint, Class implementation) {
        if (isRegisteredType(extensionPoint, implementation.getName())) {
            throw new IllegalArgumentException("Already Registered Extension " + implementation.getName() + " for " + extensionPoint.getName());
        }
        log.log(Level.FINER, "Registering {0} for impl type {1}", new Object[]{extensionPoint, implementation.getName()});
        classes.add(extensionPoint, implementation);
    }

    public void unregisterType(Class extensionPoint, Class implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation.getName());
        if (registered != null) {
            log.log(Level.FINER, "Unregistering {0} for impl type {1}", new Object[]{extensionPoint, registered.getName()});
            classes.remove(extensionPoint, registered);
        }
    }

    public void unregisterType(Class extensionPoint, String implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation);
        if (registered != null) {
            log.log(Level.FINER, "Unregistering {0} for impl type {1}", new Object[]{extensionPoint, registered.getName()});
            classes.remove(extensionPoint, registered);
        }
    }

    protected <T> T instantiate0(Class<T> t) {
        T t1 = null;
        try {
            t1 = t.newInstance();
        } catch (InstantiationException e) {
            log.log(Level.SEVERE, "Unable to instantiate " + t, e);
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        //initialize?
        return t1;
    }

    protected <T> T instantiate0(Class<T> t, Class[] argTypes, Object[] args) {
        T t1 = null;
        try {
            t1 = t.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException e) {
            log.log(Level.SEVERE, "Unable to instantiate " + t, e);
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to instantiate " + t, e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        //initialize?
        return t1;
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType) {
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(Singleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(Prototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(Singleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(Prototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type);
                singletons.put(type, o);
                log.log(Level.FINER, "Resolve {0} to singleton instance {1}", new Object[]{baseType, o.getClass().getName()});
            }
            return (T) o;
        } else {
            T o = instantiate0(type);
            log.log(Level.FINER, "Resolve {0} to prototype instance {1}", new Object[]{baseType, o.getClass().getName()});
            return o;
        }
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType, Class[] argTypes, Object[] args) {
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(Singleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(Prototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(Singleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(Prototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            if (argTypes.length > 0) {
                throw new IllegalArgumentException("Singletons should have no types");
            }
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type);
                singletons.put(type, o);
                log.log(Level.FINER, "Resolve {0} to singleton instance {1}", new Object[]{baseType, o.getClass().getName()});
            }
            return (T) o;
        } else {
            T o = instantiate0(type, argTypes, args);
            log.log(Level.FINER, "Resolve {0} to prototype instance {1}", new Object[]{baseType, o.getClass().getName()});
            return o;
        }
    }

    //    @Override
    public <T> T create(Class<T> type) {
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            log.log(Level.FINER, "Resolve {0} to static instance {1}", new Object[]{type, one.getClass().getName()});
            return (T) one;
        }
        return (T) resolveInstance(classes.getOne(type), type);
    }
    @Override
    public <T> List<T> createAll(Class<T> type) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        for (Class c : classes.getAll(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (obj != null) {
                all.add(obj);
            }
        }
//        ServiceLoader serviceLoader = ServiceLoader.load(type);
//        for (Object object : serviceLoader) {
//            all.add((T) object);
//        }
        return all;
    }

    public <T> List<T> createAll(Class<T> type, Class[] argTypes, Object[] args) {
        List<T> all = new ArrayList<T>();
        for (Class c : classes.getAll(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (obj != null) {
                all.add(obj);
            }
        }
//        ServiceLoader serviceLoader = ServiceLoader.load(type);
//        for (Object object : serviceLoader) {
//            all.add((T) object);
//        }
        return all;
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] argTypes, Object[] args) {
        List<T> list = createAll(type, argTypes, args);
        int bestSupportLevel = Integer.MIN_VALUE;
        T bestObj = null;
        for (T t : list) {
            int supportLevel = t.getSupportLevel(supportCriteria);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
        List<T> list = createAll(type);
        int bestSupportLevel = Integer.MIN_VALUE;
        T bestObj = null;
        for (T t : list) {
            int supportLevel = t.getSupportLevel(supportCriteria);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria) {
        List<T> list = createAll(type);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(supportCriteria);
            if (supportLevel <= 0) {
                iterator.remove();
            }
        }
        return list;
    }
}
