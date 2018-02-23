package net.vpc.app.nuts;

/**
 * Default NutsClassLoaderProvider implementation which points to <code>Thread.currentThread().getContextClassLoader()</code>
 */
public class DefaultNutsClassLoaderProvider implements NutsClassLoaderProvider {

    public static final NutsClassLoaderProvider INSTANCE = new DefaultNutsClassLoaderProvider();

    @Override
    public ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
