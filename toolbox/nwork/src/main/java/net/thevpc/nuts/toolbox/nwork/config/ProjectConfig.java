package net.thevpc.nuts.toolbox.nwork.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ProjectConfig {

    private String id;
    private Set<String> technologies = new HashSet<>();
    private String path;
    private boolean zombie;
    private RepositoryAddress address;

    public Set<String> getTechnologies() {
        return technologies;
    }

    public ProjectConfig setTechnologies(Set<String> technologies) {
        this.technologies = technologies;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ProjectConfig setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isZombie() {
        return zombie;
    }

    public ProjectConfig setZombie(boolean zombie) {
        this.zombie = zombie;
        return this;
    }

    public RepositoryAddress getAddress() {
        return address;
    }

    public ProjectConfig setAddress(RepositoryAddress address) {
        this.address = address;
        return this;
    }

    public String getId() {
        return id;
    }

    public ProjectConfig setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectConfig that = (ProjectConfig) o;
        return zombie == that.zombie
                && Objects.equals(id, that.id)
                && Objects.equals(technologies, that.technologies)
                && Objects.equals(path, that.path)
                && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, technologies, path, zombie, address);
    }
}
