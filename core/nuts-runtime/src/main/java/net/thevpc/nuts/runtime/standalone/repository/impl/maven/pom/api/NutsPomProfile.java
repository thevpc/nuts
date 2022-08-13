package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class NutsPomProfile {

    private String id;
    private NutsPomProfileActivation activation;
    private Map<String, String> properties;
    private NutsPomDependency[] dependencies;
    private NutsPomDependency[] dependenciesManagement;
    private NutsPomRepository[] repositories;
    private NutsPomRepository[] pluginRepositories;
    private String[] modules;

    public NutsPomProfile() {
    }

    public String getId() {
        return id;
    }

    public NutsPomProfile setId(String id) {
        this.id = id;
        return this;
    }

    public NutsPomProfileActivation getActivation() {
        return activation;
    }

    public NutsPomProfile setActivation(NutsPomProfileActivation activation) {
        this.activation = activation;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NutsPomProfile setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public NutsPomDependency[] getDependencies() {
        return dependencies;
    }

    public NutsPomProfile setDependencies(NutsPomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public NutsPomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public NutsPomProfile setDependenciesManagement(NutsPomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public NutsPomRepository[] getRepositories() {
        return repositories;
    }

    public NutsPomProfile setRepositories(NutsPomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public NutsPomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public NutsPomProfile setPluginRepositories(NutsPomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public NutsPomProfile setModules(String[] modules) {
        this.modules = modules;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPomProfile that = (NutsPomProfile) o;
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
