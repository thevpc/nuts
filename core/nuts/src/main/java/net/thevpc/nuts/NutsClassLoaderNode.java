package net.thevpc.nuts;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

/**
 * @app.category Internal
 */
public class NutsClassLoaderNode {

    private final String id;
    private final boolean includedInClasspath;
    private final URL url;
    private final boolean enabled;
    private final NutsClassLoaderNode[] dependencies;

    public NutsClassLoaderNode(String id, URL url, boolean enabled, boolean includedInClasspath, NutsClassLoaderNode... dependencies) {
        this.id = id;
        this.url = url;
        this.enabled = enabled;
        this.includedInClasspath = includedInClasspath;
        this.dependencies = Arrays.copyOf(dependencies, dependencies.length);
    }

    public boolean isIncludedInClasspath() {
        return includedInClasspath;
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
        return "NutsClassLoaderNode{" +
                "id='" + id + '\'' +
                ", loaded=" + includedInClasspath +
                ", url=" + url +
                ", enabled=" + enabled +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsClassLoaderNode that = (NutsClassLoaderNode) o;
        return includedInClasspath == that.includedInClasspath && enabled == that.enabled && Objects.equals(id, that.id) && Objects.equals(url, that.url) && Arrays.equals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, includedInClasspath, url, enabled);
        result = 31 * result + Arrays.hashCode(dependencies);
        return result;
    }
}
