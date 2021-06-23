package net.thevpc.nuts;

public interface NutsPathFactory{
    NutsSupplier<NutsPath> create(String path, NutsSession session, ClassLoader classLoader);
}
