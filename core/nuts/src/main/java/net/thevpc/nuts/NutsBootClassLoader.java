package net.thevpc.nuts;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Simple Implementation of Nuts BootClassLoader
 * @category SPI Base
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
