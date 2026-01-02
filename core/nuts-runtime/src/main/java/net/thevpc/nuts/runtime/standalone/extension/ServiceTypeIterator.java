package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.spi.NComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceTypeIterator<T> implements Iterator<Class<? extends T>>, AutoCloseable {
    private Class<T> serviceType;
    private ClassLoader classLoader;
    private boolean finished;
    private URL currentUrl;
    private Enumeration<URL> urls;
    private ClassLoader actualClassLoader;
    private BufferedReader br;
    private Class currentType;
    private final Set<Class<?>> seenTypes = new HashSet<>();
    private final Set<String> seenNames = new HashSet<>();

    public static <T> List<Class<? extends T>> loadList(Class<T> serviceType, ClassLoader classLoader) {
        List<Class<? extends T>> all = new ArrayList<>();
        try (ServiceTypeIterator<T> it = new ServiceTypeIterator<>(serviceType, classLoader)) {
            while (it.hasNext()) {
                all.add(it.next());
            }
        }
        return all;
    }

    public ServiceTypeIterator(Class<T> serviceType, ClassLoader classLoader) {
        this.serviceType = serviceType;
        this.classLoader = classLoader;
        actualClassLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    @Override
    public boolean hasNext() {
        if (currentType != null) {
            return true;
        }
        while (true) {
            if (currentUrl == null) {
                if (urls == null) {
                    Enumeration<URL> e1 = createEnumeration(serviceType, actualClassLoader);
                    Enumeration<URL> e2 = null;
                    if (NComponent.class.isAssignableFrom(serviceType) && !serviceType.equals(NComponent.class)) {
                        e2 = createEnumeration(NComponent.class, actualClassLoader);
                    }
                    urls = concatUnique(e1, e2);
                }
                if (urls.hasMoreElements()) {
                    currentUrl = urls.nextElement();
                    if (currentUrl == null) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            if (br == null) {
                try {
                    br = new BufferedReader(new InputStreamReader(currentUrl.openStream(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    return false;
                }
            }
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    if (!seenNames.add(line)) {
                        continue;
                    }
                    try {
                        Class<?> c = Class.forName(line, false, actualClassLoader);
                        if (serviceType.isAssignableFrom(c)) {
                            if (seenTypes.add(c)) {
                                currentType = (Class<? extends T>) c;
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                        //
                    }
                }
                br.close();
                br = null;
                currentUrl = null;
            } catch (IOException ex) {
                // just ignore?
                try {
                    br.close();
                } catch (IOException e) {
                    ///
                }
                br = null;
                currentUrl = null;
            }
        }
    }

    @Override
    public Class<? extends T> next() {
        Class<? extends T> r = currentType;
        currentType = null;
        return r;
    }

    @Override
    public void close() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                ///
            }
            br = null;
        }
        currentUrl = null;
    }

    public static Enumeration<URL> createEnumeration(Class<?> serviceType, ClassLoader classLoader) {
        try {
            return classLoader.getResources("META-INF/services/" + serviceType.getName());
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> Enumeration<T> concatUnique(
            Enumeration<? extends T> e1,
            Enumeration<? extends T> e2
    ) {
        return new Enumeration<T>() {

            private final Set<T> seen = new HashSet<>();
            private Enumeration<? extends T> current = e1 != null ? e1 : e2;
            private Enumeration<? extends T> next = e1 != null ? e2 : null;
            private T nextElement;
            private boolean prepared;

            private void prepare() {
                if (prepared) return;
                prepared = true;

                while (current != null) {
                    while (current.hasMoreElements()) {
                        T candidate = current.nextElement();
                        if (seen.add(candidate)) {
                            nextElement = candidate;
                            return;
                        }
                    }
                    current = next;
                    next = null;
                }
                nextElement = null;
            }

            @Override
            public boolean hasMoreElements() {
                prepare();
                return nextElement != null;
            }

            @Override
            public T nextElement() {
                prepare();
                if (nextElement == null) {
                    throw new NoSuchElementException();
                }
                T result = nextElement;
                prepared = false;
                nextElement = null;
                return result;
            }
        };
    }
}
