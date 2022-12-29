package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class NPomProfile {

    private String id;
    private NPomProfileActivation activation;
    private Map<String, String> properties;
    private NPomDependency[] dependencies;
    private NPomDependency[] dependenciesManagement;
    private NPomRepository[] repositories;
    private NPomRepository[] pluginRepositories;
    private String[] modules;

    public NPomProfile() {
    }

    public String getId() {
        return id;
    }

    public NPomProfile setId(String id) {
        this.id = id;
        return this;
    }

    public NPomProfileActivation getActivation() {
        return activation;
    }

    public NPomProfile setActivation(NPomProfileActivation activation) {
        this.activation = activation;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NPomProfile setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public NPomDependency[] getDependencies() {
        return dependencies;
    }

    public NPomProfile setDependencies(NPomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public NPomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public NPomProfile setDependenciesManagement(NPomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public NPomRepository[] getRepositories() {
        return repositories;
    }

    public NPomProfile setRepositories(NPomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public NPomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public NPomProfile setPluginRepositories(NPomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public NPomProfile setModules(String[] modules) {
        this.modules = modules;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NPomProfile that = (NPomProfile) o;
        return Objects.equals(id, that.id) && Objects.equals(activation, that.activation) && Objects.equals(properties, that.properties) && Arrays.equals(dependencies, that.dependencies) && Arrays.equals(dependenciesManagement, that.dependenciesManagement) && Arrays.equals(repositories, that.repositories) && Arrays.equals(pluginRepositories, that.pluginRepositories) && Arrays.equals(modules, that.modules);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, activation, properties);
        result = 31 * result + Arrays.hashCode(dependencies);
        result = 31 * result + Arrays.hashCode(dependenciesManagement);
        result = 31 * result + Arrays.hashCode(repositories);
        result = 31 * result + Arrays.hashCode(pluginRepositories);
        result = 31 * result + Arrays.hashCode(modules);
        return result;
    }
}
