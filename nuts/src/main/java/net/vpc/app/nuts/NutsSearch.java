package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface NutsSearch {


    String[] getIds();

    NutsRepositoryFilter getRepositoryFilter();

    NutsVersionFilter getVersionFilter();

    boolean isSort();

    NutsIdFilter getIdFilter();

    boolean isLatestVersions();

    NutsDescriptorFilter getDescriptorFilter();
}
