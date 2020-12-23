package net.thevpc.nuts;

import java.net.URL;

/**
 * @category Boot
 */
public class NutsBootDependencyNode {
    private String id;
    private URL url;
    private NutsBootDependencyNode[] dependencies;

    public NutsBootDependencyNode(String id, URL url, NutsBootDependencyNode... dependencies) {
        this.id = id;
        this.url = url;
        this.dependencies = dependencies;
    }

    public String getId() {
        return id;
    }

    public URL getURL() {
        return url;
    }

    public NutsBootDependencyNode[] getDependencies() {
        return dependencies;
    }
}
