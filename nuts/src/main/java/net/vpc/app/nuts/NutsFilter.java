package net.vpc.app.nuts;

/**
 * Top Level filter
 */
public interface NutsFilter {

    NutsFilterOp getFilterOp();

    default Class<? extends NutsFilter> getFilterType() {
        return getWorkspace().filters().detectType(this);
    }

    NutsWorkspace getWorkspace();

    NutsFilter simplify();

    default <T extends NutsFilter> NutsFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    default NutsFilter or(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().any(this, other);
    }

    default NutsFilter and(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().all(this, other);
    }

    default NutsFilter neg() {
        return getWorkspace().filters().not(this);
    }

    default <T extends NutsFilter> T to(Class<T> type) {
        return getWorkspace().filters().to(type, this);
    }

    NutsFilter[] getSubFilters();
}
