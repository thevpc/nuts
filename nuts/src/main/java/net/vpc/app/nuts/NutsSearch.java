package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NutsSearch {

    private List<String> ids = new ArrayList<>();
    private TypedObject idFilter;
    private TypedObject dependencyFilter;
    private TypedObject repositoryFilter;
    private TypedObject versionFilter;
    private TypedObject descriptorFilter;
    private NutsDependencyScope scope = NutsDependencyScope.RUN;
    private boolean lastestVersions;
    private boolean sort = true;

    public NutsSearch(NutsSearch other) {
        copyFrom(other);
    }

    public NutsSearch copy() {
        return new NutsSearch(this);
    }

    public void copyFrom(NutsSearch other) {
        if (other != null) {
            ids.addAll(other.ids);
            idFilter = other.idFilter;
            dependencyFilter = other.dependencyFilter;
            repositoryFilter = other.repositoryFilter;
            versionFilter = other.versionFilter;
            descriptorFilter = other.descriptorFilter;
            scope = other.scope;
            lastestVersions = other.lastestVersions;
            sort = other.sort;
        }
    }

    public NutsSearch() {

    }

    public NutsSearch(String... ids) {
        addIds(ids);
    }

    public NutsSearch(NutsId... ids) {
        addIds(ids);
    }

    public boolean isSort() {
        return sort;
    }

    public NutsSearch setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public boolean isLastestVersions() {
        return lastestVersions;
    }

    public NutsSearch setLastestVersions(boolean lastestVersions) {
        this.lastestVersions = lastestVersions;
        return this;
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

    public NutsSearch setIds(String... ids) {
        this.ids.clear();
        addIds(ids);
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

//    public NutsSearch setDependencyFilter(TypedObject filter) {
//        if (filter == null) {
//            this.dependencyFilter = null;
//        } else if (NutsDependencyFilter.class.equals(filter.getType()) || String.class.equals(filter.getType())) {
//            this.dependencyFilter = filter;
//        } else {
//            throw new IllegalArgumentException("Invalid Object");
//        }
//        return this;
//    }
//
    public NutsSearch setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter == null ? null : new TypedObject(NutsDependencyFilter.class, filter, null);
        return this;
    }

    public TypedObject getDependencyFilter() {
        return dependencyFilter;
    }

    public NutsSearch setDependencyFilter(String filter) {
        this.dependencyFilter = filter == null ? null : new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter == null ? null : new TypedObject(NutsRepositoryFilter.class, filter, null);
        return this;
    }

    public TypedObject getRepositoryFilter() {
        return repositoryFilter;
    }

    public NutsSearch setRepositoryFilter(String filter) {
        this.repositoryFilter = filter == null ? null : new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setVersionFilter(NutsVersionFilter filter) {
        this.versionFilter = filter == null ? null : new TypedObject(NutsVersionFilter.class, filter, null);
        return this;
    }

    public TypedObject getVersionFilter() {
        return versionFilter;
    }

    public NutsSearch setVersionFilter(String filter) {
        this.versionFilter = filter == null ? null : new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter == null ? null : new TypedObject(NutsDescriptorFilter.class, filter, null);
        return this;
    }

    public TypedObject getDescriptorFilter() {
        return descriptorFilter;
    }

    public NutsSearch setDescriptorFilter(String filter) {
        this.descriptorFilter = filter == null ? null : new TypedObject(String.class, filter, null);
        return this;
    }

    public NutsSearch setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter == null ? null : new TypedObject(NutsIdFilter.class, filter, null);
        return this;
    }

    public TypedObject getIdFilter() {
        return idFilter;
    }

    public NutsSearch setIdFilter(String filter) {
        this.idFilter = filter == null ? null : new TypedObject(String.class, filter, null);
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.ids);
        hash = 41 * hash + Objects.hashCode(this.idFilter);
        hash = 41 * hash + Objects.hashCode(this.dependencyFilter);
        hash = 41 * hash + Objects.hashCode(this.repositoryFilter);
        hash = 41 * hash + Objects.hashCode(this.versionFilter);
        hash = 41 * hash + Objects.hashCode(this.descriptorFilter);
        hash = 41 * hash + Objects.hashCode(this.scope);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsSearch other = (NutsSearch) obj;
        if (!Objects.equals(this.ids, other.ids)) {
            return false;
        }
        if (!Objects.equals(this.idFilter, other.idFilter)) {
            return false;
        }
        if (!Objects.equals(this.dependencyFilter, other.dependencyFilter)) {
            return false;
        }
        if (!Objects.equals(this.repositoryFilter, other.repositoryFilter)) {
            return false;
        }
        if (!Objects.equals(this.versionFilter, other.versionFilter)) {
            return false;
        }
        if (!Objects.equals(this.descriptorFilter, other.descriptorFilter)) {
            return false;
        }
        if (this.scope != other.scope) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSearch{");
        sb.append(scope);
        if (ids != null && ids.size() > 0) {
            sb.append(",ids=" + ids);
        }
        if (idFilter != null) {
            sb.append(",idFilter=" + idFilter);
        }
        if (dependencyFilter != null) {
            sb.append(",dependencyFilter=" + dependencyFilter);
        }
        if (repositoryFilter != null) {
            sb.append(",repositoryFilter=" + repositoryFilter);
        }
        if (versionFilter != null) {
            sb.append(",versionFilter=" + versionFilter);
        }
        if (descriptorFilter != null) {
            sb.append(",descriptorFilter=" + descriptorFilter);
        }
        sb.append('}');
        return sb.toString();
    }

}
