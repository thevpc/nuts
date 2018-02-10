package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NutsDependencySearch {

    private List<String> ids = new ArrayList<>();
    private boolean includeMain = false;
    private boolean trackNotFound = false;
    private TypedObject dependencyFilter;
    private NutsDependencyScope scope = NutsDependencyScope.RUN;
    private Set<NutsId> noFoundResult = new HashSet<>();

    public NutsDependencySearch() {

    }

    public NutsDependencySearch(String... ids) {
        addIds(ids);
    }

    public NutsDependencySearch(NutsId... ids) {
        addIds(ids);
    }

    public NutsDependencySearch addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    public NutsDependencySearch addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    public NutsDependencySearch addId(String id) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
        return this;
    }

    public String[] getIds() {
        return this.ids.toArray(new String[this.ids.size()]);
    }

    public boolean isIncludeMain() {
        return includeMain;
    }

    public NutsDependencySearch setIncludeMain(boolean includeMain) {
        this.includeMain = includeMain;
        return this;
    }

    public NutsDependencySearch setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = new TypedObject(NutsDependencyFilter.class, filter, null);
        return this;
    }

    public NutsDependencyScope getScope() {
        return scope;
    }

    public NutsDependencySearch setScope(NutsDependencyScope scope) {
        this.scope = scope;
        return this;
    }

    public TypedObject getDependencyFilter() {
        return dependencyFilter;
    }

    public NutsDependencySearch setDependencyFilter(String filter) {
        this.dependencyFilter = new TypedObject(String.class, filter, null);
        return this;
    }

    public boolean isTrackNotFound() {
        return trackNotFound;
    }

    public NutsDependencySearch setTrackNotFound(boolean trackNotFound) {
        this.trackNotFound = trackNotFound;
        return this;
    }

    public Set<NutsId> getNoFoundResult() {
        return noFoundResult;
    }
}
