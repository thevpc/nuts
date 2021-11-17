package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NutsFetchMode;
import net.thevpc.nuts.NutsRepository;

import java.util.Objects;

public class NutsRepositoryAndFetchMode {

    private NutsRepository repository;
    private NutsFetchMode fetchMode;

    public NutsRepositoryAndFetchMode(NutsRepository repository, NutsFetchMode fetchMode) {
        this.repository = repository;
        this.fetchMode = fetchMode;
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NutsRepositoryAndFetchMode that = (NutsRepositoryAndFetchMode) o;
        return Objects.equals(repository, that.repository) && fetchMode == that.fetchMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, fetchMode);
    }

    @Override
    public String toString() {
        return fetchMode.id() + "(" + repository + ')';
    }

}
