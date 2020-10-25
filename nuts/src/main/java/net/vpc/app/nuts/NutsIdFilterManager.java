package net.vpc.app.nuts;

import java.util.Set;

public interface NutsIdFilterManager extends NutsTypedFilters<NutsIdFilter> {
    NutsIdFilter byExpression(String expression);

    NutsIdFilter byDefaultVersion(Boolean defaultVersion);

    NutsIdFilter byInstallStatus(NutsInstallStatus... installStatus);

    NutsIdFilter byInstallStatus(Set<NutsInstallStatus>... installStatus);

    NutsIdFilter byName(String... names);
}
