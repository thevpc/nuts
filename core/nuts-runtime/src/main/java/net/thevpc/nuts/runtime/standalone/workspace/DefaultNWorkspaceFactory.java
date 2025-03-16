/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElementNotFoundException;

import net.thevpc.nuts.ext.NFactoryException;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.reserved.rpi.NCollectionsRPI;
import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.app.NAppImpl;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElements;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectFormat;
import net.thevpc.nuts.runtime.standalone.format.NFormatsImpl;
import net.thevpc.nuts.runtime.standalone.id.format.DefaultNIdFormat;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIO;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIORPI;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogs;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.NClassClassMap;
import net.thevpc.nuts.util.NListValueMap;
import net.thevpc.nuts.runtime.standalone.util.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.stream.DefaultNCollectionsRPI;
import net.thevpc.nuts.runtime.standalone.version.format.DefaultNVersionFormat;
import net.thevpc.nuts.runtime.standalone.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmd;
import net.thevpc.nuts.runtime.standalone.workspace.factorycache.CachedConstructor;
import net.thevpc.nuts.runtime.standalone.workspace.factorycache.NBeanCache;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNLibPaths;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.web.NWebCli;

import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNWorkspaceFactory implements NWorkspaceFactory {

    private final NLog LOG;
    private final NListValueMap<Class<?>, Object> instances = new NListValueMap<>();
    private final Map<NId, IdCache> discoveredCacheById = new HashMap<>();
    private final HashMap<String, String> _alreadyLogger = new HashMap<>();
    private final NWorkspace workspace;
    private final NBeanCache cache;

    public DefaultNWorkspaceFactory(NWorkspace ws) {
        this.workspace = ws;
        LOG = ((DefaultNWorkspace) ws).LOG;
        cache = new NBeanCache(LOG, CoreNUtils.isDevVerbose() ? System.err : null);
    }

    @Override
    public Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader) {
        return discoverTypes(id, url, bootClassLoader, new Class[]{NComponent.class});
    }

    @Override
    public Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class<? extends NComponent>[] extensionPoints) {
        if (!discoveredCacheById.containsKey(id)) {
            IdCache value = new IdCache(id, url, bootClassLoader, LOG, extensionPoints, workspace);
            discoveredCacheById.put(id, value);
            Set<Class<? extends NComponent>> all = new HashSet<>();
            for (NClassClassMap m : value.classes.values()) {
                Collection<Class<? extends NComponent>> values = (Collection) m.values();
                all.addAll(values);
            }
            return all;
        }
        return Collections.emptySet();
    }

    @Override
    public <T extends NComponent> NOptional<T> createComponent(Class<T> type, Object supportCriteria) {
        NSession session = workspace.currentSession();
        NSupportLevelContext context = new NDefaultSupportLevelContext(supportCriteria);
        // should handle NApp specifically because It's the root for resolving scoped properties
        // TODO should it, or should it not??

        switch (type.getName()) {
            case "net.thevpc.nuts.NApp": {
                return NOptional.of((T) ((DefaultNSession) session).getPropertiesHolder().getOrComputeProperty(type.getName(), () -> new NAppImpl(), NScopeType.TRANSITIVE_SESSION));
            }
        }
        List<T> all = createAll(type);
        NCallableSupport<T> s = NCallableSupport.resolve(all.stream().map(x -> NCallableSupport.of(x.getSupportLevel(context), x)),
                () -> NMsg.ofMissingValue(NMsg.ofC("extensions component %s", type).toString())
        );
        if (!s.isValid()) {
            //fallback needed in bootstrap or if the extensions are broken!
            switch (type.getName()) {
                case "net.thevpc.nuts.log.NLogs": {
                    DefaultNLogs p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNLogs(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.text.NTexts": {
                    DefaultNTexts p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNTexts(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.format.NObjectFormat": {
                    DefaultNObjectFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNObjectFormat(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NIO": {
                    DefaultNIO p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.WORKSPACE, () -> new DefaultNIO(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.elem.NElements": {
                    DefaultNElements p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNElements(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NLibPaths": {
                    DefaultNLibPaths p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNLibPaths(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NDigest": {
                    NDigest p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNDigest(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.reserved.rpi.NIORPI": {
                    NIORPI p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNIORPI(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.reserved.rpi.NCollectionsRPI": {
                    NCollectionsRPI p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNCollectionsRPI(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NIdFormat": {
                    NIdFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNIdFormat());
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.format.NVersionFormat": {
                    NVersionFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNVersionFormat(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NExecCmd": {
                    NExecCmd p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNExecCmd(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.web.NWebCli": {
                    NWebCli p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNWebCli());
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NIdBuilder": {
                    return NOptional.of((T) new DefaultNIdBuilder());
                }
                case "net.thevpc.nuts.NDependencyBuilder": {
                    return NOptional.of((T) new DefaultNDependencyBuilder());
                }
                case "net.thevpc.nuts.NEnvConditionBuilder": {
                    return NOptional.of((T) new DefaultNEnvConditionBuilder());
                }
                case "net.thevpc.nuts.NDescriptorBuilder": {
                    return NOptional.of((T) new DefaultNDescriptorBuilder());
                }
                case "net.thevpc.nuts.NBootOptionsBuilder": {
                    return NOptional.of((T) new DefaultNBootOptionsBuilder());
                }
                case "net.thevpc.nuts.NWorkspaceOptionsBuilder": {
                    return NOptional.of((T) new DefaultNWorkspaceOptionsBuilder());
                }
                case "net.thevpc.nuts.format.NFormats": {
                    NFormats p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.WORKSPACE, () -> new NFormatsImpl(workspace));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NApp": {
                    return NOptional.of((T) new NAppImpl());
                }
                default: {
                    //wont use NLog because not yet initialized!
                    //System.err.println("[Nuts] createComponent failed for :" + type.getName());
                }
            }
            if (all.isEmpty()) {
                if (!session.isBot()) {
                    System.err.println("[Nuts] unable to resolve " + type);
                    Set<Class<? extends T>> extensionTypes = getExtensionTypes(type);
                    System.err.println("[Nuts] extensionTypes =  " + extensionTypes);
                    dump(type, session);
                    new Throwable().printStackTrace();
                }
            }
        }
        if (all.isEmpty()) {
            if (!session.isBot()) {
                System.err.println("[Nuts] unable to resolve " + type);
                Set<Class<? extends T>> extensionTypes = getExtensionTypes(type);
                System.err.println("[Nuts] extensionTypes =  " + extensionTypes);
                dump(type, session);
                new Throwable().printStackTrace();
            }
        }
        return s.toOptional();
    }

    @Override
    public <T extends NComponent> List<T> createComponents(Class<T> type, Object supportCriteria) {
        List<T> list = createAll(type);
        class TypeAndLevel {
            final T t;
            final int lvl;

            public TypeAndLevel(T t, int lvl) {
                this.t = t;
                this.lvl = lvl;
            }
        }
        List<TypeAndLevel> r = new ArrayList<>();
        NDefaultSupportLevelContext context = new NDefaultSupportLevelContext(supportCriteria);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel <= 0) {
                iterator.remove();
            } else {
                r.add(new TypeAndLevel(t, supportLevel));
            }
        }
        return r.stream().sorted(Comparator.comparing(x -> -x.lvl)).map(x -> x.t).collect(Collectors.toList());
    }

    @Override
    public <T extends NComponent> List<T> createAll(Class<T> type) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        for (Class<? extends T> c : getExtensionTypes(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type);
            } catch (Exception e) {
                LOG.with().level(Level.FINEST).verb(NLogVerb.FAIL).error(e)
                        .log(NMsg.ofJ("error while instantiating {0} for {1} : {2}", c, type, e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    @Override
    public <T extends NComponent> T createFirst(Class<T> type) {
        for (Object obj : instances.getAll(type)) {
            return (T) obj;
        }
        for (Class c : getExtensionTypes(type)) {
            return (T) resolveInstance(c, type);
        }
        return null;
    }

    @Override
    public <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> type) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypes(type));
        }
        return all;
    }

    private <T extends NComponent> Set<Class<? extends T>> getExtensionTypesNoCache(Class<T> type, NSession session) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypesNoCache(type));
        }
        return all;
    }

    private <T extends NComponent> Set<Class<? extends T>> getExtensionTypesNoCache2(Class<T> type, NSession session) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypesNoCache2(type));
        }
        return all;
    }

    @Override
    public <T extends NComponent> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return new ArrayList<T>((List) instances.getAll(extensionPoint));
    }

    @Override
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPoint, String implementation) {
        return findRegisteredType(extensionPoint, implementation) != null;
    }

    @Override
    public <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPoint, T implementation) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public <T extends NComponent> void registerInstance(Class<T> extensionPoint, T implementation) {
        if (isRegisteredInstance(extensionPoint, implementation)) {
            throw new NIllegalArgumentException(NMsg.ofC("already registered Extension %s for %s", implementation, extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("bind    {0} for impl instance {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                            implementation.getClass().getName()));
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public <T extends NComponent> void registerType(Class<T> extensionPoint, Class<? extends T> implementationType, NId source) {
        if (isRegisteredType(extensionPoint, implementationType.getName())) {
            throw new NIllegalArgumentException(NMsg.ofC("already registered Extension %s for %s", implementationType.getName(), extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("bind    {0} for impl type {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                            implementationType.getName()));
        }
        IdCache t = discoveredCacheById.get(source);
        if (t == null) {
            t = new IdCache(source, workspace);
            discoveredCacheById.put(source, t);
        }
        t.add(NComponent.class, implementationType);
    }

    @Override
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPoint, Class<? extends T> implementationType) {
        return getExtensionTypes(extensionPoint).contains(implementationType);
    }

    public <T extends NComponent> Class<? extends T> findRegisteredType(Class<T> extensionPoint, String implementation) {
        for (Class<? extends T> cls : getExtensionTypes(extensionPoint)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }


//    private Object resolveClassSource(Class implementation) {
//        return null;
//    }


    public <T extends NComponent> T newInstance(Class<T> t, Class<? super T> apiType) {
        return newInstance(t, new Class[0], new Object[0], apiType);
    }

    protected <T extends NComponent> T newInstanceAndLog(Class<? extends T> implementation, Class<?>[] argTypes, Object[] args, Class<T> apiType, NScopeType scope) {
        T o = newInstance(implementation, apiType);
//        if (LOG.isLoggable(Level.CONFIG)) {
//            LOG.with().level(Level.FINEST).verb(NLogVerb.READ)
//                    .log(NMsg.ofJ("resolve {0} to  ```underlined {1}``` {2}",
//                            NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST),
//                            scope,
//                            o.getClass().getName()));
//        }

        //skip logging this to avoid infinite recursion
        if (isBootstrapLogType(apiType)) {
            //
        } else if (LOG.isLoggable(Level.CONFIG)) {
            String old = _alreadyLogger.get(apiType.getName());
            if (old == null || !old.equals(implementation.getName())) {
                _alreadyLogger.put(apiType.getName(), implementation.getName());
                LOG.with().level(Level.FINEST).verb(NLogVerb.READ)
                        .log(NMsg.ofC("resolve %s to  %s %s",
                                NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST),
                                scope,
                                implementation.getName()
                        ));
            }
        }

        return o;
    }

    protected <T> T newInstance(Class<T> t, Class[] argTypes, Object[] args, Class apiType) {
        T t1 = null;
        NSession session = workspace.currentSession();
        CachedConstructor<T> ctrl0 = cache.findConstructor(t, argTypes);
        if (ctrl0 == null) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("error when instantiating %s as %s : no constructor found", apiType, t), null);
            } else {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().level(Level.FINEST).verb(NLogVerb.FAIL).error(null)
                            .log(NMsg.ofC("error when instantiating %s as %s : no constructor found", apiType, t));
                }
            }
            NBeanCache cache2 = new NBeanCache(LOG, CoreNUtils.isDevVerbose() ? System.err : null);
            cache2.findConstructor(t, argTypes);
            throw new NFactoryException(NMsg.ofC("instantiate '%s' failed", t), new NoSuchElementException(
                    NMsg.ofC("No constructor was found %s(%s). All %s available constructors are : %s",
                            t.getName(),
                            Arrays.stream(argTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                            t.getDeclaredConstructors().length,
                            Arrays.stream(t.getDeclaredConstructors()).map(x -> toString()).collect(Collectors.joining(" ; "))
                    ).toString()
            ));
        }
        try {
            t1 = ctrl0.newInstance(args, session);
        } catch (Exception e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("error when instantiating %s as %s : %s", apiType, t, e), e);
            } else {

                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().level(Level.FINEST).verb(NLogVerb.FAIL).error(e)
                            .log(NMsg.ofC("error when instantiating %s as %s : %s", apiType, t, e));
                }
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NFactoryException(NMsg.ofC("error when instantiating %s as %s : %s", apiType, t, e), cause);
        }
        //initialize?
        return t1;
    }

    protected <T extends NComponent> T resolveInstance(Class<? extends T> implType, Class<T> apiType) {
        return resolveInstance(implType, apiType, new Class[0], new Object[0]);
    }

    private <T extends NComponent> NScopeType computeScope(Class<? extends T> implType, Class<T> apiType) {
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
                    if (LOG.isLoggable(Level.CONFIG)) {
                        switch (apiType.getName()) {
                            //skip logging for NTexts to avoid infinite recursion
                            case "net.thevpc.nuts.text.NTexts": {
                                break;
                            }
                            default: {
                                LOG.with().level(Level.FINEST).verb(NLogVerb.FAIL)
                                        .log(NMsg.ofJ("invalid scope {0} ; expected {1} for  {2}",
                                                implScope.value(),
                                                apiScope.value(),
                                                implType.getName()
                                        ));
                            }
                        }
                    }
                }
            }
        }
        return scope;
    }

    public void safeLog(NMsg msg, Throwable any) {
        //TODO: should we use boot stdio?
        PrintStream err = NWorkspaceExt.of().getModel().bootModel.getBootTerminal().getErr();
        if (err == null) {
            err = System.err;
        }
        err.println(msg.toString() + ":");
        any.printStackTrace();
    }

    public boolean isBootstrapLogType(Class apiType) {
        switch (apiType.getName()) {
            //skip logging this to avoid infinite recursion
            case "net.thevpc.nuts.io.NPaths":
            case "net.thevpc.nuts.text.NTexts":
            case "net.thevpc.nuts.log.NLogs":
            case "net.thevpc.nuts.log.NLog":
            case "net.thevpc.nuts.log.NLogOp": {
                return true;
            }
        }
        return false;
    }

    protected <T extends NComponent> T resolveInstance(Class<? extends T> implementation, Class<T> apiType, Class<?>[] argTypes, Object[] args) {
        if (implementation == null) {
            return null;
        }
        NScopeType scope = computeScope(implementation, apiType);
        if (apiType.getAnnotation(NComponentScope.class) != null) {
            scope = apiType.getAnnotation(NComponentScope.class).value();
        }
        if (scope == null) {
            scope = NScopeType.PROTOTYPE;
        }
        NScopeType finalScope = scope;
        if (scope == NScopeType.PROTOTYPE) {
            return newInstanceAndLog(implementation, argTypes, args, apiType, finalScope);
        }
        NPropertiesHolder beans = resolveBeansHolder(scope);
        return (T) beans.getOrComputeProperty(implementation.getName(), () -> {
            return newInstanceAndLog(implementation, argTypes, args, apiType, finalScope);
        }, finalScope);
    }

    private static NPropertiesHolder resolveBeansHolder(NScopeType scope) {
        return NApp.of().getOrComputeProperty(NWorkspaceFactory.class.getName() + "::beans", scope, () -> new NPropertiesHolder());
    }

    //    @Override
    public <T extends NComponent> T create(Class<T> type, NSession session) {
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb(NLogVerb.READ)
                        .log(NMsg.ofJ("resolve {0} to singleton {1}", NStringUtils.formatAlign(type.getSimpleName(), 40, NPositionType.FIRST), one.getClass().getName()));
            }
            return (T) one;
        }
        Set<Class<? extends T>> extensionTypes = this.getExtensionTypes(type);
        for (Class<? extends T> e : extensionTypes) {
            return (T) resolveInstance(e, type);
        }
        for (Class<? extends T> t : extensionTypes) {
            return newInstance(t, type);
        }
        throw new NElementNotFoundException(NMsg.ofC("type %s not found", type));
    }

    public <T extends NComponent> List<T> createAll(Class<T> type, Class<?>[] argTypes, Object[] args, NSession session) {
        List<T> all = new ArrayList<T>();
        for (Class<? extends T> c : getExtensionTypes(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args);
            } catch (Exception e) {
                LOG.with().level(Level.WARNING).verb(NLogVerb.FAIL).error(e)
                        .log(NMsg.ofC("error when instantiating %s : %s", type, e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }


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

    public void dump(Class<?> type, NSession session) {
        System.err.println("Start Extensions Factory Dump");
        String tname = type.getName();
        for (Map.Entry<NId, IdCache> e : discoveredCacheById.entrySet()) {
            IdCache idCache = e.getValue();
            System.err.println("\t" + e.getKey() + " :: " + idCache.url);
            for (Map.Entry<Class<?>, NClassClassMap> v : idCache.classes.entrySet()) {
                NClassClassMap vv = v.getValue();
                Set<Class> classes = vv.allKeySet();
                for (Class k : classes) {
                    if (k.isInterface()) {
                        if (k.getName().equals(tname)) {
                            if (k.equals(type)) {
                                System.err.println("\t\t --->  " + k + "->" + vv.get(k));
                            } else {
                                System.err.println("\t\t --->  " + k + "->" + vv.get(k) + " ::: class loader : found " + k.getClassLoader() + " __VS__ expected " + type.getClassLoader());
                            }
                            System.err.println("\t\t\t --->  " + type + "->" + vv.get(type) + " ::: class loader : found " + k.getClassLoader() + " __VS__ expected " + type.getClassLoader());
                            System.err.println("\t\t\t\t --->  getAll => " + type + "->" + Arrays.asList(vv.getAll(type)));
                            System.err.println("\t\t\t\t --->  getExtensionTypes => " + getExtensionTypes((Class) type));
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache => " + getExtensionTypesNoCache((Class) type, session));
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache2 => " + getExtensionTypesNoCache2((Class) type, session));
                        } else {
                            System.err.println("\t\t" + k + "->" + vv.get(k));
                        }
                    }
                }
                for (Class k : classes) {
                    if (!k.isInterface()) {
                        if (k.getName().equals(tname)) {
                            if (k.equals(type)) {
                                System.err.println("\t\t --->  " + k + "->" + vv.get(k));
                            } else {
                                System.err.println("\t\t --->  " + k + "->" + vv.get(k) + " ::: class loader : found " + k.getClassLoader() + " __VS__ expected " + type.getClassLoader());
                            }
                            System.err.println("\t\t\t --->  " + type + "->" + vv.get(type) + " ::: class loader : found " + k.getClassLoader() + " __VS__ expected " + type.getClassLoader());
                        } else {
                            System.err.println("\t\t" + k + "->" + vv.get(k));
                        }
                    }
                }
            }
        }
        System.err.println("Finish Extensions Factory Dump");
    }


}
