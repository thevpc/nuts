package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Map;

import org.w3c.dom.Document;

public class NutsPom {

    String groupId;
    String artifactId;
    String version;
    String packaging;
    String name;
    String url;
    String inceptionYear;
    String description;
    Map<String, String> properties;
    NutsPomDependency[] dependencies;
    NutsPomDependency[] dependenciesManagement;
    NutsPomId parent;
    NutsPomRepository[] repositories;
    NutsPomRepository[] pluginRepositories;
    NutsPomProfile[] profiles;
    String[] modules;
    Document xml;

    public NutsPom(String groupId, String artifactId, String version, String packaging,
                   NutsPomId parent,
                   String name, String desc,
                   String url, String inceptionYear,
                   Map<String, String> properties,
                   NutsPomDependency[] dependencies,
                   NutsPomDependency[] dependenciesManagement,
                   NutsPomRepository[] repositories, NutsPomRepository[] pluginRepositories,
                   String[] modules, NutsPomProfile[] profiles, Document xml
    ) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.properties = properties;
        this.dependencies = dependencies;
        this.dependenciesManagement = dependenciesManagement;
        this.name = name;
        this.description = desc;
        this.url = url;
        this.inceptionYear = inceptionYear;
        this.packaging = packaging;
        this.parent = parent;
        this.repositories = repositories;
        this.pluginRepositories = pluginRepositories;
        this.modules = modules;
        this.profiles = profiles;
        this.xml = xml;
    }

    public NutsPomProfile[] getProfiles() {
        return profiles;
    }

    public Document getXml() {
        return xml;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getPackaging() {
        return packaging;
    }

    public NutsPom setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NutsPom setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public NutsPom setVersion(String version) {
        this.version = version;
        return this;
    }

    public NutsPom setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public NutsPom setName(String name) {
        this.name = name;
        return this;
    }

    public NutsPom setDescription(String description) {
        this.description = description;
        return this;
    }

    public NutsPom setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public NutsPom setDependencies(NutsPomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public NutsPom setParent(NutsPomId parent) {
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NutsPomDependency[] getDependencies() {
        return dependencies;
    }

    public NutsPomId getParent() {
        return parent;
    }

    public NutsPomRepository[] getRepositories() {
        return repositories;
    }

    public NutsPom setRepositories(NutsPomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public NutsPomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public NutsPom setPluginRepositories(NutsPomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public NutsPomId getPomId() {
        String g = groupId;
        String v = version;
        if (parent != null) {
            if (g == null || g.isEmpty() || "${groupId}".equals(g)) {
                g = parent.getGroupId();
            }
            if (v == null || v.isEmpty() || "${version}".equals(v)) {
                v = parent.getVersion();
            }
        }
        return new NutsPomId(g, artifactId, v);
    }

    public NutsPomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public NutsPom setDependenciesManagement(NutsPomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NutsPom setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getInceptionYear() {
        return inceptionYear;
    }

    public NutsPom setInceptionYear(String inceptionYear) {
        this.inceptionYear = inceptionYear;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public NutsPom setModules(String[] modules) {
        this.modules = modules;
        return this;
    }
}
