package net.vpc.app.nuts;

public interface NutsLogManager {
    NutsLogger of(String name);

    NutsLogger of(Class clazz);
}
