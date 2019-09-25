/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.main.DefaultNutsWorkspace;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.core.NutsWorkspaceFactory;
import java.lang.reflect.Modifier;
import net.vpc.app.nuts.*;

import java.util.*;
import java.util.logging.Level;

import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.common.ClassClassMap;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.ListMap;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsWorkspaceFactory implements NutsWorkspaceFactory {

    private final NutsLogger LOG;

    private final ListMap<Class, Class> classes = new ListMap<>();
    private final ListMap<Class, Object> instances = new ListMap<>();
    private final Map<Class, Object> singletons = new HashMap<>();
    private final Map<ClassLoader, List<Class>> discoveredCacheByLoader = new HashMap<>();
    private final ClassClassMap discoveredCacheByClass = new ClassClassMap();
    private NutsWorkspace workspace;

    public DefaultNutsWorkspaceFactory(NutsWorkspace ws) {
        this.workspace = ws;
        LOG= ((DefaultNutsWorkspace)ws).LOG;
    }

    @Override
    public List<Class> discoverTypes(ClassLoader bootClassLoader) {
        List<Class> types = discoveredCacheByLoader.get(bootClassLoader);
        if (types == null) {
            types = CoreCommonUtils.loadServiceClasses(NutsComponent.class, bootClassLoader);
            discoveredCacheByLoader.put(bootClassLoader, types);
            for (Iterator<Class> it = types.iterator(); it.hasNext();) {
                Class type = it.next();
                if (!discoveredCacheByClass.containsExactKey(type)) {
                    if (type.isInterface()
                            || (type.getModifiers() & Modifier.ABSTRACT) != 0) {
                        LOG.with().level(Level.WARNING).verb( NutsLogVerb.WARNING).formatted()
                        .log("abstract type {0} is defined as implementation. Ignored.", type.getName());
                        it.remove();
                    } else {
                        discoveredCacheByClass.add(type);
                    }
                }
            }
        }
        return Collections.unmodifiableList(types);
    }

    @Override
    public List<Class> getImplementationTypes(Class type) {
        return Arrays.asList(discoveredCacheByClass.getAll(type));
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
    public boolean isRegisteredInstance(Class extensionPoint, Object implementation) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
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

    @Override
    public boolean isRegisteredType(Class extensionPoint, String implementation) {
        return findRegisteredType(extensionPoint, implementation) != null;
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation) {
        if (isRegisteredInstance(extensionPoint, implementation)) {
            throw new NutsIllegalArgumentException(workspace, "Already Registered Extension " + implementation + " for " + extensionPoint.getName());
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
            .log("bind    {0} for __impl instance__ {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getClass().getName());
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public Set<Class> getExtensionPoints() {
        return new HashSet<>(classes.keySet());
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return new HashSet<>(classes.getAll(extensionPoint));
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return new ArrayList<>(instances.getAll(extensionPoint));
    }

    @Override
    public void registerType(Class extensionPoint, Class implementation) {
        if (isRegisteredType(extensionPoint, implementation.getName())) {
            throw new NutsIllegalArgumentException(workspace, "Already Registered Extension " + implementation.getName() + " for " + extensionPoint.getName());
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
            .log("bind    {0} for __impl type__ {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getName());
        }
        classes.add(extensionPoint, implementation);
    }

    public void unregisterType(Class extensionPoint, Class implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation.getName());
        if (registered != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
                .log("unbind  {0} for __impl type__ {1}", extensionPoint, registered.getName());
            }
            classes.remove(extensionPoint, registered);
        }
    }

    public void unregisterType(Class extensionPoint, String implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation);
        if (registered != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
                .log("unbind  Unregistering {0} for __impl type__ {1}", extensionPoint, registered.getName());
            }
            classes.remove(extensionPoint, registered);
        }
    }

    protected <T> T instantiate0(Class<T> t) {
        T theInstance = null;
        try {
            theInstance = t.newInstance();
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log("unable to instantiate {0}", t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(workspace, cause);
        } catch (IllegalAccessException e) {
            throw new NutsFactoryException(workspace, e);
        }
        //initialize?
        return theInstance;
    }

    protected <T> T instantiate0(Class<T> t, Class[] argTypes, Object[] args) {
        T t1 = null;
        try {
            t1 = t.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0}" , t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(workspace, cause);
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0}",t);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new NutsFactoryException(workspace, e);
        }
        //initialize?
        return t1;
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType) {
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
                o = instantiate0(type);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                            .log("resolve {0} to  __singleton__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to  __prototype__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType, Class[] argTypes, Object[] args) {
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
                throw new NutsIllegalArgumentException(workspace, "Singletons should have no arg types");
            }
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                    .log("resolve {0} to  __singleton__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type, argTypes, args);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to  __prototype__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    //    @Override
    public <T> T create(Class<T> type) {
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to singleton {1}", CoreStringUtils.alignLeft(type.getSimpleName(), 40), one.getClass().getName());
            }
            return (T) one;
        }
        Class oneType = classes.getOne(type);
        if (oneType != null) {
            return (T) resolveInstance(oneType, type);
        }
        for (Class<T> t : getImplementationTypes(type)) {
            return (T) instantiate0(t);
        }
        throw new NutsElementNotFoundException(workspace, "Type " + type + " not found");
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        LinkedHashSet<Class> allTypes = new LinkedHashSet<>();
        allTypes.addAll(classes.getAll(type));
        allTypes.addAll(getImplementationTypes(type));
        for (Class c : allTypes) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type);
            } catch (Exception e) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0} for {1} : {2}" ,c,type, e.getMessage());
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    public <T> List<T> createAll(Class<T> type, Class[] argTypes, Object[] args) {
        List<T> all = new ArrayList<T>();
        for (Class c : classes.getAll(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args);
            } catch (Exception e) {
                LOG.with().level(Level.WARNING).verb( NutsLogVerb.FAIL).formatted().error(e)
                        .log( "unable to instantiate {0} for {1} : {2}" ,c,type, e.getMessage());
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
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        List<T> list = createAll(type, constructorParameterTypes, constructorParameters);
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
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("Not Found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"Missing Implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria) {
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
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("Not Found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"Missing Implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria) {
        List<T> list = createAll(type);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(supportCriteria);
            if (supportLevel <= 0) {
                iterator.remove();
            }
        }
        return list;
    }

}
