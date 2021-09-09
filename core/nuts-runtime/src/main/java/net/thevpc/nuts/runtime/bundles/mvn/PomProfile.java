package net.thevpc.nuts.runtime.bundles.mvn;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class PomProfile {

    private String id;
    private PomProfileActivation activation;
    private Map<String, String> properties;
    private PomDependency[] dependencies;
    private PomDependency[] dependenciesManagement;
    private PomRepository[] repositories;
    private PomRepository[] pluginRepositories;
    private String[] modules;

    public PomProfile() {
    }

    public String getId() {
        return id;
    }

    public PomProfile setId(String id) {
        this.id = id;
        return this;
    }

    public PomProfileActivation getActivation() {
        return activation;
    }

    public PomProfile setActivation(PomProfileActivation activation) {
        this.activation = activation;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public PomProfile setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public PomDependency[] getDependencies() {
        return dependencies;
    }

    public PomProfile setDependencies(PomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public PomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public PomProfile setDependenciesManagement(PomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public PomRepository[] getRepositories() {
        return repositories;
    }

    public PomProfile setRepositories(PomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public PomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public PomProfile setPluginRepositories(PomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public PomProfile setModules(String[] modules) {
        this.modules = modules;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomProfile that = (PomProfile) o;
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
