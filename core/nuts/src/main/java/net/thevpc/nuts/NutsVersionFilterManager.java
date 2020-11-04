package net.thevpc.nuts;

public interface NutsVersionFilterManager extends NutsTypedFilters<NutsVersionFilter>{
    NutsVersionFilter byValue(String version);

    NutsVersionFilter byExpression(String expression);
}
