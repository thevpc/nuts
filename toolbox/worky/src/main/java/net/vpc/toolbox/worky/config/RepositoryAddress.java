package net.vpc.toolbox.worky.config;

import java.util.Objects;

public class RepositoryAddress {
    private String nutsHome;
    private String nutsWorkspace;
    private String nutsRepository;

    public String getNutsHome() {
        return nutsHome;
    }

    public RepositoryAddress setNutsHome(String nutsHome) {
        this.nutsHome = nutsHome;
        return this;
    }

    public String getNutsWorkspace() {
        return nutsWorkspace;
    }

    public RepositoryAddress setNutsWorkspace(String nutsWorkspace) {
        this.nutsWorkspace = nutsWorkspace;
        return this;
    }

    public String getNutsRepository() {
        return nutsRepository;
    }

    public RepositoryAddress setNutsRepository(String nutsRepository) {
        this.nutsRepository = nutsRepository;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryAddress that = (RepositoryAddress) o;
        return Objects.equals(nutsHome, that.nutsHome) &&
                Objects.equals(nutsWorkspace, that.nutsWorkspace) &&
                Objects.equals(nutsRepository, that.nutsRepository);
    }

    @Override
    public int hashCode() {

        return Objects.hash(nutsHome, nutsWorkspace, nutsRepository);
    }
}
