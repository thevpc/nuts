package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.extension.CoreServiceUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ClassClassMap;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

class IdCache {

    private final NId id;
    final Map<Class<?>, ClassClassMap> classes = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> cacheExtensionTypes = new HashMap<>();
    private final NWorkspace workspace;
    URL url;

    public IdCache(NId id, NWorkspace workspace) {
        this.id = id;
        this.workspace = workspace;
    }

    public IdCache(NId id, URL url, ClassLoader bootClassLoader, NLog LOG, Class<?>[] extensionPoints, NSession session, NWorkspace workspace) {
        NAssert.requireNonBlank(extensionPoints, "extensionPoints");
        if((id==null)!=(url==null)){
            NAssert.requireNonNull(id,"id");
            NAssert.requireNonNull(url,"url");
        }
        Set<String> extensionPointStrings = Arrays.stream(extensionPoints).map(x -> x.getName()).collect(Collectors.toSet());
        this.id = id;
        this.url = url;
        this.workspace = workspace;
        NLogOp lop = LOG.with().session(validLogSession(session));
        Class<NComponent> serviceClass = NComponent.class;
        NRef<Boolean> logStart = NRef.of(false);
        int count = 0;
        if (url != null) {
            for (String n : CoreServiceUtils.loadZipServiceClassNames(url, serviceClass, session)) {
                count += _addOne(n, bootClassLoader, extensionPoints, lop, session, logStart, serviceClass);
            }
            lop.verb(NLogVerb.INFO).level(Level.FINE)
                    .log(NMsg.ofC("found %s extensions from %s (id=%s) (classloader %s). looking for %s", count, url, id, bootClassLoader, extensionPointStrings));
        } else {
            for (String n : CoreServiceUtils.loadZipServiceClassNamesFromClassLoader(bootClassLoader, serviceClass, session)) {
                count += _addOne(n, bootClassLoader, extensionPoints, lop, session, logStart, serviceClass);
            }
            lop.verb(NLogVerb.INFO).level(Level.FINE)
                    .log(NMsg.ofC("found %s extensions from classloader %s (id=%s) . looking for %s", count, bootClassLoader, id, extensionPointStrings));
        }
    }

    private int _addOne(String className, ClassLoader bootClassLoader, Class<?>[] extensionPoints, NLogOp lop,
                        NSession session,
                        NRef<Boolean> logStart,
                        Class<?> serviceClass
    ) {
        int count = 0;
        for (Class<?> extensionPoint : extensionPoints) {
            Class<?> c = null;
            try {
                c = Class.forName(className, false, bootClassLoader);
            } catch (ClassNotFoundException x) {
                lop.verb(NLogVerb.WARNING).level(Level.FINE).error(x)
                        .log(NMsg.ofC("not a valid type %s", c));
            }
            if (c != null) {
                if (!logStart.get()) {
                    Set<String> extensionPointStrings = Arrays.stream(extensionPoints).map(x -> x.getName()).collect(Collectors.toSet());
                    if(id==null){
                        lop.verb(NLogVerb.INFO).level(Level.FINE)
                                .log(NMsg.ofC("discover extensions from current classloader %s. looking for %s",bootClassLoader, extensionPointStrings));
                    }else {
                        lop.verb(NLogVerb.INFO).level(Level.FINE)
                                .log(NMsg.ofC("discover extensions from %s (id=%s) (classloader %s). looking for %s", url, id, bootClassLoader, extensionPointStrings));
                    }
                    logStart.set(true);
                }
                if (!serviceClass.isAssignableFrom(c)) {
                    lop.verb(NLogVerb.WARNING).level(Level.FINE)
                            .log(NMsg.ofC("not a valid type %s <> %s, ignore...", c, serviceClass));
                } else {
                    ClassClassMap cc = classes.computeIfAbsent(extensionPoint, r -> new ClassClassMap());
                    cc.add(c);
                    lop.verb(NLogVerb.INFO).level(Level.FINE)
                            .log(NMsg.ofC("discovered %s as %s in %s", c, extensionPoint, url==null?"default classloader":url));
                    count++;
                }
            }
        }
        return count;
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


    void add(Class<?> extensionPoint, Class<?> implementation) {
        ClassClassMap y = getClassClassMap(extensionPoint, true);
        if (!y.containsExactKey(implementation)) {
            y.add(implementation);
            invalidateCache();
        }
    }

    private void invalidateCache() {
        cacheExtensionTypes.clear();
    }

    private ClassClassMap getClassClassMap(Class extensionPoint, boolean create) {
        ClassClassMap r = classes.get(extensionPoint);
        if (r == null && create) {
            r = new ClassClassMap();
            classes.put(extensionPoint, r);
        }
        return r;
    }

    public NId getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }

    public Set<Class<?>> getExtensionPoints() {
        return new LinkedHashSet<>(classes.keySet());
    }

    public <T> Set<Class<? extends T>> getExtensionTypesNoCache(Class<T> extensionPoint) {
        Set<Class<? extends T>> all = new LinkedHashSet<>();
        for (Map.Entry<Class<?>, ClassClassMap> rr : this.classes.entrySet()) {
            if (rr.getKey().isAssignableFrom(extensionPoint)) {
                all.addAll((Collection) Arrays.asList(rr.getValue().getAll(extensionPoint)));
            }
        }
        return all;
    }

    public <T> Set<Class<? extends T>> getExtensionTypesNoCache2(Class<T> extensionPoint) {
        Set<Class<? extends T>> all = new LinkedHashSet<>();
        for (Map.Entry<Class<?>, ClassClassMap> rr : this.classes.entrySet()) {
            all.addAll((Collection) Arrays.asList(rr.getValue().getAll(extensionPoint)));
        }
        return all;
    }

    public <T> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint) {
        Set<Class<? extends T>> r = (Set) cacheExtensionTypes.get(extensionPoint);
        if (r != null) {
            return r;
        }
        r = Collections.unmodifiableSet(getExtensionTypesNoCache(extensionPoint));
        cacheExtensionTypes.put(extensionPoint, (Set) r);
        return r;
    }
}
