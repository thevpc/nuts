/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceFactory;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.bundles.collections.ClassClassMap;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.collections.ListMap;
import net.thevpc.nuts.spi.NutsComponent;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.core.util.CoreServiceUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsWorkspaceFactory implements NutsWorkspaceFactory {

    private final NutsLogger LOG;

    private final static class ClassExtension {

        Class clazz;
        Object source;
        boolean enabled = true;

        public ClassExtension(Class clazz, Object source, boolean enabled) {
            this.clazz = clazz;
            this.source = source;
            this.enabled = enabled;
        }
    }

//    private final ListMap<Class, ClassExtension> classes = new ListMap<>();
    private final ListMap<Class, Object> instances = new ListMap<>();
    private final Map<Class, Object> singletons = new HashMap<>();
//    private final Map<ClassLoader, List<Class>> discoveredCacheByLoader = new HashMap<>();
//    private final Map<URL, List<Class>> discoveredCacheByURL = new HashMap<>();
    private final Map<NutsId, IdCache> discoveredCacheById = new HashMap<>();
//    private final ClassClassMap discoveredCacheByClass = new ClassClassMap();
    private NutsWorkspace workspace;

    public DefaultNutsWorkspaceFactory(NutsWorkspace ws) {
        this.workspace = ws;
        LOG = ((DefaultNutsWorkspace) ws).LOG;
    }

    private static class IdCache {

        private NutsId id;
        private URL url;
        private Map<Class, ClassClassMap> classes = new HashMap<>();

        public IdCache(NutsId id) {
            this.id = id;
        }

        public IdCache(NutsId id, URL url, ClassLoader bootClassLoader, NutsLogger LOG, Class[] extensionPoints, NutsSession session) {
            this.id = id;
            this.url = url;
            for (Class extensionPoint : extensionPoints) {
                ClassClassMap cc = new ClassClassMap();
                classes.put(extensionPoint, cc);
                Class<NutsComponent> serviceClass = NutsComponent.class;
                for (String n : CoreServiceUtils.loadZipServiceClassNames(url, serviceClass)) {
                    Class<?> c = null;
                    try {
                        c = Class.forName(n, false, bootClassLoader);
                    } catch (ClassNotFoundException x) {
                        LOG.with().session(session).verb(NutsLogVerb.WARNING).level(Level.FINE).error(x).log("not a valid type {0}", c);
                    }
                    if (c != null) {
                        if (!serviceClass.isAssignableFrom(c)) {
                            LOG.with().session(session).verb(NutsLogVerb.WARNING).level(Level.FINE).log("not a valid type {0} <> {1}, ignore...", c, serviceClass);
                        } else {
                            cc.add(c);
                        }
                    }
                }
            }
        }

        private ClassClassMap getClassClassMap(Class extensionPoint, boolean create) {
            ClassClassMap r = classes.get(extensionPoint);
            if (r == null && create) {
                r = new ClassClassMap();
                classes.put(extensionPoint, r);
            }
            return r;
        }

        public NutsId getId() {
            return id;
        }

        public URL getUrl() {
            return url;
        }

        public Set<Class> getExtensionPoints() {
            return new LinkedHashSet<>(classes.keySet());
        }

        public Set<Class> getExtensionTypes(Class extensionPoint) {
            LinkedHashSet<Class> all = new LinkedHashSet<>();
            for (Map.Entry<Class, ClassClassMap> rr : classes.entrySet()) {
                if (rr.getKey().isAssignableFrom(extensionPoint)) {
                    all.addAll(Arrays.asList(rr.getValue().getAll(extensionPoint)));
                }
            }
            return all;
        }
    }

    @Override
    public Set<Class> discoverTypes(NutsId id, URL url, ClassLoader bootClassLoader, NutsSession session) {
        return discoverTypes(id, url, bootClassLoader, new Class[]{NutsComponent.class}, session);
    }

    @Override
    public Set<Class> discoverTypes(NutsId id, URL url, ClassLoader bootClassLoader, Class[] extensionPoints, NutsSession session) {
        if (!discoveredCacheById.containsKey(id)) {
            discoveredCacheById.put(id, new IdCache(id, url, bootClassLoader, LOG, extensionPoints, session));
        }
        return null;
    }

//    @Override
//    public Set<Class> discoverTypes(ClassLoader bootClassLoader) {
//        List<Class> types = discoveredCacheByLoader.get(bootClassLoader);
//        if (types == null) {
//            types = CoreCommonUtils.loadServiceClasseNames(NutsComponent.class, bootClassLoader);
//            discoveredCacheByLoader.put(bootClassLoader, types);
//            for (Iterator<Class> it = types.iterator(); it.hasNext();) {
//                Class type = it.next();
//                if (!discoveredCacheByClass.containsExactKey(type)) {
//                    if (type.isInterface()
//                            || (type.getModifiers() & Modifier.ABSTRACT) != 0) {
//                        LOG.with().session(session).level(Level.WARNING).verb( NutsLogVerb.WARNING).formatted()
//                        .log("abstract type {0} is defined as implementation. Ignored.", type.getName());
//                        it.remove();
//                    } else {
//                        discoveredCacheByClass.add(type);
//                    }
//                }
//            }
//        }
//        return Collections.unmodifiableList(types);
//    }
    @Override
    public Set<Class> getExtensionTypes(Class type, NutsSession session) {
        LinkedHashSet<Class> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypes(type));
        }
        return all;
    }
