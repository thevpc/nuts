package net.vpc.app.nuts;

public interface NutsVersionManager {
    NutsVersionParser parser();

    NutsVersionFormat formatter();

    NutsVersionFormat formatter(NutsVersion version);

    NutsVersionFilterManager filter();

}
