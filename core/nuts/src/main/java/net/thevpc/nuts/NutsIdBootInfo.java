package net.thevpc.nuts;

import java.util.Arrays;

public class NutsIdBootInfo {
    private String id;
    private String[] dependencies;

    public NutsIdBootInfo(String id, String[] dependencies) {
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
        return "NutsIdBootInfo{" +
                "id='" + id + '\'' +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }
}
