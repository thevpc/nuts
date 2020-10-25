package net.vpc.app.nuts;

public interface NutsRepositoryFilterManager extends NutsTypedFilters<NutsRepositoryFilter>{
    NutsRepositoryFilter byName(String... names);
    NutsRepositoryFilter byUuid(String... uuids);
}
