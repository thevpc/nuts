package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.extension.CoreServiceUtils;
import net.thevpc.nuts.util.NClassClassMap;
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
    final Map<Class<?>, NClassClassMap> classes = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> cacheExtensionTypes = new HashMap<>();
    private final NWorkspace workspace;
    URL url;

    public IdCache(NId id, NWorkspace workspace) {
        this.id = id;
        this.workspace = workspace;
    }

    public IdCache(NId id, URL url, ClassLoader bootClassLoader, NLog LOG, Class<?>[] extensionPoints, NWorkspace workspace) {
        NAssert.requireNonBlank(extensionPoints, "extensionPoints");
        if ((id == null) != (url == null)) {
            NAssert.requireNonNull(id, "id");
            NAssert.requireNonNull(url, "url");
        }
        Set<String> extensionPointStrings = Arrays.stream(extensionPoints).map(x -> x.getName()).collect(Collectors.toSet());
        this.id = id;
        this.url = url;
        this.workspace = workspace;
        Class<NComponent> serviceClass = NComponent.class;
        NRef<Boolean> logStart = NRef.of(false);
        int count = 0;
        if (url != null) {
            for (String n : CoreServiceUtils.loadZipServiceClassNames(url, serviceClass)) {
                count += _addOne(n, bootClassLoader, extensionPoints, LOG, logStart, serviceClass);
            }
            LOG.log(NMsg.ofC("found %s extensions from %s (id=%s) (classloader %s). looking for %s", count, url, id, bootClassLoader, extensionPointStrings)
                    .withIntent(NMsgIntent.INFO).withLevel(Level.FINE)
            );
        } else {
            for (String n : CoreServiceUtils.loadZipServiceClassNamesFromClassLoader(bootClassLoader, serviceClass)) {
                count += _addOne(n, bootClassLoader, extensionPoints, LOG, logStart, serviceClass);
            }
            LOG.log(NMsg.ofC("found %s extensions from classloader %s (id=%s) . looking for %s", count, bootClassLoader, id, extensionPointStrings)
                    .withIntent(NMsgIntent.INFO).withLevel(Level.FINE)
            );
        }
    }

    private int _addOne(String className, ClassLoader bootClassLoader, Class<?>[] extensionPoints, NLog LOG,
                        NRef<Boolean> logStart,
                        Class<?> serviceClass
    ) {
        int count = 0;
        for (Class<?> extensionPoint : extensionPoints) {
            Class<?> c = null;
            try {
                c = Class.forName(className, false, bootClassLoader);
            } catch (ClassNotFoundException x) {
                LOG.log(NMsg.ofC("not a valid type %s", c).asFineAlert(x));
            }
            if (c != null) {
                if (!logStart.get()) {
                    Set<String> extensionPointStrings = Arrays.stream(extensionPoints).map(x -> x.getName()).collect(Collectors.toSet());
                    if (id == null) {
                        LOG
                                .log(NMsg.ofC("discover extensions from current classloader %s. looking for %s", bootClassLoader, extensionPointStrings)
                                        .withIntent(NMsgIntent.INFO).withLevel(Level.FINE)
                                );
                    } else {
                        LOG
                                .log(NMsg.ofC("discover extensions from %s (id=%s) (classloader %s). looking for %s", url, id, bootClassLoader, extensionPointStrings)
                                        .withIntent(NMsgIntent.INFO).withLevel(Level.FINE)
                                );
                    }
                    logStart.set(true);
                }
                if (!serviceClass.isAssignableFrom(c)) {
                    LOG
                            .log(NMsg.ofC("not a valid type %s <> %s, ignore...", c, serviceClass)
                                    .withIntent(NMsgIntent.ALERT).withLevel(Level.FINE)
                            );
                } else {
                    NClassClassMap cc = classes.computeIfAbsent(extensionPoint, r -> new NClassClassMap());
                    cc.add(c);
                    LOG
                            .log(NMsg.ofC("discovered %s as %s in %s", c, extensionPoint, url == null ? "default classloader" : url)
                                    .withIntent(NMsgIntent.INFO).withLevel(Level.FINE)
                            );
                    count++;
                }
            }
        }
        return count;
    }

    void add(Class<?> extensionPoint, Class<?> implementation) {
        NClassClassMap y = getClassClassMap(extensionPoint, true);
        if (!y.containsExactKey(implementation)) {
            y.add(implementation);
            invalidateCache();
        }
    }

    private void invalidateCache() {
        cacheExtensionTypes.clear();
    }

    private NClassClassMap getClassClassMap(Class extensionPoint, boolean create) {
        NClassClassMap r = classes.get(extensionPoint);
        if (r == null && create) {
            r = new NClassClassMap();
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
        for (Map.Entry<Class<?>, NClassClassMap> rr : this.classes.entrySet()) {
            if (rr.getKey().isAssignableFrom(extensionPoint)) {
                all.addAll((Collection) Arrays.asList(rr.getValue().getAll(extensionPoint)));
            }
        }
        return all;
    }

    public <T> Set<Class<? extends T>> getExtensionTypesNoCache2(Class<T> extensionPoint) {
        Set<Class<? extends T>> all = new LinkedHashSet<>();
        for (Map.Entry<Class<?>, NClassClassMap> rr : this.classes.entrySet()) {
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
