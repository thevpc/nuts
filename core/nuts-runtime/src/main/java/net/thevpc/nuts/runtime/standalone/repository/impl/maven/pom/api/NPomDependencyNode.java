package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

public interface NPomDependencyNode extends NPomNode {
    NPomDependency getObject();

    void setVersion(String version);

    void setGroupId(String version);

    void setArtifactId(String version);
    void setClassifier(String version);
    void setOptional(String version);
}
