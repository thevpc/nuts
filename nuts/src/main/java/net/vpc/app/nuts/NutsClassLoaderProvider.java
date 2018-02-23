package net.vpc.app.nuts;

/**
 * NutsClassLoaderProvider is responsible of resolving the actual Class Loader for loading ClassWorldURLs.
 * ClassWorldURLs are urls of nuts-core dependencies (as a parent loader).
 */
public interface NutsClassLoaderProvider {

    /**
     * resolves to actual context aware ClassLoader to use as a parent of created classloaders
     * @return context class loader
     */
    ClassLoader getContextClassLoader();
}
