package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NFetchMode;
import net.thevpc.nuts.NRepository;

import java.util.Objects;

public class NRepositoryAndFetchMode {

    private NRepository repository;
    private NFetchMode fetchMode;

    public NRepositoryAndFetchMode(NRepository repository, NFetchMode fetchMode) {
        this.repository = repository;
        this.fetchMode = fetchMode;
    }

    public NRepository getRepository() {
        return repository;
    }

    public NFetchMode getFetchMode() {
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
        NRepositoryAndFetchMode that = (NRepositoryAndFetchMode) o;
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
