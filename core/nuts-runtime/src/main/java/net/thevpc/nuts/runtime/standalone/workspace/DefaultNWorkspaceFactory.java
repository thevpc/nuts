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

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFormat;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;

import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.concurrent.NConcurrentImpl;
import net.thevpc.nuts.runtime.standalone.extension.*;
import net.thevpc.nuts.runtime.standalone.platform.NEnvLocal;
import net.thevpc.nuts.runtime.standalone.util.FixedNScoredValue;
import net.thevpc.nuts.runtime.standalone.util.NUtilSPIImpl;
import net.thevpc.nuts.text.NFormats;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.text.NVersionFormat;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.app.NAppImpl;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactory;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElements;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectFormat;
import net.thevpc.nuts.runtime.standalone.format.NFormatsImpl;
import net.thevpc.nuts.runtime.standalone.elem.parser.DefaultNElementParser;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultNElementWriter;
import net.thevpc.nuts.runtime.standalone.id.format.DefaultNIdFormat;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIO;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIORPI;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogs;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.runtime.standalone.xtra.expr.DefaultNExprs;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.util.stream.DefaultNCollectionsRPI;
import net.thevpc.nuts.runtime.standalone.version.format.DefaultNVersionFormat;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExec;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNLibPaths;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.net.NWebCli;

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
    private final NWorkspace workspace;
    private final NExtensionTypeInfoPool extensionTypeInfoPool;
    private final NBeanCache beanCache;

    public DefaultNWorkspaceFactory(NWorkspace ws) {
        this.workspace = ws;
        LOG = ((DefaultNWorkspace) ws).getModel().LOG;
        beanCache = NWorkspace.of().getOrComputeProperty(NBeanCache.class, () -> new NBeanCache(LOG));
        extensionTypeInfoPool = new NExtensionTypeInfoPool(LOG,beanCache);
    }

    @Override
    public Set<Class<?>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader) {
        return discoverTypes(id, url, bootClassLoader, new Class[]{NComponent.class});
    }

    @Override
    public Set<Class<?>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class<?>[] extensionPoints) {
        if (!discoveredCacheById.containsKey(id)) {
            IdCache value = new IdCache(id, url, bootClassLoader, LOG, extensionPoints, workspace);
            discoveredCacheById.put(id, value);
            Set<Class<?>> all = new HashSet<>();
            for (NClassClassMap m : value.classes.values()) {
                Collection<Class<?>> values = (Collection) m.values();
                all.addAll(values);
            }
            return all;
        }
        return Collections.emptySet();
    }

    @Override
    public <T> NOptional<T> createComponent(Class<T> type, Object supportCriteria) {
        NSession session = workspace.currentSession();
        // should handle NApp specifically because It's the root for resolving scoped properties
        // TODO should it, or should it not??

        switch (type.getName()) {
            case "net.thevpc.nuts.app.NApp": {
                return NOptional.of((T) ((DefaultNSession) session).getPropertiesHolder().getOrComputeProperty(type.getName(), () -> new NAppImpl(), NScopeType.TRANSITIVE_SESSION));
            }
        }
        NScorableContext context = NScorableContext.of(supportCriteria);
        List<NScoredValue<T>> all = createAllScored(type, context);
        for (NScoredValue<T> a : all) {
            try {
                T y = a.value();
                return NOptional.of(y);
            } catch (Exception e) {
                LOG.log(NMsg.ofJ("error while instantiating {0} for {1} : {2}", a, type, e).asError(e));
            }
        }

        //fallback needed in bootstrap or if the extensions are broken!
        switch (type.getName()) {
            case "net.thevpc.nuts.log.NLogs": {
                DefaultNLogs p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNLogs());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.text.NTexts": {
                DefaultNTexts p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNTexts());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.text.NObjectFormat": {
                DefaultNObjectFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNObjectFormat(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.io.NIO": {
                DefaultNIO p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.WORKSPACE, () -> new DefaultNIO());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElements": {
                DefaultNElements p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNElements());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElementWriter": {
                DefaultNElementWriter p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNElementWriter());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElementParser": {
                DefaultNElementParser p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNElementParser());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElementFactory": {
                DefaultNElementFactory p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNElementFactory());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.io.NLibPaths": {
                DefaultNLibPaths p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNLibPaths(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.io.NDigest": {
                NDigest p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNDigest(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.internal.rpi.NIORPI": {
                NIORPI p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNIORPI(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.internal.rpi.NCollectionsRPI": {
                NCollectionsRPI p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNCollectionsRPI());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.artifact.NIdFormat": {
                NIdFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNIdFormat());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.text.NVersionFormat": {
                NVersionFormat p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNVersionFormat(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.command.NExec": {
                NExec p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNExec(workspace));
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.net.NWebCli": {
                NWebCli p = NApp.of().getOrComputeProperty("fallback::" + type.getName(), NScopeType.SESSION, () -> new DefaultNWebCli());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.artifact.NIdBuilder": {
                return NOptional.of((T) new DefaultNIdBuilder());
            }
            case "net.thevpc.nuts.artifact.NDependencyBuilder": {
                return NOptional.of((T) new DefaultNDependencyBuilder());
            }
            case "net.thevpc.nuts.artifact.NEnvConditionBuilder": {
                return NOptional.of((T) new DefaultNEnvConditionBuilder());
            }
            case "net.thevpc.nuts.artifact.NDescriptorBuilder": {
                return NOptional.of((T) new DefaultNDescriptorBuilder());
            }
            case "net.thevpc.nuts.core.NBootOptionsBuilder": {
                return NOptional.of((T) new DefaultNBootOptionsBuilder());
            }
            case "net.thevpc.nuts.core.NWorkspaceOptionsBuilder": {
                return NOptional.of((T) new DefaultNWorkspaceOptionsBuilder());
            }
            case "net.thevpc.nuts.expr.NExprs": {
                return NOptional.of((T) new DefaultNExprs());
            }
            case "net.thevpc.nuts.text.NFormats": {
                NFormats p = workspace.getOrComputeProperty("fallback::" + type.getName(), () -> new NFormatsImpl());
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.app.NApp": {
                return NOptional.of((T) new NAppImpl());
            }
            case "net.thevpc.nuts.spi.NUtilSPI": {
                return NOptional.of((T) new NUtilSPIImpl());
            }
            case "net.thevpc.nuts.concurrent.NConcurrent": {
                return NOptional.of((T) new NConcurrentImpl());
            }
            case "net.thevpc.nuts.platform.NEnv": {
                if (supportCriteria == null) {
                    NEnv p = workspace.getOrComputeProperty("fallback::" + type.getName(), () -> new NEnvLocal());
                    return NOptional.of((T) p);
                }
                break;
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
                dump(type);
                new Throwable().printStackTrace();
            }
        }

        if (all.isEmpty()) {
            if (!session.isBot()) {
                System.err.println("[Nuts] unable to resolve " + type);
                Set<Class<? extends T>> extensionTypes = getExtensionTypes(type);
                System.err.println("[Nuts] extensionTypes =  " + extensionTypes);
                dump(type);
                new Throwable().printStackTrace();
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("missing %s", type));
    }

    @Override
    public <T> List<T> createComponents(Class<T> type, Object supportCriteria) {
        NScorableContext context = NScorableContext.of(supportCriteria);
        List<NScoredValue<T>> all = createAllScored(type, context);
        List<T> ret = new ArrayList<>();
        for (NScoredValue<T> a : all) {
            try {
                T y = a.value();
                ret.add(y);
            } catch (Exception e) {
                LOG.log(NMsg.ofC("error while instantiating %s for %s : %s", a, type, e).asError(e));
            }
        }
        return ret;
    }

    @Override
    public <T> NScoredValue<T> resolveTypeScore(Class<? extends T> implType, Class<T> apiType, NScorableContext scorableContext) {
        if (implType == null || apiType == null || !apiType.isAssignableFrom(implType)) {
            return FixedNScoredValue.ofUnsupported(implType, apiType);
        }
        return extensionTypeInfoPool.get(implType, apiType).getTypeScoredInstance(scorableContext);
    }

    @Override
    public <T> NScoredValue<T> resolveInstanceScore(T instance, Class<T> apiType, NScorableContext scorableContext) {
        if (instance == null || apiType == null || !apiType.isInstance(instance)) {
            if(instance==null){
                return FixedNScoredValue.ofUnsupported(null,apiType);
            }
            Class<? extends T> implType = (Class<? extends T>) ((T)instance).getClass();
            if(apiType==null){
                return FixedNScoredValue.ofUnsupported(implType,null);
            }
            return FixedNScoredValue.ofUnsupported(implType,apiType);
        }
        if (!apiType.isAssignableFrom(instance.getClass())) {
            return FixedNScoredValue.ofUnsupported(null,apiType);
        }
        if (instance instanceof NScorable) {
            NScorable scorable = (NScorable) instance;
            return new LazyNScoredValueImpl<T>(
                    ()->scorable,
                    ()->instance,
                    scorableContext,
                    (Class)instance.getClass(),
                    apiType
            );
        }
        T o = (T) instance;
        Class<? extends T> c = (Class<? extends T>) o.getClass();
        return extensionTypeInfoPool.get(c, apiType).getTypeScoredInstance(scorableContext);
    }

    @Override
    public <T> NOptional<NScorable> getTypeScorer(Class<? extends T> implType, Class<T> apiType) {
        if (implType == null || apiType == null || !apiType.isAssignableFrom(implType)) {
            return NOptional.ofNamedEmpty("type scorer");
        }
        return NOptional.of(extensionTypeInfoPool.get(implType, apiType).getTypeScorer());
    }

    @Override
    public <T> NOptional<NScorable> getInstanceScorer(T instance, Class<T> apiType) {
        if (instance == null || apiType == null || !apiType.isInstance(instance)) {
            return NOptional.ofNamedEmpty("type scorer");
        }
        T o = (T) instance;
        Class<? extends T> c = (Class<? extends T>) o.getClass();
        return NOptional.of(extensionTypeInfoPool.get(c, apiType).getInstanceScorer(o));
    }

    @Override
    public <T> List<NScoredValue<T>> createAllScored(Class<T> type, NScorableContext supportCriteria) {
        List<NScoredValue<T>> all = new ArrayList<>();
        for (Object obj : instances.getAll(type)) {
            T o = (T) obj;
            int s = getInstanceScorer(o, type).get().getScore(supportCriteria);
            if (s > 0) {
                all.add(new FixedNScoredValue<>(o, s));
            }
        }
        for (Class<? extends T> c : getExtensionTypes(type)) {
            NExtensionTypeInfo<T> tnExtensionType = extensionTypeInfoPool.get(c, type);
            all.add(new LazyNScoredValueImpl<T>(
                    () -> tnExtensionType.getTypeScorer(),
                    () -> tnExtensionType.resolveInstance(new Class[0], new Object[0], new NBeanConstructorContextAsScorableContext(supportCriteria)),
                    supportCriteria,
                    tnExtensionType.getImplType(),
                    tnExtensionType.getApiType()
            ));
        }
        return all.stream().filter(x -> x.isValid()).sorted((a, b) -> Integer.compare(b.score(), a.score())).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        for (Class<? extends T> c : getExtensionTypes(type)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type);
            } catch (Exception e) {
                LOG.log(NMsg.ofJ("error while instantiating {0} for {1} : {2}", c, type, e).asError(e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    @Override
    public <T> T createFirst(Class<T> type) {
        for (Object obj : instances.getAll(type)) {
            return (T) obj;
        }
        for (Class c : getExtensionTypes(type)) {
            return (T) resolveInstance(c, type);
        }
        return null;
    }

    @Override
    public <T> Set<Class<? extends T>> getExtensionTypes(Class<T> type) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypes(type));
        }
        return all;
    }

    private <T> Set<Class<? extends T>> getExtensionTypesNoCache(Class<T> type) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypesNoCache(type));
        }
        return all;
    }

    private <T> Set<Class<? extends T>> getExtensionTypesNoCache2(Class<T> type) {
        LinkedHashSet<Class<? extends T>> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypesNoCache2(type));
        }
        return all;
    }

    @Override
    public <T> List<T> getExtensionObjects(Class<T> extensionPoint) {
        return new ArrayList<T>((List) instances.getAll(extensionPoint));
    }

    @Override
    public <T> boolean isRegisteredType(Class<T> extensionPoint, String implementation) {
        return findRegisteredType(extensionPoint, implementation) != null;
    }

    @Override
    public <T> boolean isRegisteredInstance(Class<T> extensionPoint, T implementation) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation) {
        if (isRegisteredInstance(extensionPoint, implementation)) {
            throw new NIllegalArgumentException(NMsg.ofC("already registered Extension %s for %s", implementation, extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG
                    .log(NMsg.ofJ("bind    {0} for impl instance {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                                    implementation.getClass().getName())
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.ADD)
                    );
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public <T> void registerType(Class<T> extensionPoint, Class<? extends T> implementationType, NId source) {
        if (isRegisteredType(extensionPoint, implementationType.getName())) {
            throw new NIllegalArgumentException(NMsg.ofC("already registered Extension %s for %s", implementationType.getName(), extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG
                    .log(NMsg.ofJ("bind    {0} for impl type {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                                    implementationType.getName())
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.ADD)
                    );
        }
        IdCache t = discoveredCacheById.get(source);
        if (t == null) {
            t = new IdCache(source, workspace);
            discoveredCacheById.put(source, t);
        }
        t.add(NComponent.class, implementationType);
    }

    @Override
    public <T> boolean isRegisteredType(Class<T> extensionPoint, Class<? extends T> implementationType) {
        return getExtensionTypes(extensionPoint).contains(implementationType);
    }

    public <T> Class<? extends T> findRegisteredType(Class<T> extensionPoint, String implementation) {
        for (Class<? extends T> cls : getExtensionTypes(extensionPoint)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }

    public <T> T newInstance(Class<T> t, Class<? super T> apiType) {
        return extensionTypeInfoPool.get(t, apiType).newInstance();
    }


    protected <T> T resolveInstance(Class<? extends T> implType, Class<T> apiType) {
        return extensionTypeInfoPool.get(implType, apiType).resolveInstance(new Class[0], new Object[0],null);
    }

    public void dump(Class<?> type) {
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
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache => " + getExtensionTypesNoCache((Class) type));
                            System.err.println("\t\t\t\t --->  getExtensionTypesNoCache2 => " + getExtensionTypesNoCache2((Class) type));
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

    private static class NBeanConstructorContextAsScorableContext implements NBeanConstructorContext {
        private final NScorableContext supportCriteria;

        public NBeanConstructorContextAsScorableContext(NScorableContext supportCriteria) {
            this.supportCriteria = supportCriteria;
        }

        @Override
        public boolean isSupported(Class<?> paramType) {
            switch (paramType.getName()){
                case "net.thevpc.nuts.util.NScorableContext":
                    return true;
            }
            return false;
        }

        @Override
        public Object resolve(Class<?> paramType) {
            return supportCriteria;
        }
    }
}
