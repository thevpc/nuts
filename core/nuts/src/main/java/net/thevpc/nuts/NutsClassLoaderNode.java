package net.thevpc.nuts;

import java.net.URL;
import java.util.Arrays;

/**
 * @app.category Internal
 */
public class NutsClassLoaderNode {

    private final String id;
    private final URL url;
    private final boolean enabled;
    private final NutsClassLoaderNode[] dependencies;

    public NutsClassLoaderNode(String id, URL url, boolean enabled, NutsClassLoaderNode... dependencies) {
        this.id = id;
        this.url = url;
        this.enabled = enabled;
        this.dependencies = Arrays.copyOf(dependencies, dependencies.length);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return id;
    }

    public URL getURL() {
        return url;
    }

    public NutsClassLoaderNode[] getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "NutsClassLoaderNode{" + "id=" + id + ", url=" + url + ", enabled=" + enabled + ", dependencies=" + dependencies + '}';
    }

}
