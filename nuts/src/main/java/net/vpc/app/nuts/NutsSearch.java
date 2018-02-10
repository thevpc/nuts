package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;

public class NutsSearch {

    private List<String> ids = new ArrayList<>();
//    private boolean includeDependencies;
    private TypedObject idFilter;
    private TypedObject dependencyFilter;
    private TypedObject repositoryFilter;
    private TypedObject versionFilter;
    private TypedObject descriptorFilter;
    private NutsDependencyScope scope = NutsDependencyScope.RUN;

    public NutsSearch() {

    }

    public NutsSearch(String... ids) {
        addIds(ids);
    }

    public NutsSearch(NutsId... ids) {
        addIds(ids);
    }

    public NutsSearch addIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    public NutsSearch addIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id == null ? null : id.toString());
            }
        }
        return this;
    }

    public NutsSearch addId(String id) {
        if (id != null && !id.isEmpty()) {
            ids.add(id);
        }
        return this;
    }

    public String[] getIds() {
        return this.ids.toArray(new String[this.ids.size()]);
    }

    public NutsDependencyScope getScope() {
        return scope;
    }

    public NutsSearch setScope(NutsDependencyScope scope) {
        this.scope = scope;
        return this;
    }

    public NutsSearch setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = new TypedObject(NutsDependencyFilter.class, filter, null);
        return this;
    }

    public TypedObject getDependencyFilter() {
        return dependencyFilter;
    }

    public NutsSearch setDependencyFilter(String filter) {
        this.dependencyFilter = new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = new TypedObject(NutsRepositoryFilter.class, filter, null);
        return this;
    }

    public TypedObject getRepositoryFilter() {
        return repositoryFilter;
    }

    public NutsSearch setRepositoryFilter(String filter) {
        this.repositoryFilter = new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setVersionFilter(NutsVersionFilter filter) {
        this.versionFilter = new TypedObject(NutsVersionFilter.class, filter, null);
        return this;
    }

    public TypedObject getVersionFilter() {
        return versionFilter;
    }

    public NutsSearch setVersionFilter(String filter) {
        this.versionFilter = new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = new TypedObject(NutsDescriptorFilter.class, filter, null);
        return this;
    }

    public TypedObject getDescriptorFilter() {
        return descriptorFilter;
    }

    public NutsSearch setDescriptorFilter(String filter) {
        this.descriptorFilter = new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setIdFilter(NutsIdFilter filter) {
        this.idFilter = new TypedObject(NutsIdFilter.class, filter, null);
        return this;
    }

    public TypedObject getIdFilter() {
        return idFilter;
    }

    public NutsSearch setIdFilter(String filter) {
        this.idFilter = new TypedObject(String.class, filter, null);
        return this;
    }

//    public boolean isIncludeDependencies() {
//        return includeDependencies;
//    }
//
//    public NutsSearch setIncludeDependencies(boolean includeDependencies) {
//        this.includeDependencies = includeDependencies;
//        return this;
//    }
}
