package net.thevpc.nuts;

public interface NutsVersionManager {
    NutsVersionParser parser();

    NutsVersionFormat formatter();

    NutsVersionFormat formatter(NutsVersion version);

    NutsVersionFilterManager filter();

}
