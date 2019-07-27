package net.vpc.app.nuts;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Simple Implementation of Nuts BootClassLoader
 */
class NutsBootClassLoader extends URLClassLoader {

    /**
     * default constructor
     * @param urls urls
     * @param parent parent class loader
     */
    NutsBootClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
