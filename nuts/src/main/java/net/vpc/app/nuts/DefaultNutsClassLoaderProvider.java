package net.vpc.app.nuts;

public class DefaultNutsClassLoaderProvider implements NutsClassLoaderProvider {

    public static final NutsClassLoaderProvider INSTANCE = new DefaultNutsClassLoaderProvider();

    @Override
    public ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
