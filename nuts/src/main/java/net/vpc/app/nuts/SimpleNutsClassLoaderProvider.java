package net.vpc.app.nuts;

public class SimpleNutsClassLoaderProvider implements NutsClassLoaderProvider {
    private ClassLoader classLoader;

    public SimpleNutsClassLoaderProvider(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getContextClassLoader() {
        return classLoader;
    }
}