//    @Override
//    public <T> List<T> discoverInstances(Class<T> type) {
//        List<Class> types = discoverTypes(type, bootClassLoader);
//        List<T> valid = new ArrayList<>();
//        for (Class t : types) {
//            valid.add((T) instantiate0(t));
//        }
//        return valid;
//    }

    @Override
    public boolean isRegisteredInstance(Class extensionPoint, Object implementation, NutsSession session) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, Class implementation, NutsSession session) {
        return getExtensionTypes(extensionPoint, session).contains(implementation);
    }

    public Class findRegisteredType(Class extensionPoint, String implementation, NutsSession session) {
        for (Class cls : getExtensionTypes(extensionPoint, session)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, String implementation, NutsSession session) {
        return findRegisteredType(extensionPoint, implementation, session) != null;
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(workspace, session);
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation, NutsSession session) {
        checkSession(session);
        if (isRegisteredInstance(extensionPoint, implementation, session)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("already registered Extension %s for %s", implementation, extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                    .log("bind    {0} for impl instance {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getClass().getName());
        }
        instances.add(extensionPoint, implementation);
    }

//    @Override
//    public Set<Class> getExtensionPoints() {
//        HashSet<Class> s = new HashSet<>();
//        for (IdCache c : discoveredCacheById.values()) {
//            s.addAll(c.getExtensionPoints());
//        }
//        return s;
//    }
    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return new ArrayList<>(instances.getAll(extensionPoint));
    }

    private Object resolveClassSource(Class implementation) {
        return null;
    }

    @Override
    public void registerType(Class extensionPoint, Class implementation, NutsId source, NutsSession session) {
        checkSession(session);
        if (isRegisteredType(extensionPoint, implementation.getName(), session)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("already registered Extension %s for %s", implementation.getName(), extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.UPDATE).formatted()
                    .log("bind    {0} for impl type {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getName());
        }
        IdCache t = discoveredCacheById.get(source);
        if (t == null) {
            t = new IdCache(source);
            discoveredCacheById.put(source, t);
        }
        ClassClassMap y = t.getClassClassMap(NutsComponent.class, true);
        if (!y.containsExactKey(implementation)) {
            y.add(implementation);
        }
    }

//    public void unregisterType(Class extensionPoint, Class implementation) {
//        Class registered = findRegisteredType(extensionPoint, implementation.getName());
//        if (registered != null) {
//            if (LOG.isLoggable(Level.FINEST)) {
//                LOG.with().session(session).level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
//                .log("unbind  {0} for __impl type__ {1}", extensionPoint, registered.getName());
//            }
//            ClassExtension found = classes.getAll(extensionPoint).stream().filter(x->x.clazz.equals(registered)).findFirst().orElse(null);
//            if(found!=null) {
//                classes.remove(extensionPoint, found);
//            }
//        }
//    }
//    public void unregisterType(Class extensionPoint, String implementation) {
//        Class registered = findRegisteredType(extensionPoint, implementation);
//        if (registered != null) {
//            if (LOG.isLoggable(Level.FINEST)) {
//                LOG.with().session(session).level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
//                .log("unbind  Unregistering {0} for __impl type__ {1}", extensionPoint, registered.getName());
//            }
//            ClassExtension found = classes.getAll(extensionPoint).stream().filter(x->x.clazz.equals(registered)).findFirst().orElse(null);
//            if(found!=null) {
//                classes.remove(extensionPoint, found);
//            }
//        }
//    }
    protected <T> T instantiate0(Class<T> t, NutsSession session) {
        checkSession(session);
        T theInstance = null;
        try {
            theInstance = t.newInstance();
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).formatted().error(e)
                        .log("unable to instantiate {0}", t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(session, cause);
        } catch (IllegalAccessException e) {
            throw new NutsFactoryException(session, e);
        }
        //initialize?
        return theInstance;
    }

    protected <T> T instantiate0(Class<T> t, Class[] argTypes, Object[] args, NutsSession session) {
        checkSession(session);
        T t1 = null;
        try {
            t1 = t.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).formatted().error(e)
                        .log("unable to instantiate {0}", t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(session, cause);
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).formatted().error(e)
                        .log("unable to instantiate {0}", t);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new NutsFactoryException(session, e);
        }
        //initialize?
        return t1;
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType, NutsSession session) {
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type, session);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.READ).formatted()
                            .log("resolve {0} to  ```underlined singleton``` {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type, session);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.READ).formatted()
                        .log("resolve {0} to  ```underlined prototype``` {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType, Class[] argTypes, Object[] args, NutsSession session) {
        checkSession(session);
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            if (argTypes.length > 0) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("singletons should have no arg types"));
            }
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type, session);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.READ).formatted()
                            .log("resolve {0} to  ```underlined singleton``` {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type, argTypes, args, session);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.READ).formatted()
                        .log("resolve {0} to  ```underlined prototype``` {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    //    @Override
    public <T> T create(Class<T> type, NutsSession session) {
        checkSession(session);
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.READ).formatted()
                        .log("resolve {0} to singleton {1}", CoreStringUtils.alignLeft(type.getSimpleName(), 40), one.getClass().getName());
            }
            return (T) one;
        }
        Set<Class> extensionTypes = getExtensionTypes(type, session);
        for (Class e : extensionTypes) {
            return (T) resolveInstance(e, type, session);
        }
        for (Class<T> t : extensionTypes) {
            return (T) instantiate0(t, session);
        }
        throw new NutsElementNotFoundException(session, NutsMessage.cstyle("type %s not found",type));
    }

    @Override
    public <T> List<T> createAll(Class<T> type, NutsSession session) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        LinkedHashSet<Class> allTypes = new LinkedHashSet<>();
        allTypes.addAll(getExtensionTypes(type, session));
        for (Class c : allTypes) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, session);
            } catch (Exception e) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).formatted().error(e)
                        .log("unable to instantiate {0} for {1} : {2}", c, type, e);
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    public <T> List<T> createAll(Class<T> type, Class[] argTypes, Object[] args, NutsSession session) {
        List<T> all = new ArrayList<T>();
        for (Class c : getExtensionTypes(type, session)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args, session);
            } catch (Exception e) {
                LOG.with().session(session).level(Level.WARNING).verb(NutsLogVerb.FAIL).formatted().error(e)
                        .log("unable to instantiate {0} for {1} : {2}", c, type, e);
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
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, NutsSession session) {
        List<T> list = createAll(type, constructorParameterTypes, constructorParameters, session);
        int bestSupportLevel = Integer.MIN_VALUE;
        NutsSupportLevelContext<V> lc = new NutsDefaultSupportLevelContext<V>(session, supportCriteria);
        T bestObj = null;
        for (T t : list) {
            int supportLevel = t.getSupportLevel(lc);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("not found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"missing Implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, NutsSession session) {
        List<T> list = createAll(type, session);
        int bestSupportLevel = Integer.MIN_VALUE;
        T bestObj = null;
        NutsSupportLevelContext<V> context = new NutsDefaultSupportLevelContext<>(session, supportCriteria);
        for (T t : list) {
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("not found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"missing implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria, NutsSession session) {
        List<T> list = createAll(type, session);
        NutsDefaultSupportLevelContext<V> context = new NutsDefaultSupportLevelContext<>(session, supportCriteria);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel <= 0) {
                iterator.remove();
            }
        }
        return list;
    }

}
