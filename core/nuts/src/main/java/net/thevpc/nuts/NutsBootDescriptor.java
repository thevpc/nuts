package net.thevpc.nuts;

import java.util.Arrays;

/**
 * @app.category Internal
 */
public class NutsBootDescriptor {
    private NutsBootId id;
    private NutsBootId[] dependencies;

    public NutsBootDescriptor(NutsBootId id, NutsBootId[] dependencies) {
        this.id = id;
        this.dependencies = dependencies;
    }

    public NutsBootId getId() {
        return id;
    }

    public NutsBootId[] getDependencies() {
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
