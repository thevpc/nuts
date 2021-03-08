package net.thevpc.nuts;

import java.net.URL;
import java.util.Arrays;

/**
 * @category Internal
 */
public class NutsClassLoaderNode {

    private String id;
    private URL url;
    private boolean enabled;
    private NutsClassLoaderNode[] dependencies;

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
