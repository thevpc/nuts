package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.net.NConnectionString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class ServiceTypeIterator<T> implements Iterator<Class<? extends T>> , AutoCloseable{
    private Class<T> serviceType;
    private ClassLoader classLoader;
    private boolean finished;
    private URL currentUrl;
    private Enumeration<URL> urls;
    private ClassLoader actualClassLoader;
    private BufferedReader br;
    private Class currentType;

    public static <T> List<Class<? extends T>> loadList(Class<T> serviceType, ClassLoader classLoader) {
        List<Class<? extends T>> all = new ArrayList<>();
        try (ServiceTypeIterator<T> it = new ServiceTypeIterator<>(serviceType,classLoader )) {
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
        while(true) {
            if (currentUrl == null) {
                if (urls == null) {
                    try {
                        urls = actualClassLoader.getResources("META-INF/services/" + serviceType.getName());
                    } catch (IOException e) {
                        urls = new Enumeration<URL>() {
                            @Override
                            public boolean hasMoreElements() {
                                return false;
                            }

                            @Override
                            public URL nextElement() {
                                return null;
                            }
                        };
                    }
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
                    try {
                        Class<?> c = Class.forName(line, false, actualClassLoader);
                        if(serviceType.isAssignableFrom(c)) {
                            currentType = (Class<? extends T>) c;
                        }else{
                            //do some log
                        }
                        return true;
                    } catch (Exception ex) {
                        //
                    }
                }
                br.close();
                br=null;
                currentUrl=null;
            } catch (IOException ex) {
                // just ignore?
                try {
                    br.close();
                } catch (IOException e) {
                    ///
                }
                br=null;
                currentUrl=null;
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
        if(br!=null){
            try {
                br.close();
            } catch (IOException e) {
                ///
            }
            br=null;
        }
        currentUrl=null;
    }
}
