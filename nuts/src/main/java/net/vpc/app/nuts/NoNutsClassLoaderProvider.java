package net.vpc.app.nuts;

public class NoNutsClassLoaderProvider implements NutsClassLoaderProvider {

    public static final NutsClassLoaderProvider INSTANCE = new NoNutsClassLoaderProvider();

    @Override
    public ClassLoader getContextClassLoader() {
        return null;
    }
}
