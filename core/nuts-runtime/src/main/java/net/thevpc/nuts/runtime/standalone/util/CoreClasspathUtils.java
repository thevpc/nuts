package net.thevpc.nuts.runtime.standalone.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreClasspathUtils {
    public static URL[] resolveClasspathURLs(ClassLoader contextClassLoader) {
        List<URL> all = new ArrayList<>();
        if (contextClassLoader != null) {
            if (contextClassLoader instanceof URLClassLoader) {
                all.addAll(Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()));
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }

}
