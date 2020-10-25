package net.vpc.app.nuts;

import java.util.Set;

public interface NutsImportManager {
    void add(String[] importExpression, NutsAddOptions options);

    void removeAll(NutsRemoveOptions options);

    void remove(String[] importExpression, NutsRemoveOptions options);

    void set(String[] imports, NutsUpdateOptions options);

    Set<String> getAll();
}
