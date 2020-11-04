package net.thevpc.nuts;

public interface NutsRepositoryFilterManager extends NutsTypedFilters<NutsRepositoryFilter>{
    NutsRepositoryFilter byName(String... names);
    NutsRepositoryFilter byUuid(String... uuids);
}
