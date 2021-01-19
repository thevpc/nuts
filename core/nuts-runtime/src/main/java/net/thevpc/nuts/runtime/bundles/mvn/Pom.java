package net.thevpc.nuts.runtime.bundles.mvn;

import java.util.Map;
import org.w3c.dom.Document;

public class Pom {

    String groupId;
    String artifactId;
    String version;
    String packaging;
    String name;
    String url;
    String inceptionYear;
    String description;
    Map<String, String> properties;
    PomDependency[] dependencies;
    PomDependency[] dependenciesManagement;
    PomId parent;
    PomRepository[] repositories;
    PomRepository[] pluginRepositories;
    String[] modules;
    Document xml;

    public Pom(String groupId, String artifactId, String version, String packaging,
            PomId parent,
            String name, String desc,
            String url, String inceptionYear,
            Map<String, String> properties,
            PomDependency[] dependencies,
            PomDependency[] dependenciesManagement,
            PomRepository[] repositories, PomRepository[] pluginRepositories,
            String[] modules, Document xml
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
        this.xml = xml;
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

    public Pom setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Pom setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public Pom setVersion(String version) {
        this.version = version;
        return this;
    }

    public Pom setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public Pom setName(String name) {
        this.name = name;
        return this;
    }

    public Pom setDescription(String description) {
        this.description = description;
        return this;
    }

    public Pom setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public Pom setDependencies(PomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Pom setParent(PomId parent) {
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

    public PomDependency[] getDependencies() {
        return dependencies;
    }

    public PomId getParent() {
        return parent;
    }

    public PomRepository[] getRepositories() {
        return repositories;
    }

    public Pom setRepositories(PomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public PomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public Pom setPluginRepositories(PomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public PomId getPomId() {
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
        return new PomId(g, artifactId, v);
    }

    public PomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public Pom setDependenciesManagement(PomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Pom setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getInceptionYear() {
        return inceptionYear;
    }

    public Pom setInceptionYear(String inceptionYear) {
        this.inceptionYear = inceptionYear;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public Pom setModules(String[] modules) {
        this.modules = modules;
        return this;
    }
}
