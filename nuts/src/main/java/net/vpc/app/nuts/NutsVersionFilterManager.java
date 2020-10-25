package net.vpc.app.nuts;

public interface NutsVersionFilterManager extends NutsTypedFilters<NutsVersionFilter>{
    NutsVersionFilter byValue(String version);

    NutsVersionFilter byExpression(String expression);
}
