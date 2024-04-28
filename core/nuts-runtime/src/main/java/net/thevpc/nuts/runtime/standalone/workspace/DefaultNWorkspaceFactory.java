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
 * <p>
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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.ext.NFactoryException;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NContentTypes;
import net.thevpc.nuts.reserved.rpi.NCollectionsRPI;
import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootManager;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElements;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectFormat;
import net.thevpc.nuts.runtime.standalone.id.format.DefaultNIdFormat;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIO;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIORPI;
import net.thevpc.nuts.runtime.standalone.io.path.DefaultNPaths;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogs;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ClassClassMap;
import net.thevpc.nuts.runtime.standalone.util.collections.ListMap;
import net.thevpc.nuts.runtime.standalone.util.collections.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.stream.DefaultNCollectionsRPI;
import net.thevpc.nuts.runtime.standalone.version.format.DefaultNVersionFormat;
import net.thevpc.nuts.runtime.standalone.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmd;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNImports;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNEnvs;
import net.thevpc.nuts.runtime.standalone.workspace.factorycache.CachedConstructor;
import net.thevpc.nuts.runtime.standalone.workspace.factorycache.NBeanCache;
import net.thevpc.nuts.runtime.standalone.xtra.contenttype.DefaultNContentTypes;
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
    private final ListMap<Class<?>, Object> instances = new ListMap<>();
    private final Map<NId, IdCache> discoveredCacheById = new HashMap<>();
    private final HashMap<String, String> _alreadyLogger = new HashMap<>();
    private final NWorkspace workspace;
    private final NBeanCache cache;

    public DefaultNWorkspaceFactory(NWorkspace ws) {
        this.workspace = ws;
        LOG = ((DefaultNWorkspace) ws).LOG;
        cache = new NBeanCache(LOG, CoreNUtils.isDevVerbose()?System.err:null);
    }

    @Override
    public Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, NSession session) {
        return discoverTypes(id, url, bootClassLoader, new Class[]{NComponent.class}, session);
    }

    @Override
    public Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class<? extends NComponent>[] extensionPoints, NSession session) {
        if (!discoveredCacheById.containsKey(id)) {
            IdCache value = new IdCache(id, url, bootClassLoader, LOG, extensionPoints, session, workspace);
            discoveredCacheById.put(id, value);
            Set<Class<? extends NComponent>> all = new HashSet<>();
            for (ClassClassMap m : value.classes.values()) {
                Collection<Class<? extends NComponent>> values = (Collection) m.values();
                all.addAll(values);
            }
            return all;
        }
        return Collections.emptySet();
    }

    @Override
    public <T extends NComponent> NOptional<T> createComponent(Class<T> type, Object supportCriteria, NSession session) {
        NSupportLevelContext context = new NDefaultSupportLevelContext(session, supportCriteria);
        List<T> all = createAll(type, session);
        NCallableSupport<T> s = NCallableSupport.resolve(all.stream().map(x -> NCallableSupport.of(x.getSupportLevel(context), x)),
                ss -> NMsg.ofMissingValue(NMsg.ofC("extensions component %s", type).toString())
        );
        if (!s.isValid()) {
            //fallback needed in bootstrap or if the extensions are broken!
            switch (type.getName()) {
                case "net.thevpc.nuts.log.NLogs": {
                    DefaultNLogs p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNLogs(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NPaths": {
                    DefaultNPaths p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNPaths(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.text.NTexts": {
                    DefaultNTexts p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNTexts(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NEnvs": {
                    DefaultNEnvs p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNEnvs(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.format.NObjectFormat": {
                    DefaultNObjectFormat p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNObjectFormat(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NIO": {
                    DefaultNIO p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNIO(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NConfigs": {
                    DefaultNConfigs p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNConfigs(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NBootManager": {
                    DefaultNBootManager p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNBootManager(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.elem.NElements": {
                    DefaultNElements p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNElements(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NLibPaths": {
                    DefaultNLibPaths p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNLibPaths(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NDigest": {
                    NDigest p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNDigest(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.io.NContentTypes": {
                    NContentTypes p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNContentTypes(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.reserved.rpi.NIORPI": {
                    NIORPI p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNIORPI(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.reserved.rpi.NCollectionsRPI": {
                    NCollectionsRPI p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNCollectionsRPI(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NIdFormat": {
                    NIdFormat p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNIdFormat(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NVersionFormat": {
                    NVersionFormat p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNVersionFormat(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NImports": {
                    NImports p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNImports(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.NExecCmd": {
                    NExecCmd p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNExecCmd(session));
                    return NOptional.of((T) p);
                }
                case "net.thevpc.nuts.web.NWebCli": {
                    NWebCli p = session.getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, ss -> new DefaultNWebCli(session));
                    return NOptional.of((T) p);
                }
                default: {
                    //wont use NLog because not yet initialized!
                    //System.err.println("[Nuts] createComponent failed for :" + type.getName());
                }
            }
            if (all.isEmpty()) {
                if(!session.isBot()) {
                    System.err.println("[Nuts] unable to resolve " + type);
                    Set<Class<? extends T>> extensionTypes = getExtensionTypes(type, session);
                    System.err.println("[Nuts] extensionTypes =  " + extensionTypes);
                    dump(type, session);
                    new Throwable().printStackTrace();
                }
            }
        }
        if (all.isEmpty()) {
            if(!session.isBot()) {
                System.err.println("[Nuts] unable to resolve " + type);
                Set<Class<? extends T>> extensionTypes = getExtensionTypes(type, session);
                System.err.println("[Nuts] extensionTypes =  " + extensionTypes);
                dump(type, session);
                new Throwable().printStackTrace();
            }
        }
        return s.toOptional();
    }

    @Override
    public <T extends NComponent> List<T> createComponents(Class<T> type, Object supportCriteria, NSession session) {
        List<T> list = createAll(type, session);
        class TypeAndLevel {
            final T t;
            final int lvl;

            public TypeAndLevel(T t, int lvl) {
                this.t = t;
                this.lvl = lvl;
            }
        }
        List<TypeAndLevel> r = new ArrayList<>();
        NDefaultSupportLevelContext context = new NDefaultSupportLevelContext(session, supportCriteria);
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
    public <T extends NComponent> List<T> createAll(Class<T> type, NSession session) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        for (Class<? extends T> c : getExtensionTypes(type, session)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, session);
            } catch (Exception e) {
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.FAIL).error(e)
                        .log(NMsg.ofJ("error while instantiating {0} for {1} : {2}", c, type, e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    @Override
    public <T extends NComponent> T createFirst(Class<T> type, NSession session) {
        for (Object obj : instances.getAll(type)) {
            return (T) obj;
        }
        for (Class c : getExtensionTypes(type, session)) {
            return (T) resolveInstance(c, type, session);
        }
        return null;
    }

    @Override
    public <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> type, NSession session) {
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
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPoint, String implementation, NSession session) {
        return findRegisteredType(extensionPoint, implementation, session) != null;
    }

    @Override
    public <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPoint, T implementation, NSession session) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public <T extends NComponent> void registerInstance(Class<T> extensionPoint, T implementation, NSession session) {
        checkSession(session);
        if (isRegisteredInstance(extensionPoint, implementation, session)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("already registered Extension %s for %s", implementation, extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.ADD)
                    .log(NMsg.ofJ("bind    {0} for impl instance {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                            implementation.getClass().getName()));
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public <T extends NComponent> void registerType(Class<T> extensionPoint, Class<? extends T> implementationType, NId source, NSession session) {
        checkSession(session);
        if (isRegisteredType(extensionPoint, implementationType.getName(), session)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("already registered Extension %s for %s", implementationType.getName(), extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.ADD)
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
    public <T extends NComponent> boolean isRegisteredType(Class<T> extensionPoint, Class<? extends T> implementationType, NSession session) {
        return getExtensionTypes(extensionPoint, session).contains(implementationType);
    }

    public <T extends NComponent> Class<? extends T> findRegisteredType(Class<T> extensionPoint, String implementation, NSession session) {
        for (Class<? extends T> cls : getExtensionTypes(extensionPoint, session)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(workspace, session);
    }

//    private Object resolveClassSource(Class implementation) {
//        return null;
//    }


    public <T extends NComponent> T newInstance(Class<T> t, Class<? super T> apiType, NSession session) {
        checkSession(session);
        return newInstance(t, new Class[0], new Object[0], apiType, session);
    }

    protected <T extends NComponent> T newInstanceAndLog(Class<? extends T> implementation, Class<?>[] argTypes, Object[] args, Class<T> apiType, NSession session, NScopeType scope) {
        T o = newInstance(implementation, apiType, session);
//        if (LOG.isLoggable(Level.CONFIG)) {
//            LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.READ)
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
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.READ)
                        .log(NMsg.ofC("resolve %s to  %s %s",
                                NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST),
                                scope,
                                implementation.getName()
                        ));
            }
        }

        return o;
    }

    protected <T> T newInstance(Class<T> t, Class[] argTypes, Object[] args, Class apiType, NSession session) {
        checkSession(session);
        T t1 = null;
        CachedConstructor<T> ctrl0 = cache.findConstructor(t, argTypes, session);
        if (ctrl0 == null) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("error when instantiating %s as %s : no constructor found", apiType, t), null, session);
            } else {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.FAIL).error(null)
                            .log(NMsg.ofC("error when instantiating %s as %s : no constructor found", apiType, t));
                }
            }
            NBeanCache cache2 = new NBeanCache(LOG,CoreNUtils.isDevVerbose()?System.err:null);
            cache2.findConstructor(t, argTypes, session);
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed", t), new NoSuchElementException(
                    NMsg.ofC("No constructor was found %s(%s). All %s available constructors are : %s",
                            t.getName(),
                            Arrays.stream(argTypes).map(Class::getSimpleName).collect(Collectors.joining(",")),
                            t.getDeclaredConstructors().length,
                            Arrays.stream(t.getDeclaredConstructors()).map(x->toString()).collect(Collectors.joining(" ; "))
                    ).toString()
            ));
        }
        try {
            t1 = ctrl0.newInstance(args, session);
        } catch (Exception e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("error when instantiating %s as %s : %s", apiType, t, e), e, session);
            } else {

                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.FAIL).error(e)
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
            throw new NFactoryException(session, NMsg.ofC("error when instantiating %s as %s : %s", apiType, t, e), cause);
        }
        //initialize?
        return t1;
    }

    protected <T extends NComponent> T resolveInstance(Class<? extends T> implType, Class<T> apiType, NSession session) {
        return resolveInstance(implType, apiType, new Class[0], new Object[0], session);
    }

    private <T extends NComponent> NScopeType computeScope(Class<? extends T> implType, Class<T> apiType, NSession session) {
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
                            //skip logging for NutsTexts to avoid infinite recursion
                            case "net.thevpc.nuts.text.NTexts": {
                                break;
                            }
                            default: {
                                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.FAIL)
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

    public void safeLog(NMsg msg, Throwable any, NSession session) {
        //TODO: should we use boot stdio?
        PrintStream err = NWorkspaceExt.of(session).getModel().bootModel.getBootTerminal().getErr();
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

    private NSession validLogSession(NSession session) {
        if (session == null) {
            //this is a bug
            return NSessionUtils.defaultSession(workspace);
        }
        if (session.getTerminal() == null) {
            //chances are that we are creating the session or the session's Terminal
            return NSessionUtils.defaultSession(workspace);
        }
        return session;
    }

    protected <T extends NComponent> T resolveInstance(Class<? extends T> implementation, Class<T> apiType, Class<?>[] argTypes, Object[] args, NSession session) {
        checkSession(session);
        if (implementation == null) {
            return null;
        }
        NScopeType scope = computeScope(implementation, apiType, session);
        if (apiType.getAnnotation(NComponentScope.class) != null) {
            scope = apiType.getAnnotation(NComponentScope.class).value();
        }
        if (scope == null) {
            scope = NScopeType.PROTOTYPE;
        }
        NScopeType finalScope = scope;
        if (scope == NScopeType.PROTOTYPE) {
            return newInstanceAndLog(implementation, argTypes, args, apiType, session, finalScope);
        }
        NPropertiesHolder beans = resolveBeansHolder(session, scope);
        return (T) beans.getOrComputeProperty(implementation.getName(), session, k -> {
            return newInstanceAndLog(implementation, argTypes, args, apiType, session, finalScope);
        });
    }

    private static NPropertiesHolder resolveBeansHolder(NSession session, NScopeType scope) {
        return session.getOrComputeProperty(NWorkspaceFactory.class.getName() + "::beans", scope, k -> new NPropertiesHolder());
    }

    //    @Override
    public <T extends NComponent> T create(Class<T> type, NSession session) {
        checkSession(session);
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLogVerb.READ)
                        .log(NMsg.ofJ("resolve {0} to singleton {1}", NStringUtils.formatAlign(type.getSimpleName(), 40, NPositionType.FIRST), one.getClass().getName()));
            }
            return (T) one;
        }
        Set<Class<? extends T>> extensionTypes = this.getExtensionTypes(type, session);
        for (Class<? extends T> e : extensionTypes) {
            return (T) resolveInstance(e, type, session);
        }
        for (Class<? extends T> t : extensionTypes) {
            return newInstance(t, type, session);
        }
        throw new NElementNotFoundException(session, NMsg.ofC("type %s not found", type));
    }

    public <T extends NComponent> List<T> createAll(Class<T> type, Class<?>[] argTypes, Object[] args, NSession session) {
        List<T> all = new ArrayList<T>();
        for (Class<? extends T> c : getExtensionTypes(type, session)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args, session);
            } catch (Exception e) {
                LOG.with().session(validLogSession(session)).level(Level.WARNING).verb(NLogVerb.FAIL).error(e)
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

    public void dump(Class<?> type,NSession session) {
        System.err.println("Start Extensions Factory Dump");
        String tname = type.getName();
        for (Map.Entry<NId, IdCache> e : discoveredCacheById.entrySet()) {
            IdCache idCache = e.getValue();
            System.err.println("\t" + e.getKey() + " :: " + idCache.url);
            for (Map.Entry<Class<?>, ClassClassMap> v : idCache.classes.entrySet()) {
                ClassClassMap vv = v.getValue();
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
                            System.err.println("\t\t\t\t --->  getExtensionTypes => " + getExtensionTypes((Class)type,session));
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache => " + getExtensionTypesNoCache((Class)type,session));
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache2 => " + getExtensionTypesNoCache2((Class)type,session));
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
