package net.thevpc.nuts;

import java.util.Arrays;

/**
 * @category Internal
 */
public class NutsBootDescriptor {
    private String id;
    private String[] dependencies;

    public NutsBootDescriptor(String id, String[] dependencies) {
        this.id = id;
        this.dependencies = dependencies;
    }

    public String getId() {
        return id;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "NutsBootDescriptor{" +
                "id='" + id + '\'' +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }
}
