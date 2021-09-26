package net.thevpc.nuts.boot;

import java.util.Arrays;

/**
 * @app.category Internal
 */
public class NutsBootDescriptor {
    private final NutsBootId id;
    private final NutsBootId[] dependencies;

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
